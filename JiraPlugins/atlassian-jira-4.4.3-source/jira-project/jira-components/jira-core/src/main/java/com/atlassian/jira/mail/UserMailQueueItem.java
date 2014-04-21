/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.mail;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.event.user.UserEvent;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.mail.MailException;
import com.atlassian.mail.queue.AbstractMailQueueItem;
import com.atlassian.mail.queue.SingleMailQueueItem;
import com.atlassian.velocity.VelocityManager;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

import static com.atlassian.jira.config.properties.APKeys.JIRA_WEBWORK_ENCODING;

public class UserMailQueueItem extends AbstractMailQueueItem
{
    private static final Logger log = Logger.getLogger(UserMailQueueItem.class);

    private static final String EMAIL_TEMPLATES = "templates/email/";
    private static final Integer PADSIZE = 20;

    private final UserEvent event;
    private final String subjectKey;
    private final String template;

    public UserMailQueueItem(UserEvent event, String subject, String subjectKey, String template)
    {
        super(subject);
        this.event = event;
        this.subjectKey = subjectKey;
        this.template = template;
    }

    public void send() throws MailException
    {
        incrementSendCount();

        try
        {
            User user = event.getUser();
            if (user != null)
            {
                String to = user.getEmailAddress();

                Map<String, Object> params = getUserContextParamsBody(event);
                params.put("initiatingUser", event.getInitiatingUser());

                // Pass the i18nHelper to the template - allows the email notification to be displayed in the language of the recipient
                // Specify the translation file as an additional resource
                I18nHelper i18nBean = new I18nBean(user);
                params.put("i18n", i18nBean);
                params.put("stringUtils", new StringUtils());
                // Text email formatting - used to leftPad strings in text emails.
                params.put("padSize", PADSIZE);

                final VelocityManager velocityManager = ManagerFactory.getVelocityManager();
                final ApplicationProperties applicationProperties = ManagerFactory.getApplicationProperties();
                String body = velocityManager.getEncodedBody(EMAIL_TEMPLATES, "text/" + template, (String) event.getParams().get("baseurl"), applicationProperties.getString(JIRA_WEBWORK_ENCODING), params);
                Email email = new Email(to);
                email.setSubject(i18nBean.getText(getSubjectKey()));
                email.setBody(body);
                email.setMimeType("text/plain");
                ManagerFactory.getMailQueue().addItem(new SingleMailQueueItem(email));
            }
            else
            {
                log.warn("Mail with subject '" + getSubject() + "' not sent since user '" + user + "' no longer exists.");
            }
        }
        catch (Exception ex)
        {
            throw new MailException(ex);
        }
    }

    public String getSubjectKey()
    {
        return subjectKey;
    }

    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }

    protected Map<String, Object> getUserContextParamsBody(UserEvent uEvent)
    {
        Map<String, Object> contextParams = new HashMap<String, Object>();

        contextParams.put("user", uEvent.getUser());
        contextParams.put("params", uEvent.getParams());

        return JiraMailQueueUtils.getContextParamsBody(contextParams);
    }
}
