package com.atlassian.jira.webtests;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.FunctTestConstants;
import com.atlassian.jira.webtests.util.mail.MailService;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.opensymphony.util.TextUtils;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.net.BindException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import static com.atlassian.jira.webtests.JIRAServerSetup.*;

/**
 * This class extends FuncTestCase by adding methods to test emails being sent from JIRA.
 */
public class EmailFuncTestCase extends FuncTestCase implements FunctTestConstants
{
    public static final String DEAFULT_FROM_ADDRESS = "jiratest@atlassian.com";
    public static final String DEFAULT_SUBJECT_PREFIX = "[JIRATEST]";
    public static final String newline = "\r\n";

    protected MailService mailService;

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        mailService = new MailService(log);
    }

    public void tearDownTest()
    {
        mailService.stop();
    }

    /**
     * Use this method to start a {@link com.icegreen.greenmail.smtp.SmtpServer}. <p> This will also configure JIRA to
     * use this SMTP server in the admin section. You should call this after your data import. This will override any
     * existing mail servers setup already. </p> <p> A simple SMTP server proxy is started by first attempting to start
     * on a default port number. If this port is already used we try that port number plus one and so on for 10
     * attempts. this allows for multiple tests running in Bamboo concurrently, and also for a particular test machine
     * maybe using that port already. </p> <p> The tearDown() method will close the TCP socket. </p>
     */
    protected void configureAndStartSmtpServer()
    {
        configureAndStartMailServers(DEAFULT_FROM_ADDRESS, DEFAULT_SUBJECT_PREFIX, SMTP);
    }

    protected void configureAndStartSmtpServer(String from, String prefix)
    {
        configureAndStartMailServers(from, prefix, SMTP);
    }

    protected void configureAndStartMailServers(String from, String prefix, JIRAServerSetup... jiraServerSetups)
    {
        assertSendingMailIsEnabled();

        startMailService(jiraServerSetups);

        List<JIRAServerSetup> serverSetupList = Arrays.asList(jiraServerSetups);

        if (serverSetupList.contains(IMAP) || serverSetupList.contains(POP3))
        {
            setupJiraImapPopServer();
        }
        if (serverSetupList.contains(SMTP))
        {
            int smtpPort = mailService.getSmtpPort();
            log("Setting SMTP server to 'localhost:" + smtpPort + "'");
            backdoor.mailServers().addSmtpServer(from, prefix, smtpPort);
        }
    }

    protected void configureAndStartSmtpServerWithNotify()
    {
        configureAndStartSmtpServer();
        // Chances are you if you're testing mail you want this setting enabled from previous default
        navigation.userProfile().changeNotifyMyChanges(true);
    }

    protected void startMailService(JIRAServerSetup... jiraServerSetups)
    {
        try
        {
            mailService.configureAndStartGreenMail(jiraServerSetups);
        }
        catch (BindException e)
        {
            fail("Error: Could not start green mail server. See log for details.");
        }
    }

    /**
     * Given a comma seperated list of email addresses, returns a collection of the email addresses.
     *
     * @param emails comma seperated list of email addresses
     * @return collection of individual email address
     */
    protected Collection<String> parseEmailAddresses(String emails)
    {
        StringTokenizer st = new StringTokenizer(emails, ",");
        Collection<String> emailList = new ArrayList<String>();
        while (st.hasMoreTokens())
        {
            String email = st.nextToken().trim();
            if (TextUtils.stringSet(email))
            {
                emailList.add(email.trim());
            }
        }
        return emailList;
    }

    protected void assertRecipientsHaveMessages(Collection<String> recipients) throws MessagingException
    {
        for (String recipient : recipients)
        {
            assertFalse("Recipient '" + recipient + "' did not receive any messages", getMessagesForRecipient(recipient).isEmpty());
        }
    }

    protected List<MimeMessage> getMessagesForRecipient(String recipient) throws MessagingException
    {
        MimeMessage[] messages = mailService.getReceivedMessages();
        List<MimeMessage> ret = new ArrayList<MimeMessage>();

        for (MimeMessage message : messages)
        {
            if (Arrays.asList(message.getHeader("To")).contains(recipient))
            {
                ret.add(message);
            }
        }

        return ret;
    }

    private void assertSendingMailIsEnabled()
    {
        navigation.gotoAdmin();
        tester.clickLink("mail_queue");

        try
        {
            final String responseText = tester.getDialog().getResponse().getText();
            if (responseText.contains("Sending mail is disabled"))
            {
                fail("Mail sending is disabled. Please restart your server without -Datlassian.mail.senddisabled=true.");
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void setupJiraImapPopServer()
    {
        navigation.gotoAdmin();
        tester.clickLink("incoming_mail");
        tester.clickLinkWithText("Add POP / IMAP mail server");
        tester.setFormElement("name", "Local Test Pop/Imap Server");
        tester.setFormElement("serverName", "localhost");
        tester.setFormElement("username", ADMIN_USERNAME);
        tester.setFormElement("password", ADMIN_USERNAME);
        tester.submit("Add");
    }

    protected void setupPopService()
    {
        setupPopService("project=MKY, issue=1, createusers=true");
    }

    protected void setupPopService(String handlerParameters)
    {
        navigation.gotoAdmin();
        tester.clickLink("services");
        tester.setFormElement("name", "pop");
        tester.setFormElement("clazz", "com.atlassian.jira.service.services.mail.MailFetcherService");
        tester.setFormElement("delay", "1");
        tester.submit("Add Service");
        tester.setFormElement("handler.params", handlerParameters);
        tester.setFormElement("delay", "1");
        tester.submit("Update");

    }

    protected void setupImapService()
    {
        setupImapService("project=MKY, issue=1, createusers=true");
    }

    protected void setupImapService(String handlerParameters)
    {
        navigation.gotoAdmin();
        tester.clickLink("services");
        tester.setFormElement("name", "imap");
        tester.setFormElement("clazz", "com.atlassian.jira.service.services.mail.MailFetcherService");
        tester.setFormElement("delay", "1");
        tester.submit("Add Service");
        tester.setFormElement("handler.params", handlerParameters);
        tester.setFormElement("delay", "1");
        tester.submit("Update");
    }

    /**
     * This is useful for writing func tests that test that the correct notifications are being sent. It goest to the
     * admin section mail-queue and flushes the queue and waits till it recieves emailCount number of emails before
     * timeout. If the timeout is reached before the expected number of emails arrives will fail.
     *
     * @param emailCount number of expected emails to wait to receive
     * @throws InterruptedException if interrupted
     */
    protected void flushMailQueueAndWait(int emailCount) throws InterruptedException
    {
        flushMailQueueAndWait(emailCount, 500);
    }

    /**
     * Does the same as {@link #flushMailQueueAndWait(int)} but allows the user to specify the wait period in case a lot
     * of e-mails are being sent.
     *
     * @param emailCount number of expected emails to wait to receive
     * @param waitPeriodMillis The amout of time to wait in millis until the e-mails should have arrived.
     * @throws InterruptedException if interrupted
     */
    protected void flushMailQueueAndWait(int emailCount, int waitPeriodMillis) throws InterruptedException
    {
        //flush mail queue
        navigation.gotoAdmin();
        tester.clickLink("mail_queue");
        tester.clickLinkWithText("Flush mail queue");
        log("Flushed mail queue. Waiting for '" + waitPeriodMillis + "' ms...");
        // Sleep for a small while - just to be sure the mail is received.
        final boolean receivedAllMail = mailService.waitForIncomingMessage(waitPeriodMillis, emailCount);

        if (!receivedAllMail)
        {
            String msg = "Did not recieve all expected emails (" + emailCount + ") within the timeout.";
            MimeMessage[] receivedMessages = mailService.getReceivedMessages();
            if (receivedMessages != null)
            {
                msg += " Only received " + receivedMessages.length + " message(s).";
                if (receivedMessages.length > 0)
                {
                    msg += "\n  Recipients: " + display(receivedMessages);
                }
            }
            else
            {
                msg += " Received zero messages.";
            }
            fail(msg);
        }
    }

    private String display(MimeMessage[] receivedMessages)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < receivedMessages.length; i++)
        {
            if (i > 0)
                sb.append(", ");
            MimeMessage receivedMessage = receivedMessages[i];
            try
            {
                sb.append(receivedMessage.getRecipients(Message.RecipientType.TO)[0]);
            }
            catch (MessagingException e)
            {
                sb.append("???");
            }
        }
        return sb.toString();
    }

    protected void waitForMail(int emailCount) throws InterruptedException
    {
        final int waitPeriodMillis = 500;
        assertTrue("Did not recieve all expected emails within the timeout", mailService.waitForIncomingMessage(waitPeriodMillis, emailCount));
    }

    /**
     * Asserts that the given email's body contains the bodySubString using indexOf.
     *
     * @param email email to extract the content body from
     * @param bodySubString expected substring of the email body
     * @throws MessagingException Message error
     * @throws IOException IO error
     * @see GreenMailUtil#getBody(javax.mail.Part)
     */
    protected void assertEmailBodyContains(MimeMessage email, String bodySubString)
            throws MessagingException, IOException
    {
        final String emailBody = GreenMailUtil.getBody(email);
        assertTrue("The string '" + bodySubString + "' was not found in the e-mail body [" + emailBody + "]",
                emailBody.contains(bodySubString));
    }

    /**
     * Asserts that the given email's body contains a line which matches the given string or pattern.
     *
     * @param email email to extract the content body from
     * @param linePattern expected line or line pattern
     * @throws MessagingException Message error
     * @throws IOException IO error
     * @see GreenMailUtil#getBody(javax.mail.Part)
     */
    protected void assertEmailBodyContainsLine(MimeMessage email, String linePattern)
            throws MessagingException, IOException
    {
        final String emailBody = GreenMailUtil.getBody(email);
        String[] lines = emailBody.split("\\n");
        for (String line : lines)
        {
            if (line.trim().matches(linePattern))
                return;     // Found - all good!
        }

        fail("The line '" + linePattern + "' was not found in the e-mail body [" + emailBody + "]");
    }

    /**
     * Asserts that the given email's body does not contain the bodySubString using indexOf.
     *
     * @param email email to extract the content body from
     * @param bodySubString string to not occur in body
     * @throws MessagingException Message error
     * @throws IOException IO error
     * @see GreenMailUtil#getBody(javax.mail.Part)
     */
    protected void assertEmailBodyDoesntContain(MimeMessage email, String bodySubString)
            throws MessagingException, IOException
    {
        final String emailBody = GreenMailUtil.getBody(email);
        assertTrue("The string '" + bodySubString + "' was found (shouldn't exist) in the e-mail body [" + emailBody + "]",
                !emailBody.contains(bodySubString));
    }

    /**
     * Assert that the String emailBody contains bodySubString
     *
     * @param emailBody body
     * @param bodySubString expected substring
     * @throws MessagingException message error
     * @throws IOException IO error
     */
    protected void assertEmailBodyContains(String emailBody, String bodySubString)
            throws MessagingException, IOException
    {
        assertTrue("Expected '" + bodySubString + "' to be present in email body '" + emailBody + "'", emailBody.contains(bodySubString));
    }

    protected void assertEmailHasNumberOfParts(MimeMessage email, int expectedNumOfParts)
            throws MessagingException, IOException
    {
        Object emailContent = email.getContent();
        if (emailContent instanceof Multipart)
        {
            Multipart multiPart = (Multipart) emailContent;
            assertEquals(expectedNumOfParts, multiPart.getCount());
        }
        else
        {
            fail("Cannot assert number of parts for email. Email is not a multipart type.");
        }
    }

    /**
     * Assert that the email was addressed to the expectedTo
     *
     * @param email email to assert the value of the to header
     * @param expectedTo the single or comma seperated list of expected email addresses
     * @throws MessagingException meesage error
     * @see #assertEmailToEquals(javax.mail.internet.MimeMessage, java.util.Collection)
     */
    protected void assertEmailToEquals(MimeMessage email, String expectedTo) throws MessagingException
    {
        assertEmailToEquals(email, parseEmailAddresses(expectedTo));
    }

    /**
     * Assert that the email was addressed to each and everyone of the expectedAddresses
     *
     * @param email email to assert the value of the to header
     * @param expectedToAddresses collection of expected email addresses
     * @throws MessagingException meesage error
     */
    protected void assertEmailToEquals(MimeMessage email, Collection expectedToAddresses) throws MessagingException
    {
        String[] toHeader = email.getHeader("to");
        assertEquals(1, toHeader.length);
        Collection actualAddresses = parseEmailAddresses(toHeader[0]);
        assertEmailsEquals(expectedToAddresses, actualAddresses);
    }

    protected void assertEmailCcEquals(MimeMessage email, Collection expectedCcAddresses) throws MessagingException
    {
        String[] ccHeader = email.getHeader("cc");
        if (ccHeader != null)
        {
            assertEquals(1, ccHeader.length);
            Collection actualAddresses = parseEmailAddresses(ccHeader[0]);
            assertEmailsEquals(expectedCcAddresses, actualAddresses);
        }
        else
        {
            //if there is no Cc header, assert that we were not expecting any emails.
            assertTrue("Expected Cc address but was null", expectedCcAddresses.isEmpty());
        }
    }

    private void assertEmailsEquals(Collection expectedAddresses, Collection actualAddresses)
    {
        assertEquals("Expected '" + expectedAddresses.size() + "' email addresses but only found '" + actualAddresses.size() + "'", expectedAddresses.size(), actualAddresses.size());
        assertEquals(expectedAddresses, actualAddresses);
    }

    protected void assertEmailFromEquals(MimeMessage email, String expectedTo) throws MessagingException
    {
        String[] addresses = email.getHeader("from");
        assertEquals(1, addresses.length);
        assertEquals(expectedTo, addresses[0]);
    }

    protected void assertEmailSubjectEquals(MimeMessage email, String subject) throws MessagingException
    {
        assertEquals(subject, email.getSubject());
    }

    protected void assertEmailSent(String recipient, String subject, String issueComment)
            throws MessagingException, IOException
    {
        final List emails = getMessagesForRecipient(recipient);
        assertEquals("Incorrect number of e-mails received for '" + recipient + "'", 1, emails.size());
        final MimeMessage emailMessage = (MimeMessage) emails.get(0);
        assertEmailBodyContains(emailMessage, issueComment);
        assertEmailSubjectEquals(emailMessage, subject);
    }

    protected void assertCorrectNumberEmailsSent(int numOfMessages)
            throws MessagingException
    {
        final MimeMessage[] messages = mailService.getReceivedMessages();
        if (messages.length != numOfMessages)
        {
            for (MimeMessage message : messages)
            {
                log("Mail sent to '" + message.getHeader("to")[0] + "' with SUBJECT '" + message.getSubject() + "'");
            }
            fail("Invalid number of e-mails received.  Was " + messages.length + " but should have been " + numOfMessages + ".");
        }
    }
}
