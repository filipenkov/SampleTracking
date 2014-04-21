package com.atlassian.jira.webtest.webdriver.tests.projectconfig;

import com.atlassian.integrationtesting.runner.restore.RestoreOnce;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.jira.pageobjects.project.components.ComponentsPageTab;
import com.atlassian.jira.pageobjects.project.summary.ProjectSummaryPageTab;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Web test for the project configuration summary page.
 *
 * @since v4.4
 */
@WebTest({ Category.WEBDRIVER_TEST, Category.ADMINISTRATION, Category.PLUGINS, Category.PROJECTS, Category.IGNITE  })
@RestoreOnce ("xml/TestProjectConfigSummary.xml")
public class TestProjectConfigSummary extends BaseJiraWebTest
{
    private static final String HSP_KEY = "HSP";
    private static final String MKY_KEY = "MKY";

    @Test
    public void testTabNavigation()
    {
        ProjectSummaryPageTab summaryTab = jira.gotoLoginPage().loginAsSysAdmin(ProjectSummaryPageTab.class, HSP_KEY);
        assertTrue(summaryTab.getTabs().isSummaryTabSelected());

        ComponentsPageTab componentsPage = summaryTab.getTabs().gotoComponentsTab();
        assertTrue(componentsPage.getTabs().isComponentsTabSelected());
        assertEquals(HSP_KEY, componentsPage.getProjectKey());

        summaryTab = summaryTab.getTabs().gotoSummaryTab();
        assertTrue(summaryTab.getTabs().isSummaryTabSelected());
        assertEquals(HSP_KEY, summaryTab.getProjectKey());
    }

    @Test
    public void testPanelsAreCollapsible()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);

        final ProjectSummaryPageTab hspSummaryPage = navigateToSummaryPageFor(HSP_KEY);

        final Collection<String> panelHeadings = hspSummaryPage.getPanelHeadingTexts();
        for (final String panelHeading : panelHeadings)
        {
            assertTrue(hspSummaryPage.isPanelContentExpanded(panelHeading));
            hspSummaryPage.collapsePanelContent(panelHeading);
            assertTrue(hspSummaryPage.isPanelContentCollapsed(panelHeading));
            hspSummaryPage.expandPanelContent(panelHeading);
            assertTrue(hspSummaryPage.isPanelContentExpanded(panelHeading));
        }
    }

    @Test
    public void testPanelTwixiRetainsStateFromCookie()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);

        ProjectSummaryPageTab hspSummaryPage = navigateToSummaryPageFor(HSP_KEY);

        final Collection<String> panelHeadings = hspSummaryPage.getPanelHeadingTexts();
        for (final String panelHeading : panelHeadings)
        {
            assertTrue(hspSummaryPage.isPanelContentExpanded(panelHeading));
            hspSummaryPage.collapsePanelContent(panelHeading);
            assertTrue(hspSummaryPage.isPanelContentCollapsed(panelHeading));
        }

        final ProjectSummaryPageTab mkySummaryPage = navigateToSummaryPageFor(MKY_KEY);

        for (final String panelHeading : panelHeadings)
        {
            assertTrue(mkySummaryPage.isPanelContentCollapsed(panelHeading));
        }

        hspSummaryPage = navigateToSummaryPageFor(HSP_KEY);

        for (final String panelHeading : panelHeadings)
        {
            assertTrue(hspSummaryPage.isPanelContentCollapsed(panelHeading));
            hspSummaryPage.expandPanelContent(panelHeading);
            assertTrue(hspSummaryPage.isPanelContentExpanded(panelHeading));
        }
    }

    private ProjectSummaryPageTab navigateToSummaryPageFor(final String projectKey)
    {
        return pageBinder.navigateToAndBind(ProjectSummaryPageTab.class, projectKey);
    }
}
