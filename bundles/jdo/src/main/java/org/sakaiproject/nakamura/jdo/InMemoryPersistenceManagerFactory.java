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
package org.sakaiproject.nakamura.jdo;

import org.datanucleus.api.jdo.JDOPersistenceManagerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class InMemoryPersistenceManagerFactory extends
    DelegatingPersistenceManagerFactory {
  private static final long serialVersionUID = 1L;

  protected final String dbName;
  
  public InMemoryPersistenceManagerFactory() {
    this(String.valueOf(System.currentTimeMillis()));
  }
  
  public InMemoryPersistenceManagerFactory(String dbName) {
    this.dbName = dbName;
    
    // build the in-memory properties
    Map<String, Object> props = new HashMap<String, Object>();
    props.put("datanucleus.ConnectionDriverName", "org.apache.derby.jdbc.EmbeddedDriver");
    props.put("datanucleus.ConnectionURL", "jdbc:derby:memory:"+dbName+";create=true");
    props.put("datanucleus.ConnectionUserName", "sa");
    props.put("datanucleus.ConnectionPassword", "");
    props.put("datanucleus.storeManagerType", "rdbms");
    props.put("datanucleus.autoCreateSchema", "true");
    props.put("datanucleus.validateTables", "true");
    props.put("datanucleus.validateColumns", "true");
    props.put("datanucleus.validateConstraints", "true");
    props.put("datanucleus.rdbms.CheckExistTablesOrViews", "true");
    props.put("datanucleus.Optimistic", "true");
    
    this.pmf = JDOPersistenceManagerFactory.getPersistenceManagerFactory(props);
  }

}
