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
package org.sakaiproject.nakamura.impl.storage.infinispan.events;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.sakaiproject.nakamura.api.storage.CloseableIterator;
import org.sakaiproject.nakamura.api.storage.DomainProvider;
import org.sakaiproject.nakamura.api.storage.Entity;
import org.sakaiproject.nakamura.api.storage.EntityDao;
import org.sakaiproject.nakamura.api.storage.StorageEventUtil;
import org.sakaiproject.nakamura.api.storage.StorageService;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 *
 */
@Service
@Component
public class RefreshAllEventHandler implements EventHandler {

  @Property(value = { StorageEventUtil.TOPIC_REFRESH_ALL_DEFAULT }, propertyPrivate = true)
  static final String TOPICS = EventConstants.EVENT_TOPIC;
  
  @Reference
  protected StorageService storageService;

  @Reference
  protected EventAdmin eventAdmin;

  @Reference(referenceInterface=DomainProvider.class, bind="bindDomainProvider",
      unbind="unbindDomainProvider")
  protected Set<DomainProvider> domainProviders = new HashSet<DomainProvider>();
  
  /**
   * {@inheritDoc}
   * @see org.osgi.service.event.EventHandler#handleEvent(org.osgi.service.event.Event)
   */
  @Override
  public void handleEvent(Event refreshAllEvent) {
    // refresh all domain classes from all domain providers.
    if (domainProviders != null) {
      for (DomainProvider provider : domainProviders) {
        if (provider != null) {
          Set<Class<? extends Entity>> clazzes = provider.getDomainClasses();
          for (Class<? extends Entity> clazz : clazzes) {
            EntityDao<? extends Entity> dao = storageService.getDao(clazz);
            CloseableIterator<? extends Entity> i = dao.findAll(new MatchAllDocsQuery());
            try {
              while (i.hasNext()) {
                Event refreshEntityEvent = StorageEventUtil.createStorageEvent(
                    StorageEventUtil.TOPIC_REFRESH_DEFAULT, (String)refreshAllEvent.getProperty(
                        StorageEventUtil.FIELD_ACTOR_USER_ID), i.next(), null);
                eventAdmin.sendEvent(refreshEntityEvent);
              }
            } finally {
              close(i);
            }
          }
        }
      }
    }
  }

  @Deactivate
  public void deactivate() {
    domainProviders.clear();
  }
  
  void bindDomainProvider(DomainProvider domainProvider) {
    domainProviders.add(domainProvider);
  }
  
  void unbindDomainProvider(DomainProvider domainProvider) {
    domainProviders.remove(domainProvider);
  }
  
  private void close(Closeable c) {
    if (c != null) {
      try {
        c.close();
      } catch (IOException e) {}
    }
  }
}
