package com.atlassian.security.random;

import org.apache.commons.codec.binary.Hex;

/**
 * Implementation of {@link SecureTokenGenerator} which uses the {@link DefaultSecureRandomService}
 * for random byte generation.
 */
public final class DefaultSecureTokenGenerator implements SecureTokenGenerator
{
    private static final SecureTokenGenerator INSTANCE = new DefaultSecureTokenGenerator(DefaultSecureRandomService.getInstance());

    // see https://atlaseye.atlassian.com/cru/CR-ATL-204 for a discussion on why 160 bits was chosen
    private static final int TOKEN_LENGTH_BYTES = 20;

    private final SecureRandomService randomService;

    DefaultSecureTokenGenerator(SecureRandomService randomService)
    {
        this.randomService = randomService;
    }

    /**
     * @return shared {@link DefaultSecureTokenGenerator} instance.
     */
    public static SecureTokenGenerator getInstance()
    {
        return INSTANCE;
    }

    /**
     * Generates a hexadecimal {@link String} representation of 20 random bytes,
     * produced by {@link DefaultSecureRandomService#nextBytes(byte[])}.
     *
     * The generated {@link String} is 40 characters in length and is composed of
     * characters in the range '0'-'9' and 'a'-'f'.
     *
     * The length (20 bytes / 160 bits) was selected as it is the same as the size
     * of the internal state of the SHA1PRNG.
     *
     * @return returns a hexadecimal encoded representation of 20 random bytes.
     */
    public String generateToken()
    {
        byte[] bytes = new byte[TOKEN_LENGTH_BYTES];

        randomService.nextBytes(bytes);

        // can replace this with Hex.encodeHexString(bytes) when we upgrade to commons-codec 1.4.
        return new String(Hex.encodeHex(bytes));
    }
}
