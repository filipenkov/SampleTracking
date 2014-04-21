package com.atlassian.jira.portal;

import com.opensymphony.module.propertyset.PropertyException;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetSchema;
import org.w3c.dom.Document;

import java.util.*;

/**
 * Simple mock @{link PropertySet} for dashboard tests. * Not completely implemented *.
*
* @since v3.13
*/
public class MockPropertySet implements com.opensymphony.module.propertyset.PropertySet
{
    private PropertySetSchema propertySetSchema;
    private Map map = new HashMap();

    public MockPropertySet()
    {
        this (new HashMap());
    }

    public MockPropertySet(Map map)
    {
        this.map = map;
    }

    public Map getMap()
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
        map.put(s, new Boolean(b));
    }

    public boolean getBoolean(final String s) throws PropertyException
    {
        return false;
    }

    public void setData(final String s, final byte[] bytes) throws PropertyException
    {
        map.put(s, bytes);
    }

    public byte[] getData(final String s) throws PropertyException
    {
        return new byte[0];
    }

    public void setDate(final String s, final Date date) throws PropertyException
    {
        map.put(s, date);
    }

    public Date getDate(final String s) throws PropertyException
    {
        return null;
    }

    public void setDouble(final String s, final double v) throws PropertyException
    {
        map.put(s, new Double(v));
    }

    public double getDouble(final String s) throws PropertyException
    {
        return 0;
    }

    public void setInt(final String s, final int i) throws PropertyException
    {
        map.put(s, new Integer(i));
    }

    public int getInt(final String s) throws PropertyException
    {
        return 0;
    }

    public Collection getKeys() throws PropertyException
    {
        //wrapping the result in a list here. If you iterate over them and remove() keys you'll get a
        //concurrentmodification excpeiton otherwise.
        return new ArrayList(map.keySet());
    }

    public Collection getKeys(final int i) throws PropertyException
    {
        return map.keySet();
    }

    public Collection getKeys(final String s) throws PropertyException
    {
        return null;
    }

    public Collection getKeys(final String s, final int i) throws PropertyException
    {
        return null;
    }

    public void setLong(final String s, final long l) throws PropertyException
    {
        map.put(s, new Long(l));
    }

    public long getLong(final String s) throws PropertyException
    {
        return 0;
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
        map.put(s, new TextValue(s1));
    }

    public String getText(final String s) throws PropertyException
    {
        return String.valueOf(map.get(s));
    }

    public int getType(final String key) throws PropertyException
    {
        Object value = map.get(key);
        if (value instanceof String)
        {
            return PropertySet.STRING;
        }
        if (value instanceof TextValue)
        {
            return PropertySet.TEXT;
        }
        else
        {
            return PropertySet.OBJECT;
        }
    }

    public void setXML(final String s, final Document document) throws PropertyException
    {
    }

    public Document getXML(final String s) throws PropertyException
    {
        return null;
    }

    public boolean exists(final String s) throws PropertyException
    {
        return map.containsKey(s);
    }

    public void init(final Map map, final Map map1)
    {
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
        return true;
    }

    /**
     * A special object that can be used to indicate a PropertySet.TEXT value
     */
    public static class TextValue
    {
        private final String value;

        public TextValue(String value)
        {
            this.value = value;
        }

        public String getValue()
        {
            return value;
        }

        public String toString()
        {
            return value;
        }

        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            final TextValue textValue = (TextValue) o;

            if (value != null ? !value.equals(textValue.value) : textValue.value != null)
            {
                return false;
            }

            return true;
        }

        public int hashCode()
        {
            return (value != null ? value.hashCode() : 0);
        }
    }
}
