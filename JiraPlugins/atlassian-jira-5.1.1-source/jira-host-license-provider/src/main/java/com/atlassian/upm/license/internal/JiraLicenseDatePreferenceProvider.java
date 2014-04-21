package com.atlassian.upm.license.internal;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.upm.license.internal.impl.DefaultLicenseDatePreferenceProvider;

import static com.google.common.base.Preconditions.checkNotNull;

public class JiraLicenseDatePreferenceProvider extends DefaultLicenseDatePreferenceProvider
{
    private final JiraAuthenticationContext jiraAuthenticationContext;

    public JiraLicenseDatePreferenceProvider(JiraAuthenticationContext jiraAuthenticationContext)
    {
        this.jiraAuthenticationContext = checkNotNull(jiraAuthenticationContext, "jiraAuthenticationContext");
    }

    @Override
    public String getDateTimeFormat()
    {
        return jiraAuthenticationContext.getOutlookDate().getCompleteDateTimeFormat();
    }
}
