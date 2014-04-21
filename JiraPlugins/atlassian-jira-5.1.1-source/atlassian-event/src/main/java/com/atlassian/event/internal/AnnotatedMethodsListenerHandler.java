package com.atlassian.event.internal;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.spi.ListenerHandler;
import com.atlassian.event.spi.ListenerInvoker;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * <p>A listener handler that will check for single parameter methods annotated with the given annotation.</p>
 * <p>The default annotation for methods is {@link com.atlassian.event.api.EventListener}.</p>
 * @see com.atlassian.event.api.EventListener
 * @since 2.0
 */
public final class AnnotatedMethodsListenerHandler implements ListenerHandler
{
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final Class annotation;

    public AnnotatedMethodsListenerHandler()
    {
        this(EventListener.class);
    }

    public AnnotatedMethodsListenerHandler(Class annotation)
    {
        this.annotation = checkNotNull(annotation);
    }

    public List<? extends ListenerInvoker> getInvokers(final Object listener)
    {
        final List<Method> validMethods = getValidMethods(checkNotNull(listener));

        if (validMethods.isEmpty())
        {
            log.debug("Couldn't find any valid listener methods on class <{}>", listener.getClass().getName());
        }

        return Lists.transform(validMethods, new Function<Method, ListenerInvoker>()
        {
            public ListenerInvoker apply(Method method)
            {
                return new SingleParameterMethodListenerInvoker(listener, method);
            }
        });
    }

    private List<Method> getValidMethods(Object listener)
    {
        final List<Method> annotatedMethods = Lists.newArrayList();
        for (Method method : listener.getClass().getMethods())
        {
            if (isValidMethod(method))
            {
                annotatedMethods.add(method);
            }
        }
        return annotatedMethods;
    }

    private boolean isValidMethod(Method method)
    {
        if (isAnnotated(method))
        {
            if (hasOneAndOnlyOneParameter(method))
            {
                return true;
            }
            else
            {
                throw new RuntimeException("Method <" + method + "> of class <" + method.getDeclaringClass() + "> " +
                        "is annotated with <" + annotation.getClass().getName() + "> but has 0 or more than 1 parameters! " +
                        "Listener methods MUST have 1 and only 1 parameter.");
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private boolean isAnnotated(Method method)
    {
        return method.getAnnotation(annotation) != null;
    }

    private boolean hasOneAndOnlyOneParameter(Method method)
    {
        return method.getParameterTypes().length == 1;
    }
}
