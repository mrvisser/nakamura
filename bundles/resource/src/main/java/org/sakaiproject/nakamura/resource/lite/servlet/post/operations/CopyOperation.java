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
package org.sakaiproject.nakamura.resource.lite.servlet.post.operations;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.servlets.HtmlResponse;
import org.apache.sling.servlets.post.Modification;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.content.ActionRecord;
import org.sakaiproject.nakamura.api.lite.content.ContentManager;
import org.sakaiproject.nakamura.api.resource.CopyCleaner;
import org.sakaiproject.nakamura.api.resource.lite.AbstractSparsePostOperation;
import org.sakaiproject.nakamura.api.resource.lite.SparsePostOperation;
import org.sakaiproject.nakamura.util.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@Service(value = SparsePostOperation.class)
@Reference(name = "copyCleaners",
    cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE,
    policy = ReferencePolicy.DYNAMIC,
    referenceInterface = CopyCleaner.class,
    bind = "bindCleaner", unbind = "unbindCleaner")
@Property(name = "sling.post.operation", value = "copy")
public class CopyOperation extends AbstractSparsePostOperation {
  private final static Logger LOGGER = LoggerFactory.getLogger(CopyOperation.class);
  public final static String PROP_SOURCE = ":from";

  private CopyOnWriteArrayList<CopyCleaner> copyCleaners;

  public CopyOperation() {
    copyCleaners = new CopyOnWriteArrayList<CopyCleaner>();
  }
  
  @Override
  protected void doRun(SlingHttpServletRequest request, HtmlResponse response,
      ContentManager contentManager, List<Modification> changes, String contentPath)
      throws StorageClientException, AccessDeniedException, IOException {
    String from = getSource(request);
    String to = contentPath;
    
    LOGGER.debug("Copying content tree from '{}' to '{}'", from, to);
    
    if (contentManager.exists(to)) {
      throw new StorageClientException("Copy destination already exists.");
    }
    
    // convert all ActionRecord objects into Modification copy objects
    List<ActionRecord> copies = StorageClientUtils.copyTree(contentManager, from, to, true);
    for (ActionRecord copy : copies) {
      changes.add(Modification.onCopied(copy.getFrom(), copy.getTo()));
    }
    
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Copied {} items", String.valueOf(copies.size()));
    }
    
    //Apply all cleaners to all actions that were performed in this request.
    List<Modification> allCleanerChanges = new LinkedList<Modification>();
    if (copyCleaners != null && !copyCleaners.isEmpty()) {
      for (Modification modification : changes) {
        for (CopyCleaner cleaner : copyCleaners) {
          List<Modification> currentCleanerChanges = cleaner.clean(modification.getSource(),
              modification.getDestination(), contentManager);
          if (currentCleanerChanges != null) {
            allCleanerChanges.addAll(currentCleanerChanges);
          }
        }
      }
    }
  }

  private String getSource(SlingHttpServletRequest request) {
    return PathUtils.toUserContentPath(request.getParameter(PROP_SOURCE));
  }
  
  // ---------- SCR integration ----------
  protected void bindCleaner(CopyCleaner cleaner) {
    copyCleaners.add(cleaner);
  }

  protected void unbindCleaner(CopyCleaner cleaner) {
    copyCleaners.remove(cleaner);
  }
}
