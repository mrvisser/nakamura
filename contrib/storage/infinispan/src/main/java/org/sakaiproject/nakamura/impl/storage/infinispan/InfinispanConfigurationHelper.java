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
package org.sakaiproject.nakamura.impl.storage.infinispan;

import org.apache.commons.lang.StringUtils;
import org.infinispan.configuration.parsing.ConfigurationBuilderHolder;
import org.infinispan.configuration.parsing.Parser;
import org.osgi.service.component.ComponentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 */
public class InfinispanConfigurationHelper {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(InfinispanConfigurationHelper.class);

  public static final String CACHENAME_ENTITY_DEFAULT = "EntityCache";
  
  public static ConfigurationBuilderHolder parseConfiguration(ClassLoader cl, String urlStr) {
    InputStream configStream = null;
    
    try {
      configStream = InfinispanConfigurationHelper.resolveConfiguration(cl, urlStr);
    } catch (IOException e) {
      throw new RuntimeException("Failed to load infinispan configuration.", e);
    }
    
    ConfigurationBuilderHolder config = new Parser(cl).parse(configStream);
    config.getGlobalConfigurationBuilder().classLoader(cl);
    
    return config;
  }
  
  public static InputStream resolveConfiguration(ClassLoader cl, String urlStr)
      throws IOException {
    InputStream configStream = null;
    if (!StringUtils.isBlank(urlStr)) {
      try {
        URL url = new URL(urlStr);
        configStream = url.openStream();
      } catch (MalformedURLException e) {
        throw new ComponentException("Failed to load configuration file from location: "+urlStr, e);
      } catch (IOException e) {
        throw new ComponentException("Failed to load configuration file from location: "+urlStr, e);
      }
    }

    // fall back to internal default
    if (configStream == null) {
      try {
        configStream = getInternalConfiguration(cl);
      } catch (IOException e) {
        LOGGER.error("Could not open internal infinispan configuration", e);
        throw e;
      }
    }

    return configStream;
  }

  public static InputStream getInternalConfiguration(ClassLoader cl) throws IOException {
    URL url = cl.getResource("org/sakaiproject/nakamura/impl/storage/infinispan/cfg/infinispan-default.xml");
    return url.openStream();
  }
}
