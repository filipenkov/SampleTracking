package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.mail.Email;
import com.atlassian.jira.mail.JiraMailUtils;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.velocity.DefaultVelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.web.util.HelpUtil;
import com.atlassian.mail.MailException;
import com.atlassian.mail.queue.AbstractMailQueueItem;
import com.atlassian.mail.queue.SingleMailQueueItem;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Mail item used for sending out e-mails about lossy cron conversions and conversion errors.
 */
public class UpgradeTask_Build232MailItem extends AbstractMailQueueItem
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build232MailItem.class);

    private static final String ENCODING_UTF8 = "UTF-8";
    private static final String EMAIL_TEMPLATES = "templates/email/text/";
    private static final String MIME_TYPE = "text/plain";

    private final String fullName;
    private final String emailAddress;
    private final Locale locale;
    private final String subject;
    private final String template;
    private final Map velocityParams;
    private final Set lossySubscriptions;
    private final I18nHelper.BeanFactory i18n;

    public UpgradeTask_Build232MailItem(String emailAddress, String fullName, Locale locale, String subject, String template, Set lossySubscriptions, I18nHelper.BeanFactory i18n)
    {
        this.lossySubscriptions = lossySubscriptions;
        this.emailAddress = emailAddress;
        this.fullName = fullName;
        this.locale = locale;
        this.subject = subject;
        this.template = template;
        this.i18n = i18n;
        this.velocityParams = new HashMap();
    }

    public void send() throws MailException
    {
        if (!JiraMailUtils.isHasMailServer())
        {
            return;
        }

        try
        {
            I18nHelper i18nBean = i18n.getInstance(locale);
            velocityParams.put("subscriberFullName", fullName);
            velocityParams.put("i18n", i18nBean);
            velocityParams.put("helpUtil", HelpUtil.getInstance());
            velocityParams.put("subscriptions", lossySubscriptions);
            VelocityRequestContext velocityRequestContext = new DefaultVelocityRequestContextFactory(ComponentAccessor.getApplicationProperties()).getJiraVelocityRequestContext();
            velocityParams.put("baseurl", velocityRequestContext.getBaseUrl());
            String body = ComponentAccessor.getVelocityManager().getEncodedBody(EMAIL_TEMPLATES, template, ENCODING_UTF8, velocityParams);
            Email email = new Email(emailAddress);
            email.setSubject(i18nBean.getText(subject));
            email.setBody(body);
            email.setMimeType(MIME_TYPE);
            email.setEncoding(ENCODING_UTF8);
            ManagerFactory.getMailQueue().addItem(new SingleMailQueueItem(email));
        } catch (Exception e)
        {
            log.error("Error sending upgrade task 232 mail item", e);
        }
    }

}
