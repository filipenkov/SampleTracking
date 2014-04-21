package com.atlassian.jira.user.profile;

import com.atlassian.core.user.preferences.Preferences;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.config.LocaleManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.timezone.TimeZoneService;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.jira.user.preferences.UserPreferencesManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.VelocityParamFactory;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.jira.web.util.JiraLocaleUtils;

import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * User Profile Fragment that displays the users preferences
 *
 * @since v4.1
 */
public class PreferencesUserProfileFragment extends AbstractUserProfileFragment
{
    private final ApplicationProperties applicationProperties;
    private final UserPreferencesManager preferencesManager;
    private final LocaleManager localeManager;
    private final I18nBean.BeanFactory i18nFactory;
    private final TimeZoneService timeZoneManager;

    public PreferencesUserProfileFragment(final ApplicationProperties applicationProperties, final JiraAuthenticationContext jiraAuthenticationContext,
            final VelocityTemplatingEngine templatingEngine, final VelocityParamFactory velocityParamFactory, final UserPreferencesManager preferencesManager,
            final LocaleManager localeManager, final I18nBean.BeanFactory i18nCachingFactory, final TimeZoneService timeZoneManager)
    {
        super(jiraAuthenticationContext, templatingEngine, velocityParamFactory);
        this.applicationProperties = applicationProperties;
        this.preferencesManager = preferencesManager;
        this.localeManager = localeManager;
        this.i18nFactory = i18nCachingFactory;
        this.timeZoneManager = timeZoneManager;
    }

    @Override
    protected Map<String, Object> createVelocityParams(User profileUser, User currentUser)
    {
        final Preferences preferences = preferencesManager.getPreferences(profileUser);
        final String userLocale = preferences.getString(PreferenceKeys.USER_LOCALE);

        String localeName;
        if (isNotBlank(userLocale))
        {
            localeName = localeManager.getLocale(userLocale).getDisplayName(jiraAuthenticationContext.getLocale());
        }
        else
        {
            final I18nHelper i18n = i18nFactory.getInstance(profileUser);

            localeName = i18n.getText(JiraLocaleUtils.DEFAULT_LOCALE_I18N_KEY, applicationProperties.getDefaultLocale().getDisplayName(jiraAuthenticationContext.getLocale()));
        }
        final Map<String, Object> params = super.createVelocityParams(profileUser, currentUser);

        params.put("user", profileUser);
        params.put("localeName", localeName);
        final SimpleErrorCollection simpleErrorCollection = new SimpleErrorCollection();
        JiraServiceContext serviceContext = new JiraServiceContext()
        {
            @Override
            public ErrorCollection getErrorCollection()
            {
                return simpleErrorCollection;
            }

            @Override
            public User getLoggedInUser()
            {
                return jiraAuthenticationContext.getLoggedInUser();
            }

            @Override
            public I18nHelper getI18nBean()
            {
                return jiraAuthenticationContext.getI18nHelper();
            }
        };
        params.put("usesJiraTimeZone", timeZoneManager.usesJiraTimeZone(serviceContext));
        params.put("timezone", timeZoneManager.getUserTimeZoneInfo(serviceContext));
        params.put("pageSize", preferences.getLong(PreferenceKeys.USER_ISSUES_PER_PAGE));
        final String mimeType = preferences.getString(PreferenceKeys.USER_NOTIFICATIONS_MIMETYPE);
        if (NotificationRecipient.MIMETYPE_HTML.equals(mimeType))
        {
            params.put("mimeType", NotificationRecipient.MIMETYPE_HTML_DISPLAY);
        }
        else
        {
            params.put("mimeType", NotificationRecipient.MIMETYPE_TEXT_DISPLAY);                        
        }
        params.put("notifyOwnChanges", preferences.getBoolean(PreferenceKeys.USER_NOTIFY_OWN_CHANGES));
        params.put("sharePrivate", preferences.getBoolean(PreferenceKeys.USER_DEFAULT_SHARE_PRIVATE));
        params.put("keyboardShortcutsEnabled", !preferences.getBoolean(PreferenceKeys.USER_KEYBOARD_SHORTCUTS_DISABLED));
        params.put("autowatchEnabled", !preferences.getBoolean(PreferenceKeys.USER_AUTOWATCH_DISABLED));
        params.put("showAutowatch", applicationProperties.getOption(APKeys.JIRA_OPTION_WATCHING));

        return params;
    }

    @Override
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="NM_WRONG_PACKAGE", justification="OSUser is deprecated and dying anyway. Plus the method in question is final so we can't override it.")
    public boolean showFragment(final User profileUser, final User currentUser)
    {
        return profileUser.equals(currentUser);
    }

    public String getId()
    {
        return "preferences-profile-fragment";
    }
}
