package com.atlassian.plugins.rest.common.expand.resolver;

import com.atlassian.plugins.rest.common.expand.*;
import com.atlassian.plugins.rest.common.expand.parameter.Indexes;
import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * {@link EntityExpanderResolver Entity expander resolver} that will get resolver for classes (objects) with method annotated with {@link ExpandConstraint @ExpandConstraint}.
 * Method signature should be the same as below:
 * <code>
 * @ExpandConstraint public void expandInstance(Indexes indexes)
 * {
 * // your code to expand the object here
 * }
 * </code>
 */
public class ExpandConstraintEntityExpanderResolver implements EntityExpanderResolver
{
    public boolean hasExpander(Class<?> type)
    {
        return getConstrainMethod(checkNotNull(type)) != null;
    }

    public <T> EntityExpander<T> getExpander(Class<? extends T> type)
    {
        final Method method = getConstrainMethod(checkNotNull(type));
        return method != null ? (EntityExpander<T>) new ExpandConstraintEntityExpander(method) : null;
    }

    private <T> Method getConstrainMethod(Class<? extends T> type)
    {
        for (Method method : type.getDeclaredMethods())
        {
            if (method.getAnnotation(ExpandConstraint.class) != null
                    && method.getParameterTypes().length == 1
                    && method.getParameterTypes()[0].equals(Indexes.class))
            {
                return method;
            }
        }
        return null;
    }

    private static class ExpandConstraintEntityExpander implements EntityExpander<Object>
    {
        private final Method method;

        public ExpandConstraintEntityExpander(Method method)
        {
            this.method = checkNotNull(method);
        }

        public Object expand(ExpandContext<Object> context, EntityExpanderResolver expanderResolver, EntityCrawler entityCrawler)
        {
            final Object entity = context.getEntity();
            try
            {
                method.invoke(entity, context.getEntityExpandParameter().getIndexes(context.getExpandable()));
            }
            catch (IllegalAccessException e)
            {
                throw new ExpandException(e);
            }
            catch (InvocationTargetException e)
            {
                throw new ExpandException(e);
            }

            entityCrawler.crawl(entity, context.getEntityExpandParameter().getExpandParameter(context.getExpandable()), expanderResolver);

            return entity;
        }
    }
}
