/**
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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.sakaiproject.nakamura.api.activity.ActivityConstants;
import org.sakaiproject.nakamura.api.activity.ActivityUtils;
import org.sakaiproject.nakamura.testutils.easymock.AbstractEasyMockTest;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jcr.RepositoryException;

/**
 *
 */
public class ActivityUtilsTest extends AbstractEasyMockTest{

  EventAdmin eventAdmin;

  @Before
  public void setup() {
    eventAdmin = Mockito.mock(EventAdmin.class);
  }

  @Test
  public void testUserFeed() throws RepositoryException {
    String user = "admin";
    String expected = "a:admin/private/"
        + ActivityConstants.ACTIVITY_FEED_NAME;
    String result = ActivityUtils.getUserFeed(user);
    Assert.assertEquals(expected, result);
  }

  @Test
  public void testCreateID() throws UnsupportedEncodingException,
      NoSuchAlgorithmException {
    List<String> ids = new ArrayList<String>();
    for (int i = 0; i < 1000; i++) {
      String s = ActivityUtils.createId();
      if (ids.contains(s)) {
        Assert.fail("This id is already in the list.");
      }
      ids.add(s);
    }
  }

  @Test
  public void testGetPath() {
    String id = "2010-01-22-09-ef12a1e112d21f31431b3c4535d1d3a13";
    String startPath = "/_user/private/activityFeed";
    String result = ActivityUtils.getPathFromId(id, startPath);
    Assert
        .assertEquals(
            "/_user/private/activityFeed/2010/01/22/09/2010-01-22-09-ef12a1e112d21f31431b3c4535d1d3a13",
            result);
  }

  @Test
  public void testGetPathNullStart() {
    String id = "2010-01-22-09-ef12a1e112d21f31431b3c4535d1d3a13";
    String result = ActivityUtils.getPathFromId(id, null);
    Assert.assertEquals(
        "/2010/01/22/09/2010-01-22-09-ef12a1e112d21f31431b3c4535d1d3a13",
        result);
  }

  @Test
  public void postActivity() {
    Map<String, Object> activityProps = ImmutableMap.<String, Object>of(
        "sakai:activity-appid", "Content",
        "sakai:activity-type", "pooled content",
        "sakai:activityMessage", "UPDATED_FILE");
    ActivityUtils.postActivity(eventAdmin, "joe", "/some/path", activityProps);
    Mockito.verify(eventAdmin).postEvent(Matchers.any(Event.class));
  }

  @Test(expected = IllegalArgumentException.class)
  public void postActivityWithNullProps() {
    ActivityUtils.postActivity(eventAdmin, "joe", "/some/path", null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void postActivityWithMissingMandatoryProp() {
    Map<String, Object> activityProps = ImmutableMap.<String, Object>of(
        "sakai:activity-appid", "Content");
    ActivityUtils.postActivity(eventAdmin, "joe", "/some/path", activityProps);
  }

}
