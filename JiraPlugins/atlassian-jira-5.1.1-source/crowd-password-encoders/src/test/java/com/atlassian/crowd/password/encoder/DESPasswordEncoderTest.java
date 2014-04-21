package com.atlassian.crowd.password.encoder;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import com.atlassian.crowd.exception.PasswordEncoderException;
import com.atlassian.crowd.manager.property.PropertyManager;
import com.atlassian.crowd.manager.property.PropertyManagerException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * DESPasswordEncoder Tester.
 */
@RunWith(MockitoJUnitRunner.class)
public class DESPasswordEncoderTest
{
    private static final String ENCODED_DES_PASSWORD = "PTz5ICyK1HE=";
    private static final String DECODED_DES_PASSWORD = "secret";

    private DESPasswordEncoder encoder = null;

    @Mock
    private PropertyManager propertyManager;

    @Before
    public void setUp() throws Exception
    {
        when(propertyManager.getDesEncryptionKey()).thenReturn(getEncryptionKey());

        encoder = new DESPasswordEncoder();
        encoder.setPropertyManager(propertyManager);
    }

    @Test
    public void testEncodePassword()
            throws PropertyManagerException
    {
        String encodedPass = encoder.encodePassword(DECODED_DES_PASSWORD, null);

        assertNotNull(encodedPass);
        assertEquals(ENCODED_DES_PASSWORD, encodedPass);
        verify(propertyManager).getDesEncryptionKey();
    }

    @Test
    public void testIsPasswordValid()
            throws PropertyManagerException
    {
        assertTrue(encoder.isPasswordValid(ENCODED_DES_PASSWORD, DECODED_DES_PASSWORD, null));
        verify(propertyManager).getDesEncryptionKey();
    }

    @Test(expected = PasswordEncoderException.class)
    public void testInvalidPasswordEncoding()
    {
        encoder.encodePassword(null, null);
    }

    private static java.security.Key getEncryptionKey() throws Exception
    {
        // create a DES key spec
        DESKeySpec ks = new DESKeySpec(new sun.misc.BASE64Decoder().decodeBuffer("secret-seed"));

        // generate the key from the DES key spec
        return SecretKeyFactory.getInstance(DESPasswordEncoder.PASSWORD_ENCRYPTION_ALGORITHM).generateSecret(ks);
    }
}
