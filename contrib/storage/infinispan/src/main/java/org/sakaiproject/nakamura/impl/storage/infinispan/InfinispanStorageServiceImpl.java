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

import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.parsing.ConfigurationBuilderHolder;
import org.infinispan.configuration.parsing.Parser;
import org.infinispan.manager.CacheContainer;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.util.TypedProperties;
import org.osgi.service.component.ComponentException;
import org.osgi.service.event.EventAdmin;
import org.sakaiproject.nakamura.api.storage.Entity;
import org.sakaiproject.nakamura.api.storage.EntityDao;
import org.sakaiproject.nakamura.api.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 *
 */
@Service
@Component(immediate=true, metatype=true)
public class InfinispanStorageServiceImpl implements StorageService {

  private static final Logger LOGGER = LoggerFactory.getLogger(
      InfinispanStorageServiceImpl.class);
  
  @Property(description="The name of the Named Cache for entity storage",
      value="EntityCache")
  public static final String PROP_ENTITY_CACHE_NAME = "ispn_entity_cache_name";
  
  @Property(description="The file-system location of the infinispan configuration file.")
  public static final String PROP_CONFIG_FILE_LOCATION = "ispn_config_file_location";
  
  /**
   * The hibernate property that identifies the cache directory provider.
   */
  public static final String ISPN_INDEX_PROP_DIRECTORY_PROVIDER = "hibernate.search.default.directory_provider";
  
  /**
   * The name of the file-system directory provider for hibernate search.
   */
  public static final String ISPN_INDEX_DIRECTORY_PROVIDER_FILESYSTEM = "filesystem";

  /**
   * The name of the ram directory provider for hibernate search.
   */
  public static final String ISPN_INDEX_DIRECTORY_PROVIDER_RAM = "ram";
  
  /**
   * The hibernate property that identifies the index storage location
   */
  private static final String ISPN_INDEX_PROP_INDEX_BASE = "hibernate.search.default.indexBase";
  
  @Reference
  protected EventAdmin eventAdmin;

  private CacheContainer cacheContainer;
  private Cache<String, Entity> entityCache;

  public InfinispanStorageServiceImpl() {
    
  }
  
  public InfinispanStorageServiceImpl(CacheContainer cacheContainer, String entityCacheName) {
    this.cacheContainer = cacheContainer;
    this.entityCache = cacheContainer.getCache(entityCacheName);
  }
  
  @Activate
  public void activate(Map<String, Object> properties) throws IOException {
    // get and ensure the entity cache can be found.
    String entityCacheName = (String) properties.get(PROP_ENTITY_CACHE_NAME);
    if (entityCacheName == null || "".equals(entityCacheName.trim())) {
      throw new ComponentException("Entity cache name must not be empty!");
    }
    
    ClassLoader bundleClassLoader = getClass().getClassLoader();
    InputStream configStream = null;
    
    try {
      configStream = InfinispanConfigurationHelper.resolveConfiguration(
          bundleClassLoader, (String) properties.get(PROP_CONFIG_FILE_LOCATION));
    } catch (IOException e) {
      throw new ComponentException("Failed to load infinispan configuration.", e);
    }
    
    ConfigurationBuilderHolder config = new Parser(bundleClassLoader).parse(configStream);
    config.getGlobalConfigurationBuilder().classLoader(bundleClassLoader);    
    
    validateEntityCacheConfiguration(entityCacheName, config);
    
    cacheContainer = new DefaultCacheManager(config, true);
    entityCache = cacheContainer.getCache(entityCacheName);
  }
  
  @Deactivate
  public void deactivate() {
    try {
      if (cacheContainer != null) {
        cacheContainer.stop();
      }
    } finally {
      cacheContainer = null;
      entityCache = null;
    }
  }

  /**
   * {@inheritDoc}
   * @see org.sakaiproject.nakamura.api.storage.StorageService#getDao(java.lang.Class)
   */
  @SuppressWarnings("unchecked")
  @Override
  public <T extends Entity> EntityDao<T> getDao(Class<T> clazz) {
    return new GenericEntityDao<T>((Cache<String, T>) entityCache, clazz);
  }

  private void validateEntityCacheConfiguration(String entityCacheName,
      ConfigurationBuilderHolder config) {
    ConfigurationBuilder entityConfigBuilder = config.getNamedConfigurationBuilders()
        .get(entityCacheName);
    if (entityConfigBuilder == null) {
      throw new ComponentException(String.format(
          "Could not find cache for entities with name %s", entityCacheName));
    }
    
    Configuration entityConfig = entityConfigBuilder.build();
    if (!entityConfig.indexing().enabled()) {
      throw new ComponentException("Entity Cache must support indexing, but it currently does not.");
    }
    
    TypedProperties indexProperties = entityConfig.indexing().properties();
    String directoryProvider = indexProperties.getProperty(ISPN_INDEX_PROP_DIRECTORY_PROVIDER);
    if (isFilesystemDirectoryProvider(directoryProvider)) {
      String indexLocation = indexProperties.getProperty(ISPN_INDEX_PROP_INDEX_BASE);
      if (StringUtils.isBlank(indexLocation)) {
        LOGGER.warn("Property '{}' is not set for file-system index directory provider. " +
            "Default index location will be working directory.", ISPN_INDEX_PROP_INDEX_BASE);
      } else {
        LOGGER.info("Infinispan index location: {}", indexLocation);
      }
    }
  }
  
  private boolean isFilesystemDirectoryProvider(String provider) {
    return StringUtils.isBlank(provider) ||
        ISPN_INDEX_DIRECTORY_PROVIDER_FILESYSTEM.equals(provider);
  }
}
