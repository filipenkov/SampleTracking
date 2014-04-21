package com.atlassian.crowd.directory.ldap.name;

import org.springframework.ldap.core.LdapEncoder;

/**
 * Escapes the defined Ldap special characters. See interface for more detail. Uses spring-ldap for LDAP encoding, and
 * adds JNDI escaping on top.
 */
public class GenericEncoder implements Encoder
{
    public String nameEncode(String name)
    {
        return LdapEncoder.nameEncode(name.replace("\\", "\\\\"));
    }

    public String dnEncode(String dn)
    {
        return dn.replace("\\", "\\\\");
    }
}
