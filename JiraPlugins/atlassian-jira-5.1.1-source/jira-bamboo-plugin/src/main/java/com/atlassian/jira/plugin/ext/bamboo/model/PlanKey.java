package com.atlassian.jira.plugin.ext.bamboo.model;

import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;

/**
 * Identifier for {@link Plan}
 *
 * {@link PlanKey}s can be created via {@link PlanKeys} 
 */
public final class PlanKey implements Serializable
{
    private final String key;

    PlanKey(final String key)
    {
        this.key = key;
    }

    /**
     * Full plan key (e.g. BAM-MAIN)
     *
     * @return Key for the plan BAM-MAIN @NotNull
     */
    public String getKey()
    {
        return key;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof PlanKey))
        {
            return false;
        }

        final PlanKey key = (PlanKey) o;

        return this.key.equals(key.key);
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder()
                .append(31)
                .append(key)
                .toHashCode();
    }

    @Override
    public String toString()
    {
        return getKey();
    }
}
