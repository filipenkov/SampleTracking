package com.atlassian.jira.tzdetect;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.timezone.TimeZoneInfo;
import com.atlassian.jira.timezone.TimeZoneService;
import com.atlassian.jira.user.UserPropertyManager;
import com.opensymphony.module.propertyset.PropertySet;

public class BannerPreferences
{
    private static final String NO_THANKS_PROPERTY_KEY = BannerPreferences.class.getName();

    private final TimeZoneService timeZoneService;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final UserPropertyManager userPropertyManager;

    public BannerPreferences(TimeZoneService timeZoneService, JiraAuthenticationContext jiraAuthenticationContext, UserPropertyManager userPropertyManager)
    {
        this.timeZoneService = timeZoneService;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.userPropertyManager = userPropertyManager;
    }

    public boolean isDisplayBanner()
    {
        return userIsKnown();
    }

    public String getNoThanksTimeZone()
    {
        User user = jiraAuthenticationContext.getLoggedInUser();
        if (user == null)
        {
            return null;
        }

        PropertySet userProperties = userPropertyManager.getPropertySet(jiraAuthenticationContext.getLoggedInUser());
        return userProperties.getString(NO_THANKS_PROPERTY_KEY);
    }

    public void setNoThanksTimeZone(String id)
    {
        User user = jiraAuthenticationContext.getLoggedInUser();
        if (user != null)
        {
            PropertySet userProperties = userPropertyManager.getPropertySet(user);
            userProperties.setString(NO_THANKS_PROPERTY_KEY, id);
        }
    }

    public void setUserTimeZonePreference(String id)
    {
        User user = jiraAuthenticationContext.getLoggedInUser();
        if (user != null)
        {
            JiraServiceContext context = new JiraServiceContextImpl(user);
            timeZoneService.setUserDefaultTimeZone(id, context);
        }
    }

    public TimeZoneInfo getUserTimeZonePreference()
    {
        return timeZoneService.getUserTimeZoneInfo(new JiraServiceContextImpl(jiraAuthenticationContext.getLoggedInUser()));
    }

    private boolean userIsKnown()
    {
        return jiraAuthenticationContext.getLoggedInUser() != null;
    }
}
