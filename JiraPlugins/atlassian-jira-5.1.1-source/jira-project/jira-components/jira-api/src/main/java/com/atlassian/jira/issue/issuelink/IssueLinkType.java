package com.atlassian.jira.issue.issuelink;

import com.atlassian.annotations.PublicApi;

/**
 * This interfaces represents the possible linking information between two issues
 *
 * @since v4.3
 *
 * @deprecated Use {@link com.atlassian.jira.issue.link.IssueLinkType} instead. Since v5.0.
 */
@PublicApi
public interface IssueLinkType
{
    /**
     * @return the id of this IssueLinkType
     */
    public Long getId();

    /**
     * @return the name of this IssueLinkType
     */
    public String getName();

    /**
     * @return the outward name of this IssueLinkType
     */
    public String getOutward();

    /**
     * @return the inward name of this IssueLinkType
     */
    public String getInward();

    /**
     * @return the style name of this IssueLinkType
     */
    public String getStyle();

    /**
     * @return true if the link type is a subtask link
     */
    public boolean isSubTaskLinkType();

    /**
     * Checks if this link type is a System Link type. System link types are used by JIRA to denote a special
     * relationship between issues. For example, a sub-task is linked ot its parent issue using a link that is of a
     * system link type.
     *
     * @return true if its a system link type
     */
    public boolean isSystemLinkType();
}
