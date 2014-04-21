package com.atlassian.jira.webtest.webdriver.tests.projectconfig;

import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.jira.pageobjects.project.EditProjectLeadAndDefaultAssigneeDialog;
import com.atlassian.jira.pageobjects.project.summary.ProjectSummaryPageTab;
import com.atlassian.jira.pageobjects.project.summary.people.PeoplePanel;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Web test for the project configuration summary page's People panel.
 *
 * @since v4.4
 */
@WebTest({ Category.WEBDRIVER_TEST, Category.ADMINISTRATION, Category.PLUGINS, Category.PROJECTS, Category.IGNITE  })
@Restore("xml/TestProjectConfigSummaryPeoplePanel.xml")
public class TestSummaryPeoplePanel extends BaseJiraWebTest
{

    private static final String HSP_KEY = "HSP";
    private static final String MKY_KEY = "MKY";
    private static final String XSS_KEY = "XSS";
    private static final String BLUK_KEY = "BLUK";
    private static final String TST_KEY = "TST";

    private static final String PROJECT_LEAD = "Project Lead";
    private static final String ADMIN_FULLNAME = "Administrator";
    private static final String NON_EXISTENT_USERNAME = "adminXXX";
    private static final String UNASSIGNED_ASSIGNEE = "Unassigned";
    private static final String DELETED_USERNAME = "mark";

    @Test
    public void testCanViewPeoplePanel()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);

        final PeoplePanel peoplePanel = navigateToSummaryPageFor(MKY_KEY)
                .openPanel(PeoplePanel.class);
        assertTrue(peoplePanel.isProjectLeadAvatarPresent());
        assertEquals(jira.getProductInstance().getBaseUrl() + "/secure/useravatar?size=small&avatarId=10062",
                peoplePanel.getProjectLeadAvatarSrc());
        assertEquals(peoplePanel.getProjectLead(), ADMIN_FULLNAME);
        assertEquals(peoplePanel.getDefaultAssignee(), PROJECT_LEAD);
        assertFalse(peoplePanel.isProjectLeadNonExistentIndicated());
        assertFalse(peoplePanel.isProjectLeadNotAssignableIndicated());
        assertTrue(peoplePanel.isDefaultAssigneeUserHoverEnabled());
    }

    @Test
    public void testNonExistentProjectLeadShownInRed()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);

        final PeoplePanel peoplePanel = navigateToSummaryPageFor(HSP_KEY)
                .openPanel(PeoplePanel.class);
        assertEquals(peoplePanel.getProjectLead(), NON_EXISTENT_USERNAME);
        assertEquals(peoplePanel.getDefaultAssignee(), PROJECT_LEAD);
        assertTrue(peoplePanel.isProjectLeadNonExistentIndicated());
        assertTrue(peoplePanel.isProjectLeadNotAssignableIndicated());
        assertFalse(peoplePanel.isDefaultAssigneeUserHoverEnabled());
    }

    @Test
    public void testNonAssignableProjectLeadShownInRed()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);

        final PeoplePanel peoplePanel = navigateToSummaryPageFor(XSS_KEY)
                .openPanel(PeoplePanel.class);
        assertEquals(peoplePanel.getProjectLead(), ADMIN_FULLNAME);
        assertEquals(peoplePanel.getDefaultAssignee(), PROJECT_LEAD);
        assertFalse(peoplePanel.isProjectLeadNonExistentIndicated());
        assertTrue(peoplePanel.isProjectLeadNotAssignableIndicated());
        assertTrue(peoplePanel.isDefaultAssigneeUserHoverEnabled());
    }

    @Test
    public void testProjectWithIssuesThatCanBeUnassignedDisplaysCorrectDefaultAssignee()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);

        final PeoplePanel peoplePanel = navigateToSummaryPageFor(BLUK_KEY)
                .openPanel(PeoplePanel.class);
        assertEquals(peoplePanel.getProjectLead(), ADMIN_FULLNAME);
        assertEquals(peoplePanel.getDefaultAssignee(), UNASSIGNED_ASSIGNEE);
        assertFalse(peoplePanel.isProjectLeadNonExistentIndicated());
        assertFalse(peoplePanel.isProjectLeadNotAssignableIndicated());
        assertTrue(peoplePanel.isDefaultAssigneeUserHoverEnabled());
    }

    @Test
    @Restore("xml/TestProjectConfigSummaryPeoplePanelWithDeletedButAssignableUser.xml")
    public void testProjectWithDeletedUserThatCanStillBeAssigned()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);

        final PeoplePanel peoplePanel = navigateToSummaryPageFor(TST_KEY)
                .openPanel(PeoplePanel.class);
        assertEquals(peoplePanel.getProjectLead(), DELETED_USERNAME);
        assertEquals(peoplePanel.getDefaultAssignee(), UNASSIGNED_ASSIGNEE);
        assertTrue(peoplePanel.isProjectLeadNonExistentIndicated());
        assertFalse(peoplePanel.isProjectLeadNotAssignableIndicated());
        assertFalse(peoplePanel.isDefaultAssigneeUserHoverEnabled());
    }

    private ProjectSummaryPageTab navigateToSummaryPageFor(final String projectKey)
    {
        return pageBinder.navigateToAndBind(ProjectSummaryPageTab.class, projectKey);
    }

}
