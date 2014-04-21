package com.atlassian.crowd.password.encoder;

import com.atlassian.crowd.exception.PasswordEncoderException;
import com.atlassian.crowd.manager.property.PropertyManager;
import com.atlassian.crowd.manager.property.PropertyManagerException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.mockito.Mockito.when;

/**
 * LdapSshaPasswordEncoder Tester.
 */
@RunWith(MockitoJUnitRunner.class)
public class LdapSshaPasswordEncoderTest
{
    LdapSshaPasswordEncoder passwordEncoder = null;

    @Mock
    private PropertyManager propertyManager;

    @Before
    public void setUp() throws Exception
    {
        passwordEncoder = new LdapSshaPasswordEncoder();
        passwordEncoder.setPropertyManager(propertyManager);
    }

    @Test
    public void testEncodePasswordWithNullSalt()
            throws PropertyManagerException
    {
        when(propertyManager.getTokenSeed()).thenReturn("secret-seed");

        String encodedPassword = passwordEncoder.encodePassword("secret", null);

        assertNotNull(encodedPassword);
        assertNotSame("secret", encodedPassword);
    }

    @Test
    public void testEncodePasswordWithSalt()
            throws PropertyManagerException
    {
        when(propertyManager.getTokenSeed()).thenReturn("secret-seed");

        String encodedPassword = passwordEncoder.encodePassword("secret", "secret-salt".getBytes());

        assertNotNull(encodedPassword);
        assertNotSame("secret", encodedPassword);
    }

    @Test(expected = PasswordEncoderException.class)
    public void testEncodePasswordWithNullSaltAndNoTokenSeed()
            throws PropertyManagerException
    {
        when(propertyManager.getTokenSeed()).thenThrow(new PropertyManagerException());
        passwordEncoder.encodePassword("secret", null);
    }
}
