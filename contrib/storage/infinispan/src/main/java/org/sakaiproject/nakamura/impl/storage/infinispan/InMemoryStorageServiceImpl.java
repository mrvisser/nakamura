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
package org.sakaiproject.nakamura.impl.storage.infinispan;

import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.parsing.ConfigurationBuilderHolder;
import org.infinispan.eviction.EvictionStrategy;
import org.infinispan.manager.CacheContainer;
import org.infinispan.manager.DefaultCacheManager;
import org.sakaiproject.nakamura.api.storage.Entity;
import org.sakaiproject.nakamura.api.storage.EntityDao;
import org.sakaiproject.nakamura.api.storage.StorageService;

import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

/**
 * A configuration wrapper to bootstrap a storage service implementation that is
 * entire in-memory.
 */
public class InMemoryStorageServiceImpl implements StorageService {

  private StorageService storageService;
  
  public InMemoryStorageServiceImpl() {
    ConfigurationBuilderHolder cfgHolder = InfinispanConfigurationHelper.parseConfiguration(
        getClass().getClassLoader(), null);
    
    ConfigurationBuilder entityConfig = cfgHolder.getNamedConfigurationBuilders().get(
        InfinispanConfigurationHelper.CACHENAME_ENTITY_DEFAULT);
    
    // store all entities in memory, do not persist to disk or evict from memory
    entityConfig.loaders().clearCacheLoaders();
    entityConfig.eviction().strategy(EvictionStrategy.NONE).maxEntries(-1);
    
    // indexes should be in-memory
    entityConfig.indexing().addProperty(
        InfinispanStorageServiceImpl.ISPN_INDEX_PROP_DIRECTORY_PROVIDER,
        InfinispanStorageServiceImpl.ISPN_INDEX_DIRECTORY_PROVIDER_RAM);
    
    CacheContainer container = new DefaultCacheManager(entityConfig.build(), true);
    storageService = new InfinispanStorageServiceImpl(container, "EntityCache");
  }
  
  /**
   * {@inheritDoc}
   * @see org.sakaiproject.nakamura.api.storage.StorageService#getDao(java.lang.Class)
   */
  @Override
  public <T extends Entity> EntityDao<T> getDao(Class<T> clazz) {
    return storageService.getDao(clazz);
  }

  @Override
  public TransactionManager getTransactionManager() {
    return storageService.getTransactionManager();
  }

  @Override
  public UserTransaction getUserTransaction() {
    return storageService.getUserTransaction();
  }

}
