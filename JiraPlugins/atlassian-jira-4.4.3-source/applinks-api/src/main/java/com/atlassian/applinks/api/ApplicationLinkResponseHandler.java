package com.atlassian.applinks.api;

import com.atlassian.sal.api.net.Response;
import com.atlassian.sal.api.net.ResponseException;
import com.atlassian.sal.api.net.ReturningResponseHandler;

/**
 * Callback interface used by {@link ApplicationLinkRequest#execute(ApplicationLinkResponseHandler)}
 * method. The implementation of this interface performs actual handling of
 * the response.
 *
 * @since v3.0
 */
public interface ApplicationLinkResponseHandler<R> extends ReturningResponseHandler<Response, R>
{
    /**
     * Triggered when a call to a remote server failed because the caller does
     * not have an established session with the server and will need to
     * authorize requests first by visiting the authorization URL provided by
     * {@link com.atlassian.applinks.api.AuthorisationURIGenerator}.
     * <p>
     * In this callback method the user still gets access to the full response
     * the server returned.
     * <p>
     * Note that for any request, only one callback method will ever be
     * invoked. Either this one, or {@link #handle(com.atlassian.sal.api.net.Response)}.
     * <p>
     * If your implementation of this callback method produces a result object,
     * you can parameterise the handler accordingly and return your result. If
     * you are not returning anything, you can parameterise with {@link java.lang.Void}
     * and return {@code null}.
     *
     * @param response a response object. Never null.
     * @return the result produced by this handler.
     * @throws com.atlassian.sal.api.net.ResponseException can be thrown by
     * when there was a problem processing the response.
     */
    R credentialsRequired(Response response) throws ResponseException;
}
