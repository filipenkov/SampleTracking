/**
 * Created by IntelliJ IDEA.
 * User: Edwin
 * Date: Nov 29, 2002
 * Time: 1:25:34 PM
 * To change this template use Options | File Templates.
 */
package com.atlassian.core.util;

import alt.java.io.File;
import alt.java.io.FileImpl;
import org.apache.log4j.Category;

import java.util.HashMap;
import java.util.Map;

public class FileFactory
{
    private static Map mockFiles;
    private static transient final Category log = Category.getInstance(FileFactory.class);


    public static File getFile(String absoluteFilename)
    {
        if (mockFiles == null || mockFiles.get(absoluteFilename) == null)
            return new FileImpl(absoluteFilename);
        else
            return (File) mockFiles.get(absoluteFilename);
    }

    public static void addMockFile(String filename, File file)
    {
        if (mockFiles == null)
            mockFiles = new HashMap();

        mockFiles.put(filename, file);
    }

    public static void flushMockFiles()
    {
        mockFiles = null;
    }

    public static void removeDirectory(File directory)
    {
        String[] list = directory.list();

        if (list == null)
        {
            list = new String[0];
        }

        for (int i = 0; i < list.length; i++)
        {
            String filename = list[i];
            File f = FileFactory.getFile(directory.getAbsolutePath() + System.getProperty("file.separator") + filename);
            if (f.isDirectory())
            {
                removeDirectory(f);
            }
            else
            {
                log.debug("Deleting " + f.getAbsolutePath());
                if (!f.delete())
                {
                    log.warn("Unable to delete file " + f.getAbsolutePath());
                }
            }
        }

        if (!directory.delete())
        {
            log.error("Unable to delete directory " + directory.getAbsolutePath());
        }
    }
}
