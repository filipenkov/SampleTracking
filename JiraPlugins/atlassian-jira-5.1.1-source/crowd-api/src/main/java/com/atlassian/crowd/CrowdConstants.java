package com.atlassian.crowd;

/**
 * Crowd server constants.
 */
public class CrowdConstants
{
    /**
     * Character encoding.
     */
    public static final String DEFAULT_CHARACTER_ENCODING = "UTF-8";

    /**
     * Default content type for web/html documents.
     */
    public static final String DEFAULT_CONTENT_TYPE = "text/html; charset="+DEFAULT_CHARACTER_ENCODING;

    public static class CrowdHome
    {
        public static final String PLUGIN_DATA_LOCATION = "plugin-data";

        public static final String LOG_FILE_LOCATION = "logs";

        public static final String BACKUPS_LOCATION = "backups";

        private CrowdHome()
        {
        }
    }


    private CrowdConstants()
    {
        // stop this class being instantiated
    }
}