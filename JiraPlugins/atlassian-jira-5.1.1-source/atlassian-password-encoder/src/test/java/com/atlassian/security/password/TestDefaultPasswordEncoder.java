package com.atlassian.security.password;

import junit.framework.TestCase;
import org.apache.commons.lang.ArrayUtils;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.atlassian.security.password.StringUtils.newStringUtf8;
import static org.apache.commons.codec.binary.Base64.encodeBase64;
import static org.mockito.Mockito.*;

public final class TestDefaultPasswordEncoder extends TestCase
{
    private static final String PREFIX = "prefix";

    @Mock
    private PasswordHashGenerator hashGenerator;
    @Mock
    private SaltGenerator saltGenerator;
    private PasswordEncoder encoder;

    protected void setUp() throws Exception
    {
        super.setUp();
        MockitoAnnotations.initMocks(this);

        encoder = new DefaultPasswordEncoder(PREFIX, hashGenerator, saltGenerator);
    }

    public void testPrefixIsAdded() throws Exception
    {
        when(hashGenerator.generateHash(any(byte[].class), any(byte[].class))).thenReturn(new byte[0]);
        when(saltGenerator.generateSalt(anyInt())).thenReturn(new byte[0]);

        String encoded = encoder.encodePassword("secret");

        assertEquals("{" + PREFIX + "}", encoded);
    }

    public void testEncodeNullPasswordThrowsException() throws Exception
    {
        try
        {
            encoder.encodePassword(null);
            fail("Expected exception when encoding null password");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
    }

    public void testEncodeEmptyPasswordThrowsException() throws Exception
    {
        try
        {
            encoder.encodePassword("");
            fail("Expected exception when encoding empty password");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
    }

    public void testValidateNullRawPasswordThrowsException() throws Exception
    {
        try
        {
            encoder.isValidPassword(null, "{" + PREFIX + "}");
            fail("Expected exception when verifying null password");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
    }

    public void testValidateNullEncodedPasswordThrowsException() throws Exception
    {
        try
        {
            encoder.isValidPassword("secret", null);
            fail("Expected exception when verifying null password");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
    }

    public void testCanDecodeNullPasswordReturnsFalse() throws Exception
    {
        boolean result = encoder.canDecodePassword(null);
        assertFalse("should not be able to decode null password", result);
    }

    public void testCanDecodeEmptyPasswordReturnsFalse() throws Exception
    {
        boolean result = encoder.canDecodePassword("");
        assertFalse("should not be able to decode empty password", result);
    }

    public void testCanDecodePrefixReturnsTrue() throws Exception
    {
        boolean result = encoder.canDecodePassword("{" + PREFIX + "}");
        assertTrue("should be able to decode password with correct prefix", result);
    }

    public void testSaltIsIncludedInOutput() throws Exception
    {
        byte[] salt = {0x0, 0x1, 0x2, 0x3, 0x4};
        when(saltGenerator.generateSalt(anyInt())).thenReturn(salt);
        byte[] hash = new byte[0];
        when(hashGenerator.generateHash(any(byte[].class), any(byte[].class))).thenReturn(hash);

        String encoded = encoder.encodePassword("secret");

        assertEquals("{" + PREFIX + "}" + newStringUtf8(encodeBase64(salt)), encoded);
    }

    public void testHashIsIncludedInOutput() throws Exception
    {
        byte[] salt = new byte[0];
        when(saltGenerator.generateSalt(anyInt())).thenReturn(salt);
        byte[] hash = {0x0, 0x1, 0x2, 0x3, 0x4};
        when(hashGenerator.generateHash(any(byte[].class), any(byte[].class))).thenReturn(hash);

        String encoded = encoder.encodePassword("secret");

        assertEquals("{" + PREFIX + "}" + newStringUtf8(encodeBase64(hash)), encoded);
    }

    public void testSaltAndHashIncludedInOutput() throws Exception
    {
        byte[] salt = {0x0, 0x1, 0x2, 0x3, 0x4};
        when(saltGenerator.generateSalt(anyInt())).thenReturn(salt);
        byte[] hash = {0x5, 0x6, 0x7, 0x8, 0x9};
        when(hashGenerator.generateHash(any(byte[].class), any(byte[].class))).thenReturn(hash);

        String encoded = encoder.encodePassword("secret");
        byte[] saltPlusHash = ArrayUtils.addAll(salt, hash);
        assertEquals("{" + PREFIX + "}" + newStringUtf8(encodeBase64(saltPlusHash)), encoded);
    }
}
