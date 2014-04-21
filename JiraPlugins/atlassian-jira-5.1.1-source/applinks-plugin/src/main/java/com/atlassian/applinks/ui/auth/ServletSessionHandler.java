package com.atlassian.applinks.ui.auth;

import javax.servlet.http.HttpServletRequest;

class ServletSessionHandler implements AdminUIAuthenticator.SessionHandler
{
    private final HttpServletRequest request;

    public ServletSessionHandler(final HttpServletRequest request)
    {
        this.request = request;
    }

    public void set(final String key, final Object value)
    {
        request.getSession().setAttribute(key, value);
    }

    public Object get(final String key)
    {
        return request.getSession().getAttribute(key);
    }
}
