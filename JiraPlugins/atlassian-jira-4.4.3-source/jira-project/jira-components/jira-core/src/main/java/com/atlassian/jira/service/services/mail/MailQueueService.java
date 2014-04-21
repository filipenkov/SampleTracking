/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.service.services.mail;

import com.atlassian.configurable.ObjectConfiguration;
import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.service.AbstractService;
import com.atlassian.mail.queue.MailQueue;
import com.opensymphony.module.propertyset.PropertySet;

public class MailQueueService extends AbstractService
{
    public void run()
    {
        MailQueue queue = ManagerFactory.getMailQueue();

        if (!queue.isSending())
        {
            queue.sendBuffer();
        }
    }

    public void init(PropertySet props) throws ObjectConfigurationException
    {
    }

    public boolean isUnique()
    {
        return true;
    }

    public boolean isInternal()
    {
        return true;
    }

    public void destroy()
    {
        run(); // run one last time to make sure we try to send the queue.
    }

    public ObjectConfiguration getObjectConfiguration() throws ObjectConfigurationException
    {
        return getObjectConfiguration("MAILQUEUESERVICE", "services/com/atlassian/jira/service/services/mail/mailservice.xml", null);
    }
}
