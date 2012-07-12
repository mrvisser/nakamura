package org.sakaiproject.nakamura.connections;
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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.sakaiproject.nakamura.api.connections.ConnectionConstants;
import org.sakaiproject.nakamura.api.connections.ConnectionEventUtil;
import org.sakaiproject.nakamura.api.connections.ConnectionException;
import org.sakaiproject.nakamura.api.connections.ConnectionState;
import org.sakaiproject.nakamura.api.connections.ConnectionStorage;
import org.sakaiproject.nakamura.api.connections.ContactConnection;
import org.sakaiproject.nakamura.api.lite.Repository;
import org.sakaiproject.nakamura.api.lite.Session;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.Permissions;
import org.sakaiproject.nakamura.api.lite.accesscontrol.Security;
import org.sakaiproject.nakamura.api.lite.authorizable.Authorizable;
import org.sakaiproject.nakamura.api.lite.content.Content;
import org.sakaiproject.nakamura.util.LitePersonalUtils;
import org.sakaiproject.nakamura.util.SparseUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;


/**
 *
 */
@Service
@Component
public class KeyEntityConnectionStorage implements ConnectionStorage {

  private final static String
      QUERY_BY_KEY            = "select unique from "+ContactConnection.class.getCanonicalName()+" where key == :keyParam",
      QUERY_BY_STATE_AND_USER = "select from "+ContactConnection.class.getCanonicalName()+" where connectionState == :connectionState && fromUserId == :fromUserId";
  
  @Reference
  protected Repository repository;

  @Reference
  protected PersistenceManagerFactory persistenceManagerFactory;
  
  @Reference
  protected EventAdmin eventAdmin;
  
  public KeyEntityConnectionStorage() {
    
  }
  
  public KeyEntityConnectionStorage(Repository repository,
      PersistenceManagerFactory persistenceManagerFactory, EventAdmin eventAdmin) {
    this.repository = repository;
    this.persistenceManagerFactory = persistenceManagerFactory;
    this.eventAdmin = eventAdmin;
  }

  @Override
  public ContactConnection getOrCreateContactConnection(Authorizable fromUser, Authorizable toUser)
      throws ConnectionException {
    String nodePath = ConnectionUtils.getConnectionPath(fromUser, toUser);
    PersistenceManager pm = persistenceManagerFactory.getPersistenceManager();
    Transaction tx = pm.currentTransaction();
    Session session = null;
    try {
      tx.begin();
      session = repository.loginAdministrative();
      ContactConnection connection = (ContactConnection) pm.newQuery(
          QUERY_BY_KEY).execute(nodePath);
      if (connection == null) {
        // Add auth name for sorting (KERN-1924)
        String firstName = "";
        String lastName = "";
        if (toUser.getProperty("firstName") != null) {
          firstName = (String) toUser.getProperty("firstName");
        }
        if (toUser.getProperty("lastName") != null) {
          lastName = (String) toUser.getProperty("lastName");
        }

        Map<String, Object> props = ImmutableMap.of(
            "sling:resourceType", (Object) ConnectionConstants.SAKAI_CONTACT_RT,
            "reference", LitePersonalUtils.getProfilePath(toUser.getId()),
            "sakai:contactstorepath", ConnectionUtils.getConnectionPathBase(fromUser),
            "firstName", firstName,
            "lastName", lastName);
        
        connection = makeContactConnection(fromUser, toUser, new Content(nodePath, props));
        pm.makePersistent(connection);
        Event event = ConnectionEventUtil.createCreateConnectionEvent(connection);
        eventAdmin.sendEvent(event);
      }
      connection = pm.detachCopy(connection);
      tx.commit();
      session.logout();
      return connection;
    } catch (Exception e) {
      throw new ConnectionException(500, e);
    } finally {
      SparseUtils.logoutQuietly(session);
      if (tx.isActive())
        tx.rollback();
      pm.close();
    }
  }

  private ContactConnection makeContactConnection(Authorizable fromUser, Authorizable toUser, Content connectionContent) {
    if (fromUser == null || toUser == null || connectionContent == null) {
      return null;
    }
    ConnectionState connectionState = connectionContent.hasProperty(ConnectionConstants.SAKAI_CONNECTION_STATE) ?
        ConnectionState.valueOf((String) connectionContent.getProperty(ConnectionConstants.SAKAI_CONNECTION_STATE)) : ConnectionState.NONE;
    Set<String> connectionTypes = Sets.newHashSet(StorageClientUtils.nonNullStringArray((String[]) connectionContent.getProperty(ConnectionConstants.SAKAI_CONNECTION_TYPES)));
    Map<String, Object> additionalProperties = Maps.newHashMap();
    additionalProperties.putAll(connectionContent.getProperties());
    additionalProperties.remove(ConnectionConstants.SAKAI_CONNECTION_STATE);
    additionalProperties.remove(ConnectionConstants.SAKAI_CONNECTION_TYPES);
    additionalProperties.remove("firstName");
    additionalProperties.remove("lastName");
    return new ContactConnection(connectionContent.getPath(), connectionState, connectionTypes, fromUser.getId(),
        toUser.getId(), (String)toUser.getProperty("firstName"), (String)toUser.getProperty("lastName"),
        additionalProperties);
  }

  @Override
  public void saveContactConnectionPair(ContactConnection thisNode, ContactConnection otherNode)
      throws ConnectionException {
    PersistenceManager pm = persistenceManagerFactory.getPersistenceManager();
    Transaction tx = pm.currentTransaction();
    try {
      tx.begin();
      Query jdoQuery = pm.newQuery(QUERY_BY_KEY);
      ContactConnection oldThisNode = (ContactConnection) jdoQuery.execute(thisNode.getKey());
      ContactConnection oldOtherNode = (ContactConnection) jdoQuery.execute(otherNode.getKey());
      
      pm.makePersistent(thisNode);
      pm.makePersistent(otherNode);
      
      thisNode = pm.detachCopy(thisNode);
      otherNode = pm.detachCopy(otherNode);
      
      tx.commit();
      
      Event event = null;
      if (oldThisNode == null) {
        event = ConnectionEventUtil.createCreateConnectionEvent(thisNode); 
      } else {
        event = ConnectionEventUtil.createUpdateConnectionEvent(oldThisNode, thisNode);
      }
      
      eventAdmin.postEvent(event);
      
      if (oldOtherNode == null) {
        event = ConnectionEventUtil.createCreateConnectionEvent(otherNode);
      } else {
        event = ConnectionEventUtil.createUpdateConnectionEvent(oldOtherNode, otherNode);
      }
      
      eventAdmin.postEvent(event);

    } catch (Exception e) {
      throw new ConnectionException(500, e);
    } finally {
      if (tx.isActive())
        tx.rollback();
      pm.close();
    }
  }

  @Override
  public ContactConnection getContactConnection(Authorizable thisUser, Authorizable otherUser) throws ConnectionException {
    PersistenceManager pm = persistenceManagerFactory.getPersistenceManager();
    try {
      String contentPath = ConnectionUtils.getConnectionPath(thisUser, otherUser, null);
      ContactConnection contactConnection = (ContactConnection) pm.newQuery(QUERY_BY_KEY).execute(contentPath); 
      return contactConnection;
    } finally {
      pm.close();
    }
    
  }

  /**
   * {@inheritDoc}
   * @see org.sakaiproject.nakamura.api.connections.ConnectionStorage#getConnectedUsers(org.sakaiproject.nakamura.api.lite.Session, java.lang.String, org.sakaiproject.nakamura.api.connections.ConnectionState)
   */
  @Override
  public List<String> getConnectedUsers(Session session, String userId, ConnectionState state) throws ConnectionException {
    checkCanReadConnections(session, userId);
    List<String> usernames = Lists.newArrayList();
    PersistenceManager pm = persistenceManagerFactory.getPersistenceManager();
    try {
      @SuppressWarnings("unchecked")
      List<ContactConnection> connections = (List<ContactConnection>) pm.newQuery(QUERY_BY_STATE_AND_USER)
          .execute(state, userId);
      for(ContactConnection connection : connections) {
        usernames.add(connection.getToUserId());
      }
    } finally {
      pm.close();
    }
    
    return usernames;
  }
  
  /**
   * Determine whether or not the active user can view the connections of user {@code userId}.
   * 
   * @param session
   * @param userId
   * @throws IllegalStateException
   */
  private void checkCanReadConnections(Session session, String userId) throws IllegalStateException {
    String path = ConnectionUtils.getConnectionPathBase(userId);
    try {
      session.getAccessControlManager().check(Security.ZONE_CONTENT, path, Permissions.CAN_READ);
    } catch (AccessDeniedException e) {
      throw new IllegalStateException("Not allowed to read connections for user " + userId, e);
    } catch (StorageClientException e) {
      throw new IllegalStateException("Error checking if user is allowed to read connections for user "
          + userId, e);
    }
  }

}
