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
package org.sakaiproject.nakamura.api.activity;

import java.util.Date;

/**
 *
 */
public class ActivitySearchQuery {

  public static final String ORDER_ASC = "asc";
  public static final String ORDER_DESC = "desc";
  
  /**
   * The paths (OR'd) under which the activities should be located
   */
  public String[] paths;
  
  /**
   * The types (OR'd) of which the activities should be
   */
  public String[] types;
  
  /**
   * The messages (OR'd) the activities should have
   */
  public String[] messages;
  
  /**
   * The lower-bound date, that represents the earliest date of activity to return. Only
   * activities that occurred after this date will be returned.
   */
  public Date then;
  
  /**
   * Which field should be sorted on
   */
  public String sortBy;
  
  /**
   * In which direction ({@link #ORDER_ASC} or {@link #ORDER_DESC}) the activities should
   * be sorted
   */
  public String sortOrder;
  
  /**
   * How many activities to offset before returning activities
   */
  public long offset = 0;
  
  /**
   * The maximum number of activities to return. If 0, there is no limit.
   */
  public long maxResults = 0;
}
