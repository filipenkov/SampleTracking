/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.service.services.pop;

import com.atlassian.configurable.ObjectConfiguration;
import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.jira.service.services.mail.MailFetcherService;

/**
 * Connect to a POP mailbox specified by the 'mail-hostname', 'username' and 'password' parameters, and
 * for each message call handleMessage().
 */
public class PopService extends MailFetcherService
{
    protected String getProtocol(boolean useSSL)
    {
        if (useSSL)
            return "pop3s";
        else
            return "pop3";
    }

    protected String getFolderName()
    {
        return DEFAULT_FOLDER;
    }

    public ObjectConfiguration getObjectConfiguration() throws ObjectConfigurationException
    {
        return getObjectConfiguration("POPSERVICE", "services/com/atlassian/jira/service/services/pop/popservice.xml", null);
    }
}
