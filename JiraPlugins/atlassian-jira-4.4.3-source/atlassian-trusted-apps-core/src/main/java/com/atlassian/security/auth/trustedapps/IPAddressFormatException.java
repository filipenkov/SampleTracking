package com.atlassian.security.auth.trustedapps;

public class IPAddressFormatException extends IllegalArgumentException
{
    ///CLOVER:OFF
    
    private final String badIPAddress;

    public IPAddressFormatException(String badIPAddress)
    {
        super("\"" + badIPAddress + "\" does not represent a valid IP address.");
        this.badIPAddress = badIPAddress;
    }

    public String getBadIPAddress()
    {
        return badIPAddress;
    }
}
