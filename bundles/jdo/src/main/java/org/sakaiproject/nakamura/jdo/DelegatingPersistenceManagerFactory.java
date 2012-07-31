package org.sakaiproject.nakamura.jdo;

import java.util.Collection;
import java.util.Properties;
import java.util.Set;

import javax.jdo.FetchGroup;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.datastore.DataStoreCache;
import javax.jdo.listener.InstanceLifecycleListener;
import javax.jdo.metadata.JDOMetadata;
import javax.jdo.metadata.TypeMetadata;

/**
 * {@code DelegatingPersistenceManagerFactory} is an instance of a persistence manager that simply
 * delegates to a provided instance of another concrete {@code PersistenceManagerFactory}.
 * <p>
 * This class was entirely generated by Eclipse {@code Source > Generate Delegate Methods...}
 */
public class DelegatingPersistenceManagerFactory implements PersistenceManagerFactory {
  private static final long serialVersionUID = 1L;
  
  protected PersistenceManagerFactory pmf;
  
  /**
   * @param arg0
   * @see javax.jdo.PersistenceManagerFactory#addFetchGroups(javax.jdo.FetchGroup[])
   */
  public void addFetchGroups(FetchGroup... arg0) {
    pmf.addFetchGroups(arg0);
  }

  /**
   * @param arg0
   * @param arg1
   * @see javax.jdo.PersistenceManagerFactory#addInstanceLifecycleListener(javax.jdo.listener.InstanceLifecycleListener, java.lang.Class[])
   */
  public void addInstanceLifecycleListener(InstanceLifecycleListener arg0, @SuppressWarnings("rawtypes") Class[] arg1) {
    pmf.addInstanceLifecycleListener(arg0, arg1);
  }

  /**
   * @see javax.jdo.PersistenceManagerFactory#close()
   */
  public void close() {
    pmf.close();
  }

  /**
   * @return
   * @see javax.jdo.PersistenceManagerFactory#getConnectionDriverName()
   */
  public String getConnectionDriverName() {
    return pmf.getConnectionDriverName();
  }

  /**
   * @return
   * @see javax.jdo.PersistenceManagerFactory#getConnectionFactory()
   */
  public Object getConnectionFactory() {
    return pmf.getConnectionFactory();
  }

  /**
   * @return
   * @see javax.jdo.PersistenceManagerFactory#getConnectionFactory2()
   */
  public Object getConnectionFactory2() {
    return pmf.getConnectionFactory2();
  }

  /**
   * @return
   * @see javax.jdo.PersistenceManagerFactory#getConnectionFactory2Name()
   */
  public String getConnectionFactory2Name() {
    return pmf.getConnectionFactory2Name();
  }

  /**
   * @return
   * @see javax.jdo.PersistenceManagerFactory#getConnectionFactoryName()
   */
  public String getConnectionFactoryName() {
    return pmf.getConnectionFactoryName();
  }

  /**
   * @return
   * @see javax.jdo.PersistenceManagerFactory#getConnectionURL()
   */
  public String getConnectionURL() {
    return pmf.getConnectionURL();
  }

  /**
   * @return
   * @see javax.jdo.PersistenceManagerFactory#getConnectionUserName()
   */
  public String getConnectionUserName() {
    return pmf.getConnectionUserName();
  }

  /**
   * @return
   * @see javax.jdo.PersistenceManagerFactory#getCopyOnAttach()
   */
  public boolean getCopyOnAttach() {
    return pmf.getCopyOnAttach();
  }

  /**
   * @return
   * @see javax.jdo.PersistenceManagerFactory#getDataStoreCache()
   */
  public DataStoreCache getDataStoreCache() {
    return pmf.getDataStoreCache();
  }

  /**
   * @return
   * @see javax.jdo.PersistenceManagerFactory#getDatastoreReadTimeoutMillis()
   */
  public Integer getDatastoreReadTimeoutMillis() {
    return pmf.getDatastoreReadTimeoutMillis();
  }

  /**
   * @return
   * @see javax.jdo.PersistenceManagerFactory#getDatastoreWriteTimeoutMillis()
   */
  public Integer getDatastoreWriteTimeoutMillis() {
    return pmf.getDatastoreWriteTimeoutMillis();
  }

  /**
   * @return
   * @see javax.jdo.PersistenceManagerFactory#getDetachAllOnCommit()
   */
  public boolean getDetachAllOnCommit() {
    return pmf.getDetachAllOnCommit();
  }

  /**
   * @param arg0
   * @param arg1
   * @return
   * @see javax.jdo.PersistenceManagerFactory#getFetchGroup(java.lang.Class, java.lang.String)
   */
  public FetchGroup getFetchGroup(@SuppressWarnings("rawtypes") Class arg0, String arg1) {
    return pmf.getFetchGroup(arg0, arg1);
  }

  /**
   * @return
   * @see javax.jdo.PersistenceManagerFactory#getFetchGroups()
   */
  @SuppressWarnings("rawtypes")
  public Set getFetchGroups() {
    return pmf.getFetchGroups();
  }

  /**
   * @return
   * @see javax.jdo.PersistenceManagerFactory#getIgnoreCache()
   */
  public boolean getIgnoreCache() {
    return pmf.getIgnoreCache();
  }

  /**
   * @return
   * @see javax.jdo.PersistenceManagerFactory#getManagedClasses()
   */
  @SuppressWarnings("rawtypes")
  public Collection<Class> getManagedClasses() {
    return pmf.getManagedClasses();
  }

  /**
   * @return
   * @see javax.jdo.PersistenceManagerFactory#getMapping()
   */
  public String getMapping() {
    return pmf.getMapping();
  }

  /**
   * @param arg0
   * @return
   * @see javax.jdo.PersistenceManagerFactory#getMetadata(java.lang.String)
   */
  public TypeMetadata getMetadata(String arg0) {
    return pmf.getMetadata(arg0);
  }

  /**
   * @return
   * @see javax.jdo.PersistenceManagerFactory#getMultithreaded()
   */
  public boolean getMultithreaded() {
    return pmf.getMultithreaded();
  }

  /**
   * @return
   * @see javax.jdo.PersistenceManagerFactory#getName()
   */
  public String getName() {
    return pmf.getName();
  }

  /**
   * @return
   * @see javax.jdo.PersistenceManagerFactory#getNontransactionalRead()
   */
  public boolean getNontransactionalRead() {
    return pmf.getNontransactionalRead();
  }

  /**
   * @return
   * @see javax.jdo.PersistenceManagerFactory#getNontransactionalWrite()
   */
  public boolean getNontransactionalWrite() {
    return pmf.getNontransactionalWrite();
  }

  /**
   * @return
   * @see javax.jdo.PersistenceManagerFactory#getOptimistic()
   */
  public boolean getOptimistic() {
    return pmf.getOptimistic();
  }

  /**
   * @return
   * @see javax.jdo.PersistenceManagerFactory#getPersistenceManager()
   */
  public PersistenceManager getPersistenceManager() {
    return pmf.getPersistenceManager();
  }

  /**
   * @param arg0
   * @param arg1
   * @return
   * @see javax.jdo.PersistenceManagerFactory#getPersistenceManager(java.lang.String, java.lang.String)
   */
  public PersistenceManager getPersistenceManager(String arg0, String arg1) {
    return pmf.getPersistenceManager(arg0, arg1);
  }

  /**
   * @return
   * @see javax.jdo.PersistenceManagerFactory#getPersistenceManagerProxy()
   */
  public PersistenceManager getPersistenceManagerProxy() {
    return pmf.getPersistenceManagerProxy();
  }

  /**
   * @return
   * @see javax.jdo.PersistenceManagerFactory#getPersistenceUnitName()
   */
  public String getPersistenceUnitName() {
    return pmf.getPersistenceUnitName();
  }

  /**
   * @return
   * @see javax.jdo.PersistenceManagerFactory#getProperties()
   */
  public Properties getProperties() {
    return pmf.getProperties();
  }

  /**
   * @return
   * @see javax.jdo.PersistenceManagerFactory#getReadOnly()
   */
  public boolean getReadOnly() {
    return pmf.getReadOnly();
  }

  /**
   * @return
   * @see javax.jdo.PersistenceManagerFactory#getRestoreValues()
   */
  public boolean getRestoreValues() {
    return pmf.getRestoreValues();
  }

  /**
   * @return
   * @see javax.jdo.PersistenceManagerFactory#getRetainValues()
   */
  public boolean getRetainValues() {
    return pmf.getRetainValues();
  }

  /**
   * @return
   * @see javax.jdo.PersistenceManagerFactory#getServerTimeZoneID()
   */
  public String getServerTimeZoneID() {
    return pmf.getServerTimeZoneID();
  }

  /**
   * @return
   * @see javax.jdo.PersistenceManagerFactory#getTransactionIsolationLevel()
   */
  public String getTransactionIsolationLevel() {
    return pmf.getTransactionIsolationLevel();
  }

  /**
   * @return
   * @see javax.jdo.PersistenceManagerFactory#getTransactionType()
   */
  public String getTransactionType() {
    return pmf.getTransactionType();
  }

  /**
   * @return
   * @see javax.jdo.PersistenceManagerFactory#isClosed()
   */
  public boolean isClosed() {
    return pmf.isClosed();
  }

  /**
   * @return
   * @see javax.jdo.PersistenceManagerFactory#newMetadata()
   */
  public JDOMetadata newMetadata() {
    return pmf.newMetadata();
  }

  /**
   * @param arg0
   * @see javax.jdo.PersistenceManagerFactory#registerMetadata(javax.jdo.metadata.JDOMetadata)
   */
  public void registerMetadata(JDOMetadata arg0) {
    pmf.registerMetadata(arg0);
  }

  /**
   * 
   * @see javax.jdo.PersistenceManagerFactory#removeAllFetchGroups()
   */
  public void removeAllFetchGroups() {
    pmf.removeAllFetchGroups();
  }

  /**
   * @param arg0
   * @see javax.jdo.PersistenceManagerFactory#removeFetchGroups(javax.jdo.FetchGroup[])
   */
  public void removeFetchGroups(FetchGroup... arg0) {
    pmf.removeFetchGroups(arg0);
  }

  /**
   * @param arg0
   * @see javax.jdo.PersistenceManagerFactory#removeInstanceLifecycleListener(javax.jdo.listener.InstanceLifecycleListener)
   */
  public void removeInstanceLifecycleListener(InstanceLifecycleListener arg0) {
    pmf.removeInstanceLifecycleListener(arg0);
  }

  /**
   * @param arg0
   * @see javax.jdo.PersistenceManagerFactory#setConnectionDriverName(java.lang.String)
   */
  public void setConnectionDriverName(String arg0) {
    pmf.setConnectionDriverName(arg0);
  }

  /**
   * @param arg0
   * @see javax.jdo.PersistenceManagerFactory#setConnectionFactory(java.lang.Object)
   */
  public void setConnectionFactory(Object arg0) {
    pmf.setConnectionFactory(arg0);
  }

  /**
   * @param arg0
   * @see javax.jdo.PersistenceManagerFactory#setConnectionFactory2(java.lang.Object)
   */
  public void setConnectionFactory2(Object arg0) {
    pmf.setConnectionFactory2(arg0);
  }

  /**
   * @param arg0
   * @see javax.jdo.PersistenceManagerFactory#setConnectionFactory2Name(java.lang.String)
   */
  public void setConnectionFactory2Name(String arg0) {
    pmf.setConnectionFactory2Name(arg0);
  }

  /**
   * @param arg0
   * @see javax.jdo.PersistenceManagerFactory#setConnectionFactoryName(java.lang.String)
   */
  public void setConnectionFactoryName(String arg0) {
    pmf.setConnectionFactoryName(arg0);
  }

  /**
   * @param arg0
   * @see javax.jdo.PersistenceManagerFactory#setConnectionPassword(java.lang.String)
   */
  public void setConnectionPassword(String arg0) {
    pmf.setConnectionPassword(arg0);
  }

  /**
   * @param arg0
   * @see javax.jdo.PersistenceManagerFactory#setConnectionURL(java.lang.String)
   */
  public void setConnectionURL(String arg0) {
    pmf.setConnectionURL(arg0);
  }

  /**
   * @param arg0
   * @see javax.jdo.PersistenceManagerFactory#setConnectionUserName(java.lang.String)
   */
  public void setConnectionUserName(String arg0) {
    pmf.setConnectionUserName(arg0);
  }

  /**
   * @param arg0
   * @see javax.jdo.PersistenceManagerFactory#setCopyOnAttach(boolean)
   */
  public void setCopyOnAttach(boolean arg0) {
    pmf.setCopyOnAttach(arg0);
  }

  /**
   * @param arg0
   * @see javax.jdo.PersistenceManagerFactory#setDatastoreReadTimeoutMillis(java.lang.Integer)
   */
  public void setDatastoreReadTimeoutMillis(Integer arg0) {
    pmf.setDatastoreReadTimeoutMillis(arg0);
  }

  /**
   * @param arg0
   * @see javax.jdo.PersistenceManagerFactory#setDatastoreWriteTimeoutMillis(java.lang.Integer)
   */
  public void setDatastoreWriteTimeoutMillis(Integer arg0) {
    pmf.setDatastoreWriteTimeoutMillis(arg0);
  }

  /**
   * @param arg0
   * @see javax.jdo.PersistenceManagerFactory#setDetachAllOnCommit(boolean)
   */
  public void setDetachAllOnCommit(boolean arg0) {
    pmf.setDetachAllOnCommit(arg0);
  }

  /**
   * @param arg0
   * @see javax.jdo.PersistenceManagerFactory#setIgnoreCache(boolean)
   */
  public void setIgnoreCache(boolean arg0) {
    pmf.setIgnoreCache(arg0);
  }

  /**
   * @param arg0
   * @see javax.jdo.PersistenceManagerFactory#setMapping(java.lang.String)
   */
  public void setMapping(String arg0) {
    pmf.setMapping(arg0);
  }

  /**
   * @param arg0
   * @see javax.jdo.PersistenceManagerFactory#setMultithreaded(boolean)
   */
  public void setMultithreaded(boolean arg0) {
    pmf.setMultithreaded(arg0);
  }

  /**
   * @param arg0
   * @see javax.jdo.PersistenceManagerFactory#setName(java.lang.String)
   */
  public void setName(String arg0) {
    pmf.setName(arg0);
  }

  /**
   * @param arg0
   * @see javax.jdo.PersistenceManagerFactory#setNontransactionalRead(boolean)
   */
  public void setNontransactionalRead(boolean arg0) {
    pmf.setNontransactionalRead(arg0);
  }

  /**
   * @param arg0
   * @see javax.jdo.PersistenceManagerFactory#setNontransactionalWrite(boolean)
   */
  public void setNontransactionalWrite(boolean arg0) {
    pmf.setNontransactionalWrite(arg0);
  }

  /**
   * @param arg0
   * @see javax.jdo.PersistenceManagerFactory#setOptimistic(boolean)
   */
  public void setOptimistic(boolean arg0) {
    pmf.setOptimistic(arg0);
  }

  /**
   * @param arg0
   * @see javax.jdo.PersistenceManagerFactory#setPersistenceUnitName(java.lang.String)
   */
  public void setPersistenceUnitName(String arg0) {
    pmf.setPersistenceUnitName(arg0);
  }

  /**
   * @param arg0
   * @see javax.jdo.PersistenceManagerFactory#setReadOnly(boolean)
   */
  public void setReadOnly(boolean arg0) {
    pmf.setReadOnly(arg0);
  }

  /**
   * @param arg0
   * @see javax.jdo.PersistenceManagerFactory#setRestoreValues(boolean)
   */
  public void setRestoreValues(boolean arg0) {
    pmf.setRestoreValues(arg0);
  }

  /**
   * @param arg0
   * @see javax.jdo.PersistenceManagerFactory#setRetainValues(boolean)
   */
  public void setRetainValues(boolean arg0) {
    pmf.setRetainValues(arg0);
  }

  /**
   * @param arg0
   * @see javax.jdo.PersistenceManagerFactory#setServerTimeZoneID(java.lang.String)
   */
  public void setServerTimeZoneID(String arg0) {
    pmf.setServerTimeZoneID(arg0);
  }

  /**
   * @param arg0
   * @see javax.jdo.PersistenceManagerFactory#setTransactionIsolationLevel(java.lang.String)
   */
  public void setTransactionIsolationLevel(String arg0) {
    pmf.setTransactionIsolationLevel(arg0);
  }

  /**
   * @param arg0
   * @see javax.jdo.PersistenceManagerFactory#setTransactionType(java.lang.String)
   */
  public void setTransactionType(String arg0) {
    pmf.setTransactionType(arg0);
  }

  /**
   * @return
   * @see javax.jdo.PersistenceManagerFactory#supportedOptions()
   */
  public Collection<String> supportedOptions() {
    return pmf.supportedOptions();
  }
  
}
