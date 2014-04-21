package com.atlassian.jira.web;

import javax.annotation.concurrent.Immutable;
import javax.servlet.http.HttpServletRequest;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Per-request variable (similar to {@code ThreadLocal}, but request-scoped).
 *
 * @since v4.4
 */
@Immutable
public class HttpRequestLocal<T>
{
    /**
     * The name of the request attribute that will be used.
     */
    private final String name;

    public HttpRequestLocal(String name)
    {
        this.name = checkNotNull(name);
    }

    /**
     * Returns the value of this HttpRequestLocal, or null if it is not set.
     *
     * @return the value of this HttpRequestLocal, or null
     */
    @SuppressWarnings ("unchecked")
    public T get()
    {
        return ifRequestAvailable(new RequestOperation<T>()
        {
            @Override
            public T run(HttpServletRequest request)
            {
                return (T) request.getAttribute(name);
            }
        });
    }

    /**
     * Sets the value of this HttpRequestLocal.
     *
     * @param value the value to set
     */
    public void set(final T value)
    {
        ifRequestAvailable(new RequestOperation<Void>()
        {
            @Override
            public Void run(HttpServletRequest request)
            {
                request.setAttribute(name, value);
                return null;
            }
        });
    }

    /**
     * Removes the value of this HttpRequestLocal.
     */
    public void remove()
    {
        ifRequestAvailable(new RequestOperation<Void>()
        {
            @Override
            public Void run(HttpServletRequest request)
            {
                request.removeAttribute(name);
                return null;
            }
        });
    }

    private <T> T ifRequestAvailable(RequestOperation<T> requestOperation)
    {
        HttpServletRequest request = ExecutingHttpRequest.get();
        if (request != null)
        {
            try
            {
                return requestOperation.run(request);
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }

        return null;
    }

    private interface RequestOperation<T>
    {
        T run(HttpServletRequest request);
    }
}
