package com.atlassian.crowd.embedded.admin.ldap;

import junit.framework.TestCase;

public final class LdapDirectoryConfigurationTest extends TestCase
{
    public void testSetLdapUrlSimple() throws Exception
    {
        LdapDirectoryConfiguration configuration = new LdapDirectoryConfiguration();
        configuration.setLdapUrl("ldap://hostname.example.com:389");
        assertFalse("should not be SSL", configuration.isUseSSL());
        assertEquals("hostname.example.com", configuration.getHostname());
        assertEquals(389, configuration.getPort());
    }

    public void testSetLdapUrlSsl() throws Exception
    {
        LdapDirectoryConfiguration configuration = new LdapDirectoryConfiguration();
        configuration.setLdapUrl("ldaps://hostname.example.com:636");
        assertTrue("should be SSL", configuration.isUseSSL());
        assertEquals("hostname.example.com", configuration.getHostname());
        assertEquals(636, configuration.getPort());
    }

    public void testSetLdapUrlNull() throws Exception
    {
        LdapDirectoryConfiguration configuration = new LdapDirectoryConfiguration();
        configuration.setLdapUrl(null);
        // no exception thrown
        assertNull(configuration.getHostname());
        assertEquals(389, configuration.getPort());
    }
}
