package com.atlassian.jira.upgrade.tasks;

import com.atlassian.core.user.UserUtils;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.mail.Email;
import com.atlassian.jira.mail.JiraMailUtils;
import com.atlassian.jira.util.velocity.DefaultVelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.jira.web.util.HelpUtil;
import com.atlassian.mail.MailException;
import com.atlassian.mail.queue.AbstractMailQueueItem;
import com.atlassian.mail.queue.SingleMailQueueItem;
import com.opensymphony.user.User;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Mail item used for sending out e-mails about partial or errored JQL conversions.
 *
 * @since v4.0
 */
public class UpgradeTask_Build604MailItem extends AbstractMailQueueItem
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build604MailItem.class);

    private static final String EMAIL_TEMPLATE_PARTIAL = "partialsavedfilterconversion.vm";

    private static final String ENCODING_UTF8 = "UTF-8";
    private static final String EMAIL_TEMPLATES = "templates/email/text/";
    private static final String MIME_TYPE = "text/plain";

    private final Map velocityParams;
    private final UpgradeTask_Build604.UserSavedFilterConversionInformations userSavedFilterConversionInformations;

    public UpgradeTask_Build604MailItem(final UpgradeTask_Build604.UserSavedFilterConversionInformations userSavedFilterConversionInformations)
    {
        this.userSavedFilterConversionInformations = userSavedFilterConversionInformations;
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
            User owner = UserUtils.getUser(userSavedFilterConversionInformations.getOwnerName());
            I18nBean i18nBean = getI18nBean(owner);
            velocityParams.put("owner", owner);
            velocityParams.put("i18n", i18nBean);
            velocityParams.put("helpUtil", HelpUtil.getInstance());
            velocityParams.put("conversionInformation", userSavedFilterConversionInformations);
            VelocityRequestContext velocityRequestContext = new DefaultVelocityRequestContextFactory(ComponentAccessor.getApplicationProperties()).getJiraVelocityRequestContext();
            velocityParams.put("baseurl", velocityRequestContext.getBaseUrl());
            String body = ComponentAccessor.getVelocityManager().getEncodedBody(EMAIL_TEMPLATES, EMAIL_TEMPLATE_PARTIAL, ENCODING_UTF8, velocityParams);
            Email email = new Email(owner.getEmail());
            email.setSubject(i18nBean.getText("template.filters.jql.partial.subject"));
            email.setBody(body);
            email.setMimeType(MIME_TYPE);
            email.setEncoding(ENCODING_UTF8);
            ManagerFactory.getMailQueue().addItem(new SingleMailQueueItem(email));
        } catch (Exception e)
        {
            log.error("Error sending upgrade task 428 mail item", e);
        }
    }

    private I18nBean getI18nBean(User subscriber)
    {
        return new I18nBean(subscriber);
    }
}
