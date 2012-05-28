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
package org.sakaiproject.nakamura.solr;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class Utils {

	private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

	public static String getParentPath(String path) {
		if ("/".equals(path)) {
			return "/";
		}
		int i = path.lastIndexOf('/');
		if (i == path.length() - 1) {
			i = path.substring(0, i).lastIndexOf('/');
		}
		String res = path;
		if (i > 0) {
			res = path.substring(0, i);
		} else if (i == 0) {
			return "/";
		}
		return res;
	}

	public static String getSolrHome(BundleContext bundleContext)
			throws IOException {
		String slingHomePath = bundleContext.getProperty("sling.home");
		File solrHome = new File(slingHomePath, "solr");
		if (!solrHome.isDirectory() && !solrHome.mkdirs()) {
      LOGGER.info(
          "verifyConfiguration: Cannot create Solr home {}, failed creating default configuration ",
          solrHome.getAbsolutePath());
      return null;
		}
		return solrHome.getAbsolutePath();
	}

	public static String toString(Object property, String defaultValue) {
		if (property == null) {
			return defaultValue;
		}
		return String.valueOf(property);
	}

	public static int toInt(Object property, int defaultValue) {
		if (property == null) {
			return defaultValue;
		}
		return Integer.parseInt(String.valueOf(property));
	}

	public static long toLong(Object property, long defaultValue) {
		if (property == null) {
			return defaultValue;
		}
		return Long.parseLong(String.valueOf(property));
	}

	public static boolean toBoolean(Object property, boolean defaultValue) {
		if (property == null) {
			return defaultValue;
		}
		return Boolean.parseBoolean(String.valueOf(property));
	}

    public static String[] toStringArray(Object property, String[] defaultArray) {
        if (property == null) {
            return defaultArray;

        } else if (property instanceof String && !((String)property).isEmpty()) {
            return ((String) property).split("\\|");

        } else if (property instanceof String[]) {
            // String[]
            return (String[]) property;

        } else if (property.getClass().isArray()) {
            // other array
            Object[] valueArray = (Object[]) property;
            List<String> values = Lists.newArrayListWithExpectedSize(valueArray.length);
            for (Object value : valueArray) {
                if (value != null) {
                    values.add(value.toString());
                }
            }
            return values.toArray(new String[values.size()]);

        } else if (property instanceof Collection<?>) {
            // collection
            Collection<?> valueCollection = (Collection<?>) property;
            List<String> valueList = Lists.newArrayListWithExpectedSize(valueCollection.size());
            for (Object value : valueCollection) {
                if (value != null) {
                    valueList.add(value.toString());
                }
            }
            return valueList.toArray(new String[valueList.size()]);
        }

        return defaultArray;
    }

	public static int defaultMax(int ttl) {
		if (ttl <= 0) {
			return Integer.MAX_VALUE;
		}
		return ttl;
	}

}
