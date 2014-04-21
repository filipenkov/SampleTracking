package com.atlassian.crowd.password.encoder;

import com.atlassian.crowd.exception.PasswordEncoderException;
import com.atlassian.crowd.manager.property.PropertyManager;
import com.atlassian.crowd.manager.property.PropertyManagerException;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

/**
 * LdapSshaPasswordEncoder Tester.
 */
public class LdapSshaPasswordEncoderTest extends MockObjectTestCase
{
    LdapSshaPasswordEncoder passwordEncoder = null;
    private Mock propertyManager;

    public void setUp() throws Exception
    {
        super.setUp();

        passwordEncoder = new LdapSshaPasswordEncoder();

        propertyManager = new Mock(PropertyManager.class);

        passwordEncoder.setPropertyManager((PropertyManager) propertyManager.proxy());
    }

    public void testEncodePasswordWithNullSalt()
    {
        propertyManager.expects(once()).method("getTokenSeed").withNoArguments().will(returnValue("secret-seed"));

        String encodedPassword = passwordEncoder.encodePassword("secret", null);

        assertNotNull(encodedPassword);
        assertNotSame("secret", encodedPassword);

    }

    public void testEncodePasswordWithSalt()
    {
        propertyManager.expects(never()).method("getTokenSeed").withNoArguments().will(returnValue("secret-seed"));

        String encodedPassword = passwordEncoder.encodePassword("secret", "secret-salt".getBytes());

        assertNotNull(encodedPassword);
        assertNotSame("secret", encodedPassword);

    }

    public void testEncodePasswordWithNullSaltAndNoTokenSeed()
    {
        propertyManager.expects(once()).method("getTokenSeed").withNoArguments().will(throwException(new PropertyManagerException()));

        try
        {
            passwordEncoder.encodePassword("secret", null);
            fail("We should of thrown an exception");
        }
        catch (PasswordEncoderException e)
        {

        }
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
    }
}
