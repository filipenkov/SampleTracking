package com.atlassian.jira.webtest.selenium.issue;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.auidialog.AbstractAuiDialogTest;
import com.atlassian.jira.webtest.selenium.harness.util.IssueNavigation;
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
public class TestManageAttachments extends AbstractAuiDialogTest
{
    private static final String MGE_ATTACH_LINK_LOCATOR = "id=manage-attachment-link";
    private static final String MGE_ATTACH_DIALOG_LOCATOR = "id=manage-attachment-dialog";
    private static final String VIEW_ISSUE_LOCATOR = "id=comment-issue";
    private static final String TEST_ISSUE_KEY = "TST-1";
    private static final String DYLAN_USER = "detkin";
    private static final String FILE_PARAM = "tempFilename";
    private static final String BARNEY_USER = "barney";
    private static final String JOE_USER = "joe";

    public static Test suite()
    {
        return suiteFor(TestManageAttachments.class);
    }

    public void onSetUp()
    {
        super.onSetUp();
        restoreData("TestDeleteAttachments.xml");
    }

    public void testDeleteOwnAttachmentWithOwnPermission()
    {
        // Login as simple user (with "Delete Own Attachment" permission) and create an attachment as that user
        getNavigator().login(DYLAN_USER);

        //Test + Images because this is now different markup.
        IssueNavigation issueNavigation = getNavigator().issue().viewIssue(TEST_ISSUE_KEY);
        issueNavigation.attachFile(FILE_PARAM, "somefile.txt", 1024);
        issueNavigation.attachFile(FILE_PARAM, "somefile2.txt", 1024);

        File screenshot = getScreenshot();
        issueNavigation.attachFile(FILE_PARAM, screenshot.getAbsolutePath(), 1024);
        issueNavigation.attachFile(FILE_PARAM, screenshot.getAbsolutePath(), 1024);

        assertCancelDeleteAttachmentOnViewIssue(10010);
        gotoManageAttachments();
        assertCancelDeleteAttachmentOnManage(10011);

        assertDeleteAttachmentOnViewIssue(10010);
        assertDeleteAttachmentOnViewIssue(10012);

        gotoManageAttachments();
        assertDeleteAttachmentOnManage(10011);
        assertDeleteAttachmentOnManage(10013);
    }

    public void testDeleteAttachmentWithAllPermission()
    {
        // Login as simple user (with "Delete Own Attachment" permission) and create an attachment as that user
        getNavigator().login(DYLAN_USER);

        //Test + Images because this is now different markup.
        IssueNavigation issueNavigation = getNavigator().issue().viewIssue(TEST_ISSUE_KEY);
        issueNavigation.attachFile(FILE_PARAM, "somefile.txt", 1024);
        issueNavigation.attachFile(FILE_PARAM, "somefile2.txt", 1024);

        issueNavigation.attachFile(FILE_PARAM, getScreenshot().getAbsolutePath(), 1024);
        issueNavigation.attachFile(FILE_PARAM, getScreenshot2().getAbsolutePath(), 1024);

        // Login as an administrator (with the "Delete All Attachments" permission) and attempt to delete the created attachment
        getNavigator().logout(getXsrfToken());
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
        issueNavigation.viewIssue(TEST_ISSUE_KEY);

        assertDeleteAttachmentOnViewIssue(10010);
        assertDeleteAttachmentOnViewIssue(10012);

        gotoManageAttachments();
        assertDeleteAttachmentOnManage(10011);
        assertDeleteAttachmentOnManage(10013);
    }

    public void testDeleteOthersAttachmentWithOwnPermission()
    {
        // Login as simple user and create an attachment as that user
        getNavigator().login(DYLAN_USER);
        IssueNavigation issueNavigation = getNavigator().issue();
        issueNavigation.viewIssue(TEST_ISSUE_KEY).attachFile(FILE_PARAM, "somefile.txt", 1024);
        issueNavigation.attachFile(FILE_PARAM, getScreenshot().getAbsolutePath(), -1);

        // Login as another user (with the "Delete Own Attachment" permission) and assert that the delete link is hidden
        getNavigator().logout(getXsrfToken());
        getNavigator().login(BARNEY_USER);

        issueNavigation.viewIssue(TEST_ISSUE_KEY);
        assertThat.elementNotPresent(createDeleteLinkLocator(10010));
        assertThat.elementNotPresent(createDeleteLinkLocator(10011));

        gotoManageAttachments();
        assertThat.elementNotPresent(createDeleteLinkLocator(10010));
        assertThat.elementNotPresent(createDeleteLinkLocator(10011));
    }

    public void testManageAttachmentLinkShowsWithNoCreatePermAndOnlyDeleteOwn()
    {
        // Login as simple user (with "Delete Own Attachment" permission) and create an attachment as that user
        getNavigator().login(DYLAN_USER);
        getNavigator().issue().viewIssue(TEST_ISSUE_KEY).attachFile(FILE_PARAM, "somefile.txt", 1024);

        // Delete the create attachment permission
        getNavigator().logout(getXsrfToken());
        getNavigator().login(ADMIN_USERNAME);
        getAdministration().removeRolePermission(Permissions.CREATE_ATTACHMENT, 10000);

        getNavigator().logout(getXsrfToken());
        // Login as simple user (with "Delete Own Attachment" permission) and create an attachment as that user
        getNavigator().login(DYLAN_USER);

        getNavigator().issue().viewIssue(TEST_ISSUE_KEY);
        assertThat.linkPresentWithText("Manage Attachments");
    }

    public void testManageAttachmentLinkHiddenWithNoCreatePermAndOnlyDeleteOwnNoAuthoredAttachment()
    {
        // Login as simple user (with "Delete Own Attachment" permission) and create an attachment as that user
        getNavigator().login(DYLAN_USER);
        getNavigator().issue().viewIssue(TEST_ISSUE_KEY).attachFile(FILE_PARAM, "somefile.txt", 1024);

        // Delete the create attachment permission
        getNavigator().logout(getXsrfToken());
        getNavigator().login(ADMIN_USERNAME);
        getAdministration().removeRolePermission(Permissions.CREATE_ATTACHMENT, 10000);

        getNavigator().logout(getXsrfToken());
        // Login as simple user (with "Delete Own Attachment" permission) and create an attachment as that user
        getNavigator().login(BARNEY_USER);

        getNavigator().issue().viewIssue(TEST_ISSUE_KEY);
        assertThat.elementNotPresent(MGE_ATTACH_LINK_LOCATOR);
        assertThat.elementNotPresent("id=add-attachments-link");
    }

    public void testManageAttachmentLinkShowsWithNoCreatePermAndDeleteAllNoAuthoredAttachment()
    {
        // Login as simple user (with "Delete Own Attachment" permission) and create an attachment as that user
        getNavigator().login(DYLAN_USER);
        getNavigator().issue().viewIssue(TEST_ISSUE_KEY).attachFile(FILE_PARAM, "somefile.txt", 1024);

        // Delete the create attachment permission
        getNavigator().logout(getXsrfToken());
        getNavigator().login(ADMIN_USERNAME);
        getAdministration().removeRolePermission(Permissions.CREATE_ATTACHMENT, 10000);

        getNavigator().issue().viewIssue(TEST_ISSUE_KEY);
        assertThat.linkPresentWithText("Manage Attachments");
    }

    public void testAttachmentLinksPresentWhenUserHasCreatePermission()
    {
        getNavigator().login(ADMIN_USERNAME);
        getNavigator().issue().viewIssue(TEST_ISSUE_KEY).attachFile(FILE_PARAM, "somefile.txt", 1024);
        assertThat.elementPresent("id=attach-file");
        assertThat.linkPresentWithText("Manage Attachments");
    }

    public void testAddAttachmentLinksNotPresentWhenUserHasNoCreatePermission()
    {
        getNavigator().issue().viewIssue(TEST_ISSUE_KEY).attachFile(FILE_PARAM, "somefile.txt", 1024);

        // Remove the create attachments permission
        getAdministration().removeRolePermission(Permissions.CREATE_ATTACHMENT, 10000);

        getNavigator().issue().viewIssue(TEST_ISSUE_KEY);
        assertThat.elementNotPresent("id=attach-file");
    }

    public void testManageAttachmentsNoAttachments()
    {
        getNavigator().gotoPage("secure/ManageAttachments.jspa?id=10000", true);
        assertThat.textPresent("There are no attachments.");
        getNavigator().issue().viewIssue(TEST_ISSUE_KEY).attachFile(FILE_PARAM, "somefile.txt", 1024);
        getNavigator().gotoPage("secure/ManageAttachments.jspa?id=10000", true);
        assertThat.textPresent("Manage Attachments");
        assertThat.textNotPresent("There are no attachments.");
    }

    public void testModifyAttachmentLinksNotPresentWhenIssueInNonEditableWorkflowState()
    {
        getNavigator().issue().viewIssue(TEST_ISSUE_KEY).attachFile(FILE_PARAM, "somefile.txt", 1024);

        // Push issue into non-editable workflow state
        client.clickLinkWithText("Close Issue", false);
        assertDialogIsOpenAndReady();
        client.click("id=issue-workflow-transition-submit");
        waitForContentUpdate();

        assertThat.elementNotPresent("id=attach-file");
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
        getNavigator().issue().viewIssue(TEST_ISSUE_KEY);
        client.clickLinkWithText("Close Issue", false);
        assertDialogIsOpenAndReady();
        client.click("id=issue-workflow-transition-submit");
        waitForContentUpdate();

        //reopen the issue and make sure attaching a file works!
        client.clickLinkWithText("Reopen Issue", false);
        assertDialogIsOpenAndReady();
        getNavigator().issue().attachFileInExistingForm(FILE_PARAM, "myawesomefile.txt", 1024);
        client.click("id=issue-workflow-transition-submit");
        waitForContentUpdate();

        //check the issue was reopend and the file was attached!
        assertThat.linkPresentWithText("Close Issue");
        assertThat.linkPresentWithText("myawesomefile.txt");
    }

    public void testManageAttachmentsWithNoPermissions()
    {
        final File image = new File(getEnvironmentData().getXMLDataLocation(), "xss_exploit_files/1179826282.png");
        getNavigator().issue().viewIssue(TEST_ISSUE_KEY).attachFile(FILE_PARAM, image.getAbsolutePath(), 1024);

        // if they view the issue they should have a link to Manage Attachments
        assertThat.linkPresentWithText("Manage Attachments");

        //login as user with no attachment permissions
        getNavigator().logout(getXsrfToken());
        getNavigator().login(ADMIN_USERNAME);
        getAdministration().removeRolePermission(Permissions.CREATE_ATTACHMENT, 10000);
        getAdministration().removeRolePermission(Permissions.ATTACHMENT_DELETE_ALL, 10000);
        getAdministration().removeRolePermission(Permissions.ATTACHMENT_DELETE_OWN, 10000);
        getNavigator().logout(getXsrfToken());
        getNavigator().login(BARNEY_USER);

        // if they view the issue they should NOT have a link to Manage Attachments
        getNavigator().issue().viewIssue(TEST_ISSUE_KEY);
        assertThat.elementNotPresent(MGE_ATTACH_LINK_LOCATOR);
        assertThat.elementNotPresent("id=add-attachments-link");

        //user can jump to the manage attachments page, but not execute any actions there
        getNavigator().gotoPage("/secure/ManageAttachments.jspa?id=10000", true);

        assertThat.linkNotPresentWithText("Attach More Files");
        assertThat.linkNotPresentWithText("Delete");
    }

    public void testManageAttachmentsWhenNotLoggedIn()
    {
        getNavigator().issue().viewIssue(TEST_ISSUE_KEY).attachFile(FILE_PARAM, "somefile.txt", 1024);

        //login as user with no attachment permissions
        getNavigator().logout(getXsrfToken());

        //user can jump to the manage attachments page, but not execute any actions there
        getNavigator().gotoPage("/secure/ManageAttachments.jspa?id=10000", true);

        assertThat.textNotPresent("This page allows you to manage the attachments for a particular issue");
        assertThat.textPresent("You must log in to access this page");
    }

    public void testCreateAttachmentsWithNoPermissions()
    {
        getNavigator().login(ADMIN_USERNAME);
        getNavigator().issue().viewIssue(TEST_ISSUE_KEY).attachFile(FILE_PARAM, "somefile.txt", 1024);

        //login as user with no attachment permissions
        getNavigator().logout(getXsrfToken());
        getNavigator().login(ADMIN_USERNAME);
        getAdministration().removeRolePermission(Permissions.CREATE_ATTACHMENT, 10000);
        getAdministration().removeRolePermission(Permissions.ATTACHMENT_DELETE_ALL, 10000);
        getAdministration().removeRolePermission(Permissions.ATTACHMENT_DELETE_OWN, 10000);
        getNavigator().logout(getXsrfToken());
        getNavigator().login(BARNEY_USER);

        getNavigator().gotoPage("/secure/AttachFile!default.jspa?id=10000", true);
        assertThat.textPresent("You do not have permission to create attachments for this issue.");
        assertThat.elementNotPresent("Attach");
    }

    public void testDeleteAttachmentsWithNoPermissions()
    {
        getNavigator().login(ADMIN_USERNAME);
        getNavigator().issue().viewIssue(TEST_ISSUE_KEY).attachFile(FILE_PARAM, "somefile.txt", 1024);
        //login as user with no attachment permissions

        getNavigator().logout(getXsrfToken());
        getNavigator().login(ADMIN_USERNAME);
        getAdministration().removeRolePermission(Permissions.CREATE_ATTACHMENT, 10000);
        getAdministration().removeRolePermission(Permissions.ATTACHMENT_DELETE_ALL, 10000);
        getAdministration().removeRolePermission(Permissions.ATTACHMENT_DELETE_OWN, 10000);
        getNavigator().logout(getXsrfToken());
        getNavigator().login(BARNEY_USER);

        //user can jump to the delete attachments page, but not execute the delete action
        getNavigator().gotoPage("/secure/DeleteAttachment.jspa?id=10000&deleteAttachmentId=10010&atl_token=" + getXsrfToken(), true);
        assertThat.textPresent("You do not have permission to delete attachment with id: 10010");
    }

    // JRA-14575: when an attachment is requested and the user is not logged in, return an error page which allows the
    // user to log in and will then redirect them back to the attachment

    public void testViewAttachmentsWithNoPermissions() throws IOException, SAXException
    {
        backdoor.dataImport().restoreData("TestViewAttachmentServletPermissionViolation.xml");
        getNavigator().login(ADMIN_USERNAME);
        getAdministration().enableAttachments();
        getNavigator().issue().viewIssue("HSP-1").attachFile(FILE_PARAM, "DummyAttachment.txt", 1024);

        //login as user with no permissions to browse the project
        getNavigator().logout(getXsrfToken());
        getNavigator().login(JOE_USER);

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

        // security breach page must not be cached!
        getWebUnitTest().login(JOE_USER);
        getWebUnitTest().gotoPage("/secure/attachment/10020/DummyAttachment.txt");
        String header = getWebUnitTest().getDialog().getResponse().getHeaderField("cache-control");
        assertNotNull(header);
        assertTrue(header.contains("no-cache"));

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
        assertTrue(header.contains("no-cache"));

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
            getNavigator().issue().viewIssue(TEST_ISSUE_KEY).attachFile(FILE_PARAM, "somefile.txt", 1024);
            assertThat.elementPresent("id=attach-file");

            // Disable the attach-file issue operation
            getAdministration().disablePluginModule(pluginId, "View Issue Ops Bar Attach Files Link");

            getNavigator().issue().viewIssue(TEST_ISSUE_KEY);
            assertThat.elementNotPresent("id=attach-file");
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
        final String issueKey = TEST_ISSUE_KEY;

        // Attach a file
        File file = new File(getEnvironmentData().getXMLDataLocation().getAbsolutePath() + "/picture-attachment.jpg");
        getNavigator().issue().viewIssue(issueKey).attachFile(FILE_PARAM, file.getAbsolutePath(), 0);

        // Modify the permission scheme so that we have no attachment permission
        getAdministration().removeRolePermission(Permissions.ATTACHMENT_DELETE_ALL, 10002);
        getAdministration().removeRolePermission(Permissions.ATTACHMENT_DELETE_OWN, 10000);
        getAdministration().removeRolePermission(Permissions.CREATE_ATTACHMENT, 10000);

        getNavigator().issue().viewIssue(issueKey);
        assertThat.textNotPresent("You do not have permission to manage the attachments for this issue.");
    }

    private void gotoManageAttachments()
    {
        client.click(MGE_ATTACH_LINK_LOCATOR);
        waitForContentUpdate();
        assertOnManageAttachment();
    }

    private void assertDeleteAttachmentOnManage(long id)
    {
        doDelete(id);
        assertOnManageAttachment();
    }

    private void assertOnManageAttachment()
    {
        //Still on the manage page.
        assertThat.elementPresent(MGE_ATTACH_DIALOG_LOCATOR);
    }

    private void assertDeleteAttachmentOnViewIssue(long id)
    {
        doDelete(id);
        assertOnViewIssue();
    }

    private void assertOnViewIssue()
    {
        assertThat.elementNotPresent(MGE_ATTACH_DIALOG_LOCATOR);
        assertThat.elementPresent(VIEW_ISSUE_LOCATOR);
    }

    private void assertCancelDeleteAttachmentOnViewIssue(long id)
    {
        doCancel(id);
        assertOnViewIssue();
    }

    private void assertCancelDeleteAttachmentOnManage(long id)
    {
        doCancel(id);
        assertOnViewIssue();
    }

    private void doCancel(long id)
    {
        String deleteLocator = createDeleteLinkLocator(id);
        openAndWaitForDeleteDialog(deleteLocator);
        client.click("id=delete-attachment-cancel");
        assertThat.elementPresent(deleteLocator);
        client.refresh();
        client.waitForPageToLoad();
        assertThat.elementPresent(deleteLocator);
    }

    private void doDelete(long id)
    {
        String deleteLocator = createDeleteLinkLocator(id);
        openAndWaitForDeleteDialog(deleteLocator);
        client.click("id=delete-attachment-submit");
        waitForContentUpdate();
        assertThat.elementNotPresent(deleteLocator);
    }

    private void openAndWaitForDeleteDialog(String trigger)
    {
        client.click(trigger);
        assertThat.visibleByTimeout("css=#delete-attachment-dialog.aui-dialog-content-ready", PAGE_LOAD_WAIT_TIME);
    }

    private String createDeleteLinkLocator(long id)
    {
        return String.format("id=del_%d", id);
    }

    private File getScreenshot()
    {
        return getFile("screenshot.png");
    }

    private File getScreenshot2()
    {
        return getFile("screenshot2.png");
    }

    private File getFile(final String file)
    {
        File screenshot = new File(getEnvironmentData().getXMLDataLocation(), file);
        try
        {
            return screenshot.getCanonicalFile();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
