package com.atlassian.testing.stubs;

import com.google.common.base.Predicate;
import com.opensymphony.module.propertyset.PropertyException;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetSchema;
import org.w3c.dom.Document;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

/**
 * An in-memory implementation of a PropertySet backed by a {@link HashMap} instance.
 *
 * @since v5.1
 */
public class InMemoryPropertySet implements PropertySet
{
    private PropertySetSchema propertySetSchema;

    private Map<String, Object> map = newHashMap();

    public InMemoryPropertySet(final Map<String, Object> map)
    {
        this.map = map;
    }

    public Map<String, Object> asMap()
    {
        return map;
    }

    public void setSchema(final PropertySetSchema propertySetSchema) throws PropertyException
    {
        this.propertySetSchema = propertySetSchema;
    }

    public PropertySetSchema getSchema() throws PropertyException
    {
        return propertySetSchema;
    }

    public void setAsActualType(final String s, final Object o) throws PropertyException
    {
        map.put(s, o);
    }

    public Object getAsActualType(final String s) throws PropertyException
    {
        return map.get(s);
    }

    public void setBoolean(final String s, final boolean b) throws PropertyException
    {
        map.put(s, b);
    }

    public boolean getBoolean(final String s) throws PropertyException
    {
        return (Boolean) map.get(s);
    }

    public void setData(final String s, final byte[] bytes) throws PropertyException
    {
        map.put(s, bytes);
    }

    public byte[] getData(final String s) throws PropertyException
    {
        return (byte[]) map.get(s);
    }

    public void setDate(final String s, final Date date) throws PropertyException
    {
        map.put(s, date);
    }

    public Date getDate(final String s) throws PropertyException
    {
        return (Date) map.get(s);
    }

    public void setDouble(final String s, final double v) throws PropertyException
    {
        map.put(s, v);
    }

    public double getDouble(final String s) throws PropertyException
    {
        return (Double) map.get(s);
    }

    public void setInt(final String s, final int i) throws PropertyException
    {
        map.put(s, i);
    }

    public int getInt(final String s) throws PropertyException
    {
        return (Integer) map.get(s);
    }

    public Collection getKeys() throws PropertyException
    {
        //wrapping the result in a list here. If you iterate over them and remove() keys you'll get a
        //concurrentmodification excpeiton otherwise.
        return newArrayList(map.keySet());
    }

    public Collection getKeys(final int i) throws PropertyException
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public Collection getKeys(final String prefix) throws PropertyException
    {
        final Set<String> allKeys = map.keySet();
        return newArrayList
                (
                        filter(allKeys, new Predicate<String>()
                        {
                            public boolean apply(final String aKey)
                            {
                                return aKey.startsWith(prefix);
                            }
                        })
                );
    }

    public Collection getKeys(final String s, final int i) throws PropertyException
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public void setLong(final String s, final long l) throws PropertyException
    {
        map.put(s, l);
    }

    public long getLong(final String s) throws PropertyException
    {
        return (Long) map.get(s);
    }

    public void setObject(final String s, final Object o) throws PropertyException
    {
        map.put(s, o);
    }

    public Object getObject(final String s) throws PropertyException
    {
        return map.get(s);
    }

    public void setProperties(final String s, final Properties properties) throws PropertyException
    {
        map.put(s, properties);
    }

    public Properties getProperties(final String s) throws PropertyException
    {
        return (Properties) map.get(s);
    }

    public boolean isSettable(final String s)
    {
        return true;
    }

    public void setString(final String s, final String s1) throws PropertyException
    {
        map.put(s, s1);
    }

    public String getString(final String s) throws PropertyException
    {
        return (String) map.get(s);
    }

    public void setText(final String s, final String s1) throws PropertyException
    {
        map.put(s, s1);
    }

    public String getText(final String s) throws PropertyException
    {
        return (String) map.get(s);
    }

    public int getType(final String key) throws PropertyException
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public void setXML(final String s, final Document document) throws PropertyException
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public Document getXML(final String s) throws PropertyException
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public boolean exists(final String s) throws PropertyException
    {
        return map.containsKey(s);
    }

    public void init(final Map map, final Map map1)
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public void remove(final String s) throws PropertyException
    {
        map.remove(s);
    }

    public void remove() throws PropertyException
    {
        map.clear();
    }

    public boolean supportsType(final int i)
    {
        return true;
    }

    public boolean supportsTypes()
    {
        return false;
    }
}
