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

import static org.apache.sling.jcr.resource.JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY;
import static org.sakaiproject.nakamura.api.activity.ActivityConstants.ACTIVITY_FEED_NAME;
import static org.sakaiproject.nakamura.api.activity.ActivityConstants.ACTIVITY_STORE_NAME;
import static org.sakaiproject.nakamura.api.activity.ActivityConstants.ACTIVITY_STORE_RESOURCE_TYPE;
import static org.sakaiproject.nakamura.api.activity.ActivityConstants.LITE_EVENT_TOPIC;
import static org.sakaiproject.nakamura.api.activity.ActivityConstants.PARAM_ACTOR_ID;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.jcr.resource.JcrResourceConstants;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.sakaiproject.nakamura.api.activity.ActivityConstants;
import org.sakaiproject.nakamura.api.activity.ActivityService;
import org.sakaiproject.nakamura.api.lite.Session;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AclModification;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AclModification.Operation;
import org.sakaiproject.nakamura.api.lite.accesscontrol.Permissions;
import org.sakaiproject.nakamura.api.lite.accesscontrol.Security;
import org.sakaiproject.nakamura.api.lite.authorizable.Group;
import org.sakaiproject.nakamura.api.lite.authorizable.User;
import org.sakaiproject.nakamura.api.lite.content.Content;
import org.sakaiproject.nakamura.api.lite.content.ContentManager;
import org.sakaiproject.nakamura.api.user.UserConstants;
import org.sakaiproject.nakamura.util.osgi.EventUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import javax.servlet.ServletException;

@Component(immediate=true, metatype=true)
@Service(value=ActivityService.class)
public class ActivityServiceImpl implements ActivityService {

  public static final Logger LOGGER = LoggerFactory
      .getLogger(ActivityServiceImpl.class);

  @Reference
  EventAdmin eventAdmin;

  private static SecureRandom random = null;

  public void postActivity(String userId, String path, Map<String, Object> attributes) {
    if (attributes == null) {
      throw new IllegalArgumentException("Map of properties cannot be null");
    }
    if (attributes.get("sakai:activity-appid") == null) {
      throw new IllegalArgumentException("The sakai:activity-appid parameter must not be null");
    }
    if (attributes.get("sakai:activity-type") == null) {
      throw new IllegalArgumentException("The sakai:activity-type parameter must not be null");
    }
    Map<String, Object> eventProps = Maps.newHashMap();
    eventProps.put("path", path);
    eventProps.put("userid", userId);
    eventProps.put("attributes", attributes);
    // ActivityPostedHandler will pick up this event and call back to create the activity
    eventAdmin.postEvent(new Event("org/sakaiproject/nakamura/activity/POSTED", eventProps));
  }

  protected void createActivity(Session session, Content targetLocation,  String userId, Map<String, Object> activityProperties)
      throws AccessDeniedException, StorageClientException, ServletException, IOException {
    if ( userId == null ) {
      userId = session.getUserId();
    }
    if ( !userId.equals(session.getUserId()) && !User.ADMIN_USER.equals(session.getUserId()) ) {
      throw new IllegalStateException("Only Administrative sessions may act on behalf of another user for activities");
    }
    ContentManager contentManager = session.getContentManager();
    // create activityStore if it does not exist
    String path = StorageClientUtils.newPath(targetLocation.getPath(), ACTIVITY_STORE_NAME);
    if (!contentManager.exists(path)) {
      contentManager.update(new Content(path, ImmutableMap.<String, Object> of(
          SLING_RESOURCE_TYPE_PROPERTY, ACTIVITY_STORE_RESOURCE_TYPE)));
      // set ACLs so that everyone can add activities; anonymous = none.
      session.getAccessControlManager().setAcl(
          Security.ZONE_CONTENT,
          path,
          new AclModification[] {
              new AclModification(AclModification.denyKey(User.ANON_USER),
                  Permissions.ALL.getPermission(), Operation.OP_REPLACE),
              new AclModification(AclModification.grantKey(Group.EVERYONE),
                  Permissions.CAN_READ.getPermission(), Operation.OP_REPLACE),
              new AclModification(AclModification.grantKey(Group.EVERYONE),
                  Permissions.CAN_WRITE.getPermission(), Operation.OP_REPLACE),
              new AclModification(AclModification.grantKey(userId),
                  Permissions.ALL.getPermission(), Operation.OP_REPLACE) });
    }
    // create activity within activityStore
    String activityPath = StorageClientUtils.newPath(path, createId());
    String activityFeedPath = StorageClientUtils.newPath(targetLocation.getPath(), ACTIVITY_FEED_NAME);

    if (!contentManager.exists(activityFeedPath)) {
      contentManager.update(new Content(activityFeedPath, null));
    }
    if (!contentManager.exists(activityPath)) {
      contentManager.update(new Content(activityPath, ImmutableMap.of(
          JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY,
          (Object) ActivityConstants.ACTIVITY_SOURCE_ITEM_RESOURCE_TYPE)));
    }

    Content activityNode = contentManager.get(activityPath);
    activityNode.setProperty(PARAM_ACTOR_ID, userId);
    activityNode.setProperty(ActivityConstants.PARAM_SOURCE, targetLocation.getPath());
    for ( String key : activityProperties.keySet() ) {
      activityNode.setProperty(key, activityProperties.get(key));
    }

    //save the content
    contentManager.update(activityNode);

    // post the asynchronous OSGi event
    final Dictionary<String, String> properties = new Hashtable<String, String>();
    properties.put(UserConstants.EVENT_PROP_USERID, userId);
    properties.put(ActivityConstants.EVENT_PROP_PATH, activityPath);
    properties.put("path", activityPath);
    properties.put("resourceType", ActivityConstants.ACTIVITY_SOURCE_ITEM_RESOURCE_TYPE);
    EventUtils.sendOsgiEvent(properties, LITE_EVENT_TOPIC, eventAdmin);
  }

  /**
   * @return Creates a unique path to an activity in the form of 2010-01-21-09-randombit
   */
  static String createId() {
    Calendar c = Calendar.getInstance();

    String[] vals = new String[4];
    vals[0] = "" + c.get(Calendar.YEAR);
    vals[1] = StringUtils.leftPad("" + (c.get(Calendar.MONTH) + 1), 2, "0");
    vals[2] = StringUtils.leftPad("" + c.get(Calendar.DAY_OF_MONTH), 2, "0");
    vals[3] = StringUtils.leftPad("" + c.get(Calendar.HOUR_OF_DAY), 2, "0");

    StringBuilder id = new StringBuilder();

    for (String v : vals) {
      id.append(v).append("-");
    }

    byte[] bytes = new byte[20];
    String randomHash = "";
    try {
      if (random == null) {
        random = SecureRandom.getInstance("SHA1PRNG");
      }
      random.nextBytes(bytes);
      randomHash = Arrays.toString(bytes);
      randomHash = org.sakaiproject.nakamura.util.StringUtils
          .sha1Hash(randomHash);
    } catch (NoSuchAlgorithmException e) {
      LOGGER.error("No SHA algorithm on system?", e);
    } catch (UnsupportedEncodingException e) {
      LOGGER.error("Byte encoding not supported?", e);
    }

    id.append(randomHash);
    return id.toString();
  }

}
