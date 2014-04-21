package com.atlassian.crowd.directory;

import com.atlassian.crowd.embedded.api.Attributes;
import org.apache.commons.lang.math.NumberUtils;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Concrete implementation of Attributes interface.
 *
 * This is useful for places where we want 'composition' instead of 'inheritence'.
 */
public class AttributeValuesHolder implements Attributes
{
    private final Map<String, String> attributes;

    public AttributeValuesHolder(final Map<String, String> attributes)
    {
        this.attributes = attributes;
    }

    public Set<String> getValues(final String name)
    {
        String value = getValue(name);
        if (value != null)
        {
            return Collections.singleton(value);
        }
        else
        {
            return null;
        }
    }

    public String getValue(final String name)
    {
        return attributes.get(name);
    }

    public long getAttributeAsLong(final String name, long defaultValue)
    {
        String value = getValue(name);
        if (NumberUtils.isNumber(value))
        {
            return NumberUtils.createLong(value);
        }
        else
        {
            return defaultValue;
        }
    }

    public boolean getAttributeAsBoolean(final String name, boolean defaultValue)
    {
        String value = getValue(name);
        if (value != null)
        {
            return Boolean.valueOf(value);
        }
        else
        {
            return defaultValue;
        }
    }

    public Set<String> getKeys()
    {
        return attributes.keySet();
    }

    public boolean isEmpty()
    {
        return attributes.isEmpty();
    }

    public Map<String, String> getAttributes()
    {
        return attributes;
    }
}