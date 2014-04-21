/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.service.util.handler;

import com.atlassian.jira.local.LegacyJiraMockTestCase;

import com.atlassian.core.util.ClassLoaderUtils;

public class TestHandlers extends LegacyJiraMockTestCase
{
    public TestHandlers(String s)
    {
        super(s);
    }

    public void testInstantiationOfCreateOrCommentHandler()
    {
        _testInstantiation("com.atlassian.jira.service.util.handler.CreateOrCommentHandler");
    }

    public void testInstantiationOfCVSLogHandler()
    {
        _testInstantiation("com.atlassian.jira.service.util.handler.CVSLogHandler");
    }

    public void testInstantiationOfFullCommentHandler()
    {
        _testInstantiation("com.atlassian.jira.service.util.handler.NonQuotedCommentHandler");
    }

    public void testInstantiationOfCreateIssueHandler()
    {
        _testInstantiation("com.atlassian.jira.service.util.handler.CreateIssueHandler");
    }

    // Ensure that the handlers can be instanciated
    public void _testInstantiation(String clazz)
    {
        try
        {
            MessageHandler m = (MessageHandler) ClassLoaderUtils.loadClass(clazz, TestHandlers.class).newInstance();
        }
        catch (InstantiationException e)
        {
            fail();
        }
        catch (IllegalAccessException e)
        {
            fail();
        }
        catch (ClassNotFoundException e)
        {
            fail();
        }
    }
}
