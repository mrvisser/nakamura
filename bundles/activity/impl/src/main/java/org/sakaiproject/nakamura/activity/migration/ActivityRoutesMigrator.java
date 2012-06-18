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
package org.sakaiproject.nakamura.activity.migration;

import com.google.common.collect.ImmutableMap;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.sakaiproject.nakamura.api.activity.ActivityConstants;
import org.sakaiproject.nakamura.api.lite.ClientPoolException;
import org.sakaiproject.nakamura.api.lite.Repository;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Migrates all activities to conform to the new routing schema. In previous versions, 
 */
@Component
@Service
@Property(name="alias", value="/migration/1-4-0/activity-routes")
public class ActivityRoutesMigrator extends HttpServlet {
  private static final long serialVersionUID = 1L;

  private static final Logger LOGGER = LoggerFactory.getLogger(ActivityRoutesMigrator.class);
  
  @Reference
  Repository repository;

  /**
   * {@inheritDoc}
   * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
   */
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    try {
      migrate();
    } catch (ClientPoolException e) {
      LOGGER.error("Error performing migration.", e);
      resp.sendError(500);
    } catch (StorageClientException e) {
      LOGGER.error("Error performing migration.", e);
      resp.sendError(500);
    } catch (AccessDeniedException e) {
      LOGGER.error("Error performing migration.", e);
      resp.sendError(500);
    }
  }

  public void migrate() throws ClientPoolException, StorageClientException, AccessDeniedException {
    Session session = repository.loginAdministrative();
    try {
      ContentManager cm = session.getContentManager();

      // give all sakai/activity items routes so they appear in search
      Iterable<Content> i = cm.find(ImmutableMap.<String, Object>of(
          Content.SLING_RESOURCE_TYPE_FIELD, ActivityConstants.ACTIVITY_ITEM_RESOURCE_TYPE));
      for (Content c : i) {
        if (ActivityConstants.ACTIVITY_ITEM_RESOURCE_TYPE.equals(c.getProperty(
            Content.SLING_RESOURCE_TYPE_FIELD))) {
          String path = c.getPath();
          
          // get the route path, which is 2 above the activity path (first activityFeed, then the route)
          String routePath = StorageClientUtils.getParentObjectPath(path);
          routePath = StorageClientUtils.getParentObjectPath(routePath);
          
          // create the route info and lock it down to admin
          String routesFieldPath = StorageClientUtils.newPath(path, ActivityConstants.PARAM_ROUTES);
          if (!cm.exists(routesFieldPath)) {
            Content route = new Content(routesFieldPath, new HashMap<String, Object>());
            route.setProperty(ActivityConstants.PARAM_ROUTES, new String[] { routePath });
            cm.update(route);
            session.getAccessControlManager().setAcl(Security.ZONE_CONTENT, routesFieldPath, new AclModification[] {
                new AclModification(AclModification.denyKey(User.ANON_USER), Permissions.ALL.getPermission(), Operation.OP_REPLACE),
                new AclModification(AclModification.denyKey(Group.EVERYONE), Permissions.ALL.getPermission(), Operation.OP_REPLACE)
              });
          }
        }
      }
    } finally {
      try {
        session.logout();
      } catch (ClientPoolException e) {
        LOGGER.error("Error logging out of admin session", e);
      }
    }
  }
  
}
