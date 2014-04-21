package com.atlassian.security.cookie;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CookieTestServlet extends HttpServlet
{
    public static final String COOKIE_NAME = "myCookieName";
    public static final String COOKIE_VALUE = "myCookieValue";

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
        throws ServletException, IOException
    {
        Cookie cookie = new Cookie(COOKIE_NAME, COOKIE_VALUE);

        String param = request.getParameter("httpOnly");
        boolean httpOnly = param != null && Boolean.parseBoolean(param);
        
        if (httpOnly)
        {
            HttpOnlyCookies.addHttpOnlyCookie(response, cookie);
        }
        else
        {
            response.addCookie(cookie);
        }
    }
}
