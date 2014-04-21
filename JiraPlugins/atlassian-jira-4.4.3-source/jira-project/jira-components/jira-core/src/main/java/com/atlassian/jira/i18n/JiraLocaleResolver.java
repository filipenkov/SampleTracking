package com.atlassian.jira.i18n;

import com.atlassian.jira.config.LocaleManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.sal.api.message.LocaleResolver;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;
import java.util.Set;

/**
 * Resolves the locale for a particular request.  Depends on the user that's currently logged in, otherwise the system
 * default locale will be used.
 *
 * @since v2.1
 */
public class JiraLocaleResolver implements LocaleResolver
{
    private final LocaleManager localeManager;
    private final JiraAuthenticationContext jiraAuthenticationContext;

    public JiraLocaleResolver(final LocaleManager localeManager, final JiraAuthenticationContext jiraAuthenticationContext)
    {
        this.localeManager = localeManager;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    @Override
    public Locale getLocale(final HttpServletRequest request)
    {
        return jiraAuthenticationContext.getLocale();
    }

    @Override
    public Locale getLocale()
    {
        return jiraAuthenticationContext.getLocale();
    }

    @Override
    public Set<Locale> getSupportedLocales()
    {
        return localeManager.getInstalledLocales();
    }
}
