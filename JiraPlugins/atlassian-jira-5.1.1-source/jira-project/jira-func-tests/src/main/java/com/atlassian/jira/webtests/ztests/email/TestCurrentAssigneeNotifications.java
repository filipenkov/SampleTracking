package com.atlassian.jira.webtests.ztests.email;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.EmailFuncTestCase;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.List;

/**
 * A test that checks that the new assignee notifications of JRA-6344 are respected.
 * <p/>
 * It uses a "hacked" XML file so that jira-applications.properties property "jira.assignee.change.is.sent.to.both.parties"
 * is set to false (old way) and true (new way)
 *
 * @since v4.0
 */
// passing now
@WebTest ({ Category.FUNC_TEST, Category.EMAIL })
public class TestCurrentAssigneeNotifications extends EmailFuncTestCase
{
    
    @Override
    public void setUpTest()
    {
        super.setUpTest();
        // naming of our tests is important here
        if (getName().indexOf("PreJRA6244") != -1)
        {
            restoreDataAndConfigureSmtp("TestCurrentAssigneeNotifications_PreJRA6244.xml");
        }
        else
        {
            restoreDataAndConfigureSmtp("TestCurrentAssigneeNotifications_PostJRA6244.xml");
        }
        backdoor.darkFeatures().enableForSite("no.frother.assignee.field");
    }

    @Override
    public void tearDownTest()
    {
        backdoor.darkFeatures().disableForSite("no.frother.assignee.field");
        super.tearDownTest();
    }

    /*
     test that 2 emails are sent on re-assign operation
    */
    public void testAssignIssue_PreJRA6244() throws InterruptedException, MessagingException, IOException
    {
        navigation.issue().assignIssue("HSP-1", "This has been re-assigned", "homer");
        assertHomerAndBartsEmails(1, 1);
    }

    /*
      This works the same as the old behavior.  In other words it always worked for re-assign
     */
    public void testAssignIssue_PostJRA6244() throws InterruptedException, MessagingException, IOException
    {
        navigation.issue().assignIssue("HSP-1", "This has been re-assigned", "homer");
        assertHomerAndBartsEmails(1, 1);
    }


    /*
        This asserts that only one email is sent on edit issue 
     */
    public void testEditIssue_PreJRA6244() throws MessagingException, InterruptedException, IOException
    {
        editIssueAndChangeAssignee("HSP-1", "This has been re-assigned", "homer");
        assertHomerAndBartsEmails(1, 0);
    }

    /*
        This asserts that 2 emails is sent on edit issue
     */
    public void testEditIssue_PostJRA6244() throws MessagingException, InterruptedException, IOException
    {
        editIssueAndChangeAssignee("HSP-1", "This has been re-assigned", "homer");
        assertHomerAndBartsEmails(1, 1);
    }

    /*
        This asserts that only one email is sent on transitioning an issue
     */
    public void testTransitionIssue_PreJRA6244() throws MessagingException, InterruptedException, IOException
    {
        workflowIssueAndChangeAssignee("HSP-1", "homer");
        assertHomerAndBartsEmails(1, 0);
    }

    /*
        This asserts that 2 emails is sent on transitioning an issue
     */
    public void testTransitionIssue_PostJRA6244() throws MessagingException, InterruptedException, IOException
    {
        workflowIssueAndChangeAssignee("HSP-1", "homer");
        assertHomerAndBartsEmails(1, 1);
    }

    /*
        This asserts that only one email is sent on bulk edit issue
     */
    public void testBulkEditIssue_PreJRA6244() throws MessagingException, InterruptedException, IOException
    {
        bulkEditHSP_1("homer");
        assertHomerAndBartsEmails(1, 0);
    }

    /*
        This asserts that 2 emails is sent on edit issue
     */
    public void testBulkEditIssue_PostJRA6244() throws MessagingException, InterruptedException, IOException
    {
        bulkEditHSP_1("homer");
        assertHomerAndBartsEmails(1, 1);
    }

    /*
        This asserts that only one email is sent on bulk transition issue
     */
    public void testBulkTransitionIssue_PreJRA6244() throws MessagingException, InterruptedException, IOException
    {
        bulkTransitionHSP_1("homer");
        assertHomerAndBartsEmails(1, 0);
    }

    /*
        This asserts that 2 emails is sent on bulk transition issue
     */
    public void testBulkTransitionIssue_PostJRA6244() throws MessagingException, InterruptedException, IOException
    {
        bulkTransitionHSP_1("homer");
        assertHomerAndBartsEmails(1, 1);
    }

    /* ==== PRIVATE METHODS =====================================================*/
    private void bulkEditHSP_1(final String newAssigneeName)
    {
        // Click Link 'find issues' (id='find_link').
        navigation.issueNavigator().displayAllIssues();
        tester.submit("show");
        // Click Link 'all 1 issue(s)' (id='bulkedit_all').
        tester.clickLink("bulkedit_all");
        tester.checkCheckbox("bulkedit_10000", "on");
        tester.submit("Next");
        tester.checkCheckbox("operation", "bulk.edit.operation.name");
        tester.submit("Next");
        tester.checkCheckbox("actions", "assignee");
        // Select 'Administrator' from select box 'assignee'.
        tester.selectOption("assignee", newAssigneeName);
        tester.checkCheckbox("sendBulkNotification", "true");
        tester.submit("Next");
        tester.submit("Confirm");
    }

    private void bulkTransitionHSP_1(String newAssigneeName)
    {
        // Click Link 'find issues' (id='find_link').
        navigation.issueNavigator().displayAllIssues();
        tester.submit("show");
        // Click Link 'all 1 issue(s)' (id='bulkedit_all').
        tester.clickLink("bulkedit_all");
        tester.checkCheckbox("bulkedit_10000", "on");
        tester.submit("Next");
        tester.checkCheckbox("operation", "bulk.workflowtransition.operation.name");
        tester.submit("Next");
        tester.checkCheckbox("wftransition", "jira_5_5");
        tester.submit("Next");
        // Select 'Won't Fix' from select box 'resolution'.
        tester.checkCheckbox("actions", "resolution");
        tester.selectOption("resolution", "Won't Fix");

        tester.checkCheckbox("actions", "assignee");
        tester.selectOption("assignee", newAssigneeName);
        tester.checkCheckbox("sendBulkNotification", "true");
        tester.submit("Next");
        tester.submit("Next");
    }

    private void editIssueAndChangeAssignee(final String issueKey, final String commentStr, final String newAssigneeName)
    {
        navigation.issue().viewIssue(issueKey);
        tester.clickLink("edit-issue");

        tester.selectOption("assignee", newAssigneeName);
        tester.setFormElement("comment", commentStr);
        tester.submit("Update");
    }

    private void workflowIssueAndChangeAssignee(final String issueKey, final String newAssigneeName)
    {
        navigation.issue().viewIssue(issueKey);
        tester.clickLink("action_id_5");
        tester.setWorkingForm("issue-workflow-transition");
        tester.selectOption("resolution", "Won't Fix");
        tester.selectOption("assignee", newAssigneeName);
        tester.submit("Transition");
    }


    private void assertHomerAndBartsEmails(int expectedHomerEmailCount, int expectedBartEmailCount)
            throws MessagingException, InterruptedException, IOException
    {
        flushMailQueueAndWait(expectedHomerEmailCount + expectedBartEmailCount);
        assertHomerWasAssignedEmails("homer@localhost", expectedHomerEmailCount);
        assertHomerWasAssignedEmails("bart@localhost", expectedBartEmailCount);
    }

    private void assertHomerWasAssignedEmails(String emailAddress, int expectedEmailCount)
            throws MessagingException, IOException
    {
        List messagesForRecipient = getMessagesForRecipient(emailAddress);
        assertEquals(expectedEmailCount, messagesForRecipient.size());

        for (Object msg : messagesForRecipient)
        {
            final MimeMessage message = (MimeMessage) msg;
            final String subject = message.getSubject();
            assertTrue(subject.contains("HSP-1"));
            assertEmailBodyContains(message, "HSP-1");
            assertEmailBodyContains(message, "Assignee:");
            assertEmailBodyContainsLine(message, ".*diffremovedchars.*bart.*");
            assertEmailBodyContainsLine(message, ".*diffaddedchars.*homer.*");
        }
    }

    private void restoreDataAndConfigureSmtp(final String fileName)
    {
        administration.restoreData(fileName);
        configureAndStartSmtpServer();
    }

}
