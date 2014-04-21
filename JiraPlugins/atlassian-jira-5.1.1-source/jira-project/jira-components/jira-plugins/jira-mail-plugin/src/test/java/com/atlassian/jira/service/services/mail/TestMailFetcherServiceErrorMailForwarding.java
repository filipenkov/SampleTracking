package com.atlassian.jira.service.services.mail;

import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.ApplicationPropertiesImpl;
import com.atlassian.jira.config.properties.ApplicationPropertiesStore;
import com.atlassian.jira.config.properties.MemorySwitchToDatabaseBackedPropertiesManager;
import com.atlassian.jira.config.properties.PropertiesManager;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.mail.MailLoggingManager;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.plugins.mail.handlers.HandlerTestUtil;
import com.atlassian.jira.service.services.file.AbstractMessageHandlingService;
import com.atlassian.jira.service.util.handler.MessageHandler;
import com.atlassian.jira.service.util.handler.MessageHandlerContext;
import com.atlassian.jira.service.util.handler.MessageHandlerErrorCollector;
import com.atlassian.jira.service.util.handler.MessageHandlerFactory;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.mail.MailException;
import com.atlassian.mail.MailFactory;
import com.atlassian.mail.server.MailServer;
import com.atlassian.mail.server.MailServerManager;
import com.atlassian.mail.server.SMTPMailServer;
import com.google.common.collect.ImmutableList;
import com.opensymphony.module.propertyset.map.MapPropertySet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.mail.Message;
import javax.mail.MessagingException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestMailFetcherServiceErrorMailForwarding
{
    private MockComponentWorker worker;
    private MailFetcherService.MessageProvider messageProvider;
    private MapPropertySet ps;
    private String forwardEmail;

    class MyTestMessageHandler implements com.atlassian.jira.service.util.handler.MessageHandler
    {

        private final boolean[] handleResult;
        int counter;

        public MyTestMessageHandler(boolean ...handleResult)
        {
            this.handleResult = handleResult;
        }

        @Override
        public void init(Map<String, String> params, MessageHandlerErrorCollector errorCollector)
        {
        }

        @Override
        public boolean handleMessage(Message message, MessageHandlerContext context) throws MessagingException
        {
            context.getMonitor().error("My fake error");
            context.getMonitor().error("And another one");
            context.getMonitor().error("My fake exception", new NullPointerException());
            return handleResult[counter++];
        }
    }


    @Before
    public void setUp() throws MailException
    {
        worker = new MockComponentWorker();
        ComponentAccessor.initialiseWorker(worker);
        worker.addMock(MailLoggingManager.class, Mockito.mock(MailLoggingManager.class, Mockito.RETURNS_DEEP_STUBS));
        ApplicationPropertiesImpl applicationProperties = new ApplicationPropertiesImpl(new ApplicationPropertiesStore(new PropertiesManager(new MemorySwitchToDatabaseBackedPropertiesManager()), Mockito.mock(JiraHome.class, Mockito.RETURNS_DEFAULTS)));
        worker.addMock(ApplicationProperties.class, applicationProperties);
        final MailServerManager mailServerManager = Mockito.mock(MailServerManager.class);
        SMTPMailServer smtpMailServer = Mockito.mock(SMTPMailServer.class);
        Mockito.when(mailServerManager.getMailServer(Mockito.anyLong())).thenReturn(smtpMailServer);
        MailFactory.setServerManager(mailServerManager);
        messageProvider = new MailFetcherService.MessageProvider()
        {
            @Override
            public void getAndProcessMail(SingleMessageProcessor singleMessageProcessor, MailServer mailServer, MessageHandlerContext context)
            {
                final Iterable<String> messageFiles = ImmutableList.of("GmailHtml.msg");
                for (String messageFile : messageFiles)
                {
                    try
                    {
                        final Message message = HandlerTestUtil.createMessageFromFile(messageFile);
                        context.getMonitor().nextMessage(message);
                        singleMessageProcessor.process(message, context);
                    }
                    catch (Exception e)
                    {
                        throw new AssertionError(e);
                    }

                }
            }
        };

        forwardEmail = "wseliga@atlassian.com";
        final HashMap<String, Object> map = MapBuilder.<String, Object>newBuilder()
                .add(AbstractMessageHandlingService.KEY_HANDLER, MyTestMessageHandler.class.getName())
                .add(MailFetcherService.FORWARD_EMAIL, forwardEmail)
                .add(MailFetcherService.KEY_MAIL_SERVER, "123")
                .toHashMap();
        ps = new MapPropertySet();
        ps.setMap(map);

    }

    @Test
    public void testIsForwardErrorEmailGetsCorrectlyCalled() throws ObjectConfigurationException, MailException
    {
        worker.addMock(MessageHandlerFactory.class, new MyMessageHandlerFactory(false, false, true));
        final boolean[] forwardHasBeenFired = new boolean[1];

        final MailFetcherService service = new MailFetcherService(new MailFetcherService.ErrorEmailForwarder()
        {
            @Override
            public boolean forwardEmail(Message message, MessageHandlerContext context, String toAddress, String errorsAsString, String exceptionsAsString)
                    throws MailException
            {
                forwardHasBeenFired[0] = true;
                assertEquals(forwardEmail, toAddress);
                assertEquals("My fake error\n"
                        + "And another one\n"
                        + "My fake exception", errorsAsString);
                Assert.assertTrue(exceptionsAsString.startsWith("java.lang.NullPointerException"));
                assertTrue(exceptionsAsString.length() > 100);
                return false;
            }
        }, messageProvider);
        service.init(ps);
        service.run();
        Assert.assertTrue(forwardHasBeenFired[0]);
        // let's repeat it. The second run should produce exactly the same resutls - e.g. no accumulation of error messages or strings
        forwardHasBeenFired[0] = false;
        service.run();
        Assert.assertTrue(forwardHasBeenFired[0]);

        // finally (the 3rd run) our handler should accept the message and our forwarder then should not be called.
        forwardHasBeenFired[0] = false;
        service.run();
        Assert.assertFalse(forwardHasBeenFired[0]);
    }


    @Test
    public void testIsForwardErrorEmailIsNotCalledForHandledSuccessfulyMessages()
            throws ObjectConfigurationException, MailException
    {
        worker.addMock(MessageHandlerFactory.class, new MyMessageHandlerFactory(true, true));

        final MailFetcherService service = new MailFetcherService(new MailFetcherService.ErrorEmailForwarder()
        {
            @Override
            public boolean forwardEmail(Message message, MessageHandlerContext context, String toAddress, String errorsAsString, String exceptionsAsString)
                    throws MailException
            {
                fail("it should not be called - the message should have been successfully handled, so there should be no forwarding");
                return false;
            }
        }, messageProvider);
        service.init(ps);
        service.run();
        // let's repeat it. The second run should produce exactly the same resutls
        service.run();
    }

    private class MyMessageHandlerFactory implements MessageHandlerFactory
    {

        private final boolean[] handleResult;

        public MyMessageHandlerFactory(boolean ...handleResult)
        {
            this.handleResult = handleResult;
        }

        @Override
        public MessageHandler getHandler(String clazz)
        {
            return new MyTestMessageHandler(handleResult);
        }

        @Override
        public String getCorrespondingModuleDescriptorKey(String clazz)
        {
            return null;
        }
    }
}
