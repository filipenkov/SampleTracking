package com.atlassian.plugins.rest.common.expand;

import com.atlassian.plugins.rest.common.expand.resolver.EntityExpanderResolver;

/**
 * This is a general-purpose expander for atlassian-rest that delegates the
 * expand process to the entity that is to be expanded (instead of having that
 * knowledge in a separate {@link com.atlassian.plugins.rest.common.expand.EntityExpander}.
 * As a result, this expander can be used for every entity that implements
 * {@link SelfExpanding}.
 *
 * @since   v1.0.7
 * @author  Erik van Zijst
 */
public class SelfExpandingExpander extends AbstractRecursiveEntityExpander<SelfExpanding>
{
    protected SelfExpanding expandInternal(SelfExpanding selfExpandingObject)
    {
        selfExpandingObject.expand();
        return selfExpandingObject;
    }

    /**
     * To use the self expanding mechanism, make sure you register an instance
     * of this {@link EntityExpanderResolver} in your application's
     * {@link ExpandResponseFilter}.
     */
    public static class Resolver implements EntityExpanderResolver
    {
        private static final SelfExpandingExpander expander = new SelfExpandingExpander();

        public <T> boolean hasExpander(T instance)
        {
            return hasExpander(instance.getClass());
        }

        public boolean hasExpander(Class<?> aClass)
        {
            return SelfExpanding.class.isAssignableFrom(aClass);
        }

        @SuppressWarnings("unchecked")
        public <T> EntityExpander<T> getExpander(T instance)
        {
            return (EntityExpander<T>) getExpander(instance.getClass());
        }

        public <T> EntityExpander<T> getExpander(Class<? extends T> aClass)
        {
            return hasExpander(aClass) ? (EntityExpander<T>) expander : null;
        }
    }
}
