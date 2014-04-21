package com.atlassian.crowd.directory.ldap.name;

/**
 * Escapes special characters in names and/or DNs to make them suitable for LDAP-through-JNDI. Backslashes are a special
 * character in both LDAP and JNDI, and so must be escaped twice. This means that a \ becomes \\\\
 *
 * Other Ldap special characters are escaped with a single backslash. The characters in question are:
 * \ + # = , < >
 *
 * An example escaped DN:
 * cn=Smith\, John, dc=example, dc=org
 */
public interface Encoder
{
    /**
     * Given a name, such as "Smith, John", returns a version escaped for Ldap and JNDI: "Smith\, John".
     * @param name
     * @return
     */
    String nameEncode(String name);

    /**
     * Given a DN, escapes it for JNDI. "cn=in \\ out, dc=example, dc=org" becomes "cn=in \\\\ out, dc=example, dc=org".
     * Does not perform LDAP escaping.
     * @param dn
     * @return
     */
    String dnEncode(String dn);
}
