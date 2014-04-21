package com.atlassian.jira.plugin.issuetabpanel;

import com.atlassian.annotations.Internal;
import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Request object used in the {@link IssueTabPanel2} SPI.
 *
 * @see IssueTabPanel2
 * @since v5.0
 */
@PublicApi
@Immutable
final public class GetActionsRequest
{
    private final Issue issue;
    private final User remoteUser;
    private final boolean asynchronous;

    @Internal
    public GetActionsRequest(@Nonnull Issue issue, @Nullable User remoteUser, boolean asynchronous)
    {
        this.issue = checkNotNull(issue);
        this.remoteUser = remoteUser;
        this.asynchronous = asynchronous;
    }

    /**
     * @return the Issue on which the panel will be displayed
     */
    @Nonnull
    public Issue issue()
    {
        return issue;
    }

    /**
     * @return the User that is viewing the page, or null for an anonymous user
     */
    @Nullable
    public User remoteUser()
    {
        return remoteUser;
    }

    /**
     * @return true iff the user that is viewing the page is anonymous (i.e. not logged in)
     */
    public boolean isAnonymous()
    {
        return remoteUser() == null;
    }

    /**
     * @return true if the actions are being loaded asynchronously, e.g. using an AJAX request
     */
    public boolean isAsynchronous()
    {
        return asynchronous;
    }
}
