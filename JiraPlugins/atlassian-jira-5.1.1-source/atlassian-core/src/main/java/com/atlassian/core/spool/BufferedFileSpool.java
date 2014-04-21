package com.atlassian.core.spool;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;

/**
 * Spool bytes via buffered file streams. The returned input stream is a SpoolFileInputStream
 *
 * @see SpoolFileInputStream
 */
public class BufferedFileSpool implements FileSpool {
	
    private FileFactory fileFactory = DefaultSpoolFileFactory.getInstance();

    public FileFactory getFileFactory()
    {
        return fileFactory;
    }

    public void setFileFactory(FileFactory fileFactory)
    {
        this.fileFactory = fileFactory;
    }

    public InputStream spool(InputStream is) throws IOException
    {
		File spoolFile = fileFactory.createNewFile();

		OutputStream os = new BufferedOutputStream(new FileOutputStream(spoolFile));
		IOUtils.copy(is, os);
		os.close();
		
		return new BufferedInputStream(new SpoolFileInputStream(spoolFile));
	}

}
