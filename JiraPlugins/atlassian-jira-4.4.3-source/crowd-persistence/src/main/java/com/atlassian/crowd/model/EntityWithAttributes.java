package com.atlassian.crowd.model;

import com.atlassian.crowd.embedded.api.Attributes;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Base class for any entity that can have attributes.
 */
public abstract class EntityWithAttributes implements Attributes
{
    private final Map<String, Set<String>> attributes;

    public EntityWithAttributes(final Map<String, Set<String>> attributes)
    {
        this.attributes = attributes;
    }

    public Set<String> getValues(String name)
    {
        if (attributes.containsKey(name))
        {
            return Collections.unmodifiableSet(attributes.get(name));
        }
        else
        {
            return Collections.emptySet();
        }
    }

    public String getValue(String name)
    {
        Set<String> values = getValues(name);
        if (!values.isEmpty())
        {
            return values.iterator().next();
        }
        else
        {
            return null;
        }
    }

    public boolean isEmpty()
    {
        return attributes.isEmpty();
    }

    public Set<String> getKeys()
    {
        return Collections.unmodifiableSet(attributes.keySet());
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this).
                append("attributes", attributes).
                toString();
    }
}
