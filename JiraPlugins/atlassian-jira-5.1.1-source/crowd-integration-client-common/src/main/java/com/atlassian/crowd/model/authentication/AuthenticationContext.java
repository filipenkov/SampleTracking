package com.atlassian.crowd.model.authentication;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Minimal information necessary when authenticating with the Crowd server.
 */
public abstract class AuthenticationContext implements Serializable
{
    private String name;
    private PasswordCredential credential;
    private ValidationFactor[] validationFactors;

    protected AuthenticationContext()
    {
    }

    protected AuthenticationContext(String name, PasswordCredential credential, ValidationFactor[] validationFactors)
    {
        this.name = name;
        this.credential = credential;
        this.validationFactors = validationFactors;
    }

    /**
     * Gets the name of the authenticating entity.
     *
     * @return The name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Sets the name of the authenticating entity.
     *
     * @param name The name.
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Gets the authenticating credential information.
     *
     * @return The credentials.
     */
    public PasswordCredential getCredential()
    {
        return credential;
    }

    /**
     * Sets the authenticating credential information.
     *
     * @param credential The credentials.
     */
    public void setCredential(PasswordCredential credential)
    {
        this.credential = credential;
    }

    /**
     * Gets the authenticating validation factors.
     *
     * @return The validation factors.
     */
    public ValidationFactor[] getValidationFactors()
    {
        return validationFactors;
    }

    /**
     * Sets the authenticating validation factors.
     *
     * @param validationFactors The validation factors.
     */
    public void setValidationFactors(ValidationFactor[] validationFactors)
    {
        this.validationFactors = validationFactors;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AuthenticationContext that = (AuthenticationContext) o;

        if (credential != null ? !credential.equals(that.credential) : that.credential != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (!Arrays.equals(validationFactors, that.validationFactors)) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (credential != null ? credential.hashCode() : 0);
        result = 31 * result + (validationFactors != null ? Arrays.hashCode(validationFactors) : 0);
        return result;
    }

    public String toString()
    {
        return new ToStringBuilder(this).
                append("name", name).
                append("credential", credential).
                append("validationFactors", validationFactors).toString();
    }
}