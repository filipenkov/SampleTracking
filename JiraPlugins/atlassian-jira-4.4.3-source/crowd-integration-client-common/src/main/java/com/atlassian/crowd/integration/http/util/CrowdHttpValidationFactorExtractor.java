package com.atlassian.crowd.integration.http.util;

import com.atlassian.crowd.model.authentication.ValidationFactor;

import java.util.List;
import javax.servlet.http.HttpServletRequest;

/**
 * Classes which extracts validation factors from a request object should implement this interface.
 */
public interface CrowdHttpValidationFactorExtractor
{
    /**
     * Retrieves validation factors from the request:
     * <p/>
     * <ol>
     *     <li>Remote Address: the source IP address of the HTTP request.</li>
     *     <li>Original Address: the X-Forwarded-For HTTP header (if present and distinct from the Remote Address).</li>
     * </ol>
     *
     * @param request HttpServletRequest.
     * @return array of validation factors.
     */
    List<ValidationFactor> getValidationFactors(HttpServletRequest request);
}
