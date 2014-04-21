package com.atlassian.crowd.directory.ldap.name;

/**
 * Also handles escaping of the AD-specific special character /
 */
public class ActiveDirectoryEncoder extends GenericEncoder
{
    public String nameEncode(String name)
    {
        // Encode the / after the super-class encodes, as the spring-ldap libraries used by GenericEncoder are not
        //  smart enough to understand that \\/ is escaping and escape it again.
        return super.nameEncode(name).replace("/", "\\/");
    }

    public String dnEncode(String dn)
    {
        // Encode the / after the super-class encodes, as the spring-ldap libraries used by GenericEncoder are not
        //  smart enough to understand that \\/ is escaping and escape it again.
        return super.dnEncode(dn).replace("/", "\\/");
    }
}
