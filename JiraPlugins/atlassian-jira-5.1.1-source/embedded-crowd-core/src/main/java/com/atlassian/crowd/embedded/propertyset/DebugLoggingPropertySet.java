package com.atlassian.crowd.embedded.propertyset;

import com.opensymphony.module.propertyset.PropertyException;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetSchema;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

/**
 * Adds debug logging when accessing the wrapped {@link PropertySet}.
 */
public final class DebugLoggingPropertySet implements PropertySet, Serializable
{
    private static final Logger log = LoggerFactory.getLogger(DebugLoggingPropertySet.class);
    private static final long serialVersionUID = 1L;
    private final PropertySet delegate;

    public DebugLoggingPropertySet(PropertySet delegate)
    {
        this.delegate = delegate;
    }

    public void setSchema(PropertySetSchema schema) throws PropertyException
    {
        // no need for "if (log.isDebugEnabled())" because we're using MessageFormat-style arguments
        log.debug("Setting schema '{}' for: {}", schema, this);
        delegate.setSchema(schema);
    }

    public PropertySetSchema getSchema() throws PropertyException
    {
        log.debug("Getting schema for: {}", this);
        return delegate.getSchema();
    }

    public void setAsActualType(String key, Object value) throws PropertyException
    {
        log.debug("Setting key '{}' for: {}", key, this);
        delegate.setAsActualType(key, value);
    }

    public Object getAsActualType(String key) throws PropertyException
    {
        log.debug("Getting key '{}' for: {}", key, this);
        return delegate.getAsActualType(key);
    }

    public void setBoolean(String key, boolean value) throws PropertyException
    {
        log.debug("Setting key '{}' for: {}", key, this);
        delegate.setBoolean(key, value);
    }

    public boolean getBoolean(String key) throws PropertyException
    {
        log.debug("Getting key '{}' for: {}", key, this);
        return delegate.getBoolean(key);
    }

    public void setData(String key, byte[] value) throws PropertyException
    {
        log.debug("Setting key '{}' for: {}", key, this);
        delegate.setData(key, value);
    }

    public byte[] getData(String key) throws PropertyException
    {
        log.debug("Getting key '{}' for: {}", key, this);
        return delegate.getData(key);
    }

    public void setDate(String key, Date value) throws PropertyException
    {
        log.debug("Setting key '{}' for: {}", key, this);
        delegate.setDate(key, value);
    }

    public Date getDate(String key) throws PropertyException
    {
        log.debug("Getting key '{}' for: {}", key, this);
        return delegate.getDate(key);
    }

    public void setDouble(String key, double value) throws PropertyException
    {
        log.debug("Setting key '{}' for: {}", key, this);
        delegate.setDouble(key, value);
    }

    public double getDouble(String key) throws PropertyException
    {
        log.debug("Getting key '{}' for: {}", key, this);
        return delegate.getDouble(key);
    }

    public void setInt(String key, int value) throws PropertyException
    {
        log.debug("Setting key '{}' for: {}", key, this);
        delegate.setInt(key, value);
    }

    public int getInt(String key) throws PropertyException
    {
        log.debug("Getting key '{}' for: {}", key, this);
        return delegate.getInt(key);
    }

    public Collection getKeys() throws PropertyException
    {
        log.debug("Getting keys for: {}", this);
        return delegate.getKeys();
    }

    public Collection getKeys(int type) throws PropertyException
    {
        log.debug("Getting keys of type '{}' for: {}", type, this);
        return delegate.getKeys(type);
    }

    public Collection getKeys(String prefix) throws PropertyException
    {
        log.debug("Getting keys with prefix '{}' for: {}", prefix, this);
        return delegate.getKeys(prefix);
    }

    public Collection getKeys(String prefix, int type) throws PropertyException
    {
        log.debug("Getting keys of prefix '{}' and type '{}' for: {}", new Object[]{ prefix, type, this });
        return delegate.getKeys(prefix, type);
    }

    public void setLong(String key, long value) throws PropertyException
    {
        log.debug("Setting key '{}' for: {}", key, this);
        delegate.setLong(key, value);
    }

    public long getLong(String key) throws PropertyException
    {
        log.debug("Getting key '{}' for: {}", key, this);
        return delegate.getLong(key);
    }

    public void setObject(String key, Object value) throws PropertyException
    {
        log.debug("Setting key '{}' for: {}", key, this);
        delegate.setObject(key, value);
    }

    public Object getObject(String key) throws PropertyException
    {
        log.debug("Getting key '{}' for: {}", key, this);
        return delegate.getObject(key);
    }

    public void setProperties(String key, Properties value) throws PropertyException
    {
        log.debug("Setting key '{}' for: {}", key, this);
        delegate.setProperties(key, value);
    }

    public Properties getProperties(String key) throws PropertyException
    {
        log.debug("Getting key '{}' for: {}", key, this);
        return delegate.getProperties(key);
    }

    public boolean isSettable(String property)
    {
        log.debug("Checking key '{}' is settable for: {}", property, this);
        return delegate.isSettable(property);
    }

    public void setString(String key, String value) throws PropertyException
    {
        log.debug("Setting key '{}' for: {}", key, this);
        delegate.setString(key, value);
    }

    public String getString(String key) throws PropertyException
    {
        log.debug("Getting key '{}' for: {}", key, this);
        return delegate.getString(key);
    }

    public void setText(String key, String value) throws PropertyException
    {
        log.debug("Setting key '{}' for: {}", key, this);
        delegate.setText(key, value);
    }

    public String getText(String key) throws PropertyException
    {
        log.debug("Getting key '{}' for: {}", key, this);
        return delegate.getText(key);
    }

    public int getType(String key) throws PropertyException
    {
        log.debug("Getting type of key '{}' for: {}", key, this);
        return delegate.getType(key);
    }

    public void setXML(String key, Document value) throws PropertyException
    {
        log.debug("Setting key '{}' for: {}", key, this);
        delegate.setXML(key, value);
    }

    public Document getXML(String key) throws PropertyException
    {
        log.debug("Getting key '{}' for: {}", key, this);
        return delegate.getXML(key);
    }

    public boolean exists(String key) throws PropertyException
    {
        log.debug("Checking whether key '{}' exists for: {}", key, this);
        return delegate.exists(key);
    }

    public void init(Map config, Map args)
    {
        log.debug("Initialising PropertySet with config: {}, arguments: {}", config, args);
        delegate.init(config, args);
    }

    public void remove(String key) throws PropertyException
    {
        log.debug("Removing key '{}' for: {}", key, this);
        delegate.remove(key);
    }

    public boolean supportsType(int type)
    {
        log.debug("Checking whether type '{}' is supported for: {}", type, this);
        return delegate.supportsType(type);
    }

    public boolean supportsTypes()
    {
        log.debug("Getting supported types for: {}", this);
        return delegate.supportsTypes();
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this).append(delegate).toString();
    }
}