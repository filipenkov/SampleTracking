package com.atlassian.crowd.model.authentication;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * Validation factors are used to construct a unique token when a {@see com.atlassian.crowd.integration.model.RemotePrincipal}
 * authenticates with the crowd server. When the remote principal later attempts an authentication or authentication
 * attempt if the validation factors do not match the client will then be considered invalid.
 *
 * @author Justen Stepka <jstepka@atlassian.com>
 * @version 1.0
 */
public class ValidationFactor implements Serializable
{
    /**
     * The key to use when setting the client remote address. Host should not beused because if a DNS server is
     * comprimized, the host value can then be forged.
     */
    public static final String REMOTE_ADDRESS = "remote_address";

    /**
     * If you need to set the host - use only for an application.
     */
    public static final String REMOTE_HOST = "remote_host";

    /**
     * The key to use when setting the client remote forwarding address through a proxy.
     */
    public static final String X_FORWARDED_FOR = "X-Forwarded-For";

    /**
     * The Key that represents the generated 'Random-Number' ValidationFactor
     */
    public static final String RANDOM_NUMBER = "Random-Number";

    /**
     * Application name
     */
    public static final String NAME = "NAME";


    private String name;
    private String value;

    public ValidationFactor()
    {
    }

    public ValidationFactor(String name, String value)
    {
        this.name = name;
        this.value = value;
    }

    /**
     * Gets the name.
     *
     * @return The name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Sets the name.
     *
     * @param name The name.
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Gets the value.
     *
     * @return The value.
     */
    public String getValue()
    {
        return value;
    }

    /**
     * Sets the value.
     *
     * @param value The value.
     */
    public void setValue(String value)
    {
        this.value = value;
    }

    public String toString()
    {
        return new ToStringBuilder(this).
                append("name", name).
                append("value", value).toString();
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof ValidationFactor))
        {
            return false;
        }

        ValidationFactor that = (ValidationFactor) o;

        if (name != null ? !name.equals(that.name) : that.name != null)
        {
            return false;
        }
        if (value != null ? !value.equals(that.value) : that.value != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (name != null ? name.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }
}
