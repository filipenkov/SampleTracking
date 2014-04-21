/*
 * Copyright (c) 2006 Atlassian Software Systems. All Rights Reserved.
 */
package com.atlassian.crowd.model.authentication;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * An authentication token maps the valid authentication. The <code>token</code>
 * attribute maintains a unique identifier that can be validated verses the crowd
 * server.
 *
 * @author Justen Stepka <jstepka@atlassian.com>
 * @version 1.0
 */
public class AuthenticatedToken implements Serializable
{
    private String name;
    private String token;

    /**
     * Default constructor.
     */
    public AuthenticatedToken()
    {
    }

    /**
     * @param name  The name of the authenticated principal or application.
     * @param token The token.
     */
    public AuthenticatedToken(String name, String token)
    {
        this.name = name;
        this.token = token;
    }

    /**
     * Gets the name of the authenticated client..
     *
     * @return The name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Sets the name of the authenticated client.
     *
     * @param name The name.
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Gets the token.
     *
     * @return The token.
     */
    public String getToken()
    {
        return token;
    }

    /**
     * Sets the token.
     *
     * @param token The token.
     */
    public void setToken(String token)
    {
        this.token = token;
    }

    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final AuthenticatedToken that = (AuthenticatedToken) o;

        if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null)
        {
            return false;
        }
        if (getToken() != null ? !getToken().equals(that.getToken()) : that.getToken() != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result = getName() != null ? getName().hashCode() : 0;
        result = 31 * result + (getToken() != null ? getToken().hashCode() : 0);
        return result;
    }

    public String toString()
    {
        return new ToStringBuilder(this).
                append("name", getName()).
                append("token", getToken()).toString();
    }
}