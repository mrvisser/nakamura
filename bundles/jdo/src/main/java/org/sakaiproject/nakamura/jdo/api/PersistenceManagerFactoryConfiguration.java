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
package org.sakaiproject.nakamura.jdo.api;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.datanucleus.api.jdo.JDOPersistenceManagerFactory;
import org.osgi.framework.BundleContext;
import org.sakaiproject.nakamura.jdo.DelegatingPersistenceManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import javax.jdo.PersistenceManagerFactory;

/**
 * 
 */
@Service(value=PersistenceManagerFactory.class)
@Component(metatype=true, immediate=true)
public class PersistenceManagerFactoryConfiguration extends DelegatingPersistenceManagerFactory {
  private static final long serialVersionUID = 1L;
  
  private static Logger LOGGER = LoggerFactory.getLogger(PersistenceManagerFactoryConfiguration.class);

  private final static String DEFAULT_CONNECTION_DRIVER_NAME = "org.apache.derby.jdbc.EmbeddedDriver";
  private final static String DEFAULT_CONNECTION_URL = "jdbc:derby:directory:sling/db;create=true";
  private final static String DEFAULT_USERNAME = "sa";
  private final static String DEFAULT_PASSWORD = "";
  
  @Property(label="Driver", description="e.g., com.mysql.jdbc.Driver", value=DEFAULT_CONNECTION_DRIVER_NAME)
  private final static String PROP_CONNECTION_DRIVER_NAME = "org.sakaiproject.nakamura.jdo.PROP_CONNECTION_DRIVER_NAME";

  @Property(label="Connection String", description="e.g., jdbc:mysql://localhost:3306/oae",
      value=DEFAULT_CONNECTION_URL)
  private final static String PROP_CONNECTION_URL = "org.sakaiproject.nakamura.jdo.PROP_CONNECTION_URL";

  @Property(label="Username", value=DEFAULT_USERNAME)
  private final static String PROP_USERNAME = "org.sakaiproject.nakamura.jdo.PROP_USERNAME";

  @Property(label="Password", value=DEFAULT_PASSWORD)
  private final static String PROP_PASSWORD = "org.sakaiproject.nakamura.jdo.PROP_PASSWORD";
  
  @Reference(cardinality=ReferenceCardinality.OPTIONAL_UNARY, policy=ReferencePolicy.DYNAMIC, target="(type=global)")
  ClassLoader dynamicClassLoader;
  
  @Activate
  void activate(Map<String, Object> properties) {
    if (dynamicClassLoader == null) {
      throw new IllegalStateException("Could not find the dynamic global classloader " +
      		"(is org.sakaiproject.nakamura.classloader deployed? It is required.)");
    }
    
    super.pmf = JDOPersistenceManagerFactory.getPersistenceManagerFactory(getJdoProps(properties));
  }
  
  private Map<String, Object> getJdoProps(Map<String, Object> config) {
    String driver = PropertiesUtil.toString(config.get(PROP_CONNECTION_DRIVER_NAME), DEFAULT_CONNECTION_DRIVER_NAME);
    String url = PropertiesUtil.toString(config.get(PROP_CONNECTION_URL), DEFAULT_CONNECTION_URL);
    String username = PropertiesUtil.toString(config.get(PROP_USERNAME), DEFAULT_USERNAME);
    String password = PropertiesUtil.toString(config.get(PROP_PASSWORD), DEFAULT_PASSWORD);
    
    LOGGER.info("Database connection info:");
    LOGGER.info("\tDriver: {}", driver);
    LOGGER.info("\tConnection String: {}", url);
    LOGGER.info("\tUsername: {}", username);
    
    Map<String, Object> props = new HashMap<String, Object>();
    props.put("datanucleus.ConnectionDriverName", driver);
    props.put("datanucleus.ConnectionURL", url);
    props.put("datanucleus.ConnectionUserName", username);
    props.put("datanucleus.ConnectionPassword", password);
    props.put("datanucleus.storeManagerType", "rdbms");
    props.put("datanucleus.autoCreateSchema", "true");
    props.put("datanucleus.validateTables", "true");
    props.put("datanucleus.validateColumns", "true");
    props.put("datanucleus.validateConstraints", "true");
    props.put("datanucleus.rdbms.CheckExistTablesOrViews", "true");
    props.put("datanucleus.plugin.pluginRegistryClassName", "org.datanucleus.plugin.OSGiPluginRegistry");
    
    // we pass in an "all-seeing" class loader to which we have no static wire to. This is
    // important because it keeps us from having a hard wire to all bundles that provide model
    // objects. This stops the Nakamura JDO bundle from being refreshed (and subsequently many
    // other bundles in the container) when one bundle that provides a model object is reloaded.
    props.put("datanucleus.primaryClassLoader", dynamicClassLoader);
    
    props.put("datanucleus.metadata.allowXML", "false");
    return props;
  }
  
  @Deactivate
  void deactivate(BundleContext context) {
    if (super.pmf != null) {
      super.pmf.close();
      super.pmf = null;
    }
  }
}
