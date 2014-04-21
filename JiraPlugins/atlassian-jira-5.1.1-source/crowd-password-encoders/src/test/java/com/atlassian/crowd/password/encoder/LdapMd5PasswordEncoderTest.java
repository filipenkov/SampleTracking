package com.atlassian.crowd.password.encoder;

import junit.framework.TestCase;
import org.apache.commons.lang.StringUtils;

/**
 * LdapMd5PasswordEncoder Tester.
 */
public class LdapMd5PasswordEncoderTest extends TestCase
{
    private LdapMd5PasswordEncoder encoder = null;
    private static final String SECRET_PASSWORD = "asecretpassword";
    private static final String SECRET_PASSWORD_MD5 = "+7dgt9v4JmaFZQDU/UoCyQ==";

    public void setUp() throws Exception
    {
        super.setUp();
        encoder = new LdapMd5PasswordEncoder();
        encoder.setForceLowerCasePrefix(false);
        encoder.setEncodeHashAsBase64(true);
    }

    public void testEncodePassword()
    {
        String encodedPassword = encoder.encodePassword(SECRET_PASSWORD, null);

        assertEquals(0, StringUtils.indexOf(encodedPassword, LdapMd5PasswordEncoder.MD5_PREFIX, -1));

        assertEquals(LdapMd5PasswordEncoder.MD5_PREFIX + SECRET_PASSWORD_MD5, encodedPassword);
    }

    public void testEncodePasswordWithLowercasePrefix()
    {
        encoder.setForceLowerCasePrefix(true);

        String encodedPassword = encoder.encodePassword(SECRET_PASSWORD, null);

        assertEquals(0, StringUtils.indexOf(encodedPassword, LdapMd5PasswordEncoder.MD5_PREFIX_LC, -1));

        assertEquals(LdapMd5PasswordEncoder.MD5_PREFIX_LC + SECRET_PASSWORD_MD5, encodedPassword);
    }

    public void testEncodePasswordWithNullPassword()
    {
        String encodedPassword = encoder.encodePassword(null, null);

        assertEquals(0, StringUtils.indexOf(encodedPassword, LdapMd5PasswordEncoder.MD5_PREFIX, -1));
    }

    public void testIsPasswordValidWithNoPrefix()
    {
        boolean passwordValid = encoder.isPasswordValid(SECRET_PASSWORD_MD5, SECRET_PASSWORD, null);

        assertTrue(passwordValid);
    }

    public void testIsPasswordInvalid()
    {
        boolean passwordValid = encoder.isPasswordValid(LdapMd5PasswordEncoder.MD5_PREFIX + SECRET_PASSWORD_MD5, SECRET_PASSWORD, null);

        assertTrue(passwordValid);
    }

    public void testIsPasswordInvalidWithLowerCasePrefix()
    {
        encoder.setForceLowerCasePrefix(true);

        boolean passwordValid = encoder.isPasswordValid(LdapMd5PasswordEncoder.MD5_PREFIX_LC + SECRET_PASSWORD_MD5, SECRET_PASSWORD, null);

        assertTrue(passwordValid);
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
        encoder = null;
    }
}
