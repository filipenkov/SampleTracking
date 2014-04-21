package com.atlassian.jira.plugin.searchrequestview;

/**
 * This is a subset of the HttpServletResponse that just deals with setting headers.
 *
 * @see javax.servlet.http.HttpServletResponse
 */
public interface RequestHeaders
{
    void setDateHeader(java.lang.String string, long l);

    void addDateHeader(java.lang.String string, long l);

    void setHeader(java.lang.String string, java.lang.String string1);

    void addHeader(java.lang.String string, java.lang.String string1);

    void setIntHeader(java.lang.String string, int i);

    void addIntHeader(java.lang.String string, int i);

}
