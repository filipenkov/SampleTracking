package com.atlassian.jira.permission;

/**
 * A simple interface that defines a permission
 */
public interface Permission
{
    public String getId();

    public String getName();

    public String getNameKey();

    public String getDescription();
}
