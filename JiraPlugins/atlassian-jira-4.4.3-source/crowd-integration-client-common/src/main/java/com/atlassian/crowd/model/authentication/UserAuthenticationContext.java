package com.atlassian.crowd.model.authentication;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * The <code>UserAuthenticationContext</code> is used to authenticate
 * {@link com.atlassian.crowd.model.user.User users}.
 */
public class UserAuthenticationContext extends AuthenticationContext implements Serializable
{
    private String application;

    public UserAuthenticationContext()
    {
    }

    public UserAuthenticationContext(String name, PasswordCredential credential, ValidationFactor[] validationFactors, String application)
    {
        super(name, credential, validationFactors);
        this.application = application;
    }

    /**
     * Gets the application name the authentication
     * request is for.
     *
     * @return The application name.
     */
    public String getApplication()
    {
        return application;
    }

    /**
     * Sets the application name the authentication
     * request is for.
     *
     * @param application The application name.
     */
    public void setApplication(String application)
    {
        this.application = application;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        UserAuthenticationContext that = (UserAuthenticationContext) o;

        if (application != null ? !application.equals(that.application) : that.application != null) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (application != null ? application.hashCode() : 0);
        return result;
    }

    public String toString()
    {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("application", application).toString();
    }
}