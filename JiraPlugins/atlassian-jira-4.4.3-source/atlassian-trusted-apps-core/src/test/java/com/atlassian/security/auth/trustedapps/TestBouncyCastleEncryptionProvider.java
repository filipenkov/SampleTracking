package com.atlassian.security.auth.trustedapps;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.atlassian.security.auth.trustedapps.ApplicationRetriever.ApplicationNotFoundException;
import com.atlassian.security.auth.trustedapps.ApplicationRetriever.RemoteSystemNotFoundException;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;

import junit.framework.TestCase;

public class TestBouncyCastleEncryptionProvider extends TestCase
{
    private final EncryptionProvider encryptionProvider = new BouncyCastleEncryptionProvider() {
        @Override
        public Application getApplicationCertificate(String baseUrl) throws ApplicationRetriever.RetrievalException {
            InputStream inputStream = null;
            try {
                URL url = new URL(baseUrl + TrustedApplicationUtils.Constant.CERTIFICATE_URL_PATH);
                inputStream = new FileInputStream(url.getFile());
                return new InputStreamApplicationRetriever(inputStream, this).getApplication();
            } catch (FileNotFoundException e) {
                throw new ApplicationNotFoundException(e);
            } catch (MalformedURLException e) {
                throw new RemoteSystemNotFoundException(e);
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                       // ignore.
                    }
                }
            }
        }
    };

    public void testGetApplicationCertificateReturnsEmpty() throws Exception
    {
        URL url = this.getClass().getResource("/trustedapps");
        File root = new File(new URI(url.toString()));
        assertTrue(root.isDirectory());
        File cert = new File(root, "admin/appTrustCertificate");
        cert.delete();
        File certDir = new File(root, "admin");
        certDir.mkdirs();
        assertTrue(cert.getCanonicalPath(), cert.createNewFile());
        FileWriter writer = new FileWriter(cert);
        writer.write("");
        writer.close();
        try
        {
            encryptionProvider.getApplicationCertificate(root.toURI().toString());
            fail("CertNotFound expected");
        }
        catch (ApplicationNotFoundException e)
        {
            // expected
        }
    }

    public void testGetApplicationCertificateNotFoundAtAll() throws Exception
    {
        URL url = this.getClass().getResource("/");
        File root = new File(new URI(url.toString()));
        assertTrue(root.isDirectory());
        try
        {
            encryptionProvider.getApplicationCertificate(root.toURI().toString());
            fail("FNFE expected");
        }
        catch (ApplicationNotFoundException e)
        {
            // expected
        }
    }

    public void testGetApplicationCertificateMalformedUrl() throws Exception
    {
        try
        {
            encryptionProvider.getApplicationCertificate("noscheme://some/url");
            fail("InvalidApplicationDetailsException expected");
        }
        catch (RemoteSystemNotFoundException yay)
        {
            // expected
        }
    }

    public void testGetApplicationCertificateReturnsProperly() throws Exception
    {
        final String publicKeyData = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAu6q2RIBqhT0Ny59W7cZ1zwSHFBAJeCpkbzY0nCMUK5eiim/9TUfNvlpSJj5Ks/zs5Ll4R129rYtTfIfvIq4sSMjXKB8rftswet4uQWdTcTJyesLHbYgHqSIS0b+1JtQsDvjB5aEYApS4nc+fjZmwJQkpN++J8QpqQeoGDzq+zVxAzuGdVrkMEuCXcAS4znYFqW7VytvS0DHhreSIh1fGWLJIqE15Jih91Up2W+dRdLAdW0OVozOsWYFx5+L325PWkSgsDCMGVyNCdTnwCzI4FiYsT4MLODkOnOt9gDQTMjn8H1NM5K3d3Sb7WH2hKwuBxLyDRUY7qUB1bwtGU6LgjQIDAQAB";

        URL url = this.getClass().getResource("/trustedapps");
        File root = new File(new URI(url.toString()));
        assertTrue(root.isDirectory());
        File cert = new File(root, "admin/appTrustCertificate");
        cert.delete();
        File certDir = new File(root, "admin");
        certDir.mkdirs();
        assertTrue(cert.getCanonicalPath(), cert.createNewFile());
        FileWriter writer = new FileWriter(cert);
        writer.write("myApplicationId\n");
        writer.write(publicKeyData);
        writer.close();
        Application app = encryptionProvider.getApplicationCertificate(root.toURI().toString());
        assertEquals("myApplicationId", app.getID());
    }

    public void testDecodeBadSecretKey() throws Exception
    {
        Token token = new Token();
        long time = System.currentTimeMillis();
        EncryptedCertificate encrypted = new DefaultEncryptedCertificate("id", "bad-secret-key-bad!", token.encrypt("fred", time), TrustedApplicationUtils.Constant.VERSION, token.getMagic());
        try
        {
            encryptionProvider.decodeEncryptedCertificate(encrypted, token.getPublicKey(), "id");
            fail("InvalidCertificateException expected");
        }
        catch (InvalidCertificateException e)
        {
            // expected
        }
    }

    public void testDecodeSecretKey() throws Exception
    {
        Token token = new Token();
        long time = System.currentTimeMillis();
        EncryptedCertificate encrypted = new DefaultEncryptedCertificate("id", token.getSecretKey(), token.encrypt("fred", time), TrustedApplicationUtils.Constant.VERSION, token.getMagic());
        ApplicationCertificate cert = encryptionProvider.decodeEncryptedCertificate(encrypted, token.getPublicKey(), "id");
        assertEquals(time, cert.getCreationTime().getTime());
        assertEquals("fred", cert.getUserName());
        assertEquals("id", cert.getApplicationID());
    }

    public void testDecodeBadMagicNumber() throws Exception
    {
        Token token = new Token();
        long time = System.currentTimeMillis();
        EncryptedCertificate encrypted = new DefaultEncryptedCertificate("id", token.getSecretKey(), token.encrypt("fred", time), TrustedApplicationUtils.Constant.VERSION, "bad-magic, bad!");
        try
        {
            encryptionProvider.decodeEncryptedCertificate(encrypted, token.getPublicKey(), "id");
            fail("InvalidCertificateException expected");
        }
        catch (InvalidCertificateException e)
        {
            // expected
        }
    }

    public void testDecodeInvalidCertificate() throws Exception
    {
        Token token = new Token();
        EncryptedCertificate encrypted = new DefaultEncryptedCertificate("id", token.getSecretKey(), "TestTrustedApplicationClient.id", TrustedApplicationUtils.Constant.VERSION, token.getMagic());
        try
        {
            encryptionProvider.decodeEncryptedCertificate(encrypted, token.getPublicKey(), "id");
            fail("InvalidCertificateException expected");
        }
        catch (InvalidCertificateException e)
        {
            // expected - will be a SystemException under IBM JDK due to SER-118
        }
    }

    public void testDecodeNumberFormatException() throws Exception
    {
        Token token = new Token()
        {
            String encrypt(String userName, long time)
            {
                try
                {
                    Cipher cipher = Cipher.getInstance(KeyData.STREAM_CIPHER, KeyData.BOUNCY_CASTLE_PROVIDER);
                    cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                    StringBuffer buffer = new StringBuffer("not-a-number").append("\n").append(userName).append("\n").append(TrustedApplicationUtils.Constant.MAGIC);
                    return new String(Base64.encode(cipher.doFinal(buffer.toString().getBytes())), TrustedApplicationUtils.Constant.CHARSET_NAME);
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
        };
        long time = System.currentTimeMillis();
        EncryptedCertificate encrypted = new DefaultEncryptedCertificate("id", token.getSecretKey(), token.encrypt("fred", time), TrustedApplicationUtils.Constant.VERSION, token.getMagic());
        try
        {
            encryptionProvider.decodeEncryptedCertificate(encrypted, token.getPublicKey(), "id");
            fail("InvalidCertificateException expected");
        }
        catch (SystemException e)
        {
            // expected
        }
    }

    public void testDecodeNullMagicNumberVersion0() throws Exception
    {
        Token token = new Token();
        long time = System.currentTimeMillis();
        EncryptedCertificate encrypted = new DefaultEncryptedCertificate("id", token.getSecretKey(), token.encrypt("fred", time), null, null);
        ApplicationCertificate cert = encryptionProvider.decodeEncryptedCertificate(encrypted, token.getPublicKey(), "id");
        assertEquals(time, cert.getCreationTime().getTime());
        assertEquals("fred", cert.getUserName());
        assertEquals("id", cert.getApplicationID());
    }

    public void testDecodeNullMagicNumberVersion1() throws Exception
    {
        Token token = new Token();
        long time = System.currentTimeMillis();
        EncryptedCertificate encrypted = new DefaultEncryptedCertificate("id", token.getSecretKey(), token.encrypt("fred", time), TrustedApplicationUtils.Constant.VERSION, null);
        try
        {
            encryptionProvider.decodeEncryptedCertificate(encrypted, token.getPublicKey(), "id");
            fail("InvalidCertificateException expected");
        }
        catch (InvalidCertificateException e)
        {
            // expected
        }
    }

    public void testBCSecretKeyFactory()
    {
        BouncyCastleEncryptionProvider.SecretKeyFactory keyFactory = new BouncyCastleEncryptionProvider.BCKeyFactory();
        SecretKey secretKey = keyFactory.generateSecretKey();
        assertNotNull(secretKey);
        assertEquals("RC4", secretKey.getAlgorithm());
        assertEquals("RAW", secretKey.getFormat());
        assertNotNull(secretKey.getEncoded());
        assertEquals(16, secretKey.getEncoded().length);
    }

    public void testSecretKeyValidatorValidatesLength16()
    {
        BouncyCastleEncryptionProvider.SecretKeyValidator keyValidator = new BouncyCastleEncryptionProvider.TransmissionValidator();
        SecretKey secretKey = new SecretKeySpec(new byte[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, "ALG");
        assertTrue(keyValidator.isValid(secretKey));
    }

    public void testSecretKeyValidatorInValidatesLengthLessThan16()
    {
        BouncyCastleEncryptionProvider.SecretKeyValidator keyValidator = new BouncyCastleEncryptionProvider.TransmissionValidator();
        SecretKey secretKey = new SecretKeySpec(new byte[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, "ALG");
        assertFalse(keyValidator.isValid(secretKey));
    }

    public void testSecretKeyValidatorInValidatesLengthGreaterThan16()
    {
        BouncyCastleEncryptionProvider.SecretKeyValidator keyValidator = new BouncyCastleEncryptionProvider.TransmissionValidator();
        SecretKey secretKey = new SecretKeySpec(new byte[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, "ALG");
        assertFalse(keyValidator.isValid(secretKey));
    }

    public void testSecretKeyValidatorInValidatesLeadingZero()
    {
        BouncyCastleEncryptionProvider.SecretKeyValidator keyValidator = new BouncyCastleEncryptionProvider.TransmissionValidator();
        SecretKey secretKey = new SecretKeySpec(new byte[] { 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, "ALG");
        assertFalse(keyValidator.isValid(secretKey));
    }

    public void testValidatingSecretKeyFactory()
    {
        final int[] called = new int[2];
        final int factory = 0;
        final int validation = 1;
        final int expectedCallCount = 5;

        BouncyCastleEncryptionProvider.SecretKeyFactory mockFactory = new BouncyCastleEncryptionProvider.SecretKeyFactory()
        {
            public SecretKey generateSecretKey()
            {
                ++called[factory];
                return new SecretKeySpec(new byte[] { (byte) called[factory], 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, "ALG");
            }
        };

        BouncyCastleEncryptionProvider.SecretKeyValidator mockValidator = new BouncyCastleEncryptionProvider.SecretKeyValidator()
        {
            public boolean isValid(SecretKey secretKey)
            {
                return ++called[validation] == expectedCallCount;
            }
        };

        BouncyCastleEncryptionProvider.SecretKeyFactory testFactory = new BouncyCastleEncryptionProvider.ValidatingSecretKeyFactory(mockFactory, mockValidator);
        SecretKey secretKey = testFactory.generateSecretKey();
        assertNotNull(secretKey);
        assertEquals(expectedCallCount, called[factory]);
        assertEquals(expectedCallCount, called[validation]);
        assertEquals(expectedCallCount, called[factory]);
        assertNotNull(secretKey.getEncoded());
        assertEquals(16, secretKey.getEncoded().length);
        assertEquals(expectedCallCount, secretKey.getEncoded()[0]);
    }

    public void testEncryptedCertificateOmitsSignatureWhenUrlIsNotProvided() throws NoSuchAlgorithmException
    {
        Token t = new Token();
        
        EncryptedCertificate c = encryptionProvider.createEncryptedCertificate("user", t.privateKey, "appId", null);
        assertNull(c.getSignature());
        assertEquals(Integer.valueOf(1), c.getProtocolVersion());
    }
    
    public void testEncryptedCertificateIncludesUrlSignature() throws NoSuchAlgorithmException
    {
        Token t = new Token();
        
        EncryptedCertificate c = encryptionProvider.createEncryptedCertificate("user", t.privateKey, "appId", "http://www.example.com/");
        assertNotNull(c.getSignature());
        assertEquals(Integer.valueOf(2), c.getProtocolVersion());
    }
    
    public void testEncryptedCertificateSignatureValidates() throws Exception
    {
        Token t = new Token();
        
        EncryptedCertificate c = encryptionProvider.createEncryptedCertificate("user", t.privateKey, "appId", "http://www.example.com/");

        // Decrypt to get the timestamp out
        ApplicationCertificate cert = encryptionProvider.decodeEncryptedCertificate(c, t.publicKey, "appId");
        
        Signature s = Signature.getInstance("SHA1withRSA");
        s.initVerify(t.publicKey);
        s.update((cert.getCreationTime().getTime() + "\n" + "http://www.example.com/").getBytes("us-ascii"));
        assertTrue(s.verify(Base64.decode(c.getSignature())));
    }

    
    private static class Token
    {
        static final class KeyData
        {
            private static final String PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCySptbugHAzWUJY3ALWhuSCPhVXnwbUBfsRExYQitBCVny4V1DcU2SAx22bH9dSM0X7NdMObF74r+Wd77QoPAtaySqFLqCeRCbFmhHgVSi+pGeCipTpueefSkz2AX8Aj+9x27tqjBsX1LtNWVLDsinEhBWN68R+iEOmf/6jGWObQIDAQAB";
            private static final String PRIVATE_KEY = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBALJKm1u6AcDNZQljcAtaG5II+FVefBtQF+xETFhCK0EJWfLhXUNxTZIDHbZsf11IzRfs10w5sXviv5Z3vtCg8C1rJKoUuoJ5EJsWaEeBVKL6kZ4KKlOm5559KTPYBfwCP73Hbu2qMGxfUu01ZUsOyKcSEFY3rxH6IQ6Z//qMZY5tAgMBAAECgYB4QXJAkFmWXfOEPZnZTlHCUmKN0kkLcx5vsjF8ZkUefNw6wl9Rmh6kGY30+YF+vhf3xzwAoflggjSPnP0LY0Ibf0XxMcNjR1zBsl9X7gKfXghIunS6gbcwrEwBNc5GR4zkYjYaZQ4zVvm3oMS2glV9NlXAUl41VL2XAQC/ENwbUQJBAOdoAz4hZGgke9AxoKLZh215gY+PLXqVLlWf14Ypk70Efk/bVvF10EsAOuAm9queCyr0qNf/vgHrm4HHXwJz4SsCQQDFPXir5qs+Kf2Y0KQ+WO5IRaNmrOlNvWDqJP/tDGfF/TYo6nSI0dGtWNfwZyDB47PbUq3zxCHYjExBJ9vQNZLHAkEA4JlCtHYCl1X52jug1w7c9DN/vc/Q626J909aB3ypSUdoNagFPf0EexcxDcijmDSgUEQA8Qzm5cRBPfg9Tgsc2wJBAIKbiv2hmEFowtHfTvMuJlNbMbF6zF67CaLib0oEDe+QFb4QSqyS69py20MItytM4btYy3GArbzcYl4+y5La9t8CQE2BkMV3MLcpAKjxtK5SYwCyLT591k35isGxmIlSQBQbDmGP9L5ZeXmVGVxRCGbBQjCzeoafPvUZo65kaRQHUJc=";
            // private static final String SECRET_KEY = "6FcMEe0BgY6ohvXJrjnuig==";
            private static final String SECRET_KEY = "T52KBiVRRol8V2/DS7cy9G9iRdv+vSH4wLqH9q7wdSiFtZw9MRG3ihhLSmmH1MnkwPeLg+y0wtDZuokMr6eJwPo+1dHO3t2pb7IwVJg+UqGgn97LdihAMgwqLApnQIMquDe5uDuuK6Qaey+D6EXu2E90FI4Z0mHqaE3Wbo+HC50=";
            private static final String ALGORITHM = "RSA";
            private static final Provider BOUNCY_CASTLE_PROVIDER = new BouncyCastleProvider();
            private static final String STREAM_CIPHER = "RC4";
            private static final String ASYM_CIPHER = "RSA/NONE/NoPadding";
        }

        final PrivateKey privateKey;
        final PublicKey publicKey;
        final SecretKey secretKey;

        Token()
        {
            try
            {
                KeyFactory keyFactory = KeyFactory.getInstance(KeyData.ALGORITHM, KeyData.BOUNCY_CASTLE_PROVIDER);
                privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(Base64.decode(KeyData.PRIVATE_KEY.getBytes(TrustedApplicationUtils.Constant.CHARSET_NAME))));
                publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(Base64.decode(KeyData.PUBLIC_KEY.getBytes(TrustedApplicationUtils.Constant.CHARSET_NAME))));

                Cipher cipher = Cipher.getInstance(KeyData.ASYM_CIPHER, KeyData.BOUNCY_CASTLE_PROVIDER);
                cipher.init(Cipher.DECRYPT_MODE, publicKey);

                byte[] secretKeyData = cipher.doFinal(Base64.decode(KeyData.SECRET_KEY.getBytes(TrustedApplicationUtils.Constant.CHARSET_NAME)));

                secretKey = new SecretKeySpec(secretKeyData, KeyData.STREAM_CIPHER);
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }

        PublicKey getPublicKey()
        {
            return publicKey;
        }

        String getSecretKey()
        {
            try
            {
                Cipher cipher = Cipher.getInstance(KeyData.ASYM_CIPHER, KeyData.BOUNCY_CASTLE_PROVIDER);
                cipher.init(Cipher.ENCRYPT_MODE, privateKey);
                return new String(Base64.encode(cipher.doFinal(secretKey.getEncoded())), TrustedApplicationUtils.Constant.CHARSET_NAME);
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }

        String getMagic()
        {
            try
            {
                Cipher cipher = Cipher.getInstance(KeyData.ASYM_CIPHER, KeyData.BOUNCY_CASTLE_PROVIDER);
                cipher.init(Cipher.ENCRYPT_MODE, privateKey);
                return new String(Base64.encode(cipher.doFinal(TrustedApplicationUtils.Constant.MAGIC.getBytes())), TrustedApplicationUtils.Constant.CHARSET_NAME);
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }

        String encrypt(String userName, long time)
        {
            try
            {
                Cipher cipher = Cipher.getInstance(KeyData.STREAM_CIPHER, KeyData.BOUNCY_CASTLE_PROVIDER);
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                StringBuffer buffer = new StringBuffer(String.valueOf(time)).append("\n").append(userName).append("\n").append(TrustedApplicationUtils.Constant.MAGIC);
                return new String(Base64.encode(cipher.doFinal(buffer.toString().getBytes(TrustedApplicationUtils.Constant.CHARSET_NAME))), TrustedApplicationUtils.Constant.CHARSET_NAME);
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
    }
}