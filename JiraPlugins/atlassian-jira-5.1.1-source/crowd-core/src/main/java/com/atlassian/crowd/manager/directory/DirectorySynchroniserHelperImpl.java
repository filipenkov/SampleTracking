package com.atlassian.crowd.manager.directory;

import com.atlassian.crowd.directory.SynchronisableDirectory;
import com.atlassian.crowd.directory.SynchronisableDirectoryProperties;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.spi.DirectoryDao;
import com.atlassian.crowd.event.directory.DirectoryUpdatedEvent;
import com.atlassian.crowd.exception.DirectoryNotFoundException;
import com.atlassian.crowd.model.directory.DirectoryImpl;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;

import java.util.HashMap;
import java.util.Map;

/**
 * A {@link DirectorySynchroniserHelper} that requests a {@link SynchronisableDirectory} synchronises its cache, and
 * stores synchronisation information in the {@link Directory}'s attributes.
 */
public class DirectorySynchroniserHelperImpl implements DirectorySynchroniserHelper
{
    private final DirectoryDao directoryDao;

    public DirectorySynchroniserHelperImpl(DirectoryDao directoryDao, EventPublisher eventPublisher)
    {
        this.directoryDao = directoryDao;
        eventPublisher.register(this);
    }

    public void updateSyncStartTime(final SynchronisableDirectory synchronisableDirectory)
            throws DirectoryNotFoundException
    {
        Directory directory = findDirectoryById(synchronisableDirectory.getDirectoryId());
        // Attributes map may be immutable - create a new one
        Map<String, String> attributes = new HashMap<String, String>(directory.getAttributes());
        attributes.put(SynchronisableDirectoryProperties.CURRENT_START_SYNC_TIME, String.valueOf(System.currentTimeMillis()));
        attributes.put(SynchronisableDirectoryProperties.IS_SYNCHRONISING, Boolean.TRUE.toString());

        updateAttributes(directory, attributes);
    }

    public void updateSyncEndTime(final SynchronisableDirectory synchronisableDirectory)
            throws DirectoryNotFoundException
    {
        Directory directory = findDirectoryById(synchronisableDirectory.getDirectoryId());
        // Attributes map may be immutable - create a new one
        Map<String, String> directoryAttributes = new HashMap<String, String>(directory.getAttributes());

        String startTime = directoryAttributes.get(SynchronisableDirectoryProperties.CURRENT_START_SYNC_TIME);
        if (startTime == null)
        {
            throw new IllegalStateException("Updating end time with no current start synchronisation time.");
        }

        String duration = String.valueOf(System.currentTimeMillis() - Long.parseLong(startTime));

        directoryAttributes.put(SynchronisableDirectoryProperties.LAST_START_SYNC_TIME, startTime);
        directoryAttributes.put(SynchronisableDirectoryProperties.LAST_SYNC_DURATION_MS, duration);
        directoryAttributes.put(SynchronisableDirectoryProperties.CURRENT_START_SYNC_TIME, null);
        directoryAttributes.put(SynchronisableDirectoryProperties.IS_SYNCHRONISING, Boolean.FALSE.toString());

        updateAttributes(directory, directoryAttributes);
    }

    public boolean isSynchronising(final long synchronisableDirectoryId) throws DirectoryNotFoundException
    {
        final Directory directory = findDirectoryById(synchronisableDirectoryId);
        final String isSynchronising = directory.getAttributes().get(SynchronisableDirectoryProperties.IS_SYNCHRONISING);
        return isSynchronising != null && Boolean.valueOf(isSynchronising);
    }

    private void updateAttributes(final Directory directory, final Map<String, String> attributes)
            throws DirectoryNotFoundException
    {
        DirectoryImpl newDirectory = new DirectoryImpl(directory);
        newDirectory.setAttributes(attributes);
        directoryDao.update(newDirectory);
    }

    private Directory findDirectoryById(final long directoryId) throws DirectoryNotFoundException
    {
        return directoryDao.findById(directoryId);
    }

    /**
     * Removes {@link SynchronisableDirectoryProperties#IS_SYNCHRONISING}
     * attribute from the updated directory, so we can recognise when
     * a directory requires initial synchronisation.
     *
     * @param event directory update event
     */
    @EventListener
    public void handleEvent(final DirectoryUpdatedEvent event)
    {
        final Directory directory = event.getDirectory();
        final Map<String, String> directoryAttributes = new HashMap<String, String>(directory.getAttributes());

        directoryAttributes.remove(SynchronisableDirectoryProperties.IS_SYNCHRONISING);

        try
        {
            updateAttributes(directory, directoryAttributes);
        } catch (DirectoryNotFoundException e)
        {
            // No need to update attributes for non-existing directory.
        }
    }
}
