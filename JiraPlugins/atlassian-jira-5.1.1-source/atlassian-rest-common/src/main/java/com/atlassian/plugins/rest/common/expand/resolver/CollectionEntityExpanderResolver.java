package com.atlassian.plugins.rest.common.expand.resolver;

import com.atlassian.plugins.rest.common.expand.DefaultExpandContext;
import com.atlassian.plugins.rest.common.expand.EntityCrawler;
import com.atlassian.plugins.rest.common.expand.EntityExpander;
import com.atlassian.plugins.rest.common.expand.ExpandContext;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * An {@link EntityExpanderResolver} that can find {@link EntityExpander entity expanders} for:
 * <ul>
 * <li>{@link List}</li>
 * <li>{@link Collection}</li>
 * </ul>
 */
public class CollectionEntityExpanderResolver implements EntityExpanderResolver
{
    private static final List<Class<? extends Collection>> TYPES = Lists.newArrayList(List.class, Collection.class);

    private static final Map<Class<?>, EntityExpander<?>> EXPANDERS = ImmutableMap.<Class<?>, EntityExpander<?>>builder()
            .put(List.class, new ListExpander())
            .put(Collection.class, new CollectionExpander())
            .build();

    public boolean hasExpander(Class<?> type)
    {
        for (Class<? extends Collection> expandableType : TYPES)
        {
            if (expandableType.isAssignableFrom(type))
            {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public <T> EntityExpander<T> getExpander(Class<? extends T> type)
    {
        for (Class<? extends Collection> expandableType : TYPES)
        {
            if (expandableType.isAssignableFrom(type))
            {
                return (EntityExpander<T>) EXPANDERS.get(expandableType);
            }
        }
        return null;
    }

    private static class ListExpander implements EntityExpander<List>
    {
        public List expand(ExpandContext<List> context, EntityExpanderResolver expanderResolver, EntityCrawler entityCrawler)
        {
            final List list = new LinkedList();
            for (Object item : context.getEntity())
            {
                final ExpandContext<Object> itemContext = new DefaultExpandContext<Object>(item, context.getExpandable(), context.getEntityExpandParameter());
                final EntityExpander<Object> entityExpander = item != null ? expanderResolver.getExpander(item.getClass()) : null;
                list.add(entityExpander != null ? entityExpander.expand(itemContext, expanderResolver, entityCrawler) : item);
            }
            return list;
        }
    }

    private static class CollectionExpander implements EntityExpander<Collection>
    {
        public Collection expand(ExpandContext<Collection> context, EntityExpanderResolver expanderResolver, EntityCrawler entityCrawler)
        {
            final List list = new LinkedList();
            for (Object item : context.getEntity())
            {
                final ExpandContext<Object> itemContext = new DefaultExpandContext<Object>(item, context.getExpandable(), context.getEntityExpandParameter());
                final EntityExpander<Object> entityExpander = item != null ? expanderResolver.getExpander(item.getClass()) : null;
                list.add(entityExpander != null ? entityExpander.expand(itemContext, expanderResolver, entityCrawler) : item);
            }
            return list;
        }
    }
}
