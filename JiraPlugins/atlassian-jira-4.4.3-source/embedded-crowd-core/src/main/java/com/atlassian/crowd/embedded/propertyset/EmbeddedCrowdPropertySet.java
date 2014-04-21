package com.atlassian.crowd.embedded.propertyset;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.api.UserWithAttributes;
import com.atlassian.crowd.exception.*;
import com.google.common.collect.ImmutableMap;
import com.opensymphony.module.propertyset.IllegalPropertyException;
import com.opensymphony.module.propertyset.PropertyException;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.map.MapPropertySet;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.w3c.dom.Document;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * An implementation of PropertySet that is backed by Embedded Crowd.
 * This provides limited compatibility with the old User.getPropertySet().
 *<p/>
 * The values are all backed by Strings, so many types are not supported and
 * type safety is not assured. Dates are stored in an ISO 8601 date-time
 * format in UTC.
 * <p/>
 * Updates are pushed straight through to Embedded Crowd, but reads are
 * done only from the copy of the attributes made when the PropertySet
 * is created.
 * <p/>
 * This class is not thread-safe and should not be cached across operations.
 */
public final class EmbeddedCrowdPropertySet extends MapPropertySet
{
    private final CrowdService crowdService;
    private final User user;

    public EmbeddedCrowdPropertySet(UserWithAttributes user, CrowdService crowdService)
    {
        Map<String, String> attributesMap = new HashMap<String, String>();
        for (String key : user.getKeys())
        {
            // get first attribute value from Crowd (what does first mean for unordered attribute values??)
            attributesMap.put(key, user.getValue(key));
        }

        // initialise underlying map
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("map", attributesMap);
        super.init(null, args);

        this.crowdService = crowdService;
        this.user = user;
    }

    public int getType(final String key) throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException("PropertySet does not support types");
    }

    @Override
    public boolean getBoolean(final String key)
    {
        String value = getString(key);
        // For backward compatibility, if not found return default
        if (value == null)
        {
            return false;
        }
        return Boolean.valueOf(value);
    }

    @Override
    public void setBoolean(String key, boolean value) {
        setString(key, Boolean.toString(value));
    }

    @Override
    public int getInt(final String key)
    {
        String value = getString(key);
        // For backward compatibility, if not found return default
        if (value == null)
        {
            return 0;
        }
        return Integer.valueOf(value);
    }

    @Override
    public void setInt(String key, int value) {
        setString(key, Integer.toString(value));
    }

    @Override
    public long getLong(final String key)
    {
        String value = getString(key);
        // For backward compatibility, if not found return default
        if (value == null)
        {
            return 0;
        }
        return Long.valueOf(value);
    }

    @Override
    public void setLong(String key, long value) {
        setString(key, Long.toString(value));
    }

    @Override
    public double getDouble(final String key)
    {
        String value = getString(key);
        // For backward compatibility, if not found return default
        if (value == null)
        {
            return 0;
        }
        return Double.valueOf(value);
    }

    @Override
    public void setDouble(String key, double value) {
        setString(key, Double.toString(value));
    }

    @Override
    public Date getDate(final String key)
    {
        String value = getString(key);
        // For backward compatibility, if not found return default
        if (value == null)
        {
            return null;
        }
        try
        {
            return DateFormats.getDateFormat().parse(value);
        }
        catch (ParseException e)
        {
            throw new PropertyException("Could not parse date '" + value + "'.");
        }
    }

    @Override
    public void setDate(String key, Date value) {
        if (value == null)
        {
            setString(key, null);
            return;
        }
        setString(key, DateFormats.getDateFormat().format(value));
    }

    /**
     * Throws IllegalPropertyException if value length greater than 255.
     * This is the limit enforced by embedded Crowd.
     */
    @Override
    public void setText(String key, String value) throws IllegalPropertyException
    {
        super.setString(key, value);
    }

    @Override
    public byte[] getData(final String key)
    {
        throw new UnsupportedOperationException("Unsupported data type");
    }

    @Override
    public void setData(String key, byte[] value)
    {
        throw new UnsupportedOperationException("Unsupported data type");
    }

    @Override
    public Object getObject(final String key)
    {
        throw new UnsupportedOperationException("Unsupported data type");
    }

    @Override
    public void setObject(String key, Object value)
    {
        throw new UnsupportedOperationException("Unsupported data type");
    }

    @Override
    public Properties getProperties(final String key)
    {
        throw new UnsupportedOperationException("Unsupported data type");
    }

    @Override
    public void setProperties(String key, Properties value)
    {
        throw new UnsupportedOperationException("Unsupported data type");
    }

    @Override
    public Document getXML(final String key)
    {
        throw new UnsupportedOperationException("Unsupported data type");
    }

    @Override
    public void setXML(String key, Document value)
    {
        throw new UnsupportedOperationException("Unsupported data type");
    }

    @Override
    public boolean supportsTypes()
    {
        // See javadoc: We do not support storing the type of object with the value.
        return false;
    }

    @Override
    public synchronized Map getMap()
    {
        // try to keep at least a modicum of privacy with regard to internal data
        return ImmutableMap.copyOf(super.getMap());
    }

    @Override
    public synchronized void setMap(Map map)
    {
        // try to keep at least a modicum of privacy with regard to internal data
        throw new UnsupportedOperationException("Underlying map cannot be changed in this implementation");
    }

    @Override
    public void remove(String key)
    {
        try
        {
            crowdService.removeUserAttribute(user, key);
        }
        catch (OperationNotPermittedException e)
        {
            throw new RuntimeException(e);
        }
        super.remove(key);
    }

    @Override
    protected void setImpl(final int type, final String key, final Object value)
    {
        if (type != PropertySet.STRING && type != PropertySet.TEXT)
        {
            // should never happen, because we've overridden all the set* methods above
            throw new UnsupportedOperationException("EmbeddedCrowdPropertySet doesn't support type '" + type + "'");
        }

        // this implementation only support single-valued attributes, so remove any existing values
        try
        {
            crowdService.removeUserAttribute(user, key);
        }
        catch (OperationNotPermittedException e)
        {
            throw new RuntimeException(e);
        }
        if (value != null) // only store non-null values
        {
            try
            {
                crowdService.setUserAttribute(user, key, (String) value);
            }
            catch (OperationNotPermittedException e)
            {
                throw new RuntimeException(e);
            }
        }
        super.setImpl(type, key, value);
    }

    @Override
    public boolean supportsType(int type)
    {
        switch (type)
        {
            case PropertySet.BOOLEAN:
            case PropertySet.DATE:
            case PropertySet.DOUBLE:
            case PropertySet.INT:
            case PropertySet.LONG:
            case PropertySet.STRING:
            case PropertySet.TEXT:
                return true;
            case PropertySet.DATA:
            case PropertySet.OBJECT:
            case PropertySet.PROPERTIES:
            case PropertySet.XML:
                return false;
        }
        throw new IllegalArgumentException("Unknown type " + type);
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this).append("user", user).toString();
    }
}