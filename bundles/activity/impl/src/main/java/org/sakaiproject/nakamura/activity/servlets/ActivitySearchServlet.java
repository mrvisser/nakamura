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

import static org.sakaiproject.nakamura.api.search.solr.SolrSearchConstants.DEFAULT_PAGED_ITEMS;
import static org.sakaiproject.nakamura.api.search.solr.SolrSearchConstants.PARAMS_ITEMS_PER_PAGE;
import static org.sakaiproject.nakamura.api.search.solr.SolrSearchConstants.PARAMS_PAGE;

import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.io.JSONWriter;
import org.sakaiproject.nakamura.activity.Activity;
import org.sakaiproject.nakamura.activity.jdbc.ActivityDataStorageService;
import org.sakaiproject.nakamura.api.activity.ActivitySearchQuery;
import org.sakaiproject.nakamura.api.activity.ActivityUtils;
import org.sakaiproject.nakamura.api.search.solr.Result;
import org.sakaiproject.nakamura.api.search.solr.SolrSearchBatchResultProcessor;
import org.sakaiproject.nakamura.api.search.solr.SolrSearchConstants;
import org.sakaiproject.nakamura.api.search.solr.SolrSearchResultProcessor;
import org.sakaiproject.nakamura.api.search.solr.SolrSearchUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.servlet.ServletException;

/**
 *
 */
@SlingServlet(methods={"GET"}, resourceTypes={"sakai/activity/search"})
public class ActivitySearchServlet extends SlingSafeMethodsServlet {
  private static final long serialVersionUID = 1062929177008566552L;

  private static final Logger LOGGER = LoggerFactory.getLogger(ActivitySearchServlet.class);
  
  @Reference
  ActivityDataStorageService activityService;

  /**
   * TODO: All these reference bindings are redundant -- I mistakenly thought that the
   * search batch result processors are not exposed externally, but they're actually
   * published services. These can all be removed in favor of the search trackers, but
   * the unit tests will need to be updated to mock them.
   */
  @Reference(referenceInterface = SolrSearchResultProcessor.class,
      bind = "bindProcessor", unbind = "unbindProcessor",
      cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE,
      policy = ReferencePolicy.DYNAMIC)
  private Map<String, SolrSearchResultProcessor> resultProcessorMap =
      new HashMap<String, SolrSearchResultProcessor>();

  @Reference(referenceInterface = SolrSearchBatchResultProcessor.class,
      bind = "bindBatchProcessor", unbind = "unbindBatchProcessor",
      cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE,
      policy = ReferencePolicy.DYNAMIC)
  private Map<String, SolrSearchBatchResultProcessor> resultBatchProcessorMap =
      new HashMap<String, SolrSearchBatchResultProcessor>();

  /**
   * {@inheritDoc}
   * @see org.apache.sling.api.servlets.SlingSafeMethodsServlet#doGet(org.apache.sling.api.SlingHttpServletRequest, org.apache.sling.api.SlingHttpServletResponse)
   */
  @Override
  protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
      throws ServletException, IOException {
    
    // TODO: support 'NOT'.. such as the all.json "-path:_myFeed" query. May just need to
    // expose a find by 'Query' object query to ActivityDataStorageService.
    
    String path = cleanSingle(request.getRequestParameter("path"));
    String pNodePath = cleanSingle(request.getRequestParameter("p"));
    String[] groups = clean(request.getRequestParameters("group"));
    String[] activityTypes = clean(request.getRequestParameters("activityType"));
    String[] activityMessages = clean(request.getRequestParameters("activityMessage"));
    String sortOrder = cleanSingle(request.getRequestParameter("sortOrder"));

    long nitems = SolrSearchUtil.longRequestParameter(request, PARAMS_ITEMS_PER_PAGE,
        DEFAULT_PAGED_ITEMS);
    long page = SolrSearchUtil.longRequestParameter(request, PARAMS_PAGE, 0);
    
    // TODO: processors should never be looked up here. they should be found on the node.
    // these should be removed and the unit test should be changed to provide the processors
    // on the Node instead of on the request params.
    String processorStr = cleanSingle(request.getRequestParameter(SolrSearchConstants.SAKAI_RESULTPROCESSOR));
    String batchProcessorStr = cleanSingle(request.getRequestParameter(SolrSearchConstants.SAKAI_BATCHRESULTPROCESSOR));
    
    Resource resource = request.getResource();
    if (resource != null) {
      Node node = resource.adaptTo(Node.class);
      try {
        // grab the default query string options to fall back on
        if (node.hasNode(SolrSearchConstants.SAKAI_QUERY_TEMPLATE_DEFAULTS)) {
          Node defaults = node.getNode(SolrSearchConstants.SAKAI_QUERY_TEMPLATE_DEFAULTS);
          PropertyIterator properties = defaults.getProperties();
          while (properties.hasNext()) {
            // can only default path and sortOrder at this point.
            Property property = properties.nextProperty();
            if (path == null && "path".equals(property.getName())) {
              path = property.getString();
            } else if (sortOrder == null && "sortOrder".equals(property.getName())) {
              sortOrder = property.getString();
            }
          }
        }
        
        // get the result processor, if any
        if (node.hasProperty(SolrSearchConstants.SAKAI_RESULTPROCESSOR)) {
          Property processor = node.getProperty(SolrSearchConstants.SAKAI_RESULTPROCESSOR);
          processorStr = processor.getString();
        }
        
        // get the batch result processor, if any
        if (processorStr == null && node.hasProperty(
            SolrSearchConstants.SAKAI_BATCHRESULTPROCESSOR)) {
          Property processor = node.getProperty(SolrSearchConstants.SAKAI_BATCHRESULTPROCESSOR);
          batchProcessorStr = processor.getString();
        }
        
      } catch (RepositoryException e) {
        LOGGER.warn("Could not load default search properties.", e);
      }
    }
    
    List<String> pathList = new LinkedList<String>();
    if (path != null) {
      if (path.equals("_myFeed")) {
        // supports myfeed.json
        String userFeed = ActivityUtils.getUserFeed(request.getRemoteUser());
        if (!StringUtils.isBlank(userFeed)) {
          pathList.add(userFeed.trim());
        }
      } else if (path.equals("_pNodePath")) {
        // supports pooledcontent.json
        pathList.add(pNodePath+"/activityFeed");
      } else {
        pathList.add(path);
      }
    }
    
    // add group criteria
    if (groups.length > 0) {
      for (String group : groups) {
        pathList.add(String.format("/group/%s/activityFeed", group));
      }
    }
    
    String[] paths = pathList.toArray(new String[pathList.size()]);
    
    SolrSearchResultProcessor processor = null;
    SolrSearchBatchResultProcessor batchProcessor = null;
    
    if (processorStr != null) {
      processor = resultProcessorMap.get(processorStr);
    }
    
    if (processor == null && batchProcessorStr != null) {
      batchProcessor = resultBatchProcessorMap.get(batchProcessorStr);
    }
    
    if (processor == null && batchProcessor == null) {
      // don't continue without any processors
      if (!response.isCommitted())
        response.setStatus(SlingHttpServletResponse.SC_OK);
      return;
    }
    
    ActivitySearchQuery query = new ActivitySearchQuery();
    query.paths = paths;
    query.types = activityTypes;
    query.messages = activityMessages;
    query.sortOrder = sortOrder;
    query.maxResults = nitems;
    query.offset = nitems*page;
    
    List<Activity> activities = activityService.findAll(query);
    Iterator<Result> results = createActivityResultIterator(activities);
    
    try {
      JSONWriter writer = new JSONWriter(response.getWriter());
      writer.setTidy(true);
      writer.object();
      writer.key(SolrSearchConstants.PARAMS_ITEMS_PER_PAGE);
      writer.value(nitems);
      writer.key(SolrSearchConstants.JSON_RESULTS);
      writer.array();
      
      if (processor != null) {
        while (results.hasNext()) {
          processor.writeResult(request, writer, results.next());
        }
      } else if (batchProcessor != null) {
        batchProcessor.writeResults(request, writer, results);
      }
      
      writer.endArray();
      
      writer.key(SolrSearchConstants.TOTAL);
      writer.value(activities.size());
      
      writer.endObject();
    } catch (JSONException e) {
      throw new IOException("Error writing JSON to output.", e);
    }
    
    if (!response.isCommitted())
      response.setStatus(SlingHttpServletResponse.SC_OK);
  }

  /**
   * Create an iterator that iterates over solr results to bridge RDBMS functionality
   * over the solr search functionality. This is just a little patch work to re-use
   * the output JSON generated in the existing activities result processors.
   * 
   * @param activitiesIterable
   * @return
   */
  private Iterator<Result> createActivityResultIterator(Iterable<Activity> activitiesIterable) {
    final Iterator<Activity> activities = activitiesIterable.iterator();
    
    // TODO: extract this to a top-level class
    return new Iterator<Result>() {

      @Override
      public boolean hasNext() {
        return activities.hasNext();
      }

      @SuppressWarnings("unchecked")
      @Override
      public Result next() {
        final Activity activity = activities.next();
        final Map<String, Collection<Object>> properties =
            new HashMap<String, Collection<Object>>();
        
        for (Map.Entry<String, Object> entry : activity.createContentMap().entrySet()) {
          String key = entry.getKey();
          Object value = entry.getValue();
          
          // since the solr search result properties maps always to collections,
          // we must convert them here.
          if (value instanceof Collection) {
            properties.put(key, (Collection<Object>)value);
          } else if (value instanceof Object[]) {
            properties.put(key, Arrays.asList((Object[])value));
          } else {
            properties.put(key, Arrays.asList(new Object[] { value }));
          } 
        }
        
        properties.put("activity", Arrays.asList(new Object[] { activity }));
        
        // TODO: Probably best to extract this as something like an "ActivityResult" class
        return new Result() {
          @Override
          public String getPath() {
            // this likely wants the activity path node, so we should give the full path
            return String.format("%s/%s", activity.getParentPath(), activity.getEid());
          }

          @Override
          public Map<String, Collection<Object>> getProperties() {
            return properties;
          }

          @Override
          public Object getFirstValue(String name) {
            Collection<Object> values = properties.get(name);
            if (values != null && values.size() > 0) {
              return values.iterator().next();
            }
            return null;
          }
          
        };
      }

      @Override
      public void remove() {
        activities.remove();
      }
      
    };
  }
  
  void bindProcessor(SolrSearchResultProcessor helper, Map<?, ?> props) {
    resultProcessorMap.put((String)props.get(SolrSearchConstants.REG_PROCESSOR_NAMES),
        helper);
  }

  void unbindProcessor(SolrSearchResultProcessor helper, Map<?, ?> props) {
    resultProcessorMap.remove((String)props.get(SolrSearchConstants.REG_PROCESSOR_NAMES));
  }

  void bindBatchProcessor(SolrSearchBatchResultProcessor helper, Map<?, ?> props) {
    resultBatchProcessorMap.put((String)props.get(SolrSearchConstants.REG_PROCESSOR_NAMES),
        helper);
  }

  void unbindBatchProcessor(SolrSearchBatchResultProcessor helper, Map<?, ?> props) {
    resultBatchProcessorMap.remove((String)props.get(SolrSearchConstants.REG_PROCESSOR_NAMES));
  }

  private String cleanSingle(RequestParameter param) {
    if (!isEmpty(param)) {
      return param.getString().trim();
    } else {
      return null;
    }
  }
  
  private String[] clean(RequestParameter...params) {
    if (params == null || params.length == 0)
      return new String[0];
    List<String> paramList = new LinkedList<String>();
    for (RequestParameter param : params) {
      if (!isEmpty(param)) {
        paramList.add(param.toString().trim());
      }
    }
    return paramList.toArray(new String[paramList.size()]);
  }
  
  private boolean isEmpty(RequestParameter param) {
    return (param == null || StringUtils.isBlank(param.getString()));
  }
  
}
