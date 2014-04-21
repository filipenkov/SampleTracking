package com.atlassian.plugins.rest.common.expand.resolver;

import com.atlassian.plugins.rest.common.expand.AbstractRecursiveEntityExpander;
import com.atlassian.plugins.rest.common.expand.EntityExpander;

public class IdentityEntityExpanderResolver implements EntityExpanderResolver
{
    private final static EntityExpander IDENTITY = new IdentityExpander();

    public boolean hasExpander(Class<?> type)
    {
        return true;
    }

    public <T> EntityExpander<T> getExpander(Class<? extends T> type)
    {
        return IDENTITY;
    }

    private static class IdentityExpander extends AbstractRecursiveEntityExpander<Object>
    {
        protected Object expandInternal(Object entity)
        {
            return entity;
        }
    }
}
