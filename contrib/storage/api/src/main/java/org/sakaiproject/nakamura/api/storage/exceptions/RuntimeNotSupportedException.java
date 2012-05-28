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
package org.sakaiproject.nakamura.api.storage.exceptions;

/**
 * Runtime version of the javax.transaction.SystemException
 */
public class RuntimeNotSupportedException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  /**
   * 
   */
  public RuntimeNotSupportedException() {
    super();
  }

  /**
   * @param message
   * @param e
   */
  public RuntimeNotSupportedException(String message, Throwable e) {
    super(message, e);
  }

  /**
   * @param message
   */
  public RuntimeNotSupportedException(String message) {
    super(message);
  }

  /**
   * @param message
   */
  public RuntimeNotSupportedException(Throwable e) {
    super(e);
  }
}
