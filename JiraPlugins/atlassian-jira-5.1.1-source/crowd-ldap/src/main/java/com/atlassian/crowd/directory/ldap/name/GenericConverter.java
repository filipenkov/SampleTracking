package com.atlassian.crowd.directory.ldap.name;

import javax.naming.CompositeName;
import javax.naming.InvalidNameException;
import javax.naming.Name;

/**
 * See Converter interface for details.
 */
public class GenericConverter implements Converter
{
    private final Encoder encoder;

    public GenericConverter(Encoder encoder)
    {
        this.encoder = encoder;
    }

    public Name getName(String dn) throws InvalidNameException
    {
        String escapedDn = encoder.dnEncode(dn);
        return new CompositeName(escapedDn);
    }
    
    public Name getName(String attributeName, String objectName, Name baseDN) throws InvalidNameException
    {
        String dn = attributeName + "=" + encoder.nameEncode(objectName) + ", " + baseDN;
        return new CompositeName(dn);
    }
}
