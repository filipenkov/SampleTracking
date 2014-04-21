package com.atlassian.plugins.rest.common.expand.resolver;

import com.atlassian.plugins.rest.common.expand.EntityExpander;
import com.atlassian.plugins.rest.common.expand.Expander;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * <p>An {@link EntityExpander} resolver that uses the {@link Expander} annotation for resolution.</p>
 * <p>Implementation must implement {@link #getEntityExpander(Expander)}</p>
 */
public abstract class AbstractAnnotationEntityExpanderResolver implements EntityExpanderResolver
{
    public boolean hasExpander(Class<?> type)
    {
        return checkNotNull(type).getAnnotation(Expander.class) != null;
    }

    public final <T> EntityExpander<T> getExpander(Class<? extends T> type)
    {
        final Expander expander = checkNotNull(type).getAnnotation(Expander.class);
        return expander != null ? (EntityExpander<T>) getEntityExpander(expander) : null;
    }

    /**
     * Retrieves the {@link EntityExpander} associated to the {@link Expander} annotation. The entity expander is created if necessary.
     *
     * @param expander the annotation
     * @return an instance of {@link EntityExpander}
     */
    protected abstract EntityExpander<?> getEntityExpander(Expander expander);
}
