package com.atlassian.jira.user.preferences;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.seraph.config.SecurityConfig;
import com.atlassian.seraph.interceptor.LoginInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * A simple interceptor to ensure any cached user preferences are cleared on login (more as a precaution).
 */
public class UserPreferencesResetInterceptor implements LoginInterceptor
{
    public void beforeLogin(HttpServletRequest request, HttpServletResponse response, String username, String password, boolean cookieLogin)
    {
    }

    public void afterLogin(HttpServletRequest request, HttpServletResponse response, String username, String password, boolean cookieLogin, String loginStatus)
    {
        ComponentAccessor.getUserPreferencesManager().clearCache(username);
    }

    public void destroy()
    {
    }

    public void init(Map params, SecurityConfig config)
    {
    }
}
