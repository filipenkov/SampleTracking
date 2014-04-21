package com.atlassian.jira.webtest.webdriver.tests.projectconfig;

import com.atlassian.integrationtesting.runner.restore.RestoreOnce;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.global.User;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.jira.pageobjects.project.summary.ProjectSummaryPageTab;
import com.atlassian.jira.pageobjects.project.summary.components.ComponentsSummaryPanel;
import com.atlassian.jira.pageobjects.project.summary.components.ProjectComponent;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Web test for the project configuration summary page.
 *
 * @since v4.4
 */
@WebTest({ Category.WEBDRIVER_TEST, Category.ADMINISTRATION, Category.PLUGINS, Category.PROJECTS, Category.IGNITE  })
@RestoreOnce ("xml/TestProjectConfigSummaryComponentPanel.xml")
public class TestSummaryComponentsPanel extends BaseJiraWebTest
{
    private static final String PROJECT_SOME = "FSH";
    private static final String PROJECT_ALL = "HSP";
    private static final String PROJECT_NONE = "MKY";

    @Test
    public void testProjectAllComponents()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);

        final ProjectSummaryPageTab summaryPage = navigateToSummaryPageFor(PROJECT_ALL);
        final ComponentsSummaryPanel panel = summaryPage.openPanel(ComponentsSummaryPanel.class);
        final List<ProjectComponent> actualComponents = panel.components();

        List<ProjectComponent> expectedComponents = Lists.newArrayList();
        expectedComponents.add(new ProjectComponent("New Component 1", new User("admin", "Administrator")));
        expectedComponents.add(new ProjectComponent("New Component 2", new User("fred", "Fred Normal")));
        expectedComponents.add(new ProjectComponent("New Component 3", null));

        assertNoComponentsNotShown(panel);
        assertSomeComponentsNotShown(panel);

        assertEquals(expectedComponents, actualComponents);
    }

    @Test
    public void testProjectNoComponents()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);

        final ProjectSummaryPageTab summaryPage = navigateToSummaryPageFor(PROJECT_NONE);
        final ComponentsSummaryPanel panel = summaryPage.openPanel(ComponentsSummaryPanel.class);
        final List<ProjectComponent> actualComponents = panel.components();

        assertTrue(actualComponents.isEmpty());
        assertEquals("This project does not use any components.", panel.getNoComponentsText());
        assertEquals("Add a component", panel.getNoComponentsLinkText());

        assertEquals(panel.getNoComponentsLinkUrl(), createComponentsUrl(PROJECT_NONE));

        assertSomeComponentsNotShown(panel);
    }

    @Test
    public void testProjectSomeComponents()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);

        final ProjectSummaryPageTab summaryPage = navigateToSummaryPageFor(PROJECT_SOME);
        final ComponentsSummaryPanel panel = summaryPage.openPanel(ComponentsSummaryPanel.class);
        final List<ProjectComponent> actualComponents = panel.components();

        List<ProjectComponent> expectedComponents = Lists.newArrayList();
        expectedComponents.add(new ProjectComponent("\"><script>alert(\"XSS\")</script>", null));
        expectedComponents.add(new ProjectComponent("<script>alert(\"XSS\")</script>", new User("\"><script>alert(\"xss\")</script>", "<script>alert(\"XSS\")</script>")));
        expectedComponents.add(new ProjectComponent("Component C", null));
        expectedComponents.add(new ProjectComponent("Component D", null));
        expectedComponents.add(new ProjectComponent("Component E", null));

        assertEquals(expectedComponents, actualComponents);
        assertEquals("Showing 5 of 12 components.", panel.getSomeComponentText());
        assertEquals("Show all", panel.getSomeComponentLinkText());

        assertEquals(panel.getSomeComponentLinkUrl(), createComponentsUrl(PROJECT_SOME));

        assertNoComponentsNotShown(panel);
    }

    private void assertNoComponentsNotShown(ComponentsSummaryPanel panel)
    {
        assertNull(null, panel.getNoComponentsText());
        assertNull(null, panel.getNoComponentsLinkText());
        assertNull(null,  panel.getNoComponentsLinkUrl());
    }

    private void assertSomeComponentsNotShown(ComponentsSummaryPanel panel)
    {
        assertNull(null, panel.getSomeComponentText());
        assertNull(null, panel.getSomeComponentLinkText());
        assertNull(null,  panel.getSomeComponentLinkUrl());
    }

    private ProjectSummaryPageTab navigateToSummaryPageFor(final String projectKey)
    {
        return pageBinder.navigateToAndBind(ProjectSummaryPageTab.class, projectKey);
    }

    private String createComponentsUrl(String projectKey)
    {
        return jira.getProductInstance().getContextPath() + "/plugins/servlet/project-config/" + projectKey + "/components";
    }
}
