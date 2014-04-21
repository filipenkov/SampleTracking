package com.atlassian.security.auth.trustedapps;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

import junit.framework.TestCase;

/**
 * This test is slow as it generates a new public/private KeyPair each time (very CPU intensive prime search)
 */
public class TestBouncyCastleEncryptionProviderKeyPairGeneration extends TestCase
{
    private final EncryptionProvider encryptionProvider = new BouncyCastleEncryptionProvider();
    private final KeyPair keyPair;

    public TestBouncyCastleEncryptionProviderKeyPairGeneration()
    {
        try
        {
            keyPair = encryptionProvider.generateNewKeyPair();
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException(e);
        }
        catch (NoSuchProviderException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void testPrivateKey() throws Exception
    {
        assertEquals("RSA", keyPair.getPrivate().getAlgorithm());
        byte[] data = keyPair.getPrivate().getEncoded();

        PrivateKey privateKey = encryptionProvider.toPrivateKey(data);
        assertEquals(keyPair.getPrivate(), privateKey);

        try
        {
            data[7] += 7;
            encryptionProvider.toPrivateKey(data);
            fail("wrong data");
        }
        catch (InvalidKeySpecException e)
        {
            // expected
        }
    }

    public void testPublicKey() throws Exception
    {
        assertEquals("RSA", keyPair.getPublic().getAlgorithm());
        byte[] data = keyPair.getPublic().getEncoded();

        PublicKey publicKey = encryptionProvider.toPublicKey(data);
        assertEquals(keyPair.getPublic(), publicKey);

        try
        {
            data[5] += 7;
            encryptionProvider.toPublicKey(data);
            fail("wrong data");
        }
        catch (InvalidKeySpecException e)
        {
            // expected
        }
    }

    public void testCertificateLifecycle() throws Exception
    {
        EncryptedCertificate encrypted = encryptionProvider.createEncryptedCertificate("TestBouncyCastleEncryptionProvider", keyPair.getPrivate(), "myAppId");
        assertNotNull(encrypted);
        assertEquals("myAppId", encrypted.getID());
        assertNotNull(encrypted.getCertificate());
        assertNotNull(encrypted.getSecretKey());

        ApplicationCertificate decrypted = encryptionProvider.decodeEncryptedCertificate(encrypted, keyPair.getPublic(), "myAppId");
        assertNotNull(decrypted);
        assertEquals("TestBouncyCastleEncryptionProvider", decrypted.getUserName());
        assertEquals("myAppId", decrypted.getApplicationID());
    }

    public void testFunnyUserName() throws Exception
    {
        String user = "\u8FCE\u6B61\u5149\u81E8\u5178";
        EncryptedCertificate encrypted = encryptionProvider.createEncryptedCertificate(user, keyPair.getPrivate(), "myAppId");
        assertNotNull(encrypted);
        assertEquals("myAppId", encrypted.getID());
        assertNotNull(encrypted.getCertificate());
        assertNotNull(encrypted.getSecretKey());

        ApplicationCertificate decrypted = encryptionProvider.decodeEncryptedCertificate(encrypted, keyPair.getPublic(), "myAppId");
        assertNotNull(decrypted);
        assertEquals(user, decrypted.getUserName());
        assertEquals("myAppId", decrypted.getApplicationID());
    }
}
