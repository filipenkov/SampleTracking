package com.atlassian.jira.plugin.ext.bamboo.model;

import org.apache.commons.lang.builder.CompareToBuilder;

public class BambooProject implements Comparable<BambooProject>
{
    private String projectKey;
    private String projectName;

    public BambooProject(String projectKey, String projectName)
    {
        this.projectKey = projectKey;
        this.projectName = projectName;
    }

    public String getKey()
    {
        return projectKey;
    }

    public String getName()
    {
        return projectName;
    }

    public int compareTo(BambooProject o)
    {
        return new CompareToBuilder()
                .append(projectName, o.projectName, String.CASE_INSENSITIVE_ORDER)
                .toComparison();
    }
}