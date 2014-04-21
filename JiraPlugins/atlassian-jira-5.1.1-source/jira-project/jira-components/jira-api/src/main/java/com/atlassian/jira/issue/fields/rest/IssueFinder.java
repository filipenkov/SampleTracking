package com.atlassian.jira.issue.fields.rest;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.rest.json.beans.IssueRefJsonBean;
import com.atlassian.jira.util.ErrorCollection;

import javax.annotation.Nonnull;

/**
 * Finds an issue based on its 'id' or 'key'.
 * For the key, it will follow moved issues, and try to match the key in a case-insensitive way
 *
 * @since v5.0
 */
public interface IssueFinder
{
    /**
     * Finds an issue based on the passed-in id or key.
     *
     * @param issueRef an IssueRefJsonBean that contains either an id or key
     * @param errorCollection an ErrorCollection where any errors will be added
     * @return an Issue or null
     */
    Issue findIssue(@Nonnull IssueRefJsonBean issueRef, @Nonnull ErrorCollection errorCollection);
    
    /**
     * Finds an issue based on the passed-in id or key.
     *
     * @param idOrKey a string that may be an an id or key
     * @param errorCollection an ErrorCollection where any errors will be added
     * @return an Issue or null
     */
    Issue findIssue(@Nonnull String idOrKey, @Nonnull ErrorCollection errorCollection);
}
