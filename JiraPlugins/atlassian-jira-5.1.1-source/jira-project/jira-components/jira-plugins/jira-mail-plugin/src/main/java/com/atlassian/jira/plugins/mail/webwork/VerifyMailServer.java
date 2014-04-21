/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.mail.webwork;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.mail.server.MailServer;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

abstract class VerifyMailServer
{
    protected final Collection<String> errors = new ArrayList<String>();
    protected final static Logger log = Logger.getLogger(VerifyMailServer.class);

    public boolean hasErrors()
    {
          return errors.size() > 0;
    }

    public Collection<String> getErrorMessages()
    {
        return errors;
    }

    protected I18nHelper getI18nHelper() {
        return ComponentAccessor.getJiraAuthenticationContext().getI18nHelper();
    }

    public abstract void verifyMailServer(MailServer server);

    protected void addTimeouts(MailServer server, long verifyTimeout)
    {
        Properties p = server.getProperties();
        final String protocol = p.getProperty("mail.transport.protocol");
        final String connectionTimeout = String.format("mail.%s.connectiontimeout", protocol);
        final String socketTimeout = String.format("mail.%s.timeout", protocol);
        p.setProperty(connectionTimeout, "" + verifyTimeout);
        p.setProperty(socketTimeout, "" + verifyTimeout);
    }
}
