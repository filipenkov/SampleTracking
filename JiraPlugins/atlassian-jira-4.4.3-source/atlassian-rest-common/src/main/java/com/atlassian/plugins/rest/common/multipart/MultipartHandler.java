package com.atlassian.plugins.rest.common.multipart;

import javax.servlet.http.HttpServletRequest;

public interface MultipartHandler
{
    /**
     * Get the first file part for the given field name from the request
     *
     * @param request The request
     * @param field The field name
     * @return The first file part, or null if none was found
     */
    FilePart getFilePart(HttpServletRequest request, String field);

    /**
     * Parse the multipart form from this request
     *
     * @param request The request to parse
     * @return The form
     * @since 2.4
     */
    MultipartForm getForm(HttpServletRequest request);
}
