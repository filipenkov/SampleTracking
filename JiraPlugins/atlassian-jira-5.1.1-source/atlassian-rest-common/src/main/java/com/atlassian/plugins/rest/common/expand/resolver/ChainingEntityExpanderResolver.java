package com.atlassian.plugins.rest.common.expand.resolver;

import com.atlassian.plugins.rest.common.expand.EntityExpander;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

public class ChainingEntityExpanderResolver implements EntityExpanderResolver
{
    private final List<EntityExpanderResolver> resolvers;

    public ChainingEntityExpanderResolver(List<EntityExpanderResolver> resolvers)
    {
        for (EntityExpanderResolver resolver : checkNotNull(resolvers))
        {
            if (resolver == null)
            {
                throw new NullPointerException("Resolver items cannot be null!");
            }
        }
        this.resolvers = resolvers;
    }

    public boolean hasExpander(Class<?> type)
    {
        checkNotNull(type);
        for (EntityExpanderResolver resolver : resolvers)
        {
            if (resolver.hasExpander(type))
            {
                return true;
            }
        }
        return false;
    }
    
    public <T> EntityExpander<T> getExpander(Class<? extends T> type)
    {
        for (EntityExpanderResolver resolver : resolvers)
        {
            final EntityExpander<T> expander = resolver.getExpander(type);
            if (expander != null)
            {
                return expander;
            }
        }
        return null;
    }
}
