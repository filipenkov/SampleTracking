package com.atlassian.crowd.embedded.impl;

import com.atlassian.crowd.embedded.api.Attributes;
import com.atlassian.util.concurrent.NotNull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * A general purpose immutable implementation of the {@link Attributes} interface.
 */
public final class ImmutableAttributes implements Attributes, Serializable
{
    private static final long serialVersionUID = -356796362241352598L;

    private final Map<String, Set<String>> map;

    /**
     * Constructs an empty immutable Attributes object.
     */
    public ImmutableAttributes()
    {
        map = Collections.emptyMap();
    }

    /**
     * Constructs an immutable Attributes object from the given Map.
     * An immutable copy of the incoming map is built, so the passed map does not need to be immutable itself.
     * @param attributesMap The incoming attributes Map.
     */
    public ImmutableAttributes(@NotNull final Map<String, Set<String>> attributesMap)
    {
        map = immutableCopyOf(attributesMap);
    }

    /**
     * Constructs an immutable copy of the passed in attributes.
     *
     * @param attributes to clone.
     */
    public ImmutableAttributes(@NotNull final Attributes attributes)
    {
        final ImmutableMap.Builder<String, Set<String>> builder = ImmutableMap.builder();
        for (final String key : attributes.getKeys())
        {
            builder.put(key, ImmutableSet.copyOf(attributes.getValues(key)));
        }
        map = builder.build();
    }

    public Set<String> getValues(final String key)
    {
        // Returns null if the key does not exist as per the interface contract.
        return map.get(key);
    }

    public String getValue(final String key)
    {
        final Set<String> values = getValues(key);
        if ((values == null) || values.isEmpty())
        {
            return null;
        }
        return values.iterator().next();
    }

    public Set<String> getKeys()
    {
        return map.keySet();
    }

    public boolean isEmpty()
    {
        return map.isEmpty();
    }

    private static Map<String, Set<String>> immutableCopyOf(final Map<String, Set<String>> source)
    {
        final ImmutableMap.Builder<String, Set<String>> builder = ImmutableMap.builder();
        for (final Map.Entry<String, Set<String>> entry : source.entrySet())
        {
            builder.put(entry.getKey(), ImmutableSet.copyOf(entry.getValue()));
        }
        return builder.build();
    }
}
