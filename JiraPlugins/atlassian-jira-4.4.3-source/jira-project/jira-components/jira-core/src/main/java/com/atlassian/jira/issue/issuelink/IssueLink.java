package com.atlassian.jira.issue.issuelink;

import com.atlassian.jira.issue.Issue;

/**
 * Represents a link between two issues
 *
 * @since v4.4
 */
public interface IssueLink
{
    /**
     * @return the source issue that is the issue that the link goes from
     */
    Issue getSourceIssue();

    /**
     * @return the destination issue, that is the issue that the link goes to
     */
    Issue getDestinationIssue();

    /**
     * @return the type of link
     */
    IssueLinkType getIssueLinkType();
}
