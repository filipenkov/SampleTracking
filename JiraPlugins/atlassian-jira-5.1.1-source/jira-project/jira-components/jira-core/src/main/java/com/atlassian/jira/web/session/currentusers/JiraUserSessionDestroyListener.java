package com.atlassian.jira.web.session.currentusers;

import com.atlassian.jira.bc.security.login.LoginLoggers;
import com.atlassian.jira.web.filters.accesslog.AtlassianSessionIdUtil;
import com.atlassian.multitenant.MultiTenantContext;
import com.atlassian.multitenant.MultiTenantManager;
import com.atlassian.multitenant.Tenant;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.security.Principal;

/**
 * This {@link javax.servlet.http.HttpSessionListener} is used to track and remove entries from the {@link
 * JiraUserSessionTracker} when the sessions expired
 *
 * @since v4.0
 */
public class JiraUserSessionDestroyListener implements HttpSessionListener
{
    private static final Logger loggerSecurityEvents = LoginLoggers.LOGIN_SECURITY_EVENTS;

    public void sessionCreated(final HttpSessionEvent event)
    {
        HttpSession httpSession = event.getSession();
        // lets be defensive.  Very unlikely I know!
        if (httpSession != null)
        {
            loggerSecurityEvents.info("HttpSession created [" + encodeSessionId(httpSession.getId()) + "]");
        }
    }

    public void sessionDestroyed(final HttpSessionEvent event)
    {
        HttpSession httpSession = event.getSession();
        // lets be defensive.  Very unlikely I know!
        if (httpSession != null)
        {
            if (MultiTenantContext.getManager() == null || MultiTenantContext.getManager().isSingleTenantMode() || MultiTenantContext.getTenantReference().isSet())
            {
                destroySession(httpSession);
            }
            else
            {
                // There is no tenant reference set. We need to set one before we can proceed or we'll get an IllegalStateException.
                // This will happen when tomcat decides to expire a session.
                MultiTenantManager multiTenantManager = MultiTenantContext.getManager();
                final Tenant tenant = multiTenantManager.getTenantFromSession(httpSession);
                MultiTenantContext.getTenantReference().set(tenant, false);
                try
                {
                    destroySession(httpSession);
                }
                finally
                {
                    MultiTenantContext.getTenantReference().remove();
                }
            }
        }
    }

    private void destroySession(final HttpSession session)
    {
        getJiraSessionTracker().removeSession(session.getId());

        Principal principal = (Principal) session.getAttribute(DefaultAuthenticator.LOGGED_IN_KEY);
        loggerSecurityEvents.info("HttpSession [" + encodeSessionId(session.getId()) + "] destroyed for '" + (principal == null ? "anonymous" : principal.getName()) + "'");
    }

    JiraUserSessionTracker getJiraSessionTracker()
    {
        return JiraUserSessionTracker.getInstance();
    }

    private String encodeSessionId(final String id)
    {
        return AtlassianSessionIdUtil.generateASESSIONID(id);
    }
}
