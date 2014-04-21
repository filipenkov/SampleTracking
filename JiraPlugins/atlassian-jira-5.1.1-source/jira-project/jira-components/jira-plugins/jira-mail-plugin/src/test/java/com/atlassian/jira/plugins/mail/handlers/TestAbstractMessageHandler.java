package com.atlassian.jira.plugins.mail.handlers;

import com.atlassian.jira.JiraApplicationContext;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.mail.Email;
import com.atlassian.jira.mail.MailLoggingManager;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.service.util.handler.MessageHandlerContext;
import com.atlassian.jira.service.util.handler.MessageUserProcessor;
import com.atlassian.jira.user.util.MockUserManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.jira.web.bean.MockI18nBean;
import org.apache.log4j.Logger;
import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.stubbing.answers.Returns;

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
public class TestAbstractMessageHandler
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
        final SimpleTestMessageHandlerExecutionMonitor monitor = new SimpleTestMessageHandlerExecutionMonitor();

        handler.init(MapBuilder.build(AbstractMessageHandler.KEY_CATCHEMAIL, "some_random_email@nowhere.ak"), monitor);
        assertFalse(handler.canHandleMessage(message, monitor));
        assertTrue(monitor.hasErrors());
    }

    /**
     * JRA-13996: Make sure that handler correctly parses the "notifyusers" parameter
     */
    @Test
    public void testNotifyUsers()
    {
        final ApplicationProperties props = new MockApplicationProperties();
        final SimpleTestMessageHandlerExecutionMonitor monitor = new SimpleTestMessageHandlerExecutionMonitor();

        //Make sure that it is enabled by default.
        AbstractMessageHandler handler = createSimpleAbstractMessageHandler(props);
        handler.init(Collections.<String, String>emptyMap(), monitor);
        assertTrue(handler.notifyUsers);

        //Make sure that it is enabled by default.
        handler = createSimpleAbstractMessageHandler(props);
        handler.init(MapBuilder.build("createusers", "true"), monitor);
        assertTrue(handler.notifyUsers);

        //Make sure notifyusers is false on empty string.
        handler = createSimpleAbstractMessageHandler(props);
        handler.init(MapBuilder.build("notifyusers", "", "createusers", "true"), monitor);
        assertFalse(handler.notifyUsers);

        //Make sure notifyusers is false on "saska".
        handler = createSimpleAbstractMessageHandler(props);
        handler.init(MapBuilder.build("notifyusers", "saska", "createusers", "true"), monitor);
        assertFalse(handler.notifyUsers);

        //Make sure notifyusers is false on "false".
        handler = createSimpleAbstractMessageHandler(props);
        handler.init(MapBuilder.build("notifyusers", "false", "createusers", "true"), monitor);
        assertFalse(handler.notifyUsers);

        //Make sure notifyusers is false on "tru".
        handler = createSimpleAbstractMessageHandler(props);
        handler.init(MapBuilder.build("notifyusers", "tru", "createusers", "true"), monitor);
        assertFalse(handler.notifyUsers);

        //Make sure that true works.
        handler = createSimpleAbstractMessageHandler(props);
        handler.init(MapBuilder.build("notifyusers", "true", "createusers", "true"), monitor);
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
        assertEquals(expectedCheckResult, abstractMessageHandler.fingerPrintCheck(mockMessage, new SimpleTestMessageHandlerExecutionMonitor()));
        mockControl.verify();
    }

    private static AbstractMessageHandler createSimpleAbstractMessageHandler()
    {
        return createSimpleAbstractMessageHandler(null);
    }

    private static AbstractMessageHandler createSimpleAbstractMessageHandler(final ApplicationProperties applicationProperties)
    {
        final MailLoggingManager mockMailLoggingManager = Mockito.mock(MailLoggingManager.class);
        Mockito.when(mockMailLoggingManager.getIncomingMailChildLogger(Mockito.<String>any())).thenReturn(Logger.getLogger(TestAbstractMessageHandler.class));
        AbstractMessageHandler handler = new MockMessageHandler(new MockUserManager(), applicationProperties, new MockJiraApplicationContext(),
                mockMailLoggingManager, Mockito.mock(MessageUserProcessor.class), Mockito.mock(PermissionManager.class, new Returns(true)));
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
        public MockMessageHandler(UserManager userManager,
                final ApplicationProperties applicationProperties, final JiraApplicationContext jiraApplicationContext,
                MailLoggingManager mailLoggingManager, MessageUserProcessor messageUserProcessor, PermissionManager permissionManager)
        {
            super(userManager, applicationProperties, jiraApplicationContext, mailLoggingManager, messageUserProcessor, permissionManager);
        }

        @Override
        public boolean handleMessage(Message message, MessageHandlerContext context)
                throws MessagingException
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
