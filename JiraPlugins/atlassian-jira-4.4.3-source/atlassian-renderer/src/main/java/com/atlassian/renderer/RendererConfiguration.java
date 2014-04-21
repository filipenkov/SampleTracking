package com.atlassian.renderer;

/**
 * Interface that provides the renderer with configuration information about its current environment.
 */
public interface RendererConfiguration
{
    /**
     * Returns the context path of the running web application.
     * @return the current context path.
     */
    public String getWebAppContextPath();

    /**
     * If this method returns true then external links will be generated with the
     * rel=nofollow attribute.
     * @return true to include the nofollow attribute, false to not include it.
     */
    public boolean isNofollowExternalLinks();

    /**
     * If this method returns true then camelCase links will be allowed and generated.
     * @return true to allow camel case links, false otherwise.
     */
    public boolean isAllowCamelCase();

    /**
     * Will return the character encoding for the current application.
     * @return a string representation of the current applications character encoding.
     */
    public String getCharacterEncoding();
}
