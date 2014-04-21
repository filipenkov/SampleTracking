package com.atlassian.crowd.manager.validation;

import com.atlassian.crowd.model.application.Application;

import javax.servlet.http.HttpServletRequest;

/**
 * Manager that validates whether a client can make a request.
 */
public interface ClientValidationManager
{
    /**
     * Validates that the client is allowed to perform the request.
     *
     * @param application Application to validate with.
     * @param request HttpServletRequest
     * @throws ClientValidationException if the client fails to validate with the application
     */
    void validate(Application application, HttpServletRequest request) throws ClientValidationException;
}
