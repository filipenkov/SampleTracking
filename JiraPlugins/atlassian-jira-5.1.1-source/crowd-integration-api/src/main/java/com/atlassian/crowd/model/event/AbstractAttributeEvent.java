package com.atlassian.crowd.model.event;

import com.atlassian.crowd.embedded.api.Directory;

import java.util.Map;
import java.util.Set;

public abstract class AbstractAttributeEvent extends AbstractOperationEvent
{
    private final Map<String, Set<String>> storedAttributes;

    private final Set<String> deletedAttributes;

    public AbstractAttributeEvent(Operation operation, Directory directory, Map<String, Set<String>> storedAttributes, Set<String> deletedAttributes)
    {
        super(operation, directory);
        this.storedAttributes = storedAttributes;
        this.deletedAttributes = deletedAttributes;
    }

    public Map<String, Set<String>> getStoredAttributes()
    {
        return storedAttributes;
    }

    public Set<String> getDeletedAttributes()
    {
        return deletedAttributes;
    }
}
