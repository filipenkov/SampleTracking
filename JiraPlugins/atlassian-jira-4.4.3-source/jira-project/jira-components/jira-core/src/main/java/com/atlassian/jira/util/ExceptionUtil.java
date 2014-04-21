package com.atlassian.jira.util;

import com.atlassian.jira.issue.search.SearchException;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.exception.NestableException;

public class ExceptionUtil
{

    /**
     * Extracts the closest thing to an end-user-comprehensible message from an exception.
     */
    public static String getMessage(Exception e)
    {
        if (e instanceof NestableException)
        {
            Throwable cause = ((NestableException)e).getCause();
            if (cause != null) return cause.getMessage();
        }
        return e.getMessage();
    }

    public static String getExceptionAsHtml(SearchException e)
    {
        return TextUtils.plainTextToHtml(ExceptionUtils.getFullStackTrace(e));
    }
}
