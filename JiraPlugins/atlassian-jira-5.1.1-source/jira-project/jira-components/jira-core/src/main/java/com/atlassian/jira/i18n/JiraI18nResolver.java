package com.atlassian.jira.i18n;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A JIRA I18nResolver.
 * Uses the user's locale if a user is logged in or the default locale if none can be found.
 * <p>
 * By rights we should implement this in the jira-sal-plugin, but this is the one thing that we need from SAL during Bootstrap.
 * The simple solution is to define JiraI18nResolver in jira-core and include it in BootstrapContainer, but during
 * normal runtime we don't put it in PICO - we define it in jira-sal-plugin as expected.
 * <p>
 * IMPORTANT! JRA-25571 Do not add dependency on sal-core to jira-core: this causes Bugs due to ClassLoader issues.
 */
public class JiraI18nResolver extends AbstractI18nResolver
{
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final I18nHelper.BeanFactory beanFactory;

    public JiraI18nResolver(final JiraAuthenticationContext jiraAuthenticationContext, final I18nHelper.BeanFactory beanFactory)
    {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.beanFactory = beanFactory;
    }

    public String resolveText(String key, Serializable[] arguments)
    {
        final I18nHelper bean = beanFactory.getInstance(jiraAuthenticationContext.getLocale());
        return bean.getText(key, arguments);
    }

    @Override
    public String getRawText(String key)
    {
        final I18nHelper bean = beanFactory.getInstance(jiraAuthenticationContext.getLocale());
        return bean.getUnescapedText(key);
    }

    public Map<String, String> getAllTranslationsForPrefix(final String prefix)
    {
        return getAllTranslationsForPrefix(prefix, jiraAuthenticationContext.getLocale());
    }

    public Map<String, String> getAllTranslationsForPrefix(final String prefix, final Locale locale)
    {
        notNull("prefix", prefix);
        notNull("locale", locale);

        final I18nHelper i18nBean = beanFactory.getInstance(locale);
        final Set<String> keys = i18nBean.getKeysForPrefix(prefix);

        final Map<String, String> ret = new HashMap<String, String>();
        for (String key : keys)
        {
            ret.put(key, i18nBean.getUnescapedText(key));
        }

        return Collections.unmodifiableMap(ret);
    }
}
