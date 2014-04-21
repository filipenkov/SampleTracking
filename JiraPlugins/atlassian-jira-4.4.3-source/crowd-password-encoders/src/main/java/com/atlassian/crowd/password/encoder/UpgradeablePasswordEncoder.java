package com.atlassian.crowd.password.encoder;

/**
 * Password encoder which provides a way to ask if the password should be re-encoded as it does not match the encoding
 * format of the most secure underlying encoder.
 */
public interface UpgradeablePasswordEncoder extends PasswordEncoder
{
    /**
     * Return true if the password should be re-encoded as it does not match the encoding format of the most secure
     * underlying encoder.
     *
     * @param encPass a pre-encoded password
     * @return true if the password should be re-encoded.
     */
    boolean isUpgradeRequired(String encPass);
}
