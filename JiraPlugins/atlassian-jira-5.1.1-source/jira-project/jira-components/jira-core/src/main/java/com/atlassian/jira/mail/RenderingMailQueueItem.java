package com.atlassian.jira.mail;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.mail.util.MimeTypes;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.mail.Email;
import com.atlassian.mail.MailException;
import com.atlassian.mail.queue.SingleMailQueueItem;
import com.google.common.annotations.VisibleForTesting;
import org.apache.log4j.Logger;
import org.apache.velocity.exception.VelocityException;

import java.util.Map;

import static com.atlassian.jira.template.TemplateSources.file;

/**
 * Renders e-mails on send.
 * <p/>
 * Required so that we can populate the Velocity render context from the mail-queue flushing thread rather than the
 * thread that triggered the sending of the mail. In turn, that is required because the thread will get things like
 * baseUrl incorrect (it renders as a relative rather than absolute link).
 *
 * @since v5.0
 */
public class RenderingMailQueueItem extends SingleMailQueueItem
{
    private static final Logger log = Logger.getLogger(RenderingMailQueueItem.class);

    private final String subjectTemplatePath;
    private final String bodyTemplatePath;
    private final Map<String, Object> params;

    public RenderingMailQueueItem(Email email, String subjectTemplatePath, String bodyTemplatePath, Map<String, Object> params)
    {
        super(email);
        this.subjectTemplatePath = subjectTemplatePath;
        this.bodyTemplatePath = bodyTemplatePath;
        this.params = params;
    }

    /**
     * This is the subject as displayed in the Mail Queue Admin page. The subject is displayed in the preference
     * language of the current user viewing items to be sent (i.e. different from items CURRENTLY being sent).
     * <p/>
     * The subject will be displayed in the preference language of the mail recipient once the mail is actually being
     * sent. When the mail is being sent, it is a SingleMailQueueItem.
     *
     * @return String the subject as displayed on the mail queue admin page
     */
    @Override
    public String getSubject()
    {
        final I18nHelper i18n = ComponentAccessor.getJiraAuthenticationContext().getI18nHelper();
        final Map<String, Object> contextParams = JiraMailQueueUtils.getContextParamsBody(params);
        contextParams.put("i18n", i18n);
        try
        {
            return renderEmailSubject(contextParams);
        }
        catch (VelocityException e)
        {
            log.error("Could not determine e-mail subject", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void send() throws MailException
    {
        final Map<String, Object> contextParams = JiraMailQueueUtils.getContextParamsBody(params);

        try
        {
            final String subject = renderEmailSubject(contextParams);

            final String body = renderEmailBody(getEmail(), contextParams);

            getEmail().setSubject(subject);
            getEmail().setBody(body);
        }
        catch (VelocityException e)
        {
            throw new RuntimeException(e);
        }

        super.send();
    }

    private String renderEmailSubject(Map<String, Object> contextParams)
    {
        return getTemplatingEngine().render(file(subjectTemplatePath)).applying(contextParams).asPlainText();
    }

    private String renderEmailBody(final Email message, final Map<String, Object> contextParams)
    {
        if (message.getMimeType().equals(MimeTypes.Text.HTML))
        {
            return getTemplatingEngine().render(file(bodyTemplatePath)).applying(contextParams).asHtml();
        }
        return getTemplatingEngine().render(file(bodyTemplatePath)).applying(contextParams).asPlainText();
    }

    @VisibleForTesting
    VelocityTemplatingEngine getTemplatingEngine()
    {
        return ComponentAccessor.getComponent(VelocityTemplatingEngine.class);
    }
}
