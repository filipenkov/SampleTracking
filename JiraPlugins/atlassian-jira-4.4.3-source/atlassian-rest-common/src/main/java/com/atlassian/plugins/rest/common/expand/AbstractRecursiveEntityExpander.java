package com.atlassian.plugins.rest.common.expand;

import com.atlassian.plugins.rest.common.expand.resolver.EntityExpanderResolver;

public abstract class AbstractRecursiveEntityExpander<T> implements EntityExpander<T>
{
    public final T expand(ExpandContext<T> context, EntityExpanderResolver expanderResolver, EntityCrawler entityCrawler)
    {
        final T expandedEntity = expandInternal(context.getEntity());
        if (!context.getEntityExpandParameter().isEmpty())
        {
            entityCrawler.crawl(expandedEntity, context.getEntityExpandParameter().getExpandParameter(context.getExpandable()), expanderResolver);
        }
        return expandedEntity;
    }

    protected abstract T expandInternal(T entity);
}
