package com.atlassian.jira.mail;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.mail.MailException;
import org.apache.log4j.Logger;

/**
 * Helper methods for common mail related operations.
 */
public class JiraMailUtils
{
    private static final Logger log = Logger.getLogger(JiraMailUtils.class);

    public static boolean isHasMailServer()
    {
        try
        {
            return (ComponentAccessor.getMailServerManager().getDefaultSMTPMailServer() != null);
        }
        catch (MailException e)
        {
            log.error("Error occurred while retrieving mail server information.", e);
            return false;
        }
    }
}
