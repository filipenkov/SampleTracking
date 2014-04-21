package com.atlassian.core.spool;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;

/**
 * A very simple spool that uses a ByteArray buffer. Default buffer size is 10KiB
 */

public class ByteArraySpool implements Spool
{
    private int initialBufferSize = 10 * 1024;

    public int getInitialBufferSize()
    {
        return initialBufferSize;
    }

    /**
     * Configure the initial size of the byte array buffer.
     *
     * @param initialBufferSize The initial size of the buffer in bytes
     */
    public void setInitialBufferSize(int initialBufferSize)
    {
        this.initialBufferSize = initialBufferSize;
    }

    public InputStream spool(InputStream is) throws IOException
    {
        ByteArrayOutputStream buf = new ByteArrayOutputStream(initialBufferSize);
        IOUtils.copy(is, buf);
        return new ByteArrayInputStream(buf.toByteArray());
    }

}
