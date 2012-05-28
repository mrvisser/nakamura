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

import junit.framework.Assert;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.TermQuery;
import org.junit.Test;
import org.sakaiproject.nakamura.api.storage.CloseableIterator;
import org.sakaiproject.nakamura.api.storage.Entity;
import org.sakaiproject.nakamura.api.storage.EntityDao;
import org.sakaiproject.nakamura.api.storage.StorageService;
import org.sakaiproject.nakamura.api.storage.UserTransactionUtil;

import javax.transaction.UserTransaction;

/**
 *
 */
public class StorageServiceImplTest {

  @Test
  public void testTransactionAutoCommit() {
    StorageService service = new InMemoryStorageServiceImpl();
    service.getDao(GenericEntity.class).update(new GenericEntity("key", "prop1"));
    GenericEntity e = service.getDao(GenericEntity.class).get("key");
    Assert.assertNotNull(e);
    Assert.assertEquals("prop1", e.getProp1());
  }
  
  @Test
  public void testUserTransactionCommit() {
    StorageService service = new InMemoryStorageServiceImpl();
    UserTransaction tx = UserTransactionUtil.startOrJoin(service);
    service.getDao(GenericEntity.class).update(new GenericEntity("key", "prop1"));
    UserTransactionUtil.commit(tx);
    GenericEntity e = service.getDao(GenericEntity.class).get("key");
    Assert.assertNotNull(e);
    Assert.assertEquals("prop1", e.getProp1());
  }

  @Test
  public void testUserTransactionRollback() {
    StorageService service = new InMemoryStorageServiceImpl();
    UserTransaction tx = UserTransactionUtil.startOrJoin(service);
    service.getDao(GenericEntity.class).update(new GenericEntity("key", "prop1"));
    UserTransactionUtil.rollback(tx);
    GenericEntity e = service.getDao(GenericEntity.class).get("key");
    Assert.assertNull(e);
  }

  @Test
  public void testUserTransactionJoin() {
    StorageService service = new InMemoryStorageServiceImpl();
    UserTransaction tx = UserTransactionUtil.startOrJoin(service);
    service.getDao(GenericEntity.class).update(new GenericEntity("key", "prop1"));
    
    //rejoin the transaction
    tx = UserTransactionUtil.startOrJoin(service);
    UserTransactionUtil.rollback(tx);
    
    GenericEntity e = service.getDao(GenericEntity.class).get("key");
    Assert.assertNull(e);
  }
  
  @Test
  public void testUserTxReadBeforeCommit() {
    StorageService service = new InMemoryStorageServiceImpl();
    UserTransaction tx = UserTransactionUtil.startOrJoin(service);
    service.getDao(GenericEntity.class).update(new GenericEntity("key", "prop1"));
    GenericEntity e = service.getDao(GenericEntity.class).get("key");
    Assert.assertNotNull(e);
    Assert.assertEquals("prop1", e.getProp1());
    UserTransactionUtil.commit(tx);
    
    e = service.getDao(GenericEntity.class).get("key");
    Assert.assertNotNull(e);
    Assert.assertEquals("prop1", e.getProp1());
  }
  
  @Test
  public void testStartNewUserTx() {
    StorageService service = new InMemoryStorageServiceImpl();
    UserTransaction tx = UserTransactionUtil.startOrJoin(service);
    service.getDao(GenericEntity.class).update(new GenericEntity("key", "prop1"));
    GenericEntity e = service.getDao(GenericEntity.class).get("key");
    Assert.assertNotNull(e);
    Assert.assertEquals("prop1", e.getProp1());
    UserTransactionUtil.commit(tx);
    
    UserTransaction newTx = UserTransactionUtil.startOrJoin(service);
    GenericEntity newEntity = service.getDao(GenericEntity.class).get("key");
    newEntity.setProp1("blah!");
    service.getDao(GenericEntity.class).update(newEntity);
    UserTransactionUtil.commit(newTx);
    
    e = service.getDao(GenericEntity.class).get("key");
    Assert.assertNotNull(e);
    Assert.assertEquals("blah!", e.getProp1());
  }
  
  @Test
  public void testIndexTransactionEnlistment() {
    StorageService service = new InMemoryStorageServiceImpl();
    EntityDao<GenericEntity> dao = service.getDao(GenericEntity.class);
    
    // create the entity
    UserTransaction tx = UserTransactionUtil.startOrJoin(service);
    dao.update(new GenericEntity("key", "prop1"));
    UserTransactionUtil.commit(tx);
    
    // perform an update, but roll it back.
    tx = UserTransactionUtil.startOrJoin(service);
    GenericEntity e = dao.get("key");
    e.setProp1("prop2");
    dao.update(e);
    UserTransactionUtil.rollback(tx);
    
    e = dao.get("key");
    Assert.assertEquals("prop1", "prop1");
    
    // verify the index is still in sync
    CloseableIterator<GenericEntity> i = dao.findAll(new TermQuery(new Term("prop1", "prop2")));
    Assert.assertTrue(i.hasNext());
    
    i = dao.findAll(new TermQuery(new Term("prop1", "prop1")));
    Assert.assertFalse(i.hasNext());
    
  }
  
  @Test
  public void testFindAll() {
    StorageService service = new InMemoryStorageServiceImpl();
    UserTransaction tx = UserTransactionUtil.startOrJoin(service);
    service.getDao(GenericEntity.class).update(new GenericEntity("key", "prop1"));
    UserTransactionUtil.commit(tx);
    
    CloseableIterator<GenericEntity> allEntities = service.getDao(GenericEntity.class)
        .findAll(new MatchAllDocsQuery());
    Entity found = allEntities.next();
    
    Assert.assertFalse(allEntities.hasNext());
    Assert.assertNotNull(found);
    Assert.assertTrue(found instanceof GenericEntity);
    Assert.assertEquals("key", ((GenericEntity)found).getKey());
    Assert.assertEquals("prop1", ((GenericEntity)found).getProp1());
  }

}
