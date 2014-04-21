package com.atlassian.jira.webtest.selenium.issue.dialogs;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.functest.framework.Splitable;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.webtest.selenium.auidialog.AbstractAuiDialogTest;
import com.atlassian.jira.webtest.selenium.framework.dialogs.AttachFileDialog;
import com.atlassian.jira.webtest.selenium.framework.dialogs.QuickCreateIssue;
import com.atlassian.jira.webtest.selenium.framework.dialogs.QuickCreateSubtask;
import com.atlassian.jira.webtest.selenium.framework.model.ActionType;
import com.atlassian.jira.webtest.selenium.framework.model.SubmitType;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Attachment;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Issue;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.IssueClient;
import com.atlassian.selenium.Browser;
import com.atlassian.selenium.SkipInBrowser;
import junit.framework.Test;
import org.apache.commons.io.FileUtils;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Selenium tests for attachments.
 *
 * @since v4.1
 */

@SkipInBrowser (browsers={ Browser.IE})
@Splitable
@WebTest({Category.SELENIUM_TEST })
public class TestAttachFile extends AbstractAuiDialogTest
{
    private static final String MAX_FILESIZE_DESCRIPTION = "The maximum file upload size is 10.00 MB.";
    private static final String ATTACH_FILES_TITLE = "Attach Files";
    private static final String ATTACH_FILE_NAME_LOCATOR = "tempFilename";
    private static final String MIME_SNIFFING_WORKAROUND = "workaround";
    private static final String MIME_SNIFFING_OWNED = "insecure";
    private static final String MIME_SNIFFING_PARANOID = "secure";
    private static final String SAMPLE_IE_USER_AGENT = "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)";

    //Regex that should match when a file over the attachment limit is attached when using the File API (e.g. Firefox 3.6+)
    private static final Pattern PATTERN_ERROR_AJAX = Pattern.compile("(.*) is too large to attach. Attachment is .* but the largest allowed attachment is .*\\.");

    //Regex that should match when a file over the attachment limit is attached when using form Upload (e.g. IE{7,8}, Firefox 3.5).
    private static final Pattern PATTERN_ERROR_FORM = Pattern.compile("(.*) is larger than .*\\.");

    //Regex that should match the error that occurs when attaching an invalid file name.
    private static final Pattern PATTEN_ILLEGAL_CHAR = Pattern.compile("(.*) contains the invalid character '.'\\. Please rename the file and try again\\.");

    private static final FilenameFilter NO_DOT_FILES = new FilenameFilter()
    {
        public boolean accept(File dir, String name)
        {
            return name.charAt(0) != '.';
        }
    };

    private QuickCreateIssue quickCreate;
    private QuickCreateSubtask quickCreateSubtask;

    public static Test suite()
    {
        return suiteFor(TestAttachFile.class);
    }

    public void onSetUp()
    {
        super.onSetUp();
        restoreData("TestFullAnonymousPermissions.xml");
        getAdministration().enableAttachments();
        quickCreate = new QuickCreateIssue(context());
        quickCreateSubtask = new QuickCreateSubtask(context());
    }

    public void onTearDown()
    {
        quickCreate = null;
        quickCreateSubtask = null;
    }

    //JRA-23383: Filename is reported back. Need to make sure we escape it or include it on the page in a safe way.
    public void testAttachFileXSS()
    {
        //This test relies on the ability to be able to create files with "<" in the name. This is not possible under
        //Windows.
        if (System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("windows"))
        {
            return;
        }

        getNavigator().gotoIssue("MKY-1");
        client.click("attach-file");
        assertDialogIsOpenAndReady();
        List<String> list = getNavigator().issue().attachFileWithErrors(ATTACH_FILE_NAME_LOCATOR, "<input onfocus=alert(1) autofocus>", 10);
        assertFalse(client.isAlertPresent());
        assertTrue(list.contains("<input onfocus=alert(1) autofocus> contains the invalid character '<'. Please rename the file and try again."));
    }

    public void testAttachFile() throws Exception
    {
        getNavigator().gotoIssue("MKY-1");
        client.click("attach-file");
        assertDialogIsOpenAndReady();
        assertFormIsUndecorated();
        assertDialogContainsText(ATTACH_FILES_TITLE);
        assertDialogContainsText(MAX_FILESIZE_DESCRIPTION);
        closeDialogByClickingCancel();

        getNavigator().issue().attachFile(ATTACH_FILE_NAME_LOCATOR, "testfile", 1024);

        assertThat.elementPresentByTimeout("css=header#stalker h1", DEFAULT_TIMEOUT);
        assertThat.elementContainsText("css=header#stalker h1", "test");
        assertThat.elementPresent("jquery=dt.attachment-title:contains(testfile)");
    }

    public void testAttachFileNoFileSelected()
    {
        final AttachFileDialog attachFileDialog = new AttachFileDialog(context());
        getNavigator().gotoIssue("MKY-1");
        assertThat.textNotPresent("testfile3456.txt");
        attachFileDialog.openFromViewIssue();
        attachFileDialog.assertReady();
        assertThat.textNotPresent("Please indicate the file you wish to upload");

        // TODO WRONG, errors should be encapsulated by the dialog
        attachFileDialog.submit(SubmitType.BY_CLICK, false);
        ActionType.AJAX.waitForAction(context());

        assertThat.textPresent("Please indicate the file you wish to upload");
        getNavigator().issue().attachFileInExistingForm(ATTACH_FILE_NAME_LOCATOR, "testfile3456.txt", 1024);
        attachFileDialog.submit();

        assertThat.textPresent("testfile3456.txt");
    }

    public void testAttachmentSearchById()
    {
        final File currentAttachmentPath = new File(getAdministration().getCurrentAttachmentPath());
        final File attachmentFile = new File(currentAttachmentPath, "MKY/MKY-3/10000");
        final File modifiedFilename = new File(currentAttachmentPath, "MKY/MKY-3/10000_blahblah.txt");
        //Ensure we don't have files lying about from previous runs of this test
        deleteFileIfExists(attachmentFile);
        deleteFileIfExists(modifiedFilename);

        createIssueWithSummary("A summary");
        String fileNameOk1 = "okFile1.txt";
        getNavigator().issue().attachFileInExistingForm("tempFilename", fileNameOk1, 1024);
        client.click("id=issue-create-submit", true);

        // should succeed
        assertThat.linkPresentWithText(fileNameOk1);
        assertTrue(String.format("File '%s' was not created.", attachmentFile.getAbsolutePath()), attachmentFile.exists());
        assertTrue(String.format("Unable to rename '%s' to '%s'.", attachmentFile.getAbsolutePath(), modifiedFilename.getAbsolutePath()),
                attachmentFile.renameTo(modifiedFilename));

        gotoAttachment(fileNameOk1);
        assertThat.textPresent("xxxxx");
    }

    public void testFileSizesForSingleAttachment()
    {
        // create an issue
        createIssueWithSummary("A summary");
        assertThat.elementContainsText("css=#content header h1", "A summary");
        String fileNameInvalid1 = "12mbFile1.txt";
        assertFileSizeErrorMsg(fileNameInvalid1, getNavigator().issue().attachFileWithErrors("tempFilename", fileNameInvalid1, 1024 * 1024 * 12));

        String fileNameOk1 = "okFile1.txt";
        getNavigator().issue().attachFileInExistingForm("tempFilename", fileNameOk1, 1024);
        //should have cleared errors
        assertTrue(getNavigator().issue().getAttachmentErrors().isEmpty());
        client.click("id=issue-create-submit", true);

        // should succeed
        assertThat.linkPresentWithText(fileNameOk1);
        assertThat.linkNotPresentWithText(fileNameInvalid1);

        // now we have an issue lets attach some more files
        client.click("attach-file");
        assertDialogIsOpenAndReady();
        String fileNameInvalid2 = "12mbFile2.txt";
        assertFileSizeErrorMsg(fileNameInvalid2, getNavigator().issue().attachFileWithErrors("tempFilename", fileNameInvalid2, 1024 * 1024 * 12));

        // now work
        String fileNameOk2 = "okFile2.txt";
        getNavigator().issue().attachFileInExistingForm("tempFilename", fileNameOk2, 1024);
        assertTrue(getNavigator().issue().getAttachmentErrors().isEmpty());
        if (isKickAssEnabled())
        {
            submitDialogAndWaitForAjax();
        }
        else
        {
            submitDialogAndWaitForReload();
        }

        client.click("manage-attachment-link", true);

        // should succeed
        assertThat.textPresent("Manage Attachments");
        assertThat.linkPresentWithText(fileNameOk1);
        assertThat.linkPresentWithText(fileNameOk2);
        assertThat.linkNotPresentWithText(fileNameInvalid1);
        assertThat.linkNotPresentWithText(fileNameInvalid2);

        client.click("back-lnk", true);
        //back on issue
        assertThat.elementContainsText("css=#content header h1", "A summary");
    }

    public void testAttachFileWithBadCharacters()
    {
        createIssueWithSummary("bad chars");

        String badChars = "a?file.txt";
        List<String> errors = getNavigator().issue().attachFileWithErrors("tempFilename", badChars, 1024);
        assertFileIllegalCharError(badChars, errors);
        //create the issue anyway w/o the attachment
        client.click("id=issue-create-submit", true);

        //try the same from the attach file dialog
        client.click("attach-file");
        assertDialogIsOpenAndReady();

        errors = getNavigator().issue().attachFileWithErrors("tempFilename", badChars, 1024);
        assertFileIllegalCharError(badChars, errors);

        client.click("id=attach-file-cancel");
        assertDialogNotOpen();
    }

    public void testAttachFileWithWeirdCharacters()
    {
        createIssueWithSummary("spaces");

        String fileNameSpace = "a file.txt";
        getNavigator().issue().attachFileInExistingForm("tempFilename", fileNameSpace, 1024);
        client.click("id=issue-create-submit", true);

        assertThat.linkPresentWithText(fileNameSpace);
        String href = client.getAttribute("jquery=.attachment-title a:contains(" + fileNameSpace + ")@href");
        assertTrue(href.endsWith("a%20file.txt"));

        gotoAttachment(fileNameSpace);
        assertTrue(client.getLocation().endsWith("a%20file.txt"));

        String filename = "(test\'file)";
        String htmlEncodedFilename = "(test&#39;file)"; // HTML view
        String xmlEncodedFilename = "(test&apos;file)";    // used for XML view

        getNavigator().gotoPage("/browse/MKY-1", true);
        createIssueWithSummary("quotes");
        String issueCreated = "MKY-4";
        getNavigator().issue().attachFileInExistingForm("tempFilename", filename, 1024);
        client.click("id=issue-create-submit", true);
        assertThat.linkPresentWithText(filename);
        href = client.getAttribute("jquery=.attachment-title a:contains(" + filename + ")@href");
        assertTrue(href.endsWith("%28test%27file%29"));

        getWebUnitTest().gotoIssue(issueCreated);
        getWebUnitTest().getTester().clickLinkWithText("XML");
        assertTrue("text/xml".equals(getWebUnitTest().getTester().getDialog().getResponse().getContentType()));
        getWebUnitTest().getTester().assertTextPresent(xmlEncodedFilename);
        getWebUnitTest().beginAt("/browse/" + issueCreated);

        getWebUnitTest().getTester().clickLinkWithText("Word");
        getWebUnitTest().getTester().assertTextPresent(htmlEncodedFilename);
        getWebUnitTest().beginAt("/browse/" + issueCreated);

        client.clickLinkWithText("Printable", true);
        assertThat.htmlPresent(filename);
    }

    /**
     * Test editing the attachment size (total upload size limit) and verify the limit has changed by attaching files.
     *
     * @throws org.xml.sax.SAXException error when uploading attachment
     */
    public void testAttachFileAndEditAttachmentSize() throws SAXException
    {
        //base size of each attachment to be attached
        final int attachmentFileSize1024k = 1024;
        int fileSize = attachmentFileSize1024k + 1;

        //set the attachment size limit to 1kb
        int maxSizeInBytes = attachmentFileSize1024k;
        getAdministration().enableAttachments(String.valueOf(maxSizeInBytes));

        //attach single file larger than maxSizeBytes in create issue page
        String fileName = "createIssueAttachFailure.txt";
        createIssueWithSummary("too large");
        assertFileSizeErrorMsg(fileName, getNavigator().issue().attachFileWithErrors("tempFilename", fileName, fileSize));
        client.click("id=issue-create-cancel", true);

        //attach single file larger than maxSizeBytes in attach file page
        getNavigator().issue().viewIssue("MKY-1");
        client.click("attach-file");
        assertDialogIsOpenAndReady();
        assertFileSizeErrorMsg(fileName, getNavigator().issue().attachFileWithErrors("tempFilename", fileName, fileSize));
        client.click("id=attach-file-cancel");
        assertDialogNotOpen();

        //increase the attachment size limit to 2kb
        maxSizeInBytes = 1024 * 3;
        getAdministration().enableAttachments(String.valueOf(maxSizeInBytes));

        //attach single file less than maxSizeBytes in create issue page
        fileName = "createIssueAttachSuccess.txt";
        createIssueWithSummary("Success");
        getNavigator().issue().attachFileInExistingForm("tempFilename", fileName, fileSize);
        client.click("id=issue-create-submit", true);

        assertThat.linkPresentWithText(fileName);

        //attach single file less than maxSizeBytes in attach file page
        fileName = "viewIssueAttachSuccess.txt";
        getNavigator().issue().viewIssue("MKY-1");
        client.click("attach-file");
        assertDialogIsOpenAndReady();
        getNavigator().issue().attachFileInExistingForm("tempFilename", fileName, fileSize);
        if (isKickAssEnabled())
        {
            submitDialogAndWaitForAjax();
        }
        else
        {
            submitDialogAndWaitForReload();
        }
        assertThat.linkPresentWithText(fileName);
    }

    public void testMimeCasing()
    {
        final String issueKey = "MKY-2";
        final String fileName = "image.GIF";

        // Attach a file
        final File file = new File(getEnvironmentData().getXMLDataLocation().getAbsolutePath() + "/" + fileName);
        getNavigator().issue().viewIssue(issueKey);
        getNavigator().issue().attachFile("tempFilename", file.getAbsolutePath(), 0);

        // make sure it didn't get added as a file attachment
        assertThat.elementNotPresent("id=file_attachments");
        assertThat.elementPresent("id=attachment_thumbnails");
        assertThat.linkPresentWithText(fileName);
    }

    /*
     * JRA-10862 HTML attachments need to have a different Content-Disposition set in the header of the response
     * to prevent XSS attacks
     */

    public void testHtmlAttachmentDownloadDefaultMimeSniffingPolicy()
    {
        assertExploitFilesHaveDisposition("inline");
        assertAttachmentContentDisposition("screenshot.html", "screenshot.html", "attachment; filename*=UTF-8''screenshot.html;");
        assertAttachmentContentDisposition("screenshot.htm", "screenshot.htm", "attachment; filename*=UTF-8''screenshot.htm;");
        assertAttachmentContentDisposition("screenshot-png.htm", "screenshot-png.htm", "attachment; filename*=UTF-8''screenshot-png.htm;");
        assertAttachmentContentDisposition("screenshot-png.html", "screenshot-png.html", "attachment; filename*=UTF-8''screenshot-png.html;");
        assertAttachmentContentDisposition("really-png.png", "really-png.png", "inline; filename*=UTF-8''really-png.png;");
    }

    public void testHtmlAttachmentDownloadParanoidMimeSniffingPolicy() throws SAXException
    {
        getAdministration().setMimeSniffingPolicy(MIME_SNIFFING_PARANOID);
        assertExploitFilesHaveDisposition("attachment");
        assertAttachmentContentDisposition("screenshot.html", "screenshot.html", "attachment; filename*=UTF-8''screenshot.html;");
        assertAttachmentContentDisposition("screenshot.htm", "screenshot.htm", "attachment; filename*=UTF-8''screenshot.htm;");
        assertAttachmentContentDisposition("screenshot-png.htm", "screenshot-png.htm", "attachment; filename*=UTF-8''screenshot-png.htm;");
        assertAttachmentContentDisposition("screenshot-png.html", "screenshot-png.html", "attachment; filename*=UTF-8''screenshot-png.html;");
        assertAttachmentContentDisposition("really-png.png", "really-png.png", "attachment; filename*=UTF-8''really-png.png;");
    }

    public void testHtmlAttachmentDownloadWorkaroundMimeSniffingPolicy() throws SAXException
    {
        getAdministration().setMimeSniffingPolicy(MIME_SNIFFING_WORKAROUND);
        assertExploitFilesHaveDisposition("inline");
        assertAttachmentContentDisposition("screenshot.html", "screenshot.html", "attachment; filename*=UTF-8''screenshot.html;");
        assertAttachmentContentDisposition("screenshot.htm", "screenshot.htm", "attachment; filename*=UTF-8''screenshot.htm;");
        assertAttachmentContentDisposition("screenshot-png.htm", "screenshot-png.htm", "attachment; filename*=UTF-8''screenshot-png.htm;");
        assertAttachmentContentDisposition("screenshot-png.html", "screenshot-png.html", "attachment; filename*=UTF-8''screenshot-png.html;");
        assertAttachmentContentDisposition("really-png.png", "really-png.png", "inline; filename*=UTF-8''really-png.png;");
    }

    public void testHtmlAttachmentDownloadPwnedMimeSniffingPolicy() throws SAXException
    {
        getAdministration().setMimeSniffingPolicy(MIME_SNIFFING_OWNED);
        assertExploitFilesHaveDisposition("inline");
        assertAttachmentContentDisposition("screenshot.html", "screenshot.html", "inline; filename*=UTF-8''screenshot.html;");
        assertAttachmentContentDisposition("screenshot.htm", "screenshot.htm", "inline; filename*=UTF-8''screenshot.htm;");
        assertAttachmentContentDisposition("screenshot-png.htm", "screenshot-png.htm", "inline; filename*=UTF-8''screenshot-png.htm;");
        assertAttachmentContentDisposition("screenshot-png.html", "screenshot-png.html", "inline; filename*=UTF-8''screenshot-png.html;");
        assertAttachmentContentDisposition("really-png.png", "really-png.png", "inline; filename*=UTF-8''really-png.png;");
    }

    public void testAttachmentDownloadDefaultMimeSniffingPolicyInternetExplorer() throws SAXException
    {
        getWebUnitTest().getDialog().getWebClient().getClientProperties().setUserAgent(SAMPLE_IE_USER_AGENT);
        assertExploitFilesHaveDisposition("attachment");
        // now do the files that do not smell like html
        assertAttachmentContentDisposition("screenshot.html", "screenshot.html", "attachment; filename*=UTF-8''screenshot.html;");
        assertAttachmentContentDisposition("screenshot.htm", "screenshot.htm", "attachment; filename*=UTF-8''screenshot.htm;");
        assertAttachmentContentDisposition("screenshot-png.htm", "screenshot-png.htm", "attachment; filename*=UTF-8''screenshot-png.htm;");
        assertAttachmentContentDisposition("screenshot-png.html", "screenshot-png.html", "attachment; filename*=UTF-8''screenshot-png.html;");
        assertAttachmentContentDisposition("really-png.png", "really-png.png", "inline; filename*=UTF-8''really-png.png;");
    }

    public void testAttachmentDownloadParanoidMimeSniffingPolicyInternetExplorer() throws SAXException
    {
        getWebUnitTest().getDialog().getWebClient().getClientProperties().setUserAgent(SAMPLE_IE_USER_AGENT);
        getAdministration().setMimeSniffingPolicy(MIME_SNIFFING_PARANOID);
        assertExploitFilesHaveDisposition("attachment");
        // now do the files that do not have html tags inside them
        assertAttachmentContentDisposition("screenshot.html", "screenshot.html", "attachment; filename*=UTF-8''screenshot.html;");
        assertAttachmentContentDisposition("screenshot.htm", "screenshot.htm", "attachment; filename*=UTF-8''screenshot.htm;");
        assertAttachmentContentDisposition("screenshot-png.htm", "screenshot-png.htm", "attachment; filename*=UTF-8''screenshot-png.htm;");
        assertAttachmentContentDisposition("screenshot-png.html", "screenshot-png.html", "attachment; filename*=UTF-8''screenshot-png.html;");
        assertAttachmentContentDisposition("really-png.png", "really-png.png", "attachment; filename*=UTF-8''really-png.png;");
    }

    public void testAttachmentDownloadPwnedMimeSniffingPolicyInternetExplorer() throws SAXException
    {
        getWebUnitTest().getDialog().getWebClient().getClientProperties().setUserAgent(SAMPLE_IE_USER_AGENT);
        getAdministration().setMimeSniffingPolicy(MIME_SNIFFING_OWNED);
        assertExploitFilesHaveDisposition("inline");
        assertAttachmentContentDisposition("screenshot.html", "screenshot.html", "inline; filename*=UTF-8''screenshot.html;");
        assertAttachmentContentDisposition("screenshot.htm", "screenshot.htm", "inline; filename*=UTF-8''screenshot.htm;");
        assertAttachmentContentDisposition("screenshot-png.htm", "screenshot-png.htm", "inline; filename*=UTF-8''screenshot-png.htm;");
        assertAttachmentContentDisposition("screenshot-png.html", "screenshot-png.html", "inline; filename*=UTF-8''screenshot-png.html;");
        assertAttachmentContentDisposition("really-png.png", "really-png.png", "inline; filename*=UTF-8''really-png.png;");
    }

    public void testAttachmentDownloadWorkaroundMimeSniffingPolicyInternetExplorer() throws SAXException
    {
        getWebUnitTest().getDialog().getWebClient().getClientProperties().setUserAgent(SAMPLE_IE_USER_AGENT);
        getAdministration().setMimeSniffingPolicy(MIME_SNIFFING_WORKAROUND);
        assertExploitFilesHaveDisposition("attachment");
        assertAttachmentContentDisposition("screenshot.html", "screenshot.html", "attachment; filename*=UTF-8''screenshot.html;");
        assertAttachmentContentDisposition("screenshot.htm", "screenshot.htm", "attachment; filename*=UTF-8''screenshot.htm;");
        assertAttachmentContentDisposition("screenshot-png.htm", "screenshot-png.htm", "attachment; filename*=UTF-8''screenshot-png.htm;");
        assertAttachmentContentDisposition("screenshot-png.html", "screenshot-png.html", "attachment; filename*=UTF-8''screenshot-png.html;");
        assertAttachmentContentDisposition("really-png.png", "really-png.png", "inline; filename*=UTF-8''really-png.png;");
    }

    public void testAttachFileFromSubTaskQuickCreateForm() throws Exception
    {
        // test data has fudged properties to change the fields in the STQC form
        restoreData("TestAttachmentsInSubTaskQuickCreateForm.xml");
        getAdministration().enableAttachments();
        final String fileName = "attach1.txt";
        getNavigator().issue().viewIssue("MKY-2");
        quickCreateSubtask.open().assertReady(5000);
        quickCreateSubtask.setFieldValue("summary", "My subtask");
        getNavigator().issue().attachFileInExistingForm("jquery=#create-subtask-dialog #attachment_box", fileName, 1024);
        quickCreateSubtask.submit(SubmitType.BY_CLICK);
        getNavigator().issue().viewIssue("MKY-3");
        assertThat.linkPresentWithText(fileName);
    }

    // JRA-14580. prevent recursion by web crawler that index the content of attachments.

    public void testAttachmentSlashLimiting() throws IOException, SAXException
    {
        final String fileName = "screenshot.txt";

        getNavigator().issue().viewIssue("MKY-1").attachFile("tempFilename", fileName, 1024);
        assertThat.linkPresentWithText(fileName);

        gotoAttachment(fileName);
        final String badUrl = client.getLocation() + "/morestuff";
        try
        {
            getNavigator().gotoPage(badUrl, true);
            fail("Request to invalid attachment path should have returned 404");
        }
        catch (Exception e)
        {
            assertTrue(e.getMessage().contains("Response_Code = 404"));
        }
    }

    public void testCanDownloadAPlainTextFileWithGzipCompression() throws SAXException
    {
        getAdministration().enableGzipCompression();

        final String testfile = "testfile.txt";
        getNavigator().issue().viewIssue("MKY-1").attachFile("tempFilename", testfile, 1024);

        gotoAttachment(testfile);
    }

    public void testFileNotFoundWhenAttachmentNotOnDisk() throws IOException
    {
        String currentAttachmentPath = getAdministration().getCurrentAttachmentPath();
        FileUtils.deleteDirectory(new File(currentAttachmentPath));
        String fileName = "mysuperrandomfilename.txt";
        File attachmentFile = new File(currentAttachmentPath, "MKY/MKY-2/10000");
        getNavigator().issue().viewIssue("MKY-2").attachFile("tempFilename", fileName, 1024);

        // delete file from server
        assertTrue(attachmentFile.exists());
        if (!attachmentFile.delete())
        {
            fail("Could not delete file '" + attachmentFile.getAbsolutePath() + "' from server");
        }

        try
        {
            getNavigator().gotoPage("secure/attachment/10000/" + fileName, true);
            fail("should have thrown an error!");
        }
        catch (Exception e)
        {
            //check we got a 404 error!
            assertTrue("Expected attachment not found message but got '" + e.getMessage() + "'", e.getMessage().contains("404"));
        }
    }

    public void testIssueResourceAttachmentsWithThumbnail() throws Exception
    {
        // first attach an image
        File fileToAttach = new File(getEnvironmentData().getXMLDataLocation().getAbsolutePath() + File.separator + "picture-attachment.jpg");
        String currentAttachmentPath = getAdministration().getCurrentAttachmentPath();
        FileUtils.deleteDirectory(new File(currentAttachmentPath));
        getNavigator().issue().viewIssue("MKY-1").attachFile("tempFilename", fileToAttach.getAbsolutePath(), 0);

        Issue issue = new IssueClient(getEnvironmentData()).get("MKY-1");
        for (Attachment item : issue.fields.attachment)
        {
            if (item.self.endsWith("rest/api/2/attachment/10000"))
            {
                Assert.assertThat(item.thumbnail, Matchers.endsWith("secure/thumbnail/10000/_thumb_10000.png"));
                return;
            }
        }

        fail("Issue does not contain attachment: picture-attachment.jpg");
    }

    public void testCommentVisibility()
    {
        restoreData("TestBlankInstancePlusAFewUsers.xml");
        backdoor.plugins().disablePlugin("com.atlassian.jira.jira-issue-nav-plugin");
        getAdministration().enableAttachments();

        getAdministration().toogleCommentGroupVisibility(true);

        String USER_COMMENT1 = "should be viewable by jira-users";
        String USER_COMMENT2 = "should be viewable by All Users";
        String ADMIN_COMMENT1 = "should be viewable by admins group";
        String ADMIN_COMMENT2 = "should be viewable by admins role";
        String DEV_COMMENT1 = "should be viewable by developer group";
        String DEV_COMMENT2 = "should be viewable by developer role";

        getNavigator().issue().viewIssue("HSP-2");
        getNavigator().issue().attachFileWithComment("tempFilename", "filename1", 1024, ADMIN_COMMENT1, "jira-administrators");
        getNavigator().issue().attachFileWithComment("tempFilename", "filename2", 1024, ADMIN_COMMENT2, "Administrators");
        getNavigator().issue().attachFileWithComment("tempFilename", "filename3", 1024, DEV_COMMENT1, "jira-developers");
        getNavigator().issue().attachFileWithComment("tempFilename", "filename4", 1024, DEV_COMMENT2, "Developers");
        getNavigator().issue().attachFileWithComment("tempFilename", "filename5", 1024, USER_COMMENT1, "jira-users");
        getNavigator().issue().attachFileWithComment("tempFilename", "filename6", 1024, USER_COMMENT2, "Users");

        final List<String> devComments = CollectionBuilder.newBuilder(DEV_COMMENT1, DEV_COMMENT2).asList();
        final List<String> adminComments = CollectionBuilder.newBuilder(ADMIN_COMMENT1, ADMIN_COMMENT2).asList();
        final List<String> userComments = CollectionBuilder.newBuilder(USER_COMMENT1, USER_COMMENT2).asList();

        checkCommentVisibility("devman", "HSP-2", EasyList.mergeLists(devComments, userComments, null), adminComments);
        checkCommentVisibility("onlyadmin", "HSP-2", EasyList.mergeLists(adminComments, userComments, null), devComments);
        checkCommentVisibility("fred", "HSP-2", userComments, EasyList.mergeLists(devComments, adminComments, null));
        //restore admin login
        getNavigator().login(ADMIN_USERNAME);
    }

    /**
     * Attach a picture to an issue, and use the wiki to display the picture/thumbnail inline as part of the comment
     */
    public void testWikiRendererInlineThumbnail()
    {
        restoreData("TestPluggableRendererComponents.xml");
        backdoor.plugins().disablePlugin("com.atlassian.jira.jira-issue-nav-plugin");

        //attach an image to the issue to test with
        File file = new File(getEnvironmentData().getXMLDataLocation().getAbsolutePath() + "/picture-attachment.jpg");
        getNavigator().issue().viewIssue("HSP-1").attachFile("tempFilename", file.getAbsolutePath(), 0);

        //add a comment to display the image as a thumbnail
        client.click("id=footer-comment-button");
        assertThat.visibleByTimeout("id=comment", 5000);
        client.typeWithFullKeyEvents("id=comment", "!picture-attachment.jpg|thumbnail!");
        client.click("id=issue-comment-add-submit", true);

        //check that the inline thumbnail is there and links to the original image
        assertThat.elementPresent("id=10020_thumb");

        //add a comment to display the actual image
        client.click("id=footer-comment-button");
        assertThat.visibleByTimeout("id=comment", 5000);
        client.typeWithFullKeyEvents("id=comment", "!picture-attachment.jpg!");
        client.click("id=issue-comment-add-submit", true);

        //check that the inline image is there and has no link
        assertTrue(client.getHtmlSource().contains("<img src=\"" + getEnvironmentData().getContext() + "/secure/attachment/10020/10020_picture-attachment.jpg\""));
    }

    public void testIssueNavigatorWithPicture() throws IOException
    {
        File fileToAttach = new File(getEnvironmentData().getXMLDataLocation().getAbsolutePath() + File.separator + "picture-attachment.jpg");
        String currentAttachmentPath = getAdministration().getCurrentAttachmentPath();
        FileUtils.deleteDirectory(new File(currentAttachmentPath));
        getNavigator().issue().viewIssue("MKY-1").attachFile("tempFilename", fileToAttach.getAbsolutePath(), 0);

        getNavigator().gotoPage("/secure/ViewUserIssueColumns!default.jspa", true);
        client.selectOption("fieldId", "Images");
        client.click("id=issue-nav-add-columns-submit", true);
        getNavigator().findAllIssues();

        assertThat.elementPresent("jQuery=td.thumbnail a img");
        assertThat.attributeContainsValue("jQuery=td.thumbnail a img", "src", "10000");

        //check the printable view too
        client.click("id=printable", true);
        assertThat.elementPresent("jQuery=td.thumbnail a img");
        assertThat.attributeContainsValue("jQuery=td.thumbnail a img", "src", "10000");
    }

    public void testIssueNavigator() throws IOException
    {
        AttachFileDialog attachFileDialog = new AttachFileDialog(context());
        File fileToAttach = new File(getEnvironmentData().getXMLDataLocation().getAbsolutePath() + File.separator + "picture-attachment.jpg");
        String currentAttachmentPath = getAdministration().getCurrentAttachmentPath();
        FileUtils.deleteDirectory(new File(currentAttachmentPath));

        getNavigator().gotoPage("/secure/ViewUserIssueColumns!default.jspa", true);
        client.selectOption("fieldId", "Images");
        client.click("id=issue-nav-add-columns-submit", true);
        getNavigator().findAllIssues().gotoFindIssues();

        attachFileDialog.openFromIssueNav(10010).assertReady();
        getNavigator().issue().attachFileInExistingForm("tempFilename", fileToAttach.getAbsolutePath(), 0);
        attachFileDialog.submit();
        client.waitForPageToLoad();

        assertThat.elementPresentByTimeout("jQuery=#affectedIssueMsg");
        assertThat.elementContainsText("jquery=#affectedIssueMsg", "The files(s) have been attached to MKY-1.");
        assertThat.elementContainsText("jquery=section#content header h1", "Issue Navigator");

        assertThat.elementPresent("jQuery=td.thumbnail a img");
        assertThat.attributeContainsValue("jQuery=td.thumbnail a img", "src", "10000");

        getNavigator().issue().viewIssue("MKY-1");

        assertThat.attributeContainsValue("jQuery=div.attachment-thumb a img", "src", "10000");
    }

    public void testAttachMultiple()
    {
        createIssueWithSummary(null);
        //attach an image to the issue to test with
        getNavigator().issue().attachFileInExistingForm("tempFilename", "somefile23.txt", 1024);
        getNavigator().issue().attachFileInExistingForm("tempFilename", "somefile24.txt", 1024);
        getNavigator().issue().attachFileInExistingForm("tempFilename", "somefile25.txt", 1024);

        assertEquals("3", client.getEval("dom=this.browserbot.getCurrentWindow().jQuery(\"input[name=filetoconvert]:checked\").length"));
        client.click("id=issue-create-submit", true);
        assertThat.textPresent("You must specify a summary of the issue");

        //check all 3 are still checked
        assertEquals("3", client.getEval("dom=this.browserbot.getCurrentWindow().jQuery(\"input[name=filetoconvert]:checked\").length"));
        //now uncheck the first one
        client.click("jquery=input[name=filetoconvert]:first");
        assertEquals("2", client.getEval("dom=this.browserbot.getCurrentWindow().jQuery(\"input[name=filetoconvert]:checked\").length"));

        client.click("id=issue-create-submit", true);
        assertThat.textPresent("You must specify a summary of the issue");
        assertEquals("2", client.getEval("dom=this.browserbot.getCurrentWindow().jQuery(\"input[name=filetoconvert]:checked\").length"));

        client.type("summary", "some summary");
        client.click("id=issue-create-submit", true);

        assertThat.linkNotPresentWithText("somefile23.txt");
        assertThat.linkPresentWithText("somefile24.txt");
        assertThat.linkPresentWithText("somefile25.txt");
    }

    private void checkCommentVisibility(final String usernameAndPassword, final String issueKey, final List<String> expectedComments, final List<String> absentComments)
    {
        getNavigator().logout(getXsrfToken());
        getNavigator().login(usernameAndPassword);
        getNavigator().issue().viewIssue(issueKey);
        for (String expectedComment : expectedComments)
        {
            assertThat.textPresent(expectedComment);
        }
        for (String absentComment : absentComments)
        {
            assertThat.textNotPresent(absentComment);
        }

    }

    private void assertExploitFilesHaveDisposition(String disposition)
    {
        getNavigator().issue().viewIssue("MKY-1");
        File exploitDir = new File(getEnvironmentData().getXMLDataLocation(), "xss_exploit_files/");
        File[] exploitFiles = exploitDir.listFiles(NO_DOT_FILES);
        for (File exploitFile : exploitFiles)
        {
            final String expectedDisposition = disposition + "; filename*=UTF-8''" + exploitFile.getName() + ";";
            assertAttachmentContentDisposition(exploitFile.getAbsolutePath(), exploitFile.getName(), expectedDisposition);
        }
    }

    private void assertAttachmentContentDisposition(final String filePath, final String fileName, final String expectedDisposition)
    {
        getNavigator().issue().attachFile("tempFilename", filePath, 1024);
        getWebUnitTest().gotoPage("/browse/MKY-1");
        getWebUnitTest().getTester().clickLinkWithText(fileName);
        final String realDisposition = getWebUnitTest().getDialog().getResponse().getHeaderField("Content-Disposition");
        assertTrue("Content-Disposition  expected [" + expectedDisposition + "] but found [" + realDisposition + "]", realDisposition.indexOf(expectedDisposition) >= 0);
    }


    private void gotoAttachment(final String fileNameSpace)
    {
        client.click("jquery=.attachment-title a:contains(" + fileNameSpace + ")", true);
    }

    private void createIssueWithSummary(final String summary)
    {
        if(client.isElementPresent("leave_admin"))
        {
            client.click("leave_admin", true);
        }

        quickCreate.open();
        client.click(quickCreate.submitTriggerLocator());
        client.waitForPageToLoad();
        if (summary != null)
        {
            client.type("summary", summary);
        }
    }

    private void assertFileIllegalCharError(final String fileName, final List<String> errors)
    {
        for (String error : errors)
        {
            Matcher matcher = PATTEN_ILLEGAL_CHAR.matcher(error);
            assertTrue("Error '" + error + "' did not contain message about illegal character.", matcher.matches());
            assertEquals("Error expected for file '" + fileName + "' but was reported for file '" + matcher.group(1) + "'.", fileName, matcher.group(1));
        }
    }

    private void assertFileSizeErrorMsg(final String fileName, final List<String> errors)
    {
        for (String error : errors)
        {
            String messageName = null;
            Matcher matcher = PATTERN_ERROR_AJAX.matcher(error);
            if (!matcher.matches())
            {
                matcher = PATTERN_ERROR_FORM.matcher(error);
                if (!matcher.matches())
                {
                    fail("Could not find attachment size message for '" + fileName + "' in '" + error +".");
                }
                else
                {
                    messageName = matcher.group(1);
                }
            }
            else
            {
                messageName = matcher.group(1);
            }

            assertEquals("Looking for error message for '" + fileName + "' but got one for '" + messageName + "'.", fileName, messageName);
        }
    }

    private static void deleteFileIfExists(final File deleteMe)
    {
        if (deleteMe.exists())
        {
            assertTrue(String.format("Unable to delete '%s'.", deleteMe.getAbsolutePath()), deleteMe.delete());
        }
    }
}
