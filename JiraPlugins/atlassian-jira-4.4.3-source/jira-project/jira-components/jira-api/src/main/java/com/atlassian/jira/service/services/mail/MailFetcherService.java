/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.service.services.mail;

import com.atlassian.configurable.ObjectConfigurable;
import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.mail.Email;
import com.atlassian.jira.service.services.file.AbstractMessageHandlingService;
import com.atlassian.jira.service.util.handler.MessageErrorHandler;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.PortUtil;
import com.atlassian.mail.MailException;
import com.atlassian.mail.MailFactory;
import com.atlassian.mail.server.MailServer;
import com.atlassian.mail.server.SMTPMailServer;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.util.TextUtils;
import com.sun.mail.pop3.POP3Message;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.exception.VelocityException;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * An abstract mail fetcher for POP and IMAP protocols
 */
public abstract class MailFetcherService extends AbstractMessageHandlingService implements ObjectConfigurable
{
    private static final Logger log = Logger.getLogger(MailFetcherService.class);

    private static final String OLD_MAIL_DISABLED_KEY = "atlassian.mail.popdisabled";
    private static final String MAIL_DISABLED_KEY = "atlassian.mail.fetchdisabled";
    private static final String KEY_PORT = "port";
    private static final String KEY_MAIL_SERVER = "popserver";
    protected Long mailserverId = null;
    public static final String USE_SSL = "usessl";
    public static final String FORWARD_EMAIL = "forwardEmail";
    protected static final String DEFAULT_FOLDER = "INBOX";

    private static final String EMAIL_TEMPLATES = "templates/email/";
    private final ApplicationProperties applicationProperties = ComponentAccessor.getApplicationProperties();
    private final String baseUrl = applicationProperties.getString(APKeys.JIRA_BASEURL);
    private static final String ERROR_TEMPLATE = "errorinhandler.vm";

    // -1 means "use the default for this service" in the javax.mail API for connect
    private int port = -1;

    /**
     * MUST pass 'mail-hostname', 'username' and 'password' as parameters
     */
    public void init(PropertySet props) throws ObjectConfigurationException
    {
        super.init(props);
        if (hasProperty(KEY_MAIL_SERVER))
        {
            try
            {
                this.mailserverId = new Long(getProperty(KEY_MAIL_SERVER));
            }
            catch (Exception e)
            {
                log.error("Invalid mail server id: " + e, e);
            }
        }
        if (hasProperty(KEY_PORT))
        {
            final int parsedPort = PortUtil.parsePort(getProperty(KEY_PORT));
            if (parsedPort >= 0)
            {
                port = parsedPort;
            }
            else
            {
                port = -1;
                log.error("Invalid port number: " + getProperty(KEY_PORT) + " for mail service: " + getName() + ". Using the default port for this service type.");
            }
        }
    }

    /**
     * Connect to the POP / IMAPemail box and then handle each message.
     */
    public void run()
    {
        // TODO break me up please!
        if (isMailDisabled())
        {
            return;
        }

        MailServer mailserver = getMailServer();
        if (mailserver == null)
        {
            return;
        }
        String hostname = mailserver.getHostname();
        String username = mailserver.getUsername();
        String password = mailserver.getPassword();
        if (hostname == null || username == null || password == null)
        {
            log.warn(addHandlerInfo("Cannot retrieve mail due to a missing parameter in Mail Server '" + mailserver.getName() + "': [host," + hostname + "],[username," + username + "],[password," + password + "]"));
            return;
        }

        Store store;
        Folder folder = null;
        Properties props = mailserver.getProperties();
        Session session = Session.getInstance(props, null);

        String protocol = null;
        try
        {
            protocol = getProtocol(useSSL());
            store = session.getStore(protocol);

        }
        catch (NoSuchProviderException e)
        {
            log.error(addHandlerInfo("Error getting provider for protocol " + protocol + ": " + e), e);
            return;
        }

        try
        {
            store.connect(hostname, port, username, password);
        }
        catch (MessagingException e)
        {
            log.error(addHandlerInfo("Error connecting to host '" + hostname + "' as user '" + username + "' via protocol '"
                    + protocol + "': " + e), e);
            return;
        }

        try
        {
            folder = store.getFolder(getFolderName());
            folder.open(Folder.READ_WRITE);

            Message[] messages = folder.getMessages();

            log.debug(addHandlerInfo("There are " + messages.length + " messages in the " + protocol + " folder"));

            for (Message message : messages)
            {
                MessageErrorHandler errorHandler = new MessageErrorHandler();
                getHandler().setErrorHandler(errorHandler);
                boolean deleteThisMessage = false;
                String msgId = null;
                try
                {
                    msgId = message.getHeader("Message-ID") != null ? message.getHeader("Message-ID")[0] : "null";
                    if (log.isDebugEnabled())
                    {
                        try
                        {
                            log.debug("Message Subject: " + message.getSubject());
                            log.debug("Message-ID: " + msgId);
                        }
                        catch (MessagingException e)
                        {
                            log.warn("Messaging exception thrown on getting message subject. Message may have corrupt headers.", e);
                        }
                    }

                    deleteThisMessage = getHandler().handleMessage(message);
                    // if there is any error, forwarding is configured and we should not deleteThisMessage, then attempt a forward
                    if (errorHandler.getError() != null && forwardEmailParam() != null && !deleteThisMessage)
                    {
                        log.debug("Forwarding Message: " + msgId);
                        // if the forward was successful we want to delete the email, otherwise not
                        deleteThisMessage = forwardEmail(message, errorHandler);
                    }
                }
                catch (Exception e)
                {
                    log.error(addHandlerInfo("Exception: " + e.getLocalizedMessage()), e);
                }
                finally
                {
                    if (message != null)
                    {
                        // This fixes JRA-11046 - the pop messages hold onto the attachments in memory and since we
                        // process all the messages at once we need to make sure we only ever need one attachment
                        // in memory at a time. For IMAP this problem does not exist.
                        if (message instanceof POP3Message)
                        {
                            ((POP3Message) message).invalidate(true);
                        }

                        if (deleteThisMessage)
                        {
                            log.debug("Deleting Message: " + msgId);
                            message.setFlag(Flags.Flag.DELETED, true);
                        }
                    }
                }
            }
        }
        catch (MessagingException e)
        {
            log.error(addHandlerInfo("Messaging Exception in service '" + getClass().getName() + "' when getting mail: " + e.getMessage()), e);
        }
        finally
        {
            try
            {
                if (folder != null)
                {
                    folder.close(true); //expunge any deleted messages
                }
                store.close();
            }
            catch (Exception e)
            {
                log.debug(addHandlerInfo("Error whilst closing folder and store: " + e.getMessage()));
            }
        }
    }

    /**
     * Gets the mail server or null if none is defined. Will also return null if there is a problem getting the
     * mailserver.
     *
     * @return the mail server or null.
     */
    MailServer getMailServer()
    {
        MailServer mailserver = null;
        if (mailserverId != null)
        {
            try
            {
                // TODO resolve this weird inconsistency - we seem to assume that mailserverId and the return
                // TODO value from getProperty(KEY_MAIL_SERVER) will be in sync but this invariant is
                // TODO not controlled by this class. Shouldn't we use mailServerId here too?
                // TODO I don't have time to verify that this is OK to change right now
                mailserver = MailFactory.getServerManager().getMailServer(new Long(getProperty(KEY_MAIL_SERVER)));
            }
            catch (Exception e)
            {
                log.error(addHandlerInfo("Could not retrieve mail server: " + e), e);
            }
        }
        else
        {
            log.error(getClass().getName() + " cannot run without a configured Mail Server");
        }
        return mailserver;
    }

    boolean isMailDisabled()
    {
        if (Boolean.getBoolean(OLD_MAIL_DISABLED_KEY))
        {
            log.info("Service disabled by '" + OLD_MAIL_DISABLED_KEY + "' property.");
            return true;
        }
        if (Boolean.getBoolean(MAIL_DISABLED_KEY))
        {
            log.info("Service disabled by '" + MAIL_DISABLED_KEY + "' property.");
            return true;
        }
        return false;
    }

    protected abstract String getProtocol(boolean useSSL);

    protected abstract String getFolderName();

    private boolean useSSL()
    {
        try
        {
            return "true".equals(getProperty(USE_SSL));
        }
        catch (ObjectConfigurationException e)
        {
            throw new DataAccessException(addHandlerInfo("Error retrieving SSL flag."), e);
        }
    }


    private String forwardEmailParam()
    {
        try
        {
            return getProperty(FORWARD_EMAIL);
        }
        catch (ObjectConfigurationException e)
        {
            throw new DataAccessException(addHandlerInfo("Error retrieving Forward Email flag."), e);
        }
    }

    /**
     * Creates a message to be forwarded to the configured address that explains an error occurred sending the given
     * message and displays the errors.
     *
     * @param message to be forwarded.
     * @param errorHandler uses this to get errors from and reports them.
     * @return the email to be forwarded.
     * @throws VelocityException if there's a problem getting the email template.
     * @throws MessagingException if java mail decides so.
     */
    private Email createErrorForwardEmail(Message message, MessageErrorHandler errorHandler)
            throws VelocityException, MessagingException
    {
        Email email = new Email(forwardEmailParam());

        String error = errorHandler.getError();
        String exceptionString = errorHandler.getException();

        email.setSubject(getI18nHelper().getText("template.errorinhandler.subject", message.getSubject()));
        Map<String, Object> contextParams = new HashMap<String, Object>();
        contextParams.putAll(getVelocityParams(error));

        String body = ComponentAccessor.getVelocityManager().getEncodedBody(EMAIL_TEMPLATES, "text/" + ERROR_TEMPLATE, baseUrl, applicationProperties.getString(APKeys.JIRA_WEBWORK_ENCODING), contextParams);

        // Set the error as the body of the mail
        email.setBody(body);
        Multipart mp = new MimeMultipart();

        if (exceptionString != null)
        {
            MimeBodyPart exception = new MimeBodyPart();
            exception.setContent(exceptionString, "text/plain");
            exception.setFileName("ErrorStackTrace.txt");
            mp.addBodyPart(exception);
        }

        // Attach the cloned message
        MimeBodyPart messageAttachment = new MimeBodyPart(); //TODO add message as attachment that can be replied to and edited.
        messageAttachment.setContent(message, "message/rfc822");
        String subject = message.getSubject();
        if (StringUtils.isBlank(subject))
        {
            subject = "NoSubject";
        }
        messageAttachment.setFileName(subject + ".eml");
        mp.addBodyPart(messageAttachment);

        email.setMultipart(mp);

        return email;
    }

    /**
     * Forwards the email to the configured
     *
     * @param message to forward.
     * @param errorHandler for handling errors.
     * @return true if forwarding the email worked.
     * @throws MailException if there's a problem sending the mail or getting the server.
     */
    private boolean forwardEmail(Message message, MessageErrorHandler errorHandler) throws MailException
    {
        if (TextUtils.verifyEmail(forwardEmailParam()))
        {
            try
            {
                Email email = createErrorForwardEmail(message, errorHandler);
                sendMail(email);
                return true;
            }
            catch (VelocityException e)
            {
                log.error(addHandlerInfo("Could not create email template for."), e);
            }
            catch (MessagingException e)
            {
                log.error(addHandlerInfo("Could not retrieve information from message."), e);
            }
        }
        else
        {
            log.warn(addHandlerInfo("Forward Email is invalid."));
        }

        return false;
    }

    private void sendMail(Email email) throws MailException
    {
        SMTPMailServer mailserver = MailFactory.getServerManager().getDefaultSMTPMailServer();
        if (mailserver == null)
        {
            log.warn("You do not currently have a smtp mail server set up yet.");
        }
        else if (MailFactory.isSendingDisabled())
        {
            log.warn("Sending mail is currently disabled in Jira.");
        }
        else
        {
            email.setFrom(mailserver.getDefaultFrom());
            mailserver.send(email);
        }
    }

    /**
     * Creates Velocity parameters with baseline defaults as well as the given error parameter. If there's a problem
     * with retrieving the mail server name or base url, these will be absent from the returned Map.
     *
     * @param error The error to include in the parameters.
     * @return The parameters.
     */
    private Map<String, Object> getVelocityParams(String error)
    {
        Map<String, Object> params = new HashMap<String, Object>();

        final String handlerName = getHandler().getClass().toString();
        try
        {
            params.put("i18n", getI18nHelper());
            params.put("handlerName", handlerName);
            Long serverId = new Long(getProperty(KEY_MAIL_SERVER));
            params.put("serverName", MailFactory.getServerManager().getMailServer(serverId).getName());
            params.put("error", error);
            params.put("baseurl", ComponentAccessor.getApplicationProperties().getString(APKeys.JIRA_BASEURL));
        }
        catch (ObjectConfigurationException e)
        {
            log.error("Could not retrieve mail server", e);
        }
        catch (MailException e)
        {
            log.error("Could not retrieve mail server", e);
        }

        return params;
    }

    /**
     * JRA-13590 Small decorator to add the service handler name and the mail service ID to log messages to make it
     * easier if you have multiple services configured to determine which one is throwing exceptions.
     *
     * @param msg log message
     * @return log message decorated with handler name and mail server ID
     */
    private String addHandlerInfo(String msg)
    {
        return getName() + "[" + mailserverId + "]: " + msg;
    }

    private static I18nHelper getI18nHelper()
    {
        // As this is run from a service, we do not have a user. So use the default system locale, i.e. specify null
        // for the user.
        return ComponentAccessor.getI18nHelperFactory().getInstance((User) null);
    }
}