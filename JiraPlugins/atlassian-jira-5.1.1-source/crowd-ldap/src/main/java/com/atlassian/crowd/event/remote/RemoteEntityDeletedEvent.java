package com.atlassian.crowd.event.remote;

public abstract class RemoteEntityDeletedEvent extends RemoteDirectoryEvent
{
    private final String entityName;

    protected RemoteEntityDeletedEvent(Object source, long directoryID, String entityName)
    {
        super(source, directoryID);
        this.entityName = entityName;
    }

    public String getEntityName()
    {
        return entityName;
    }
}
