package com.atlassian.jira.plugin.ext.bamboo.upgrade.legacy.manager;

import java.security.InvalidKeyException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

/**
 * Encrypts legacy Bamboo server passwords. This should only be used by the upgrade tasks.
 */
public class BambooStringEncrypter
{
    private static final Logger log = Logger.getLogger(BambooStringEncrypter.class);

    private static final String DEFAULT_ENCRYPTION_KEY = "Bamboo Password Encryption for Jira Plugin - Bamboo Server Authentication";
    private static final String UNICODE_FORMAT = "UTF8";
    private static final String DESEDE_ENCRYPTION_SCHEME = "DESede";

    protected Cipher myCipher;
    protected SecretKeyFactory myKeyFactory;
    protected DESedeKeySpec myKeySpec;

    public BambooStringEncrypter() throws EncryptionException
    {
        try
        {
            myKeySpec = new DESedeKeySpec(DEFAULT_ENCRYPTION_KEY.getBytes(UNICODE_FORMAT));
            myKeyFactory = SecretKeyFactory.getInstance(DESEDE_ENCRYPTION_SCHEME);
            myCipher = Cipher.getInstance(DESEDE_ENCRYPTION_SCHEME);
        }
        catch (Exception e)
        {
            log.fatal("This shouldn't really occur", e);
            throw new EncryptionException("Failed to initialise", e);
        }
    }

    public String encrypt(String stringToEncrypt) throws EncryptionException
    {
        if (stringToEncrypt == null || stringToEncrypt.length() == 0)
        {
            return "";
        }

        try
        {
            initilizeCipher(Cipher.ENCRYPT_MODE);
            return new String(Base64.encodeBase64(myCipher.doFinal(stringToEncrypt.getBytes(UNICODE_FORMAT))));
        }
        catch (Exception e)
        {
            throw new EncryptionException("Failed to encrypt.", e);
        }
    }

    public String decrypt(String stringToDecrypt) throws EncryptionException
    {
        if (stringToDecrypt == null || stringToDecrypt.length() == 0)
        {
            return "";
        }

        try
        {
            initilizeCipher(Cipher.DECRYPT_MODE);
            return new String(myCipher.doFinal(Base64.decodeBase64(stringToDecrypt.getBytes())));
        }
        catch (Exception e)
        {
            throw new EncryptionException("Failed to decrypt.", e);
        }
    }

    private void initilizeCipher(int mode) throws InvalidKeySpecException, InvalidKeyException
    {
        SecretKey secretKey = myKeyFactory.generateSecret(myKeySpec);
        myCipher.init(mode, secretKey);
    }
}
