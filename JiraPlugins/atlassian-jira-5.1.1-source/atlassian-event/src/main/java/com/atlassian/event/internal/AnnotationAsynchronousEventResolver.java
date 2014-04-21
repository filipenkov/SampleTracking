package com.atlassian.event.internal;

import com.atlassian.event.api.AsynchronousPreferred;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * <p>Annotation based {@link AsynchronousEventResolver}. This will check whether the event is annotated with the given
 * annotation.</p>
 * <p>The default annotation used is {@link com.atlassian.event.api.AsynchronousPreferred}</p>
 * @see com.atlassian.event.api.AsynchronousPreferred
 * @since 2.0
 */
final class AnnotationAsynchronousEventResolver implements AsynchronousEventResolver
{
    private final Class annotationClass;

    AnnotationAsynchronousEventResolver()
    {
        this(AsynchronousPreferred.class);
    }

    AnnotationAsynchronousEventResolver(Class annotationClass)
    {
        this.annotationClass = checkNotNull(annotationClass);
    }

    @SuppressWarnings("unchecked")
    public boolean isAsynchronousEvent(Object event)
    {
        return checkNotNull(event).getClass().getAnnotation(annotationClass) != null;
    }
}
