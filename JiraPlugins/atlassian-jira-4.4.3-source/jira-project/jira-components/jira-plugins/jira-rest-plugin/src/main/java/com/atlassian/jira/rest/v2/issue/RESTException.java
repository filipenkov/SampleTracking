package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.rest.api.util.ErrorCollection;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import static com.atlassian.jira.rest.api.http.CacheControl.never;

/**
 * @since v4.2
 */
public class RESTException extends WebApplicationException
{
    /**
     * Creates a new RESTException for the given issue. Whenever possible it is preferable to use {@link
     * #RESTException(javax.ws.rs.core.Response.Status, com.atlassian.jira.rest.api.util.ErrorCollection)} constructor, passing a collection of
     * errors and the status
     */
    public RESTException()
    {
        this(Response.Status.NOT_FOUND, ErrorCollection.builder().build());
    }

    public RESTException(final ErrorCollection errors)
    {
        this(Response.Status.NOT_FOUND, errors);
    }

    public RESTException(final Response.Status status, final String... errorMessages)
    {
        this(status, ErrorCollection.of(errorMessages));
    }

    /**
     * Creates a new RESTException for the given issue, with a collection of errors.
     *
     * @param status the HTTP status of this error (401, 403, etc)
     * @param errors an ErrorCollection containing the errors
     */
    public RESTException(final Response.Status status, final ErrorCollection errors)
    {
        super(createResponse(status, errors));
    }

    /**
     * Creates a new RESTException for the given issue and allows to nest an exception.
     *
     * @param status the HTTP status of this error (401, 403, etc)
     * @param cause the nested exception that will be logged by the ExceptionInterceptor, before returning the response to the user.
     */
    public RESTException(Response.Status status, Throwable cause)
    {
        super(cause, status);
    }

    /**
     * Creates a new HTTP response with the given status, returning the errors in the provided ErrorCollection.
     *
     * @param status the HTTP status to use for this response
     * @param errors an ErrorCollection containing errors
     * @return a Response
     */
    private static Response createResponse(Response.Status status, ErrorCollection errors)
    {
        // the issue key is not used yet, but should make it into the entity in the future...
        return Response.status(status).entity(errors).cacheControl(never()).build();
    }
}
