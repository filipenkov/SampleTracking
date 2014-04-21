package com.atlassian.gadgets.opensocial.model;

import net.jcip.annotations.Immutable;

/**
 * Representation of a {@link Person}'s address
 *
 * @since 2.0
 */
@Immutable
public final class Address
{

    private final String address;

    public Address(String address)
    {
        if (address == null)
        {
            throw new NullPointerException("address parameter to Address must not be null");
        }
        this.address = address.intern();
    }

    public String value()
    {
        return address;
    }

    public String toString()
    {
        return address;
    }

    public static Address valueOf(String address)
    {
        return new Address(address);
    }

    public boolean equals(Object obj)
    {
        return obj instanceof Address && address.equals(((Address) obj).value());
    }

    public int hashCode()
    {
        return address.hashCode();
    }
}
