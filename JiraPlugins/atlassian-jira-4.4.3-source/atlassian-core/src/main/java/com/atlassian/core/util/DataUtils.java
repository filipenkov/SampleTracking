/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Nov 19, 2002
 * Time: 4:42:36 PM
 * To change this template use Options | File Templates.
 */
package com.atlassian.core.util;

public class DataUtils
{
    public static final String SUFFIX_ZIP = ".zip";
    public static final String SUFFIX_XML = ".xml";

    public static String getXmlFilename(String filename)
    {
        if (filename.toLowerCase().endsWith(DataUtils.SUFFIX_ZIP))
            return getXmlFilename(filename.substring(0, filename.length() - 4));
        else if (!filename.toLowerCase().endsWith(DataUtils.SUFFIX_XML))
            filename += DataUtils.SUFFIX_XML;

        return filename;
    }

    public static String getZipFilename(String filename)
    {
        if (filename.toLowerCase().endsWith(SUFFIX_XML))
            return getZipFilename(filename.substring(0, filename.length() - 4));
        else if (!filename.toLowerCase().endsWith(SUFFIX_ZIP))
            filename += SUFFIX_ZIP;

        return filename;
    }
}
