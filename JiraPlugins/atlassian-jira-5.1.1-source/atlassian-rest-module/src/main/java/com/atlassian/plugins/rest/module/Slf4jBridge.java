package com.atlassian.plugins.rest.module;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 * Utility class for safely installing and uninstalling the JUL->SLF4J bridge.
 *
 * @see SLF4JBridgeHandler
 */
public final class Slf4jBridge
{
    /**
     * Logger for this class.
     */
    private static final Logger log = LoggerFactory.getLogger(Slf4jBridge.class);

    /**
     * Returns a Helper instance that can be used to install/uninstall the JUL->SLF4J bridge. This method tries to
     * reflectively load the class <code>org.slf4j.bridge.SLF4JBridgeHandler</code> before using it, so it is safe to
     * use in an OSGi environment where the class may or may not be present.
     * <p/>
     * If the SLF4J bridge is found, then the helper can be used to install and uninstall the bridge in the thread
     * context class loader (TCCL). If the bridge is not found then the helper's operations are no-ops.
     *
     * @return a Helper instance that can be used to install/uninstall the JUL->SLF4J bridge.
     */
    public static Helper createHelper()
    {
        try
        {
            Class.forName("org.slf4j.bridge.SLF4JBridgeHandler");

            return new BridgePresentHelper();
        }
        catch (ClassNotFoundException e)
        {
            return new BridgeMissingHelper();
        }
    }

    public interface Helper
    {
        /**
         * Installs the bridge.
         */
        void install();

        /**
         * Un-installs the bridge.
         */
        void uninstall();

    }

    private static class BridgeMissingHelper implements Helper
    {
        public void install()
        {
            log.debug("Skipping installation of SLF4JBridgeHandler for {}. Have you provided jcl-over-slf4j.jar?", Thread.currentThread().getContextClassLoader());
        }

        public void uninstall()
        {
        }
    }

    private static class BridgePresentHelper implements Helper
    {
        public void install()
        {
            log.debug("Installing SLF4JBridgeHandler for {}.", Thread.currentThread().getContextClassLoader());
            SLF4JBridgeHandler.install();
        }

        public void uninstall()
        {
            SLF4JBridgeHandler.uninstall();
            log.debug("Uninstalled SLF4JBridgeHandler for {}.", Thread.currentThread().getContextClassLoader());
        }
    }
}
