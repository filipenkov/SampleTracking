package com.atlassian.jira.issue.link;

import com.atlassian.jira.issue.Issue;
import org.apache.commons.lang.NotImplementedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mock RemoteIssueLinkStore for testing without persisting in a database.
 *
 * @since v5.0
 */
public class MemoryRemoteIssueLinkStore implements RemoteIssueLinkStore
{
    private final Map<Long, RemoteIssueLink> remoteIssueLinks = new HashMap<Long, RemoteIssueLink>();
    private volatile long nextId;

    @Override
    public RemoteIssueLink getRemoteIssueLink(Long remoteIssueLinkId)
    {
        return remoteIssueLinks.get(remoteIssueLinkId);
    }

    @Override
    public List<RemoteIssueLink> getRemoteIssueLinksForIssue(Issue issue)
    {
        final List<RemoteIssueLink> result = new ArrayList<RemoteIssueLink>();

        for (final RemoteIssueLink remoteIssueLink : remoteIssueLinks.values())
        {
            if (issue.getId().equals(remoteIssueLink.getIssueId()))
            {
                result.add(remoteIssueLink);
            }
        }

        return result;
    }

    @Override
    public List<RemoteIssueLink> getRemoteIssueLinksByGlobalId(Issue issue, String globalId)
    {
        final List<RemoteIssueLink> result = new ArrayList<RemoteIssueLink>();

        for (final RemoteIssueLink remoteIssueLink : getRemoteIssueLinksForIssue(issue))
        {
            if (globalId.equals(remoteIssueLink.getGlobalId()))
            {
                result.add(remoteIssueLink);
            }
        }

        return result;
    }

    @Override
    public RemoteIssueLink createRemoteIssueLink(RemoteIssueLink remoteIssueLink)
    {
        final RemoteIssueLink linkToStore = createBuilder(remoteIssueLink).id(getNextId()).build();
        remoteIssueLinks.put(linkToStore.getId(), linkToStore);
        return linkToStore;
    }

    @Override
    public void updateRemoteIssueLink(RemoteIssueLink remoteIssueLink)
    {
        remoteIssueLinks.put(remoteIssueLink.getId(), remoteIssueLink);
    }

    @Override
    public void removeRemoteIssueLink(Long remoteIssueLinkId)
    {
        remoteIssueLinks.remove(remoteIssueLinkId);
    }

    private RemoteIssueLinkBuilder createBuilder(final RemoteIssueLink remoteIssueLink)
    {
        return new RemoteIssueLinkBuilder(remoteIssueLink);
    }

    private synchronized long getNextId()
    {
        nextId++;
        return nextId;
    }
}
