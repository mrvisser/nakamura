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

import com.google.common.collect.ImmutableMap;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.nakamura.activity.Activity;
import org.sakaiproject.nakamura.activity.ActivityTestHelper;
import org.sakaiproject.nakamura.api.lite.ClientPoolException;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.lite.BaseMemoryRepository;
import org.sakaiproject.nakamura.lite.RepositoryImpl;

import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 */
public class ActivityDataStorageServiceTest {

  private ActivityJdbcConnectionPoolImpl pool;
  private ActivityDataStorageServiceImpl service;
  private RepositoryImpl repository;
  
  @Before
  public void setup() throws ClientPoolException, StorageClientException,
      AccessDeniedException, ClassNotFoundException, IOException {
    pool = ActivityTestHelper.createPoolInMemory();
    service = new ActivityDataStorageServiceImpl(pool); 
    repository = new BaseMemoryRepository().getRepository();
  }
  
  @Test
  public void testPersistActivity() {
    Activity activity = new Activity();
    
    Calendar created = Calendar.getInstance();
    created.add(Calendar.HOUR_OF_DAY, -1);
    
    activity.setEid("someEid");
    activity.setPath("/the/path");
    activity.setOccurred(created.getTime());
    activity.setActor("test");
    activity.setType("type");
    activity.setMessage("message");
    
    service.save(activity);
    Activity readActivity = service.findAll(new String[] {"/the/path"}, null, null,
        null, null).iterator().next();
    
    Assert.assertNotNull(readActivity);
    Assert.assertNotNull(activity.getId());
    Assert.assertEquals("someEid", activity.getEid());
    Assert.assertEquals("/the/path", activity.getPath());
    Assert.assertEquals(created.getTimeInMillis(), activity.getOccurred().getTime());
    Assert.assertEquals("test", activity.getActor());
    Assert.assertEquals("type", activity.getType());
    Assert.assertEquals("message", activity.getMessage());  
  }
  
  @Test
  public void testPersistActivitySort() {
    Calendar created1 = Calendar.getInstance();
    created1.add(Calendar.HOUR_OF_DAY, -1);
    
    Activity activity1 = new Activity();
    activity1.setEid("eid1");
    activity1.setPath("/the/path");
    activity1.setOccurred(created1.getTime());
    activity1.setActor("test");
    activity1.setType("type1");
    activity1.setMessage("message");
    service.save(activity1);
    
    Activity activity2 = new Activity();
    activity2.setEid("eid2");
    activity2.setPath("/the/path");
    activity2.setOccurred(new Date());
    activity2.setActor("test");
    activity2.setType("type2");
    activity2.setMessage("message");
    service.save(activity2);
    
    Iterator<Activity> activities = service.findAll(new String[] {"/the/path"}, null, null,
        null, null).iterator();
    Activity readActivity = activities.next();
    Assert.assertEquals("type2", readActivity.getType());
    
    readActivity = activities.next();
    Assert.assertEquals("type1", readActivity.getType());
  }
  
  @Test
  public void testFindUnique() throws ClientPoolException, StorageClientException,
      AccessDeniedException {
    ActivityTestHelper.persistActivities(repository.loginAdministrative()
        .getContentManager(), service, 5, "eid", "/test/path", "joe", "type",
        ImmutableMap.<String, Serializable>of());
    
    for (int i = 0; i < 5; i++) {
      Assert.assertNotNull(service.load("/test/path", "eid"+i));
    }
  }
  
  @After
  public void tearDown() throws SQLException {
    pool.deactivate(new HashMap<String, Object>());
  }

}
