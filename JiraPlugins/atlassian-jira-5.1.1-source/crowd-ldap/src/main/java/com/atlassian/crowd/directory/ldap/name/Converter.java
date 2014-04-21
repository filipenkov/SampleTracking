package com.atlassian.crowd.directory.ldap.name;

import javax.naming.InvalidNameException;
import javax.naming.Name;

/**
 * Converts a string name into a Name, for use with spring-ldap.
 */
public interface Converter
{
    /**
     * Escapes and converts a DN into a Name, for use with LDAP through JNDI.
     * @param dn
     * @return
     */
    Name getName(String dn) throws InvalidNameException;

    /**
     * Returns a Name escaped for JNDI and LDAP, for use with a directory. Used when building a DN from an object name
     * plus directory configuration information
     *
     * Given:
     * attributeName: "cn"
     * objectName: "Smith, John"
     * baseDN: "dc=example, dc=org"
     * It will return a Name that represents the DN:
     * "cn=Smith\, John, dc=example, dc=org"
     * @param attributeName
     * @param objectName
     * @param baseDN A correctly-escaped DN
     * @return
     */
    Name getName(String attributeName, String objectName, Name baseDN) throws InvalidNameException;
}
