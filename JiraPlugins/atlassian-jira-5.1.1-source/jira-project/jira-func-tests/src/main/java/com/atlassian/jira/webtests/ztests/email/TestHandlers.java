package com.atlassian.jira.webtests.ztests.email;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.EmailFuncTestCase;
import com.atlassian.jira.webtests.JIRAServerSetup;

/**
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.EMAIL })
// Passing because most of the test is commented out. The rationale for commenting it out is not valid anymore, so we
// probably should introduce it again
public class TestHandlers extends EmailFuncTestCase
{
    public void setUpTest()
    {
        super.setUpTest();
        administration.restoreBlankInstance();
        navigation.gotoDashboard();

        configureAndStartMailServers("admin@example.com", "PRE", JIRAServerSetup.ALL);
    }

    /**
     * As part of https://jdog.atlassian.com/browse/JRADEV-7738 port was removed from service settings
     */
    public void testPortsNotInService()
    {
        setupPopService();
        tester.assertTextNotPresent("port:</strong> " + mailService.getPop3Port());

        setupImapService();
        tester.assertTextNotPresent("port:</strong> " + mailService.getImapPort());
    }

    /*
     * We can't really write func tests for the handlers until we can make them
     * run on demand. Waiting for a couple of seconds for the scheduler to run the handlers
     * means the tests a) take forever and b) are fragile (i.e. if you don't wait long enough).
     * Until a "run now" is added for handlers, we'll leave these commented out.
     */

    /*
    private void sendTextEmail(String to, String from, String subject, String msg, final ServerSetup setup)
    {
        try
        {
            Session session = getGreenMail().util().getSession(setup);

            Address[] tos = new javax.mail.Address[0];
            tos = new InternetAddress[] { new InternetAddress(to) };
            Address[] froms = new InternetAddress[] { new InternetAddress(from) };
            MimeMessage mimeMessage = new MimeMessage(session);
            mimeMessage.setSubject(subject);
            mimeMessage.setFrom(froms[0]);

            mimeMessage.setRecipients(javax.mail.Message.RecipientType.TO, tos);

            mimeMessage.setText(msg);
            Transport.send(mimeMessage, tos);
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
    }

    public void testCreateIssueFromEmail() throws MessagingException, InterruptedException
    {
        sendTextEmail("admin@example.com", "fred@example.com", "the monkeys escaped", "aarrrgh!", JIRAServerSetup.SMTP);
        flushMailQueueAndWait(1);

        setupImapService();
        gotoAdmin();
        tester.clickLink("services");
        tester.setFormElement("name", "");
        tester.clickLink("edit_10010");
        tester.submit("Update");
        tester.setFormElement("name", "");

        Thread.sleep(600000);
        gotoIssue("MKY-1");
    }
    */
}
