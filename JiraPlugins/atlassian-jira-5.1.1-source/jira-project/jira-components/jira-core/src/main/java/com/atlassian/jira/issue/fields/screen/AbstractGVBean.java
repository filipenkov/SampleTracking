package com.atlassian.jira.issue.fields.screen;

import org.ofbiz.core.entity.GenericValue;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
public abstract class AbstractGVBean
{
    private GenericValue genericValue;
    private boolean modified;

    public GenericValue getGenericValue()
    {
        return genericValue;
    }

    public void setGenericValue(GenericValue genericValue)
    {
        this.genericValue = genericValue;
        init();
    }

    protected abstract void init();

    protected void updateGV(String fieldName, Object value)
    {
        if (genericValue != null)
        {
            if (!valuesEqual(value, genericValue.get(fieldName)))
            {
                genericValue.set(fieldName, value);
                modified = true;
            }
        }
        else
        {
            modified = true;
        }
    }

    protected boolean valuesEqual(Object value1, Object value2)
    {
        if (value1 == null)
            return value2 == null;
        else
            return value1.equals(value2);
    }

    public boolean isModified()
    {
        return modified;
    }

    protected void setModified(boolean modified)
    {
        this.modified = modified;
    }
}
