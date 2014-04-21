/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.mail.webwork;

/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.mail.MailException;
import com.atlassian.mail.server.MailServer;
import com.atlassian.mail.server.MailServerManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;

@WebSudoRequired
public class DeleteMailServer extends MailServerActionSupport
{
    private boolean confirmed;

    private final MailServerManager mailServerManager;

    public DeleteMailServer(final MailServerManager mailServerManager)
    {
        this.mailServerManager = mailServerManager;
    }

    protected void doValidation()
    {
        if (getId() == null || !isConfirmed())
        {
            addErrorMessage(getText("admin.errors.mail.confirm.deletion.of.server"));
        }
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        if (canDeleteMailServer())
        {
            final boolean wasSmtp = isSmtp(mailServerManager.getMailServer(getId()));
            mailServerManager.delete(getId());
            return getRedirect(wasSmtp ? ViewMailServers.OUTGOING_MAIL_ACTION : ViewMailServers.INCOMING_MAIL_ACTION);
        }
        else
        {
            return "securitybreach";
        }
    }

    private boolean canDeleteMailServer() throws MailException
    {
        MailServer mailServer = mailServerManager.getMailServer(getId());

        if (mailServer == null)
        {
            return false;
        }
        else if (isPop(mailServer))
        {
            return canManagePopMailServers();
        }
        else if (isSmtp(mailServer))
        {
            return canManageSmtpMailServers();
        }
        return false;
    }

    public boolean isConfirmed()
    {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed)
    {
        this.confirmed = confirmed;
    }

    @Override
    public String getActiveTab()
    {
        return MailServerManager.SERVER_TYPES[1].equals(getType()) ? ViewMailServers.OUTGOING_MAIL_TAB : ViewMailServers.INCOMING_MAIL_TAB;
    }

    public String getCancelURI()
    {
        return MailServerManager.SERVER_TYPES[1].equals(getType()) ? ViewMailServers.OUTGOING_MAIL_ACTION : ViewMailServers.INCOMING_MAIL_ACTION;
    }

}

