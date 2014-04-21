package com.atlassian.crowd.password.encoder;

import com.atlassian.crowd.exception.PasswordEncoderException;
import com.atlassian.crowd.manager.property.PropertyManager;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

/**
 * DESPasswordEncoder Tester.
 */
public class DESPasswordEncoderTest extends MockObjectTestCase
{
    private DESPasswordEncoder encoder = null;
    private static final String ENCODED_DES_PASSWORD = "PTz5ICyK1HE=";
    private static final String DECODED_DES_PASSWORD = "secret";

    public void setUp() throws Exception
    {
        super.setUp();

        Mock propertyManager = new Mock(PropertyManager.class);

        propertyManager.expects(atMostOnce()).method("getDesEncryptionKey").withNoArguments().will(returnValue(getEncryptionKey()));

        encoder = new DESPasswordEncoder();
        encoder.setPropertyManager((PropertyManager) propertyManager.proxy());
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testEncodePassword()
    {

        String encodedPass = encoder.encodePassword(DECODED_DES_PASSWORD, null);

        assertNotNull(encodedPass);
        assertEquals(ENCODED_DES_PASSWORD, encodedPass);
    }

    public void testIsPasswordValid()
    {
        assertTrue(encoder.isPasswordValid(ENCODED_DES_PASSWORD, DECODED_DES_PASSWORD, null));
    }

    public void testInvalidPasswordEncoding()
    {
        try
        {
            encoder.encodePassword(null, null);
            fail("We should of thrown an exception here");
        }
        catch (PasswordEncoderException e)
        {

        }
    }

    private java.security.Key getEncryptionKey() throws Exception
    {
        // create a DES key spec
        DESKeySpec ks = new DESKeySpec(new sun.misc.BASE64Decoder().decodeBuffer("secret-seed"));

        // generate the key from the DES key spec
        return SecretKeyFactory.getInstance(DESPasswordEncoder.PASSWORD_ENCRYPTION_ALGORITHM).generateSecret(ks);
    }

}
