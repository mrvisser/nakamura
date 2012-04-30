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
package org.sakaiproject.nakamura.activity.jdbc;

import org.sakaiproject.nakamura.activity.Activity;

import java.util.Date;
import java.util.List;

/**
 *
 */
public interface ActivityDataStorageService {

  /**
   * Save the activity to the database.
   * 
   * @param activity
   */
  void save(Activity activity);

  /**
   * Load an activity by its canonical parent path and eid.
   * 
   * @param path
   * @param eid
   * @return
   */
  Activity load(String path, String eid);
  
  /**
   * Find all activities using the given search criteria. To omit any of the parameters
   * as a restriction criteria, simply provide it as {@code null}, or an empty array
   * where applicable.
   * 
   * @param paths The paths (OR'd) that the activities should be part of
   * @param types The types (OR'd) of which the activities should be
   * @param messages The messages (OR'd) the activities should have
   * @param then The date (floor) that represents the earliest date of activity to return
   * @param sortOrder Which direction ("asc" or "desc") to sort the activity date.
   * @return
   * 
   * @todo implement 'search criteria' and 'search result' objects that will handle more
   * rich information about the query restrictions (e.g., all these parameters) and
   * result (e.g., number of total results, number of returned results, page number).
   */
  public List<Activity> findAll(String[] paths, String[] types, String[] messages,
      Date then, String sortOrder);
}
