package com.atlassian.jira.webtest.webdriver.tests.projectconfig;

import com.atlassian.integrationtesting.runner.restore.RestoreOnce;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.pages.admin.SelectIssueTypeScreenScheme;
import com.atlassian.jira.pageobjects.pages.admin.issuetype.screen.schemes.configure.ConfigureIssueTypeScreenSchemePage;
import com.atlassian.jira.pageobjects.project.screens.ScreensPageTab;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import com.google.common.collect.ImmutableList;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test for the Screens panel.
 *
 * @since v4.4
 */
@WebTest({ Category.WEBDRIVER_TEST, Category.ADMINISTRATION, Category.PLUGINS, Category.PROJECTS, Category.IGNITE  })
@RestoreOnce ("xml/TestProjectConfigScreensTab.xml")
public class TestScreensPanel extends BaseJiraWebTest
{
    private static final String PROJECT_WITH_DEFAULT = "HSP";
    private static final String PROJECT_WITH_CONFIGURED_SCREENS = "MKY";
    private static final Map<String, String> ALL_PROJECTS = new HashMap<String, String>();
    private static final Map<String, String> ALL_ISSUE_TYPES = new HashMap<String, String>();
    private static final List<String> PROJECTS_VISIBLE_TO_ADMIN = ImmutableList.of("homosapien", "monkey", "BLUK", "XSS");
    private static final List<String> PROJECTS_VISIBLE_TO_FRED = ImmutableList.of("homosapien", "monkey");
    private static final List<String> DEFAULT_SCHEME_PROJECTS = ImmutableList.of("BLUK", "monkey");
    private static final List<String> SCHEME_A_PROJECTS = ImmutableList.of("BLUK", "monkey", "XSS");
    private static final List<String> SCHEME_B_PROJECTS = ImmutableList.of("monkey", "XSS");

    static
    {
        ALL_PROJECTS.put("homosapien", "projectavatar?size=small&pid=10000&avatarId=10011");
        ALL_PROJECTS.put("XSS", "projectavatar?size=small&pid=10010&avatarId=10011");
        ALL_PROJECTS.put("BLUK", "projectavatar?size=small&pid=10110&avatarId=10011");
        ALL_PROJECTS.put("monkey", "projectavatar?size=small&pid=10001&avatarId=10011");

        ALL_ISSUE_TYPES.put("Bug", "images/icons/bug.gif");
        ALL_ISSUE_TYPES.put("New Feature", "images/icons/newfeature.gif");
        ALL_ISSUE_TYPES.put("Task", "images/icons/task.gif");
        ALL_ISSUE_TYPES.put("Improvement", "images/icons/improvement.gif");
    }

    @Test
    public void testDefaultSchemeAsAdmin()
    {
        ScreensPageTab screensPage = jira.gotoLoginPage().loginAsSysAdmin(ScreensPageTab.class, PROJECT_WITH_DEFAULT);
        testDefaultScheme(screensPage, true, PROJECTS_VISIBLE_TO_ADMIN);
    }

    @Test
    public void testDefaultSchemeNotAsAdmin()
    {
        ScreensPageTab screensPage = jira.gotoLoginPage().login("fred", "fred", ScreensPageTab.class, PROJECT_WITH_DEFAULT);
        testDefaultScheme(screensPage, false, PROJECTS_VISIBLE_TO_FRED);
    }

    private void testDefaultScheme(ScreensPageTab screensPage, boolean withAdminRights, List<String> visibleProjects)
    {
        assertEquals(withAdminRights, screensPage.isSchemeLinked());
        assertEquals(withAdminRights, screensPage.isSchemeChangeAvailable());

        List<ScreensPageTab.ScreenSchemeInfo> screenSchemes = screensPage.getScreenSchemes();
        assertEquals(1, screenSchemes.size());

        ScreensPageTab.ScreenSchemeInfo screenSchemeInfo = screenSchemes.get(0);
        assertEquals("Default Screen Scheme", screenSchemeInfo.getName());
        assertTrue(screenSchemeInfo.isDefault());
        assertEquals(withAdminRights, screenSchemeInfo.isEditLinkPresent());
        // Get the projects
        List<ScreensPageTab.OtherProjectInfo> projects = screenSchemeInfo.getProjects();
        assertProjects(projects, ImmutableList.of("homosapien", "XSS"), visibleProjects);

        // Get the Issue types
        List<ScreensPageTab.IssueTypeInfo> issueTypes = screenSchemeInfo.getIssueTypes();
        assertIssueTypes(issueTypes, ImmutableList.of("Bug", "New Feature", "Task", "Improvement"));

        // Get the Screens
        List<ScreensPageTab.ScreenInfo> screens = screenSchemeInfo.getScreens();
        assertDefaultFieldScheme(screens, withAdminRights);
    }

    @Test
    public void testComplexSchemeAsAdmin()
    {
        ScreensPageTab screensPage = jira.gotoLoginPage().loginAsSysAdmin(ScreensPageTab.class, PROJECT_WITH_CONFIGURED_SCREENS);
        testComplexScreen(screensPage, true, PROJECTS_VISIBLE_TO_ADMIN);
    }

    @Test
    public void testComplexSchemeNotAsAdmin()
    {
        ScreensPageTab screensPage = jira.gotoLoginPage().login("fred", "fred", ScreensPageTab.class, PROJECT_WITH_CONFIGURED_SCREENS);
        testComplexScreen(screensPage, false, PROJECTS_VISIBLE_TO_FRED);
    }

    private void testComplexScreen(ScreensPageTab screensPage, boolean withAdminRights, List<String> visibleProjects)
    {
        assertEquals(withAdminRights, screensPage.isSchemeLinked());
        assertEquals(withAdminRights, screensPage.isSchemeChangeAvailable());

        List<ScreensPageTab.ScreenSchemeInfo> screenSchemes = screensPage.getScreenSchemes();
        assertEquals(3, screenSchemes.size());

        // WTF scheme
        ScreensPageTab.ScreenSchemeInfo screenSchemeInfo = screenSchemes.get(0);
        assertEquals("<script>alert(\"wtf\");</script>", screenSchemeInfo.getName());
        assertEquals(withAdminRights, screenSchemeInfo.isEditLinkPresent());
        // Get the projects
        List<ScreensPageTab.OtherProjectInfo> projects = screenSchemeInfo.getProjects();
        assertProjects(projects, DEFAULT_SCHEME_PROJECTS, visibleProjects);
        // Get the Issue types
        List<ScreensPageTab.IssueTypeInfo> issueTypes = screenSchemeInfo.getIssueTypes();
        assertIssueTypes(issueTypes, ImmutableList.of("New Feature", "Task"));
        // Get the Screens - Although a different scheme it is identical to default
        List<ScreensPageTab.ScreenInfo> screens = screenSchemeInfo.getScreens();
        assertDefaultFieldScheme(screens, withAdminRights);

        // Scheme A
        screenSchemeInfo = screenSchemes.get(1);
        assertEquals("Scheme A", screenSchemeInfo.getName());
        assertEquals(withAdminRights, screenSchemeInfo.isEditLinkPresent());
        // Get the projects
        projects = screenSchemeInfo.getProjects();
        assertProjects(projects, SCHEME_A_PROJECTS, visibleProjects);
        // Get the Issue types
        issueTypes = screenSchemeInfo.getIssueTypes();
        assertIssueTypes(issueTypes, ImmutableList.of("Bug"));
        // Get the Screens - Although a different scheme it is identical to default
        screens = screenSchemeInfo.getScreens();
        assertSchemeA(screens, withAdminRights);

        // Scheme B
        screenSchemeInfo = screenSchemes.get(2);
        assertEquals("Scheme B", screenSchemeInfo.getName());
        assertEquals(withAdminRights, screenSchemeInfo.isEditLinkPresent());
        // Get the projects
        projects = screenSchemeInfo.getProjects();
        assertProjects(projects, SCHEME_B_PROJECTS, visibleProjects);
        // Get the Issue types
        issueTypes = screenSchemeInfo.getIssueTypes();
        assertIssueTypes(issueTypes, ImmutableList.of("Improvement"));
        // Get the Screens - Although a different scheme it is identical to default
        screens = screenSchemeInfo.getScreens();
        assertSchemeB(screens, withAdminRights);
    }

    @Test
    public void testLinks()
    {
        ScreensPageTab screensPage = jira.gotoLoginPage().loginAsSysAdmin(ScreensPageTab.class, PROJECT_WITH_CONFIGURED_SCREENS);

        ConfigureIssueTypeScreenSchemePage editScheme = screensPage.gotoScheme();
        screensPage = editScheme.back(ScreensPageTab.class, PROJECT_WITH_CONFIGURED_SCREENS);

        SelectIssueTypeScreenScheme changeScheme = screensPage.gotoSelectScheme();
        screensPage = changeScheme.back(ScreensPageTab.class, PROJECT_WITH_CONFIGURED_SCREENS);

        List<ScreensPageTab.ScreenSchemeInfo> screenSchemes = screensPage.getScreenSchemes();
        assertEquals(3, screenSchemes.size());

// Webdriver doesn't work for these links so tough.
//        //for each page
//        for (ScreensPageTab.ScreenSchemeInfo screenSchemeInfo : screenSchemes)
//        {
//            ConfigureFieldScreenScheme editFieldSchemePage = screenSchemeInfo.gotoEditScheme();
//            screensPage = editFieldSchemePage.back(ScreensPageTab.class, PROJECT_WITH_CONFIGURED_SCREENS);
//        }
    }

    private void assertIssueTypes(List<ScreensPageTab.IssueTypeInfo> issueTypes, List<String> expectedIssueTypes)
    {
        assertEquals(expectedIssueTypes.size(), issueTypes.size());
        for (String expectedIssueType : expectedIssueTypes)
        {
            assertIssueType(issueTypes, expectedIssueType, ALL_ISSUE_TYPES.get(expectedIssueType));
        }
    }

    private void assertProjects(List<ScreensPageTab.OtherProjectInfo> projects, List<String> schemeProjects, List<String> visibleProjects)
    {
        // The real expected projects is the intersection of the scheme projects and the visible projects
        Collection<String> realExpectation = CollectionUtils.intersection(schemeProjects, visibleProjects);
        if(realExpectation.size() == 1)
        {
            assertEquals(0, projects.size());
        }
        else
        {
            assertEquals(realExpectation.size(), projects.size());
            for (String visibleProject : realExpectation)
            {
                assertProject(projects, visibleProject, ALL_PROJECTS.get(visibleProject));
            }
        }

    }

    private void assertDefaultFieldScheme(List<ScreensPageTab.ScreenInfo> screens, boolean withAdminRights)
    {
        assertEquals(3, screens.size());
        assertScreen(screens, "Create Issue", "Default Screen", withAdminRights);
        assertScreen(screens, "Edit Issue", "Default Screen", withAdminRights);
        assertScreen(screens, "View Issue", "Default Screen", withAdminRights);
    }

    private void assertSchemeA(List<ScreensPageTab.ScreenInfo> screens, boolean withAdminRights)
    {
        assertEquals(3, screens.size());
        assertScreen(screens, "Create Issue", "Default Screen", withAdminRights);
        assertScreen(screens, "Edit Issue", "Edit Screen 1", withAdminRights);
        assertScreen(screens, "View Issue", "View Screen 1", withAdminRights);
    }

    private void assertSchemeB(List<ScreensPageTab.ScreenInfo> screens, boolean withAdminRights)
    {
        assertEquals(3, screens.size());
        assertScreen(screens, "Create Issue", "Simple Create Screen", withAdminRights);
        assertScreen(screens, "Edit Issue", "Edit Screen 2", withAdminRights);
        assertScreen(screens, "View Issue", "Edit Screen 2", withAdminRights);
    }

    private void assertScreen(List<ScreensPageTab.ScreenInfo> screens, String operation, String name, boolean linkPresent)
    {
        // Find the entry in the list
        for (ScreensPageTab.ScreenInfo screen : screens)
        {
            if (screen.getOperationName().equals(operation))
            {
                // assert the name
                assertTrue(screen.getScreenName().equals(name));
                assertEquals(linkPresent, screen.getLink().isPresent());
                return;
            }
        }
        fail("Expected operation '" + operation+ "' not found.");

    }

    private void assertProject(List<ScreensPageTab.OtherProjectInfo> projects, String name, String iconUrl)
    {
        // Find the entry in the list
        for (ScreensPageTab.OtherProjectInfo project : projects)
        {
            if (project.getName().equals(name))
            {
                // assert the url
                assertTrue(project.getIconUrl().contains(iconUrl));
                return;
            }
        }
        fail("Expected project '" + name + "' not found.");
    }

    private void assertIssueType(List<ScreensPageTab.IssueTypeInfo> issueTypes, String name, String iconUrl)
    {
        // Find the entry in the list
        for (ScreensPageTab.IssueTypeInfo issueType : issueTypes)
        {
            if (issueType.getName().equals(name))
            {
                // assert the url
                assertTrue(issueType.getIconUrl().contains(iconUrl));
                return;
            }
        }
        fail("Expected issue type '" + name + "' not found.");
    }

}
