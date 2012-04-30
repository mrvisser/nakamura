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

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.sling.jcr.resource.JcrResourceConstants;
import org.osgi.service.component.ComponentContext;
import org.sakaiproject.nakamura.activity.jdbc.ActivityDataStorageService;
import org.sakaiproject.nakamura.activity.routing.ActivityRoute;
import org.sakaiproject.nakamura.activity.routing.ActivityRouterManager;
import org.sakaiproject.nakamura.api.activemq.ConnectionFactoryService;
import org.sakaiproject.nakamura.api.activity.ActivityConstants;
import org.sakaiproject.nakamura.api.lite.Repository;
import org.sakaiproject.nakamura.api.lite.Session;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.content.Content;
import org.sakaiproject.nakamura.api.lite.content.ContentManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Topic;

@Component(immediate = true)
public class ActivityDeliverer implements MessageListener {

  // References/properties need for JMS
  @Reference
  protected ConnectionFactoryService connFactoryService;

  // References needed to actually deliver the activity.
  @Reference
  protected Repository sparseRepository;
  @Reference
  protected ActivityRouterManager activityRouterManager;
  @Reference
  protected ActivityDataStorageService storage;
  
  public static final Logger LOG = LoggerFactory
      .getLogger(ActivityDeliverer.class);

  private Connection connection = null;

  /**
   * Start a JMS connection.
   */
  public void activate(ComponentContext componentContext) {
    try {
      connection = connFactoryService.getDefaultConnectionFactory().createConnection();
      javax.jms.Session session = connection.createSession(false,
          javax.jms.Session.AUTO_ACKNOWLEDGE);
      Topic dest = session.createTopic(ActivityConstants.LITE_EVENT_TOPIC);
      MessageConsumer consumer = session.createConsumer(dest);
      consumer.setMessageListener(this);
      connection.start();
    } catch (JMSException e) {
      LOG.error(e.getMessage(), e);
      if (connection != null) {
        try {
          connection.close();
        } catch (JMSException e1) {
        }
      }
    }
  }

  /**
   * Close the JMS connection
   */
  protected void deactivate(ComponentContext ctx) {
    if (connection != null) {
      try {
        connection.close();
      } catch (JMSException e) {
        LOG.error("Cannot close the activity JMS connection.", e);
      }
    }
  }

  /**
   * {@inheritDoc}
   *
   * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
   */
  public void onMessage(Message message) {
    try {
      final String activityItemPath = message
          .getStringProperty(ActivityConstants.EVENT_PROP_PATH);
      Session session = sparseRepository.loginAdministrative(); 
      try {
        Activity activity = storage.load(StorageClientUtils.getParentObjectPath(activityItemPath),
            StorageClientUtils.getObjectName(activityItemPath));
        if (activity == null || activity.getActor() == null) {
          // we must know the actor
          throw new IllegalStateException(
              "Could not determine actor of activity: " + activity);
        }
  
        // Get all the routes for this activity.
        List<ActivityRoute> routes = activityRouterManager
            .getActivityRoutes(new Content(activityItemPath, activity.createContentMap()), session);
  
        // Copy the activity items to each endpoint.
        for (ActivityRoute route : routes) {
          deliverActivityToFeed(session, activity, route.getDestination());
        }
      } finally {
        try { 
          session.logout(); 
        } catch ( Exception e) {
          LOG.warn("Failed to logout of administrative session {} ",e.getMessage());
        }
      }

    } catch (JMSException e) {
      LOG.error("Got a JMS exception in the activity listener.", e);
    } catch (AccessDeniedException e) {
      LOG.error("Got a repository exception in the activity listener.", e);
    } catch (StorageClientException e) {
      LOG.error("Got a repository exception in the activity listener.", e);
    }
  }

  /**
   * Delivers an activity to a feed.
   * 
   * @param session
   *          The session that should be used to do the delivering.
   * @param activity
   *          The node that represents the activity.
   * @param activityFeedPath
   *          The path that holds the feed where the activity should be delivered.
   * @throws RepositoryException
   * @throws StorageClientException 
   * @throws AccessDeniedException 
   */
  protected void deliverActivityToFeed(Session session, Activity activity,
      String activityFeedPath) throws AccessDeniedException, StorageClientException {
    // ensure the activityFeed node with the proper type
    ContentManager contentManager = session.getContentManager();
    String deliveryPath = StorageClientUtils
        .newPath(activityFeedPath, StorageClientUtils.getObjectName(activity.getEid()));
    
    // we are copying this activity to the delivery location
    Activity deliveredActivity = activity.clone();
    
    // set the path to the new copied location
    deliveredActivity.setParentPath(StorageClientUtils.getParentObjectPath(deliveryPath));
    
    // give it resource type sakai/activity, not sakai/activity-posted
    if (deliveredActivity.getExtraProperties() == null)
      deliveredActivity.setExtraProperties(new HashMap<String, Serializable>());
    deliveredActivity.getExtraProperties().put(JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY,
        ActivityConstants.ACTIVITY_ITEM_RESOURCE_TYPE);
    
    // store the content "stub"
    Content content = new Content(deliveryPath, ImmutableMap.<String, Object>of(
        JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY, ActivityConstants.ACTIVITY_ITEM_RESOURCE_TYPE));
    contentManager.update(content);
    
    // save the activity entity in the activity storage
    storage.save(deliveredActivity);
  }

}
