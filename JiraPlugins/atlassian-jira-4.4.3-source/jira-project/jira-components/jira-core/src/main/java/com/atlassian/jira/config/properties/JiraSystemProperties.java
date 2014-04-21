package com.atlassian.jira.config.properties;

import org.apache.log4j.Logger;

/**
 * This class provides access to system properties.  It should be used to access properties that can't be
 * accessed via ApplicationProperties yet because the world hasn't been brought up yet.
 *
 * @since v4.0
 */
public final class JiraSystemProperties
{

    private JiraSystemProperties()
    {
        //don't create this object.
    }

    public static boolean isXsrfDetectionCheckRequired()
    {
        return Boolean.getBoolean(SystemPropertyKeys.XSRF_DETECTION_CHECK);
    }

    /**
     *
     * @return true if jira is running in dev mode (meaning jira.home lock files will be ignored)
     */
    public static boolean isDevMode()
    {
        return Boolean.getBoolean(SystemPropertyKeys.JIRA_DEV_MODE) || Boolean.getBoolean(SystemPropertyKeys.ATLASSIAN_DEV_MODE);
    }

    /**
     * @return true if bundled plugins are disabled
     */
    public static boolean isBundledPluginsDisabled()
    {
        return Boolean.getBoolean(SystemPropertyKeys.DISABLE_BUNDLED_PLUGINS);
    }

    /**
     * @return true if reloading i18n bundles is true
     */
    public static boolean isI18nReloadBundles()
    {
        return Boolean.getBoolean(SystemPropertyKeys.JIRA_I18N_RELOADBUNDLES);
    }

    /**
     * @return return true if the system property has been set to decode e-mails parameters as specified by RFC-2231.
     */
    public static boolean isDecodeMailParameters()
    {
        return getBooleanSafely(SystemPropertyKeys.MAIL_DECODE_PARAMETERS, false);
    }

    /**
     * @return return true if the system property has been set to decode the "filename" from an e-mail.
     */
    public static boolean isDecodeMailFileName()
    {
        return getBooleanSafely(SystemPropertyKeys.MAIL_DECODE_FILENAME, false);
    }

    public static boolean isSuperBatchingDisabled()
    {
        return getBooleanSafely(SystemPropertyKeys.SUPER_BATCH_DISABLED, false);
    }

    public static boolean isWebSudoDisabled() {
        return getBooleanSafely(SystemPropertyKeys.WEBSUDO_IS_DISABLED, false);
    }

    public static boolean showPerformanceMonitor()
    {
        return getBooleanSafely(SystemPropertyKeys.SHOW_PERF_MONITOR, false);
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
}