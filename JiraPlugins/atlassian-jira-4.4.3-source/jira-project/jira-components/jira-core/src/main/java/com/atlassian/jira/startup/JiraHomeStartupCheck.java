package com.atlassian.jira.startup;

import com.atlassian.jira.config.properties.JiraSystemProperties;
import com.atlassian.jira.config.util.AttachmentPathManager;
import com.atlassian.jira.config.util.IndexPathManager;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.plugin.PluginPath;
import com.atlassian.jira.service.services.file.FileService;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.web.ServletContextProvider;
import com.atlassian.jira.web.util.HelpUtil;
import com.atlassian.multitenant.MultiTenantComponentMap;
import com.atlassian.multitenant.MultiTenantContext;
import com.atlassian.multitenant.MultiTenantCreator;
import com.atlassian.multitenant.Tenant;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 * This StartupCheck will check that there is a valid jira.home configured that we can get an exclusive lock on.
 * <p/>
 * <em>Note: this has the side effect that the jira.home directory is created, if required, and "locked".</em> These
 * side-effects are REQUIRED in order to return valid results.
 *
 * @since v4.0
 */
public class JiraHomeStartupCheck implements StartupCheck
{
    private static final Logger log = Logger.getLogger(JiraHomeStartupCheck.class);
    private static final JiraHomeStartupCheck SYSTEM_TENANT_CHECK = new JiraHomeStartupCheck(new SystemTenantJiraHomeLocator());

    private static MultiTenantComponentMap<JiraHomeStartupCheck> INSTANCE_MAP;

    public static synchronized JiraHomeStartupCheck getInstance()
    {
        if (!MultiTenantContext.isEnabled())
        {
            // We use this check if called before MultiTenancy has been enabled. This will be the case with log4j
            // and the JiraHomeAppender.
            return SYSTEM_TENANT_CHECK;
        }
        else if (INSTANCE_MAP == null)
        {
            INSTANCE_MAP = MultiTenantContext.getFactory().createComponentMapBuilder(new MultiTenantCreator<JiraHomeStartupCheck>()
            {
                @Override
                public JiraHomeStartupCheck create(Tenant tenant)
                {
                    if (tenant == MultiTenantContext.getSystemTenant())
                    {
                        return SYSTEM_TENANT_CHECK;
                    }
                    else
                    {
                        return new JiraHomeStartupCheck(new MultiTenantJiraHomeLocator());
                    }
                }
            }).setNoTenantStrategy(MultiTenantComponentMap.NoTenantStrategy.SYSTEM)
                    .construct();
        }
        return INSTANCE_MAP.get();
    }

    private final JiraHomePathLocator locator;
    private final JiraHomeLockAcquirer lockAcquirer;
    private volatile String faultDescription;
    private volatile String faultDescriptionHtml;
    private volatile File jiraHomeDirectory;
    private volatile boolean initalised = false;

    public JiraHomeStartupCheck(final JiraHomePathLocator locator)
    {
        this.locator = locator;
        this.lockAcquirer = new JiraHomeLockAcquirer();
    }

    public String getName()
    {
        return "Jira.Home Startup Check";
    }

    public boolean isOk()
    {
        if (!initalised)
        {
            try
            {
                // Get configured jira.home
                final String jiraHome = getConfiguredJiraHome();
                // Validate the jira.home, and create the directory if required
                // Note that we only save jiraHomeDirectory if everything is valid.
                jiraHomeDirectory = validateJiraHome(jiraHome);
                // The Jira Home is now available for the JiraHomeService to pick up once Pico starts up.
            }
            catch (final JiraHomeException ex)
            {
                faultDescriptionHtml = ex.getHtmlMessage();
                faultDescription = ex.getMessage();
            }
            finally
            {
                initalised = true;
            }
        }
        return jiraHomeDirectory != null;
    }

    public boolean isInitialised()
    {
        return initalised;
    }

    private String getConfiguredJiraHome() throws JiraHomeException
    {
        final String home = locator.getJiraHome();
        if (StringUtils.isBlank(home))
        {
            // No jira.home is configured by any method.
            final HelpUtil.HelpPath helpPath = HelpUtil.getInstance().getHelpPath("jirahome");
            final String s = "No jira.home is configured.\nSee %s for instructions on setting jira.home";
            final String plainText = String.format(s, helpPath.getUrl());

            final String href = String.format("<a href=\"%s\">%s</a>", helpPath.getUrl(), TextUtils.htmlEncode(helpPath.getTitle()));
            final String htmlText = String.format(TextUtils.htmlEncode(s), href);
            throw new JiraHomeException(plainText, htmlText);
        }
        return home;
    }

    /**
     * Traverses up the specified directory parents' searching for a specified directory
     *
     * @param directoryToTraverse the one to traverse
     * @param directoryToFind the one to find
     * @return if the find directory is in the traverse directory
     */
    private boolean findDirectory(File directoryToTraverse, File directoryToFind)
    {
        File currentDirectory = directoryToTraverse;
        while (currentDirectory != null)
        {
            if (currentDirectory.equals(directoryToFind))
            {
                return true;
            }
            currentDirectory = currentDirectory.getParentFile();
        }
        return false;
    }

    private File validateJiraHome(final String jiraHome) throws JiraHomeException
    {
        final File proposedJiraHome = new File(jiraHome);

        // try to show useful error messages if the user puts a single-backslash in their jira-application.properties
        // for jira.home. java.util.Properties does a lot of magic parsing so we don't have much to work with.
        if (!proposedJiraHome.isAbsolute())
        {
            if (JiraSystemProperties.isDevMode())
            {
                // NOTE: this override is only here for development, this should not be used in production.
                log.warn("jira.home is a relative path, but jira.dev.mode is set to true so we will allow this.");
            }
            else
            {
                final HelpUtil.HelpPath helpPath = HelpUtil.getInstance().getHelpPath("jirahome");
                final String s = "jira.home must be an absolute path.\nSee %s for instructions on setting jira.home";
                final StringBuffer plainText = new StringBuffer(String.format(s, helpPath.getUrl()));

                final String href = String.format("<a href=\"%s\">%s</a>", helpPath.getUrl(), TextUtils.htmlEncode(helpPath.getTitle()));
                final StringBuffer htmlText = new StringBuffer(String.format(TextUtils.htmlEncode(s), href));

                plainText.append("\nYour current jira.home is:\n");
                plainText.append(jiraHome);

                final boolean deadlyBackslash = System.getProperty("file.separator").equals("\\");
                if (deadlyBackslash)
                {
                    plainText.append("\n");
                    plainText.append("It looks like you are on Windows. This is usually caused by incorrect use of backslashes inside of jira-application.properties.\n");
                    plainText.append("Use forward slashes (/) instead.");
                }

                throw new JiraHomeException(plainText.toString(), htmlText.toString());
            }
        }

        ServletContext servletContext = ServletContextProvider.getServletContext();
        if (servletContext == null)
        {
            log.error("No ServletContext exists - cannot check if jira.home is on the servlet path.");
        }
        else
        {
            File webappServletPath = new File(servletContext.getRealPath("/"));

            if (proposedJiraHome.equals(webappServletPath))
            {
                throw new JiraHomeException("Configured jira.home '" + proposedJiraHome.getAbsolutePath() +
                        "' must not be the same as the webapp servlet path '" + webappServletPath.getAbsolutePath() + "'");
            }
            if (findDirectory(webappServletPath, proposedJiraHome))
            {
                throw new JiraHomeException("Configured jira.home '" + proposedJiraHome.getAbsolutePath() +
                        "' must not be a parent directory of the webapp servlet path '" + webappServletPath.getAbsolutePath() + "'");
            }
            if (findDirectory(proposedJiraHome, webappServletPath))
            {
                throw new JiraHomeException("Configured jira.home '" + proposedJiraHome.getAbsolutePath() +
                        "' must not be a directory within the webapp servlet path '" + webappServletPath.getAbsolutePath() + "'");
            }
        }

        // Check if the jiraHome actually exists
        if (proposedJiraHome.exists())
        {
            // Check that it is a directory
            if (!proposedJiraHome.isDirectory())
            {
                final String message = "Configured jira.home '" + proposedJiraHome.getAbsolutePath() + "' is not a directory.";
                throw new JiraHomeException(message);
            }
        }
        else
        {
            log.info("Configured jira.home '" + proposedJiraHome.getAbsolutePath() + "' does not exist. We will create it.");
            // Attempt to create the directory
            try
            {
                if (!proposedJiraHome.mkdirs())
                {
                    final String jiraHomeURL = HelpUtil.getInstance().getHelpPath("jirahome").getUrl();
                    throw new JiraHomeException("Could not create jira.home directory '" + proposedJiraHome.getAbsolutePath() + "'. Please see " + jiraHomeURL + " for more information on how to set up your JIRA home directory.");
                }
            }
            catch (final SecurityException ex)
            {
                final String jiraHomeURL = HelpUtil.getInstance().getHelpPath("jirahome").getUrl();
                throw new JiraHomeException("Could not create jira.home directory '" + proposedJiraHome.getAbsolutePath() + "'. A Security Exception occured. Please see " + jiraHomeURL + " for more information on how to set up your JIRA home directory.");
            }
        }

        // JRA-18645 ensure that all of the subdirectories of jiraHome also exist
        try
        {
            createAllHomeDirectories(proposedJiraHome);
        }
        catch (final SecurityException ex)
        {
            final String jiraHomeURL = HelpUtil.getInstance().getHelpPath("jirahome").getUrl();
            throw new JiraHomeException("Could not create jira.home directory '" + proposedJiraHome.getAbsolutePath() + "'. A Security Exception occured. Please see " + jiraHomeURL + " for more information on how to set up your JIRA home directory.");
        }

        // attempt to lock the home directory
        lockJiraHome(proposedJiraHome);

        // All tests passed
        log.info("The jira.home directory '" + proposedJiraHome.getAbsolutePath() + "' is validated and locked for exclusive use by this instance.");
        return proposedJiraHome;
    }

    void createAllHomeDirectories(final File proposedJiraHome) throws JiraHomeException
    {
        Set<String> subdirs = CollectionBuilder.<String>newBuilder()
                .add(IndexPathManager.INDEXES_DIR)
                .add(AttachmentPathManager.ATTACHMENTS_DIR)
                .add(PluginPath.PLUGINS_DIRECTORY)
                .add(PluginPath.BUNDLED_SUBDIR)
                .add(PluginPath.INSTALLED_PLUGINS_SUBDIR)
                .add(PluginPath.OSGI_SUBDIR)
                .add(FileService.MAIL_DIR)
                .addAll(JiraHome.subdirs)
                .asMutableSortedSet();

        for (String subdir : subdirs)
        {
            try
            {
                final File dir = new File(proposedJiraHome, subdir);
                if (!dir.exists())
                {
                    if (!dir.mkdirs())
                    {
                        final String s = String.format("Could not create necessary subdirectory '%1$s' of jira.home.", subdir);
                        throw new JiraHomeException(s);
                    }
                }
            }
            catch (JiraHomeException homeException)
            {
                throw homeException;
            }
            catch (Exception e)
            {
                final String s = String.format("Could not create necessary subdirectory '%1$s' of jira.home.", subdir);
                throw new JiraHomeException(s + "\n" + e.getMessage());
            }
        }
    }

    private void lockJiraHome(final File proposedJiraHome) throws JiraHomeException
    {
        Assertions.notNull("You should not be in this method if you have a null lockAcquirer", lockAcquirer);

        String jiraHomePath = null;
        try
        {
            jiraHomePath = proposedJiraHome.getCanonicalPath();
        }
        catch (IOException e)
        {
            log.debug("Couldn't obtain canonical path for jira.home", e);
            jiraHomePath = proposedJiraHome.getAbsolutePath();
        }

        // Look for Lock file
        // Try to lock the directory for ourselves
        String failMsg = "Unable to create and acquire lock file for jira.home directory '" + jiraHomePath + "'.";
        try
        {
            JiraHomeLockAcquirer.LockResult result = lockAcquirer.acquire(proposedJiraHome);
            if (result != JiraHomeLockAcquirer.LockResult.OK)
            {
                if (result == JiraHomeLockAcquirer.LockResult.HELD_BY_OTHERS)
                {
                    final String jiraHomeURL = HelpUtil.getInstance().getHelpPath("jirahomelocked").getUrl();
                    final String href = String.format("<a href=\"%s\">the JIRA documentation</a>", jiraHomeURL);
                    final String s = "The jira.home directory '%s' is already locked. Please see %s for more information on locked jira.home directories.";
                    final String htmlText = String.format(TextUtils.htmlEncode(s), TextUtils.htmlEncode(jiraHomePath), href);
                    final String plainText = "The jira.home directory '" + jiraHomePath + "' is already locked. Please see " + jiraHomeURL + " for more information on locked jira.home directories.";
                    throw new JiraHomeException(plainText, htmlText);
                }
                else
                {
                    // Creation failed.
                    throw new JiraHomeException(failMsg);
                }
            }
        }
        catch (final JiraHomeException ex)
        {
            throw ex;
        }
        catch (final Exception ex)
        {
            // We log here to get the stack trace - may help the JIRA admin or support. Note that there will be a fatal log message later as well.
            log.error(failMsg + " " + ex.getMessage(), ex);
            throw new JiraHomeException(failMsg);
        }
    }

    @Override
    public void stop()
    {
        lockAcquirer.release();
    }

    public String getFaultDescription()
    {
        return faultDescription;
    }

    public String getHTMLFaultDescription()
    {
        return faultDescriptionHtml;
    }

    public File getJiraHomeDirectory()
    {
        return jiraHomeDirectory;
    }

    @Override
    public String toString()
    {
        return getName();
    }
}
