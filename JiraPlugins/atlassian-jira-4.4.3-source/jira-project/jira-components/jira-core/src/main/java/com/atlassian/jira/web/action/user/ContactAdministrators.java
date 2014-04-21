package com.atlassian.jira.web.action.user;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.fields.renderer.wiki.AtlassianWikiRenderer;
import com.atlassian.jira.mail.Email;
import com.atlassian.jira.mail.JiraMailUtils;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.user.preferences.JiraUserPreferences;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.velocity.DefaultVelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.mail.MailException;
import com.atlassian.mail.queue.MailQueue;
import com.atlassian.mail.queue.SingleMailQueueItem;
import com.atlassian.plugin.webresource.UrlMode;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ContactAdministrators extends JiraWebActionSupport
{
    private static final String EMAIL_TEMPLATES = "templates/email";
    private static final String EMAIL_TEMPLATE = "contactadministrator.vm";
    private static final String ENCODING_UTF8 = "UTF-8";
    private static final Integer PADSIZE = 20;

    private final VelocityRequestContextFactory velocityRequestContextFactory;
    private final WebResourceManager webResourceManager;
    private final RendererManager rendererManager;
    private final MailQueue mailQueue;
    private final UserUtil userUtil;

    private String to;
    private String from;
    private String subject;
    private String details;

    public ContactAdministrators(RendererManager rendererManager, MailQueue mailQueue, UserUtil userUtil, WebResourceManager webResourceManager, VelocityRequestContextFactory velocityRequestContextFactory)
    {
        this.rendererManager = rendererManager;
        this.mailQueue = mailQueue;
        this.userUtil = userUtil;
        this.webResourceManager = webResourceManager;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        to = getText("admin.global.permissions.administer");
        if (getLoggedInUser() != null)
        {
            from = getLoggedInUser().getEmailAddress();
        }
    }


    public String doDefault() throws Exception
    {
        return super.doDefault();
    }

    protected void doValidation()
    {
        if (StringUtils.isEmpty(from) || !TextUtils.verifyEmail(from))
        {
            addError("from", getText("admin.errors.must.specify.valid.from.address"));
        }
        if (StringUtils.isEmpty(subject))
        {
            addError("subject", getText("admin.errors.must.specify.subject"));
        }
        if (StringUtils.isEmpty(details))
        {
            addError("details", getText("admin.errors.must.specify.request.details"));
        }
    }


    protected String doExecute() throws Exception
    {
        send();
        return getRedirect("/secure/Dashboard.jspa");
    }

    public String getTo()
    {
        return to;
    }

    public void setTo(String to)
    {
        this.to = to;
    }

    public String getFrom()
    {
        return from;
    }

    public void setFrom(String from)
    {
        this.from = from;
    }

    public String getSubject()
    {
        return subject;
    }

    public void setSubject(String subject)
    {
        this.subject = subject;
    }

    public String getDetails()
    {
        return details;
    }

    public void setDetails(String details)
    {
        this.details = details;
    }

    public boolean getSendEmail()
    {
        if (!JiraMailUtils.isHasMailServer())
        {
            return false;
        }
        return getApplicationProperties().getOption(APKeys.JIRA_SHOW_CONTACT_ADMINISTRATORS_FORM);
    }

    public String getRenderedMessage()
    {
        String message = getApplicationProperties().getDefaultBackedText(APKeys.JIRA_CONTACT_ADMINISTRATORS_MESSSAGE);
        if (StringUtils.isEmpty(message)  && !getSendEmail())
        {
            message = getText("admin.generalconfiguration.contact.administrators.message.default");
        }
        return rendererManager.getRendererForType(AtlassianWikiRenderer.RENDERER_TYPE).render(message, null);
    }

    public void send() throws MailException
    {
        Collection<User> administrators = userUtil.getJiraAdministrators();
        for (User administrator : administrators)
        {
            sendTo(administrator);
        }
    }

    private void sendTo(User administrator) throws MailException
    {
        if (!JiraMailUtils.isHasMailServer())
        {
            return;
        }

        try
        {
            Map<String, Object> velocityParams = new HashMap<String, Object>();
            velocityParams.put("content", details);
            velocityParams.put("i18n", new I18nBean(administrator));
            velocityParams.put("webResourceManager", webResourceManager);
            velocityParams.put("baseurl", velocityRequestContextFactory.getJiraVelocityRequestContext().getBaseUrl());
            velocityParams.put("urlModeAbsolute", UrlMode.ABSOLUTE);
            velocityParams.put("padSize", PADSIZE);
            VelocityRequestContext velocityRequestContext = new DefaultVelocityRequestContextFactory(ComponentAccessor.getApplicationProperties()).getJiraVelocityRequestContext();
            String body = ComponentAccessor.getVelocityManager().getEncodedBody(getTemplateDirectory(administrator) , EMAIL_TEMPLATE, ENCODING_UTF8, velocityParams);
            Email email = new Email(administrator.getEmailAddress());
            email.setFrom(from);
            email.setSubject(subject);
            email.setMimeType(getMimeType(administrator));
            email.setEncoding(ENCODING_UTF8);
            email.setBody(body);
            mailQueue.addItem(new SingleMailQueueItem(email));
        }
        catch (Exception e)
        {
            log.error("Error sending JIRA Administrator email", e);
        }
    }

    private String getTemplateDirectory(User to)
    {
        return EMAIL_TEMPLATES + "/" + getFormat(to) + "/";
    }

    private String getMimeType(User to)
    {
        if (getFormat(to).equals(NotificationRecipient.MIMETYPE_HTML))
        {
            return "text/html";
        }
        return "text/plain";
    }

    public String getFormat(User user)
    {
        JiraUserPreferences userPrefs = new JiraUserPreferences(user);
        String prefFormat = userPrefs.getString(PreferenceKeys.USER_NOTIFICATIONS_MIMETYPE);

        // Default to text if the property is not configured.
        if (TextUtils.stringSet(prefFormat) && (prefFormat.equals(NotificationRecipient.MIMETYPE_HTML) || prefFormat.equals(NotificationRecipient.MIMETYPE_TEXT)))
        {
            return prefFormat;
        }
        else
        {
            return NotificationRecipient.MIMETYPE_TEXT;
        }
    }

}
