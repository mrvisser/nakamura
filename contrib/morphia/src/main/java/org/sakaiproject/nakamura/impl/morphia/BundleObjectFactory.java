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

import com.google.code.morphia.mapping.DefaultCreator;

import com.mongodb.DBObject;

/**
 * Object factory implementation that uses a provided class loader instead of the
 * thread class loader.
 */
public class BundleObjectFactory extends DefaultCreator {

  private ClassLoader classLoader;
  
  public BundleObjectFactory(ClassLoader classLoader) {
    this.classLoader = classLoader;
  }

  /**
   * {@inheritDoc}
   * @see com.google.code.morphia.mapping.DefaultCreator#getClassLoaderForClass(java.lang.String, com.mongodb.DBObject)
   */
  @Override
  protected ClassLoader getClassLoaderForClass(String clazz, DBObject object) {
    return classLoader;
  }
}
