package com.atlassian.plugins.rest.common.expand;

import com.atlassian.plugins.rest.common.expand.parameter.ExpandParameter;

public class DefaultExpandContext<T> implements ExpandContext<T>
{
    private final T entity;
    private final Expandable expandable;
    private final ExpandParameter expandParameter;

    public DefaultExpandContext(T entity, Expandable expandable, ExpandParameter expandParameter)
    {
        this.expandable = expandable;
        this.entity = entity;
        this.expandParameter = expandParameter;
    }

    public Expandable getExpandable()
    {
        return expandable;
    }

    public T getEntity()
    {
        return entity;
    }

    public ExpandParameter getEntityExpandParameter()
    {
        return expandParameter;
    }
}
