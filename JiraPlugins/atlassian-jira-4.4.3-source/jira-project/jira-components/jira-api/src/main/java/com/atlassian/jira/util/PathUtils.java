package com.atlassian.jira.util;

import org.apache.commons.lang.StringUtils;

import java.io.File;

/**
 * This class contains utility methods for manipulating paths.
 *
 * @since v4.3
 */
public class PathUtils
{
    private PathUtils()
    {
        // don't instantiate
    }

    public static String appendFileSeparator(final String filePath)
    {
        return (filePath == null) ? null : (filePath.endsWith("/") || filePath.endsWith("\\") ? filePath : filePath + java.io.File.separator);
    }

    public static String joinPaths(final String... paths)
    {
        return StringUtils.join(paths, File.separator);
    }
}
