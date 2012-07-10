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
package org.sakaiproject.nakamura.classloader;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

/**
 *
 */
@Component(immediate=true)
@Service(value=ClassLoader.class)
@Property(name="type", value="global")
public class GlobalDynamicClassLoader extends ClassLoader {

  private final ClassLoader classLoader = getClass().getClassLoader();

  /**
   * 
   * @see java.lang.ClassLoader#clearAssertionStatus()
   */
  public void clearAssertionStatus() {
    classLoader.clearAssertionStatus();
  }

  /**
   * @param obj
   * @return
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object obj) {
    return classLoader.equals(obj);
  }

  /**
   * @param arg0
   * @return
   * @see java.lang.ClassLoader#getResource(java.lang.String)
   */
  public URL getResource(String arg0) {
    return classLoader.getResource(arg0);
  }

  /**
   * @param arg0
   * @return
   * @see java.lang.ClassLoader#getResourceAsStream(java.lang.String)
   */
  public InputStream getResourceAsStream(String arg0) {
    return classLoader.getResourceAsStream(arg0);
  }

  /**
   * @param arg0
   * @return
   * @throws IOException
   * @see java.lang.ClassLoader#getResources(java.lang.String)
   */
  public Enumeration<URL> getResources(String arg0) throws IOException {
    return classLoader.getResources(arg0);
  }

  /**
   * @return
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    return classLoader.hashCode();
  }

  /**
   * @param arg0
   * @return
   * @throws ClassNotFoundException
   * @see java.lang.ClassLoader#loadClass(java.lang.String)
   */
  public Class<?> loadClass(String arg0) throws ClassNotFoundException {
    return classLoader.loadClass(arg0);
  }

  /**
   * @param arg0
   * @param arg1
   * @see java.lang.ClassLoader#setClassAssertionStatus(java.lang.String, boolean)
   */
  public void setClassAssertionStatus(String arg0, boolean arg1) {
    classLoader.setClassAssertionStatus(arg0, arg1);
  }

  /**
   * @param arg0
   * @see java.lang.ClassLoader#setDefaultAssertionStatus(boolean)
   */
  public void setDefaultAssertionStatus(boolean arg0) {
    classLoader.setDefaultAssertionStatus(arg0);
  }

  /**
   * @param arg0
   * @param arg1
   * @see java.lang.ClassLoader#setPackageAssertionStatus(java.lang.String, boolean)
   */
  public void setPackageAssertionStatus(String arg0, boolean arg1) {
    classLoader.setPackageAssertionStatus(arg0, arg1);
  }

  /**
   * @return
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return classLoader.toString();
  }
}
