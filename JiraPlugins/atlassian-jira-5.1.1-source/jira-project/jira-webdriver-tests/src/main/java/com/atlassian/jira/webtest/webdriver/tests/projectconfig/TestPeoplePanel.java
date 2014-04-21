package com.atlassian.jira.webtest.webdriver.tests.projectconfig;

import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.WindowSession;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.jira.pageobjects.pages.admin.user.AddUserPage;
import com.atlassian.jira.pageobjects.pages.admin.user.EditUserGroupsPage;
import com.atlassian.jira.pageobjects.project.EditProjectLeadAndDefaultAssigneeDialog;
import com.atlassian.jira.pageobjects.project.people.EditPeopleRoleForm;
import com.atlassian.jira.pageobjects.project.people.MockPeopleRole;
import com.atlassian.jira.pageobjects.project.people.PeoplePage;
import com.atlassian.jira.pageobjects.project.people.PeopleRole;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import com.google.common.collect.Lists;
import org.junit.AfterClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilFalse;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Web test for the project configuration summary page's People panel.
 *
 * @since v4.4
 */
@WebTest({ Category.WEBDRIVER_TEST, Category.ADMINISTRATION, Category.PLUGINS, Category.PROJECTS, Category.IGNITE  })
@Restore("xml/TestProjectConfigPeople.xml")
public class TestPeoplePanel extends BaseJiraWebTest
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
    private static final String LEAD_FIELD = "lead";

    @AfterClass
    public static void enableXsrf()
    {
        backdoor.applicationProperties().enableXsrfChecking();
    }

    @Test
    public void testCanViewPeoplePanel()
    {
        PeoplePage peoplePage = jira.gotoLoginPage().loginAsSysAdmin(PeoplePage.class, MKY_KEY);

        assertTrue(peoplePage.isProjectLeadAvatarPresent());
        assertEquals(jira.getProductInstance().getBaseUrl() + "/secure/useravatar?size=small&avatarId=10062",
                peoplePage.getProjectLeadAvatarSrc());
        assertEquals(peoplePage.getProjectLead(), ADMIN_FULLNAME);
        assertEquals(peoplePage.getDefaultAssignee(), PROJECT_LEAD);
        assertFalse(peoplePage.isProjectLeadNonExistentIndicated());
        assertFalse(peoplePage.isProjectLeadNotAssignableIndicated());
        assertTrue(peoplePage.isDefaultAssigneeUserHoverEnabled());

        final List<PeopleRole> expectedRoles = Lists.<PeopleRole>newArrayList(
                new MockPeopleRole("Administrators")
                        .addUser("Administrator", prependBaseUrl("/images/icons/bug.gif"))
                        .addGroup("jira-administrators", true),
                new MockPeopleRole("Developers")
                        .addUser("Fred Normal", prependBaseUrl("/images/icons/bug.gif"))
                        .addGroup("jira-developers", true),
                new MockPeopleRole("Users")
                        .addUser("Administrator", prependBaseUrl("/images/icons/bug.gif"))
                        .addGroup("jira-users", true)
        );

        waitUntilFalse(peoplePage.isTableLoading());

        final List<PeopleRole> roles = peoplePage.getRoles();
        assertEquals(expectedRoles, roles);
    }

    @Test
    public void testCanEditPeoplePanel()
    {
        PeoplePage peoplePage = jira.gotoLoginPage().loginAsSysAdmin(PeoplePage.class, MKY_KEY);

        PeopleRole administrators = peoplePage.getRoleByName("Administrators");
        assertNotNull("Administrators role not found", administrators);

        final List<PeopleRole> originalRoles = Lists.<PeopleRole>newArrayList(
            new MockPeopleRole("Administrators")
                    .addUser("Administrator", prependBaseUrl("/images/icons/bug.gif"))
                    .addGroup("jira-administrators", true),
            new MockPeopleRole("Developers")
                    .addUser("Fred Normal", prependBaseUrl("/images/icons/bug.gif"))
                    .addGroup("jira-developers", true),
            new MockPeopleRole("Users")
                    .addUser("Administrator", prependBaseUrl("/images/icons/bug.gif"))
                    .addGroup("jira-users", true)
        );

        final List<PeopleRole> originalRolesWithFredAndDevelopersAsAdmin = Lists.<PeopleRole>newArrayList(
            new MockPeopleRole("Administrators")
                    .addUser("Fred Normal", prependBaseUrl("/images/icons/bug.gif"))
                    .addGroup("jira-developers", true),
            new MockPeopleRole("Developers")
                    .addUser("Fred Normal", prependBaseUrl("/images/icons/bug.gif"))
                    .addGroup("jira-developers", true),
            new MockPeopleRole("Users")
                    .addUser("Administrator", prependBaseUrl("/images/icons/bug.gif"))
                    .addGroup("jira-users", true)
        );

        administrators.edit("project-config-people-users-select-textarea")
                .addUser("Fred Normal")
                .removeUser("Administrator")
                .addGroup("jira-developers")
                .removeGroup("jira-administrators")
                .submit();

        waitUntilFalse(peoplePage.isTableLoading());

        // Assert modifications made
        assertEquals(originalRolesWithFredAndDevelopersAsAdmin, peoplePage.getRoles());

        administrators.edit("project-config-people-users-select-textarea")
                .addUser("Administrator")
                .removeUser("Fred Normal")
                .addGroup("jira-administrators")
                .removeGroup("jira-developers")
                .cancel();

        waitUntilFalse(peoplePage.isTableLoading());

        // Assert no modifications made
        assertEquals(originalRolesWithFredAndDevelopersAsAdmin, peoplePage.getRoles());

        administrators.edit("project-config-people-groups-select-textarea")
                .addGroup("jira-administrators")
                .removeGroup("jira-developers")
                .addUser("Administrator")
                .removeUser("Fred Normal")
                .submit();

        waitUntilFalse(peoplePage.isTableLoading());

        // Assert modifications made
        assertEquals(originalRoles, peoplePage.getRoles());


        administrators.edit("project-config-people-groups-select-textarea")
                .addGroup("jira-developers")
                .removeGroup("jira-administrators")
                .addUser("Fred Normal")
                .removeUser("Administrator")
                .cancel();

        waitUntilFalse(peoplePage.isTableLoading());

        // Assert no modifications made
        assertEquals(originalRoles, peoplePage.getRoles());

        administrators.edit("project-config-people-groups-select-textarea")
                .addGroup("jira-developers")
                .removeGroup("jira-administrators")
                .addUser("Fred Normal")
                .removeUser("Administrator");

        PeopleRole developers = peoplePage.getRoleByName("Developers");
        assertNotNull("Developers role not found", developers);

        developers.edit("project-config-people-groups-select-textarea")
                .addGroup("jira-administrators")
                .submit();

        // Assert modiciations made to both administrators and developers

        final List<PeopleRole> originalRolesWithDevelopersHavingAdmins = Lists.<PeopleRole>newArrayList(
                new MockPeopleRole("Administrators")
                        .addUser("Fred Normal", prependBaseUrl("/images/icons/bug.gif"))
                        .addGroup("jira-developers", true),
                new MockPeopleRole("Developers")
                        .addUser("Fred Normal", prependBaseUrl("/images/icons/bug.gif"))
                        .addGroup("jira-administrators", true)
                        .addGroup("jira-developers", true),
                new MockPeopleRole("Users")
                        .addUser("Administrator", prependBaseUrl("/images/icons/bug.gif"))
                        .addGroup("jira-users", true)
        );

        waitUntilFalse(peoplePage.isTableLoading());

        assertEquals(originalRolesWithDevelopersHavingAdmins, peoplePage.getRoles());

        administrators.edit("project-config-people-users-select-textarea")
                .clearGroups()
                .submit();

        waitUntilFalse(peoplePage.isTableLoading());

        // Assert modifiations made
        final List<PeopleRole> emptyAdminRole = Lists.<PeopleRole>newArrayList(
            new MockPeopleRole("Administrators")
                    .addUser("Fred Normal", prependBaseUrl("/images/icons/bug.gif")),
            new MockPeopleRole("Developers")
                    .addUser("Fred Normal", prependBaseUrl("/images/icons/bug.gif"))
                    .addGroup("jira-administrators", true)
                    .addGroup("jira-developers", true),
            new MockPeopleRole("Users")
                    .addUser("Administrator", prependBaseUrl("/images/icons/bug.gif"))
                    .addGroup("jira-users", true)
        );

        assertEquals(emptyAdminRole, peoplePage.getRoles());

        administrators = peoplePage.getRoleByName("Administrators");
        assertNotNull("Administrators role not found", administrators);

        administrators.edit("project-config-people-users-select-textarea")
                .removeUser("Fred Normal")
                .addUser("Administrator")
                .addGroup("jira-administrators")
                .submit();

        developers = peoplePage.getRoleByName("Developers");
        assertNotNull("Developers role not found", developers);

        developers.edit("project-config-people-groups-select-textarea")
                .removeGroup("jira-administrators")
                .submit();

        waitUntilFalse(peoplePage.isTableLoading());

        // Assert mofications made from scratch
        assertEquals(originalRoles, peoplePage.getRoles());

    }

    @Test
    public void testServerError()
    {
        backdoor.applicationProperties().disableXsrfChecking();
        jira.gotoLoginPage().loginAsSysAdmin(AddUserPage.class)
                .addUser("project", "project", "project", "project@project.com", false).createUser();

        pageBinder.navigateToAndBind(EditUserGroupsPage.class, "project")
                .addTo(Lists.newArrayList("jira-administrators", "jira-developers"));

        final PeoplePage peoplePage = jira.gotoLoginPage().login("project", "project", PeoplePage.class, MKY_KEY);

        PeopleRole administrators = peoplePage.getRoleByName("Administrators");
        final EditPeopleRoleForm edit = administrators.edit("project-config-people-users-select-textarea")
                .addUser("Fred Normal");

        final WindowSession windowSession = jira.windowSession();
        final WindowSession.BrowserWindow groups = windowSession.openNewWindow("groups");
        groups.switchTo();

        jira.gotoLoginPage().loginAsSysAdmin(EditUserGroupsPage.class, "project")
                .removeFrom(Lists.newArrayList("jira-administrators", "jira-developers"));

        jira.gotoLoginPage().login("project", "project", DashboardPage.class);

        groups.close();
        windowSession.switchToDefault();

        edit.submit();

        assertEquals("You cannot edit the configuration of this project.", peoplePage.getServerError());
    }

    @Test
    public void testNonExistentProjectLeadShownInRed()
    {
        PeoplePage peoplePage = jira.gotoLoginPage().loginAsSysAdmin(PeoplePage.class, HSP_KEY);

        assertEquals(peoplePage.getProjectLead(), NON_EXISTENT_USERNAME);
        assertEquals(peoplePage.getDefaultAssignee(), PROJECT_LEAD);
        assertTrue(peoplePage.isProjectLeadNonExistentIndicated());
        assertTrue(peoplePage.isProjectLeadNotAssignableIndicated());
        assertFalse(peoplePage.isDefaultAssigneeUserHoverEnabled());
    }

    @Test
    public void testNonAssignableProjectLeadShownInRed()
    {
        PeoplePage peoplePage = jira.gotoLoginPage().loginAsSysAdmin(PeoplePage.class, XSS_KEY);

        assertEquals(peoplePage.getProjectLead(), ADMIN_FULLNAME);
        assertEquals(peoplePage.getDefaultAssignee(), PROJECT_LEAD);
        assertFalse(peoplePage.isProjectLeadNonExistentIndicated());
        assertTrue(peoplePage.isProjectLeadNotAssignableIndicated());
        assertTrue(peoplePage.isDefaultAssigneeUserHoverEnabled());
    }

    @Test
    public void testProjectWithIssuesThatCanBeUnassignedDisplaysCorrectDefaultAssignee()
    {
        PeoplePage peoplePage = jira.gotoLoginPage().loginAsSysAdmin(PeoplePage.class, BLUK_KEY);

        assertEquals(peoplePage.getProjectLead(), ADMIN_FULLNAME);
        assertEquals(peoplePage.getDefaultAssignee(), UNASSIGNED_ASSIGNEE);
        assertFalse(peoplePage.isProjectLeadNonExistentIndicated());
        assertFalse(peoplePage.isProjectLeadNotAssignableIndicated());
        assertTrue(peoplePage.isDefaultAssigneeUserHoverEnabled());
    }

//    @Test
    @Ignore("Something about this import fails sometimes for some reason: https://jira.bamboo.atlassian.com/browse/JIRAHEAD-FIREFOX36-WEBDRIVER04-115/test/case/153995507. Disabling during investigation.")
    @Restore("xml/TestProjectConfigSummaryPeoplePanelWithDeletedButAssignableUser.xml")
    public void testProjectWithDeletedUserThatCanStillBeAssigned()
    {
        PeoplePage peoplePage = jira.gotoLoginPage().loginAsSysAdmin(PeoplePage.class, TST_KEY);

        assertEquals(peoplePage.getProjectLead(), DELETED_USERNAME);
        assertEquals(peoplePage.getDefaultAssignee(), UNASSIGNED_ASSIGNEE);
        assertTrue(peoplePage.isProjectLeadNonExistentIndicated());
        assertFalse(peoplePage.isProjectLeadNotAssignableIndicated());
        assertFalse(peoplePage.isDefaultAssigneeUserHoverEnabled());
    }

    @Test
    public void testChangeProjectLeadWithSuggestionsWorks()
    {
        PeoplePage peoplePage = jira.gotoLoginPage().loginAsSysAdmin(PeoplePage.class, BLUK_KEY);

        assertEquals(peoplePage.getProjectLead(), ADMIN_FULLNAME);

        final EditProjectLeadAndDefaultAssigneeDialog editDialog =
                peoplePage.openEditProjectLeadDialog();
        editDialog.setProjectLead("fre");

        assertTrue(editDialog.setProjectLead("fred")
                .submitUpdate());

        peoplePage = pageBinder.navigateToAndBind(PeoplePage.class, BLUK_KEY);

        assertEquals("Fred Normal", peoplePage.getProjectLead());
    }

    @Test
    public void testInvalidProjectLeadErrors()
    {
        PeoplePage peoplePage = jira.gotoLoginPage().loginAsSysAdmin(PeoplePage.class, BLUK_KEY);

        assertEquals(peoplePage.getProjectLead(), ADMIN_FULLNAME);

        EditProjectLeadAndDefaultAssigneeDialog editDialog =
                peoplePage.openEditProjectLeadDialog();

        editDialog.getLeadSelect().clear().type("doaks");
        assertFalse(editDialog.submitUpdate());
        assertEquals("The user you have specified as project lead does not exist.", editDialog.getFormErrors().get(LEAD_FIELD));

        editDialog = pageBinder.bind(EditProjectLeadAndDefaultAssigneeDialog.class);

        editDialog.getLeadSelect().select("");
        assertFalse(editDialog.submitUpdate());
        assertEquals("You must specify a project lead.", editDialog.getFormErrors().get(LEAD_FIELD));

        editDialog = pageBinder.bind(EditProjectLeadAndDefaultAssigneeDialog.class);

        editDialog.cancel();
    }

    @Test
    @Restore("xml/NoBrowseUsersPermission.xml")
    public void testUserPickerDisabled()
    {
        PeoplePage peoplePage = jira.gotoLoginPage().loginAsSysAdmin(PeoplePage.class, "HSP");
        EditProjectLeadAndDefaultAssigneeDialog dialog = peoplePage.openEditProjectLeadDialog();

        assertTrue("Expected lead picker to be disabled",
                dialog.isLeadpickerDisabled());

        dialog.setProjectLead("fred")
                .submitUpdate();

        assertEquals(peoplePage.getProjectLead(), "Fred Normal");
    }

    @Test
    @Restore("xml/NoBrowseUsersPermissionWithAdminAsProjectAdmin.xml")
    public void testSimpleUserPickerAllowsSelectionOfKnownUsers()
    {
        PeoplePage peoplePage = jira.gotoLoginPage().loginAsSysAdmin(PeoplePage.class, MKY_KEY);

        PeopleRole administrators = peoplePage.getRoleByName("Administrators");
        assertNotNull("Administrators role not found", administrators);

        final PeopleRole adminsWithFredAndAdminAsAdmin = new MockPeopleRole("Administrators")
                    .addUser("Administrator", prependBaseUrl("/images/icons/bug.gif"))
                    .addUser("Fred Normal", prependBaseUrl("/images/icons/bug.gif"))
                    .addGroup("jira-administrators", true);

        administrators.edit("project-config-people-users-select-textarea")
                .addUser("fred")
                .submit();

        waitUntilFalse(peoplePage.isTableLoading());

        // Assert modifications made
        assertEquals(adminsWithFredAndAdminAsAdmin, peoplePage.getRoleByName("Administrators"));
    }

    @Test
    @Restore("xml/NoBrowseUsersPermissionWithAdminAsProjectAdmin.xml")
    public void testSimpleUserPickerDoesNotAllowsSelectionOfUnknownUsers()
    {
        PeoplePage peoplePage = jira.gotoLoginPage().loginAsSysAdmin(PeoplePage.class, MKY_KEY);

        PeopleRole administrators = peoplePage.getRoleByName("Administrators");
        assertNotNull("Administrators role not found", administrators);

        final PeopleRole adminsWithOnlyAdminAsAdmin = new MockPeopleRole("Administrators")
                    .addUser("Administrator", prependBaseUrl("/images/icons/bug.gif"))
                    .addGroup("jira-administrators", true);

        EditPeopleRoleForm edit = administrators.edit("project-config-people-users-select-textarea");

        //Add a nonsense user.
        String error = edit.addUserWithError("notauser");

        //Make sure an error occurs.
        assertThat(error, equalTo("The requested user does not exist."));

        //Make sure submitting does not mess up the configuration.
        edit.submit();


        waitUntilFalse(peoplePage.isTableLoading());

        // Assert modifications not made
        assertEquals(adminsWithOnlyAdminAsAdmin, peoplePage.getRoleByName("Administrators"));
    }

    @Test
    public void testChangeDefaultAssignee()
    {
        PeoplePage peoplePage = jira.gotoLoginPage().loginAsSysAdmin(PeoplePage.class, BLUK_KEY);

        assertEquals(UNASSIGNED_ASSIGNEE, peoplePage.getDefaultAssignee());

        final EditProjectLeadAndDefaultAssigneeDialog editDialog =
                peoplePage.openEditDefaultAssigneeDialog();

        assertTrue(editDialog.setDefaultAssignee("Project Lead")
                .submitUpdate());

        peoplePage = pageBinder.navigateToAndBind(PeoplePage.class, BLUK_KEY);

        assertEquals(PROJECT_LEAD, peoplePage.getDefaultAssignee());
    }

    private String prependBaseUrl(final String url)
    {
        return jira.getProductInstance().getBaseUrl() + url;
    }
}
