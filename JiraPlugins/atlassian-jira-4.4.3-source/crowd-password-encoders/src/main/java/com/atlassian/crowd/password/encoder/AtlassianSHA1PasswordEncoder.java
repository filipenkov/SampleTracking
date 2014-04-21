package com.atlassian.crowd.password.encoder;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.apache.commons.codec.binary.Base64.encodeBase64;

/**
 * The Atlassian implementation of the SHA-1 password encoder, based on the OSUser implementation.
 * It uses the Bouncy Castle SHA-512 digest, followed by the commons-codec base64 encoding.
 * <p/>
 * Note: the password String is converted to bytes using the <em>platform encoding</em>, to preserve
 * the same behaviour as OSUser.
 */
public class AtlassianSHA1PasswordEncoder implements InternalPasswordEncoder
{
    public static final String ATLASSIAN_SHA1_KEY = "atlassian-sha1";

    /**
     * This method will handle the hashing of the passed in <code>password</code> param
     *
     * @param password the password to encrypt
     * @param salt     can be null, and is not currently used by the underlying implementation
     * @return java.util.String the hashed password
     */
    public String encodePassword(String password, Object salt)
    {
        byte[] bytes = password.getBytes(); // for compatibility with OSUser - use platform encoding
        byte[] hash;
        try
        {
            hash = MessageDigest.getInstance("SHA-512").digest(bytes);
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException(e);
        }
        return new String(encodeBase64(hash)); // for compatibility with OSUser - use platform encoding
    }

    /**
     * @see org.springframework.security.providers.encoding.PasswordEncoder#isPasswordValid(String, String, Object)
     */
    public boolean isPasswordValid(String encPass, String rawPass, Object salt)
    {
        boolean valid = false;
        if (encPass != null && rawPass != null)
        {
            valid = encPass.equals(encodePassword(rawPass, salt));
        }
        return valid;
    }

    public String getKey()
    {
        return ATLASSIAN_SHA1_KEY;
    }
}
