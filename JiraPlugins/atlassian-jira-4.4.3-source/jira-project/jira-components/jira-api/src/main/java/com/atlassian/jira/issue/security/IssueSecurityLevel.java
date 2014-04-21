package com.atlassian.jira.issue.security;

/**
 * Defines an issue security level in JIRA.
 *
 * NOTE: there are currently no implementations of this interface, but security levels will be referenced through this
 * interface in the future
 * 
 * @since v4.0
 */
public interface IssueSecurityLevel
{
    /**
     * @return the unique identifier for this issue security level
     */
    Long getId();

    /**
     * @return the user provided name for this issue security level
     */
    String getName();

    /**
     * @return the user provided description for this issue security level
     */
    String getDescription();

    /**
     * @return the unique identifier of the IssueSecurityScheme that this issue security level is associated with
     */
    Long getSchemeId();
}
