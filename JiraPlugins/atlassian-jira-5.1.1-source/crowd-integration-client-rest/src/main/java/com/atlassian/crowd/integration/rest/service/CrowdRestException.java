package com.atlassian.crowd.integration.rest.service;

import com.atlassian.crowd.integration.rest.entity.*;

/**
 * Thrown when the REST method returns an error response other than 401 (Unauthorized) or 403 (Forbidden). This
 * exception should only be used by {@link RestCrowdClient} and its helper classes (e.g. {@link RestExecutor}).
 *
 * @since v2.1
 */
class CrowdRestException extends Exception
{
    private final ErrorEntity errorEntity;
    private final int statusCode;

    /**
     * Constructs a new error entity.
     *
     * @param msg exception message
     * @param errorEntity ErrorEntity
     * @param statusCode HTTP status code
     */
    CrowdRestException(final String msg, final ErrorEntity errorEntity, final int statusCode)
    {
        super(msg);
        this.errorEntity = errorEntity;
        this.statusCode = statusCode;
    }

    /**
     * @return ErrorEntity
     */
    ErrorEntity getErrorEntity()
    {
        return errorEntity;
    }

    /**
     * @return HTTP status code
     */
    int getStatusCode()
    {
        return statusCode;
    }
}
