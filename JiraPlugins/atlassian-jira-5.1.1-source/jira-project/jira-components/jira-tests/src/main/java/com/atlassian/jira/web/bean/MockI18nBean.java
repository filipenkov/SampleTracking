package com.atlassian.jira.web.bean;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.language.TranslationTransform;
import com.atlassian.jira.plugin.util.InvolvedPluginsTracker;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.plugin.PluginAccessor;

import java.util.Collections;
import java.util.Locale;

/**
 * Mock I18nBean to get around the problem of having to lookup a default locale via
 * applicationProperties via the DB.
 * If you want to assert the key and not use any property bundles you can use MockI18nHelper.
 *
 * @since v3.13
 */
public class MockI18nBean extends I18nBean
{
    public MockI18nBean()
    {
        super(new MockBackingI18n());
    }

    @Override
    protected BeanFactory getFactory()
    {
        return new BeanFactory()
        {
            public I18nHelper getInstance(Locale locale)
            {
                return new BackingI18n(locale, new MockI18nTranslationMode(), new InvolvedPluginsTracker(), Collections.<TranslationTransform>emptyList());
            }

            public I18nHelper getInstance(User user)
            {
                throw new UnsupportedOperationException();
            }
        };
    }

    public static class MockI18nBeanFactory implements I18nHelper.BeanFactory
    {
        public I18nHelper getInstance(final Locale locale)
        {
            return new MockI18nBean();
        }

        public I18nHelper getInstance(final User user)
        {
            return new MockI18nBean();
        }
    }

    private static class MockBackingI18n extends BackingI18n
    {
        public MockBackingI18n()
        {
            super(Locale.ENGLISH, new MockI18nTranslationMode(), new InvolvedPluginsTracker(), Collections.<TranslationTransform>emptyList());
        }


        protected PluginAccessor getPluginAccessor()
        {
            // In Unit Tests we don't want to try find plugins for translations.
            return null;
        }
    }
}
