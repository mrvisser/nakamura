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

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Indexed;
import com.google.code.morphia.annotations.Serialized;
import com.google.code.morphia.annotations.Transient;

import org.bson.types.ObjectId;

import java.util.Map;
import java.util.Set;

/**
 *
 */
@Entity
public class Message {

  @Id
  ObjectId id;
  
  @Indexed(unique=true)
  public String path;
  
  @Indexed
  public String from;
  
  @Indexed
  public String to;
  
  @Indexed
  public String body;
  
  @Serialized
  Map<String, Object> headers;
  
  public Set<String> cc;
  
  @Transient
  public Set<String> bcc;
  
  public Message() {
    
  }
  
  public Message(String path, String from, String to, String body) {
    this.path = path;
    this.from = from;
    this.to = to;
    this.body = body;
  }
}
