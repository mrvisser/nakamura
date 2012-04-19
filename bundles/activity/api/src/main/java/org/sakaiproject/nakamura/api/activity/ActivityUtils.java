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
package org.sakaiproject.nakamura.api.activity;

import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.sakaiproject.nakamura.util.LitePersonalUtils;
import org.sakaiproject.nakamura.util.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Map;

/**
 *
 */
public class ActivityUtils {

  private static SecureRandom random = null;

  public static final Logger LOG = LoggerFactory
      .getLogger(ActivityUtils.class);

  /**
   * Returns the path to the activity feed for a user.
   *
   * @param user
   * @return
   */
  public static String getUserFeed(String user) {
    return LitePersonalUtils.getPrivatePath(user) + "/"
        + ActivityConstants.ACTIVITY_FEED_NAME;
  }

  /**
   * Get the path from an activity id.
   *
   * @param id        The ID for an activity.
   * @param startPath The starting path.
   * @return Given an id '2010-01-21-09-randombit' and startPath '/foo/bar' this will
   *         return '/foo/bar/2010/01/21/09/2010-01-21-09-randombit'.
   */
  public static String getPathFromId(String id, String startPath) {
    String[] hashes = StringUtils.split(id, '-');
    StringBuilder sb;

    if (startPath == null) {
      sb = new StringBuilder();
    } else {
      startPath = PathUtils.normalizePath(startPath);
      sb = new StringBuilder(startPath);
    }

    for (int i = 0; i < (hashes.length - 1); i++) {
      sb.append("/").append(hashes[i]);
    }
    return sb.append("/").append(id).toString();

  }

  /**
   * @return Creates a unique path to an activity in the form of 2010-01-21-09-randombit
   */
  public static String createId() {
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
      LOG.error("No SHA algorithm on system?", e);
    } catch (UnsupportedEncodingException e) {
      LOG.error("Byte encoding not supported?", e);
    }

    id.append(randomHash);
    return id.toString();
  }

  /**
   * Post an activity event. processed by activity listeners.
   *
   * @param eventAdmin
   * @param userId     the userID performing the activity
   * @param path       the path to the node the activity is associated with
   * @param attributes attributes, required, and must contain sakai:activity-appid and sakai:activity-type.
   */
  //public static void postActivity(EventAdmin eventAdmin, String userId, String path, String appId, String templateId, String type, String message, Map<String, Object> attributes ) {
  public static void postActivity(EventAdmin eventAdmin, String userId, String path, Map<String, Object> attributes) {
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
    eventAdmin.postEvent(new Event("org/sakaiproject/nakamura/activity/POSTED", eventProps));
  }
}
