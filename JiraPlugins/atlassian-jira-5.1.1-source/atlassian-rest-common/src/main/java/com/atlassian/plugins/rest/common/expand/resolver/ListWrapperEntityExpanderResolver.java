package com.atlassian.plugins.rest.common.expand.resolver;

import com.atlassian.plugins.rest.common.expand.EntityCrawler;
import com.atlassian.plugins.rest.common.expand.EntityExpander;
import com.atlassian.plugins.rest.common.expand.ExpandContext;
import com.atlassian.plugins.rest.common.expand.entity.ListWrapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Set;

import static com.atlassian.plugins.rest.common.util.ReflectionUtils.setFieldValue;

/**
 * An {@link EntityExpanderResolver entity expander resolver} that can create a {@link EntityExpander entity expander} for {@link ListWrapper list wrappers}
 */
public class ListWrapperEntityExpanderResolver implements EntityExpanderResolver
{
    static final ListWrapperEntityExpander EXPANDER = new ListWrapperEntityExpander();

    public boolean hasExpander(Class<?> type)
    {
        return ListWrapper.class.isAssignableFrom(Preconditions.checkNotNull(type));
    }

    public <T> EntityExpander<T> getExpander(Class<? extends T> type)
    {
        return ListWrapper.class.isAssignableFrom(type) ? (EntityExpander<T>) EXPANDER : null;
    }

    static class ListWrapperEntityExpander<T> implements EntityExpander<ListWrapper<T>>
    {
        public ListWrapper<T> expand(ExpandContext<ListWrapper<T>> context, EntityExpanderResolver expanderResolver, EntityCrawler entityCrawler)
        {
            final ListWrapper<T> entity = context.getEntity();

            final Set<Field> collectionFields = Sets.newHashSet();
            for (Class cls = entity.getClass(); cls != null; cls = cls.getSuperclass())
            {
                final Field[] fields = cls.getDeclaredFields();
                for (Field field : fields)
                {
                    if (Collection.class.isAssignableFrom(field.getType()))
                    {
                        collectionFields.add(field);
                    }
                }
            }
            if (collectionFields.isEmpty())
            {
                throw new RuntimeException("Entity " + entity.getClass() + " has no collection field, cannot expand.");
            }
            if (collectionFields.size() > 1)
            {
                throw new RuntimeException("Entity " + entity.getClass() + " has more than one collection field, cannot determine which collection to expand.");
            }

            setFieldValue(collectionFields.iterator().next(), entity, entity.getCallback().getItems(context.getEntityExpandParameter().getIndexes(context.getExpandable())));

            entityCrawler.crawl(entity, context.getEntityExpandParameter().getExpandParameter(context.getExpandable()), expanderResolver);
            return entity;
        }

    }
}
