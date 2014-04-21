/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.mail.queue.MailQueue;
import com.mockobjects.dynamic.Mock;
import org.picocontainer.ComponentAdapter;

public class TestMailQueueAdmin extends LegacyJiraMockTestCase
{
    private Mock mockMailQueue;
    private ComponentAdapter mailQueueAdapter;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        mockMailQueue = new Mock(MailQueue.class);
        UtilsForTests.cleanWebWork();
        mailQueueAdapter = ManagerFactory.addService(MailQueue.class, (MailQueue) mockMailQueue.proxy());
    }

    @Override
    protected void tearDown() throws Exception
    {
        ManagerFactory.addService(MailQueue.class, (MailQueue) mailQueueAdapter.getComponentInstance());
    }

    public void testNormalExecuteNoFlush() throws Exception
    {
        mockMailQueue.expectNotCalled("sendBuffer");

        final MailQueueAdmin mqa = new MailQueueAdmin();

        mqa.execute();
        mockMailQueue.verify();
    }

    public void testExecuteWithFlush() throws Exception
    {
        ManagerFactory.addService(MailQueue.class, (MailQueue) mockMailQueue.proxy());
        mockMailQueue.expectVoid("sendBuffer");

        final MailQueueAdmin mqa = new MailQueueAdmin();
        mqa.setFlush(true);

        mqa.execute();
        mockMailQueue.verify();
    }
}
