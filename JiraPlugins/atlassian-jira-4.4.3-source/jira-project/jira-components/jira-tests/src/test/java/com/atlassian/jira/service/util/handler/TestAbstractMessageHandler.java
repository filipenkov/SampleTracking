package com.atlassian.jira.service.util.handler;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.JiraApplicationContext;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.mail.Email;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.jira.web.bean.MockI18nBean;
import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.junit.Test;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;
import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for {@link AbstractMessageHandler}.
 *
 * @since v3.13
 */
public class TestAbstractMessageHandler extends ListeningTestCase
{
    @Test
    public void testFingerPrintHeaderNoMatch()
    {
        testFingerPrint(null, new String[] { "fingerprint-xyz456" }, true);
    }

    @Test
    public void testFingerPrintHeaderGrubby()
    {
        testFingerPrint(null, new String[] { "fingerprint-abc123" }, false);
    }

    @Test
    public void testFingerPrintHeaderGrubbyMultiHeaders()
    {
        testFingerPrint(null, new String[] { "fingerprint-someother", "fingerprint-abc123", null }, false);
    }

    /**
     * Make sure that the handler assumes that illegal addresses mean illegal messages. In this case, we must return
     * false and add an error message to the handler.
     *
     * @throws Exception just rethrow so the test fails.
     */
    @Test
    public void testCanHandleMessage() throws Exception
    {
        Message message = HandlerTestUtil.createMessageFromFile("BadAddress.msg");

        final AbstractMessageHandler handler = createSimpleAbstractMessageHandler();
        final MessageErrorHandler messageErrorHandler = new MessageErrorHandler();

        handler.setErrorHandler(messageErrorHandler);
        handler.init(EasyMap.build(AbstractMessageHandler.KEY_CATCHEMAIL, "some_random_email@nowhere.ak"));
        assertFalse(handler.canHandleMessage(message));
        assertTrue(messageErrorHandler.hasErrors());
    }

    /**
     * JRA-13996: Make sure that handler correctly parses the "notifyusers" parameter
     */
    @Test
    public void testNotifyUsers()
    {
        final ApplicationProperties props = new MockApplicationProperties();

        //Make sure that it is enabled by default.
        AbstractMessageHandler handler = createSimpleAbstractMessageHandler(props);
        handler.init(Collections.EMPTY_MAP);
        assertTrue(handler.notifyUsers);

        //Make sure that it is enabled by default.
        handler = createSimpleAbstractMessageHandler(props);
        handler.init(EasyMap.build("createusers", "true"));
        assertTrue(handler.notifyUsers);

        //Make sure notifyusers is false on empty string.
        handler = createSimpleAbstractMessageHandler(props);
        handler.init(EasyMap.build("notifyusers", "", "createusers", "true"));
        assertFalse(handler.notifyUsers);

        //Make sure notifyusers is false on "saska".
        handler = createSimpleAbstractMessageHandler(props);
        handler.init(EasyMap.build("notifyusers", "saska", "createusers", "true"));
        assertFalse(handler.notifyUsers);

        //Make sure notifyusers is false on "false".
        handler = createSimpleAbstractMessageHandler(props);
        handler.init(EasyMap.build("notifyusers", "false", "createusers", "true"));
        assertFalse(handler.notifyUsers);

        //Make sure notifyusers is false on "tru".
        handler = createSimpleAbstractMessageHandler(props);
        handler.init(EasyMap.build("notifyusers", "tru", "createusers", "true"));
        assertFalse(handler.notifyUsers);

        //Make sure that true works.
        handler = createSimpleAbstractMessageHandler(props);
        handler.init(EasyMap.build("notifyusers", "true", "createusers", "true"));
        assertTrue(handler.notifyUsers);
    }

    /**
     * Although the message has JIRA's fingerprints on it, test that a handler configured to accept this will pass the
     * fingerprint check.
     */
    @Test
    public void testFingerPrintGrubbyAccept()
    {
        testFingerPrint(AbstractMessageHandler.VALUE_FINGER_PRINT_ACCEPT, new String[] { "fingerprint-abc123" }, true);
    }

    @Test
    public void testFingerPrintGrubbyIgnore()
    {
        testFingerPrint(AbstractMessageHandler.VALUE_FINGER_PRINT_IGNORE, new String[] { "fingerprint-abc123" }, false);
    }

    private void testFingerPrint(String policy, String[] headerValues, boolean expectedCheckResult)
    {
        AbstractMessageHandler abstractMessageHandler = createSimpleAbstractMessageHandler();
        if (policy != null)
        {
            abstractMessageHandler.setFingerPrintPolicy(policy);
        }
        final MockControl mockControl = MockClassControl.createControl(Message.class);
        final Message mockMessage = (Message) mockControl.getMock();
        try
        {
            mockMessage.getHeader(Email.HEADER_JIRA_FINGER_PRINT);
        }
        catch (MessagingException e)
        {
            throw new RuntimeException(e);
        }
        mockControl.setReturnValue(headerValues);
        mockControl.replay();
        assertEquals(expectedCheckResult, abstractMessageHandler.fingerPrintCheck(mockMessage));
        mockControl.verify();
    }

    private static AbstractMessageHandler createSimpleAbstractMessageHandler()
    {
        return createSimpleAbstractMessageHandler(null);
    }

    private static AbstractMessageHandler createSimpleAbstractMessageHandler(final ApplicationProperties applicationProperties)
    {
        AbstractMessageHandler handler = new MockMessageHandler(null, null, applicationProperties, new MockJiraApplicationContext());
        handler.setFingerPrintPolicy(AbstractMessageHandler.VALUE_FINGER_PRINT_FORWARD);
        return handler;
    }

    private static class MockJiraApplicationContext implements JiraApplicationContext
    {
        public String getFingerPrint()
        {
            return "fingerprint-abc123";
        }
    }

    private static class MockMessageHandler extends AbstractMessageHandler
    {
        public MockMessageHandler(final CommentManager commentManager, final IssueFactory issueFactory, final ApplicationProperties applicationProperties, final JiraApplicationContext jiraApplicationContext)
        {
            super(commentManager, issueFactory, applicationProperties, jiraApplicationContext);
        }

        public boolean handleMessage(Message message) throws MessagingException
        {
            throw new IllegalStateException("I shouldn't handle messages");
        }

        protected boolean attachPlainTextParts(final Part part) throws MessagingException, IOException
        {
            throw new UnsupportedOperationException();
        }

        protected boolean attachHtmlParts(final Part part) throws MessagingException, IOException
        {
            throw new UnsupportedOperationException();
        }

        protected I18nBean getI18nBean()
        {
            return new MockI18nBean();
        }
    }
}
