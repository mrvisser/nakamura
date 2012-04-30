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
package org.sakaiproject.nakamura.activity;

import com.google.common.collect.ImmutableMap;

import org.sakaiproject.nakamura.activity.jdbc.ActivityDataStorageServiceImpl;
import org.sakaiproject.nakamura.activity.jdbc.ActivityJdbcConnectionPoolImpl;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.content.Content;
import org.sakaiproject.nakamura.api.lite.content.ContentManager;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class ActivityTestHelper {

  /**
   * @return an in-memory ActivityJdbcConnectionPool
   */
  public static ActivityJdbcConnectionPoolImpl createPoolInMemory() {
    ActivityJdbcConnectionPoolImpl pool = new ActivityJdbcConnectionPoolImpl();
    Map<String, Object> props = new HashMap<String, Object>();
    props.put(ActivityJdbcConnectionPoolImpl.PROPKEY_DRIVER, "org.hsqldb.jdbcDriver");
    props.put(ActivityJdbcConnectionPoolImpl.PROPKEY_URL, "jdbc:hsqldb:mem:testdb");
    pool.activate(props);
    return pool;
  }
  
  public static Activity createActivity(String eid, String path, String type,
      String message, Date occurred, String actor, Map<String, Serializable> props) {
    Activity activity = new Activity();
    activity.setEid(eid);
    activity.setPath(path);
    activity.setType(type);
    activity.setMessage(message);
    activity.setOccurred(occurred);
    activity.setActor(actor);
    activity.setExtraProperties(props);
    return activity;
  }

  public static void persistActivities(ContentManager contentManager,
      ActivityDataStorageServiceImpl service, int num, String eidPrefix, String path,
      String user, String type, Map<String, Serializable> props) {
    for (int i = 0; i < num; i++) {
      String eid = eidPrefix+i;
      
      try {
        contentManager.update(new Content(path+"/"+eid, ImmutableMap.<String, Object>of(
            Content.SLING_RESOURCE_TYPE_FIELD, "sakai/activity")));
      } catch (StorageClientException e) {
        throw new RuntimeException("Error creating node at path {}", e);
      } catch (AccessDeniedException e) {
        throw new RuntimeException("Error creating node at path {}", e);
      }
      
      service.save(createActivity(eid, path, type, "message"+i, new Date(), user,
          props));
    }
  }
}
