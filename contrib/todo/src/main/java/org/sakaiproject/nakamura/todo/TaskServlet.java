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
package org.sakaiproject.nakamura.todo;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.sakaiproject.nakamura.todo.api.Task;
import org.sakaiproject.nakamura.todo.api.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 */
@Service(value=Servlet.class)
@Component
@Property(name="alias", value="/api/tasks")
public class TaskServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  private final static Logger LOGGER = LoggerFactory.getLogger(TaskServlet.class);
  
  @Reference
  TaskService taskService;
  
  /**
   * {@inheritDoc}
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
   */
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String id = req.getParameter("id");
    if (id == null) {
      Task task = new Task(req.getParameter("name"), req.getParameter("who"));
      taskService.createTask(task);
    } else {
      if ("all".equals(id)) {
        List<Task> tasks = taskService.listAll();
        for (Task task : tasks) {
          resp.getWriter().write(task.toString());
          resp.getWriter().write("\n");
        }
      } else {
        Task task = taskService.getTaskById(Long.valueOf(id));
        resp.getWriter().write(task.toString());
      }
    }
  }

}
