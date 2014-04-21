package com.atlassian.gadgets.opensocial.model;

import net.jcip.annotations.Immutable;

/**
 * Representation of a {@link Person}'s e-mail address
 *
 * @since 2.0
 */
@Immutable
public final class EmailAddress
{
    private final String emailAddress;

    public EmailAddress(String emailAddress)
    {
        if (emailAddress == null)
        {
            throw new NullPointerException("emailAddress parameter to EmailAddress must not be null");
        }
        this.emailAddress = emailAddress.intern();
    }

    public String value()
    {
        return emailAddress;
    }

    public String toString()
    {
        return emailAddress;
    }

    public static EmailAddress valueOf(String emailAddress)
    {
        return new EmailAddress(emailAddress);
    }

    public boolean equals(Object obj)
    {
        return obj instanceof EmailAddress && emailAddress.equals(((EmailAddress) obj).value());
    }

    public int hashCode()
    {
        return emailAddress.hashCode();
    }

}
