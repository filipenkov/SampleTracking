package com.atlassian.crowd.model.authentication;

/**
 * Configuration of cookies.
 */
public class CookieConfiguration
{
    private final String domain;
    private final boolean secure;
    private final String name;

    public CookieConfiguration(final String domain, final boolean secure, final String name)
    {
        this.domain = domain;
        this.secure = secure;
        this.name = name;
    }

    /**
     * Returns the domain that the cookie should be set for.
     *
     * @return domain
     */
    public String getDomain()
    {
        return domain;
    }

    /**
     * Returns <tt>true</tt> if the cookie should only be included in a secure connection.
     *
     * @return <tt>true</tt> if the cookie should only be included in a secure connection
     */
    public boolean isSecure()
    {
        return secure;
    }

    /**
     * Returns the name of the SSO token cookie.
     *
     * @return name of the SSO token cookie
     */
    public String getName()
    {
        return name;
    }
}
