package com.atlassian.jira.issue.link;

import com.atlassian.jira.issue.Issue;

import java.util.List;

/**
 * Deals with DB operations on RemoteIssueLinks
 *
 * @since v5.0
 */
public interface RemoteIssueLinkStore
{
    RemoteIssueLink getRemoteIssueLink(Long remoteIssueLinkId);

    List<RemoteIssueLink> getRemoteIssueLinksForIssue(Issue issue);

    /**
     * Returns remote issue links in given issue with given globalId sorted by id.
     *
     * As the index for enforcing these to be unique would be too long, we have to get by without, and presume that
     * duplicates might appear any time.
     *
     * @param issue issue to search
     * @param globalId globalId to search
     * @return remote issue links sorted by id.
     */
    List<RemoteIssueLink> getRemoteIssueLinksByGlobalId(Issue issue, String globalId);

    RemoteIssueLink createRemoteIssueLink(RemoteIssueLink remoteIssueLink);

    void updateRemoteIssueLink(final RemoteIssueLink remoteIssueLink);

    void removeRemoteIssueLink(Long remoteIssueLinkId);
}
