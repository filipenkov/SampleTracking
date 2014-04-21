package com.atlassian.jira.project;

/**
 * Defines a project category in JIRA.
 *
 * @since v4.0
 */
public interface ProjectCategory
{
    /**
     * @return the unique identifier for this project category
     */
    Long getId();

    /**
     * @return the user defined name for this project catetory
     */
    String getName();

    /**
     * @return the user defined description for this project category
     */
    String getDescription();
}
