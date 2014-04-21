package com.atlassian.security.auth.trustedapps;

import javax.servlet.http.HttpServletRequest;

/**
 * Validate whether a request is allowed or not. Throws an InvalidRequestException if not.
 */
public interface RequestValidator
{
    /**
     * Check if this URL is allowed to be accessed by trusted calls
     * 
     * @throws InvalidRequestException if the requested url does not match the allowed ones.
     */
    void validate(HttpServletRequest request) throws InvalidRequestException;
}