package com.atlassian.jira.upgrade.tasks;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.mail.Email;
import com.atlassian.jira.mail.JiraMailUtils;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.user.UserUtils;
import com.atlassian.jira.util.velocity.DefaultVelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.jira.web.util.HelpUtil;
import com.atlassian.mail.MailException;
import com.atlassian.mail.queue.AbstractMailQueueItem;
import com.atlassian.mail.queue.MailQueue;
import com.atlassian.mail.queue.SingleMailQueueItem;
import com.google.common.annotations.VisibleForTesting;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

import static com.atlassian.jira.template.TemplateSources.file;

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

    private final Map<String, Object> velocityParams;
    private final UpgradeTask_Build604.UserSavedFilterConversionInformations userSavedFilterConversionInformations;

    public UpgradeTask_Build604MailItem(final UpgradeTask_Build604.UserSavedFilterConversionInformations userSavedFilterConversionInformations)
    {
        this.userSavedFilterConversionInformations = userSavedFilterConversionInformations;
        this.velocityParams = new HashMap<String, Object>();
    }

    public void send() throws MailException
    {
        if (!JiraMailUtils.isHasMailServer())
        {
            return;
        }

        try
        {
            final User owner = UserUtils.getUser(userSavedFilterConversionInformations.getOwnerName());
            if (owner == null)
            {
                log.error("Error sending upgrade task 428 mail item, owner '" + userSavedFilterConversionInformations.getOwnerName() + "' not found");
                return;
            }
            final I18nBean i18nBean = getI18nBean(owner);
            velocityParams.put("owner", owner);
            velocityParams.put("i18n", i18nBean);
            velocityParams.put("helpUtil", HelpUtil.getInstance());
            velocityParams.put("conversionInformation", userSavedFilterConversionInformations);
            final VelocityRequestContext velocityRequestContext =
                    new DefaultVelocityRequestContextFactory
                            (
                                    ComponentAccessor.getApplicationProperties()
                            ).
                            getJiraVelocityRequestContext();
            velocityParams.put("baseurl", velocityRequestContext.getBaseUrl());

            final String body = getTemplatingEngine().
                    render(file(EMAIL_TEMPLATES + EMAIL_TEMPLATE_PARTIAL)).applying(velocityParams).asPlainText();

            final Email email = new Email(owner.getEmailAddress());
            email.setSubject(i18nBean.getText("template.filters.jql.partial.subject"));
            email.setBody(body);
            email.setMimeType(MIME_TYPE);
            email.setEncoding(ENCODING_UTF8);
            getMailQueue().addItem(new SingleMailQueueItem(email));
        }
        catch (Exception e)
        {
            log.error("Error sending upgrade task 428 mail item", e);
        }
    }

    @VisibleForTesting
    VelocityTemplatingEngine getTemplatingEngine()
    {
        return ComponentAccessor.getComponent(VelocityTemplatingEngine.class);
    }

    @VisibleForTesting
    MailQueue getMailQueue()
    {
        return ComponentAccessor.getMailQueue();
    }

    private I18nBean getI18nBean(final User subscriber)
    {
        return new I18nBean(subscriber);
    }
}
