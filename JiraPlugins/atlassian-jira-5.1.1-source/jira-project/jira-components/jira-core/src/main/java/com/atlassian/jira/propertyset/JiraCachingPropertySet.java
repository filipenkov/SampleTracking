package com.atlassian.jira.propertyset;

import com.opensymphony.module.propertyset.PropertyException;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetSchema;
import com.opensymphony.util.DataUtil;
import org.w3c.dom.Document;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A PropertySet which decorates another PropertySet and caches the results. Must be correctly initialised via the
 * {@link #init(Map, Map)} before use.
 * <p>
 * Similar to com.opensymphony.module.propertyset.cached.CachingPropertySet class but does more caching.
 * <p>
 * This class is threadsafe. It uses a {@link Lock} to co-ordinate concurrent access. This means that improper
 * publication is avoided as long as {@link #init(Map, Map)} is called from the creating thread before use.
 */
public class JiraCachingPropertySet implements PropertySet, Serializable
{
    private final Lock lock;
    private PropertySet decoratedPS;
    private PropertySetCache propertySetCache;

    public JiraCachingPropertySet()
    {
        this(new ReentrantLock());
    }

    JiraCachingPropertySet(final Lock lock)
    {
        this.lock = lock;
    }

    public void setAsActualType(final String key, final Object value) throws PropertyException
    {
        if (value instanceof Boolean)
        {
            setBoolean(key, DataUtil.getBoolean((Boolean) value));
        }
        else if (value instanceof Integer)
        {
            setInt(key, DataUtil.getInt((Integer) value));
        }
        else if (value instanceof Long)
        {
            setLong(key, DataUtil.getLong((Long) value));
        }
        else if (value instanceof Double)
        {
            setDouble(key, DataUtil.getDouble((Double) value));
        }
        else if (value instanceof String)
        {
            setString(key, (String) value);
        }
        else if (value instanceof Date)
        {
            setDate(key, (Date) value);
        }
        else if (value instanceof Document)
        {
            setXML(key, (Document) value);
        }
        else if (value instanceof byte[])
        {
            setData(key, (byte[]) value);
        }
        else if (value instanceof Properties)
        {
            setProperties(key, (Properties) value);
        }
        else
        {
            setObject(key, value);
        }
    }

    public Object getAsActualType(final String key) throws PropertyException
    {
        final int type = getType(key);
        Object value = null;
        switch (type)
        {
            case BOOLEAN:
                value = getBoolean(key);
                break;

            case INT:
                value = getInt(key);
                break;

            case LONG:
                value = getLong(key);
                break;

            case DOUBLE:
                value = getDouble(key);
                break;

            case STRING:
                value = getString(key);
                break;

            case DATE:
                value = getDate(key);
                break;

            case XML:
                value = getXML(key);
                break;

            case DATA:
                value = getData(key);
                break;

            case PROPERTIES:
                value = getProperties(key);
                break;

            case OBJECT:
                value = getObject(key);
                break;
        }
        return value;
    }

    public void setBoolean(final String key, final boolean value) throws PropertyException
    {
        lock.lock();
        try
        {
            decoratedPS.setBoolean(key, value);
            propertySetCache.setBoolean(key, value);
        }
        finally
        {
            lock.unlock();
        }
    }

    public boolean getBoolean(final String key) throws PropertyException
    {
        // First try to get the value from cache
        try
        {
            return propertySetCache.getBoolean(key);
        }
        catch (final PropertySetCache.NoValueCachedException ex)
        {
            // Cache miss - we obtain a lock so we can safely read from the underlying PropertySet and write the
            // retrieved value to our cache in an atomic operation.
            lock.lock();
            try
            {
                // Try the cache again - we may save a DB lookup. The faster we unlock the better.
                return propertySetCache.getBoolean(key);
                // If we get here, then we got lucky!
            }
            catch (final PropertySetCache.NoValueCachedException ex2)
            {
                // The cache is still missing this key - retrieve the value from the underlying PropertySet
                final boolean value = decoratedPS.getBoolean(key);
                // cache it
                propertySetCache.setBoolean(key, value);
                return value;
            }
            finally
            {
                lock.unlock();
            }
        }
    }

    public void setData(final String key, final byte[] value) throws PropertyException
    {
        lock.lock();
        try
        {
            decoratedPS.setData(key, value);
            propertySetCache.setData(key, value);
        }
        finally
        {
            lock.unlock();
        }
    }

    public byte[] getData(final String key) throws PropertyException
    {
        // First try to get the value from cache
        try
        {
            return propertySetCache.getData(key);
        }
        catch (final PropertySetCache.NoValueCachedException ex)
        {
            // Cache miss - we obtain a lock so we can safely read from the underlying PropertySet and write the
            // retrieved value to our cache in an atomic operation.
            lock.lock();
            try
            {
                // Try the cache again - we may save a DB lookup. The faster we unlock the better.
                return propertySetCache.getData(key);
                // If we get here, then we got lucky!
            }
            catch (final PropertySetCache.NoValueCachedException ex2)
            {
                // The cache is still missing this key - retrieve the value from the underlying PropertySet
                final byte[] value = decoratedPS.getData(key);
                // cache it
                propertySetCache.setData(key, value);
                return value;
            }
            finally
            {
                lock.unlock();
            }
        }
    }

    public void setDate(final String key, final Date value) throws PropertyException
    {
        lock.lock();
        try
        {
            decoratedPS.setDate(key, value);
            propertySetCache.setDate(key, value);
        }
        finally
        {
            lock.unlock();
        }
    }

    public Date getDate(final String key) throws PropertyException
    {
        // First try to get the value from cache
        try
        {
            return propertySetCache.getDate(key);
        }
        catch (final PropertySetCache.NoValueCachedException ex)
        {
            // Cache miss - we obtain a lock so we can safely read from the underlying PropertySet and write the
            // retrieved value to our cache in an atomic operation.
            lock.lock();
            try
            {
                // Try the cache again - we may save a DB lookup. The faster we unlock the better.
                return propertySetCache.getDate(key);
                // If we get here, then we got lucky!
            }
            catch (final PropertySetCache.NoValueCachedException ex2)
            {
                // The cache is still missing this key - retrieve the value from the underlying PropertySet
                final Date value = decoratedPS.getDate(key);
                // cache it
                propertySetCache.setDate(key, value);
                return value;
            }
            finally
            {
                lock.unlock();
            }
        }
    }

    public void setDouble(final String key, final double value) throws PropertyException
    {
        lock.lock();
        try
        {
            decoratedPS.setDouble(key, value);
            propertySetCache.setDouble(key, value);
        }
        finally
        {
            lock.unlock();
        }
    }

    public double getDouble(final String key) throws PropertyException
    {
        // First try to get the value from cache
        try
        {
            return propertySetCache.getDouble(key);
        }
        catch (final PropertySetCache.NoValueCachedException ex)
        {
            // Cache miss - we obtain a lock so we can safely read from the underlying PropertySet and write the
            // retrieved value to our cache in an atomic operation.
            lock.lock();
            try
            {
                // Try the cache again - we may save a DB lookup. The faster we unlock the better.
                return propertySetCache.getDouble(key);
                // If we get here, then we got lucky!
            }
            catch (final PropertySetCache.NoValueCachedException ex2)
            {
                // The cache is still missing this key - retrieve the value from the underlying PropertySet
                final double value = decoratedPS.getDouble(key);
                // cache it
                propertySetCache.setDouble(key, value);
                return value;
            }
            finally
            {
                lock.unlock();
            }
        }
    }

    public void setInt(final String key, final int value) throws PropertyException
    {
        lock.lock();
        try
        {
            decoratedPS.setInt(key, value);
            propertySetCache.setInt(key, value);
        }
        finally
        {
            lock.unlock();
        }
    }

    public int getInt(final String key) throws PropertyException
    {
        // First try to get the value from cache
        try
        {
            return propertySetCache.getInt(key);
        }
        catch (final PropertySetCache.NoValueCachedException ex)
        {
            // Cache miss - we obtain a lock so we can safely read from the underlying PropertySet and write the
            // retrieved value to our cache in an atomic operation.
            lock.lock();
            try
            {
                // Try the cache again - we may save a DB lookup. The faster we unlock the better.
                return propertySetCache.getInt(key);
                // If we get here, then we got lucky!
            }
            catch (final PropertySetCache.NoValueCachedException ex2)
            {
                // The cache is still missing this key - retrieve the value from the underlying PropertySet
                final int value = decoratedPS.getInt(key);
                // cache it
                propertySetCache.setInt(key, value);
                return value;
            }
            finally
            {
                lock.unlock();
            }
        }
    }

    public Collection getKeys() throws PropertyException
    {
        return decoratedPS.getKeys();
    }

    public Collection getKeys(final int type) throws PropertyException
    {
        return decoratedPS.getKeys(type);
    }

    public Collection getKeys(final String prefix) throws PropertyException
    {
        return decoratedPS.getKeys(prefix);
    }

    public Collection getKeys(final String prefix, final int type) throws PropertyException
    {
        return decoratedPS.getKeys(prefix, type);
    }

    public void setLong(final String key, final long value) throws PropertyException
    {
        lock.lock();
        try
        {
            decoratedPS.setLong(key, value);
            propertySetCache.setLong(key, value);
        }
        finally
        {
            lock.unlock();
        }
    }

    public long getLong(final String key) throws PropertyException
    {
        // First try to get the value from cache
        try
        {
            return propertySetCache.getLong(key);
        }
        catch (final PropertySetCache.NoValueCachedException ex)
        {
            // Cache miss - we obtain a lock so we can safely read from the underlying PropertySet and write the
            // retrieved value to our cache in an atomic operation.
            lock.lock();
            try
            {
                // Try the cache again - we may save a DB lookup. The faster we unlock the better.
                return propertySetCache.getLong(key);
                // If we get here, then we got lucky!
            }
            catch (final PropertySetCache.NoValueCachedException ex2)
            {
                // The cache is still missing this key - retrieve the value from the underlying PropertySet
                final long value = decoratedPS.getLong(key);
                // cache it
                propertySetCache.setLong(key, value);
                return value;
            }
            finally
            {
                lock.unlock();
            }
        }
    }

    public void setObject(final String key, final Object value) throws PropertyException
    {
        lock.lock();
        try
        {
            decoratedPS.setObject(key, value);
            propertySetCache.setObject(key, value);
        }
        finally
        {
            lock.unlock();
        }
    }

    public Object getObject(final String key) throws PropertyException
    {
        // First try to get the value from cache
        try
        {
            return propertySetCache.getObject(key);
        }
        catch (final PropertySetCache.NoValueCachedException ex)
        {
            // Cache miss - we obtain a lock so we can safely read from the underlying PropertySet and write the
            // retrieved value to our cache in an atomic operation.
            lock.lock();
            try
            {
                // Try the cache again - we may save a DB lookup. The faster we unlock the better.
                return propertySetCache.getObject(key);
                // If we get here, then we got lucky!
            }
            catch (final PropertySetCache.NoValueCachedException ex2)
            {
                // The cache is still missing this key - retrieve the value from the underlying PropertySet
                final Object value = decoratedPS.getObject(key);
                // cache it
                propertySetCache.setObject(key, value);
                return value;
            }
            finally
            {
                lock.unlock();
            }
        }
    }

    public void setProperties(final String key, final Properties value) throws PropertyException
    {
        lock.lock();
        try
        {
            decoratedPS.setProperties(key, value);
            propertySetCache.setProperties(key, value);
        }
        finally
        {
            lock.unlock();
        }
    }

    public Properties getProperties(final String key) throws PropertyException
    {
        // First try to get the value from cache
        try
        {
            return propertySetCache.getProperties(key);
        }
        catch (final PropertySetCache.NoValueCachedException ex)
        {
            // Cache miss - we obtain a lock so we can safely read from the underlying PropertySet and write the
            // retrieved value to our cache in an atomic operation.
            lock.lock();
            try
            {
                // Try the cache again - we may save a DB lookup. The faster we unlock the better.
                return propertySetCache.getProperties(key);
                // If we get here, then we got lucky!
            }
            catch (final PropertySetCache.NoValueCachedException ex2)
            {
                // The cache is still missing this key - retrieve the value from the underlying PropertySet
                final Properties value = decoratedPS.getProperties(key);
                // cache it
                propertySetCache.setProperties(key, value);
                return value;
            }
            finally
            {
                lock.unlock();
            }
        }
    }

    public void setSchema(final PropertySetSchema schema) throws PropertyException
    {
        decoratedPS.setSchema(schema);
    }

    public PropertySetSchema getSchema() throws PropertyException
    {
        return decoratedPS.getSchema();
    }

    public boolean isSettable(final String property)
    {
        return decoratedPS.isSettable(property);
    }

    public void setString(final String key, final String value) throws PropertyException
    {
        lock.lock();
        try
        {
            decoratedPS.setString(key, value);
            propertySetCache.setString(key, value);
        }
        finally
        {
            lock.unlock();
        }
    }

    public String getString(final String key) throws PropertyException
    {
        // First try to get the value from cache
        try
        {
            return propertySetCache.getString(key);
        }
        catch (final PropertySetCache.NoValueCachedException ex)
        {
            // Cache miss - we obtain a lock so we can safely read from the underlying PropertySet and write the
            // retrieved value to our cache in an atomic operation.
            lock.lock();
            try
            {
                // Try the cache again - we may save a DB lookup. The faster we unlock the better.
                return propertySetCache.getString(key);
                // Another thread just filled it in - lucky us!
            }
            catch (final PropertySetCache.NoValueCachedException ex2)
            {
                // The cache is still missing this key - retrieve the value from the underlying PropertySet
                final String value = decoratedPS.getString(key);
                // cache it
                propertySetCache.setObject(key, value);
                return value;
            }
            finally
            {
                lock.unlock();
            }
        }
    }

    public void setText(final String key, final String value) throws PropertyException
    {
        lock.lock();
        try
        {
            decoratedPS.setText(key, value);
            propertySetCache.setText(key, value);
        }
        finally
        {
            lock.unlock();
        }
    }

    public String getText(final String key) throws PropertyException
    {
        // First try to get the value from cache
        try
        {
            return propertySetCache.getText(key);
        }
        catch (final PropertySetCache.NoValueCachedException ex)
        {
            // Cache miss - we obtain a lock so we can safely read from the underlying PropertySet and write the
            // retrieved value to our cache in an atomic operation.
            lock.lock();
            try
            {
                // Try the cache again - we may save a DB lookup. The faster we unlock the better.
                return propertySetCache.getText(key);
                // If we get here, then we got lucky!
            }
            catch (final PropertySetCache.NoValueCachedException ex2)
            {
                // The cache is still missing this key - retrieve the value from the underlying PropertySet
                final String value = decoratedPS.getText(key);
                // cache it
                propertySetCache.setText(key, value);
                return value;
            }
            finally
            {
                lock.unlock();
            }
        }
    }

    public int getType(final String key) throws PropertyException
    {
        // First try to get the value from cache
        try
        {
            return propertySetCache.getType(key);
        }
        catch (final PropertySetCache.NoValueCachedException ex)
        {
            // Cache miss - we obtain a lock so we can safely read from the underlying PropertySet and write the
            // retrieved value to our cache in an atomic operation.
            lock.lock();
            try
            {
                // Try the cache again - we may save a DB lookup. The faster we unlock the better.
                return propertySetCache.getType(key);
                // If we get here, then we got lucky!
            }
            catch (final PropertySetCache.NoValueCachedException ex2)
            {
                // The cache is still missing this key - retrieve the value from the underlying PropertySet
                final int value = decoratedPS.getType(key);
                // cache it
                propertySetCache.setType(key, value);
                return value;
            }
            finally
            {
                lock.unlock();
            }
        }
    }

    public void setXML(final String key, final Document value) throws PropertyException
    {
        lock.lock();
        try
        {
            decoratedPS.setXML(key, value);
            propertySetCache.setXML(key, value);
        }
        finally
        {
            lock.unlock();
        }
    }

    public Document getXML(final String key) throws PropertyException
    {
        // First try to get the value from cache
        try
        {
            return propertySetCache.getXML(key);
        }
        catch (final PropertySetCache.NoValueCachedException ex)
        {
            // Cache miss - we obtain a lock so we can safely read from the underlying PropertySet and write the
            // retrieved value to our cache in an atomic operation.
            lock.lock();
            try
            {
                // Try the cache again - we may save a DB lookup. The faster we unlock the better.
                return propertySetCache.getXML(key);
                // If we get here, then we got lucky!
            }
            catch (final PropertySetCache.NoValueCachedException ex2)
            {
                // The cache is still missing this key - retrieve the value from the underlying PropertySet
                final Document value = decoratedPS.getXML(key);
                // cache it
                propertySetCache.setXML(key, value);
                return value;
            }
            finally
            {
                lock.unlock();
            }
        }
    }

    public boolean exists(final String key) throws PropertyException
    {
        // The PropertySet interface does not clearly define the meaning of this method.
        // There is some possible confusion over whether a value with an EXPLICIT null value "exists" or not.
        // A brief look at MemoryPropertySet and OFBizPropertySet leads me to believe that those implementations
        // define an explicit null value as "existing".
        // We will cache the existance of a key separately in the propertySetCache, and set it with the values from the
        // underlying PropertySet to ensure compatibility and consistency with however the underlying PropertySet behaves.

        // Check if we have the result for the exists() call cached.
        Boolean exists = propertySetCache.exists(key);
        if (exists != null)
        {
            // Cache hit - use it.
            return exists;
        }
        // Cache missed. Obtain a lock so we can safely update the cache.
        lock.lock();
        try
        {
            // Try the cache again in case another thread has already updated it.
            exists = propertySetCache.exists(key);
            if (exists != null)
            {
                return exists;
            }
            // If not check the decoratedPS
            final boolean keyExists = decoratedPS.exists(key);
            // Cache the result
            propertySetCache.cacheExistance(key, keyExists);
            return keyExists;
        }
        finally
        {
            lock.unlock();
        }
    }

    /**
     * Initialises this JiraCachingPropertySet. PropertySetManager first constructs an instance of a PropertySet, and
     * then calls init().
     * <p>
     * The <code>args</code> parameter must always contain an entry of type PropertySet under the key "PropertySet".
     * If <code>args</code> contains a <code>Boolean</code> entry under the key of "bulkload" which is set to
     * <code>true</code>, then all the values in the underlying PropertySet will be preloaded into the cache.
     * </p>
     * 
     * @param config
     *            Config from PropertySetConfig
     * @param args
     *            Map of args passed to PropertySetManager.getInstance()
     * @see PropertySet#init(java.util.Map,java.util.Map)
     * @see com.opensymphony.module.propertyset.PropertySetManager#getInstance(String,java.util.Map)
     */
    public void init(final Map config, final Map args)
    {
        lock.lock();
        try
        {
            decoratedPS = (PropertySet) args.get("PropertySet");
            if (decoratedPS == null)
            {
                // we are being constructed without a delegate, die now instead of dying later as per JRA-13778
                throw new NullPointerException("Decorated property set is missing! Cannot initialise.");
            }
            propertySetCache = new PropertySetCache();
            final Boolean bulkload = (Boolean) args.get("bulkload");
            if ((bulkload != null) && bulkload)
            {
                propertySetCache.bulkLoad(decoratedPS);
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    public void remove(final String key) throws PropertyException
    {
        lock.lock();
        try
        {
            propertySetCache.remove(key);
            decoratedPS.remove(key);
        }
        finally
        {
            lock.unlock();
        }
    }

    public boolean supportsType(final int type)
    {
        return decoratedPS.supportsType(type);
    }

    public boolean supportsTypes()
    {
        return decoratedPS.supportsTypes();
    }

    @Override
    public void remove() throws PropertyException
    {
        lock.lock();
        try
        {
            decoratedPS.remove();
            propertySetCache.clear();
        }
        finally
        {
            lock.unlock();
        }
    }
}
