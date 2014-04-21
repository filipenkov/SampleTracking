package com.atlassian.jira.util;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Simple utility functions for dealing with zip files.
 */
public class ZipUtils
{
    /**
     * Retrieves an input stream reading from a named entry in a zip file.
     * @param zipFile the path to the zip file.
     * @param entryName the name of the entry to which an input stream should be returned.
     * @return an input stream reading from the named entry in the specified file.
     * @throws IOException if the zip file is unreadable, or if it does not contained the named entry.
     */
    public static InputStream streamForZipFileEntry(File zipFile, String entryName) throws IOException
    {
        final ZipFile file = new ZipFile(zipFile.getAbsolutePath());
        InputStream underlyingStream = null;
        try
        {
            ZipArchiveEntry entry = file.getEntry(entryName);
            if (entry == null)
            {
                return null;
            }
            underlyingStream = file.getInputStream(entry);
        }
        finally
        {
            if (underlyingStream == null)
                ZipFile.closeQuietly(file);
        }
        return new FilterInputStream(underlyingStream)
        {
            @Override
            public void close() throws IOException
            {
                super.close();
                file.close();
            }
        };
    }
}
