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
package org.sakaiproject.nakamura.connections.search;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.io.JSONWriter;
import org.sakaiproject.nakamura.api.connections.ConnectionConstants;
import org.sakaiproject.nakamura.api.connections.ContactConnection;
import org.sakaiproject.nakamura.api.lite.Session;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.authorizable.Authorizable;
import org.sakaiproject.nakamura.api.lite.authorizable.AuthorizableManager;
import org.sakaiproject.nakamura.api.lite.authorizable.User;
import org.sakaiproject.nakamura.api.search.solr.Query;
import org.sakaiproject.nakamura.api.search.solr.Result;
import org.sakaiproject.nakamura.api.search.solr.SolrSearchException;
import org.sakaiproject.nakamura.api.search.solr.SolrSearchResultProcessor;
import org.sakaiproject.nakamura.api.search.solr.SolrSearchResultSet;
import org.sakaiproject.nakamura.api.search.solr.SolrSearchServiceFactory;
import org.sakaiproject.nakamura.api.user.BasicUserInfoService;
import org.sakaiproject.nakamura.connections.ConnectionUtils;
import org.sakaiproject.nakamura.util.ExtendedJSONWriter;
import org.sakaiproject.nakamura.util.LitePersonalUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

/**
 * Formats connection search results. We get profile nodes from the query and make a
 * uniformed result.
 */
@Component(description = "Formatter for connection search results", label = "ConnectionFinderSearchResultProcessor")
@Properties({ @Property(name = "service.vendor", value = "The Sakai Foundation"),
    @Property(name = "sakai.search.processor", value = "ConnectionFinder") })
@Service
public class ConnectionFinderSearchResultProcessor implements SolrSearchResultProcessor {

  private static final Logger logger = LoggerFactory
      .getLogger(ConnectionFinderSearchResultProcessor.class);

  @Reference
  SolrSearchServiceFactory searchServiceFactory;

  @Reference
  BasicUserInfoService basicUserInfoService;

  @Reference
  PersistenceManagerFactory persistenceManagerFactory;
  
  public void writeResult(SlingHttpServletRequest request, JSONWriter writer, Result result)
      throws JSONException {
    String contactUser = result.getPath().substring(result.getPath().lastIndexOf("/") + 1);
    if (contactUser == null) {
      throw new IllegalArgumentException("Missing " + User.NAME_FIELD);
    }

    javax.jcr.Session jcrSession = request.getResourceResolver().adaptTo(javax.jcr.Session.class);
    Session session = StorageClientUtils.adaptToSession(jcrSession);
    PersistenceManager pm = persistenceManagerFactory.getPersistenceManager();
    try {
      AuthorizableManager authMgr = session.getAuthorizableManager();
      Authorizable auth = authMgr.findAuthorizable(contactUser);
      String contactContentPath = result.getPath();
      logger.debug("getting " + contactContentPath);
      ContactConnection connection = (ContactConnection) pm.newQuery("select unique from "+ContactConnection.class.getCanonicalName() +
          " where key == :key").execute(contactContentPath);
      if (connection != null) {
        writer.object();
        writer.key("target");
        writer.value(contactUser);
        writer.key("profile");
        ExtendedJSONWriter.writeValueMap(writer, new ValueMapDecorator(basicUserInfoService.getProperties(auth)));
        writer.key("details");
        
        writer.object();
        writer.key("sling:resourceType");
        writer.value(ConnectionConstants.SAKAI_CONTACT_RT);
        writer.key("reference");
        writer.value(LitePersonalUtils.getProfilePath(connection.getToUserId()));
        writer.key("sakai:contactstorepath");
        writer.value(ConnectionUtils.getConnectionPathBase(connection.getFromUserId()));
        writer.key("lastName");
        writer.value(connection.getLastName());
        writer.endObject();
        
        writer.endObject();
      }
    } catch (StorageClientException e) {
      throw new RuntimeException(e.getMessage(), e);
    } catch (AccessDeniedException ignored) {
      // user can't read the contact's profile, that's ok, just skip
    } finally {
      pm.close();
    }
  }
  /**
   * {@inheritDoc}
   *
   * @see org.sakaiproject.nakamura.api.search.SearchResultProcessor#getSearchResultSet(org.apache.sling.api.SlingHttpServletRequest,
   *      javax.jcr.query.Query)
   */
  public SolrSearchResultSet getSearchResultSet(SlingHttpServletRequest request,
      Query query) throws SolrSearchException {
    return searchServiceFactory.getSearchResultSet(request, query);
  }
}
