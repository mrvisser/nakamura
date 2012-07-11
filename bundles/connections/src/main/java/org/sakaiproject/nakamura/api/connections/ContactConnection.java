/**
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
package org.sakaiproject.nakamura.api.connections;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.sakaiproject.nakamura.api.storage.Entity;

import java.util.Map;
import java.util.Set;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.Index;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Serialized;
import javax.jdo.annotations.Unique;

@PersistenceCapable
public class ContactConnection implements Entity {
  
  @PrimaryKey
  @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
  private Long id;
  
  @Unique
  @Persistent(nullValue=NullValue.EXCEPTION)
  private String key = null;
  
  @Index
  @Persistent
  private ConnectionState connectionState = ConnectionState.NONE;
  
  @Persistent
  private Set<String> connectionTypes = Sets.newHashSet();
  
  @Serialized
  private Map<String,Object> properties = Maps.newHashMap();
  
  @Index
  @Persistent
  private String fromUserId = "";
  
  @Persistent
  private String toUserId = "";
  
  @Persistent
  private String firstName = "";
  
  @Persistent
  private String lastName = "";

  public ContactConnection() {
    
  }
  
  public ContactConnection(String key, ConnectionState connectionState, Set<String> connectionTypes,
                           String fromUserId,
                           String toUserId,
                           String firstName,
                           String lastName,
                           Map<String, Object> additionalProperties) {
    this.key = key;
    if (connectionState != null) this.connectionState = connectionState;
    if (connectionTypes != null) this.connectionTypes = connectionTypes;
    if (fromUserId != null) this.fromUserId = fromUserId;
    if (toUserId != null) this.toUserId = toUserId;
    if (firstName != null) this.firstName = firstName;
    if (lastName != null) this.lastName = lastName;
    if (additionalProperties != null) this.properties = additionalProperties;
  }

  public Long getId() {
    return id;
  }
  
  @Override
  public String getKey() {
    return this.key;
  }

  public ConnectionState getConnectionState() {
    return connectionState;
  }

  public void setConnectionTypes(Set<String> connectionTypes) {
    this.connectionTypes = connectionTypes;
  }

  public void addProperties(Map<String, Object> additionalProperties) {
    this.getProperties().putAll(additionalProperties);
  }

  public void setConnectionState(ConnectionState connectionState) {
    this.connectionState = connectionState;
  }

  public Set<String> getConnectionTypes() {
    return connectionTypes;
  }

  public Map<String, Object> getProperties() {
    return properties;
  }

  public String getFromUserId() {
    return fromUserId;
  }

  public String getToUserId() {
    return toUserId;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public Object getProperty(String propertyName) {
    return getProperties().get(propertyName);
  }

}
