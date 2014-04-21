/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.service.services.imap;

import com.atlassian.configurable.ObjectConfiguration;
import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.service.services.mail.MailFetcherService;

/**
 * Connect to an IMAP mailbox specified by the 'mail-hostname', 'username' and 'password' parameters, and
 * for each message call handleMessage().
 * Reads mail from a specified IMAP 'foldername' JRA-6067 (default is INBOX)
 */
public class ImapService extends MailFetcherService
{
    private static final String FOLDER_NAME_KEY = "foldername";

    public String getProtocol(boolean useSSL)
    {
        if (useSSL)
            return "imaps";
        else
            return "imap";
    }

    protected String getFolderName()
    {
        try
        {
            if (getProperty(FOLDER_NAME_KEY) != null)
            {
                return getProperty(FOLDER_NAME_KEY);
            }
            else
            {
                return DEFAULT_FOLDER;
            }
        }
        catch (ObjectConfigurationException e)
        {
            throw new DataAccessException("Error retrieving foldername.", e);
        }
    }

    public ObjectConfiguration getObjectConfiguration() throws ObjectConfigurationException
    {
        return getObjectConfiguration("IMAPSERVICE", "services/com/atlassian/jira/service/services/imap/imapservice.xml", null);
    }

}
