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
package org.sakaiproject.nakamura.activity.servlets;

import com.google.common.collect.ImmutableMap;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.sakaiproject.nakamura.activity.ActivityTestHelper;
import org.sakaiproject.nakamura.activity.MockableJcrSessionSessionAdaptable;
import org.sakaiproject.nakamura.activity.jdbc.ActivityDataStorageServiceImpl;
import org.sakaiproject.nakamura.activity.jdbc.ActivityJdbcConnectionPoolImpl;
import org.sakaiproject.nakamura.activity.search.LiteAllActivitiesResultProcessor;
import org.sakaiproject.nakamura.api.lite.ClientPoolException;
import org.sakaiproject.nakamura.api.lite.Session;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.authorizable.Authorizable;
import org.sakaiproject.nakamura.api.search.solr.SolrSearchConstants;
import org.sakaiproject.nakamura.api.user.BasicUserInfoService;
import org.sakaiproject.nakamura.lite.BaseMemoryRepository;
import org.sakaiproject.nakamura.lite.RepositoryImpl;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.HashMap;

import javax.servlet.ServletException;

/**
 * Verify the functionality of the ActivitySearchServlet
 */
@RunWith(value=MockitoJUnitRunner.class)
public class ActivitySearchServletTest {

  private ActivityJdbcConnectionPoolImpl pool;
  private ActivityDataStorageServiceImpl activityDataStorageService;
  private ActivitySearchServlet servlet;
  private RepositoryImpl repository;
  private Session adminSession;
  private StringWriter responseWriter;
  private Authorizable joe;
  
  @Mock
  SlingHttpServletRequest request;
  
  @Mock
  ResourceResolver resourceResolver;
  
  @Mock
  MockableJcrSessionSessionAdaptable sessionAdaptable;
  
  @Mock
  SlingHttpServletResponse response;
  
  @Mock
  BasicUserInfoService basicUserInfoService;
  
  @Before
  public void setup() throws ClientPoolException, StorageClientException,
      AccessDeniedException, ClassNotFoundException, IOException {
    repository = new BaseMemoryRepository().getRepository();
    adminSession = repository.loginAdministrative();
    adminSession.getAuthorizableManager().createUser("joe", "joe", "joe",
        ImmutableMap.<String, Object>of());
    joe = adminSession.getAuthorizableManager().findAuthorizable("joe");
    
    pool = ActivityTestHelper.createPoolInMemory();
    activityDataStorageService = new ActivityDataStorageServiceImpl(pool);
    servlet = new ActivitySearchServlet();
    servlet.activityService = activityDataStorageService;
    
    Mockito.when(basicUserInfoService.getProperties(joe)).thenReturn(joe.getOriginalProperties());
    
    LiteAllActivitiesResultProcessor processor = new LiteAllActivitiesResultProcessor(null,
        basicUserInfoService);
    
    servlet.bindProcessor(processor, ImmutableMap.of(SolrSearchConstants.REG_PROCESSOR_NAMES,
        "LiteAllActivities"));
    
    Mockito.when(request.getResourceResolver()).thenReturn(resourceResolver);
    Mockito.when(resourceResolver.adaptTo(javax.jcr.Session.class)).thenReturn(sessionAdaptable);
    Mockito.when(sessionAdaptable.getSession()).thenReturn(adminSession);
    
    responseWriter = new StringWriter();
    Mockito.when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
  }
  
  @Test
  public void testResultFeed() throws StorageClientException, ServletException,
      IOException, JSONException {
    ActivityTestHelper.persistActivities(adminSession.getContentManager(),
        activityDataStorageService, 25, "eid", "/testResultFeed", "joe", "pooled content",
        new HashMap<String, Serializable>());
    
    RequestParameter rpPath = Mockito.mock(RequestParameter.class);
    Mockito.when(rpPath.getString()).thenReturn("/testResultFeed");
    
    RequestParameter rpProcessor = Mockito.mock(RequestParameter.class);
    Mockito.when(rpProcessor.getString()).thenReturn("LiteAllActivities");
    
    Mockito.when(request.getRequestParameter("path")).thenReturn(rpPath);
    Mockito.when(request.getRequestParameter(SolrSearchConstants.SAKAI_RESULTPROCESSOR))
        .thenReturn(rpProcessor);
    
    servlet.doGet(request, response);
    
    JSONObject json = new JSONObject(responseWriter.toString());
    JSONArray results = json.getJSONArray(SolrSearchConstants.JSON_RESULTS);
    Assert.assertEquals(25, results.length());
  }
  
  @After
  public void tearDown() {
    pool.deactivate(new HashMap<String, Object>());
  }
  
}
