package com.atlassian.gadgets.opensocial.model;

import net.jcip.annotations.Immutable;

/**
 * Representation of a {@link Person}'s phone number
 *
 * @since 2.0
 */
@Immutable
public final class PhoneNumber
{
    private final String number;

    public PhoneNumber(String number)
    {
        if (number == null)
        {
            throw new NullPointerException("number parameter to PhoneNumber must not be null");
        }
        this.number = number;
    }

    public String value()
    {
        return number;
    }

    public String toString()
    {
        return number;
    }

    public static PhoneNumber valueOf(String number)
    {
        return new PhoneNumber(number);
    }

    public boolean equals(Object obj)
    {
        return obj instanceof PhoneNumber && number.equals(((PhoneNumber) obj).value());
    }

    public int hashCode()
    {
        return number.hashCode();
    }

}
