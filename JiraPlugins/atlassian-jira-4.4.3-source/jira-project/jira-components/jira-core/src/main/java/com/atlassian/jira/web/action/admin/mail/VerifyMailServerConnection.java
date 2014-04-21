package com.atlassian.jira.web.action.admin.mail;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.mail.server.MailServer;
import com.atlassian.mail.server.PopMailServer;
import org.apache.log4j.Logger;
import webwork.action.Action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

public class VerifyMailServerConnection extends MailServerActionSupport
{

    protected static long verifyTimeout;

    public String doDefault() throws Exception
    {
        return Action.INPUT;
    }

    protected String doExecute() throws Exception
    {
        return Action.SUCCESS;
    }

    protected void validateServer(final MailServer mailServer, VerifyMailServer verifier)
    {
        verifier.verifyMailServer(mailServer);
        if (verifier.hasErrors())
        {
            setErrorMessages(verifier.getErrorMessages());
        }
    }
  
    static abstract class VerifyMailServer
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

        protected Properties getServerProperties(final MailServer server)
        {
            Properties props = new Properties();
            String protocol = server.getMailProtocol().getProtocol();

            props.put("mail."+protocol+".host", server.getHostname());
            props.put("mail."+protocol+".port", server.getPort());
            props.put("mail."+protocol+".connectiontimeout",""+VerifyMailServerConnection.verifyTimeout);
            props.put("mail."+protocol + ".timeout",""+VerifyMailServerConnection.verifyTimeout);
            if (server instanceof PopMailServer) {
                props.put("mail.store.protocol", protocol);
            }
            return props;
        }
    }
}
