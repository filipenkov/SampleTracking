package com.atlassian.config.lifecycle;

import javax.servlet.ServletContext;

/**
 * Manages operations that must be performed on startup and shutdown of the application. These operations will
 * typically be implemented as plugin modules.
 *
 * <b>Important:</b> There is no real way the application can guarantee that any of the shutdown tasks will be called
 * in the event of an unfriendly server shutdown. They are mostly useful for cleanup in situations (like an
 * appserver-triggered reload) where it can be assumed that the application will have the time to run the shutdown
 * cleanly.
 */
public interface LifecycleManager
{
    /**
     * Perform all the startup tasks for the application.
     *
     * @param servletContext the web application servlet context
     */
    void startUp(ServletContext servletContext);

    /**
     * Perform all the shutdown tasks for the application.
     *
     * @param servletContext the web application servlet context
     */
    void shutDown(ServletContext servletContext);
}
