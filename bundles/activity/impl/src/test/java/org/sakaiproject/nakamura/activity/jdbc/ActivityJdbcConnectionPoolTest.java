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

import junit.framework.Assert;

import org.hibernate.Session;
import org.junit.Test;
import org.sakaiproject.nakamura.activity.ActivityTestHelper;

import java.util.HashMap;

/**
 *
 */
public class ActivityJdbcConnectionPoolTest {

  @Test
  public void testActivate() throws Exception {
    ActivityJdbcConnectionPoolImpl pool = ActivityTestHelper.createPoolInMemory();
    Assert.assertTrue(pool.isAvailable());
    
    // verify we can get a session
    Session session = null;
    try {
      session = pool.getSession();
    } finally {
      session.close();
    }
    
    pool.deactivate(new HashMap<String, Object>());
    Assert.assertFalse(pool.isAvailable());
    
    // verify we get an IllegalStateException when getting a session
    IllegalStateException deactivatedException = null;
    try {
      session = pool.getSession();
    } catch (IllegalStateException e) {
      deactivatedException = e;
    } finally {
      if (session.isOpen())
        session.close();
    }
    
    Assert.assertNotNull(deactivatedException);
  }
}
