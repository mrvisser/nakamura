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
package org.sakaiproject.nakamura.todo.api;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;

import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

/**
 *
 */
@Service(value=TaskService.class)
@Component(immediate=true)
public class TaskService {

  @Reference
  PersistenceManagerFactory persistenceManagerFactory;

  public Task createTask(Task task) {
    PersistenceManager pm = persistenceManagerFactory.getPersistenceManager();
    try {
      task = pm.makePersistent(task);
    } finally {
      pm.close();
    }
    return task;
  }
  
  public Task getTaskById(Long id) {
    PersistenceManager pm = persistenceManagerFactory.getPersistenceManager();
    try {
      return pm.getObjectById(Task.class, id);
    } finally {
      pm.close();
    }
  }
  
  public List<Task> listAll() {
    PersistenceManager pm = persistenceManagerFactory.getPersistenceManager();
    try {
      @SuppressWarnings("unchecked")
      List<Task> tasks = (List<Task>)pm.newQuery(Task.class).execute();
      return tasks;
    } finally {
      pm.close();
    }
  }
}
