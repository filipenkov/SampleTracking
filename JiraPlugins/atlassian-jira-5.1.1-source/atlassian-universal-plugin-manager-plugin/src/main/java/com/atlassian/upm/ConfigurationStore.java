package com.atlassian.upm;

import java.io.IOException;

/**
 * Provides access to the stored configuration
 */
public interface ConfigurationStore
{
    /**
     * Gets the saved configuration value.
     *
     * @return The {@code Configuration} value. May be {@code null}
     * @throws IOException
     */
    Configuration getSavedConfiguration() throws IOException;

    /**
     * Saves the configuration.
     *
     * @param value {@code Configuration} value. {@code value} cannot be {@code null}.
     * @throws ConfigurationStoreException when there is a previously saved {@code Configuration}.
     * @throws IOException
     */
    void saveConfiguration(Configuration value) throws IOException;

    /**
     * Removes the saved configuration.
     *
     * @return The {@code Configuration} value that was removed. Null if nothing was removed.
     * @throws ConfigurationStoreException if you try to delete the "current" and "safe" configuration.
     * @throws IOException
     */
    Configuration removeSavedConfiguration() throws IOException;
}
