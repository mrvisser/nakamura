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
package org.sakaiproject.nakamura.api.storage;

/**
 *
 */
public class ClassLoaderUtil {

  /**
   * Perform the operations of the runnable in the context of the given
   * {@code contextClassLoader}. When the runnable finishes running, the
   * previous thread context will be swapped back in.
   * 
   * @param contextClassLoader
   * @param runnable
   * @return
   */
  public static void doInContext(ClassLoader contextClassLoader, Runnable runnable) {
    ClassLoader prev = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(contextClassLoader);
      runnable.run();
    } finally {
      Thread.currentThread().setContextClassLoader(prev);
    }
  }
}
