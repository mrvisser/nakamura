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

import org.infinispan.query.QueryIterator;
import org.sakaiproject.nakamura.api.storage.CloseableIterator;

import java.io.IOException;

/**
 * A closeable iterator that closes over an infinispan query iterator. It performs a
 * simple look-ahead approache to ensuring nulls don't creep out of the search results.
 * 
 * TODO: why do nulls creep out of the search results?
 * 
 * Answer: probably because I wiped out the cache contents but did not delete the indexes.
 * This can be changed to a very simple impl, but I don't think it's worth the trouble
 * since, hey, lets not return nulls.
 */
public class PreemptiveCloseableIterator<T> implements CloseableIterator<T> {

  private QueryIterator i;
  private T next;

  public PreemptiveCloseableIterator(QueryIterator i) {
    this.i = i;
    fetchNext();
  }
  
  @Override
  public boolean hasNext() {
    return next != null;
  }

  @Override
  public T next() {
    T next = this.next;
    fetchNext();
    return next;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("Cannot remove document from query iterator.");
  }

  @Override
  public void close() throws IOException {
    i.close();
  }
  
  @SuppressWarnings("unchecked")
  private void fetchNext() {
    this.next = null;
    while (i.hasNext()) {
      this.next = (T) i.next();
      if (this.next != null)
        break;
    }
  }
}
