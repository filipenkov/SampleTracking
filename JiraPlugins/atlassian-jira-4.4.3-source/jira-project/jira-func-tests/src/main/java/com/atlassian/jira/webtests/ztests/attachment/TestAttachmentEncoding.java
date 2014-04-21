package com.atlassian.jira.webtests.ztests.attachment;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.navigation.issue.AttachmentsBlock;
import com.atlassian.jira.functest.framework.navigation.issue.FileAttachmentsList;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

// Over the years JIRA has attempted several things to deal with filesystem encoding issues of attachments. In the old, old
// days we didn't know and didn't care. This lead to attachments being broken in some cases. Then we tried Workaround #1, which was
// insufficient. Now we are on Workaround #2 (http://jira.atlassian.com/browse/JRA-23311). This test verifies that all
// three methods still work.
@WebTest({Category.FUNC_TEST, Category.ATTACHMENTS })
public class TestAttachmentEncoding extends FuncTestCase
{
    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestAttachmentEncoding.xml");
        removeAttachmentFilesFromJiraHome(); // Clean up any left-overs from previous tests
        copyAttachmentFilesToJiraHome();
    }

    public void testCheckVariousAttachmentFilenames()
    {
        final AttachmentsBlock attachments = navigation.issue().attachments("HSP-1");
        final List<FileAttachmentsList.FileAttachmentItem> attachmentsList = attachments.list().get();
        assertEquals(3, attachmentsList.size());
        tester.gotoPage("/secure/attachment/10000/clover.license");
        tester.gotoPage("/secure/attachment/10001/sqltool.rc");
        tester.gotoPage("/secure/attachment/10002/svn");
    }

    // JRA-23830 Make sure that when you do a Move Issue all the various encoding of attachments get moved correctly.
    public void testMoveIssue() throws Exception
    {
        navigation.issue().viewIssue("HSP-1");
        // Click Link 'Move' (id='move_issue').
        tester.clickLink("move-issue");
        // Select 'Bovine' from select box 'pid'.
        tester.selectOption("pid", "monkey");
        tester.submit("Next >>");
        tester.submit("Next >>");
        tester.submit("Move");

        for (FileAttachmentsList.FileAttachmentItem fileAttachmentItem : navigation.issue().attachments("MKY-1").list().get())
        {
            tester.gotoPage("/secure/attachment/" + fileAttachmentItem.getId() + "/" + fileAttachmentItem.getName());
        }
    }

    @Override
    protected void tearDownTest()
    {
        navigation.gotoDashboard();
        removeAttachmentFilesFromJiraHome();
        super.tearDownTest();
    }

    protected final void copyAttachmentFilesToJiraHome()
    {
        final File jiraAttachmentsPath = new File(administration.getCurrentAttachmentPath());
        final File testAttachmentsPath = new File(environmentData.getXMLDataLocation(), "TestAttachmentEncoding/attachments");

        try
        {
            FileUtils.copyDirectory(testAttachmentsPath, jiraAttachmentsPath);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    protected final void removeAttachmentFilesFromJiraHome()
    {
        final File jiraAttachmentsPath = new File(administration.getCurrentAttachmentPath());
        try
        {
            FileUtils.cleanDirectory(jiraAttachmentsPath);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}