package com.atlassian.applinks.api;

import com.atlassian.sal.api.net.Request;
import com.atlassian.sal.api.net.Response;
import com.atlassian.sal.api.net.ResponseException;

/**
 * <p>
 * A request object preconfigured with authentication information for the
 * Application Link it was created for.
 * </p>
 * <p>
 * To execute the request, call this interface's execute method, or one of the
 * base type's execute methods.
 * </p>
 *
 * @see com.atlassian.applinks.api.ApplicationLinkRequestFactory#createRequest(com.atlassian.sal.api.net.Request.MethodType, String)
 * @since v3.0
 */
public interface ApplicationLinkRequest extends Request<ApplicationLinkRequest, Response>
{
    /**
     * Executes the request. Use this method instead of
     * {@link com.atlassian.sal.api.net.Request#execute()} and
     * {@link com.atlassian.sal.api.net.Request#execute(com.atlassian.sal.api.net.ResponseHandler)}
     * when the caller anticipates a "credentials required problem". For more
     * information about this scenario, see:
     * {@link com.atlassian.applinks.api.CredentialsRequiredException}
     *
     * @param responseHandler Callback handler of the response.
     * @throws com.atlassian.sal.api.net.ResponseException If the response cannot be retrieved
     */
    <R> R execute(ApplicationLinkResponseHandler<R> responseHandler) throws ResponseException;
}
