package com.atlassian.crowd.directory.ldap;

import java.util.Map;

import javax.naming.Context;

import com.atlassian.crowd.directory.ssl.LdapHostnameVerificationSSLSocketFactory;

import com.google.common.collect.ImmutableMap;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LDAPPropertiesMapperImplTest
{
    @Test
    public void usesHostnameVerifierFactoryWhenSecure()
    {
        LDAPPropertiesMapperImpl pm = new LDAPPropertiesMapperImpl(null);
        pm.setAttributes(ImmutableMap.of(LDAPPropertiesMapper.LDAP_SECURE_KEY, "true"));
        assertTrue(pm.isSecureSSL());
        assertEquals("", pm.getConnectionURL());

        Map<String, String> attrs = pm.getEnvironment();

        assertEquals("ssl", attrs.get(Context.SECURITY_PROTOCOL));
        assertEquals(LdapHostnameVerificationSSLSocketFactory.class.getName(),
                attrs.get("java.naming.ldap.factory.socket"));
    }

    @Test
    public void doesNotSpecifyFactoryWhenNotSecure()
    {
        LDAPPropertiesMapperImpl pm = new LDAPPropertiesMapperImpl(null);
        pm.setAttributes(ImmutableMap.of(LDAPPropertiesMapper.LDAP_SECURE_KEY, "false"));
        assertFalse(pm.isSecureSSL());
        assertEquals("", pm.getConnectionURL());

        Map<String, String> attrs = pm.getEnvironment();

        assertFalse(attrs.containsKey(Context.SECURITY_PROTOCOL));
        assertFalse(attrs.containsKey(LDAPPropertiesMapperImpl.CONNECTION_FACTORY));
    }

    @Test
    public void doesNotSpecifyFactoryWhenLdapsProtocolButNotFlaggedAsSecure()
    {
        LDAPPropertiesMapperImpl pm = new LDAPPropertiesMapperImpl(null);
        pm.setAttributes(ImmutableMap.of(
                LDAPPropertiesMapper.LDAP_SECURE_KEY, "false",
                LDAPPropertiesMapper.LDAP_URL_KEY, "ldaps://test/"
        ));
        assertFalse(pm.isSecureSSL());
        assertEquals("ldaps://test/", pm.getConnectionURL());

        Map<String, String> attrs = pm.getEnvironment();

        assertFalse(attrs.containsKey(Context.SECURITY_PROTOCOL));
        assertFalse(attrs.containsKey(LDAPPropertiesMapperImpl.CONNECTION_FACTORY));
    }
}
