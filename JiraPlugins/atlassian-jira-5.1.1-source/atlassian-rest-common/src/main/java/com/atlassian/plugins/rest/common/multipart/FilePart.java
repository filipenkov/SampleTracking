package com.atlassian.plugins.rest.common.multipart;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * A part of a multi part request
 */
public interface FilePart
{
    /**
     * Get the file name of the part
     *
     * @return The file name of the part
     */
    String getName();

    /**
     * Get the content type of the part
     *
     * @return The content type of the part
     * @since 2.4
     */
    String getContentType();

    /**
     * Write the part to the given file
     *
     * @param file The file to write the part to
     * @since 2.4
     */
    void write(File file) throws IOException;

    /**
     * Get the input stream of the part
     *
     * @return The input stream
     * @throws IOException If an error occured
     */
    InputStream getInputStream() throws IOException;

    /**
     * Get the simple value of the part
     *
     * @return The value
     * @since 2.4
     */
    String getValue();

    /**
     * Whether the part is a simple form field
     *
     * @return True if it's a simple form field
     * @since 2.4
     */
    boolean isFormField();
}
