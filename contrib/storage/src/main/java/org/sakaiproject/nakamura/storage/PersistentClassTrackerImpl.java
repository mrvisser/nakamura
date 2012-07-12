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
package org.sakaiproject.nakamura.storage;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.sakaiproject.nakamura.api.storage.Entity;
import org.sakaiproject.nakamura.api.storage.PersistentClassProvider;
import org.sakaiproject.nakamura.api.storage.PersistentClassTracker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 *
 */
@Component
@Service
public class PersistentClassTrackerImpl implements PersistentClassTracker {

  @Reference(bind="bindProvider", unbind="unbindProvider", cardinality=ReferenceCardinality.OPTIONAL_MULTIPLE,
      policy=ReferencePolicy.DYNAMIC, referenceInterface=PersistentClassProvider.class)
  private List<PersistentClassProvider> providers = Collections.synchronizedList(new ArrayList<PersistentClassProvider>());
  
  /**
   * {@inheritDoc}
   * @see org.sakaiproject.nakamura.api.storage.PersistentClassTracker#listAll()
   */
  @Override
  public List<Class<? extends Entity>> listAll() {
    List<Class<? extends Entity>> classes = new LinkedList<Class<? extends Entity>>();
    for (PersistentClassProvider provider : providers) {
      classes.addAll(provider.listAll());
    }
    return classes;
  }

  public void bindProvider(PersistentClassProvider provider) {
    providers.add(provider);
  }
  
  public void unbindProvider(PersistentClassProvider provider) {
    providers.remove(provider);
  }
  
}
