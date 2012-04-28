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
package org.sakaiproject.nakamura.activity.jdbc;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DataSources;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
@Component(immediate=true, metatype=true)
@Service
public class ActivityJdbcConnectionPool {
  
  private static final Logger LOGGER = LoggerFactory.getLogger(ActivityJdbcConnectionPool.class);
  
  public static final String DEFAULT_DRIVER = "jdbc:derby:sling/sparsemap/db;create=true";
  public static final String DEFAULT_URL = "org.apache.derby.jdbc.EmbeddedDriver";
  public static final String DEFAULT_USERNAME = "sa";
  public static final String DEFAULT_PASSWORD = "";
  
  @Property(value = { DEFAULT_DRIVER })
  public static final String PROPKEY_DRIVER = "org.sakaiproject.nakamura.activity.jdbc_driver";
  
  @Property(value = { DEFAULT_URL })
  public static final String PROPKEY_URL = "org.sakaiproject.nakamura.activity.jdbc_url";
  
  @Property(value = { DEFAULT_USERNAME })
  public static final String PROPKEY_USERNAME = "org.sakaiproject.nakamura.activity.jdbc_username";
  
  @Property(value = { DEFAULT_PASSWORD })
  public static final String PROPKEY_PASSWORD = "org.sakaiproject.nakamura.activity.jdbc_password";
  
  private ComboPooledDataSource pool;
  
  @Activate
  public void activate(Map<String, Object> properties) throws PropertyVetoException, SQLException {
    LOGGER.info("activate()");
    
    pool = new ComboPooledDataSource();
    
    String driver = StorageClientUtils.getSetting(PROPKEY_DRIVER, DEFAULT_DRIVER);
    String connectionString = StorageClientUtils.getSetting(PROPKEY_DRIVER, DEFAULT_DRIVER);
    String username = StorageClientUtils.getSetting(PROPKEY_DRIVER, DEFAULT_DRIVER);
    String password = StorageClientUtils.getSetting(PROPKEY_DRIVER, DEFAULT_DRIVER);
    
    pool.setDriverClass(driver);
    pool.setJdbcUrl(connectionString);
    pool.setUser(username);
    pool.setPassword(password);
    
    LOGGER.info("Activated c3p0 connection pool with properties:");
    LOGGER.info("Driver: {}", driver);
    LOGGER.info("JDBC URL: {}", connectionString);
    LOGGER.info("Username: {}", username);
    
    testPool();
    
    if (!schemaExists()) {
      createSchema();
    }
  }
  
  public void doWithConnection(ConnectionCallback callback) throws SQLException {
    if (callback == null)
      return;
    
    Connection connection = null;
    try {
      connection = pool.getConnection();
      callback.callback(connection);
    } finally {
      closeSilently(connection);
    }
  }
  
  public void doWithStatement(String query, StatementCallback callback) throws SQLException {
    if (query == null || callback == null)
      return;
    
    Connection connection = null;
    PreparedStatement statement = null;
    try {
      connection = pool.getConnection();
      statement = connection.prepareStatement(query);
      callback.callback(statement);
    } finally {
      closeSilently(statement);
      closeSilently(connection);
    }
  }
  
  public void execute(String sql) throws SQLException {
    doWithStatement(sql, new StatementCallback() {
      @Override
      public void callback(PreparedStatement statement) throws SQLException {
        statement.execute();
      }
    });
  }
  
  @Deactivate
  public void deactivate(Map<String, Object> properties) throws SQLException {
    LOGGER.info("deactivate()");
    if (pool != null) {
      DataSources.destroy(pool);
    }
  }
  
  private void testPool() throws SQLException {
    doWithConnection(new ConnectionCallback() {
      @Override
      public void callback(Connection connection) throws SQLException {
        Statement statement = null;
        try {
          // simply create a statement to test the liveliness of the data-source
          connection.createStatement();
        } finally {
          closeSilently(statement);
        }
      }
    });
  }
  
  private boolean schemaExists() {
    try {
      doWithStatement("select * from activity_activity", new StatementCallback() {
        @Override
        public void callback(PreparedStatement statement) throws SQLException {
          ResultSet rs = null;
          try {
            rs = statement.executeQuery();
          } finally {
            closeSilently(rs);
          }
        }
      });
    } catch (SQLException e) {
      return false;
    }
    return true;
  }
  
  private void createSchema() throws SQLException {
    execute(
        "create table activity_activity (" +
      		"path varchar(32)," +
          "created timestamp," +
      		"created_by varchar(16)," +
      		"type varchar(16)," +
      		"message varchar(32)" +
      	")"
    	);
    
    //TODO: create indexes
  }
  
  private void closeSilently(Connection connection) {
    if (connection != null) {
      try {
        connection.close();
      } catch (Throwable e) {
        LOGGER.warn("Failed to close connection.", e);
      }
    }
  }
  
  private void closeSilently(Statement statement) {
    if (statement != null) {
      try {
        statement.close();
      } catch (Throwable e) {
        LOGGER.warn("Failed to close statement.", e);
      }
    }
  }
  
  private void closeSilently(ResultSet resultSet) {
    if (resultSet != null) {
      try {
        resultSet.close();
      } catch (Throwable e) {
        LOGGER.warn("Failed to close result set.", e);
      }
    }
  }
}
