package com.atlassian.jira.mail.util;

import org.apache.commons.io.IOUtils;

import javax.activation.DataSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This is a {@link javax.activation.DataSource} that reads input from a stream and captures it into a ByteArray.  This
 * is useful for capturing strea data into emails for example.
 *
 * @since v3.13.3
 */
public class ByteArrayDataSource implements DataSource
{
    /**
     * Stream containg the Data
     */
    private ByteArrayOutputStream baos;

    /**
     * Content-type.
     */
    private final String contentType;


    /**
     * Create a datasource from an input stream.
     *
     * @param inputStream This is NOT closed as a result of this operation
     * @param contentType The content type of the data
     */
    public ByteArrayDataSource(InputStream inputStream, String contentType)
            throws IOException
    {
        this.contentType = (contentType == null ? "application/octet-stream" : contentType);
        baos = new ByteArrayOutputStream();
        IOUtils.copy(inputStream, baos);
        baos.close();
    }

    /**
     * Get the content type.
     *
     * @return A String.
     */
    public String getContentType()
    {
        return contentType;
    }

    /**
     * Get the input stream.
     *
     * @return An InputStream.
     */
    public InputStream getInputStream()
            throws IOException
    {
        return new ByteArrayInputStream(baos.toByteArray());
    }

    /**
     * Get the name.
     *
     * @return A String.
     */
    public String getName()
    {
        return "ByteArrayDataSource";
    }

    /**
     * Get the OutputStream to write to
     *
     * @return An OutputStream
     */
    public OutputStream getOutputStream() throws IOException
    {
        baos = new ByteArrayOutputStream();
        return baos;
    }
}
