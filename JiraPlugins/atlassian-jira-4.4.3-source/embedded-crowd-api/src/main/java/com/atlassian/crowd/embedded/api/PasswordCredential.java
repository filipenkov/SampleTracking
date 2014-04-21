package com.atlassian.crowd.embedded.api;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * Password based authentication information.
 *
 * @author Justen Stepka <jstepka@atlassian.com>
 * @version 1.0
 */
public class PasswordCredential implements Serializable
{
    public static final PasswordCredential NONE = encrypted("X");  // Can't use empty string because Oracle will think it is null

    private boolean encryptedCredential = false;

    protected String credential;

    public static PasswordCredential encrypted(String encryptedCredential)
    {
        return new PasswordCredential(encryptedCredential, true);
    }

    public static PasswordCredential unencrypted(String unencryptedCredential)
    {
        return new PasswordCredential(unencryptedCredential, false);
    }

    /**
     * Default constructor used by hibernate
     * @deprecated since 1.3.2, please use any other PasswordCredential constructor,
     * this constructor should only be used by hibernate. If this constructor is used,
     * please be aware that you must set {@link PasswordCredential#setEncryptedCredential(boolean)}
     */
    public PasswordCredential()
    {
        this.encryptedCredential = true;
    }

    public PasswordCredential(PasswordCredential passwordCredential)
    {
        if (passwordCredential != null)
        {
            setCredential(passwordCredential.getCredential());
            setEncryptedCredential(passwordCredential.isEncryptedCredential());
        }
    }

    /**
     * Constructor that takes an unencrypted credential. ie {@link PasswordCredential#encryptedCredential}
     * is false.
     *
     * @param unencryptedCredential the unencrypted password credential
     */
    public PasswordCredential(String unencryptedCredential)
    {
        this.encryptedCredential = false;
        this.credential = unencryptedCredential;
    }

    public PasswordCredential(String credential, boolean encryptedCredential)
    {
        this.credential = credential;
        this.encryptedCredential = encryptedCredential;
    }

    /**
     * Gets the password credential.
     *
     * @return The credential.
     */
    public String getCredential()
    {
        return credential;
    }

    /**
     * Sets the password credential.
     *
     * @param credential The credential.
     */
    public void setCredential(String credential)
    {
        this.credential = credential;
    }

    public boolean isEncryptedCredential()
    {
        return encryptedCredential;
    }

    public void setEncryptedCredential(boolean encryptedCredential)
    {
        this.encryptedCredential = encryptedCredential;
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        PasswordCredential that = (PasswordCredential) o;
        return credential != null ? credential.equals(that.credential) : that.credential == null;
    }

    public int hashCode()
    {
        return (credential != null ? credential.hashCode() : 0);
    }

    public String toString()
    {
        return new ToStringBuilder(this).
                append("credential", credential).
                append("encryptedCredential", encryptedCredential).
                toString();
    }
}