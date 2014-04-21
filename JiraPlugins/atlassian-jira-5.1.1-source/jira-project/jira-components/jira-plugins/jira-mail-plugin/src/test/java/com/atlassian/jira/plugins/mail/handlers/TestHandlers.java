/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.plugins.mail.handlers;


import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.mail.MailLoggingManager;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.plugins.mail.internal.DefaultMessageHandlerFactory;
import com.atlassian.jira.service.util.handler.MessageUserProcessor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class TestHandlers
{
    private MockComponentWorker worker;
    private ComponentAccessor.Worker initialWorker;
    private MailLoggingManager mailLoggingManager;

    @Before
    public void setUpComponentAccessor()
    {
        worker = new MockComponentWorker();
        initialWorker = ComponentAccessor.initialiseWorker(worker);

        mailLoggingManager = Mockito.mock(MailLoggingManager.class, Mockito.RETURNS_MOCKS);
        worker.addMock(MailLoggingManager.class, mailLoggingManager);
        worker.addMock(MessageUserProcessor.class, Mockito.mock(MessageUserProcessor.class, Mockito.RETURNS_MOCKS));
    }

    @After
    public void tearDownComponentAccessor()
    {
        ComponentAccessor.initialiseWorker(initialWorker);
    }

    @Test
    public void testInstantiationOfCreateOrCommentHandler()
    {
        _testInstantiation("com.atlassian.jira.plugins.mail.handlers.CreateOrCommentHandler");
    }

    @Test
    public void testInstantiationOfCVSLogHandler()
    {
        _testInstantiation("com.atlassian.jira.plugins.mail.handlers.CVSLogHandler");
    }

    @Test
    public void testInstantiationOfFullCommentHandler()
    {
        _testInstantiation("com.atlassian.jira.plugins.mail.handlers.NonQuotedCommentHandler");
    }

    @Test
    public void testInstantiationOfCreateIssueHandler()
    {
        _testInstantiation("com.atlassian.jira.plugins.mail.handlers.CreateIssueHandler");
    }

    // Ensure that the handlers can be instanciated
    public void _testInstantiation(String clazz)
    {
        try
        {
            Class.forName(clazz);
        }
        catch (ClassNotFoundException e)
        {
            throw new RuntimeException(e);
        }
    }
}
