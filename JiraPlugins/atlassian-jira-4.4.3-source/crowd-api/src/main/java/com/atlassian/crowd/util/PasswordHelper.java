package com.atlassian.crowd.util;

import com.atlassian.crowd.embedded.api.PasswordCredential;

public interface PasswordHelper
{
    /**
     * Validates a credential against a particular regular expression.
     *
     * @param regex regular expression.
     * @param credential password credential.
     * @return {@code true} iff the credential matches the regexp.
     */
    boolean validateRegex(String regex, PasswordCredential credential);

    /**
     * @return password consisting of a random permutation of letters and numbers, based on
     * the output of a cryptographically secure PRNG.
     */
    String generateRandomPassword();
}
