package com.atlassian.jira.mock.security;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.jira.web.util.OutlookDate;

import java.util.Locale;

/**
 * Simple authentication context that can be used for testing. 
 *
 * @since v3.13
 */
public class MockSimpleAuthenticationContext implements JiraAuthenticationContext
{
    private User user;
    private final Locale locale;
    private final I18nHelper helper;

    public MockSimpleAuthenticationContext(final User user)
    {
        this(user, Locale.getDefault());
    }

    public MockSimpleAuthenticationContext(final User user, final Locale locale)
    {
        this(user, locale, new MockI18nBean());
    }

    public MockSimpleAuthenticationContext(final User user, final Locale locale, final I18nHelper helper)
    {
        this.user = user;
        this.locale = locale;
        this.helper = helper;
    }

    @Override
    public User getLoggedInUser()
    {
        return user;
    }

    @Override
    public boolean isLoggedInUser()
    {
        return getLoggedInUser() != null;
    }

    @Override
    public User getUser()
    {
        return user;
    }

    @Override
    public Locale getLocale()
    {
        return locale;
    }

    @Override
    public OutlookDate getOutlookDate()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getText(final String key)
    {
        return getI18nHelper().getText(key);
    }

    @Override
    public I18nHelper getI18nHelper()
    {
        return helper;
    }

    @Override
    public I18nHelper getI18nBean()
    {
        return getI18nHelper();
    }

    @Override
    public void setLoggedInUser(final User user)
    {
        this.user = user;
    }
}
