package com.atlassian.crowd.password.factory;

import com.atlassian.crowd.exception.PasswordEncoderException;
import com.atlassian.crowd.exception.PasswordEncoderNotFoundException;
import com.atlassian.crowd.password.encoder.AtlassianSecurityPasswordEncoder;
import com.atlassian.crowd.password.encoder.LdapPasswordEncoder;
import com.atlassian.crowd.password.encoder.LdapShaPasswordEncoder;
import com.atlassian.crowd.password.encoder.PasswordEncoder;
import junit.framework.TestCase;

import java.util.Set;

/**
 * PasswordEncoderFactoryImpl Tester.
 */
public class PasswordEncoderFactoryImplTest extends TestCase
{
    private PasswordEncoderFactory passwordEncoderFactory = null;
    private PasswordEncoder internalPasswordEncoder;
    private PasswordEncoder ldapPasswordEncoder;

    public void setUp() throws Exception
    {
        super.setUp();

        passwordEncoderFactory = new PasswordEncoderFactoryImpl();

        internalPasswordEncoder = new AtlassianSecurityPasswordEncoder();
        passwordEncoderFactory.addEncoder(internalPasswordEncoder);

        ldapPasswordEncoder = new LdapShaPasswordEncoder();
        passwordEncoderFactory.addEncoder(ldapPasswordEncoder);

    }

    public void tearDown() throws Exception
    {
        passwordEncoderFactory = null;
        super.tearDown();
    }

    public void testGetInternalEncoder() throws Exception
    {
        // Get an internal password encoder from the factory
        PasswordEncoder internalEncoder = passwordEncoderFactory.getInternalEncoder(PasswordEncoderFactory.ATLASSIAN_SECURITY_ENCODER);

        assertEquals(internalPasswordEncoder, internalEncoder);
    }

    public void testGetInternalEncoderWithNullArgument()
    {
        try
        {
            passwordEncoderFactory.getInternalEncoder(null);
            fail("An exception should of been thrown");
        }
        catch (PasswordEncoderNotFoundException e)
        {

        }
    }

    public void testGetEncoderThatShouldBeAvailableToBothLDAPAndInternal()
    {
        PasswordEncoder encoder = passwordEncoderFactory.getLdapEncoder(ldapPasswordEncoder.getKey());

        assertEquals(ldapPasswordEncoder, encoder);

        encoder = passwordEncoderFactory.getInternalEncoder(ldapPasswordEncoder.getKey());

        assertEquals(ldapPasswordEncoder, encoder);
    }

    public void testGetLdapEncoder() throws Exception
    {
        PasswordEncoder internalEncoder = passwordEncoderFactory.getLdapEncoder(ldapPasswordEncoder.getKey());

        assertEquals(ldapPasswordEncoder, internalEncoder);
    }

    public void testGetLdapEncoderWithNullArgument()
    {
        try
        {
            passwordEncoderFactory.getLdapEncoder(null);
            fail("An exception should of been thrown");
        }
        catch (PasswordEncoderNotFoundException e)
        {

        }
    }

    public void testGetEncoder() throws Exception
    {
        PasswordEncoder internalEncoder = passwordEncoderFactory.getEncoder(PasswordEncoderFactory.ATLASSIAN_SECURITY_ENCODER);

        assertEquals(internalPasswordEncoder, internalEncoder);
    }

    public void testGetEncoderWithNullArgument()
    {
        try
        {
            passwordEncoderFactory.getEncoder(null);
            fail("An exception should of been thrown");
        }
        catch (PasswordEncoderNotFoundException e)
        {

        }
    }

    public void testAddEncoderWithNullArgument()
    {

        try
        {
            passwordEncoderFactory.addEncoder(null);
            fail("An exception should of been thrown");
        }
        catch (PasswordEncoderException e)
        {

        }

    }

    public void testAddEncoderWithEncoderKeyArgument()
    {
        try
        {
            passwordEncoderFactory.addEncoder(new MockLdapPasswordEncoder());
            fail("An exception should of been thrown, MockPasswordEncoder does not have a valid key");
        }
        catch (PasswordEncoderException e)
        {

        }

    }

    public void testAddEncoderWithUnsupportedEncoder()
    {
        try
        {
            passwordEncoderFactory.addEncoder(new MockUnsupportedPasswordEncoder());
            fail("An exception should of been thrown, MockPasswordEncoder does not have a valid key");
        }
        catch (PasswordEncoderException e)
        {

        }

    }

    public void testGetSupportedInternalEncoders() throws Exception
    {
        Set<String> strings = passwordEncoderFactory.getSupportedInternalEncoders();

        assertNotNull(strings);
        assertTrue(strings.contains(PasswordEncoderFactory.ATLASSIAN_SECURITY_ENCODER));
    }

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
