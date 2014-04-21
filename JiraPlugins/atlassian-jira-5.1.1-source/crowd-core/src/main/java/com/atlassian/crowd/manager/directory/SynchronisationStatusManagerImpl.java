package com.atlassian.crowd.manager.directory;

import com.atlassian.crowd.directory.SynchronisableDirectoryProperties;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.DirectorySynchronisationInformation;
import com.atlassian.crowd.embedded.api.DirectorySynchronisationRoundInformation;
import com.atlassian.crowd.event.directory.DirectoryDeletedEvent;
import com.atlassian.crowd.event.migration.XMLRestoreStartedEvent;
import com.atlassian.event.api.EventPublisher;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.math.NumberUtils;

import java.io.Serializable;
import java.util.Collections;

public class SynchronisationStatusManagerImpl implements SynchronisationStatusManager
{
    private final DirectorySynchronisationInformationStore store;

    public SynchronisationStatusManagerImpl(DirectorySynchronisationInformationStore store, EventPublisher eventPublisher)
    {
        this.store = store;
        eventPublisher.register(this);
    }

    public void syncStarted(Directory directory)
    {
        final DirectorySynchronisationInformation info = getDirectorySynchronisationInformation(directory);
        final DirectorySynchronisationRoundInformation activeRound = new DirectorySynchronisationRoundInformation(
                System.currentTimeMillis(),
                0,
                "directory.caching.sync.started",
                Collections.<Serializable>emptyList());
        store.set(directory.getId(), new DirectorySynchronisationInformation(info.getLastRound(), activeRound));
    }

    public void syncStatus(long directoryId, String key, Serializable... parameters)
    {
        final DirectorySynchronisationInformation info = store.get(directoryId);
        if (info.getActiveRound() == null)
        {
            throw new IllegalStateException("Cannot update status for a directory that is not currently synchronising");
        }
        final DirectorySynchronisationRoundInformation activeRound = new DirectorySynchronisationRoundInformation(
                info.getActiveRound().getStartTime(),
                0,
                key,
                ImmutableList.of(parameters));
        store.set(directoryId, new DirectorySynchronisationInformation(info.getLastRound(), activeRound));
    }

    public void syncFinished(long directoryId)
    {
        final DirectorySynchronisationInformation info = store.get(directoryId);
        if (info.getActiveRound() == null)
        {
            return; // Synchronisation has already been marked finished
        }
        final DirectorySynchronisationRoundInformation lastRound = new DirectorySynchronisationRoundInformation(
                info.getActiveRound().getStartTime(),
                System.currentTimeMillis() - info.getActiveRound().getStartTime(),
                info.getActiveRound().getStatusKey(),
                info.getActiveRound().getStatusParameters());
        store.set(directoryId, new DirectorySynchronisationInformation(lastRound, null));
    }

    public DirectorySynchronisationInformation getDirectorySynchronisationInformation(Directory directory)
    {
        final DirectorySynchronisationInformation info = store.get(directory.getId());
        if (info == null)
        {
            final long startTime = NumberUtils.toLong(directory.getValue(SynchronisableDirectoryProperties.LAST_START_SYNC_TIME), 0);
            final long duration = NumberUtils.toLong(directory.getValue(SynchronisableDirectoryProperties.LAST_SYNC_DURATION_MS), 0);

            final DirectorySynchronisationRoundInformation lastRound = startTime == 0 ? null : new DirectorySynchronisationRoundInformation(
                    startTime,
                    duration,
                    null,
                    null);

            return new DirectorySynchronisationInformation(lastRound, null);
        }
        else if (info.getActiveRound() != null)
        {
            final DirectorySynchronisationRoundInformation activeRound = new DirectorySynchronisationRoundInformation(
                info.getActiveRound().getStartTime(),
                System.currentTimeMillis() - info.getActiveRound().getStartTime(),
                info.getActiveRound().getStatusKey(),
                info.getActiveRound().getStatusParameters());
            return new DirectorySynchronisationInformation(info.getLastRound(), activeRound);
        }
        else
        {
            return info;
        }
    }

    @com.atlassian.event.api.EventListener
    public void handleEvent(DirectoryDeletedEvent event)
    {
        store.clear(event.getDirectory().getId());
    }

    @com.atlassian.event.api.EventListener
    public void handleEvent(XMLRestoreStartedEvent event)
    {
        store.clear();
    }
}
