package com.atlassian.jira.config.util;

import java.io.File;

/**
 * Class that helps with the implementation of the {@link com.atlassian.jira.config.util.JiraHome} interface.
 *
 * @since v4.1
 */
public abstract class AbstractJiraHome implements JiraHome
{
    public abstract File getHome();

    public final File getLogDirectory()
    {
        return new File(getHome(), JiraHome.LOG);
    }

    public final File getCachesDirectory()
    {
        return new File(getHome(), JiraHome.CACHES);
    }

    public final File getExportDirectory()
    {
        return new File(getHome(), JiraHome.EXPORT);
    }

    public final File getImportDirectory()
    {
        return new File(getHome(), JiraHome.IMPORT);
    }

    public final File getImportAttachmentsDirectory()
    {
        return new File(getImportDirectory(), JiraHome.IMPORT_ATTACHMENTS);
    }

    public final File getPluginsDirectory()
    {
        return new File(getHome(), JiraHome.PLUGINS);
    }

    public final File getDataDirectory()
    {
        return new File(getHome(), JiraHome.DATA);
    }

    public final String getHomePath()
    {
        return getHome().getAbsolutePath();
    }
}
