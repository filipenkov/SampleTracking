package com.atlassian.gadgets.util;

/**
 * Builds URLs to resources.
 */
public interface UrlBuilder
{
    /**
     * Returns the URL to the RPC javascript file.
     * 
     * @return the URL to the RPC javascript file
     */
    String buildRpcJsUrl();

    /**
     * Returns the URL to the image specified by the given path.
     * 
     * @param path path to the image
     * @return URL to the image specified by the given path.
     */
    String buildImageUrl(String path);
}
