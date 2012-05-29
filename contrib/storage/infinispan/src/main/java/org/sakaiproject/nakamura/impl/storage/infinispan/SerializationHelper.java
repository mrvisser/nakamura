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

import org.apache.commons.io.IOUtils;
import org.jboss.marshalling.Marshaller;
import org.jboss.marshalling.MarshallerFactory;
import org.jboss.marshalling.Marshalling;
import org.jboss.marshalling.MarshallingConfiguration;
import org.jboss.marshalling.SimpleClassResolver;
import org.jboss.marshalling.Unmarshaller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 *
 */
public class SerializationHelper {
  
  /**
   * Perform a deep-copy of the given object by serializing and unserializing.
   * 
   * @param source
   * @return
   */
  @SuppressWarnings("unchecked")
  public static <T> T deepCopy(T source, ClassLoader classLoader) {
    if (source == null)
      return null;
    
    MarshallerFactory factory = Marshalling.getProvidedMarshallerFactory("river");
    MarshallingConfiguration configuration = new MarshallingConfiguration();
    configuration.setClassResolver(new SimpleClassResolver(classLoader));
    
    ByteArrayInputStream bais = null;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      Marshaller marshaller = factory.createMarshaller(configuration);
      marshaller.start(Marshalling.createByteOutput(baos));
      marshaller.writeObject(source);
      marshaller.finish();
      
      bais = new ByteArrayInputStream(baos.toByteArray());
      Unmarshaller unmarshaller = factory.createUnmarshaller(configuration);
      unmarshaller.start(Marshalling.createByteInput(bais));
      return (T) unmarshaller.readObject();
    } catch (IOException e) {
      throw new RuntimeException("Error marshalling object: "+source.toString(), e);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Error unmarshalling object: "+source.toString(), e);
    } finally {
      IOUtils.closeQuietly(bais);
      IOUtils.closeQuietly(baos);
    }
  }
}
