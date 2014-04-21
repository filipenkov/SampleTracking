package com.atlassian.crowd.directory;

import com.atlassian.crowd.embedded.api.Attributes;
import org.apache.commons.collections.CollectionUtils;

import java.util.Map;
import java.util.Set;

/**
 * Concrete implementation of Attributes interface for multi-valued attributes.
 */
public class MultiValuedAttributeValuesHolder implements Attributes
{
    private final Map<String, Set<String>> attributes;

    public MultiValuedAttributeValuesHolder(final Map<String, Set<String>> attributes)
    {
        this.attributes = attributes;
    }

    public Set<String> getValues(final String name)
    {
        return attributes.get(name);
    }

    public String getValue(final String name)
    {
        final Set<String> values = getValues(name);
        if (CollectionUtils.isEmpty(values))
        {
            return null;
        }
        return values.iterator().next();
    }

    public Set<String> getKeys()
    {
        return attributes.keySet();
    }

    public boolean isEmpty()
    {
        return attributes.isEmpty();
    }
}