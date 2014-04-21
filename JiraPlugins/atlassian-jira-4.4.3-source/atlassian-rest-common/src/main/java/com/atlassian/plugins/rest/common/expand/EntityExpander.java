package com.atlassian.plugins.rest.common.expand;

import com.atlassian.plugins.rest.common.expand.resolver.EntityExpanderResolver;

/**
 * <p>Interface for expanding entities.</p>
 * <p>For recursive expansion consider implementing {@link AbstractRecursiveEntityExpander}</p>
 * @param <T> the type of entity to expand.
 */
public interface EntityExpander<T>
{
    /**
     * @param context the current entity context
     * @param entityCrawler the entity crawler to be used for recursive expansion.
     * @param expanderResolver the resolver for finding further expander when doing recursive expansion.
     * @return the expanded entity. This can be a completely different object (of the same type). This MUST NOT be {@code null}.
     */
    T expand(ExpandContext<T> context, EntityExpanderResolver expanderResolver, EntityCrawler entityCrawler);
}
