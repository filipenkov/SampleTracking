package com.atlassian.jira.util;

import com.atlassian.jira.issue.search.SearchException;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

public class ExceptionUtil
{

    /**
     * Extracts the closest thing to an end-user-comprehensible message from an exception.
     */
    public static String getMessage(Exception e)
    {
        Throwable cause = e.getCause();
        if (cause != null) return cause.getMessage();
        else return e.getMessage();
    }

    public static String getExceptionAsHtml(SearchException e)
    {
        return TextUtils.plainTextToHtml(ExceptionUtils.getFullStackTrace(e));
    }
}
