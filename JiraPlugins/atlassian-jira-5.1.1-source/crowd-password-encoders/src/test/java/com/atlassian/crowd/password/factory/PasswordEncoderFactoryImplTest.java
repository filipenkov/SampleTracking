package com.atlassian.crowd.password.factory;

import java.util.Set;

import com.atlassian.crowd.exception.PasswordEncoderException;
import com.atlassian.crowd.exception.PasswordEncoderNotFoundException;
import com.atlassian.crowd.password.encoder.AtlassianSecurityPasswordEncoder;
import com.atlassian.crowd.password.encoder.LdapPasswordEncoder;
import com.atlassian.crowd.password.encoder.LdapShaPasswordEncoder;
import com.atlassian.crowd.password.encoder.PasswordEncoder;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * PasswordEncoderFactoryImpl Tester.
 */
public class PasswordEncoderFactoryImplTest
{
    private PasswordEncoderFactory passwordEncoderFactory = null;
    private PasswordEncoder internalPasswordEncoder;
    private PasswordEncoder ldapPasswordEncoder;

    @Before
    public void setUp() throws Exception
    {
        passwordEncoderFactory = new PasswordEncoderFactoryImpl();

        internalPasswordEncoder = new AtlassianSecurityPasswordEncoder();
        passwordEncoderFactory.addEncoder(internalPasswordEncoder);

        ldapPasswordEncoder = new LdapShaPasswordEncoder();
        passwordEncoderFactory.addEncoder(ldapPasswordEncoder);

    }

    @After
    public void tearDown() throws Exception
    {
        passwordEncoderFactory = null;
    }

    @Test
    public void testGetInternalEncoder() throws Exception
    {
        // Get an internal password encoder from the factory
        PasswordEncoder internalEncoder = passwordEncoderFactory.getInternalEncoder(PasswordEncoderFactory.ATLASSIAN_SECURITY_ENCODER);

        assertEquals(internalPasswordEncoder, internalEncoder);
    }

    @Test(expected = PasswordEncoderNotFoundException.class)
    public void testGetInternalEncoderWithNullArgument()
    {
        passwordEncoderFactory.getInternalEncoder(null);
    }

    @Test
    public void testGetEncoderThatShouldBeAvailableToBothLDAPAndInternal()
    {
        PasswordEncoder encoder = passwordEncoderFactory.getLdapEncoder(ldapPasswordEncoder.getKey());

        assertEquals(ldapPasswordEncoder, encoder);

        encoder = passwordEncoderFactory.getInternalEncoder(ldapPasswordEncoder.getKey());

        assertEquals(ldapPasswordEncoder, encoder);
    }

    @Test
    public void testGetLdapEncoder() throws Exception
    {
        PasswordEncoder internalEncoder = passwordEncoderFactory.getLdapEncoder(ldapPasswordEncoder.getKey());

        assertEquals(ldapPasswordEncoder, internalEncoder);
    }

    @Test(expected = PasswordEncoderNotFoundException.class)
    public void testGetLdapEncoderWithNullArgument()
    {
        passwordEncoderFactory.getLdapEncoder(null);
    }

    @Test
    public void testGetEncoder() throws Exception
    {
        PasswordEncoder internalEncoder = passwordEncoderFactory.getEncoder(PasswordEncoderFactory.ATLASSIAN_SECURITY_ENCODER);

        assertEquals(internalPasswordEncoder, internalEncoder);
    }

    @Test(expected = PasswordEncoderNotFoundException.class)
    public void testGetEncoderWithNullArgument()
    {
        passwordEncoderFactory.getEncoder(null);
    }

    @Test(expected = PasswordEncoderException.class)
    public void testAddEncoderWithNullArgument()
    {
        passwordEncoderFactory.addEncoder(null);
    }

    @Test(expected = PasswordEncoderException.class)
    public void testAddEncoderWithEncoderKeyArgument()
    {
        // MockPasswordEncoder does not have a valid key
        passwordEncoderFactory.addEncoder(new MockLdapPasswordEncoder());
    }

    @Test(expected = PasswordEncoderException.class)
    public void testAddEncoderWithUnsupportedEncoder()
    {
        // MockPasswordEncoder does not have a valid key
        passwordEncoderFactory.addEncoder(new MockUnsupportedPasswordEncoder());
    }

    @Test
    public void testGetSupportedInternalEncoders() throws Exception
    {
        Set<String> strings = passwordEncoderFactory.getSupportedInternalEncoders();

        assertNotNull(strings);
        assertTrue(strings.contains(PasswordEncoderFactory.ATLASSIAN_SECURITY_ENCODER));
    }

    @Test
    public void testGetSupportedLdapEncoders() throws Exception
    {
        Set<String> strings = passwordEncoderFactory.getSupportedLdapEncoders();

        assertNotNull(strings);
        assertTrue(strings.contains(ldapPasswordEncoder.getKey()));
    }

    private static final class MockLdapPasswordEncoder implements LdapPasswordEncoder
    {

        public String encodePassword(String rawPass, Object salt)
        {
            return "";
        }

        public boolean isPasswordValid(String encPass, String rawPass, Object salt)
        {
            return false;
        }

        public String getKey()
        {
            return null;
        }
    }

    private static final class MockUnsupportedPasswordEncoder implements PasswordEncoder
    {

        public String encodePassword(String rawPass, Object salt)
        {
            return "";
        }

        public boolean isPasswordValid(String encPass, String rawPass, Object salt)
        {
            return false;
        }

        public String getKey()
        {
            return "unsupported-encoder";
        }
    }
}
