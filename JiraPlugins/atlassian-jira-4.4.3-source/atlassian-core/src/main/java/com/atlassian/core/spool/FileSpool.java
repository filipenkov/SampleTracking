package com.atlassian.core.spool;

/**
 * Interface for file based spools. Such spools can be configured with a FileFactory to strategise the creation
 * of files by the spool.
 */
public interface FileSpool extends Spool
{
    public FileFactory getFileFactory();
    public void setFileFactory(FileFactory fileFactory);
}
