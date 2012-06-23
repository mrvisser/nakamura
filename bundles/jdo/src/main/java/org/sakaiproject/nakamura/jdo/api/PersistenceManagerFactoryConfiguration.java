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
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
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

  @Reference
  DynamicClassLoader dynamicClassLoader;
  
  @Activate
  public void activate(BundleContext context, Map<String, Object> config) {
    LOGGER.info("activate()");
    super.pmf = JDOPersistenceManagerFactory
        .getPersistenceManagerFactory(getJdoProps(config));
  }
  
  public Map<String, Object> getJdoProps(Map<String, Object> config) {
    Map<String, Object> props = new HashMap<String, Object>();
    props.put("datanucleus.ConnectionDriverName", "com.mysql.jdbc.Driver");
    props.put("datanucleus.ConnectionURL", "jdbc:mysql://localhost:3306/oae?profileSQL=true");
    props.put("datanucleus.ConnectionUserName", "oae");
    props.put("datanucleus.ConnectionPassword", "oae");
    props.put("datanucleus.storeManagerType", "rdbms");
    props.put("datanucleus.autoCreateSchema", "true");
    props.put("datanucleus.validateTables", "true");
    props.put("datanucleus.validateColumns", "true");
    props.put("datanucleus.validateConstraints", "true");
    props.put("datanucleus.rdbms.CheckExistTablesOrViews", "true");
    props.put("datanucleus.plugin.pluginRegistryClassName", "org.datanucleus.plugin.OSGiPluginRegistry");
    props.put("datanucleus.primaryClassLoader", getClass().getClassLoader());
    props.put("datanucleus.metadata.allowXML", "false");
    return props;
  }
  
  @Deactivate
  public void deactivate(BundleContext context) {
    if (super.pmf != null) {
      super.pmf.close();
      super.pmf = null;
    }
  }

}
