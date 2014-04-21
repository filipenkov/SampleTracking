package com.atlassian.streams.jira.builder;

import java.net.URI;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.vote.VoteManager;
import com.atlassian.jira.issue.watchers.WatcherManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.streams.api.ActivityObjectTypes;
import com.atlassian.streams.api.StreamsEntry;
import com.atlassian.streams.api.StreamsEntry.HasAlternateLinkUri;
import com.atlassian.streams.api.StreamsEntry.HasApplicationType;
import com.atlassian.streams.api.StreamsEntry.HasId;
import com.atlassian.streams.api.StreamsEntry.HasPostedDate;
import com.atlassian.streams.api.StreamsEntry.Link;
import com.atlassian.streams.api.StreamsEntry.NeedsActivityObject;
import com.atlassian.streams.api.StreamsEntry.NeedsAuthors;
import com.atlassian.streams.api.StreamsEntry.NeedsRenderer;
import com.atlassian.streams.api.StreamsEntry.NeedsVerb;
import com.atlassian.streams.api.common.uri.Uris;
import com.atlassian.streams.jira.JiraActivityItem;
import com.atlassian.streams.jira.UriProvider;
import com.atlassian.streams.spi.ServletPath;
import com.atlassian.streams.spi.StreamsUriBuilder;

import com.google.common.collect.ImmutableList;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.atlassian.streams.jira.JiraStreamsActivityProvider.ISSUE_VOTE_REL;
import static com.atlassian.streams.jira.JiraStreamsActivityProvider.PROVIDER_KEY;
import static com.atlassian.streams.spi.StreamsActivityProvider.CSS_LINK_REL;
import static com.atlassian.streams.spi.StreamsActivityProvider.ICON_LINK_REL;
import static com.atlassian.streams.spi.StreamsActivityProvider.REPLY_TO_LINK_REL;
import static com.atlassian.streams.spi.StreamsActivityProvider.WATCH_LINK_REL;
import static com.google.common.base.Preconditions.checkNotNull;

public class JiraEntryBuilderFactory
{
    private static final Logger log = LoggerFactory.getLogger(JiraEntryBuilderFactory.class);

    public static final String JIRA_APPLICATION_TYPE = "com.atlassian.jira";

    private final ApplicationProperties applicationProperties;
    private final PermissionManager permissionManager;
    private final JiraAuthenticationContext authenticationContext;
    private final WatcherManager watcherManager;
    private final VoteManager voteManager;
    private final UriProvider uriProvider;

    JiraEntryBuilderFactory(ApplicationProperties applicationProperties,
            PermissionManager permissionManager,
            JiraAuthenticationContext authenticationContext,
            WatcherManager watcherManager,
            VoteManager voteManager,
            UriProvider uriProvider)
    {
        this.applicationProperties = checkNotNull(applicationProperties, "applicationProperties");
        this.authenticationContext = checkNotNull(authenticationContext, "authenticationContext");
        this.permissionManager = checkNotNull(permissionManager, "permissionManager");
        this.watcherManager = checkNotNull(watcherManager, "watcherManager");
        this.voteManager = checkNotNull(voteManager, "voteManager");
        this.uriProvider = checkNotNull(uriProvider, "uriProvider");
    }

    /**
     *
     * @param item
     * @param itemUri {@code URI} for the issue or comment, as appropriate for the activity
     * @return
     */
    public StreamsEntry.Parameters<HasId, HasPostedDate, HasAlternateLinkUri, HasApplicationType, NeedsRenderer, NeedsActivityObject, NeedsVerb, NeedsAuthors> newParams(JiraActivityItem item,
                                                                                                                                                                         URI itemUri)
    {
        return newParams(item, itemUri, false);
    }

    public StreamsEntry.Parameters<HasId, HasPostedDate, HasAlternateLinkUri, HasApplicationType, NeedsRenderer, NeedsActivityObject, NeedsVerb, NeedsAuthors> newLinkedIssueParams(JiraActivityItem item,
                                                                                                                                                                                    URI itemUri)
    {
        return newParams(item, itemUri, true);
    }

    private StreamsEntry.Parameters<HasId, HasPostedDate, HasAlternateLinkUri, HasApplicationType, NeedsRenderer, NeedsActivityObject, NeedsVerb, NeedsAuthors> newParams(JiraActivityItem item,
                                                                                                                                                                         URI itemUri,
                                                                                                                                                                         boolean issueLinked)
    {
        final StreamsUriBuilder uriBuilder = new StreamsUriBuilder().setUrl(itemUri.toASCIIString());

        if (item.getChangeHistory().isDefined() || item.getComment().isDefined())
        {
            uriBuilder.setTimestamp(item.getDate());
        }

        // The following things are constant across all types of activity
        return StreamsEntry.params()
                .id(uriBuilder.getUri())
                .postedDate(new DateTime(item.getDate()))
                .applicationType(JIRA_APPLICATION_TYPE)
                .alternateLinkUri(itemUri)
                .addLinks(buildLinks(item, issueLinked));
    }

    private Iterable<Link> buildLinks(JiraActivityItem item, boolean issueLinked)
    {
        Issue issue = item.getIssue();
        ImmutableList.Builder<Link> links = ImmutableList.builder();
        links.add(new Link(getIconUrl(applicationProperties.getBaseUrl(), issue), ICON_LINK_REL));
        links.add(new Link(uriProvider.getWikiRendererCssUri(), CSS_LINK_REL));

        // only add inline action links only if the activity is not 2 issues being linked
        if (!issueLinked)
        {
            //the project's permission scheme can be modified to allow anonymous (null) users to comment
            if (permissionManager.hasPermission(Permissions.COMMENT_ISSUE, issue, authenticationContext.getLoggedInUser()))
            {
                links.add(new Link(getReplyToUri(issue.getKey()), REPLY_TO_LINK_REL));
            }

            //watching and voting on issues always requires users to be logged into jira
            if (isUserLoggedIn())
            {
                if (canWatch(issue))
                {
                    links.add(new Link(getIssueWatchUri(issue.getKey()), WATCH_LINK_REL));
                }

                if (canVote(item))
                {
                    links.add(new Link(getIssueVoteUri(issue.getKey()), ISSUE_VOTE_REL));
                }
            }
        }
        return links.build();
    }

    private boolean canWatch(Issue issue)
    {
        try
        {
            return !watcherManager.isWatching(authenticationContext.getLoggedInUser(), issue);
        }
        catch (NullPointerException e)
        {
            if (log.isDebugEnabled())
            {
                log.debug("Error checking if current user is watching " + issue.getKey(), e);
            }
            return false;
        }
    }

    private boolean canVote(JiraActivityItem item)
    {
        Issue issue = item.getIssue();
        try
        {
            return !voteManager.hasVoted(authenticationContext.getLoggedInUser(), issue)
                    && issue.getResolution() == null
                    && !issue.getReporterId().equals(authenticationContext.getLoggedInUser().getName())
                    && !item.getActivity().first().equals(ActivityObjectTypes.comment());
        }
        catch (NullPointerException e)
        {
            if (log.isDebugEnabled())
            {
                log.debug("Error checking if current user can vote on " + issue.getKey(), e);
            }
            return false;
        }
    }

    private boolean isUserLoggedIn()
    {
        return authenticationContext.getLoggedInUser() != null;
    }

    private URI getReplyToUri(final String key)
    {
        return URI.create(applicationProperties.getBaseUrl() + ServletPath.COMMENTS + "/" +
            Uris.encode(PROVIDER_KEY) + "/" + Uris.encode(key)).normalize();
    }

    private URI getIssueWatchUri(final String key)
    {
        return URI.create(applicationProperties.getBaseUrl()
                + "/rest/jira-activity-stream/1.0/actions/issue-watch/" + Uris.encode(key)).normalize();
    }

    private URI getIssueVoteUri(final String key)
    {
        return URI.create(applicationProperties.getBaseUrl()
                + "/rest/jira-activity-stream/1.0/actions/issue-vote/" + Uris.encode(key)).normalize();
    }

    private URI getIconUrl(final String baseUrl, final Issue issue)
    {
        URI potentialURI = URI.create(issue.getIssueTypeObject().getIconUrl());
        if (potentialURI.isAbsolute())
        {
            return potentialURI;
        }
        else
        {
            return URI.create(baseUrl + issue.getIssueTypeObject().getIconUrl());
        }
    }
}
