package com.atlassian.jira.web.action.user;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.fields.renderer.wiki.AtlassianWikiRenderer;
import com.atlassian.jira.mail.Email;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.mail.MailException;
import com.atlassian.mail.queue.MailQueue;
import com.atlassian.mail.queue.SingleMailQueueItem;
import com.atlassian.mail.server.MailServerManager;
import com.atlassian.plugin.webresource.UrlMode;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.opensymphony.util.TextUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.atlassian.jira.template.TemplateSources.file;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.isNotBlank;

public class ContactAdministrators extends JiraWebActionSupport
{
    private static final String EMAIL_TEMPLATES = "templates/email";
    private static final String EMAIL_TEMPLATE = "contactadministrator.vm";
    private static final String ENCODING_UTF8 = "UTF-8";
    private static final Integer PADSIZE = 20;

    private static class MimeTypes
    {
        static final String TEXT_HTML = "text/html";
        static final String TEXT_PLAIN = "text/plain";
    }

    private final VelocityRequestContextFactory velocityRequestContextFactory;
    private final MailServerManager mailServerManager;
    private final UserPropertyManager userPropertyManager;
    private final VelocityTemplatingEngine velocityManager;
    private final WebResourceManager webResourceManager;
    private final RendererManager rendererManager;
    private final MailQueue mailQueue;
    private final UserUtil userUtil;

    private String to;
    private String replyTo;
    private String subject;
    private String details;

    public ContactAdministrators(final RendererManager rendererManager, final MailQueue mailQueue,
            final UserUtil userUtil, final WebResourceManager webResourceManager,
            final VelocityRequestContextFactory velocityRequestContextFactory,
            final MailServerManager mailServerManager, final UserPropertyManager userPropertyManager,
            final VelocityTemplatingEngine templatingEngine)
    {
        this.rendererManager = rendererManager;
        this.mailQueue = mailQueue;
        this.userUtil = userUtil;
        this.webResourceManager = webResourceManager;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.mailServerManager = mailServerManager;
        this.userPropertyManager = userPropertyManager;
        this.velocityManager = templatingEngine;
        to = getText("admin.global.permissions.administer");
        if (getLoggedInUser() != null)
        {
            replyTo = getLoggedInUser().getEmailAddress();
        }
    }


    public String doDefault() throws Exception
    {
        return super.doDefault();
    }

    protected void doValidation()
    {
        if (isEmpty(replyTo) || !TextUtils.verifyEmail(replyTo))
        {
            addError("from", getText("admin.errors.must.specify.valid.from.address"));
        }
        if (isEmpty(subject))
        {
            addError("subject", getText("admin.errors.must.specify.subject"));
        }
        if (isEmpty(details))
        {
            addError("details", getText("admin.errors.must.specify.request.details"));
        }
    }

    protected String doExecute() throws Exception
    {
        send();
        return getRedirect("/secure/MyJiraHome.jspa");
    }

    public String getTo()
    {
        return to;
    }

    public void setTo(final String to)
    {
        this.to = to;
    }

    public String getFrom()
    {
        return replyTo;
    }

    public void setFrom(final String from)
    {
        this.replyTo = from;
    }

    public String getSubject()
    {
        return subject;
    }

    public void setSubject(final String subject)
    {
        this.subject = subject;
    }

    public String getDetails()
    {
        return details;
    }

    public void setDetails(final String details)
    {
        this.details = details;
    }

    public boolean getShouldDisplayForm()
    {
        return mailServerManager.isDefaultSMTPMailServerDefined() && getApplicationProperties().getOption(APKeys.JIRA_SHOW_CONTACT_ADMINISTRATORS_FORM);
    }

    public String getRenderedMessage()
    {
        String message = getApplicationProperties().getDefaultBackedText(APKeys.JIRA_CONTACT_ADMINISTRATORS_MESSSAGE);
        if (isEmpty(message) || !getShouldDisplayForm())
        {
            message = getText("admin.generalconfiguration.contact.administrators.message.default");
        }
        return rendererManager.getRendererForType(AtlassianWikiRenderer.RENDERER_TYPE).render(message, null);
    }

    public boolean hasCustomMessage()
    {
        String message = getApplicationProperties().getDefaultBackedText(APKeys.JIRA_CONTACT_ADMINISTRATORS_MESSSAGE);
        return !isEmpty(message);
    }

    public void send() throws MailException
    {
        final Collection<User> administrators = userUtil.getJiraAdministrators();
        for (final User administrator : administrators)
        {
            sendTo(administrator);
        }
    }

    private void sendTo(final User administrator) throws MailException
    {
        if (!mailServerManager.isDefaultSMTPMailServerDefined())
        {
            return;
        }

        try
        {
            final Map<String, Object> velocityParams = new HashMap<String, Object>();
            velocityParams.put("content", details);
            velocityParams.put("i18n", new I18nBean(administrator));
            velocityParams.put("webResourceManager", webResourceManager);
            velocityParams.put("baseurl", velocityRequestContextFactory.getJiraVelocityRequestContext().getBaseUrl());
            velocityParams.put("urlModeAbsolute", UrlMode.ABSOLUTE);
            velocityParams.put("padSize", PADSIZE);

            final Email email = new Email(administrator.getEmailAddress());
            email.setReplyTo(replyTo);
            email.setSubject(subject);
            email.setMimeType(getMimeType(administrator));
            email.setEncoding(ENCODING_UTF8);
            email.setBody(renderEmailBody(administrator, velocityParams));
            mailQueue.addItem(new SingleMailQueueItem(email));
        }
        catch (Exception e)
        {
            log.error("Error sending JIRA Administrator email", e);
        }
    }

    private String renderEmailBody(final User administrator, final Map<String, Object> velocityParams)
    {
        if (getMimeType(administrator).equals(MimeTypes.TEXT_HTML))
        {
            return velocityManager.render(file(getTemplateDirectory(administrator) + EMAIL_TEMPLATE)).
                    applying(velocityParams).
                    asHtml();
        }
        else
        {
            return velocityManager.render(file(getTemplateDirectory(administrator) + EMAIL_TEMPLATE)).
                    applying(velocityParams).
                    asPlainText();
        }
    }

    private String getTemplateDirectory(final User to)
    {
        return EMAIL_TEMPLATES + "/" + getFormat(to) + "/";
    }

    private String getMimeType(final User to)
    {
        if (getFormat(to).equals(NotificationRecipient.MIMETYPE_HTML))
        {
            return MimeTypes.TEXT_HTML;
        }
        return MimeTypes.TEXT_PLAIN;
    }

    public String getFormat(final User user)
    {
        final String prefFormat = userPropertyManager.getPropertySet(user).
                getString(PreferenceKeys.USER_NOTIFICATIONS_MIMETYPE);

        // Default to html if the property is not configured.
        if (isNotBlank(prefFormat) && (prefFormat.equals(NotificationRecipient.MIMETYPE_HTML) || prefFormat.equals(NotificationRecipient.MIMETYPE_TEXT)))
        {
            return prefFormat;
        }
        else
        {
            return NotificationRecipient.MIMETYPE_HTML;
        }
    }
}
