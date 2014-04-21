package com.atlassian.plugins.rest.common.multipart;

/**
 * Configuration for multipart config
 *
 * @since 2.4
 */
public interface MultipartConfig
{
    /**
     * Get the max file size
     *
     * @return The max file size
     */
    public long getMaxFileSize();

    /**
     * Get the max size
     *
     * @return The max size
     */
    public long getMaxSize();
}
