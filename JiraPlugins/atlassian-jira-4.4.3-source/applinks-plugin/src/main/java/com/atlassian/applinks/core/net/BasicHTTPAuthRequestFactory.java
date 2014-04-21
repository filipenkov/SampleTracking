package com.atlassian.applinks.core.net;

import com.atlassian.sal.api.net.Request;
import com.atlassian.sal.api.net.RequestFactory;

/**
 * A {@link RequestFactory} decorator that applies Basic HTTP Authentication to
 * the {@link Request} objects it creates.
 *
 * @since    3.0
 */
public class BasicHTTPAuthRequestFactory<T extends Request<?, ?>> implements RequestFactory<T>
{
    private final RequestFactory<T> requestFactory;
    private final String username;
    private final String password;

    public BasicHTTPAuthRequestFactory(final RequestFactory<T> requestFactory,
                                final String username,
                                final String password)
    {
        this.password = password;
        this.requestFactory = requestFactory;
        this.username = username;
    }

    public T createRequest(final Request.MethodType methodType, final String url)
    {
        final T request = requestFactory.createRequest(methodType, url);
        request.addBasicAuthentication(username, password);
        return request;
    }

    public boolean supportsHeader()
    {
        return requestFactory.supportsHeader();
    }
}
