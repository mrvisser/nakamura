package org.sakaiproject.nakamura.connections.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.service.event.Event;
import org.sakaiproject.nakamura.api.connections.ConnectionConstants;
import org.sakaiproject.nakamura.api.connections.ConnectionState;
import org.sakaiproject.nakamura.api.connections.ConnectionStorage;
import org.sakaiproject.nakamura.api.connections.ContactConnection;
import org.sakaiproject.nakamura.api.lite.Session;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessControlManager;
import org.sakaiproject.nakamura.api.lite.accesscontrol.Permissions;
import org.sakaiproject.nakamura.api.lite.accesscontrol.Security;
import org.sakaiproject.nakamura.api.lite.authorizable.AuthorizableManager;
import org.sakaiproject.nakamura.api.lite.authorizable.User;
import org.sakaiproject.nakamura.api.solr.RepositorySession;
import org.sakaiproject.nakamura.api.storage.StorageEventUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

/**
 * User: duffy
 * Date: 5/9/12
 * Time: 3:57 PM
 */
@RunWith(MockitoJUnitRunner.class)
public class ConnectionIndexingHandlerTest {

  @Mock
  protected RepositorySession repositorySession;

  @Mock
  protected Session session;
  
  @Mock
  protected AuthorizableManager authorizableManager;
  
  @Mock
  protected ConnectionStorage connectionStorage;
  
  @Mock
  protected AccessControlManager accessControlManager;
  
  @Test
  public void testGetDocuments() throws Exception {
    Map<String, Object> props = new HashMap<String, Object>();
    props.put("sling:resourceType", ConnectionConstants.SAKAI_CONTACT_RT);
    String path = "a:fromUser/toUser";

    ContactConnection connection = new ContactConnection(path, ConnectionState.ACCEPTED, new HashSet<String>(),
        "fromUser", "toUser", "test", "user", new HashMap<String, Object>());
    
    User contactAuth = mock(User.class);
    HashMap<String, Object> safePropertiesMap = new HashMap<String, Object>();
    safePropertiesMap.put("firstName", "test");
    safePropertiesMap.put("lastName", "user");
    safePropertiesMap.put("id", "myid");

    User sourceAuth = mock(User.class);
    
    when(connectionStorage.getContactConnection(sourceAuth, contactAuth)).thenReturn(connection);
    when(contactAuth.getProperty("firstName")).thenReturn("test");
    when(contactAuth.getSafeProperties()).thenReturn(safePropertiesMap);
    when(accessControlManager.findPrincipals(Security.ZONE_CONTENT,
        path, Permissions.CAN_READ.getPermission(), true)).thenReturn(new String[0]);
    when(authorizableManager.findAuthorizable(eq("toUser"))).thenReturn(contactAuth);
    when(authorizableManager.findAuthorizable(eq("fromUser"))).thenReturn(sourceAuth);
    when(repositorySession.adaptTo(Session.class)).thenReturn(session);
    when(session.getAuthorizableManager()).thenReturn(authorizableManager);
    when(session.getAccessControlManager()).thenReturn(accessControlManager);
    
    ConnectionIndexingHandler handler = new ConnectionIndexingHandler(connectionStorage, null, null);
    Event event = new Event("topic", buildEventProperties(path));

    Collection<SolrInputDocument> documents = handler.getDocuments(repositorySession, event);

    assertNotNull(documents);
    assertTrue(!documents.isEmpty());

    Iterator<SolrInputDocument> docIt = documents.iterator();

    assertTrue(docIt.hasNext());

    SolrInputDocument doc = docIt.next();

    assertEquals("test", doc.getField("firstName").getValue());
    assertEquals("user", doc.getField("lastName").getValue());
    assertEquals(path, doc.getField("path").getValue());
    assertEquals(ConnectionState.ACCEPTED.toString(), doc.getField("state").getValue());
    
    SolrInputField field = doc.getField("id");
    assertNotNull(field);
    assertEquals(path, field.getValue());
    assertTrue(!docIt.hasNext());
  }

  @Test
  public void testDeleteQueries() throws Exception {
    String path = "path";

    when(repositorySession.adaptTo(Session.class)).thenReturn(session);

    ConnectionIndexingHandler handler = new ConnectionIndexingHandler();
    Event event = new Event("topic", buildEventProperties(path));

    Collection<String> queries = handler.getDeleteQueries(repositorySession, event);
    assertNotNull(queries);

    Iterator<String> queryIt = queries.iterator();

    assertTrue(queryIt.hasNext());

    String query = queryIt.next();
    assertEquals("id:path", query);

    assertTrue(!queryIt.hasNext());
  }

  /**
   * Build a map of test event properties for the tests.
   *
   * @return
   */
  private Map<String, Object> buildEventProperties(String path) {
    Map<String, Object> props = new HashMap<String, Object>();
    props.put("path", path);
    props.put("resourceType", ConnectionConstants.SAKAI_CONTACT_RT);
    props.put(StorageEventUtil.FIELD_ENTITY_CLASS, ContactConnection.class);
    return props;
  }

}