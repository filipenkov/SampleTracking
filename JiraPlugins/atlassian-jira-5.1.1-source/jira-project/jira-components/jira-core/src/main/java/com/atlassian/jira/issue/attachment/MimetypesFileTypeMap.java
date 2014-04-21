package com.atlassian.jira.issue.attachment;

import com.atlassian.jira.web.ServletContextProvider;

import javax.servlet.ServletContext;

public final class MimetypesFileTypeMap
{
    private static final javax.activation.MimetypesFileTypeMap MIMETYPES_FILE_TYPE_MAP = new javax.activation.MimetypesFileTypeMap();

    public static String getContentType(String filename)
    {
        ServletContext context = ServletContextProvider.getServletContext();
        if (context != null)
        {
            return context.getMimeType(filename);
        }
        else
        {
            return MIMETYPES_FILE_TYPE_MAP.getContentType(filename);
        }
    }

    private MimetypesFileTypeMap()
    {
    }
}
