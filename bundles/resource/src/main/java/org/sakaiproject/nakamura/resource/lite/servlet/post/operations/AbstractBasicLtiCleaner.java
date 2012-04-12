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

import org.apache.sling.servlets.post.Modification;
import org.sakaiproject.nakamura.api.lite.Repository;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.content.Content;
import org.sakaiproject.nakamura.api.lite.content.ContentManager;
import org.sakaiproject.nakamura.api.resource.CopyCleaner;
import org.sakaiproject.nakamura.api.resource.MoveCleaner;

import java.util.Collections;
import java.util.List;

/**
 * A base abstract implementation for cleaning after BasicLTI operations. This provides some
 * common base functionality needed when identifying if a particular node is handled by the
 * cleaner, as well as some convenience methods that would commonly be needed for concrete
 * implementations.
 */
public abstract class AbstractBasicLtiCleaner implements CopyCleaner, MoveCleaner {

  private static final String BASIC_LTI_WIDGET_RESOURCE_TYPE = "sakai/basiclti";
  private static final String LTI_KEYS_NODE = "ltiKeys";
  
  /**
   * Perform the clean operation required to complete the copy or move on the BasicLTI widget.
   * This method is only called if the node is a BasicLTI widget that should be handled by the
   * concrete implementation.
   * 
   * @param widgetFromPath The source path of the BasicLTI widget that was copied or moved.
   * @param widgetToPath The destination path of the BasicLTI widget that was copied or moved
   * @param cm
   * @return
   * @throws StorageClientException
   * @throws AccessDeniedException
   */
  public abstract List<Modification> doClean(String widgetFromPath, String widgetToPath, ContentManager cm)
      throws StorageClientException, AccessDeniedException;
  
  /**
   * @return the repository.
   */
  protected abstract Repository getRepository();
  
  /**
   * {@inheritDoc}
   * @see org.sakaiproject.nakamura.api.resource.CopyCleaner#clean(java.lang.String, java.lang.String, org.sakaiproject.nakamura.api.lite.content.ContentManager)
   */
  @Override
  public final List<Modification> clean(String fromPath, String toPath, ContentManager cm)
      throws StorageClientException, AccessDeniedException {
    Content toContent = cm.get(toPath);
    if (toContent != null && isBasicLtiWidget(toContent)) {
      return doClean(fromPath, toPath, cm);
    }
    return Collections.emptyList();
  }
  
  /**
   * Get the full path to the protected ltiKeys node for the widget at the given {@code widgetPath}
   * 
   * @param widgetPath
   * @return
   */
  protected final String getLtiKeyNode(String widgetPath) {
    return StorageClientUtils.newPath(widgetPath, LTI_KEYS_NODE);
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
