package com.atlassian.core.util;

import com.atlassian.core.util.zip.FolderArchiver;
import org.apache.log4j.Logger;
import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

/*
 * A series of utility methods for manipulating files.
 */
public class FileUtils
{
    private static final Logger log = Logger.getLogger(FileUtils.class);

    /**
     * Copy bytes from an <code>InputStream</code> to an <code>OutputStream</code>.
     * @param input the <code>InputStream</code> to read from
     * @param output the <code>OutputStream</code> to write to
     * @return the number of bytes copied
     * @throws IOException In case of an I/O problem
     * @deprecated since 3.18 use {@link IOUtils#copy(InputStream, OutputStream)}
     * @see IOUtils#copy(InputStream, OutputStream)
     */
    public static int copy(final InputStream input, final OutputStream output)
            throws IOException
    {
        return IOUtils.copy(input, output);
    }

    /**
     * Copy bytes from an <code>InputStream</code> to an <code>OutputStream</code>.
     * @param input the <code>InputStream</code> to read from
     * @param output the <code>OutputStream</code> to write to
     * @param bufferSize ignored
     * @return the number of bytes copied
     * @throws IOException In case of an I/O problem
     * @deprecated since 3.18 use {@link IOUtils#copy(InputStream, OutputStream)}
     * @see IOUtils#copy(InputStream, OutputStream)
     */
    public static int copy(final InputStream input,
                           final OutputStream output,
                           final int bufferSize)
            throws IOException
    {
        return IOUtils.copy(input, output);
    }

    /**
     * Unconditionally close an <code>OutputStream</code>.
     * Equivalent to {@link OutputStream#close()}, except any exceptions will be ignored.
     * @param output A (possibly null) OutputStream
     * @deprecated since 3.18 use {@link IOUtils#closeQuietly(OutputStream)}
     * @see IOUtils#closeQuietly(OutputStream)
     */
    public static void shutdownStream(final OutputStream output)
    {
        IOUtils.closeQuietly(output);
    }

    /**
     * Unconditionally close an <code>InputStream</code>.
     * Equivalent to {@link InputStream#close()}, except any exceptions will be ignored.
     * @param input A (possibly null) InputStream
     * @deprecated since 3.18 use {@link IOUtils#closeQuietly(OutputStream)}
     * @see IOUtils#closeQuietly(OutputStream)
     */
    public static void shutdownStream(final InputStream input)
    {
        IOUtils.closeQuietly(input);
    }

    /**
     * safely performs a recursive delete on a directory
     */
    public static boolean deleteDir(File dir)
    {
        if (dir == null)
        {
            return false;
        }

        // to see if this directory is actually a symbolic link to a directory,
        // we want to get its canonical path - that is, we follow the link to
        // the file it's actually linked to
        File candir;
        try
        {
            candir = dir.getCanonicalFile();
        }
        catch (IOException e)
        {
            return false;
        }

        // a symbolic link has a different canonical path than its actual path,
        // unless it's a link to itself
        if (!candir.equals(dir.getAbsoluteFile()))
        {
            // this file is a symbolic link, and there's no reason for us to
            // follow it, because then we might be deleting something outside of
            // the directory we were told to delete
            return false;
        }

        // now we go through all of the files and subdirectories in the
        // directory and delete them one by one
        File[] files = candir.listFiles();
        if (files != null)
        {
            for (int i = 0; i < files.length; i++)
            {
                File file = files[i];

                // in case this directory is actually a symbolic link, or it's
                // empty, we want to try to delete the link before we try
                // anything
                boolean deleted = !file.delete();
                if (deleted)
                {
                    // deleting the file failed, so maybe it's a non-empty
                    // directory
                    if (file.isDirectory()) deleteDir(file);

                    // otherwise, there's nothing else we can do
                }
            }
        }

        // now that we tried to clear the directory out, we can try to delete it
        // again
        return dir.delete();
    }


    /**
     * Recursively delete everything beneath <param>file</param> then delete dir.
     */
    public static void recursiveDelete(File file)
    {
        if (!file.isDirectory())
        {
            file.delete();
            return;
        }

        File[] files = file.listFiles();
        for (int i = 0; i < files.length; i++)
        {
            File next = files[i];
            recursiveDelete(next);
        }

        file.delete();
    }


    /**
     * Get the contents of a classpath resource as a String. Returns <tt>null</tt> if
     * the resource cannot be found or an error occurs reading the resource.
     */
    public static String getResourceContent(String resource)
    {
        InputStream is = ClassLoaderUtils.getResourceAsStream(resource, FileUtils.class);
        if (is == null) return null;

        try
        {
            return IOUtils.toString(is);
        }
        catch (IOException e)
        {
            log.error("IOException reading stream: " + e, e);
            return null;
        }
        finally
        {
            IOUtils.closeQuietly(is);
        }
    }

    /**
     * Get the contents of a servlet context resource as a String. Returns an empty
     * String ("") if the resource cannot be found or an error occurs reading the resource.
     */
    public static String getResourceContent(HttpServletRequest req, String resource)
    {
        InputStream is = req.getSession().getServletContext().getResourceAsStream(resource);
        if (is == null) return "";

        try
        {
            String result = IOUtils.toString(is);
            return (result == null) ? "" : result;
        }
        catch (IOException e)
        {
            log.error("IOException reading stream: " + e, e);
            return "";
        }
        finally
        {
            IOUtils.closeQuietly(is);
        }
    }

    /**
     * Get the contents of an inputstream as a String.
     *
     * @deprecated since 3.18 use {@link IOUtils#toString(InputStream, String)}
     * @see IOUtils#toString(InputStream, String)
     */
    public static String getInputStreamTextContent(InputStream is)
    {
        if (is == null)
        {
            return null;
        }

        try
        {
            return IOUtils.toString(is);
        }
        catch (IOException e)
        {
            log.error("IOException reading stream: " + e, e);
            return null;
        }
        finally
        {
            IOUtils.closeQuietly(is);
        }
    }

    /**
     * Writes text to the nominated file.
     * If this file already exists, its content will be overwritten
     */
    public static void saveTextFile(String stringContent, File destFile) throws IOException
    {
        ensureFileAndPathExist(destFile);

        FileWriter writer = new FileWriter(destFile);
        writer.write(stringContent);
        writer.close();
    }

    /**
     * Check that a given file and its parent directories exist - will create blank file and all directories if necessary.
     */
    public static void ensureFileAndPathExist(File file) throws IOException
    {
        file.getParentFile().mkdirs();
        file.createNewFile();
    }

    /**
     * move a directory with all it's children into another directory
     * if destination directory already exists, it will be deleted.
     *
     * e.g. rename c:/foo/bar to c:/fooz/bar
     */
    public static boolean moveDir(File dirName, File destDir)
    {
        File destParent = new File(destDir.getParent());

        // if the destDir exists, we override
        if (destDir.exists())
        {
            destDir.delete();
        }
        // destParent is the new directory we're moving dirName to
        // we have to ensure all its directories are created before moving
        destParent.mkdirs();
        return dirName.renameTo(destDir);
    }

    /**
     * Create a zip file of a given directory.
     */
    public static void createZipFile(File baseDir, File zipFile) throws Exception
    {
        FolderArchiver compressor = new FolderArchiver(baseDir, zipFile);
        compressor.doArchive();
    }

    /**
     * Get the contents of a resource as a list, one line representing one list item.
     * <p />
     * Note: lines starting with # are deemed to be comments and not included.
     */
    public static List readResourcesAsList(String resource)
    {
        List result = new ArrayList();

        InputStream is = ClassLoaderUtils.getResourceAsStream(resource, FileUtils.class);

        try
        {
            result.addAll(IOUtils.readLines(is));
        }
        catch (IOException e)
        {
            log.error("IOException reading stream: " + e, e);
            return result;
        }
        finally
        {
            IOUtils.closeQuietly(is);
        }

        for (Iterator iterator = result.iterator(); iterator.hasNext();)
        {
            String s = (String) iterator.next();
            if (org.apache.commons.lang.StringUtils.isBlank(s) || org.apache.commons.lang.StringUtils.trimToEmpty(s).startsWith("#"))
                iterator.remove();
        }

        return result;
    }

    /**
     * Copies all files from srcDir to destDir. Currently it just copies the files at te root, it's not recursive.
     */
    public static void copyDirectory(File srcDir, File destDir) throws IOException
    {
        copyDirectory(srcDir, destDir, false);
    }

    public static void copyDirectory(File srcDir, File destDir, boolean overwrite) throws IOException
    {
        File[] files = srcDir.listFiles();

        if (!destDir.exists())
            destDir.mkdirs();
        else
            log.debug(destDir.getAbsolutePath() + " already exists");

        if (files != null)
        {
            for (int i = 0; i < files.length; i++)
            {
                File file = files[i];
                File dest = new File(destDir, file.getName());

                if (file.isFile())
                    copyFile(file, dest, overwrite);
                else
                    copyDirectory(file, dest, overwrite);
            }
        }
    }

    /**
     * Copy file from source to destination. The directories up to <code>destination</code> will be
     * created if they don't already exist. <code>destination</code> will be overwritten if it
     * already exists.
     *
     * @param srcFile An existing non-directory <code>File</code> to copy bytes from.
     * @param destFile A non-directory <code>File</code> to write bytes to (possibly
     * overwriting).
     *
     * @throws java.io.IOException if <code>source</code> does not exist, <code>destination</code> cannot be
     * written to, or an IO error occurs during copying.
     *
     */
    public static void copyFile(File srcFile, File destFile) throws IOException
    {
        copyFile(srcFile, destFile, true);
    }

    /**
     * Copies a file to a new location, optionally overwriting an existing file in the new location.
     * <p/>
     * If overwrite is <tt>false</tt> and the file already exists, this method logs a warning and returns.
     * If the parent directory of the destination file does not exist, it is created.
     * <p/>
     * If the source file does not exist, this method throws an IOException. If the length of the two files
     * are not the same after the copy completes,
     *
     * @param srcFile the file to copy
     * @param destFile the file to be saved, which can already exist if overwrite is set
     * @param overwrite <tt>true</tt> if an existing file should be overwritten
     * @throws IOException if the source file does not exist, a problem occurs writing to the destination file,
     * or the destination file exists and is read-only
     */
    public static void copyFile(File srcFile, File destFile, boolean overwrite) throws IOException
    {
        if (!srcFile.exists())
        {
            throw new IOException("File " + srcFile + " does not exist");
        }

        InputStream input = new FileInputStream(srcFile);
        try
        {
            copyFile(input, destFile, overwrite);
        }
        finally
        {
            IOUtils.closeQuietly(input);
        }

        if (srcFile.length() != srcFile.length())
        {
            throw new IOException("Failed to copy full contents from " + srcFile + " to " + destFile);
        }
    }

    /**
     * Save an input stream to a file. The client is responsible for opening and closing the provided
     * InputStream. If the file already exists, a warning will be logged and the method will not
     * ovewrite it.
     * <p/>
     * If the parent directory of the destination file does not exist, it is created.
     *
     * @param srcStream the input stream to save
     * @param destFile the file to be saved
     * @throws IOException if a problem occurs writing to the file, or the file exists and is read-only
     */
    public static void copyFile(InputStream srcStream, File destFile) throws IOException
    {
        copyFile(srcStream, destFile, false);
    }

    /**
     * Save an input stream to a file, optionally overwriting the file if is exists. The client
     * is responsible for opening and closing the provided InputStream.
     * <p/>
     * If overwrite is <tt>false</tt> and the file already exists, this method logs a warning and returns.
     * If the parent directory of the destination file does not exist, it is created.
     *
     * @param srcStream the input stream to save
     * @param destFile the file to be saved, which can already exist if overwrite is set
     * @param overwrite <tt>true</tt> if an existing file should be overwritten
     * @throws IOException if a problem occurs writing to the file, or the file exists and is read-only
     */
    public static void copyFile(InputStream srcStream, File destFile, boolean overwrite) throws IOException
    {
        File parentFile = destFile.getParentFile();
        if (!parentFile.isDirectory())
        {
            parentFile.mkdirs();
        }

        if (destFile.exists())
        {
            if (!destFile.canWrite())
            {
                throw new IOException("Unable to open file " + destFile + " for writing.");
            }
            if (!overwrite)
            {
                log.warn(destFile.getAbsolutePath() + " already exists");
                return;
            }
            log.debug("Overwriting file at: " + destFile.getAbsolutePath());
        }
        else
        {
            destFile.createNewFile();
        }

        OutputStream output = new BufferedOutputStream(new FileOutputStream(destFile));
        try
        {
            IOUtils.copy(srcStream, output);
        }
        catch (IOException e)
        {
            log.error("Error writing stream to file: " + destFile.getAbsolutePath());
            throw e;
        }
        finally
        {
            IOUtils.closeQuietly(output);
        }
    }
}
