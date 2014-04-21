package com.atlassian.crowd.password.encoder;

import com.atlassian.crowd.exception.PasswordEncoderException;
import com.atlassian.crowd.manager.property.PropertyManager;
import org.apache.log4j.Logger;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;

/**
 * This encoder implements the DES algorithm
 */
public class DESPasswordEncoder implements LdapPasswordEncoder, InternalPasswordEncoder
{
    private PropertyManager propertyManager;

    public static final String PASSWORD_ENCRYPTION_ALGORITHM = "DES";

    private static final Logger logger = Logger.getLogger(DESPasswordEncoder.class);

    public String encodePassword(String rawPass, Object salt)
    {
        try
        {
            Cipher ecipher = Cipher.getInstance(PASSWORD_ENCRYPTION_ALGORITHM);

            ecipher.init(Cipher.ENCRYPT_MODE, propertyManager.getDesEncryptionKey());

            byte[] utf8 = rawPass.getBytes("UTF-8");

            byte[] enc = ecipher.doFinal(utf8);

            return new BASE64Encoder().encode(enc);

        }
        catch (Exception e)
        {
            logger.error("Failed to encrypt password to DES", e);
            throw new PasswordEncoderException("Failed to encrpyt password to DES", e);
        }
    }

    public boolean isPasswordValid(String encPass, String rawPass, Object salt)
    {
        boolean valid = false;
        if (encPass != null)
        {
            valid = encPass.equals(encodePassword(rawPass, salt));
        }
        return valid;
    }

    public String getKey()
    {
        return "des";
    }

    ///CLOVER:OFF
    public void setPropertyManager(PropertyManager propertyManager)
    {
        this.propertyManager = propertyManager;
    }
}