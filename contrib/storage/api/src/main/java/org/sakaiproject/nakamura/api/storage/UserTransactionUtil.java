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
package org.sakaiproject.nakamura.api.storage;

import org.sakaiproject.nakamura.api.storage.exceptions.RuntimeHeuristicMixedException;
import org.sakaiproject.nakamura.api.storage.exceptions.RuntimeHeuristicRollbackException;
import org.sakaiproject.nakamura.api.storage.exceptions.RuntimeNotSupportedException;
import org.sakaiproject.nakamura.api.storage.exceptions.RuntimeRollbackException;
import org.sakaiproject.nakamura.api.storage.exceptions.RuntimeSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

/**
 * Utilities for javax.transaction handling.
 * <p>
 * <b>Note:</b> In this static utility, there is quite a bit of use of "ClassLoaderUtil.doInContext".
 * The reasoning for this is that when the JBossStandaloneTransactionManager gets ahold of
 * the commit phase, it performs class lookups using the Thread's context class loader instead
 * of the class loader provided to infinispan by this bundle, which results in a ClassNotFoundException
 * as the Thread isn't necessarily always wired to find the classes needed by the persistence impl.
 * <p>
 * The class-loader provided in the thread context classloader must be able to get dynamic
 * access to any domain classes that are committed VIA this utility. Since this utility
 * currently lives in the API bundle, I don't just use UserTransactionUtil.class.getClassLoader()
 * because that does not have "Dynamic-ImportPackage" == * -- and I don't want it to. The current
 * technique is to use the implementor of the UserTransaction (i.e., tx.getClass().getClassLoader()).
 * This makes the assumption that the transaction manager implementation will always be responsible
 * for finding all domain classes, somehow -- probably VIA "Dynamic-ImportPackage" == *.
 * <p>
 * Sorry for the long rant.
 */
public class UserTransactionUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserTransactionUtil.class);
  
  /**
   * Starts the user transaction if it's not already started. This is effectively "joining"
   * an existing transaction.
   * 
   * @param storageService
   * @return
   */
  public static void beginOrJoin(StorageService storageService) {
    UserTransaction tx = storageService.getUserTransaction();
    if (tx != null) {
      try {
        if (tx == null || tx.getStatus() != Status.STATUS_ACTIVE) {
          tx.begin();
        }
      } catch (SystemException e) {
        throw new RuntimeSystemException("Error trying to begin a new transaction.", e);
      } catch (NotSupportedException e) {
        throw new RuntimeNotSupportedException("Error trying to begin a new transaction.", e);
      }
    }
  }
  
  /**
   * Roll back the given transaction.
   * 
   * @param storageService
   * @throws RuntimeSystemException if a {@link javax.transaction.SystemException} is thrown.
   */
  public static void rollback(final StorageService storageService) throws RuntimeSystemException {
    final UserTransaction tx = storageService.getUserTransaction();
    if (tx != null) {
      ClassLoaderUtil.doInContext(storageService.getClass().getClassLoader(), new Runnable() {
        @Override
        public void run() {
          try {
            tx.rollback();
          } catch (SystemException e) {
            throw new RuntimeSystemException("Error rolling back transaction.", e);
          }
        }
      });
    }
  }
  
  /**
   * Roll back the given transaction, suppressing exceptions.
   * 
   * @param storageService
   */
  public static void rollbackQuiet(final StorageService storageService) {
    final UserTransaction tx = storageService.getUserTransaction();
    if (tx != null) {
      ClassLoaderUtil.doInContext(storageService.getClass().getClassLoader(), new Runnable() {
        @Override
        public void run() {
          try {
          
            int status = tx.getStatus();
            try {
              tx.rollback();
            } catch (SystemException e) {
              LOGGER.error("Error rolling back transaction with status "+status, e);
            }
          
          } catch (SystemException e) {
            LOGGER.error("Error acquiring transaction and status in context.", e);
          }
        }
      });
    }
  }
  
  /**
   * @param storageService
   * @throws RuntimeSystemException if a {@link javax.transaction.SystemException} is thrown.
   * @throws RuntimeRollbackException if a {@link javax.transaction.RollbackException} is thrown.
   * @throws RuntimeHeuristicMixedException if a {@link javax.transaction.HeuristicMixedException} is thrown.
   * @throws RuntimeHeuristicRollbackException if a {@link javax.transaction.HeuristicRollbackException} is thrown.
   */
  public static void commit(final StorageService storageService) throws RuntimeSystemException,
      RuntimeRollbackException, RuntimeHeuristicMixedException,
      RuntimeHeuristicRollbackException {
    final UserTransaction tx = storageService.getUserTransaction();
    if (tx != null) {
      ClassLoaderUtil.doInContext(tx.getClass().getClassLoader(), new Runnable() {
        @Override
        public void run() {
          try {
            tx.commit();
          } catch (SystemException e) {
            throw new RuntimeSystemException("Error commiting transaction.", e);
          } catch (RollbackException e) {
            throw new RuntimeRollbackException("Error commiting transaction.", e);
          } catch (HeuristicMixedException e) {
            throw new RuntimeHeuristicMixedException("Error commiting transaction.", e);
          } catch (HeuristicRollbackException e) {
            throw new RuntimeHeuristicRollbackException("Error commiting transaction.", e);
          }
        }
      });
    }
  }
}
