package com.atlassian.jira.tzdetect;

import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.plugin.navigation.PluggableTopNavigation;
import com.atlassian.jira.plugin.navigation.TopNavigationModuleDescriptor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.timezone.TimeZoneInfo;
import com.atlassian.jira.timezone.TimeZoneService;
import com.google.common.collect.Maps;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 *
 */
public class HeadsUp implements PluggableTopNavigation
{
    private final TimeZoneService timeZoneService;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final BannerPreferences bannerPreferences;
    private TopNavigationModuleDescriptor descriptor;

    public HeadsUp(TimeZoneService timeZoneService, JiraAuthenticationContext jiraAuthenticationContext, BannerPreferences bannerPreferences)
    {
        this.timeZoneService = timeZoneService;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.bannerPreferences = bannerPreferences;
    }

    @Override
    public void init(TopNavigationModuleDescriptor descriptor)
    {
        this.descriptor = descriptor;
    }

    @Override
    public String getHtml(HttpServletRequest request)
    {
        Map<String, Object> params = Maps.newHashMap();
        TimeZoneInfo timeZoneInfo = timeZoneService.getUserTimeZoneInfo(new JiraServiceContextImpl(jiraAuthenticationContext.getLoggedInUser()));
        params.put("timeZoneInfo", timeZoneInfo);
        params.put("timeZoneData", timeZoneInfo.toTimeZone());
        params.put("noThanksTimeZoneId", bannerPreferences.getNoThanksTimeZone());

        return descriptor.getTopNavigationHtml(request, params);
    }
}
