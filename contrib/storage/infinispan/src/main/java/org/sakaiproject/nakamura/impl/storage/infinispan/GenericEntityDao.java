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

import org.apache.lucene.search.Query;
import org.infinispan.Cache;
import org.infinispan.query.CacheQuery;
import org.infinispan.query.Search;
import org.sakaiproject.nakamura.api.storage.CloseableIterator;
import org.sakaiproject.nakamura.api.storage.DeepCopy;
import org.sakaiproject.nakamura.api.storage.Entity;
import org.sakaiproject.nakamura.api.storage.EntityDao;

/**
 *
 */
public class GenericEntityDao<T extends Entity> implements EntityDao<T> {

  private Class<T> type;
  private Cache<String, T> cache;
  
  public GenericEntityDao(Cache<String, T> cache, Class<T> type) {
    this.cache = cache;
    this.type = type;
  }
  
  /**
   * {@inheritDoc}
   * @see org.sakaiproject.nakamura.api.storage.EntityDao#get(java.lang.String)
   */
  @SuppressWarnings("unchecked")
  @Override
  public T get(String key) {
    if (key == null)
      throw new IllegalArgumentException("Cannot get entity based on null key.");
    
    T result = cache.get(convertKey(key));
    
    if (result != null) {
      if (result instanceof DeepCopy) {
        result = ((DeepCopy<T>) result).deepCopy();
      } else {
        result = SerializationHelper.deepCopy(result, getClass().getClassLoader());
      }
    }
    
    return result;
  }

  /**
   * {@inheritDoc}
   * @see org.sakaiproject.nakamura.api.storage.EntityDao#update(org.sakaiproject.nakamura.api.storage.Entity)
   */
  @Override
  public T update(T entity) {
    cache.put(convertKey(entity.getKey()), entity);
    return entity;
  }

  /**
   * {@inheritDoc}
   * @see org.sakaiproject.nakamura.api.storage.EntityDao#findAll(org.apache.lucene.search.Query)
   */
  @Override
  public CloseableIterator<T> findAll(Query luceneQuery) {
    CacheQuery query = Search.getSearchManager(cache).getQuery(luceneQuery, type);
    return new PreemptiveCloseableIterator<T>(query.lazyIterator());
  }

  private String convertKey(String key) {
    return new StringBuilder(type.getCanonicalName()).append(":").append(key).toString();
  }
}
