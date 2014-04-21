package it.com.atlassian.jira.plugin.ext.bamboo;

import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.pageobjects.project.versions.Version;
import com.atlassian.jira.pageobjects.project.versions.VersionPageTab;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import it.com.atlassian.jira.plugin.ext.bamboo.pageobjects.BrowsableVersion;
import it.com.atlassian.jira.plugin.ext.bamboo.pageobjects.BrowseProjectPage;
import it.com.atlassian.jira.plugin.ext.bamboo.pageobjects.BrowseVersionSummaryPage;
import it.com.atlassian.jira.plugin.ext.bamboo.pageobjects.BrowseVersionsPanel;
import org.apache.log4j.Logger;
import org.junit.Assert;

import javax.annotation.Nullable;
import java.util.NoSuchElementException;

public class WebDriverVersionUtils
{

    @SuppressWarnings("UnusedDeclaration")
    private static final Logger log = Logger.getLogger(WebDriverVersionUtils.class);
    // ------------------------------------------------------------------------------------------------------- Constants
    // ------------------------------------------------------------------------------------------------- Type Properties
    // ---------------------------------------------------------------------------------------------------- Dependencies
    // ---------------------------------------------------------------------------------------------------- Constructors
    // ----------------------------------------------------------------------------------------------- Interface Methods
    // -------------------------------------------------------------------------------------------------- Action Methods
    // -------------------------------------------------------------------------------------------------- Public Methods

    public static BrowseVersionSummaryPage getBrowseVersionSummaryPageForTestProject(JiraTestedProduct jira, final String projectKey, final String versionName)
    {
        BrowseProjectPage browseProjectPage = jira.goTo(BrowseProjectPage.class, projectKey);
        BrowseVersionsPanel browseVersionsPanel = browseProjectPage.getVersionsPanel();

        BrowsableVersion version = null;
        try
        {
            version = Iterables.find(browseVersionsPanel.getVersions(), new Predicate<BrowsableVersion>()
            {
                public boolean apply(@Nullable final BrowsableVersion input)
                {

                    return input != null && input.getName().equals(versionName);
                }
            });
        }
        catch (NoSuchElementException e)
        {
            Assert.fail("Could not find version '" + versionName + "'");
        }
        Assert.assertEquals(versionName, version.getName());
        return version.getBrowseVersionSummaryPage();
    }


    public static void assertVersionReleased(JiraTestedProduct jira, final String projectKey, final String versionName)
    {
        VersionPageTab versionPageTab = jira.getPageBinder().navigateToAndBind(VersionPageTab.class, projectKey);

        final Version version = Iterables.find(versionPageTab.getVersions(), new Predicate<Version>()
        {
            public boolean apply(@Nullable final Version input)
            {
                return input != null && input.getName().equals(versionName);
            }
        });

        Assert.assertTrue("Version " + versionName + " for project " + projectKey + " should be released", version.isReleased());
    }


    public static void assertVersionNotReleased(JiraTestedProduct jira, final String projectKey, final String versionName)
    {
        VersionPageTab versionPageTab = jira.getPageBinder().navigateToAndBind(VersionPageTab.class, projectKey);

        final Version version = Iterables.find(versionPageTab.getVersions(), new Predicate<Version>()
        {
            public boolean apply(@Nullable final Version input)
            {
                return input.getName().equals(versionName);
            }
        });

        Assert.assertTrue("Version " + versionName + " for project " + projectKey + " should not be released", !version.isReleased());
    }

    public static void createNewVersion(JiraTestedProduct jira, final String projectKey, String version)
    {
        VersionPageTab versionPageTab = jira.getPageBinder().navigateToAndBind(VersionPageTab.class, projectKey);
        versionPageTab.getEditVersionForm().fill(version, "A New Test Version", "9/Jan/20").submit();
    }

    // ------------------------------------------------------------------------------------------------- Helper Methods
    // -------------------------------------------------------------------------------------- Basic Accessors / Mutators
}
