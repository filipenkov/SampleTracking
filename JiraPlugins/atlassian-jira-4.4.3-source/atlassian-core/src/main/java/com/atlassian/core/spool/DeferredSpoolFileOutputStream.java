package com.atlassian.core.spool;

import java.io.*;

/**
 * This specialisation of DeferredFileOutputStream may be configured with a FileFactory so that files are only created
 * once the deferred threshold is reached. getInputStream() returns a SpoolFileInputStream to read the result,
 * which means that the deferred file will be deleted once the input stream is closed.
 */
public class DeferredSpoolFileOutputStream extends DeferredFileOutputStream
{
    private FileFactory fileFactory = DefaultSpoolFileFactory.getInstance();
    private boolean unspooling = false;

    /**
     * Create a new DeferredSpoolFileOutputStream with the specified threshold and deferred file
     * @see #DeferredFileOutputStream(int, File)
     */
    public DeferredSpoolFileOutputStream(int threshold, File outputFile)
    {
        super(threshold, outputFile);
    }

    /**
     * Create a new DeferredSpoolFileOutputStream with the specified threshold and file factory. The file factory
     * will only be used when the threshold is reached, so you can defer the creation of files until they are necessary
     * @param threshold
     * @param fileFactory Factory to use when the deferred file must be created
     */
    public DeferredSpoolFileOutputStream(int threshold, FileFactory fileFactory)
    {
        super(threshold, null);
        this.fileFactory = fileFactory;
    }

    /**
     * @return True if the stream has been closed
     */
    public boolean isClosed()
    {
        return closed;
    }

    /**
     *
     * @return True if getInputStream() has been called already
     */
    public boolean isUnspooling()
    {
        return unspooling;
    }

    protected void thresholdReached() throws IOException
    {
        if (outputFile == null)
            outputFile = fileFactory.createNewFile();
        super.thresholdReached();
    }

    /**
     * Return an input stream of the written data. This method may only be called once and only once the output stream
     * has been closed.
     *
     * @return A InputStream - If the deferred stream has been written to disk, a SpoolFileInputStream will be returned
     * and the deferred file will be deleted when this stream is closed.
     * @throws IOException If the output stream is not closed or an input stream has already been returned
     */
    public InputStream getInputStream() throws IOException
    {
        if (!isClosed())
            throw new IOException("Output stream not closed");

        if (isUnspooling())
            throw new IOException("Stream is already being unspooled");

        InputStream spoolStream;

        if (isInMemory())
            spoolStream = new ByteArrayInputStream(getData());
        else
            try
            {
                spoolStream = new SpoolFileInputStream(getFile());
            }
            catch (FileNotFoundException ex)
            {
                throw new IOException("Deferred file does not exist");
            }

        unspooling = true;
        return spoolStream;
    }
}
