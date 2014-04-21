package com.atlassian.crowd.util;

import com.atlassian.crowd.embedded.api.PasswordCredential;

import static java.util.regex.Pattern.compile;

/**
 * Various password helper methods.
 */
public class PasswordHelperImpl implements PasswordHelper
{
    /**
     * Validates the password meets the expected regex restriction.
     * @param regex The regex pattern.
     * @param credential The password to check.
     * @return true if the password is not blank/null and matches the given regex
     */
    public boolean validateRegex(String regex, PasswordCredential credential)
    {
        return regex != null
                && credential != null
                && credential.getCredential() != null
                && compile(regex).matcher(credential.getCredential()).find();
    }

    public String generateRandomPassword()
    {
        return SecureRandomStringUtils.getInstance().randomAlphanumericString(8);
    }
}