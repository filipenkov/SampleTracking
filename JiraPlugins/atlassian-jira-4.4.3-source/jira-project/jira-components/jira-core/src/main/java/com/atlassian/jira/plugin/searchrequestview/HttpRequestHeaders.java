package com.atlassian.jira.plugin.searchrequestview;

import javax.servlet.http.HttpServletResponse;

public class HttpRequestHeaders implements RequestHeaders
{
    private final HttpServletResponse servletResponse;

    public HttpRequestHeaders(HttpServletResponse servletResponse)
    {
        this.servletResponse = servletResponse;
    }

    public void setDateHeader(String string, long l)
    {
        servletResponse.setDateHeader(string, l);
    }

    public void addDateHeader(String string, long l)
    {
        servletResponse.addDateHeader(string, l);
    }

    public void setHeader(String string, String string1)
    {
        servletResponse.setHeader(string, string1);
    }

    public void addHeader(String string, String string1)
    {
        servletResponse.addHeader(string, string1);
    }

    public void setIntHeader(String string, int i)
    {
        servletResponse.setIntHeader(string, i);
    }

    public void addIntHeader(String string, int i)
    {
        servletResponse.addIntHeader(string, i);
    }
}
