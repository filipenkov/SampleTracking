package com.atlassian.jira.webtest.webdriver.tests.projectconfig;

import com.atlassian.integrationtesting.runner.restore.RestoreOnce;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.jira.pageobjects.project.summary.ProjectSummaryPageTab;
import com.atlassian.jira.pageobjects.project.summary.versions.SummaryPanelVersion;
import com.atlassian.jira.pageobjects.project.summary.versions.VersionSummaryPanel;
import com.atlassian.jira.pageobjects.project.versions.Version;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.4
 */
@WebTest ({ Category.WEBDRIVER_TEST, Category.ADMINISTRATION, Category.PLUGINS, Category.PROJECTS, Category.IGNITE  })
@RestoreOnce ("xml/SummaryVersionsPanel.xml")
public class TestSummaryVersionsPanel extends BaseJiraWebTest
{
    private static final String PROJECT_ALL = "HSP";
    private static final String PROJECT_NONE = "FSH";
    private static final String PROJECT_SOME = "MKY";

    @Test
    public void testAllVersions()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);

        final ProjectSummaryPageTab summaryPage = navigateToSummaryPageFor(PROJECT_ALL);
        final VersionSummaryPanel panel = summaryPage.openPanel(VersionSummaryPanel.class);
        final List<Version> actualVersions = panel.getVersions();

        List<SummaryPanelVersion> expectedVersions = Lists.newArrayList();
        expectedVersions.add(new SummaryPanelVersion("New Version 6").setReleaseDate(23, 3, 1950).setOverdue(true));
        expectedVersions.add(new SummaryPanelVersion("New Version 5").setReleaseDate(16, 2, 2011).setOverdue(true));
        expectedVersions.add(new SummaryPanelVersion("New Version 1").setReleaseDate(1, 3, 2011).setReleased(true));

        assertNoVersionsNotShown(panel);
        assertSomeVersionsNotShown(panel);

        assertEquals(expectedVersions, SummaryPanelVersion.toSimple(actualVersions));
    }

    @Test
    public void testSomeVersions()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);

        final ProjectSummaryPageTab summaryPage = navigateToSummaryPageFor(PROJECT_SOME);
        final VersionSummaryPanel panel = summaryPage.openPanel(VersionSummaryPanel.class);
        final List<Version> actualVersions = panel.getVersions();

        List<SummaryPanelVersion> expectedVersions = Lists.newArrayList();
        expectedVersions.add(new SummaryPanelVersion("<script>alert(1)</script>").setReleased(false));
        expectedVersions.add(new SummaryPanelVersion("Old Version 12"));
        expectedVersions.add(new SummaryPanelVersion("Old Version 11"));
        expectedVersions.add(new SummaryPanelVersion("Old Version 10").setReleaseDate(14, 3, 2011).setReleased(true));
        expectedVersions.add(new SummaryPanelVersion("Old Version 8").setReleased(true));

        assertEquals(expectedVersions, SummaryPanelVersion.toSimple(actualVersions));

        assertEquals("Showing 5 of 11 unarchived versions.", panel.getSomeVersionsText());
        assertEquals("Show all", panel.getSomeVersionsLinkText());

        assertEquals(panel.getSomeVersionsLinkUrl(), createVersionUrl(PROJECT_SOME));

        assertNoVersionsNotShown(panel);
    }

        @Test
    public void testNoVersions()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);

        final ProjectSummaryPageTab summaryPage = navigateToSummaryPageFor(PROJECT_NONE);
        final VersionSummaryPanel panel = summaryPage.openPanel(VersionSummaryPanel.class);
        final List<Version> actualVersions = panel.getVersions();

        assertTrue(actualVersions.isEmpty());

        assertEquals("This project has no unarchived versions.", panel.getNoVersionsText());
        assertEquals("Add a version", panel.getNoVersionsLinkText());

        assertEquals(panel.getNoVersionsLinkUrl(), createVersionUrl(PROJECT_NONE));

        assertSomeVersionsNotShown(panel);
    }

    private String createVersionUrl(String projectKey)
    {
        return jira.getProductInstance().getContextPath() + "/plugins/servlet/project-config/" + projectKey + "/versions";
    }

    private void assertNoVersionsNotShown(VersionSummaryPanel panel)
    {
        assertNull(null, panel.getNoVersionsText());
        assertNull(null, panel.getNoVersionsLinkText());
        assertNull(null,  panel.getNoVersionsLinkUrl());
    }

    private void assertSomeVersionsNotShown(VersionSummaryPanel panel)
    {
        assertNull(null, panel.getSomeVersionsText());
        assertNull(null, panel.getSomeVersionsLinkText());
        assertNull(null,  panel.getSomeVersionsLinkUrl());
    }

    private ProjectSummaryPageTab navigateToSummaryPageFor(final String projectKey)
    {
        return pageBinder.navigateToAndBind(ProjectSummaryPageTab.class, projectKey);
    }
}
