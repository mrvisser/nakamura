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
package org.sakaiproject.nakamura.activity.jdbc;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.sakaiproject.nakamura.activity.Activity;
import org.sakaiproject.nakamura.api.activity.ActivitySearchQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 *
 */
@Component(immediate=true)
@Service
public class ActivityDataStorageServiceImpl implements ActivityDataStorageService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ActivityDataStorageServiceImpl.class);
  
  @Reference
  protected ActivityJdbcConnectionPool pool;
  
  /**
   * Empty constructor for Type 2 IOC
   */
  public ActivityDataStorageServiceImpl() {}
  
  public ActivityDataStorageServiceImpl(ActivityJdbcConnectionPool pool) {
    this.pool = pool;
  }
  
  /**
   * {@inheritDoc}
   * @see org.sakaiproject.nakamura.activity.jdbc.ActivityDataStorageService#save(org.sakaiproject.nakamura.activity.Activity)
   */
  @Override
  public void save(Activity activity) {
    Session s = pool.getSession();
    Transaction t = null;
    try {
      t = s.beginTransaction();
      s.save(activity);
      t.commit();
    } catch (RuntimeException e) {
      if (t != null)
        t.rollback();
      throw e;
    } finally {
      closeSilent(s);
    }
  }

  /**
   * {@inheritDoc}
   * @see org.sakaiproject.nakamura.activity.jdbc.ActivityDataStorageService#load(java.lang.String, java.lang.String)
   */
  @Override
  public Activity load(String path, String eid) {
    if (path == null)
      throw new IllegalArgumentException("Cannot load activity with a null path.");
    if (eid == null)
      throw new IllegalArgumentException("cannot load activity with a null eid");
    
    Session s = null;
    try {
      s = pool.getSession();
      Criteria criteria = s.createCriteria(Activity.class);
      criteria.add(Restrictions.eq("parentPath", path));
      criteria.add(Restrictions.eq("eid", eid));
      criteria.setReadOnly(true);
      return (Activity) criteria.uniqueResult();
    } finally {
      closeSilent(s);
    }
  }

  /**
   * {@inheritDoc}
   * @see org.sakaiproject.nakamura.activity.jdbc.ActivityDataStorageService#findAll(org.sakaiproject.nakamura.api.activity.ActivitySearchQuery)
   */
  @Override
  public List<Activity> findAll(ActivitySearchQuery query) {
    Session s = null;
    try {
      s = pool.getSession();
      
      Criteria criteria = s.createCriteria(Activity.class);
      
      if (query.paths != null && query.paths.length > 0) {
        criteria.add(Restrictions.in("parentPath", query.paths));
      }
      
      if (query.types != null && query.types.length > 0) {
        criteria.add(Restrictions.in("type", query.types));
      }
      
      if (query.messages != null && query.messages.length > 0) {
        criteria.add(Restrictions.in("message", query.messages));
      }
      
      if (query.then != null) {
        criteria.add(Restrictions.ge("occurred", query.then));
      }
      
      String sortBy = "occurred";
      if (query.sortBy != null) {
        sortBy = query.sortBy;
      }
      
      if (ActivitySearchQuery.ORDER_ASC.equals(query.sortOrder)) {
        criteria.addOrder(Order.asc(sortBy));
      } else {
        criteria.addOrder(Order.desc(sortBy));
      }
      
      // if all the results are wanted, don't set the max results, or bother with paging.
      if (query.maxResults > 0) {
        criteria.setMaxResults((int)query.maxResults);
        criteria.setFirstResult((int)query.offset);
      }
      
      @SuppressWarnings("unchecked")
      List<Activity> result = criteria.list();
      return result;
      
    } finally {
      closeSilent(s);
    }
  }
  
  private void closeSilent(Session s) {
    try {
      s.close();
    } catch (Throwable e) {
      LOGGER.warn("Error closing session.", e);
    }
  }

}
