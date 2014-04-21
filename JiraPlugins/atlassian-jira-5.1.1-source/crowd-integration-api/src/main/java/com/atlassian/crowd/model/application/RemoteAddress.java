package com.atlassian.crowd.model.application;

import com.atlassian.ip.Subnet;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.CharEncoding;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

/**
 * Represents a valid IP address (IPv4, IPv6) or hostname for an Application
 */
public class RemoteAddress implements Serializable, Comparable<RemoteAddress>
{
    private static final Logger log = LoggerFactory.getLogger(RemoteAddress.class);

    private String address;
    private String encodedAddressBytes; // note: this is stored in the db in the column 'remote_address_binary' due to re-purposing the column
    private int mask;

    // Used by hibernate only

    private RemoteAddress()
    {
    }

    /**
     * Generates a RemoteAddress based on the value provided
     * @param address can be either a hostname or IP address (IPv4 or IPv6)
     *                An IPv4, IPv6 address can also have a mask defined in CIDR format
     *                Any input that is not recognised as IPv4 or IPv6 format will be treated as a hostname.
     */
    public RemoteAddress(String address)
    {
        Validate.notEmpty(address, "You cannot create a remote address with null address");
        this.address = address;
        parseAddress();
    }

    private void parseAddress()
    {
        try
        {
            final Subnet subnet = Subnet.forPattern(address);
            // Store ip addresses with zero mask
            this.mask = isIPAddress(subnet) ? 0 : subnet.getMask();
            byte[] unchunkedEncodedByteArray = Base64.encodeBase64(subnet.getAddress(), false);
            this.encodedAddressBytes = newStringUtf8(unchunkedEncodedByteArray);
        }
        catch (IllegalArgumentException e) // Treat as a host name
        {
            this.mask = 0;
            this.encodedAddressBytes = null;
        }
    }

    private static boolean isIPAddress(Subnet subnet)
    {
        // Subnet stores ip addresses as all ones subnet
        return subnet.getMask() == subnet.getAddress().length * 8;
    }

    /**
     * Returns the address. The address could be a hostname, IPv4, IPv6 address or an IP address with a mask defined in
     * CIDR format.
     *
     * @return address
     */
    public String getAddress()
    {
        return address;
    }

    private void setAddress(String address)
    {
        this.address = address;
    }

    /**
     * Returns the IP address in bytes array (1 byte per octet), or null if the address is a hostname.
     *
     * @return IP address in bytes, or null if the address is a hostname
     */
    public byte[] getAddressBytes()
    {
        // TODO: we are decoding everytime we want to compare the address - performance issue?
        return Base64.decodeBase64(getBytesUtf8(encodedAddressBytes));
    }

    /**
     * Returns <tt>true</tt> if the address is a hostname. An address is treated as a hostname if it is not a valid IP
     * address.
     *
     * @return <tt>true</tt> if the address is a hostname
     */
    public boolean isHostName()
    {
        return (encodedAddressBytes == null);
    }

    /**
     * Returns the mask length in bits.
     *
     * @return mask length in bits
     */
    public int getMask()
    {
        return mask;
    }

    private void setMask(int mask)
    {
        this.mask = mask;
    }

    /**
     * Returns the IP address bytes as a Base64 encoded string.
     *
     * @return the IP address bytes as a Base64 encoded string
     */
    public String getEncodedAddressBytes()
    {
        return encodedAddressBytes;
    }

    private void setEncodedAddressBytes(String encodedAddressBytes)
    {
        this.encodedAddressBytes = encodedAddressBytes;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RemoteAddress that = (RemoteAddress) o;

        if (address != null ? !address.equals(that.address) : that.address != null) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        return address != null ? address.hashCode() : 0;
    }

    public int compareTo(final RemoteAddress o)
    {
        return address.compareTo(o.getAddress());
    }

    /**
     * Returns a new String by decoding the specified array of bytes using the UTF-8 charset.
     *
     * @param bytes bytes in UTF-8 format
     * @return new string
     */
    private static String newStringUtf8(final byte[] bytes)
    {
        try
        {
            return new String(bytes, CharEncoding.UTF_8);
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException("UTF-8 encoding not supported", e);
        }
    }

    /**
     * Encodes the String into a sequence of bytes using the UTF-8 charset.
     *
     * @param string string to encode
     * @return bytes array
     */
    private static byte[] getBytesUtf8(final String string)
    {
        try
        {
            return string.getBytes(CharEncoding.UTF_8);
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException("UTF-8 encoding not supported", e);
        }
    }
}
