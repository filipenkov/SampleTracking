package com.atlassian.security.auth.trustedapps;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.CharConversionException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.atlassian.security.auth.trustedapps.Transcoder.Base64Transcoder;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class BouncyCastleEncryptionProvider extends BaseEncryptionProvider
{
    public static final Provider PROVIDER = new BouncyCastleProvider();

    private static final String STREAM_CIPHER = "RC4";
    private static final String ASYM_CIPHER = "RSA/NONE/NoPadding";
    private static final String ASYM_ALGORITHM = "RSA";
    private static final String SIGNATURE_ALGORITHM = "SHA1withRSA";

    private static final String UTF8 = "utf-8";
    

    private final SecretKeyFactory secretKeyFactory;
    private final Transcoder transcoder;
    
    public BouncyCastleEncryptionProvider()
    {
        this(new ValidatingSecretKeyFactory(new BCKeyFactory(), new TransmissionValidator()), new Base64Transcoder());
    }

    private BouncyCastleEncryptionProvider(SecretKeyFactory secretKeyFactory, Transcoder transcoder)
    {
        Null.not("secretKeyFactory", secretKeyFactory);
        Null.not("transcoder", transcoder);

        this.secretKeyFactory = secretKeyFactory;
        this.transcoder = transcoder;
    }

    /**
     * Decodes the given form into the real key object according to the given algorithm Uses Bouncy Castle as a provider
     * 
     * @param encodedForm
     *            the byte[] containing the key data
     * @return the generated PublicKey
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws NoSuchProviderException
     */
    public PublicKey toPublicKey(byte[] encodedForm) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException
    {
        final X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(encodedForm);
        final KeyFactory keyFactory = KeyFactory.getInstance(ASYM_ALGORITHM, PROVIDER);
        return keyFactory.generatePublic(pubKeySpec);
    }

    /**
     * Decodes the given form into the real key object according to the given algorithm Uses Bouncy Castle as a provider
     * 
     * @param encodedForm
     *            the PKS8 encoded key data
     * @return a fully formed PrivateKey
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws NoSuchProviderException
     */
    public PrivateKey toPrivateKey(byte[] encodedForm) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException
    {
        final PKCS8EncodedKeySpec pubKeySpec = new PKCS8EncodedKeySpec(encodedForm);
        final KeyFactory keyFactory = KeyFactory.getInstance(ASYM_ALGORITHM, PROVIDER);
        return keyFactory.generatePrivate(pubKeySpec);
    }

    /**
     * Generates a new KeyPair.
     * <p>
     * Given algorithm name will be used to generate the key pair. It is mandatory. Security provides parameter is
     * optional and can be null in which case the choice of a provider is left to the VM. Key size is optional and can
     * be set to -1 in which case the default size is used.
     * 
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     */
    public KeyPair generateNewKeyPair() throws NoSuchAlgorithmException, NoSuchProviderException
    {
        final KeyPairGenerator gen = KeyPairGenerator.getInstance(ASYM_ALGORITHM, PROVIDER);
        return gen.generateKeyPair();
    }

    public ApplicationCertificate decodeEncryptedCertificate(EncryptedCertificate encCert, PublicKey publicKey, String appId) throws InvalidCertificateException
    {
        final BufferedReader in;
        try
        {
            final Cipher asymCipher = Cipher.getInstance(ASYM_CIPHER, PROVIDER);
            asymCipher.init(Cipher.DECRYPT_MODE, publicKey);

            /**
             * this should only happen with protocol version#1 or greater
             */
            final String encryptedMagicNumber = encCert.getMagicNumber();
            if (encryptedMagicNumber != null)
            {
                final String magicNumber = new String(asymCipher.doFinal(transcoder.decode(encryptedMagicNumber)), TrustedApplicationUtils.Constant.CHARSET_NAME);
                TrustedApplicationUtils.validateMagicNumber("public key", appId, encCert.getProtocolVersion(), magicNumber);
            }
            else if (encCert.getProtocolVersion() != null)
            {
                throw new InvalidCertificateException(new TransportErrorMessage.BadMagicNumber("public key", appId));
            }

            final byte[] secretKeyData = asymCipher.doFinal(transcoder.decode(encCert.getSecretKey()));
            final SecretKeySpec secretKeySpec = new SecretKeySpec(secretKeyData, STREAM_CIPHER);

            final Cipher symCipher = Cipher.getInstance(STREAM_CIPHER, PROVIDER);
            symCipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            final byte[] decryptedData = symCipher.doFinal(transcoder.decode(encCert.getCertificate()));
            in = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(decryptedData), TrustedApplicationUtils.Constant.CHARSET_NAME));
        }
        // ///CLOVER:OFF
        catch (NoSuchAlgorithmException e)
        {
            throw new AssertionError(e);
        }
        catch (NoSuchPaddingException e)
        {
            throw new AssertionError(e);
        }
        catch (NumberFormatException e)
        {
            throw new SystemException(appId, e);
        }
        catch (IllegalBlockSizeException e)
        {
            throw new SystemException(appId, e);
        }
        catch (BadPaddingException e)
        {
            throw new SystemException(appId, e);
        }
        catch (InvalidKeyException e)
        {
            throw new InvalidCertificateException(new TransportErrorMessage.BadMagicNumber("secret key", appId));
        }
        catch (SecurityException e)
        {
            // this is here for Java 1.4 only where this exception is thrown when a bad secret key is encountered
            throw new InvalidCertificateException(new TransportErrorMessage.BadMagicNumber("secret key", appId));
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        // /CLOVER:ON

        try
        {
            final String created = in.readLine();
            final String userName = in.readLine();
            // validate the magic number before trying to parse the timestamp
            TrustedApplicationUtils.validateMagicNumber("secret key", appId, encCert.getProtocolVersion(), in.readLine());
            final long timeCreated = Long.parseLong(created);

            return new DefaultApplicationCertificate(appId, userName, timeCreated);
        }
        catch (NumberFormatException e)
        {
            throw new SystemException(appId, e);
        }
        // ///CLOVER:OFF
        catch (CharConversionException e)
        {
            // only thrown under IBM JDK when unsupported utf8 chars are encountered
            throw new SystemException(appId, e);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        // /CLOVER:ON
    }

    public EncryptedCertificate createEncryptedCertificate(String userName, PrivateKey privateKey, String appId)
    {
        return createEncryptedCertificate(userName, privateKey, appId, null);
    }
    
    public EncryptedCertificate createEncryptedCertificate(String userName, PrivateKey privateKey, String appId, String urlToSign)
    {
        try
        {
            final SecretKey secretKey = secretKeyFactory.generateSecretKey();
            final Cipher symmetricCipher = Cipher.getInstance(STREAM_CIPHER, PROVIDER);
            symmetricCipher.init(Cipher.ENCRYPT_MODE, secretKey);

            final Cipher asymCipher = Cipher.getInstance(ASYM_CIPHER, PROVIDER);
            asymCipher.init(Cipher.ENCRYPT_MODE, privateKey);

            final String encryptedKey = transcoder.encode(asymCipher.doFinal(secretKey.getEncoded()));
            final String encryptedMagic = transcoder.encode(asymCipher.doFinal(transcoder.getBytes(TrustedApplicationUtils.Constant.MAGIC)));

            String stamp = Long.toString(System.currentTimeMillis());
            
            final StringWriter writer = new StringWriter();
            writer.write(stamp);
            writer.write('\n');
            writer.write(userName);
            writer.write('\n');
            writer.write(TrustedApplicationUtils.Constant.MAGIC);
            writer.flush();
            final byte[] encryptedData = symmetricCipher.doFinal(transcoder.getBytes(writer.toString()));
            final String encodedData = transcoder.encode(encryptedData);

            Integer version;
            
            String signature;
            
            if (urlToSign != null)
            {
                String signatureMaterial = stamp + '\n' + urlToSign;
                
                Signature algo = Signature.getInstance(SIGNATURE_ALGORITHM, PROVIDER);
                algo.initSign(privateKey);
                algo.update(signatureMaterial.getBytes(UTF8));
                signature = transcoder.encode(algo.sign());
                version = TrustedApplicationUtils.Constant.VERSION_TWO;
            }
            else
            {
                signature = null;
                version = TrustedApplicationUtils.Constant.VERSION;
            }
            
            return new DefaultEncryptedCertificate(appId, encryptedKey, encodedData, version, encryptedMagic, signature);
        }
        // ///CLOVER:OFF
        catch (NoSuchAlgorithmException e)
        {
            throw new AssertionError(e);
        }
        catch (NoSuchPaddingException e)
        {
            throw new AssertionError(e);
        }
        catch (InvalidKeyException e)
        {
            // only thrown under some JDKs, make it consistent
            throw new IllegalKeyException(e);
        }
        catch (IllegalBlockSizeException e)
        {
            throw new IllegalKeyException(e);
        }
        catch (BadPaddingException e)
        {
            throw new IllegalKeyException(e);
        }
        catch (SignatureException e)
        {
            throw new IllegalKeyException(e);
        }
        catch (UnsupportedEncodingException e)
        {
            throw new IllegalKeyException(e);
        }
        // /CLOVER:ON
    }

    interface SecretKeyFactory
    {
        SecretKey generateSecretKey();
    }

    static class BCKeyFactory implements SecretKeyFactory
    {
        public SecretKey generateSecretKey()
        {
            try
            {
                return KeyGenerator.getInstance(STREAM_CIPHER, PROVIDER).generateKey();
            }
            // /CLOVER:OFF
            catch (NoSuchAlgorithmException e)
            {
                throw new AssertionError(e);
            }
            // /CLOVER:ON
        }
    }

    static class ValidatingSecretKeyFactory implements SecretKeyFactory
    {
        private final SecretKeyFactory delegate;
        private final SecretKeyValidator validator;

        ValidatingSecretKeyFactory(SecretKeyFactory secretKeyFactory, SecretKeyValidator validator)
        {
            this.delegate = secretKeyFactory;
            this.validator = validator;
        }

        public SecretKey generateSecretKey()
        {
            SecretKey result = delegate.generateSecretKey();
            while (!validator.isValid(result))
            {
                result = delegate.generateSecretKey();
            }
            return result;
        }
    }

    /**
     * check that a secret key is valid
     */
    interface SecretKeyValidator
    {
        boolean isValid(SecretKey secretKey);
    }

    /**
     * leading zero's in the sevret key byte array lead to transmission problems
     */
    static class TransmissionValidator implements SecretKeyValidator
    {
        public boolean isValid(SecretKey secretKey)
        {
            final byte[] encoded = secretKey.getEncoded();
            if (encoded.length != 16)
            {
                return false;
            }
            if (encoded[0] == 0)
            {
                return false;
            }
            return true;
        }
    }

    static class IllegalKeyException extends IllegalArgumentException
    {
        IllegalKeyException(Exception ex)
        {
            super(ex.toString());
            this.initCause(ex);
        }
    }
}