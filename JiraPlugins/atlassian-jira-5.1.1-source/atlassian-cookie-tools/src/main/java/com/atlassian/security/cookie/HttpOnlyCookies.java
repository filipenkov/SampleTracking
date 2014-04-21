package com.atlassian.security.cookie;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * Constructs HttpOnly {@code Cookie}s and adds them to {@link HttpServletResponse}s.
 * The cookie encoding logic in {@link ServerCookie} is derived from Tomcat 6.0.29.
 *
 * @since 2.0
 */
public final class HttpOnlyCookies
{
    private static final String COOKIE_HEADER = "Set-Cookie";

    private HttpOnlyCookies()
    {
        // prevent construction
    }

    /**
     * Adds the specified {@code Cookie}s to the specified {@code HttpServletResponse} with the HttpOnly flag.
     * Cookies marked as HTTP-only will not be available to JavaScript code executing in browsers that support
     * the flag; only the browser itself can access them.
     *
     * @param response the response to add to
     * @param cookies the cookies to add
     */
    public static void addHttpOnlyCookies(HttpServletResponse response, Cookie[] cookies)
    {
        if (cookies != null)
        {
            for (Cookie cookie : cookies)
            {
                addHttpOnlyCookie(response, cookie);
            }
        }
    }

    /**
     * Adds the specified {@code Cookie} to the specified {@code HttpServletResponse} with the HttpOnly flag.
     * Cookies marked as HTTP-only will not be available to JavaScript code executing in browsers that support
     * the flag; only the browser itself can access them.
     *
     * @param response the response to add to
     * @param cookie the cookie to add
     */
    public static void addHttpOnlyCookie(HttpServletResponse response, Cookie cookie)
    {
        response.addHeader(COOKIE_HEADER, generateCookieString(cookie));
    }

    /**
     * Generates the textual value of the specified {@code Cookie}, enforcing the use
     * of the HttpOnly flag.
     *
     * @param cookie the cookie to add
     * @return the textual value of the produced {@code Cookie}
     */
    private static String generateCookieString(Cookie cookie)
    {
        StringBuffer sb = new StringBuffer();
        ServerCookie.appendCookieValue(sb,
            cookie.getVersion(), cookie.getName(), cookie.getValue(),
            cookie.getPath(), cookie.getDomain(), cookie.getComment(),
            cookie.getMaxAge(), cookie.getSecure(), true); // true == httpOnly
        return sb.toString();
    }
}
