package com.atlassian.renderer.util;

import com.atlassian.core.util.ClassLoaderUtils;

import javax.activation.FileTypeMap;
import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.InputStream;

/**
 * This class helps resolve properties about files. It expects there to be a mime.types file accessable
 * in the classpath of the running application to function properly.
 */
public class FileTypeUtil
{
    private static FileTypeMap fileTypeMap;

    static
    {
        InputStream mimeTypesStream = ClassLoaderUtils.getResourceAsStream("mime.types", FileTypeUtil.class);
        fileTypeMap = new MimetypesFileTypeMap(mimeTypesStream);
    }

    public static String getContentType(String fileName)
    {
        return fileTypeMap.getContentType(fileName.toLowerCase());
    }

    /**
     * This probably has an issue with files named with upper case suffixes, as fileTypeMap.getContentType("foo.PNG")
     * doesn't work.
     */ 
    public static String getContentType(File file)
    {
        return fileTypeMap.getContentType(file);
    }
}
