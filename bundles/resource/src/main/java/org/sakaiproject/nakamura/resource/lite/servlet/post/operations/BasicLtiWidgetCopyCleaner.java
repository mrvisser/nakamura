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
package org.sakaiproject.nakamura.resource.lite.servlet.post.operations;

import org.apache.felix.scr.annotations.Reference;
import org.apache.sling.servlets.post.Modification;
import org.sakaiproject.nakamura.api.lite.Repository;
import org.sakaiproject.nakamura.api.lite.Session;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.content.ActionRecord;
import org.sakaiproject.nakamura.api.lite.content.Content;
import org.sakaiproject.nakamura.api.lite.content.ContentManager;
import org.sakaiproject.nakamura.api.resource.CopyCleaner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * This cleaner will complete the copy of a BasicLTI widget by copying the protected key information
 * of the basic LTI widget to the destination location.
 */
public class BasicLtiWidgetCopyCleaner implements CopyCleaner {

  private static final Logger LOGGER = LoggerFactory.getLogger(BasicLtiWidgetCopyCleaner.class);
  private static final String LTI_KEYS_NODE = "ltiKeys";
  private static final String BASIC_LTI_WIDGET_RESOURCE_TYPE = "sakai/basiclti";
  
  @Reference
  protected Repository repository;
  
  @Override
  public List<Modification> clean(String fromPath, String toPath, ContentManager cm)
      throws StorageClientException, AccessDeniedException {
    List<Modification> modifications = new LinkedList<Modification>();
    Content fromContent = cm.get(fromPath);
    if (fromContent != null && isBasicLtiWidget(fromContent)) {
      LOGGER.debug("Cleaning a BasicLTI widget after copy from '{}' to '{}'", fromPath, toPath);
      String ltiKeyFromPath = StorageClientUtils.newPath(fromPath, LTI_KEYS_NODE);
      String ltiKeyToPath = StorageClientUtils.newPath(toPath, LTI_KEYS_NODE);
      
      boolean keysWereCopied = false;
      Session adminSession = null;
      try {
        adminSession = repository.loginAdministrative();
        ContentManager adminContentManager = adminSession.getContentManager();
        List<ActionRecord> copies = StorageClientUtils.copyTree(adminContentManager, ltiKeyFromPath, ltiKeyToPath, true);
        keysWereCopied = (copies != null && !copies.isEmpty());
      } catch (IOException e) {
        throw new StorageClientException("Exception occurred when copying BasicLTI keys to destination location.", e);
      } finally {
        if (adminSession != null) {
          adminSession.logout();
        }
      }
      
      // We may have gained sensitive path information from the admin session while copying nodes.
      // Lets only expose the fact that a root ltiKeys node was copied, just in case it had children.
      if (keysWereCopied) {
        LOGGER.debug("Copied protected key from source to destination.");
        modifications.add(Modification.onCopied(ltiKeyFromPath, ltiKeyToPath));
      } else {
        LOGGER.debug("No protected keys were copied from source to destination.");
      }
    }
    
    return modifications;
  }

  /**
   * Determine if the given piece of content is the root of a basic lti widget.
   * 
   * @param content
   * @return
   */
  private boolean isBasicLtiWidget(Content content) {
    return BASIC_LTI_WIDGET_RESOURCE_TYPE.equals(content.getProperty(Content.SLING_RESOURCE_TYPE_FIELD));
  }
}
