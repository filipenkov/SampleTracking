package com.atlassian.jira.mock.security;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.JiraAuthenticationContextImpl;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.jira.web.util.OutlookDateManager;
import com.atlassian.jira.web.util.OutlookDateManagerImpl;
import com.atlassian.seraph.auth.AuthenticationContext;

import java.security.Principal;

public class MockAuthenticationContext extends JiraAuthenticationContextImpl
{
    private final I18nHelper i18nHelper;

    public MockAuthenticationContext(final User user)
    {
        this(user, new OutlookDateManagerImpl(ComponentAccessor.getApplicationProperties(), null, null));
    }

    public MockAuthenticationContext(final User user, final OutlookDateManager mgr)
    {
        this(user, mgr, null);
    }

    public MockAuthenticationContext(final User user, final OutlookDateManager mgr, final I18nHelper i18nHelper)
    {
        super(getMockContext(user), null);
        if (i18nHelper == null)
            this.i18nHelper = new MockI18nBean();
        else
            this.i18nHelper = i18nHelper;        
    }

    public I18nHelper getI18nHelper()
    {
        if (i18nHelper != null)
        {
            return i18nHelper;
        }
        else
        {
            return super.getI18nHelper();
        }
    }

    @Override
    public I18nHelper getI18nBean()
    {
        return getI18nHelper();
    }

    private static AuthenticationContext getMockContext(final User user)
    {
        return new AuthenticationContext()
        {
            public Principal getUser()
            {
                return user;
            }

            public void setUser(final Principal user)
            {
                throw new UnsupportedOperationException("Set user not supported");
            }

            public void clearUser()
            {
                throw new UnsupportedOperationException("Clear user not supported");
            }
        };
    }
}
