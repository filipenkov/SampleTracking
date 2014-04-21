package com.atlassian.jira.plugin.ext.bamboo.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;

/**
 * Identifier for result objects such as {@link ChainResultsSummary} and {@link BuildResultsSummary}
 *
 * Can be created from {@link PlanKeys}
 */
public final class PlanResultKey implements Serializable
{
    private final PlanKey planKey;
    private final int buildNumber;

    PlanResultKey(final PlanKey planKey, final int buildNumber)
    {
        this.planKey = planKey;
        this.buildNumber = buildNumber;
    }

    /**
     * Returns the key component of this {@link PlanResultKey}
     * @return planKey
     */
    public PlanKey getPlanKey()
    {
        return planKey;
    }

    /**
     * The build number being built
     *
     * @return int
     */
    public int getBuildNumber()
    {
        return buildNumber;
    }
    
    /**
     * Full build result key (e.g. BAM-MAIN-100)
     *
     * @return Key for build result @NotNull
     */
    public String getKey()
    {
        return planKey.getKey() + "-" + getBuildNumber();
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof PlanResultKey))
        {
            return false;
        }

        final PlanResultKey resultKey = (PlanResultKey) o;

        return new EqualsBuilder()
                .append(buildNumber, resultKey.buildNumber)
                .append(planKey, resultKey.planKey)
                .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder()
                .append(31)
                .append(planKey)
                .append(buildNumber)
                .toHashCode();
    }

    @Override
    public String toString()
    {
        return getKey();
    }
}
