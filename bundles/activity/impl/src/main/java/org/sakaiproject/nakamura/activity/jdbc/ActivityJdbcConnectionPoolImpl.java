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

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.dialect.resolver.DialectFactory;
import org.sakaiproject.nakamura.activity.Activity;
import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 *
 */
@Component(immediate=true, metatype=true)
@Service
public class ActivityJdbcConnectionPoolImpl implements ActivityJdbcConnectionPool {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(ActivityJdbcConnectionPoolImpl.class);

  public static final String DEFAULT_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
  public static final String DEFAULT_URL = "jdbc:derby:sling/activity/db;create=true";
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

  private SessionFactory sessionFactory;

  @Activate
  public void activate(Map<String, Object> properties) {
    LOGGER.info("activate()");

    String driver = StorageClientUtils.getSetting(properties.get(PROPKEY_DRIVER),
        DEFAULT_DRIVER);
    String connectionString = StorageClientUtils.getSetting(
        properties.get(PROPKEY_URL), DEFAULT_URL);
    String username = StorageClientUtils.getSetting(properties.get(PROPKEY_USERNAME),
        DEFAULT_USERNAME);
    String password = StorageClientUtils.getSetting(properties.get(PROPKEY_PASSWORD),
        DEFAULT_PASSWORD);

    AnnotationConfiguration hibernateConfig = new AnnotationConfiguration()
        // connection properties
        .setProperty("hibernate.connection.driver_class", driver)
        .setProperty("hibernate.connection.url", connectionString)
        .setProperty("hibernate.connection.username", username)
        .setProperty("hibernate.connection.password", password)
        .setProperty("hibernate.hbm2ddl.auto", "create")

        // pooling properties
        .setProperty("hibernate.c3p0.max_size", "8")
        .setProperty("hibernate.c3p0.min_size", "2")
        .setProperty("hibernate.c3p0.timeout", "5000")
        .setProperty("hibernate.c3p0.max_statements", "100")
        .setProperty("hibernate.c3p0.acquire_increment", "2")
        
        // add classes
        .addAnnotatedClass(Activity.class)
        
        // debugging
        .setProperty("hibernate.show_sql", "true")
        .setProperty("hibernate.format_sql", "true");

    sessionFactory = hibernateConfig.buildSessionFactory();

    Session s = null;
    try {
      s = getSession();
      LOGGER.info("Activated hibernate with properties:");
      LOGGER.info("Driver: {}", driver);
      LOGGER.info("JDBC URL: {}", connectionString);
      LOGGER.info("Username: {}", username);
      LOGGER.info("Dialect (auto-detected): {}", DialectFactory.buildDialect(
          hibernateConfig.getProperties(), s.connection()).toString());
    } finally {
      s.close();
    }

  }

  public Session getSession() {
    if (!isAvailable())
      throw new IllegalStateException("Attempted to get session from inactive connection pool");
    return sessionFactory.openSession();
  }
  
  @Deactivate
  public void deactivate(Map<String, Object> properties) {
    LOGGER.info("deactivate()");
    sessionFactory.close();
    sessionFactory = null;
  }

  public boolean isAvailable() {
    return (sessionFactory != null && !sessionFactory.isClosed());
  }
}
