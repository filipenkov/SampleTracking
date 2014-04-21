package com.atlassian.core.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * A class that contains utility methods for formatting the size of files into human
 * readable form.
 */
public class FileSize
{
    public FileSize()
    {
        //need public constructor as this is used as a bean in Webwork's JSP taglibs (which don't handle static calls).
    }

    private static final float KB_SIZE = 1024;
    private static final float MB_SIZE = KB_SIZE * KB_SIZE;

    private static final String KB = " kB";
    private static final String MB = " MB";

    /**
     * Convenience method. Calls format(long filesize).
     * @param   filesize  The size of the file in bytes.
     * @return  The size in human readable form.
     * @see #format(long)
     */
    public static String format(Long filesize)
    {
        return format(filesize.longValue());
    }

    /**
     * Format the size of a file in human readable form.  Anything less than a kilobyte
     * is presented in kilobytes to one decimal place.  Anything between a kilobyte and a megabyte is
     * presented in kilobytes to zero decimal places.  Anything greater than one megabyte is
     * presented in megabytes to two decimal places.
     * <p>
     * eg.
     * <ul>
     *  <li>format(512) -> 0.5 kb
     *  <li>format(1024) -> 1.0 kb
     *  <li>format(2048) -> 2 kb
     *  <li>format(1024 * 400) -> 400 kb
     *  <li>format(1024 * 1024) -> 1024 kb
     *  <li>format(1024 * 1024 * 1.2) -> 1.20 Mb
     *  <li>format(1024 * 1024 * 20) -> 20.00 Mb
     * </ul>
     *
     * @param   filesize  The size of the file in bytes.
     * @return  The size in human readable form.
     */
    public static String format(long filesize)
    {
        // TODO: filesize = 1024 gives "1.0 kB", but filesize = 1025 gives "1 kB", this is kinda inconsistent.

        if (filesize > MB_SIZE)
        {
            return formatMB(filesize);
        }
        else if (filesize > KB_SIZE)
        {
            return formatKB(filesize);
        }
        else
        {
            return formatBytes(filesize);
        }

    }

    private static String formatMB(long filesize)
    {
        NumberFormat mbFormat = new DecimalFormat();
        mbFormat.setMinimumIntegerDigits(1);
        mbFormat.setMaximumFractionDigits(2); //format 2 decimal places
        mbFormat.setMinimumFractionDigits(2); //format 2 decimal places
        float mbsize = (float) filesize / MB_SIZE;
        return mbFormat.format(mbsize) + MB;
    }

    private static String formatKB(long filesize)
    {
        long kbsize = Math.round((float) filesize / KB_SIZE); //format 0 decimal places
        return String.valueOf(kbsize) + KB;
    }

    private static String formatBytes(long filesize)
    {
        NumberFormat bFormat = new DecimalFormat();
        bFormat.setMinimumIntegerDigits(1);
        bFormat.setMaximumFractionDigits(1); //format 1 decimal places
        bFormat.setMinimumFractionDigits(1); //format 1 decimal places
        float mbsize = (float) filesize / KB_SIZE;
        return bFormat.format(mbsize) + KB;
    }
}
