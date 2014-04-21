package com.atlassian.crowd.password.encoder;

import com.atlassian.crowd.exception.PasswordEncoderException;
import junit.framework.TestCase;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AtlassianSecurityPasswordEncoderTest extends TestCase
{
    private AtlassianSecurityPasswordEncoder encoder;
    private PasswordEncoder oldEncoder;
    private com.atlassian.security.password.PasswordEncoder defaultEncoder;

    @Override
    protected void setUp() throws Exception
    {
        oldEncoder = mock(PasswordEncoder.class);
        defaultEncoder = mock(com.atlassian.security.password.PasswordEncoder.class);
        encoder = new AtlassianSecurityPasswordEncoder(defaultEncoder, oldEncoder);
    }

    public void testEncodePassword()
    {
        when(defaultEncoder.encodePassword("password")).thenReturn("newEncPass");

        assertEquals("newEncPass", encoder.encodePassword("password", null));
    }

    public void testEncodePasswordWithIllegalPassword()
    {
        when(defaultEncoder.encodePassword(null)).thenThrow(new IllegalArgumentException());

        try
        {
            encoder.encodePassword(null, null);
            fail("Expected PasswordEncoderException");
        }
        catch (PasswordEncoderException e)
        {
            // Success
        }
    }

    public void testIsPasswordValidDefault()
    {
        when(defaultEncoder.canDecodePassword("newEncPass")).thenReturn(true);
        when(defaultEncoder.isValidPassword("password", "newEncPass")).thenReturn(true);
        assertTrue("New password was not valid", encoder.isPasswordValid("newEncPass", "password", null));
    }

    public void testIsPasswordValidDefaultException()
    {
        when(defaultEncoder.canDecodePassword("newFaultyEncPass")).thenReturn(true);
        when(defaultEncoder.isValidPassword("password", "newFaultyEncPass")).thenThrow(new IllegalArgumentException());

        assertFalse("Faulty password should not be valid", encoder.isPasswordValid("newFaultyEncPass", "password", null));
    }

    public void testIsPasswordValidOld()
    {
        when(defaultEncoder.canDecodePassword("oldEncPass")).thenReturn(false);
        when(oldEncoder.isPasswordValid("oldEncPass", "password", null)).thenReturn(true);
        assertTrue("Old password was not valid", encoder.isPasswordValid("oldEncPass", "password", null));
    }

    public void testIsUpgradeRequired()
    {
        when(defaultEncoder.canDecodePassword("oldEncPass")).thenReturn(false);
        assertTrue("Upgrade should be required", encoder.isUpgradeRequired("oldEncPass"));
    }
}
