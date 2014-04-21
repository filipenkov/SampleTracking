package com.atlassian.jira.webtests.ztests.email;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.EmailFuncTestCase;
import com.atlassian.jira.webtests.JIRAServerSetup;
import com.google.common.collect.Sets;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Set;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.HSP_PROJECT_ID;

/**
 * @since v5.0
 */
@WebTest ({ Category.FUNC_TEST, Category.EMAIL })
public class TestShareEmails extends EmailFuncTestCase
{
    private ShareClient shareClient;
    private String issueKey;

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        backdoor.restoreBlankInstance();

        startMailService(JIRAServerSetup.SMTP);
        int smtpPort = mailService.getSmtpPort();
        backdoor.mailServers().addSmtpServer(smtpPort);

        issueKey = backdoor.issues().createIssue(HSP_PROJECT_ID, "Issue 1", "admin").key();
        shareClient = new ShareClient(getEnvironmentData());

        mailService.addUser("admin@example.com", "admin", "admin");
        mailService.addUser("fred@example.com", "fred", "fred");
        mailService.addUser("fake@example.com", "fake", "fake");

        backdoor.userProfile().changeUserNotificationType("admin", "text");
        backdoor.userProfile().changeUserNotificationType("fred", "html");
    }

    public void testAll() throws Exception
    {
        log("Running _testShareIssue");
        _testShareIssue();
        log("Running _testShareSavedSearch");
        _testShareSavedSearch();
        log("Running _testShareJqlSearch");
        _testShareJqlSearch();
    }

    public void _testShareIssue() throws Exception
    {
        Set<String> usernames = Sets.newHashSet("fred", "admin");
        Set<String> emails = Sets.newHashSet("fake@example.com");
        String comment = "I thought you should know";
        shareClient.shareIssue(issueKey, usernames, emails, comment);

        flushMailQueueAndWait(3);
        MimeMessage[] mimeMessages = mailService.getReceivedMessagesAndClear();

        // 1. Check the HTML email
        MimeMessage message = getMessageForAddress(mimeMessages, "fred@example.com");
        String subject = message.getSubject();
        String body = message.getContent().toString();
        assertMessageIsHtml(message);
        assertEquals("[JIRATEST] Administrator shared \"HSP-1: Issue 1\" with you", subject);
        assertTrue(body.contains("just shared <img"));
        assertTrue(body.contains("HSP-1</a> with you"));
        assertTrue(body.contains(comment));
        assertTrue(body.contains("HSP-1'>View Issue</a>"));
        assertTrue(body.contains("HSP-1#add-comment'>Add Comment</a>"));

        // 2. Check the Text email
        message = getMessageForAddress(mimeMessages, "admin@example.com");
        subject = message.getSubject();
        body = message.getContent().toString();
        assertMessageIsText(message);
        assertEquals("[JIRATEST] Administrator shared \"HSP-1: Issue 1\" with you", subject);
        assertTrue(body.contains("Administrator just shared HSP-1 with you"));
        assertTrue(body.contains("/browse/HSP-1"));
        assertTrue(body.contains(comment));
        assertTrue(body.contains("Key: HSP-1"));
        assertTrue(body.contains("Project: homosapien"));

        // 3. Check that the email sent to the email address is HTML
        message = getMessageForAddress(mimeMessages, "fake@example.com");
        assertMessageIsHtml(message);
    }

    public void _testShareSavedSearch() throws Exception
    {
        String searchJql = "project = HSP";
        String searchName = "Funky Homosapiens";
        String searchDescription = "Find those dudes!";
        String jsonShareString = "[{\"type\":\"global\"}]";
        String filterId = backdoor.searchRequests().createFilter("admin", searchJql, searchName, searchDescription, jsonShareString);

        Set<String> usernames = Sets.newHashSet("fred", "admin");
        Set<String> emails = Sets.newHashSet("fake@example.com");
        String comment = "I thought you should know";
        shareClient.shareSavedSearch(filterId, usernames, emails, comment);

        flushMailQueueAndWait(3);
        MimeMessage[] mimeMessages = mailService.getReceivedMessagesAndClear();

        // 1. Check the HTML email
        MimeMessage message = getMessageForAddress(mimeMessages, "fred@example.com");
        String subject = message.getSubject();
        String body = message.getContent().toString();
        assertEquals("[JIRATEST] Administrator shared the filter \"Funky Homosapiens\" with you", subject);
        assertTrue(body.contains("Administrator</a>"));
        assertTrue(body.contains("shared a filter with you"));
        assertTrue(body.contains(">I thought you should know<"));
        assertTrue(body.contains("/secure/IssueNavigator.jspa?mode=hide&requestId=" + filterId + "\">View Filter"));

        // 2. Check the Text email
        message = getMessageForAddress(mimeMessages, "admin@example.com");
        subject = message.getSubject();
        body = message.getContent().toString();
        assertMessageIsText(message);
        assertEquals("[JIRATEST] Administrator shared the filter \"Funky Homosapiens\" with you", subject);
        assertTrue(body.contains("Administrator shared a filter with you"));
        assertTrue(body.contains("I thought you should know"));
        assertTrue(body.contains("/secure/IssueNavigator.jspa?mode=hide&requestId=" + filterId));

        // 3. Check that the email sent to the email address links to JQL, not the filter
        message = getMessageForAddress(mimeMessages, "fake@example.com");
        subject = message.getSubject();
        body = message.getContent().toString();
        assertMessageIsHtml(message);
        assertEquals("[JIRATEST] Administrator shared a search result with you", subject);
        assertTrue(body.contains("Administrator</a>"));
        assertTrue(body.contains("shared a search result with you"));
        assertTrue(body.contains(">I thought you should know<"));
        assertTrue(body.contains("/secure/IssueNavigator.jspa?reset=true&jqlQuery=project+%3D+HSP\">View search results"));
    }

    public void _testShareJqlSearch() throws Exception
    {
        String searchJql = "project = HSP";

        Set<String> usernames = Sets.newHashSet("fred", "admin");
        String comment = "I thought you should know";
        shareClient.shareSearchQuery(searchJql, usernames, null, comment);

        flushMailQueueAndWait(2);
        MimeMessage[] mimeMessages = mailService.getReceivedMessages();

        // 1. Check the HTML email
        MimeMessage message = getMessageForAddress(mimeMessages, "fred@example.com");
        String subject = message.getSubject();
        String body = message.getContent().toString();
        assertMessageIsHtml(message);
        assertEquals("[JIRATEST] Administrator shared a search result with you", subject);
        assertTrue(body.contains("Administrator</a>"));
        assertTrue(body.contains("shared a search result with you"));
        assertTrue(body.contains(">I thought you should know<"));
        assertTrue(body.contains("/secure/IssueNavigator.jspa?reset=true&jqlQuery=project+%3D+HSP\">View search results"));

        // 2. Check the Text email
        message = getMessageForAddress(mimeMessages, "admin@example.com");
        subject = message.getSubject();
        body = message.getContent().toString();
        assertMessageIsText(mimeMessages[0]);
        assertEquals("[JIRATEST] Administrator shared a search result with you", subject);
        assertTrue(body.contains("Administrator shared a search result with you"));
        assertTrue(body.contains(comment));
        assertTrue(body.contains("/secure/IssueNavigator.jspa?reset=true&jqlQuery=project+%3D+HSP"));
    }

    private void assertMessageIsText(MimeMessage textMessage) throws MessagingException
    {
        assertEquals("text/plain; charset=UTF-8", textMessage.getContentType());
    }

    private void assertMessageIsHtml(MimeMessage htmlMessage) throws MessagingException
    {
        assertEquals("text/html; charset=UTF-8", htmlMessage.getContentType());
    }

    private MimeMessage getMessageForAddress(MimeMessage[] messages, String toAddress) throws MessagingException
    {
        for (MimeMessage message : messages)
        {
            Address[] recipients = message.getRecipients(Message.RecipientType.TO);
            if (recipients[0].toString().equals(toAddress))
            {
                return message;
            }
        }
        fail("Didn't find a message for : " + toAddress);
        return null;
    }
}
