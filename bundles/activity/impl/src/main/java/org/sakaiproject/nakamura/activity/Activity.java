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
package org.sakaiproject.nakamura.activity;

import org.hibernate.annotations.Index;
import org.sakaiproject.nakamura.api.activity.ActivityConstants;
import org.sakaiproject.nakamura.api.lite.StorageClientUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 *
 */
@Entity
@Table(name="activity_activity")
@org.hibernate.annotations.Table(appliesTo="activity_activity", indexes = {
      @Index(name="idx_activity_path", columnNames={"parent_path", "occurred"})
  })
public class Activity implements Serializable, Cloneable {
  private static final long serialVersionUID = -5886999531043942605L;
  
  private Long id;
  private String eid;
  private String parentPath;
  private String type;
  private String message;
  private Date occurred;
  private String actor;
  private Map<String, Serializable> extraProperties;
  
  public Activity() {
  }
  
  /**
   * Content constructor for compatibility with Nakamura content.
   * 
   * @param content
   */
  public Activity(String path, Date occurred, Map<String, Object> content) {
    this.parentPath = StorageClientUtils.getParentObjectPath(path);
    this.eid = StorageClientUtils.getObjectName(path);
    this.occurred = occurred;
    
    // extract the top-level and "extra" properties
    for (String key : content.keySet()) {
      if (ActivityConstants.PARAM_ACTIVITY_TYPE.equals(key)) {
        this.type = (String) content.get(key);
      } else if (ActivityConstants.PARAM_ACTIVITY_MESSAGE.equals(key)) {
        this.message = (String) content.get(key);
      } else if (ActivityConstants.PARAM_ACTOR_ID.equals(key)){
        this.actor = (String)content.get(key);
      } else {
        if (extraProperties == null)
          extraProperties = new HashMap<String, Serializable>();
        extraProperties.put(key, (Serializable)content.get(key));
      }
    }
  }
  
  /**
   * @return the internally-generated id of the activity
   */
  @Id
  @GeneratedValue(strategy=GenerationType.AUTO)
  @Column(name="id")
  public Long getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * @return the externally-generated ID of the activity.
   */
  @Column(name="eid", length=54)
  public String getEid() {
    return eid;
  }

  /**
   * @param eid the eid to set
   */
  public void setEid(String eid) {
    this.eid = eid;
  }

  /**
   * @return the path under which this activity exists. A canonical path to this exact activity
   * would be {@code getParentPath()+"/"+getEid()}
   */
  @Column(name="parent_path", length=256)
  public String getParentPath() {
    return parentPath;
  }
  
  /**
   * @param parentPath the path to set
   */
  public void setParentPath(String parentPath) {
    this.parentPath = parentPath;
  }
  
  /**
   * @return the type
   */
  @Column(name="type", length=32)
  public String getType() {
    return type;
  }
  
  /**
   * @param type the type to set
   */
  public void setType(String type) {
    this.type = type;
  }
  
  /**
   * @return the message
   */
  @Column(name="message", length=256)
  public String getMessage() {
    return message;
  }
  
  /**
   * @param message the message to set
   */
  public void setMessage(String message) {
    this.message = message;
  }
  
  /**
   * @return the date the activity occurred
   */
  @Basic
  public Date getOccurred() {
    return occurred;
  }
  
  /**
   * @param occurred the date the activity occurred
   */
  public void setOccurred(Date occurred) {
    this.occurred = occurred;
  }
  
  /**
   * @return the actor
   */
  @Column(name="actor", length=32)
  public String getActor() {
    return actor;
  }
  
  /**
   * @param actor the createdBy to set
   */
  public void setActor(String actor) {
    this.actor = actor;
  }

  /**
   * @return the extraProperties
   */
  @ElementCollection(fetch=FetchType.EAGER)
  public Map<String, Serializable> getExtraProperties() {
    return extraProperties;
  }

  /**
   * @param extraProperties the extraProperties to set
   */
  public void setExtraProperties(Map<String, Serializable> extraProperties) {
    this.extraProperties = extraProperties;
  }
  
  
  @Transient
  public Map<String, Object> createContentMap() {
    HashMap<String, Object> content = new HashMap<String, Object>();
    
    if (extraProperties != null) {
      content.putAll(extraProperties);
    }
    
    if (type != null)
      content.put(ActivityConstants.PARAM_ACTIVITY_TYPE, type);
    
    if (message != null)
      content.put(ActivityConstants.PARAM_ACTIVITY_MESSAGE, message);
    
    if (actor != null)
      content.put(ActivityConstants.PARAM_ACTOR_ID, actor);
    
    return content;
  }
  
  @Transient
  @Override
  public Activity clone() {
    Activity clone = new Activity();
    clone.setActor(getActor());
    clone.setEid(getEid());
    clone.setMessage(getMessage());
    clone.setOccurred(getOccurred());
    clone.setParentPath(getParentPath());
    clone.setType(getType());
    
    if (extraProperties != null) {
      clone.setExtraProperties(new HashMap<String, Serializable>());
      for (String key : extraProperties.keySet()) {
        clone.getExtraProperties().put(key, extraProperties.get(key));
      }
    }
    
    return clone;
  }

}
