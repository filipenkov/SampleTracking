package com.atlassian.jira.user.profile;

import com.atlassian.core.user.preferences.Preferences;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.config.LocaleManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.timezone.TimeZoneService;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.jira.user.preferences.UserPreferencesManager;
import com.atlassian.jira.user.util.OSUserConverter;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.jira.web.util.JiraLocaleUtils;
import com.atlassian.velocity.VelocityManager;
import com.opensymphony.util.TextUtils;

import java.util.Map;

/**
 * User Profile Fragment that displays the users preferences
 *
 * @since v4.1
 */
public class PreferencesUserProfileFragment extends AbstractUserProfileFragment
{
    private final UserPreferencesManager preferencesManager;
    private final LocaleManager localeManager;
    private final I18nBean.BeanFactory i18nFactory;
    private final TimeZoneService timeZoneManager;

    public PreferencesUserProfileFragment(ApplicationProperties applicationProperties, JiraAuthenticationContext jiraAuthenticationContext,
            VelocityManager velocityManager, UserPreferencesManager preferencesManager,
            LocaleManager localeManager, I18nBean.BeanFactory i18nCachingFactory, TimeZoneService timeZoneManager)
    {
        super(applicationProperties, jiraAuthenticationContext, velocityManager);
        this.preferencesManager = preferencesManager;
        this.localeManager = localeManager;
        this.i18nFactory = i18nCachingFactory;
        this.timeZoneManager = timeZoneManager;
    }

    @Override
    protected Map<String, Object> createVelocityParams(User profileUser, User currentUser)
    {
        final Preferences preferences = preferencesManager.getPreferences(OSUserConverter.convertToOSUser(profileUser));
        final String userLocale = preferences.getString(PreferenceKeys.USER_LOCALE);

        String localeName;
        if (TextUtils.stringSet(userLocale))
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
            public com.opensymphony.user.User getUser()
            {
                return jiraAuthenticationContext.getUser();
            }

            @Override
            public User getLoggedInUser()
            {
                return jiraAuthenticationContext.getLoggedInUser();
            }

            @Override
            public I18nHelper getI18nBean()
            {
                return jiraAuthenticationContext.getI18nBean();
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
