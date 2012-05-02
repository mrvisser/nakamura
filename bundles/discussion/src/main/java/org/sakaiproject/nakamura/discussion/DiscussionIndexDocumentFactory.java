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
package org.sakaiproject.nakamura.discussion;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.sakaiproject.nakamura.api.lite.IndexDocument;
import org.sakaiproject.nakamura.api.lite.IndexDocumentFactory;
import org.sakaiproject.nakamura.api.lite.content.Content;

import java.util.Map;

/**
 *
 */
@Component(immediate=true)
@Service
public class DiscussionIndexDocumentFactory implements IndexDocumentFactory {

  /**
   * {@inheritDoc}
   * @see org.sakaiproject.nakamura.api.lite.IndexDocumentFactory#createIndexDocument(java.lang.String, java.util.Map)
   */
  @Override
  public IndexDocument createIndexDocument(String path, Map<String, Object> properties) {
    String resourceType = (String)properties.get(Content.SLING_RESOURCE_TYPE_FIELD);
    String type = (String)properties.get("sakai:type");
    String marker = (String)properties.get("sakai:marker");
    String messageId = (String)properties.get("sakai:id");
    
    if (!("sakai/message".equals(resourceType) && "discussion".equals(type)))
      return null;
    
    DiscussionIndexDocument doc = new DiscussionIndexDocument();
    doc.id = path;
    doc.resourceType = resourceType;
    doc.type = type;
    doc.marker = marker;
    doc.messageId = messageId;
    return doc;
  }

}
