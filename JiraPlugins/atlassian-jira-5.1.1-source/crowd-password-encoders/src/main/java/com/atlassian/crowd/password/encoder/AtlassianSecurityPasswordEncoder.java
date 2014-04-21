package com.atlassian.crowd.password.encoder;

import com.atlassian.crowd.exception.PasswordEncoderException;
import com.atlassian.crowd.password.factory.PasswordEncoderFactory;
import com.atlassian.security.password.DefaultPasswordEncoder;

/**
 * This class is responsible for encoding and validating passwords using Atlassian Password Encoder from Atlassian
 * Security project, while also validating passwords encoded in Atlassian SHA1 format in order to be backwards
 * compatible.
 */
public class AtlassianSecurityPasswordEncoder implements InternalPasswordEncoder, UpgradeablePasswordEncoder
{

    private final com.atlassian.security.password.PasswordEncoder defaultPasswordEncoder;
    private final PasswordEncoder oldPasswordEncoder;

    public AtlassianSecurityPasswordEncoder()
    {
        defaultPasswordEncoder = DefaultPasswordEncoder.getDefaultInstance();
        oldPasswordEncoder = new AtlassianSHA1PasswordEncoder();
    }

    AtlassianSecurityPasswordEncoder(com.atlassian.security.password.PasswordEncoder defaultPasswordEncoder, PasswordEncoder oldPasswordEncoder)
    {
        this.defaultPasswordEncoder = defaultPasswordEncoder;
        this.oldPasswordEncoder = oldPasswordEncoder;
    }

    /**
     * Encodes the provided rawPass using a Atlassian Password Encoder from Atlassian Security project.
     *
     * @param rawPass the password to encode
     * @param salt not used. A <code>null</code> value is legal.
     * @return encoded password
     * @throws PasswordEncoderException
     *
     * @see com.atlassian.security.password.DefaultPasswordEncoder
     */
    public String encodePassword(String rawPass, Object salt) throws PasswordEncoderException
    {
        try
        {
            return defaultPasswordEncoder.encodePassword(rawPass);
        }
        catch (IllegalArgumentException e)
        {
            throw new PasswordEncoderException("Password could not be encoded.", e);
        }
    }

    /**
     *  Returns true if the rawPass is the same password that was used to create encPass.
     * 
     * @param encPass a pre-encoded password in either Atlassian SHA1 form or the form provided by {@link DefaultPasswordEncoder#getDefaultInstance()} from atlassian-password-encoder.
     * @param rawPass a raw password to encode and compare against the pre-encoded password
     * @param salt not used. A <code>null</code> value is legal.
     * @return true if the rawPass is the same password that was used to create encPass
     */
    public boolean isPasswordValid(String encPass, String rawPass, Object salt)
    {
        if (defaultPasswordEncoder.canDecodePassword(encPass))
        {
            try
            {
                return defaultPasswordEncoder.isValidPassword(rawPass, encPass);
            }
            catch (IllegalArgumentException e)
            {
                return false;
            }
        }
        else
        {
            return oldPasswordEncoder.isPasswordValid(encPass, rawPass, salt);
        }
    }

    /**
     * Returns true if the the password is encoded using an older scheme, and if it should be re-encoded and updated.
     *
     * @param encPass a pre-encoded password
     * @return true if the the password is encoded using an older scheme, and if it should be re-encoded and updated.
     */
    public boolean isUpgradeRequired(String encPass)
    {
        return !defaultPasswordEncoder.canDecodePassword(encPass);
    }

    public String getKey()
    {
        return PasswordEncoderFactory.ATLASSIAN_SECURITY_ENCODER;
    }
}
