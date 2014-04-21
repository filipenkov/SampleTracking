package com.atlassian.crowd.manager.directory;

import com.atlassian.crowd.embedded.api.DirectorySynchronisationInformation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DirectorySynchronisationInformationStoreImpl implements DirectorySynchronisationInformationStore
{
    private final Map<Long, DirectorySynchronisationInformation> syncStatus = new ConcurrentHashMap<Long, DirectorySynchronisationInformation>();

    public DirectorySynchronisationInformation get(long directoryId)
    {
        return syncStatus.get(directoryId);
    }

    public void set(long directoryId, DirectorySynchronisationInformation syncInfo)
    {
        syncStatus.put(directoryId, syncInfo);
    }

    public void clear(long directoryId)
    {
        syncStatus.remove(directoryId);
    }

    public void clear()
    {
        syncStatus.clear();
    }
}
