/*
 * Licensed to the Sakai Foundation (SF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The SF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.sakaiproject.nakamura.impl.morphia;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.Morphia;
import com.google.code.morphia.mapping.MappedClass;
import com.google.code.morphia.mapping.MapperOptions;

import com.mongodb.Mongo;
import com.mongodb.MongoException;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.sakaiproject.nakamura.api.morphia.MorphiaDatastoreProvider;
import org.sakaiproject.nakamura.api.storage.Entity;
import org.sakaiproject.nakamura.api.storage.StorageEventUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 */
@Service
@Component(immediate=true, metatype=true)
public class MorphiaDatastoreProviderImpl implements MorphiaDatastoreProvider, EventHandler {
  private final static Logger LOGGER = LoggerFactory.getLogger(MorphiaDatastoreProviderImpl.class);
  
  public final static String DEFAULT_DB_HOSTNAME = "localhost";
  public final static int DEFAULT_DB_PORT = 27017;
  public final static String DEFAULT_DB_NAME = "oae";
  
  @Property(label="Host", description="Hostname of the MongoDB database", value=DEFAULT_DB_HOSTNAME)
  public final static String PROP_DB_HOSTNAME = "host";
  
  @Property(label="Port", description="Port of the MongoDB database", intValue=DEFAULT_DB_PORT)
  public final static String PROP_DB_PORT = "port";
  
  @Property(label="DB Name", description="Name of the MongoDB database", value=DEFAULT_DB_NAME)
  public final static String PROP_DB_NAME = "db_name";
  
  @Property(value = { StorageEventUtil.TOPIC_REFRESH_ALL_DEFAULT }, propertyPrivate = true)
  static final String TOPICS = EventConstants.EVENT_TOPIC;
  
  @Reference
  EventAdmin eventAdmin;
  
  private Morphia morphia;
  
  private Datastore datastore;
  
  public MorphiaDatastoreProviderImpl() {
    
  }
  
  public MorphiaDatastoreProviderImpl(String host, int port, String dbName) {
    host = (host == null) ? DEFAULT_DB_HOSTNAME : host;
    port = (port == -1) ? DEFAULT_DB_PORT : port;
    dbName = (dbName == null) ? DEFAULT_DB_NAME+"-test" : dbName;
    
    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put(PROP_DB_HOSTNAME, host);
    properties.put(PROP_DB_PORT, port);
    properties.put(PROP_DB_NAME, dbName);
    
    try {
      activate(properties);
    } catch (UnknownHostException e) {
      throw new RuntimeException(e);
    } catch (MongoException e) {
      throw new RuntimeException(e);
    }
  }
  
  @Activate
  public void activate(Map<String, Object> properties) throws UnknownHostException,
      MongoException {
    String host = (String) properties.get(PROP_DB_HOSTNAME);
    Integer port = (Integer) properties.get(PROP_DB_PORT);
    
    Mongo mongo = new Mongo(host, port);
    String dbName = (String) properties.get(PROP_DB_NAME);
    
    // try and acquire the database as a sanity check
    mongo.getDB(dbName);
    
    // we will have to force the object factory to use the bundle's class loader. Basically,
    // and class loader that has dynamic access to all public entities in the container will
    // do.
    MapperOptions mapperOptions = new MapperOptions();
    mapperOptions.objectFactory = new BundleObjectFactory(getClass().getClassLoader());
    
    morphia = new Morphia();
    morphia.getMapper().setOptions(mapperOptions);
    datastore = morphia.createDatastore(mongo, dbName);
  }
  
  @Deactivate
  public void deactivate() {
    try {
      datastore.getMongo().close();
    } finally {
      datastore = null;
      morphia = null;
    }
  }
  
  /**
   * {@inheritDoc}
   * @see org.sakaiproject.nakamura.api.morphia.MorphiaDatastoreProvider#datastore()
   */
  @Override
  public Datastore datastore() {
    return datastore;
  }

  /**
   * {@inheritDoc}
   * @see org.sakaiproject.nakamura.api.morphia.MorphiaDatastoreProvider#map(java.lang.Class)
   */
  @Override
  public void map(Class<?> clazz) {
    if (morphia.isMapped(clazz)) {
      LOGGER.info("Skipping mapping of already-mapped class {}", clazz.getCanonicalName());
      return;
    }
    LOGGER.info("Mapping Morphia class {}", clazz.getCanonicalName());
    morphia.map(clazz);
    LOGGER.info("Ensuring indexes...");
    datastore.ensureIndexes();
    LOGGER.info("Ensuring caps...");
    datastore.ensureCaps();
  }

  /**
   * {@inheritDoc}
   * @see org.osgi.service.event.EventHandler#handleEvent(org.osgi.service.event.Event)
   */
  @Override
  public void handleEvent(Event event) {
    // grab all entities from all mapped classes
    for (MappedClass mappedClass : morphia.getMapper().getMappedClasses()) {
      Class<?> clazz = mappedClass.getClazz();
      // we actually only care about Entity implementations. Others are not compatable with the ContentEventListener.
      // TODO: If we go forward with mongo / morphia, we should write something that is compatible with 
      // mongo objects.
      Set<Class<?>> interfacesSet = new HashSet<Class<?>>(Arrays.asList(clazz.getInterfaces()));
      if (interfacesSet.contains(Entity.class)) {
        for (Object obj : datastore.createQuery(clazz)) {
          Entity entity = (Entity) obj;
          Event storageEvent = StorageEventUtil.createStorageEvent(StorageEventUtil.TOPIC_REFRESH_DEFAULT,
              (String) event.getProperty(StorageEventUtil.FIELD_ACTOR_USER_ID), entity, null);
          eventAdmin.sendEvent(storageEvent);
        }
      }
    }
  }

}
