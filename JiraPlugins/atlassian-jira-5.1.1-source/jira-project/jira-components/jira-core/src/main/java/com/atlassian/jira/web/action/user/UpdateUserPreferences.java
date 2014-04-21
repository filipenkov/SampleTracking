package com.atlassian.jira.web.action.user;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.config.LocaleManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.event.user.UserPreferencesUpdatedEvent;
import com.atlassian.jira.issue.fields.option.TextOption;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.timezone.RegionInfo;
import com.atlassian.jira.timezone.RegionInfoImpl;
import com.atlassian.jira.timezone.TimeZoneInfo;
import com.atlassian.jira.timezone.TimeZoneInfoImpl;
import com.atlassian.jira.timezone.TimeZoneService;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.jira.user.preferences.UserPreferencesManager;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.web.SessionKeys;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.web.session.SessionPagerFilterManager;
import com.atlassian.jira.web.session.SessionSearchObjectManagerFactory;
import com.atlassian.jira.web.util.HelpUtil;
import org.apache.commons.lang.StringUtils;
import webwork.action.Action;
import webwork.action.ActionContext;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class UpdateUserPreferences extends JiraWebActionSupport
{
    public static final int MAX_ISSUES_PER_PAGE_SETTING = 1000;

    private final UserPreferencesManager userPreferencesManager;
    private final LocaleManager localeManager;
    private final SessionSearchObjectManagerFactory sessionSearchObjectManagerFactory;
    private final TimeZoneService timeZoneManager;
    private final EventPublisher eventPublisher;

    private String username;
    private long userIssuesPerPage;
    private String userNotificationsMimeType;
    private String userLocale;
    private boolean notifyOwnChanges;
    private boolean shareDefault;
    private boolean keyboardShortcutsEnabled;
    private boolean autowatchEnabled;
    private String timeZoneId;


    public UpdateUserPreferences(final UserPreferencesManager userPreferencesManager,
                                 final LocaleManager localeManager,
                                 final SessionSearchObjectManagerFactory sessionSearchObjectManagerFactory,
                                 final TimeZoneService timeZoneManager,
                                 final EventPublisher eventPublisher)
    {
        this.userPreferencesManager = userPreferencesManager;
        this.localeManager = localeManager;
        this.sessionSearchObjectManagerFactory = sessionSearchObjectManagerFactory;
        this.timeZoneManager = timeZoneManager;
        this.eventPublisher = eventPublisher;
    }

    public String doDefault()
    {
        final User current = getLoggedInUser();

        if (current == null || !current.getName().equals(username))
        {
            return ERROR;
        }

        // clear any user preference cache so we ensure we retrieve fresh (defensive code)
        userPreferencesManager.clearCache(getLoggedInUser().getName());

        setUserIssuesPerPage(getUserPreferences().getLong(PreferenceKeys.USER_ISSUES_PER_PAGE));
        setUserNotificationsMimeType(getUserPreferences().getString(PreferenceKeys.USER_NOTIFICATIONS_MIMETYPE));
        setNotifyOwnChanges(getUserPreferences().getBoolean(PreferenceKeys.USER_NOTIFY_OWN_CHANGES));
        setShareDefault(getUserPreferences().getBoolean(PreferenceKeys.USER_DEFAULT_SHARE_PRIVATE));
        setUserLocale(getUserPreferences().getString(PreferenceKeys.USER_LOCALE));
        setKeyboardShortcutsEnabled(!getUserPreferences().getBoolean(PreferenceKeys.USER_KEYBOARD_SHORTCUTS_DISABLED));
        setAutowatchEnabled(!getUserPreferences().getBoolean(PreferenceKeys.USER_AUTOWATCH_DISABLED));
        return Action.INPUT;
    }

    public boolean getShowPluginHints()
    {
        return getApplicationProperties().getOption(APKeys.JIRA_SHOW_MARKETING_LINKS);
    }

    public String getTacUrl()
    {
        return HelpUtil.getInstance().getHelpPath("userpreferences.language.more.from.tac").getUrl();
    }

    public long getUserIssuesPerPage()
    {
        return userIssuesPerPage;
    }

    public void setUserIssuesPerPage(long aLong)
    {
        userIssuesPerPage = aLong;
    }

    public String getUserNotificationsMimeType()
    {
        return userNotificationsMimeType;
    }

    public void setUserNotificationsMimeType(String userNotificationsMimeType)
    {
        this.userNotificationsMimeType = userNotificationsMimeType;
    }

    public void setShareDefault(final boolean isPublic)
    {
        shareDefault = isPublic;
    }

    public boolean isShareDefault()
    {
        return shareDefault;
    }

    /**
     * Gets the available list of options for the Sharing Default preference
     *
     * @return the available list of options for the Sharing Default preference
     */
    public Collection<TextOption> getOwnChangesList()
    {
        final String notify = getText("preferences.notification.on.short");
        final String doNotNotify = getText("preferences.notification.off.short");

        return CollectionBuilder.list(new TextOption("true", notify),
                new TextOption("false", doNotNotify));
    }
    /**
     * Gets the available list of options for the Sharing Default preference
     *
     * @return the available list of options for the Sharing Default preference
     */
    public Collection<TextOption> getShareList()
    {
        final String publicText = getText("preferences.default.share.shared.short");
        final String privateText = getText("preferences.default.share.unshared.short");

        return CollectionBuilder.list(new TextOption("false", publicText),
                new TextOption("true", privateText));
    }

    /**
     * The current value of the Sharing default
     *
     * @return The current value of the Sharing default, false (public) or true (private)
     */
    public String getShareValue()
    {
        return String.valueOf(getUserPreferences().getBoolean(PreferenceKeys.USER_DEFAULT_SHARE_PRIVATE));
    }

    /**
     * Gets the available list of options for the Keyboard shortcut preference
     *
     * @return the available list of options for the keyboard shortcut Default preference
     */
    public Collection<TextOption> getKeyboardShortcutList()
    {
        final String enabledText = getText("preferences.keyboard.shortcuts.enabled");
        final String disabledText = getText("preferences.keyboard.shortcuts.disabled");

        return CollectionBuilder.list(new TextOption("true", enabledText),
                new TextOption("false", disabledText));
    }

    /**
     * The current value of the keyboard shortcut default
     *
     * @return The current value of the keyboard shortcut default, true (enabled) or false (disabled)
     */
    public String getKeyboardShortcutValue()
    {
        return String.valueOf(!getUserPreferences().getBoolean(PreferenceKeys.USER_KEYBOARD_SHORTCUTS_DISABLED));
    }

    public Collection<TextOption> getAutowatchList()
    {
        final String enabledText = getText("preferences.autowatch.enabled");
        final String disabledText = getText("preferences.autowatch.disabled");

        return CollectionBuilder.list(new TextOption("true", enabledText),
                new TextOption("false", disabledText));
    }

    /**
     * The current value of the autowatch default
     *
     * @return The current value of the autowatch default, true (disabled) or false (enabled)
     */
    public String getAutowatchValue()
    {
        return String.valueOf(!getUserPreferences().getBoolean(PreferenceKeys.USER_AUTOWATCH_DISABLED));
    }

    public String getUserLocale()
    {
        return userLocale;
    }

    public void setUserLocale(String userLocale)
    {
        this.userLocale = userLocale;
    }

    public void setDefaultUserTimeZone(String timeZoneId)
    {
        this.timeZoneId = timeZoneId;
    }

    public List<RegionInfo> getTimeZoneRegions()
    {
        List<RegionInfo> regions = timeZoneManager.getTimeZoneRegions(getJiraServiceContext());
        // Add the jira region to the beginning
        regions.add(0, new RegionInfoImpl(TimeZoneService.JIRA, getText("timezone.region.jira")));
        return regions;
    }

    public List<TimeZoneInfo> getTimeZoneInfos()
    {
        List<TimeZoneInfo> timeZoneInfos = timeZoneManager.getTimeZoneInfos(getJiraServiceContext());
        TimeZoneInfo jiraDefaultTimeZone = timeZoneManager.getDefaultTimeZoneInfo(getJiraServiceContext());
        TimeZoneInfoImpl timeZoneInfo = new TimeZoneInfoImpl(TimeZoneService.JIRA, jiraDefaultTimeZone.getDisplayName(),  jiraDefaultTimeZone.toTimeZone(), getJiraServiceContext().getI18nBean(), TimeZoneService.JIRA);
        timeZoneInfos.add(timeZoneInfo);
        return timeZoneInfos;
    }

    public String getConfiguredTimeZoneRegion()
    {
        if (timeZoneManager.usesJiraTimeZone(getJiraServiceContext()))
        {
            return TimeZoneService.JIRA;
        }
        return timeZoneManager.getUserTimeZoneInfo(getJiraServiceContext()).getRegionKey();
    }

    public String getConfiguredTimeZoneId()
    {
        if (timeZoneManager.usesJiraTimeZone(getJiraServiceContext()))
        {
            return TimeZoneService.JIRA;
        }
        return timeZoneManager.getUserTimeZoneInfo(getJiraServiceContext()).getTimeZoneId();
    }

    public Map getMimeTypes()
    {
        return EasyMap.build(NotificationRecipient.MIMETYPE_HTML, NotificationRecipient.MIMETYPE_HTML_DISPLAY, NotificationRecipient.MIMETYPE_TEXT,
                NotificationRecipient.MIMETYPE_TEXT_DISPLAY);
    }

    public boolean getNotifyOwnChanges()
    {
        return notifyOwnChanges;
    }

    public void setNotifyOwnChanges(boolean notifyOwnChanges)
    {
        this.notifyOwnChanges = notifyOwnChanges;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(final String username)
    {
        this.username = username;
    }

    /**
     * @return the installed locales with the default option at the top
     */
    public Map<String, String> getInstalledLocales()
    {
        return localeManager.getInstalledLocalesWithDefault(getApplicationProperties().getDefaultLocale(), this);
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        final User current = getLoggedInUser();

        if (current == null || !current.getName().equals(username))
        {
            return ERROR;
        }

        getUserPreferences().setLong(PreferenceKeys.USER_ISSUES_PER_PAGE, getUserIssuesPerPage());
        getUserPreferences().setString(PreferenceKeys.USER_NOTIFICATIONS_MIMETYPE, getUserNotificationsMimeType());
        getUserPreferences().setBoolean(PreferenceKeys.USER_DEFAULT_SHARE_PRIVATE, isShareDefault());
        getUserPreferences().setBoolean(PreferenceKeys.USER_NOTIFY_OWN_CHANGES, getNotifyOwnChanges());
        getUserPreferences().setBoolean(PreferenceKeys.USER_KEYBOARD_SHORTCUTS_DISABLED, !isKeyboardShortcutsEnabled());
        getUserPreferences().setBoolean(PreferenceKeys.USER_AUTOWATCH_DISABLED, !isAutowatchEnabled());
        if (LocaleManager.DEFAULT_LOCALE.equals(getUserLocale()))
        {
            String locale = getUserPreferences().getString(PreferenceKeys.USER_LOCALE);
            if (locale != null)
            {
                getUserPreferences().remove(PreferenceKeys.USER_LOCALE);
            }
        }
        else
        {
            getUserPreferences().setString(PreferenceKeys.USER_LOCALE, getUserLocale());
        }

        if (TimeZoneService.JIRA.equals(timeZoneId))
        {
           timeZoneManager.clearUserDefaultTimeZone(getJiraServiceContext());
        }
        else
        {
           timeZoneManager.setUserDefaultTimeZone(timeZoneId, getJiraServiceContext());
        }
        
        eventPublisher.publish(new UserPreferencesUpdatedEvent(current));

        // remove any current pagers in the session
        Map session = ActionContext.getSession();
        session.remove(SessionKeys.GENERIC_PAGER);
        getSessionPagerFilterManager().setCurrentObject(null);

        // clear any caches, to ensure they are refreshed (defensive code)
        userPreferencesManager.clearCache(getLoggedInUser().getName());

        return returnComplete("ViewProfile.jspa");
    }

    private SessionPagerFilterManager getSessionPagerFilterManager()
    {
        return sessionSearchObjectManagerFactory.createPagerFilterManager();
    }

    protected void doValidation()
    {
        localeManager.validateUserLocale(getLoggedInUser(), getUserLocale(), this);
        
        if(!StringUtils.equals(getUserNotificationsMimeType(), NotificationRecipient.MIMETYPE_TEXT) &&
                !StringUtils.equals(getUserNotificationsMimeType(), NotificationRecipient.MIMETYPE_HTML))
        {
            addError("userNotificationsMimeType", getText("preferences.invalid.mime.type"));
        }
        
        if (getUserIssuesPerPage() <= 0 || getUserIssuesPerPage() > MAX_ISSUES_PER_PAGE_SETTING)
        {
            addError("userIssuesPerPage", getText("preferences.issues.per.page.error"));
        }
        super.doValidation();
    }

    public boolean isKeyboardShortcutsEnabled()
    {
        return keyboardShortcutsEnabled;
    }

    public void setKeyboardShortcutsEnabled(final boolean keyboardShortcutsEnabled)
    {
        this.keyboardShortcutsEnabled = keyboardShortcutsEnabled;
    }

    public boolean isAutowatchEnabled()
    {
        return autowatchEnabled;
    }

    public void setAutowatchEnabled(final boolean autowatchEnabled)
    {
        this.autowatchEnabled = autowatchEnabled;
    }

    public boolean getShowAutowatch()
    {
        return getApplicationProperties().getOption(APKeys.JIRA_OPTION_WATCHING);
    }

}
