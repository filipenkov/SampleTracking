package com.atlassian.jira.config;

import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.util.JiraLocaleUtils;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class DefaultLocaleManager implements LocaleManager
{
    private final JiraLocaleUtils jiraLocaleUtils;

    public DefaultLocaleManager(final JiraLocaleUtils jiraLocaleUtils)
    {
        this.jiraLocaleUtils = jiraLocaleUtils;
    }

    public Set<Locale> getInstalledLocales()
    {
        final Set<Locale> ret = new LinkedHashSet<Locale>(jiraLocaleUtils.getInstalledLocales());
        return Collections.unmodifiableSet(ret);
    }

    public Map<String, String> getInstalledLocalesWithDefault(Locale defaultLocale, I18nHelper i18nHelper)
    {
        final Map<String, String> installedLocalesWithDefault =
                jiraLocaleUtils.getInstalledLocalesWithDefault(defaultLocale, i18nHelper);
        return Collections.unmodifiableMap(installedLocalesWithDefault);
    }

    public Locale getLocale(String locale)
    {
        return jiraLocaleUtils.getLocale(locale);
    }
}
