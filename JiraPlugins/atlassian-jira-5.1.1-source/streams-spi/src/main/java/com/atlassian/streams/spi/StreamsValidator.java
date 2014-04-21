package com.atlassian.streams.spi;

/**
 * Provides validation to check if the arguments provided to filter a stream by are valid.
 *
 * @since v3.0
 */
public interface StreamsValidator
{
    /**
     * Checks if the key provided by the user is valid.  Ideally this should be key provided by the
     * {@link StreamsKeyProvider#getKeys()} method.
     *
     * @param key The key to validate
     * @return true if the key is valid, false otherwise.
     */
    boolean isValidKey(final String key);
}
