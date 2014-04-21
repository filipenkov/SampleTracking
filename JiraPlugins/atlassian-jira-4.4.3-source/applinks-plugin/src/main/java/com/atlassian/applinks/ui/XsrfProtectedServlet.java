package com.atlassian.applinks.ui;

public interface XsrfProtectedServlet
{
    public static final String OVERRIDE_HEADER_NAME = "X-Atlassian-Token";
    public static final String OVERRIDE_HEADER_VALUE = "no-check";
}
