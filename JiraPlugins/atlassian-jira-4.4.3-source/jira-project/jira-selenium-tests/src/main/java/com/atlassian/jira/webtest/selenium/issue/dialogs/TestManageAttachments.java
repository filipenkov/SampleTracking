package com.atlassian.jira.webtest.selenium.issue.dialogs;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.Permissions;
import com.atlassian.selenium.Browser;
import com.atlassian.selenium.SkipInBrowser;
import junit.framework.Test;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;

/**
 * These tests were essentially moved to selenium from func tests
 * due to the inline attachments work.
 *
 * @since v4.2
 */

@SkipInBrowser (browsers={ Browser.IE}) // skipping in IE because it does not allow us to attach files programmatically for security reasons
@WebTest({Category.SELENIUM_TEST })
public class TestManageAttachments extends AbstractIssueDialogTest
{
    public static Test suite()
    {
        return suiteFor(TestManageAttachments.class);
    }

    public void onSetUp()
    {
        super.onSetUp();
        restoreData("TestDeleteAttachments.xml");
        getAdministration().enableAttachments();
    }

    public void testDeleteOwnAttachmentWithOwnPermission()
    {
        getNavigator().logout(getXsrfToken());
        // Login as simple user (with "Delete Own Attachment" permission) and create an attachment as that user
        getNavigator().login("detkin", "detkin");
        getNavigator().issue().viewIssue("TST-1").attachFile("tempFilename", "somefile.txt", 1024);

        gotoAttachmentDeletionScreen("10010", "somefile.txt");

        // Attempt to delete own attachment
        client.click("Delete", true);

        assertThat.textNotPresent("Remove Attachment:");
        assertThat.elementNotPresent("id=del_10010");
    }

    public void testDeleteAttachmentWithAllPermission()
    {
        getNavigator().logout(getXsrfToken());
        // Login as simple user (with "Delete Own Attachment" permission) and create an attachment as that user
        getNavigator().login("detkin", "detkin");
        getNavigator().issue().viewIssue("TST-1").attachFile("tempFilename", "somefile.txt", 1024);

        // Login as an administrator (with the "Delete All Attachments" permission) and attempt to delete the created attachment
        getNavigator().logout(getXsrfToken());
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
        getNavigator().issue().viewIssue("TST-1");
        gotoAttachmentDeletionScreen("10010", "somefile.txt");
        client.click("Delete", true);

        // Assert attachment has been deleted
        assertThat.textNotPresent("Remove Attachment:");
        assertThat.elementNotPresent("id=del_10010");
    }

    public void testDeleteOthersAttachmentWithOwnPermission()
    {
        getNavigator().logout(getXsrfToken());
        // Login as simple user and create an attachment as that user
        getNavigator().login("detkin", "detkin");
        getNavigator().issue().viewIssue("TST-1").attachFile("tempFilename", "somefile.txt", 1024);

        // Login as another user (with the "Delete Own Attachment" permission) and assert that the delete link is hidden
        getNavigator().logout(getXsrfToken());
        getNavigator().login("barney", "barney");
        getNavigator().issue().viewIssue("TST-1");
        client.click("manage-attachment-link", true);
        assertThat.textNotPresent("Remove Attachment:");
        assertThat.elementNotPresent("id=del_10010");
    }

    public void testManageAttachmentLinkShowsWithNoCreatePermAndOnlyDeleteOwn()
    {
        getNavigator().logout(getXsrfToken());
        // Login as simple user (with "Delete Own Attachment" permission) and create an attachment as that user
        getNavigator().login("detkin", "detkin");
        getNavigator().issue().viewIssue("TST-1").attachFile("tempFilename", "somefile.txt", 1024);

        // Delete the create attachment permission
        getNavigator().logout(getXsrfToken());
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
        getAdministration().removeRolePermission(Permissions.CREATE_ATTACHMENT, 10000);

        getNavigator().logout(getXsrfToken());
        // Login as simple user (with "Delete Own Attachment" permission) and create an attachment as that user
        getNavigator().login("detkin", "detkin");

        getNavigator().issue().viewIssue("TST-1");
        assertThat.linkPresentWithText("Manage Attachments");
    }

    public void testManageAttachmentLinkHiddenWithNoCreatePermAndOnlyDeleteOwnNoAuthoredAttachment()
    {
        getNavigator().logout(getXsrfToken());
        // Login as simple user (with "Delete Own Attachment" permission) and create an attachment as that user
        getNavigator().login("detkin", "detkin");
        getNavigator().issue().viewIssue("TST-1").attachFile("tempFilename", "somefile.txt", 1024);

        // Delete the create attachment permission
        getNavigator().logout(getXsrfToken());
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
        getAdministration().removeRolePermission(Permissions.CREATE_ATTACHMENT, 10000);

        getNavigator().logout(getXsrfToken());
        // Login as simple user (with "Delete Own Attachment" permission) and create an attachment as that user
        getNavigator().login("barney", "barney");

        getNavigator().issue().viewIssue("TST-1");
        assertThat.elementNotPresent("id=manage-attachment-link");
        assertThat.elementNotPresent("id=add-attachments-link");
    }

    public void testManageAttachmentLinkShowsWithNoCreatePermAndDeleteAllNoAuthoredAttachment()
    {
        getNavigator().logout(getXsrfToken());
        // Login as simple user (with "Delete Own Attachment" permission) and create an attachment as that user
        getNavigator().login("detkin", "detkin");
        getNavigator().issue().viewIssue("TST-1").attachFile("tempFilename", "somefile.txt", 1024);

        // Delete the create attachment permission
        getNavigator().logout(getXsrfToken());
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
        getAdministration().removeRolePermission(Permissions.CREATE_ATTACHMENT, 10000);

        getNavigator().issue().viewIssue("TST-1");
        assertThat.linkPresentWithText("Manage Attachments");
    }

    public void testAttachmentLinksPresentWhenUserHasCreatePermission()
    {
        getNavigator().issue().viewIssue("TST-1").attachFile("tempFilename", "somefile.txt", 1024);
        assertThat.elementPresent("id=attach-file");
        assertThat.linkPresentWithText("Manage Attachments");
        client.click("id=manage-attachment-link", true);
        assertThat.linkPresentWithText("Attach More Files");
        assertThat.linkPresentWithText("Delete");
    }

    public void testAddAttachmentLinksNotPresentWhenUserHasNoCreatePermission()
    {
        getNavigator().issue().viewIssue("TST-1").attachFile("tempFilename", "somefile.txt", 1024);

        // Remove the create attachments permission
        getAdministration().removeRolePermission(Permissions.CREATE_ATTACHMENT, 10000);

        getNavigator().issue().viewIssue("TST-1");
        assertThat.elementNotPresent("id=attach-file");
        assertThat.linkPresentWithText("Manage Attachments");
        client.click("id=manage-attachment-link", true);
        assertThat.linkNotPresentWithText("Attach More Files");
        assertThat.linkPresentWithText("Delete");
    }

    public void testManageAttachmentsNoAttachments()
    {
        getNavigator().gotoPage("secure/ManageAttachments.jspa?id=10000", true);
        assertThat.textPresent("There are no attachments.");
        getNavigator().issue().viewIssue("TST-1").attachFile("tempFilename", "somefile.txt", 1024);
        getNavigator().gotoPage("secure/ManageAttachments.jspa?id=10000", true);
        assertThat.textPresent("Manage Attachments");
        assertThat.textNotPresent("There are no attachments.");
    }

    public void testModifyAttachmentLinksNotPresentWhenIssueInNonEditableWorkflowState()
    {
        getNavigator().issue().viewIssue("TST-1").attachFile("tempFilename", "somefile.txt", 1024);

        // Push issue into non-editable workflow state
        client.clickLinkWithText("Close Issue", false);
        assertDialogIsOpenAndReady();
        client.click("id=issue-workflow-transition-submit", true);
        client.waitForPageToLoad();

        assertThat.elementNotPresent("id=attach-file");
        assertThat.linkPresentWithText("Manage Attachments");
        client.click("manage-attachment-link", true);
        assertThat.linkNotPresentWithText("Attach More Files");
        assertThat.linkNotPresentWithText("Delete");
    }

    public void testAttachFileOnReopen()
    {
        //add the attachment field to the workflow screen.
        getNavigator().gotoAdmin();
        client.click("id=field_screens", true);
        client.click("id=configure_fieldscreen_Workflow Screen", true);
        client.select("fieldId", "Attachment");
        client.click("id=add_field_submit", true);

        //now close an issue
        getNavigator().issue().viewIssue("TST-1");
        client.clickLinkWithText("Close Issue", false);
        assertDialogIsOpenAndReady();
        client.click("id=issue-workflow-transition-submit", true);
        client.waitForPageToLoad();

        //reopen the issue and make sure attaching a file works!
        client.clickLinkWithText("Reopen Issue", false);
        assertDialogIsOpenAndReady();
        getNavigator().issue().attachFileInExistingForm("tempFilename", "myawesomefile.txt", 1024);
        client.click("id=issue-workflow-transition-submit", true);
        client.waitForPageToLoad();

        //check the issue was reopend and the file was attached!
        assertThat.linkPresentWithText("Close Issue");
        assertThat.linkPresentWithText("myawesomefile.txt");
    }

    public void testManageAttachmentsWithNoPermissions()
    {
        final File image = new File(getEnvironmentData().getXMLDataLocation(), "xss_exploit_files/1179826282.png");
        getNavigator().issue().viewIssue("TST-1").attachFile("tempFilename", image.getAbsolutePath(), 1024);

        // if they view the issue they should have a link to Manage Attachments
        assertThat.linkPresentWithText("Manage Attachments");

        //login as user with no attachment permissions
        getNavigator().logout(getXsrfToken());
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
        getAdministration().removeRolePermission(Permissions.CREATE_ATTACHMENT, 10000);
        getAdministration().removeRolePermission(Permissions.ATTACHMENT_DELETE_ALL, 10000);
        getAdministration().removeRolePermission(Permissions.ATTACHMENT_DELETE_OWN, 10000);
        getNavigator().logout(getXsrfToken());
        getNavigator().login("barney", "barney");

        // if they view the issue they should NOT have a link to Manage Attachments
        getNavigator().issue().viewIssue("TST-1");
        assertThat.elementNotPresent("id=manage-attachment-link");
        assertThat.elementNotPresent("id=add-attachments-link");

        //user can jump to the manage attachments page, but not execute any actions there
        getNavigator().gotoPage("/secure/ManageAttachments.jspa?id=10000", true);

        assertThat.linkNotPresentWithText("Attach More Files");
        assertThat.linkNotPresentWithText("Delete");
    }

    public void testManageAttachmentsWhenNotLoggedIn()
    {
        getNavigator().issue().viewIssue("TST-1").attachFile("tempFilename", "somefile.txt", 1024);

        //login as user with no attachment permissions
        getNavigator().logout(getXsrfToken());

        //user can jump to the manage attachments page, but not execute any actions there
        getNavigator().gotoPage("/secure/ManageAttachments.jspa?id=10000", true);

        assertThat.textNotPresent("This page allows you to manage the attachments for a particular issue");
        assertThat.textPresent("You must log in to access this page");
    }

    public void testCreateAttachmentsWithNoPermissions()
    {
        getNavigator().issue().viewIssue("TST-1").attachFile("tempFilename", "somefile.txt", 1024);

        //login as user with no attachment permissions
        getNavigator().logout(getXsrfToken());
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
        getAdministration().removeRolePermission(Permissions.CREATE_ATTACHMENT, 10000);
        getAdministration().removeRolePermission(Permissions.ATTACHMENT_DELETE_ALL, 10000);
        getAdministration().removeRolePermission(Permissions.ATTACHMENT_DELETE_OWN, 10000);
        getNavigator().logout(getXsrfToken());
        getNavigator().login("barney", "barney");

        getNavigator().gotoPage("/secure/AttachFile!default.jspa?id=10000", true);
        assertThat.textPresent("You do not have permission to create attachments for this issue.");
        assertThat.elementNotPresent("Attach");
    }

    public void testDeleteAttachmentsWithNoPermissions()
    {
        getNavigator().issue().viewIssue("TST-1").attachFile("tempFilename", "somefile.txt", 1024);
        //login as user with no attachment permissions

        getNavigator().logout(getXsrfToken());
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
        getAdministration().removeRolePermission(Permissions.CREATE_ATTACHMENT, 10000);
        getAdministration().removeRolePermission(Permissions.ATTACHMENT_DELETE_ALL, 10000);
        getAdministration().removeRolePermission(Permissions.ATTACHMENT_DELETE_OWN, 10000);
        getNavigator().logout(getXsrfToken());
        getNavigator().login("barney", "barney");

        //user can jump to the delete attachments page, but not execute the delete action
        getNavigator().gotoPage("/secure/DeleteAttachment!default.jspa?id=10000&deleteAttachmentId=10010", true);
        client.click("Delete", true);
        assertThat.textPresent("You do not have permission to delete attachment with id: 10010");
    }

    // JRA-14575: when an attachment is requested and the user is not logged in, return an error page which allows the
    // user to log in and will then redirect them back to the attachment

    public void testViewAttachmentsWithNoPermissions() throws IOException, SAXException
    {
        restoreData("TestViewAttachmentServletPermissionViolation.xml");
        getAdministration().enableAttachments();
        getNavigator().issue().viewIssue("HSP-1").attachFile("tempFilename", "DummyAttachment.txt", 1024);

        //login as user with no permissions to browse the project
        getNavigator().logout(getXsrfToken());
        getNavigator().login("joe");

        // now check that they cant get a ZIP view of the attachments
        getNavigator().gotoPage("secure/attachmentzip/10020.zip", true);
        assertThat.textPresent("Access Denied");
        assertThat.textPresent("It seems that you have tried to perform an operation which you are not permitted to perform.");

        // check that we cant download the actual attachment
        getNavigator().gotoPage("/secure/attachment/10020/DummyAttachment.txt", true);
        assertThat.textPresent("Access Denied");
        assertThat.textPresent("It seems that you have tried to perform an operation which you are not permitted to perform.");
        assertThat.textNotPresent("You cannot view this URL as a guest");
        assertThat.linkNotPresentWithText("Login");
        // os_destination parameter is the encoded path to the attachment
        assertFalse(client.getHtmlSource().contains("os_destination=%2Fsecure%2Fattachment%2F10020%2FDummyAttachment.txt"));

        // security breach page must not be cached!
        getWebUnitTest().login("joe");
        getWebUnitTest().gotoPage("/secure/attachment/10020/DummyAttachment.txt");
        String header = getWebUnitTest().getDialog().getResponse().getHeaderField("cache-control");
        assertNotNull(header);
        assertTrue(header.indexOf("no-cache") != -1);

        // try anonymous access - should get a login link
        getNavigator().gotoHome();
        getNavigator().logout(getXsrfToken());

        // now check that they cant get a ZIP view of the attachments
        getNavigator().gotoPage("secure/attachmentzip/10020.zip", true);
        assertThat.textPresent("You must log in to access this page");

        // check that we cant download the actual attachment
        getNavigator().gotoPage("/secure/attachment/10020/DummyAttachment.txt", true);
        assertThat.textPresent("You must log in to access this page");

        // security breach page must not be cached!
        getWebUnitTest().gotoPage("/secure/attachment/10020/DummyAttachment.txt");
        header = getWebUnitTest().getDialog().getResponse().getHeaderField("cache-control");
        assertNotNull(header);
        assertTrue(header.indexOf("no-cache") != -1);

        // login as a user who can see the attachment
        client.type("os_username", ADMIN_USERNAME);
        client.type("os_password", ADMIN_PASSWORD);
        client.click("id=login-form-submit", true);

        assertThat.textPresent("xxxxxxxxx");
    }

    public void testAttachmentLinksNotPresentWhenIssueOperationPluginDisabled()
    {
        final String pluginId = "355871397";
        try
        {
            getNavigator().issue().viewIssue("TST-1").attachFile("tempFilename", "somefile.txt", 1024);
            assertThat.linkPresentWithText("Manage Attachments");
            client.click("id=manage-attachment-link", true);
            assertThat.linkPresentWithText("Attach More Files");

            // Disable the attach-file issue operation
            getAdministration().disablePluginModule(pluginId, "View Issue Ops Bar Attach Files Link");

            getNavigator().issue().viewIssue("TST-1");
            assertThat.linkPresentWithText("Manage Attachments");
            client.click("id=manage-attachment-link", true);
            assertThat.linkNotPresentWithText("Attach More Files");
        }
        finally
        {
            getAdministration().enablePluginModule(pluginId, "View Issue Ops Bar Attach Files Link");
        }
    }

    /**
     * Check that if you don't have permission to attach a file that the attachment error message will
     * not be shown on the create comment box. JRA-13496.
     */
    public void testViewIssueWithCommentDivAndNoAttachmentPermission()
    {
        restoreData("TestViewIssueWithCommentDivAndNoAttachmentPermission.xml");

        getAdministration().enableAttachments();
        final String issueKey = "TST-1";

        // Attach a file
        File file = new File(getEnvironmentData().getXMLDataLocation().getAbsolutePath() + "/picture-attachment.jpg");
        getNavigator().issue().viewIssue(issueKey).attachFile("tempFilename", file.getAbsolutePath(), 0);

        // Modify the permission scheme so that we have no attachment permission
        getAdministration().removeRolePermission(Permissions.ATTACHMENT_DELETE_ALL, 10002);
        getAdministration().removeRolePermission(Permissions.ATTACHMENT_DELETE_OWN, 10000);
        getAdministration().removeRolePermission(Permissions.CREATE_ATTACHMENT, 10000);

        getNavigator().issue().viewIssue(issueKey);
        assertThat.textNotPresent("You do not have permission to manage the attachments for this issue.");
    }


    private void gotoAttachmentDeletionScreen(String attachmentId, String fileName)
    {
        client.click("manage-attachment-link", true);
        client.click("del_" + attachmentId, true);
        assertThat.textPresent("Remove Attachment: " + fileName);
    }
}
