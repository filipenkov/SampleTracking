package com.atlassian.crowd.directory.ldap.cache;

import com.atlassian.crowd.directory.RemoteCrowdDirectory;
import com.atlassian.crowd.directory.SynchronisableDirectoryProperties;
import com.atlassian.crowd.event.EventTokenExpiredException;
import com.atlassian.crowd.event.Events;
import com.atlassian.crowd.event.IncrementalSynchronisationNotAvailableException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.UnsupportedCrowdApiException;
import com.atlassian.crowd.model.event.GroupEvent;
import com.atlassian.crowd.model.event.GroupMembershipEvent;
import com.atlassian.crowd.model.event.Operation;
import com.atlassian.crowd.model.event.OperationEvent;
import com.atlassian.crowd.model.event.UserEvent;
import com.atlassian.crowd.model.event.UserMembershipEvent;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.group.GroupType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class EventTokenChangedCacheRefresher extends AbstractCacheRefresher
{
    private static final Logger log = LoggerFactory.getLogger(EventTokenChangedCacheRefresher.class);

    private final RemoteDirectoryCacheRefresher fullSyncCacheRefresher;

    private final RemoteCrowdDirectory crowdDirectory;

    private String currentEventToken;

    public EventTokenChangedCacheRefresher(final RemoteCrowdDirectory crowdDirectory)
    {
        super(crowdDirectory);
        this.crowdDirectory = crowdDirectory;
        fullSyncCacheRefresher = new RemoteDirectoryCacheRefresher(remoteDirectory);
    }

    @Override
    public void synchroniseAll(DirectoryCache directoryCache) throws OperationFailedException
    {
        try
        {
            this.currentEventToken = crowdDirectory.getCurrentEventToken();
        }
        catch (UnsupportedCrowdApiException e)
        {
            log.debug("Remote server does not support event based sync.");
        }
        catch (OperationFailedException e)
        {
            log.warn("Could not update event token.", e);
        }

        fullSyncCacheRefresher.synchroniseAll(directoryCache);
    }

    public boolean synchroniseChanges(DirectoryCache directoryCache) throws OperationFailedException
    {
        // When restarting the app, we must do a full refresh the first time.
        if (currentEventToken == null || !Boolean.parseBoolean(crowdDirectory.getValue(SynchronisableDirectoryProperties.INCREMENTAL_SYNC_ENABLED)))
        {
            return false;
        }

        final Events events;
        try
        {
            events = crowdDirectory.getNewEvents(currentEventToken);
        }
        catch (EventTokenExpiredException e)
        {
            currentEventToken = null;
            return false;
        }
        catch (IncrementalSynchronisationNotAvailableException e)
        {
            log.warn("Incremental synchronisation is not available. Falling back to full synchronisation", e);
            currentEventToken = null;
            return false;
        }

        for (OperationEvent event : events.getEvents())
        {
            if (event instanceof UserEvent)
            {
                final UserEvent userEvent = (UserEvent) event;
                if (event.getOperation() == Operation.CREATED || event.getOperation() == Operation.UPDATED)
                {
                    directoryCache.addOrUpdateCachedUser(userEvent.getUser());
                }
                else if (event.getOperation() == Operation.DELETED)
                {
                    directoryCache.deleteCachedUser(userEvent.getUser().getName());
                }
            }
            else if (event instanceof GroupEvent)
            {
                final GroupEvent groupEvent = (GroupEvent) event;
                if (event.getOperation() == Operation.CREATED || event.getOperation() == Operation.UPDATED)
                {
                    directoryCache.addOrUpdateCachedGroup(groupEvent.getGroup());
                }
                else if (event.getOperation() == Operation.DELETED)
                {
                    directoryCache.deleteCachedGroup(groupEvent.getGroup().getName());
                }
            }
            else if (event instanceof UserMembershipEvent)
            {
                UserMembershipEvent membershipEvent = (UserMembershipEvent) event;
                if (event.getOperation() == Operation.CREATED)
                {
                    for (String parentGroupName : membershipEvent.getParentGroupNames())
                    {
                        directoryCache.addUserToGroup(membershipEvent.getChildUsername(), parentGroupName);
                    }
                }
                else if (event.getOperation() == Operation.DELETED)
                {
                    for (String parentGroupName : membershipEvent.getParentGroupNames())
                    {
                        directoryCache.removeUserFromGroup(membershipEvent.getChildUsername(), parentGroupName);
                    }
                }
                else if (event.getOperation() == Operation.UPDATED)
                {
                   directoryCache.syncGroupMembershipsForUser(membershipEvent.getChildUsername(), membershipEvent.getParentGroupNames());
                }
            }
            else if (event instanceof GroupMembershipEvent)
            {
                GroupMembershipEvent membershipEvent = (GroupMembershipEvent) event;
                if (event.getOperation() == Operation.CREATED)
                {
                    for (String parentGroupName : membershipEvent.getParentGroupNames())
                    {
                        directoryCache.addGroupToGroup(membershipEvent.getGroupName(), parentGroupName);
                    }
                }
                else if (event.getOperation() == Operation.DELETED)
                {
                    for (String parentGroupName : membershipEvent.getParentGroupNames())
                    {
                        directoryCache.removeGroupFromGroup(membershipEvent.getGroupName(), parentGroupName);
                    }
                }
                else if (event.getOperation() == Operation.UPDATED)
                {
                    directoryCache.syncGroupMembershipsAndMembersForGroup(membershipEvent.getGroupName(), membershipEvent.getParentGroupNames(), membershipEvent.getChildGroupNames());
                }
            }
            else
            {
                throw new RuntimeException("Unsupported event " + event);
            }
        }

        currentEventToken = events.getNewEventToken();

        return true;
    }

    @Override
    protected void synchroniseAllUsers(DirectoryCache directoryCache) throws OperationFailedException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    protected List<? extends Group> synchroniseAllGroups(GroupType legacyRole, DirectoryCache directoryCache) throws OperationFailedException
    {
        throw new UnsupportedOperationException();
    }
}
