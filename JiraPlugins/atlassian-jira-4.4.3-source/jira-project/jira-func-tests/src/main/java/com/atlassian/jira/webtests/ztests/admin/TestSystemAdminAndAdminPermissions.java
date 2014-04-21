package com.atlassian.jira.webtests.ztests.admin;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.admin.MailServerAdministration;
import com.atlassian.jira.functest.framework.admin.ViewServices;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import java.util.List;
import java.util.Set;

/**
 * Tests permissions of sysadmins and admins.
 *
 * @since v3.12
 */
@WebTest({Category.FUNC_TEST, Category.ADMINISTRATION, Category.PERMISSIONS })
public class TestSystemAdminAndAdminPermissions extends FuncTestCase
{
    private static final String MAIL_SERVER_ADMINISTRATION_URL = "/secure/admin/ViewMailServers.jspa";
    private static final String SETUP_MAIL_SERVER_WARNING_LINK_TEXT = "mail server";
    private static final String NO_MAIL_SERVER_SETUP_WARNING_CONTAINER_LOCATOR = "//*[@id='no-mail-server-setup-warning']";
    private static final String SETUP_MAIL_SERVER_WARNING_LINK_LOCATOR = NO_MAIL_SERVER_SETUP_WARNING_CONTAINER_LOCATOR + "/a";
    private static final String CONFIGURE_MAIL_SERVER_LINK_CONTAINER_LOCATOR = "//*[@id='configure_mail_server']/../..[@class='desc-wrap']";
    private static final String CONFIGURE_MAIL_SERVER_LINK_TEXT = "configure";
    private static final String VIEW_SERVICES_PAGE_FORM_TITLES_LOCATOR = ".jiraform .formtitle";

    @Override
    public void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestSystemAdminAndAdminPermissions.xml");
    }

    /** Removing admin (but not sysadmin) from the jira-users group should not stop admin from logging in. */
    public void testRemoveAdminFromJiraUsers()
    {
        try
        {
            administration.usersAndGroups().removeUserFromGroup("nonsystemadmin", "jira-users");
            navigation.login("nonsystemadmin", "nonsystemadmin");

            tester.assertLinkPresent("log_out"); // thus we are logged in
        }
        finally
        {
            navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        }
    }

    /** Removing sysadmin from the jira-users group should not stop sysadmin from logging in. */
    public void testRemoveSysAdminFromJiraUsers()
    {
        administration.usersAndGroups().removeUserFromGroup(ADMIN_USERNAME, "jira-users");
        navigation.logout();
        navigation.login(ADMIN_USERNAME);

        tester.assertLinkPresent("log_out");
    }

    /** Removing sysadmin from the jira-users and jira-administrators group should not stop sysadmin from logging in. */
    public void testRemoveSysAdminFromAdminsAndJiraUsers()
    {
        administration.usersAndGroups().addGroup("systemadmins");
        administration.addGlobalPermission(SYSTEM_ADMINISTER, "systemadmins");
        administration.usersAndGroups().addUserToGroup(ADMIN_USERNAME, "systemadmins");

        administration.usersAndGroups().removeUserFromGroup(ADMIN_USERNAME, "jira-users");
        administration.usersAndGroups().removeUserFromGroup(ADMIN_USERNAME, "jira-administrators");

        navigation.logout();
        navigation.login(ADMIN_USERNAME);

        tester.assertLinkPresent("log_out");
    }

    public void testNotificationSchemeMailServerWarningAsAdmin()
    {
        try
        {
            administration.restoreData("TestWithSystemAdmin.xml");

            navigation.gotoAdminSection("notification_schemes");
            // Disabled until http://jdog.atlassian.com/browse/JRADEV-1422 has been resolved
            // assertLinkPresentWithText("contact a System Administrator");

            assertions.getLinkAssertions().assertLinkNotPresentWithExactText(NO_MAIL_SERVER_SETUP_WARNING_CONTAINER_LOCATOR,
                    SETUP_MAIL_SERVER_WARNING_LINK_TEXT);
        }
        finally
        {
            navigation.logout();
            // go back to sysadmin user
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            administration.restoreBlankInstance();
        }
    }

    public void testNotificationSchemeMailServerWarningAsSysAdmin()
    {
        try
        {
            administration.restoreData("TestWithSystemAdmin.xml");
            navigation.logout();
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);

            navigation.gotoAdminSection("notification_schemes");

            assertions.getLinkAssertions().assertLinkPresentWithExactText(NO_MAIL_SERVER_SETUP_WARNING_CONTAINER_LOCATOR,
                    SETUP_MAIL_SERVER_WARNING_LINK_TEXT);
            assertions.getLinkAssertions().assertLinkAtNodeContains(SETUP_MAIL_SERVER_WARNING_LINK_LOCATOR,
                    MAIL_SERVER_ADMINISTRATION_URL);

            tester.assertLinkNotPresentWithText("contact a System Administrator");
        }
        finally
        {
            navigation.logout();
            // go back to sysadmin user
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            administration.restoreBlankInstance();
        }
    }

    public void testEditNotificationSchemeMailServerWarningAsAdmin()
    {
        try
        {
            administration.restoreData("TestWithSystemAdmin.xml");

            navigation.gotoAdminSection("notification_schemes");
            tester.clickLinkWithText("Default Notification Scheme");

            // Disabled until http://jdog.atlassian.com/browse/JRADEV-1422 has been resolved
            // assertLinkPresentWithText("contact a System Administrator");

            assertions.getLinkAssertions().assertLinkNotPresentWithExactText(NO_MAIL_SERVER_SETUP_WARNING_CONTAINER_LOCATOR,
                    SETUP_MAIL_SERVER_WARNING_LINK_TEXT);
        }
        finally
        {
            navigation.logout();
            // go back to sysadmin user
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            administration.restoreBlankInstance();
        }

    }

    public void testEditNotificationSchemeMailServerWarningAsSysAdmin()
    {
        try
        {
            administration.restoreData("TestWithSystemAdmin.xml");

            navigation.logout();
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);

            navigation.gotoAdminSection("notification_schemes");
            tester.clickLinkWithText("Default Notification Scheme");

            tester.assertLinkNotPresentWithText("contact a System Administrator");

            assertions.getLinkAssertions().assertLinkPresentWithExactText(NO_MAIL_SERVER_SETUP_WARNING_CONTAINER_LOCATOR,
                    SETUP_MAIL_SERVER_WARNING_LINK_TEXT);
            assertions.getLinkAssertions().assertLinkAtNodeContains(SETUP_MAIL_SERVER_WARNING_LINK_LOCATOR,
                    MAIL_SERVER_ADMINISTRATION_URL);

        }
        finally
        {
            navigation.logout();
            // go back to sysadmin user
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            administration.restoreBlankInstance();
        }

    }

    public void testSendBulkMailMailServerWarningAsAdmin()
    {
        try
        {
            administration.restoreData("TestWithSystemAdmin.xml");

            navigation.gotoAdmin();
            tester.clickLink("send_email");

            // Disabled until http://jdog.atlassian.com/browse/JRADEV-1422 has been resolved
            // assertLinkPresentWithText("contact a System Administrator");
            assertions.getLinkAssertions().assertLinkNotPresentWithExactText(CONFIGURE_MAIL_SERVER_LINK_CONTAINER_LOCATOR,
                    CONFIGURE_MAIL_SERVER_LINK_TEXT);
        }
        finally
        {
            navigation.logout();
            // go back to sysadmin user
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            administration.restoreBlankInstance();
        }
    }

    public void testSendBulkMailMailServerWarningAsSysAdmin()
    {
        try
        {
            administration.restoreData("TestWithSystemAdmin.xml");

            navigation.logout();
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);

            navigation.gotoAdmin();
            tester.clickLink("send_email");

            // Disabled until http://jdog.atlassian.com/browse/JRADEV-1422 has been resolved
//            assertLinkNotPresentWithText("contact a System Administrator");

            assertions.getLinkAssertions().assertLinkPresentWithExactText(CONFIGURE_MAIL_SERVER_LINK_CONTAINER_LOCATOR,
                    CONFIGURE_MAIL_SERVER_LINK_TEXT);
        }
        finally
        {
            navigation.logout();
            // go back to sysadmin user
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            administration.restoreBlankInstance();
        }
    }

    public void testSupportRequestMailServerWarningAsAdmin()
    {
        try
        {
            administration.restoreData("TestWithSystemAdmin.xml");

            navigation.gotoAdmin();
            tester.gotoPage("/secure/admin/JiraSupportRequest!default.jspa");

            //Disabled until http://jdog.atlassian.com/browse/JRADEV-1422 has been resolved
//            assertLinkPresentWithText("contact a System Administrator");
            assertions.getLinkAssertions().assertLinkNotPresentWithExactText(CONFIGURE_MAIL_SERVER_LINK_CONTAINER_LOCATOR,
                    CONFIGURE_MAIL_SERVER_LINK_TEXT);
        }
        finally
        {
            navigation.logout();
            // go back to sysadmin user
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            administration.restoreBlankInstance();
        }

    }

    public void testSupportRequestMailServerWarningAsSysAdmin()
    {
        try
        {
            administration.restoreData("TestWithSystemAdmin.xml");

            navigation.logout();
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);

            navigation.gotoAdmin();
            tester.gotoPage("/secure/admin/JiraSupportRequest!default.jspa");

            //Disabled until http://jdog.atlassian.com/browse/JRADEV-1422 has been resolved
//            assertLinkNotPresentWithText("contact a System Administrator");
            assertions.getLinkAssertions().assertLinkPresentWithExactText(CONFIGURE_MAIL_SERVER_LINK_CONTAINER_LOCATOR,
                    CONFIGURE_MAIL_SERVER_LINK_TEXT);
        }
        finally
        {
            navigation.logout();
            // go back to sysadmin user
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            administration.restoreBlankInstance();
        }
    }

    public void testMailQueueMailServerWarningAsAdmin()
    {
        try
        {
            administration.restoreData("TestWithSystemAdmin.xml");

            navigation.gotoAdmin();
            tester.clickLink("mail_queue");

            //Disabled until http://jdog.atlassian.com/browse/JRADEV-1422 has been resolved
//            assertLinkPresentWithText("contact a System Administrator");
            assertions.getLinkAssertions().assertLinkNotPresentWithExactText
                    ("//*[@class='jiraformheader']//*[@class='warning']/../..", SETUP_MAIL_SERVER_WARNING_LINK_TEXT);
        }
        finally
        {
            navigation.logout();
            // go back to sysadmin user
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            administration.restoreBlankInstance();
        }
    }

    public void testMailQueueMailServerWarningAsSysAdmin()
    {
        try
        {
            administration.restoreData("TestWithSystemAdmin.xml");

            navigation.logout();
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);

            navigation.gotoAdmin();
            tester.clickLink("mail_queue");

            //Disabled until http://jdog.atlassian.com/browse/JRADEV-1422 has been resolved
//            assertLinkNotPresentWithText("contact a System Administrator");
                        assertions.getLinkAssertions().assertLinkPresentWithExactText
                    ("//*[@class='jiraformheader']//*[@class='warning']/../..", SETUP_MAIL_SERVER_WARNING_LINK_TEXT);
        }
        finally
        {
            navigation.logout();
            // go back to sysadmin user
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            administration.restoreBlankInstance();
        }
    }

    public void testDeleteProjectBackupWarningAsAdmin()
    {
        try
        {
            administration.restoreData("TestWithSystemAdmin.xml");

            navigation.gotoAdmin();
            tester.clickLink("view_projects");
            tester.clickLinkWithText("Delete");

            //Disabled until http://jdog.atlassian.com/browse/JRADEV-1422 has been resolved
//            assertLinkPresentWithText("contact a System Administrator");
            tester.assertLinkNotPresentWithText("back it up first");
        }
        finally
        {
            navigation.logout();
            // go back to sysadmin user
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            administration.restoreBlankInstance();
        }
    }

    public void testDeleteProjectBackupWarningAsSysAdmin()
    {
        try
        {
            administration.restoreData("TestWithSystemAdmin.xml");

            navigation.logout();
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);

            navigation.gotoAdmin();
            tester.clickLink("view_projects");
            tester.clickLinkWithText("Delete");

            tester.assertLinkNotPresentWithText("contact a System Administrator");
            tester.assertLinkPresentWithText("back it up first");
        }
        finally
        {
            navigation.logout();
            // go back to sysadmin user
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            administration.restoreBlankInstance();
        }
    }

    public void testActivateWorkflowBackupWarningAsAdmin()
    {
        try
        {
            administration.restoreData("TestWithSystemAdmin.xml");

            createNewWorkflowSchemeAndGotoAssociateProject();

            //Disabled until http://jdog.atlassian.com/browse/JRADEV-1422 has been resolved
//            assertLinkPresentWithText("contact a System Administrator");
            assertions.getLinkAssertions().assertLinkNotPresentWithExactText("", "backup");

            // Now test the second screen
            tester.selectOption("schemeId", "Test");
            tester.submit("Associate");
            tester.assertTextPresent("2 of 3");
            assertions.getLinkAssertions().assertLinkNotPresentWithExactText("", "backup");
            //Disabled until http://jdog.atlassian.com/browse/JRADEV-1422 has been resolved
//            assertLinkPresentWithText("contact a System Administrator");
        }
        finally
        {
            navigation.logout();
            // go back to sysadmin user
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            administration.restoreBlankInstance();
        }
    }

    public void testActivateWorkflowBackupWarningAsSysAdmin()
    {
        try
        {
            administration.restoreData("TestWithSystemAdmin.xml");

            navigation.logout();
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);

            createNewWorkflowSchemeAndGotoAssociateProject();

            tester.assertLinkNotPresentWithText("contact a System Administrator");
            tester.assertLinkPresentWithText("backup");

            // Now test the second screen
            tester.selectOption("schemeId", "Test");
            tester.submit("Associate");
            tester.assertTextPresent("2 of 3");
            tester.assertLinkPresentWithText("backup");
            tester.assertLinkNotPresentWithText("contact a System Administrator");
        }
        finally
        {
            navigation.logout();
            // go back to sysadmin user
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            administration.restoreBlankInstance();
        }
    }

    public void testDeleteWorkflowBackupWarningAsAdmin()
    {
        try
        {
            administration.restoreData("TestWithSystemAdmin.xml");
            administration.workflows().goTo().copyWorkflow("jira", "Copy of jira");
            tester.clickLink("del_Copy of jira");

            //Disabled until http://jdog.atlassian.com/browse/JRADEV-1422 has been resolved
//            assertLinkPresentWithText("contact a System Administrator");
            tester.assertLinkNotPresentWithText("do a full backup");
        }
        finally
        {
            navigation.logout();
            // go back to sysadmin user
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            administration.restoreBlankInstance();
        }
    }

    public void testDeleteWorkflowBackupWarningAsSysAdmin()
    {
        try
        {
            administration.restoreData("TestWithSystemAdmin.xml");

            navigation.logout();
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);

            administration.workflows().goTo().copyWorkflow("jira", "Copy of jira");
            tester.clickLink("del_Copy of jira");

            tester.assertLinkNotPresentWithText("contact a System Administrator");
            tester.assertLinkPresentWithText("do a full backup");
        }
        finally
        {
            navigation.logout();
            // go back to sysadmin user
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            administration.restoreBlankInstance();
        }
    }

    public void testSchemeToolsBackupWarningAsAdmin()
    {
        try
        {
            administration.restoreData("TestWithSystemAdmin.xml");
            navigation.gotoAdmin();
            tester.clickLink("scheme_tools");

            //Disabled until http://jdog.atlassian.com/browse/JRADEV-1422 has been resolved
//            assertLinkPresentWithText("contact a System Administrator");
            tester.assertLinkNotPresentWithText("backupf");
        }
        finally
        {
            navigation.logout();
            // go back to sysadmin user
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            administration.restoreBlankInstance();
        }
    }

    public void testSchemeToolsBackupWarningAsSysAdmin()
    {
        try
        {
            administration.restoreData("TestWithSystemAdmin.xml");

            navigation.logout();
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);

            navigation.gotoAdmin();

            tester.clickLink("scheme_tools");

            tester.assertLinkNotPresentWithText("contact a System Administrator");
            tester.assertLinkPresentWithText("backup");
        }
        finally
        {
            navigation.logout();
            // go back to sysadmin user
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            administration.restoreBlankInstance();
        }
    }

    public void testSchemePurgePreviewBackupWarningAsAdmin()
    {
        try
        {
            administration.restoreData("TestWithSystemAdmin.xml");

            navigation.gotoAdmin();

            // Create a permission scheme copy
            tester.clickLink("permission_schemes");
            tester.clickLinkWithText("Copy");

            tester.clickLink("scheme_tools");
            tester.clickLink("delete_tool");
            tester.checkCheckbox("selectedSchemeIds", "10000");
            tester.submit("Preview");

            //Disabled until http://jdog.atlassian.com/browse/JRADEV-1422 has been resolved
//            assertLinkPresentWithText("contact a System Administrator");
            assertions.getLinkAssertions().assertLinkNotPresentWithExactText("", "backup");
        }
        finally
        {
            navigation.logout();
            // go back to sysadmin user
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            administration.restoreBlankInstance();
        }
    }

    public void testSchemePurgePreviewBackupWarningAsSysAdmin()
    {
        try
        {
            administration.restoreData("TestWithSystemAdmin.xml");

            navigation.logout();
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);

            navigation.gotoAdmin();

            // Create a permission scheme copy
            tester.clickLink("permission_schemes");
            tester.clickLinkWithText("Copy");

            tester.clickLink("scheme_tools");
            tester.clickLink("delete_tool");
            tester.checkCheckbox("selectedSchemeIds", "10000");
            tester.submit("Preview");

            tester.assertLinkNotPresentWithText("contact a System Administrator");
            tester.assertLinkPresentWithText("backup");
        }
        finally
        {
            navigation.logout();
            // go back to sysadmin user
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            administration.restoreBlankInstance();
        }
    }

    public void testViewGroupEditLinkNotPresentAsAdmin()
    {
        try
        {
            administration.restoreData("TestWithSystemAdmin.xml");

            navigation.gotoAdmin();
            tester.clickLink("group_browser");
            tester.clickLinkWithText("jira-sys-admins");

            tester.assertLinkNotPresent("edit_members_of_jira-sys-admins");
        }
        finally
        {
            navigation.logout();
            // go back to sysadmin user
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            administration.restoreBlankInstance();
        }
    }

    public void testViewGroupEditLinkPresentAsSysAdmin()
    {
        try
        {
            administration.restoreData("TestWithSystemAdmin.xml");

            navigation.logout();
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);

            navigation.gotoAdmin();
            tester.clickLink("group_browser");
            tester.clickLinkWithText("jira-sys-admins");

            tester.assertLinkPresent("edit_members_of_jira-sys-admins");
        }
        finally
        {
            navigation.logout();
            // go back to sysadmin user
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            administration.restoreBlankInstance();
        }
    }

    public void testUserBrowserEditAndDeleteLinkNotPresentAsAdmin()
    {
        try
        {
            administration.restoreData("TestWithSystemAdmin.xml");

            navigation.gotoAdmin();
            tester.clickLink("user_browser");

            tester.assertLinkNotPresent("edituser_link_root");
            tester.assertLinkNotPresent("deleteuser_link_root");
        }
        finally
        {
            navigation.logout();
            // go back to sysadmin user
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            administration.restoreBlankInstance();
        }
    }

    public void testUserBrowserEditAndDeleteLinkPresentAsSysAdmin()
    {
        try
        {
            administration.restoreData("TestWithSystemAdmin.xml");

            navigation.logout();
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);

            navigation.gotoAdmin();
            tester.clickLink("user_browser");

            tester.assertLinkPresent("edituser_link_root");
            tester.assertLinkPresent("deleteuser_link_root");
        }
        finally
        {
            navigation.logout();
            // go back to sysadmin user
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            administration.restoreBlankInstance();
        }
    }

    public void testAttachmentEditLinkNotPresentAsAdmin()
    {
        try
        {
            administration.restoreData("TestWithSystemAdmin.xml");

            navigation.gotoAdmin();
            tester.clickLink("attachments");

            tester.assertLinkNotPresentWithText("Edit Configuration");
            //Disabled until http://jdog.atlassian.com/browse/JRADEV-1422 has been resolved
//            assertLinkPresentWithText("contact a system administrator");
        }
        finally
        {
            navigation.logout();
            // go back to sysadmin user
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            administration.restoreBlankInstance();
        }
    }

    public void testAttachmentEditAsAdmin()
    {
        try
        {
            administration.restoreData("TestWithSystemAdmin.xml");

            // Fudge the url and make sure we get an error
            tester.gotoPage(page.addXsrfToken("/secure/admin/jira/EditAttachmentSettings.jspa?thumbnailsEnabled=true"));

            tester.assertTextPresent("Attachments must be enabled to enable thumbnails.");
        }
        finally
        {
            navigation.logout();
            // go back to sysadmin user
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            administration.restoreBlankInstance();
        }
    }

    public void testAttachmentEditLinkPresentAsSysAdmin()
    {
        try
        {
            administration.restoreData("TestWithSystemAdmin.xml");

            navigation.logout();
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);

            navigation.gotoAdmin();
            tester.clickLink("attachments");

            tester.assertLinkPresentWithText("Edit Configuration");
        }
        finally
        {
            navigation.logout();
            // go back to sysadmin user
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            administration.restoreBlankInstance();
        }
    }

    public void testAdminCanNotElevateHisPrivilegesBySettingSysAdminPassword()
      {
          try
          {
              administration.restoreData("TestWithSystemAdmin.xml");

              // I am admin right now so I shouldn't be able to set a sys admins password
              tester.gotoPage("/secure/admin/user/ViewUser.jspa?name=root");
              text.assertTextNotPresent(locator.page(), "Set Password");
              text.assertTextPresent(locator.page(), "This user is a System Administrator. Your permission to modify the user is restricted because you do not have System Administrator permissions.");

              // as admin I should not be able to set the sys admins password even if I URL hack
              tester.gotoPage("/secure/admin/user/SetPassword!default.jspa?name=root");
              tester.setFormElement("password", "newpassword");
              tester.setFormElement("confirm", "newpassword");
              tester.clickButton("update_submit");
              text.assertTextNotPresent(locator.page(), "has successfully been set ");
              assertions.getJiraFormAssertions().assertFormErrMsg("Must be a System Administrator to reset a System Administrator's password");

              // but I should be able to set mine
              tester.gotoPage("/secure/admin/user/ViewUser.jspa?name=admin");
              text.assertTextPresent(locator.page(), "Set Password");
              tester.gotoPage("secure/admin/user/SetPassword!default.jspa?name=admin");
              tester.setFormElement("password", "newpassword");
              tester.setFormElement("confirm", "newpassword");
              tester.clickButton("update_submit");
              assertions.getJiraFormAssertions().assertFormSuccessMsg("has successfully been set");
          }
          finally
          {
              navigation.logout();
              // go back to sysadmin user
              navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
              administration.restoreBlankInstance();
          }
      }

    public void testAdminUsersCanAccessTheServicesAdministrationPage()
    {
        try
        {
            administration.restoreData("TestWithSystemAdmin.xml");
            administration.services().goTo();
            assertThatTheCurrentPageIsViewServices();
        }
        finally
        {
            navigation.logout();
            // go back to sysadmin user
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            administration.restoreBlankInstance();
        }
    }

    public void testSysAdminUsersCanAccessTheServicesAdministrationPage()
    {
        try
        {
            administration.restoreData("TestWithSystemAdmin.xml");
            navigation.logout();
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);

            administration.services().goTo();
            assertThatTheCurrentPageIsViewServices();
        }
        finally
        {
            navigation.logout();
            // go back to sysadmin user
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            administration.restoreBlankInstance();
        }
    }

    public void testSysAdminUsersCanViewAllServices() throws Exception
    {
        try
        {
            administration.restoreData("TestWithSystemAdmin.xml");
            Set<ViewServices.Service> expectedListOfServices =
                    ImmutableSet.of
                            (
                                    new ViewServices.Service("An IMAP Service", "com.atlassian.jira.service.services.imap.ImapService"),
                                    new ViewServices.Service("A Debugging Service", "com.atlassian.jira.service.services.DebugService"),
                                    new ViewServices.Service("Backup JIRA", "com.atlassian.jira.service.services.export.ExportService"),
                                    new ViewServices.Service("A Pop Service", "com.atlassian.jira.service.services.pop.PopService"),
                                    new ViewServices.Service("Service Provider Token Remover", "com.atlassian.sal.jira.scheduling.JiraPluginSchedulerService"),
                                    new ViewServices.Service("Mail Queue Service", "com.atlassian.jira.service.services.mail.MailQueueService")
                            );

            navigation.logout();
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);

            Set<ViewServices.Service> actualListOfServices = administration.services().goTo().list();
            assertTrue(expectedListOfServices.equals(actualListOfServices));
        }
        finally
        {
            navigation.logout();
            // go back to sysadmin user
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            administration.restoreBlankInstance();
        }
    }

    public void testAdminUsersCanOnlySeePopAndImapServices() throws Exception
    {
        try
        {
            administration.restoreData("TestWithSystemAdmin.xml");
            Set<ViewServices.Service> expectedListOfServices =
                    ImmutableSet.of
                            (
                                    new ViewServices.Service("An IMAP Service", "com.atlassian.jira.service.services.imap.ImapService"),
                                    new ViewServices.Service("A Pop Service", "com.atlassian.jira.service.services.pop.PopService")
                            );

            Set<ViewServices.Service> actualListOfServices = administration.services().goTo().list();
            assertTrue(expectedListOfServices.equals(actualListOfServices));
        }
        finally
        {
            navigation.logout();
            // go back to sysadmin user
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            administration.restoreBlankInstance();
        }
    }

    public void testSysAdminUsersCanAddABuiltInServiceThatIsNeitherAnImapNorPop() throws Exception
    {
        try
        {
            administration.restoreData("TestWithSystemAdmin.xml");
            ViewServices.Service serviceToAdd = new ViewServices.Service("Another Backup JIRA", "com.atlassian.jira.service.services.export.ExportService");

            navigation.logout();
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);

            administration.services().goTo().add(serviceToAdd, "10");

            Set<ViewServices.Service> existingServices = administration.services().goTo().list();
            assertTrue(existingServices.contains(serviceToAdd));
        }
        finally
        {
            navigation.logout();
            // go back to sysadmin user
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            administration.restoreBlankInstance();
        }
    }

    public void testAdminUsersCanOnlyAddPopAndImapServices() throws Exception
    {
        try
        {
            administration.restoreData("TestWithSystemAdmin.xml");
            ViewServices.Service serviceToAdd = new ViewServices.Service("Another Backup JIRA", "com.atlassian.jira.service.services.export.ExportService");

            try
            {
                administration.services().goTo().add(serviceToAdd, "10");
                fail("Jira Administrators should only be able to add Pop And Imap Services");
            }
            catch (ViewServices.UnableToAddServiceException e)
            {
                text.assertTextPresent(locator.page(), "Access Denied");
            }
        }
        finally
        {
            navigation.logout();
            // go back to sysadmin user
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            administration.restoreBlankInstance();
        }
    }

    public void testAdminUsersCanOnlySeePopServers() throws Exception
    {
        try
        {
            administration.restoreData("TestWithSystemAdmin.xml");
            MailServerAdministration.MailServerConfiguration expectedPopConfiguration =
                    new MailServerAdministration.
                            MailServerConfiguration("Dummy POP Server", "dummy", "dummy");

            assertFalse(administration.mailServers().goTo().Smtp().isPresent());

            assertTrue(administration.mailServers().goTo().Pop().isPresent());

            List<MailServerAdministration.MailServerConfiguration>
                    actualListOfConfiguredPopServers = administration.mailServers().goTo().Pop().list();

            assertEquals(actualListOfConfiguredPopServers, ImmutableList.of(expectedPopConfiguration));
        }
        finally
        {
            navigation.logout();
            // go back to sysadmin user
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            administration.restoreBlankInstance();
        }
    }

    public void testSysAdminUsersCanSeeSmtpAndPopServers() throws Exception
    {
        try
        {
            administration.restoreData("TestWithSystemAdmin.xml");
            MailServerAdministration.MailServerConfiguration expectedPopConfiguration =
                    new MailServerAdministration.
                            MailServerConfiguration("Dummy POP Server", "dummy", "dummy");

            navigation.logout();
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);

            assertTrue(administration.mailServers().goTo().Smtp().isPresent());
            assertFalse(administration.mailServers().goTo().Smtp().isConfigured());

            assertTrue(administration.mailServers().goTo().Pop().isPresent());

            List<MailServerAdministration.MailServerConfiguration>
                    actualListOfConfiguredPopServers = administration.mailServers().goTo().Pop().list();

            assertEquals(actualListOfConfiguredPopServers, ImmutableList.of(expectedPopConfiguration));
        }
        finally
        {
            navigation.logout();
            // go back to sysadmin user
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            administration.restoreBlankInstance();
        }
    }

    public void testAdminUsersCanAddAPopServer() throws Exception
    {
        try
        {
            administration.restoreData("TestWithSystemAdmin.xml");
            MailServerAdministration.MailServerConfiguration mailServerConfigurationToBeAdded =
                    new MailServerAdministration.MailServerConfiguration("New Pop Server", "dummy", "dummy");

            administration.
                    mailServers().goTo().Pop().add
                    (
                            mailServerConfigurationToBeAdded.getName(),
                            mailServerConfigurationToBeAdded.getHostName(),
                            mailServerConfigurationToBeAdded.getUserName(),
                            "dummy"
                    );

            List<MailServerAdministration.MailServerConfiguration>
                    actualListOfConfiguredPopServers = administration.mailServers().goTo().Pop().list();

            assertTrue(actualListOfConfiguredPopServers.contains(mailServerConfigurationToBeAdded));
        }
        finally
        {
            navigation.logout();
            // go back to sysadmin user
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            administration.restoreBlankInstance();
        }
    }

    public void testAdminUsersCanEditAPopServer() throws Exception
    {
        try
        {
            administration.restoreData("TestWithSystemAdmin.xml");
            final MailServerAdministration.MailServerConfiguration expectedUpdatedMailServerConfiguration =
                    new MailServerAdministration.MailServerConfiguration("Dummy POP Server", "PIRATES.AYE.AYE.COM", "Barbarossa");

            administration.mailServers().goTo().Pop().
                    edit("Dummy POP Server").
                        setHostName("PIRATES.AYE.AYE.COM").
                        setUserName("Barbarossa").
                        setPassword("secure-password").
                    update();

            final List<MailServerAdministration.MailServerConfiguration>
                    actualListOfConfiguredPopServers = administration.mailServers().goTo().Pop().list();

            assertTrue(Iterables.contains(actualListOfConfiguredPopServers, expectedUpdatedMailServerConfiguration));
        }
        finally
        {
            navigation.logout();
            // go back to sysadmin user
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            administration.restoreBlankInstance();
        }
    }

    public void testAdminUsersCanDeleteAPopServer() throws Exception
    {
        try
        {
            administration.restoreData("TestWithSystemAdmin.xml");
            administration.mailServers().goTo().Pop().delete("Dummy POP Server");

            final List<MailServerAdministration.MailServerConfiguration>
                    actualListOfConfiguredPopServers = administration.mailServers().goTo().Pop().list();

            // There is only one pop server in the data so the list of pop servers should be empty now.
            assertTrue(Iterables.isEmpty(actualListOfConfiguredPopServers));
        }
        finally
        {
            navigation.logout();
            // go back to sysadmin user
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            administration.restoreBlankInstance();
        }
    }

    public void testAdminCanNotAccessSendTestMailByUrlHacking() throws Exception
    {
        try
        {
            administration.restoreData("TestWithSystemAdmin.xml");
            tester.gotoPage("secure/admin/SendTestMail!default.jspa");
            text.assertTextPresent(locator.css(".aui-message.warning"), "'Administrator' does not have permission to access this page");
        }
        finally
        {
            navigation.logout();
            // go back to sysadmin user
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            administration.restoreBlankInstance();
        }
    }

    private void assertThatTheCurrentPageIsViewServices()
    {
        text.assertTextSequence(locator.css(VIEW_SERVICES_PAGE_FORM_TITLES_LOCATOR), "Services", "Add Service");
    }

    private void createNewWorkflowSchemeAndGotoAssociateProject()
    {
        navigation.gotoAdmin();
        administration.workflows().goTo().copyWorkflow("jira", "Copy of jira");

        tester.clickLink("workflow_schemes");
        tester.clickLink("add_workflowscheme");
        tester.setFormElement("name", "Test");
        tester.submit("Add");
        tester.clickLinkWithText("Assign a workflow");
        tester.selectOption("workflow", "Copy of jira");
        tester.submit("Add");

        // Goto activate the workflow
        tester.gotoPage("/plugins/servlet/project-config/" + "HSP" + "/workflows");
        tester.clickLink("project-config-workflows-scheme-change");
    }
}
