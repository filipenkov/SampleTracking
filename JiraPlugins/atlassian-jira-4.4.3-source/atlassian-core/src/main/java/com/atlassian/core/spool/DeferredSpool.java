package com.atlassian.core.spool;

import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedInputStream;

import org.apache.commons.io.IOUtils;

/**
 * Thresholding spool that uses a DeferredSpoolFileOutputStream for spooling, allowing for a balance between memory
 * usage and speed.
 */
public class DeferredSpool implements FileSpool, ThresholdingSpool
{
    private int maxMemorySpool;
    private FileFactory fileFactory = DefaultSpoolFileFactory.getInstance();

    public FileFactory getFileFactory()
    {
        return fileFactory;
    }

    public void setFileFactory(FileFactory fileFactory)
    {
        this.fileFactory = fileFactory;
    }

    public void setThresholdBytes(int bytes)
    {
        this.maxMemorySpool = bytes;
    }

    public int getThresholdBytes()
    {
        return maxMemorySpool;
    }

    public InputStream spool(InputStream is) throws IOException
    {
        DeferredSpoolFileOutputStream deferredStream = getNewDeferredSpoolFileOutputStream();
        IOUtils.copy(is, deferredStream);
        deferredStream.close();
        return new BufferedInputStream(deferredStream.getInputStream());
    }

    /**
     * @return a new DeferredSpoolFileOutputStream
     */
    protected DeferredSpoolFileOutputStream getNewDeferredSpoolFileOutputStream()
    {
        return new DeferredSpoolFileOutputStream(maxMemorySpool, getFileFactory());
    }
}
