package com.atlassian.jira.plugin.ext.bamboo.model;

import org.apache.commons.lang.builder.CompareToBuilder;

public class BambooPlan implements Comparable<BambooPlan>
{
    private String planKey;
    private String planName;
    private String shortName;

    public BambooPlan(String planKey, String planName, String shortName)
    {
        this.planKey = planKey;
        this.planName = planName;
        this.shortName = shortName;
    }

    public String getKey()
    {
        return planKey;
    }

    public String getName()
    {
        return planName;
    }

    public String getShortName()
    {
        return shortName;
    }

    public int compareTo(BambooPlan o)
    {
        return new CompareToBuilder()
                .append(planName, o.planName, String.CASE_INSENSITIVE_ORDER)
                .toComparison();
    }
}