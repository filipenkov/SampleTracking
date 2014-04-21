package com.atlassian.jira.config;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.plugin.profile.UserFormatManager;
import com.atlassian.jira.plugin.userformat.FullNameUserFormat;
import com.atlassian.jira.propertyset.JiraPropertySetFactory;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.util.OutlookDate;
import com.atlassian.jira.web.util.OutlookDateManager;
import com.atlassian.util.concurrent.ResettableLazyReference;
import com.opensymphony.module.propertyset.PropertySet;

import java.util.Date;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Default implementation of the Reindex Message Manager that uses a simple PropertySet to store the last message
 * pushed.
 *
 * @since v4.0
 */
public class DefaultReindexMessageManager implements ReindexMessageManager, Startable
{
    static final String PS_KEY = "admin.message.manager";

    static final String PS_KEY_USER = "user";
    static final String PS_KEY_TASK = "task";
    static final String PS_KEY_TIME = "time";

    private final UserFormatManager userFormatManager;
    private final I18nHelper.BeanFactory i18nFactory;
    private final OutlookDateManager outlookDateManager;
    private final VelocityRequestContextFactory velocityRequestContextFactory;
    private final EventPublisher eventPublisher;
    private final ResettableLazyReference<PropertySet> propertiesReference;

    public DefaultReindexMessageManager(final JiraPropertySetFactory jiraPropertySetFactory, final UserFormatManager userFormatManager,
            final I18nHelper.BeanFactory i18nFactory, final OutlookDateManager outlookDateManager, final VelocityRequestContextFactory velocityRequestContextFactory,
            final EventPublisher eventPublisher)
    {
        this.eventPublisher = eventPublisher;
        this.velocityRequestContextFactory = notNull("velocityRequestContextFactory", velocityRequestContextFactory);
        this.userFormatManager = notNull("userFormatManager", userFormatManager);
        this.i18nFactory = notNull("i18nFactory", i18nFactory);
        this.outlookDateManager = notNull("outlookDateManager", outlookDateManager);
        this.propertiesReference = new ResettableLazyReference<PropertySet>()
        {
            protected PropertySet create() throws Exception
            {
                return jiraPropertySetFactory.buildCachingDefaultPropertySet(PS_KEY, true);
            }
        };

    }

    public void start() throws Exception
    {
        eventPublisher.register(this);
    }

    @EventListener
    public synchronized void onClearCache(final ClearCacheEvent event)
    {
        propertiesReference.reset();
    }

    public synchronized void pushMessage(final User user, final String i18nTask)
    {
        propertiesReference.get().setString(PS_KEY_USER, user == null ? "" : user.getName());
        propertiesReference.get().setString(PS_KEY_TASK, i18nTask);
        propertiesReference.get().setDate(PS_KEY_TIME, getCurrentDate());
    }

    public synchronized void clear()
    {
        if (propertiesReference.get().exists(PS_KEY_USER))
        {
            propertiesReference.get().remove(PS_KEY_TIME);
            propertiesReference.get().remove(PS_KEY_TASK);
            propertiesReference.get().remove(PS_KEY_USER);
        }
    }

    public synchronized String getMessage(final User user)
    {
        if (propertiesReference.get().exists(PS_KEY_USER))
        {
            final String userName = propertiesReference.get().getString(PS_KEY_USER);
            final String i18nTaskKey = propertiesReference.get().getString(PS_KEY_TASK);
            final Date time = propertiesReference.get().getDate(PS_KEY_TIME);

            final I18nHelper i18n = i18nFactory.getInstance(user);
            final OutlookDate outlookDate = outlookDateManager.getOutlookDate(i18n.getLocale());

            String userFullName = userFormatManager.formatUser(userName, FullNameUserFormat.TYPE, "fullName");
            String i18nTask = i18n.getText(i18nTaskKey);
            String timeString = outlookDate.formatDMYHMS(time);

            StringBuilder message = new StringBuilder();
            if (userFullName == null)
            {
                message.append("<p>").append(i18n.getText("admin.notifications.task.requires.reindex.nouser", i18nTask, timeString));
            }
            else
            {
                message.append("<p>").append(i18n.getText("admin.notifications.task.requires.reindex", userFullName, i18nTask, timeString));
            }
            message.append("<p>").append(i18n.getText("admin.notifications.reindex.now", "<a href='" + getContextPath() + "/secure/admin/jira/IndexAdmin.jspa'>", "</a>"));
            message.append("<p>").append(i18n.getText("admin.notifications.complete.all.changes"));
            return message.toString();
        }
        return null;
    }

    ///CLOVER:OFF
    String getContextPath()
    {
        return velocityRequestContextFactory.getJiraVelocityRequestContext().getBaseUrl();
    }

    Date getCurrentDate()
    {
        return new Date(System.currentTimeMillis());
    }
    ///CLOVER:ON
}
