package com.atlassian.streams.jira;

import java.util.Locale;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.streams.spi.StreamsLocaleProvider;

public class JiraStreamsLocaleProvider implements StreamsLocaleProvider
{
    private final JiraAuthenticationContext authenticationContext;
    private final ApplicationProperties applicationProperties;
    
    public JiraStreamsLocaleProvider(JiraAuthenticationContext authenticationContext,
                                     ApplicationProperties applicationProperties)
    {
        this.authenticationContext = authenticationContext;
        this.applicationProperties = applicationProperties;
    }
    
    public Locale getApplicationLocale()
    {
        return applicationProperties.getDefaultLocale();
    } 
    
    public Locale getUserLocale()
    {
        return authenticationContext.getLocale();
    }

}
