package com.atlassian.jira.issue.link;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.exception.UpdateException;
import com.atlassian.jira.issue.Issue;

import java.util.List;

/**
 * Manages {@link RemoteIssueLink remote issue links} between a JIRA issue and remote objects in other remote applications.
 *
 * @since v5.0
 */
@PublicApi
public interface RemoteIssueLinkManager
{
    /**
     * Returns the remote issue link with the given id, or null if it does not exist.
     *
     * @param remoteIssueLinkId the id of the remote issue link to get
     * @return the remote issue link with the given id
     */
    RemoteIssueLink getRemoteIssueLink(Long remoteIssueLinkId);

    /**
     * Gets the remote issue links that link with the given JIRA issue.
     *
     * @param issue the issue to get the remote issue links for
     * @return the remote issue links for the given issue. This will never return null. If no remote issue links are stored against
     * the issue, the list will be empty.
     */
    List<RemoteIssueLink> getRemoteIssueLinksForIssue(Issue issue);

    /**
     * Returns the first remote issue link found that links with the given JIRA issue and has the given globalId, or
     * null if none exists.
     *
     * @param issue the issue to get the remote issue links for
     * @param globalId the globalId to get the remote issue link for
     * @return the remote issue link for the given issue with the given globalId
     */
    RemoteIssueLink getRemoteIssueLinkByGlobalId(Issue issue, String globalId);

    /**
     * Creates the given remote issue link.
     *
     * @param remoteIssueLink the remote issue link to create
     * @param user the current user
     * @return the RemoteIssueLink object that was created including the generated id
     * @throws CreateException
     */
    RemoteIssueLink createRemoteIssueLink(RemoteIssueLink remoteIssueLink, User user) throws CreateException;

    /**
     * Updates the given remote issue link. The remote issue link is updated using all of the values in this object.
     * Null values are written as null, and must adhere to the required field constraints.
     *
     * @param remoteIssueLink the remote issue link to update
     * @param user the current user
     * @throws UpdateException
     */
    void updateRemoteIssueLink(RemoteIssueLink remoteIssueLink, User user) throws UpdateException;

    /**
     * Deletes the given remote issue link id.
     *
     * @param remoteIssueLinkId the id of the remote issue link to delete
     * @param user the current user
     */
    void removeRemoteIssueLink(Long remoteIssueLinkId, User user);

    /**
     * Deletes any remote issue link that links with the given JIRA issue and has the given global id.
     *
     * @param issue the issue of the remote issue link
     * @param globalId the global id of the remote issue link
     * @param user the current user
     * @throws RemoveException if the link could not be deleted
     */
    void removeRemoteIssueLinkByGlobalId(Issue issue, String globalId, User user);
}
