package com.atlassian.core.util.zip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FolderArchiver
{
    //~ Static variables/initializers ----------------------------------------------------------------------------------

    private static final int BUFFER_SIZE = 10240;

    //~ Instance variables ---------------------------------------------------------------------------------------------

    private File folderToArchive;
    private File archiveFile;

    //~ Constructors ---------------------------------------------------------------------------------------------------
    public FolderArchiver(File folderToArchive, File archiveFile)
    {
        this.folderToArchive = folderToArchive;
        this.archiveFile = archiveFile;
    }

    //~ Methods --------------------------------------------------------------------------------------------------------

    public void doArchive() throws Exception
    {
        FileOutputStream stream = new FileOutputStream(archiveFile);
        ZipOutputStream output = new ZipOutputStream(stream);

        compressFile(folderToArchive, output);
        output.close();
        stream.close();
    }

    private void compressFile(File file, ZipOutputStream output)
        throws IOException
    {
        if ((file == null) || !file.exists())
        {
            return;
        }

        if (file.isFile() && !file.equals(archiveFile))
        {
            byte[] buffer = new byte[BUFFER_SIZE];
            String path = file.getPath().substring(folderToArchive.getPath().length());

            // if creating zip on a windows system, convert all backslashes to forward slashes so that the resulting file is platform-independent (work on unix and windows)
            path = path.replaceAll("\\\\", "/");

            // also drop the leading slash
            if (path.length() > 0 && path.charAt(0) == '/')
                path = path.substring(1);

            ZipEntry entry = new ZipEntry(path);

            entry.setTime(file.lastModified());
            output.putNextEntry(entry);

            FileInputStream in = new FileInputStream(file);
            int data;

            while ((data = in.read(buffer, 0, buffer.length)) > 0)
            {
                output.write(buffer, 0, data);
            }

            in.close();
        }
        else if (file.isDirectory())
        {
            File[] files = file.listFiles();

            for (int i = 0; i < files.length; i++)
            {
                compressFile(files[i], output);
            }
        }
    }
}