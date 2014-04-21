package com.atlassian.jira.config.properties;

import com.atlassian.util.concurrent.ResettableLazyReference;
import org.apache.log4j.Logger;

/**
 * This class provides access to system properties.  It should be used to access properties that can't be
 * accessed via ApplicationProperties yet because the world hasn't been brought up yet.
 *
 * These values are cached.  If you need the latest value of the system property or have changed a system propertty and
 * wish the changed value to be seen by this class you need to call {@link #resetReferences()}
 * Doing this is often is a potential performance problem {@link Boolean#getBoolean(String a)} is a blocking operation, so
 * you <b>should NOT do this</b> for every SQL statement or Web request etc.
 *
 *
 * @since v4.0
 */
public final class JiraSystemProperties
{

    private static final ResettableLazyReference<Boolean> XSRF_DETECTION_CHECK = new ResettableLazyReference<Boolean>() {
        @Override
        protected Boolean create() throws Exception
        {
            return Boolean.getBoolean(SystemPropertyKeys.XSRF_DETECTION_CHECK);
        }
    };

    private static final ResettableLazyReference<Boolean> JIRA_DEV_MODE = new ResettableLazyReference<Boolean>() {
        @Override
        protected Boolean create() throws Exception
        {
            return Boolean.getBoolean(SystemPropertyKeys.JIRA_DEV_MODE);
        }
    };

    private static final ResettableLazyReference<Boolean> ATLASSIAN_DEV_MODE = new ResettableLazyReference<Boolean>() {
        @Override
        protected Boolean create() throws Exception
        {
            return Boolean.getBoolean(SystemPropertyKeys.ATLASSIAN_DEV_MODE);
        }
    };

    private static final ResettableLazyReference<Boolean> DISABLE_BUNDLED_PLUGINS = new ResettableLazyReference<Boolean>() {
        @Override
        protected Boolean create() throws Exception
        {
            return Boolean.getBoolean(SystemPropertyKeys.DISABLE_BUNDLED_PLUGINS);
        }
    };

    private static final ResettableLazyReference<Boolean> JIRA_I18N_RELOADBUNDLES = new ResettableLazyReference<Boolean>() {
        @Override
        protected Boolean create() throws Exception
        {
            return Boolean.getBoolean(SystemPropertyKeys.JIRA_I18N_RELOADBUNDLES);
        }
    };

    private static final ResettableLazyReference<Boolean> MAIL_DECODE_PARAMETERS = new ResettableLazyReference<Boolean>() {
        @Override
        protected Boolean create() throws Exception
        {
            return getBooleanSafely(SystemPropertyKeys.MAIL_DECODE_PARAMETERS, false);
        }
    };

    private static final ResettableLazyReference<Boolean> MAIL_DECODE_FILENAME = new ResettableLazyReference<Boolean>() {
        @Override
        protected Boolean create() throws Exception
        {
            return getBooleanSafely(SystemPropertyKeys.MAIL_DECODE_FILENAME, false);
        }
    };

    private static final ResettableLazyReference<Boolean> SUPER_BATCH_DISABLED = new ResettableLazyReference<Boolean>() {
        @Override
        protected Boolean create() throws Exception
        {
            return getBooleanSafely(SystemPropertyKeys.SUPER_BATCH_DISABLED, false);
        }
    };

    private static final ResettableLazyReference<Boolean> WEBSUDO_IS_DISABLED = new ResettableLazyReference<Boolean>() {
        @Override
        protected Boolean create() throws Exception
        {
            return getBooleanSafely(SystemPropertyKeys.WEBSUDO_IS_DISABLED, false);
        }
    };

    private static final ResettableLazyReference<Boolean> SHOW_PERF_MONITOR = new ResettableLazyReference<Boolean>() {
        @Override
        protected Boolean create() throws Exception
        {
            return getBooleanSafely(SystemPropertyKeys.SHOW_PERF_MONITOR, false);
        }
    };


    private JiraSystemProperties()
    {
        //don't create this object.
    }

    public static boolean isXsrfDetectionCheckRequired()
    {
        return XSRF_DETECTION_CHECK.get();
    }

    /**
     *
     * @return true if jira is running in dev mode (meaning jira.home lock files will be ignored)
     */
    public static boolean isDevMode()
    {
        return JIRA_DEV_MODE.get() || ATLASSIAN_DEV_MODE.get();
    }

    /**
     * @return true if bundled plugins are disabled
     */
    public static boolean isBundledPluginsDisabled()
    {
        return DISABLE_BUNDLED_PLUGINS.get();
    }

    /**
     * @return true if reloading i18n bundles is true
     */
    public static boolean isI18nReloadBundles()
    {
        return JIRA_I18N_RELOADBUNDLES.get();
    }

    /**
     * @return return true if the system property has been set to decode e-mails parameters as specified by RFC-2231.
     */
    public static boolean isDecodeMailParameters()
    {
        return MAIL_DECODE_PARAMETERS.get();
    }

    /**
     * @return return true if the system property has been set to decode the "filename" from an e-mail.
     */
    public static boolean isDecodeMailFileName()
    {
        return MAIL_DECODE_FILENAME.get();
    }

    public static boolean isSuperBatchingDisabled()
    {
        return SUPER_BATCH_DISABLED.get();
    }

    public static boolean isWebSudoDisabled() {
        return WEBSUDO_IS_DISABLED.get();
    }

    public static boolean showPerformanceMonitor()
    {
        return SHOW_PERF_MONITOR.get();
    }

    private static boolean getBooleanSafely(String property, boolean defValue)
    {
        try
        {
            String value = System.getProperty(property);
            if (value == null)
            {
                return defValue;
            }
            else
            {
                return Boolean.parseBoolean(value);
            }
        }
        catch (SecurityException e)
        {
            getLogger().warn("Unable to read system property '" + property +"'. Return default value of '" + defValue + "'.", e);
            return defValue;
        }
    }

    private static Logger getLogger()
    {
        return Logger.getLogger(JiraSystemProperties.class);
    }

    public static void resetReferences()
    {
        XSRF_DETECTION_CHECK.reset();
        JIRA_DEV_MODE.reset();
        ATLASSIAN_DEV_MODE.reset();
        DISABLE_BUNDLED_PLUGINS.reset();
        JIRA_I18N_RELOADBUNDLES.reset();
        MAIL_DECODE_PARAMETERS.reset();
        MAIL_DECODE_FILENAME.reset();
        SUPER_BATCH_DISABLED.reset();
        WEBSUDO_IS_DISABLED.reset();
        SHOW_PERF_MONITOR.reset();
    }
}