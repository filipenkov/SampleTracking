package com.atlassian.core.spool;

import org.apache.log4j.Category;

import java.io.*;

/**
 * A FileInputStream that deletes its input file when closed. Useful for transient file spooling.
 *
 * Because we don't know when a database will have finished using the stream, or if it will close it itself, this class
 * closes the stream when all the data has been read from it.
 */
public class SpoolFileInputStream extends FileInputStream {

    private static final Category log = Category.getInstance(SpoolFileInputStream.class);

    private File fileToDelete;

    private boolean closed = false;

    /**
     * @param file
     * @throws FileNotFoundException
     */
    public SpoolFileInputStream(File file) throws FileNotFoundException
    {
        super(file);
        init(file);
    }

    /**
     * @param name
     * @throws FileNotFoundException
     */
    public SpoolFileInputStream(String name) throws FileNotFoundException
    {
        super(name);
        init(new File(name));
    }

    private void init(File file)
    {
        this.fileToDelete = file;
    }

    /* (non-Javadoc)
          * @see java.io.FileInputStream#close()
          */
    public void close() throws IOException
    {
        try {
            closed = true;
            super.close();
        }
        catch (IOException ex)
        {
            log.error("Error closing spool stream", ex);
        }
        finally
        {
            if (fileToDelete.exists() && !fileToDelete.delete())
            {
                log.warn("Could not delete spool file " + fileToDelete);
            }
        }
    }

    public int read() throws IOException
    {
        if (closed)
        {
            return -1;
        }
        int n = super.read();
        if (n == -1)
        {
            close();
        }
        return n;
    }

    public int read(byte b[]) throws IOException
    {
        if (closed)
        {
            return -1;
        }
        int n = super.read(b);
        if (n == -1)
        {
            close();
        }
        return n;    }

    public int read(byte b[], int off, int len) throws IOException
    {
        if (closed)
        {
            return -1;
        }
        int n = super.read(b, off, len);
        if (n == -1)
        {
            close();
        }
        return n;
    }
}
