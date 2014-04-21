package com.atlassian.jira.security;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.security.login.LoginLoggers;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.log.Log4jKit;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.jira.web.util.OutlookDate;
import com.atlassian.jira.web.util.OutlookDateManager;
import com.atlassian.seraph.auth.AuthenticationContext;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class JiraAuthenticationContextImpl implements JiraAuthenticationContext
{
    private static final ThreadLocal<Map<String, Object>> REQUEST_CACHE = new ThreadLocal<Map<String, Object>>();

    public static void clearRequestCache()
    {
        REQUEST_CACHE.set(null);
    }

    public static Map<String, Object> getRequestCache()
    {
        Map<String, Object> cache = REQUEST_CACHE.get();

        if (cache == null)
        {
            cache = new HashMap<String, Object>();
            REQUEST_CACHE.set(cache);
        }

        return cache;
    }

    //
    // members
    //

    private final AuthenticationContext authenticationContext;
    private final I18nHelper.BeanFactory i18n;

    //
    // ctors
    //

    public JiraAuthenticationContextImpl(final AuthenticationContext authenticationContext, I18nHelper.BeanFactory i18n)
    {
        this.authenticationContext = authenticationContext;
        this.i18n = i18n;
    }

    //
    // methods
    //

    @Override
    public User getLoggedInUser()
    {
        return (User) authenticationContext.getUser();
    }

    @Override
    public boolean isLoggedInUser()
    {
        return getLoggedInUser() != null;
    }

    @Override
    public User getUser()
    {
        return getLoggedInUser();
    }

    @Override
    public Locale getLocale()
    {
        return I18nBean.getLocaleFromUser(getLoggedInUser());
    }

    @Override
    public OutlookDate getOutlookDate()
    {
        return ComponentAccessor.getComponentOfType(OutlookDateManager.class).getOutlookDate(getLocale());
    }

    @Override
    public String getText(final String key)
    {
        return getI18nHelper().getText(key);
    }

    @Override
    public I18nHelper getI18nHelper()
    {
        return i18n.getInstance(I18nBean.getLocaleFromUser(getLoggedInUser()));
    }
    
    @Override
    public I18nHelper getI18nBean()
    {
        return getI18nHelper();
    }

    @Override
    public void setLoggedInUser(final User user)
    {
        //
        // make log4j aware of who is making the request
        // if we are calling setUser then we are typically
        // 'impersonating" some one different
        //
        final String userName = user == null ? null : user.getName();
        Log4jKit.putUserToMDC(userName);
        if (LoginLoggers.LOGIN_SETAUTHCTX_LOG.isDebugEnabled())
        {
            LoginLoggers.LOGIN_SETAUTHCTX_LOG.debug("Setting JIRA Auth Context to be  '" + (StringUtils.isBlank(userName) ? "anonymous" : userName) + "'");
        }
        authenticationContext.setUser(user);
    }
}
