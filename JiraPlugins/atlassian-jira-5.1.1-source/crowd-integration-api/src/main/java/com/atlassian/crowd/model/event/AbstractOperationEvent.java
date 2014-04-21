package com.atlassian.crowd.model.event;

import com.atlassian.crowd.embedded.api.Directory;

public abstract class AbstractOperationEvent implements OperationEvent
{
    private final Operation operation;

    private final Directory directory;

    public AbstractOperationEvent(Operation operation, Directory directory)
    {
        this.operation = operation;
        this.directory = directory;
    }

    public Operation getOperation()
    {
        return operation;
    }

    public Directory getDirectory()
    {
        return directory;
    }
}
