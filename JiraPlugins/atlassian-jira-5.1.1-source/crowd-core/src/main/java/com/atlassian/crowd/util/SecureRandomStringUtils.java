package com.atlassian.crowd.util;

import com.atlassian.security.random.DefaultSecureRandomService;
import com.atlassian.security.random.SecureRandomService;

/**
 * Generates random {@link String}s by selecting characters from
 * an alphabet using a cryptographically secure PRNG.
 *
 * Methods on this class are thread-safe.
 */
public class SecureRandomStringUtils
{
    private static SecureRandomStringUtils INSTANCE = new SecureRandomStringUtils(DefaultSecureRandomService.getInstance());

    private static final char[] ALPHANUMERICS = new char[]
    {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',

        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
        'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
        'U', 'V', 'W', 'X', 'Y', 'Z',

        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
        'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
        'u', 'v', 'w', 'x', 'y', 'z',
    };

    private final SecureRandomService random;

    SecureRandomStringUtils(SecureRandomService random)
    {
        this.random = random;
    }

    /**
     * @return shared instance of {@link SecureRandomStringUtils}.
     */
    public static SecureRandomStringUtils getInstance()
    {
        return INSTANCE;
    }

    /**
     * Generates a random {@link String} by randomly selecting characters from
     * the provided {@code alphabet} until the desired {@code length} is reached.
     * <p/>
     * The method for selecting a random character is specified by the implementation.
     * <p/>
     * Unicode surrogate-pairing is not supported. All the provided characters should be
     * in the Unicode BMP, if you wish to avoid generating Strings with invalid UTF-16
     * surrogates.
     *
     * @param length   desired length of random {@link String}. Must be positive.
     * @param alphabet collection of characters to select from. Must contain at least one character.
     * @return {@link String} of length {@code length}, composed of characters from the supplied {@code alphabet}.
     */
    public String randomString(int length, char[] alphabet)
    {
        if (length < 0)
        {
            throw new IllegalArgumentException("Length must be positive");
        }

        if (alphabet.length <= 0)
        {
            throw new IllegalArgumentException("Alphabet must contain at least one character");
        }

        char[] buffer = new char[length];

        for (int i = 0; i < length; i++)
        {
            buffer[i] = alphabet[random.nextInt(alphabet.length)];
        }

        return new String(buffer);
    }

    /**
     * Generates a random {@link String} by randomly selecting characters from the the
     * alphabet of characters in the range 0-9, A-Z and a-z.
     * <p/>
     * The method for selecting a random character is specified by the implementation.
     *
     * @param length desired length of random {@link String}.
     * @return alphanumeric {@link String} of length {@code length}.
     * @see #randomString(int, char[])
     */
    public String  randomAlphanumericString(int length)
    {
        return randomString(length, ALPHANUMERICS);
    }
}
