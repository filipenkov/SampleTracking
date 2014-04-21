package com.atlassian.jira.webtests.ztests.bulk;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.navigation.BulkChangeWizard;
import com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation;
import com.atlassian.jira.functest.framework.navigation.issue.FileAttachmentsList;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * @since v4.4
 */
@WebTest ({ Category.FUNC_TEST, Category.ATTACHMENTS, Category.BULK_OPERATIONS })
public class TestBulkMoveAttachments extends FuncTestCase
{
    public void setUpTest()
    {
        super.setUpTest();
        // This is the XML used to test our various attachment naming schemes so using it for the bulk
        // move test is perfect
        administration.restoreData("TestAttachmentEncoding.xml");
        removeAttachmentFilesFromJiraHome(); // Clean up any left-overs from previous tests
        copyAttachmentFilesToJiraHome();
    }

    // JRA-23830. Make sure that the attachments actually move to the new issue when we do a Bulk Move.
    public void testBulkMove() throws Exception
    {
        // move HSP-1 to MKY project and then verify that all the attachments are still there
        navigation.issueNavigator().displayAllIssues();
        final BulkChangeWizard wizard = navigation.issueNavigator().bulkChange(IssueNavigatorNavigation.BulkChangeOption.ALL_PAGES);
        wizard.selectAllIssues()
            .chooseOperation(BulkChangeWizard.BulkOperations.MOVE)
            .chooseTargetContextForAll("monkey")
            .finaliseFields()
            .complete();

        // assert that all of the attachments are still reachable
        for (FileAttachmentsList.FileAttachmentItem fileAttachmentItem : navigation.issue().attachments("MKY-1").list().get())
        {
            tester.gotoPage("/secure/attachment/" + fileAttachmentItem.getId() + "/" + fileAttachmentItem.getName());
        }
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
