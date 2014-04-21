package com.atlassian.core.spool;

import java.io.File;
import java.io.IOException;

/**
 * Default file factory for spooling. Creates a new temporary file via File::createTempFile().
 * The file is automatically scheduled for deletion via File::deleteOnExit()
 *
 * @see java.io.File#createTempFile(String, String, File)
 * @see File#deleteOnExit()
 */
public class DefaultSpoolFileFactory implements FileFactory
{
    private static final DefaultSpoolFileFactory INSTANCE = new DefaultSpoolFileFactory();

    private String spoolPrefix = "spool-";
    private String spoolSuffix = ".tmp";
    private File spoolDirectory = null;

    /**
     * @return a singleton instance of this file factory, using default settings
     */
    public static DefaultSpoolFileFactory getInstance()
    {
        return INSTANCE;
    }

    public void setSpoolPrefix(String spoolPrefix)
    {
        this.spoolPrefix = spoolPrefix;
    }

    public void setSpoolSuffix(String spoolSuffix)
    {
        this.spoolSuffix = spoolSuffix;
    }

    public void setSpoolDirectory(File spoolDirectory)
    {
        this.spoolDirectory = spoolDirectory;
    }

    public File createNewFile() throws IOException
    {
        File file = File.createTempFile(spoolPrefix, spoolSuffix, spoolDirectory);
        file.deleteOnExit();
        return file;
    }
}
