package com.atlassian.jira.webtest.webdriver.tests.projectconfig;

import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.jira.pageobjects.pages.admin.issuetype.ChangeIssueTypeSchemePage;
import com.atlassian.jira.pageobjects.pages.admin.issuetype.EditIssueTypeSchemePage;
import com.atlassian.jira.pageobjects.project.summary.ProjectSummaryPageTab;
import com.atlassian.jira.pageobjects.project.summary.issuetypes.IssueTypesPanel;
import com.atlassian.jira.pageobjects.project.summary.issuetypes.IssueTypesPanel.IssueTypeListItem;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Web test for the project configuration summary page's Issue Types panel.
 *
 * @since v4.4
 */
@WebTest({ Category.WEBDRIVER_TEST, Category.ADMINISTRATION, Category.PLUGINS, Category.PROJECTS, Category.IGNITE  })
@Restore("xml/TestProjectConfigSummaryIssueTypesPanel.xml")
public class TestSummaryIssueTypesPanel extends BaseJiraWebTest
{

    private static final String HSP_KEY = "HSP";
    private static final String XSS_KEY = "XSS";
    private static final String MKY_KEY = "MKY";

    private static final String PROJECT_ADMIN = "project_admin";

    private static List<IssueTypeListItem> HSP_ISSUE_TYPES;
    private static List<IssueTypeListItem> XSS_ISSUE_TYPES;
    private static List<IssueTypeListItem> XSS_ISSUE_TYPES_WITH_BUG_ADDED;
    private static List<IssueTypeListItem> XSS_ISSUE_TYPES_WITH_BUG_AND_REORDERED;
    private static List<IssueTypeListItem> XSS_ISSUE_TYPES_WITH_BUG_ADDED_AND_MADE_DEFAULT;
    private static String contextPath;
    private static String baseUrl;

    @Before
    public void setUp()
    {

        contextPath = jira.getProductInstance().getContextPath();
        baseUrl = jira.getProductInstance().getBaseUrl();

        HSP_ISSUE_TYPES = Lists.newArrayList(
            createListItem("Bug", "/images/icons/bug.gif", false, true),
            createListItem("<script>alert(\"wtf\");</script>", "/images/icons/newfeature.gif%22%20onLoad=%22alert(%27wtf%27)", false, false),
            createListItem("Epic", "/images/icons/ico_epic.png", false, false),
            createListItem("Improvement", "/images/icons/improvement.gif", false, false),
            createListItem("Task", "/images/icons/task.gif", false, false),
            createListItem("Sub-task", "/images/icons/issue_subtask.gif", true, false)
        );

        XSS_ISSUE_TYPES = Lists.newArrayList(
            createListItem("Epic", "/images/icons/ico_epic.png", false, false)
        );

        XSS_ISSUE_TYPES_WITH_BUG_ADDED = Lists.newArrayList(
            createListItem("Bug", "/images/icons/bug.gif", false, false),
            createListItem("Epic", "/images/icons/ico_epic.png", false, false)
        );

        XSS_ISSUE_TYPES_WITH_BUG_AND_REORDERED = Lists.newArrayList(
            createListItem("Bug", "/images/icons/bug.gif", false, false),
            createListItem("Epic", "/images/icons/ico_epic.png", false, false)
        );

        XSS_ISSUE_TYPES_WITH_BUG_ADDED_AND_MADE_DEFAULT = Lists.newArrayList(
            createListItem("Bug", "/images/icons/bug.gif", false, true),
            createListItem("Epic", "/images/icons/ico_epic.png", false, false)
        );

    }

    @Test
    public void testListIssueTypesForProject()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);

        final List<IssueTypeListItem> issueTypes  = navigateToSummaryPageFor(HSP_KEY)
                .openPanel(IssueTypesPanel.class)
                .issueTypes();

        assertEquals(HSP_ISSUE_TYPES, issueTypes);
    }

    @Test
    public void testChangeIssueTypesInCurrentIssueTypeScheme()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);
        final ProjectSummaryPageTab hspSummaryPage = navigateToSummaryPageFor(XSS_KEY);

        final IssueTypesPanel issueTypesPanel = hspSummaryPage.openPanel(IssueTypesPanel.class);
        final List<IssueTypeListItem> issueTypes = issueTypesPanel.issueTypes();
        assertEquals(XSS_ISSUE_TYPES, issueTypes);

        assertEquals(issueTypesPanel.getIssueTypeTabUrl(), createIssueTypesUrl(XSS_KEY));

        pageBinder.navigateToAndBind(EditIssueTypeSchemePage.class, "10010", "10010")
                .moveFromAvailableToBelowSelected("Bug", "Epic")
                .submitSave();

        final List<IssueTypeListItem> bugIssueTypeList = navigateToSummaryPageFor(XSS_KEY)
                .openPanel(IssueTypesPanel.class)
                .issueTypes();

        assertEquals(XSS_ISSUE_TYPES_WITH_BUG_ADDED, bugIssueTypeList);

    }

    @Test
    public void testChangeToDifferentIssueTypeScheme()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);

        final IssueTypesPanel issueTypesPanel = navigateToSummaryPageFor(HSP_KEY)
                .openPanel(IssueTypesPanel.class);
        final List<IssueTypeListItem> originalIssueTypes = issueTypesPanel.issueTypes();

        assertEquals(HSP_ISSUE_TYPES, originalIssueTypes);

        pageBinder.navigateToAndBind(ChangeIssueTypeSchemePage.class, "10000")
                .chooseExistingIssueTypeScheme("Improved Issue Type Scheme");

        final IssueTypesPanel modifiedIssueTypesPanel = navigateToSummaryPageFor(HSP_KEY)
                .openPanel(IssueTypesPanel.class);
        final List<IssueTypeListItem> modifiedIssueTypes = modifiedIssueTypesPanel.issueTypes();

        assertEquals("Improved Issue Type Scheme", modifiedIssueTypesPanel.getIssueTypeTabLinkText());
        assertEquals(XSS_ISSUE_TYPES, modifiedIssueTypes);

    }

    @Test
    public void testCorrectIssueTypeOrder()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);

        pageBinder.navigateToAndBind(ChangeIssueTypeSchemePage.class, "10010")
                .chooseExistingIssueTypeScheme("BugBelowEpic");

        final IssueTypesPanel issueTypesPanel = navigateToSummaryPageFor(XSS_KEY)
                .openPanel(IssueTypesPanel.class);
        final List<IssueTypeListItem> epicAndBugIssueTypeList = issueTypesPanel
                .issueTypes();

        assertEquals(XSS_ISSUE_TYPES_WITH_BUG_ADDED, epicAndBugIssueTypeList);

        pageBinder.navigateToAndBind(ChangeIssueTypeSchemePage.class, "10010")
                .chooseExistingIssueTypeScheme("EpicBelowBug");

        final List<IssueTypeListItem> bugAndEpicIssueTypeList = navigateToSummaryPageFor(XSS_KEY)
                .openPanel(IssueTypesPanel.class)
                .issueTypes();

        assertEquals(XSS_ISSUE_TYPES_WITH_BUG_AND_REORDERED, bugAndEpicIssueTypeList);

    }

    @Test
    public void testNoIssueTypesInCurrentIssueTypeScheme()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);

        final IssueTypesPanel issueTypesPanel =
                navigateToSummaryPageFor(MKY_KEY).openPanel(IssueTypesPanel.class);

        final List<IssueTypeListItem> emptyIssueTypesList = issueTypesPanel
                .issueTypes();

        assertEquals(Collections.EMPTY_LIST, emptyIssueTypesList);
        assertEquals("No issue types associated", issueTypesPanel.getNoIssueTypesMessage());

    }

    @Test
    public void testDefaultIssueTypeIsNotFirst()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);

        pageBinder.navigateToAndBind(EditIssueTypeSchemePage.class, "10010", "10010")
                .moveFromAvailableToBelowSelected("Bug", "Epic")
                .submitSave();

        final IssueTypesPanel issueTypesPanel = navigateToSummaryPageFor(XSS_KEY)
                .openPanel(IssueTypesPanel.class);
        final List<IssueTypeListItem> epicAndBugIssueTypeList = issueTypesPanel
                .issueTypes();

        assertEquals(XSS_ISSUE_TYPES_WITH_BUG_ADDED, epicAndBugIssueTypeList);

        pageBinder.navigateToAndBind(EditIssueTypeSchemePage.class, "10010", "10010")
                .makeDefault("Bug")
                .submitSave();

        final List<IssueTypeListItem> epicAndBugAsDefaultIssueTypeList = navigateToSummaryPageFor(XSS_KEY)
                .openPanel(IssueTypesPanel.class)
                .issueTypes();

        assertEquals(XSS_ISSUE_TYPES_WITH_BUG_ADDED_AND_MADE_DEFAULT, epicAndBugAsDefaultIssueTypeList);
    }

    private ProjectSummaryPageTab navigateToSummaryPageFor(final String projectKey)
    {
        return pageBinder.navigateToAndBind(ProjectSummaryPageTab.class, projectKey);
    }

    private IssueTypeListItem createListItem(final String issueTypeName, final String iconImage, boolean isSubtask, boolean isDefault)
    {
        return new IssueTypeListItem(issueTypeName, baseUrl + iconImage, isSubtask, isDefault);
    }

    private String createIssueTypesUrl(String projectKey)
    {
        return contextPath + "/plugins/servlet/project-config/" + projectKey + "/issuetypes";
    }

}
