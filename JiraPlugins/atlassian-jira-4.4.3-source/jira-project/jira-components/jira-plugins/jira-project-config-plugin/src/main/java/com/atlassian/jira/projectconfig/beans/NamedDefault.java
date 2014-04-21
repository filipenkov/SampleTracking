package com.atlassian.jira.projectconfig.beans;

/**
 * Object with a name and a boolean flag to indicate if it is the default.
 *
 * @since v4.4
 */
public interface NamedDefault
{
    public String getName();
    public boolean isDefault();
}
