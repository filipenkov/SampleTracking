package com.atlassian.plugins.rest.common.multipart;

import java.util.Collection;

/**
 * A parsed multipart form
 *
 * @since 2.4
 */
public interface MultipartForm
{
    /**
     * Get the first file part for the given field name
     *
     * @param field The field name
     * @return The first file part, or null if none was found
     */
    FilePart getFilePart(String field);

    /**
     * Get the first file parts for the given field name
     *
     * @param field The field name
     * @return The first file part, or null if none was found
     */
    Collection<FilePart> getFileParts(String field);

}
