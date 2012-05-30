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
package org.sakaiproject.nakamura.impl.morphia;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 */
public class PersistenceTest {

  private MorphiaDatastoreProviderImpl provider;
  
  @Before
  public void setup() {
    provider = new MorphiaDatastoreProviderImpl(null, -1, null);
    provider.map(Message.class);
  }
  
  @After
  public void tearDown() {
    provider.datastore().getDB().dropDatabase();
    provider.datastore().getMongo().close();
  }
  
  @Test
  public void testWriteAndRead() {
    Message m = new Message("/path/to/message", "bob", "alice", "Hello");
    provider.datastore().save(m);
    Assert.assertNotNull(m.id);
    Assert.assertEquals("/path/to/message", m.path);
    Assert.assertEquals("bob", m.from);
    Assert.assertEquals("alice", m.to);
    Assert.assertEquals("Hello", m.body);
    
    m = provider.datastore().createQuery(Message.class).filter("path =", "/path/to/message").get();
    Assert.assertNotNull(m.id);
    Assert.assertEquals("/path/to/message", m.path);
    Assert.assertEquals("bob", m.from);
    Assert.assertEquals("alice", m.to);
    Assert.assertEquals("Hello", m.body);
  }
}
