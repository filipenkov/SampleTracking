package com.atlassian.jira.user.util;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;

/**
 * Try to log a user in automatically behind-the-scenes.
 *
 * @since v5.0.4
 */
public class SneakyAutoLoginUtil
{
    private static final Logger log = Logger.getLogger(SneakyAutoLoginUtil.class);

    public static boolean logUserIn(final String username, final HttpServletRequest request)
    {
        final UserManager userManager = ComponentAccessor.getUserManager();
        final User user = userManager.getUser(username);
        return logUserIn(user, request);
    }

    public static boolean logUserIn(final User user, final HttpServletRequest request)
    {
        if (user == null)
        {
            log.warn("Unable to automatically log in: user is null");
        }
        else
        {
            try
            {
                final CrowdService crowdService = ComponentAccessor.getCrowdService();
                if (crowdService == null)
                {
                    log.warn("Unable to automatically log in: crowdService is null");
                }
                else
                {
                    // Log the user in by populating the appropriate session attributes.
                    final Principal principal = crowdService.getUser(user.getName());
                    request.getSession().setAttribute(DefaultAuthenticator.LOGGED_IN_KEY, principal);
                    request.getSession().setAttribute(DefaultAuthenticator.LOGGED_OUT_KEY, null);

                    return true;
                }
            }
            catch (final Exception e)
            {
                log.warn("Error with automatic log in. The user will need to log in manually.", e);
            }
        }

        return false;
    }
}
