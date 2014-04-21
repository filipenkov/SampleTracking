package com.atlassian.config.lifecycle;

public interface LifecycleItem
{
    /**
     * Called on application startup.
     *
     * @param context the application's lifecycle context
     * @throws Exception if something goes wrong during startup. No more startup items will be run, and the
     *         application will post a fatal error, shut down all LifecycleItems that have run previously, and
     *         die horribly.
     */
    void startup(LifecycleContext context) throws Exception;

    /**
     * Called on application shutdown
     *
     * @param context the application's lifecycle context
     * @throws Exception if something goes wrong during the shutdown process. The remaining shutdown items
     *         will still be run, but the lifecycle manager will log the error.
     */
    void shutdown(LifecycleContext context) throws Exception;
}
