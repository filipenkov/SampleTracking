package com.atlassian.plugins.rest.common.expand.resolver;

import com.atlassian.plugins.rest.common.expand.EntityExpander;
import static com.google.common.base.Preconditions.*;

abstract class AbstractEntityExpanderResolver implements EntityExpanderResolver
{
    public final <T> boolean hasExpander(T instance)
    {
        return hasExpander(checkNotNull(instance).getClass());
    }

    @SuppressWarnings("unchecked")
    public final <T> EntityExpander<T> getExpander(T instance)
    {
        return (EntityExpander<T>) getExpander(checkNotNull(instance).getClass());
    }
}
