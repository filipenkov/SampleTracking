package com.atlassian.jira.imports.project.core;

import java.util.Collections;
import java.util.Map;

/**
 * @since v3.13
 */
public class EntityRepresentationImpl implements EntityRepresentation
{
    private final String entityName;
    private final Map entityValues;

    public EntityRepresentationImpl(final String entityName, final Map entityValues)
    {
        this.entityName = entityName;
        this.entityValues = Collections.unmodifiableMap(entityValues);
    }

    public String getEntityName()
    {
        return entityName;
    }

    public Map getEntityValues()
    {
        return entityValues;
    }
}
