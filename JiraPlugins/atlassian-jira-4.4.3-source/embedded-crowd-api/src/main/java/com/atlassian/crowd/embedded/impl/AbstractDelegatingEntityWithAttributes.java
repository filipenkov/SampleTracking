package com.atlassian.crowd.embedded.impl;

import com.atlassian.crowd.embedded.api.Attributes;

import java.util.Set;

/**
 * Abstract class providing a delegating implementation of the {@link Attributes} interface.
 * This is the common behaviour of {@link DelegatingUserWithAttributes} and  {@link DelegatingGroupWithAttributes}.
 */
public abstract class AbstractDelegatingEntityWithAttributes implements Attributes
{
    private final Attributes attributes;

    public AbstractDelegatingEntityWithAttributes(Attributes attributes) {

        this.attributes = attributes;
    }

    public Set<String> getValues(final String key)
    {
        return attributes.getValues(key);
    }

    public String getValue(final String key)
    {
        return attributes.getValue(key);
    }

    public Set<String> getKeys()
    {
        return attributes.getKeys();
    }

    public boolean isEmpty()
    {
        return attributes.isEmpty();
    }
}
