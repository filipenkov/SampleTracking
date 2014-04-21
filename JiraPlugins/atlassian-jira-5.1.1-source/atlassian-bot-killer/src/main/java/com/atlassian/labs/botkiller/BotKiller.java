package com.atlassian.labs.botkiller;

import com.atlassian.sal.api.user.UserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * This class will process a request and decide if it should be killed or not because it exhibits bot like behaviour
 */
public class BotKiller
{
    private static final Logger log = LoggerFactory.getLogger(BotKiller.class);

    private static final int LOW_INACTIVE_TIMEOUT = 60;
    private static final int USER_LOW_INACTIVE_TIMEOUT = 10 * 60;

    private final UserManager userManager;

    public BotKiller(final UserManager userManager)
    {
        this.userManager = userManager;
    }

    void processRequest(HttpServletRequest httpServletRequest)
    {
        try
        {
            HttpSession httpSession = httpServletRequest.getSession(false);
            if (httpSession == null)
            {
                // no session leave it the hell alone
                return;
            }
            fiddleWithSession(httpServletRequest, httpSession);
        }
        catch (IllegalStateException ise)
        {
            // We get these if the session has already been invalidated
            // so we ignore it and then move on and get on with our lives!
            //
            // Live and let die!
        }
    }

    private void fiddleWithSession(final HttpServletRequest httpServletRequest, final HttpSession httpSession) throws IllegalStateException
    {
        Integer initialMaxInactiveTimeout = (Integer) httpSession.getAttribute(BotKiller.class.getName());
        if (initialMaxInactiveTimeout == null)
        {
            initialMaxInactiveTimeout = httpSession.getMaxInactiveInterval();
            if (initialMaxInactiveTimeout <= USER_LOW_INACTIVE_TIMEOUT)
            {
                // if  the system defined timeout is lower than what we are going to set it to then don't worry
                // about all this malarky.
                return;
            }

            // this is the first time we have seen this session so we are going to lower the session timeout
            int lowInactiveTimeout = LOW_INACTIVE_TIMEOUT;
            if (thereIsAUserInPlay(httpServletRequest))
            {
                //
                // If we have a user in play we give them more time than an anonymous person.
                //
                lowInactiveTimeout = USER_LOW_INACTIVE_TIMEOUT;
            }

            httpSession.setMaxInactiveInterval(lowInactiveTimeout);
            httpSession.setAttribute(BotKiller.class.getName(), initialMaxInactiveTimeout);

            if (log.isDebugEnabled())
            {
                log.debug("Lowering session inactivity timeout to " + lowInactiveTimeout);
            }
        }
        else
        {
            // ok we have seen this session so lets upgrade the inactivity timeout back to the original level
            if (httpSession.getMaxInactiveInterval() != initialMaxInactiveTimeout)
            {
                httpSession.setMaxInactiveInterval(initialMaxInactiveTimeout);
                if (log.isDebugEnabled())
                {
                    log.debug("Upping session inactivity timeout to " + initialMaxInactiveTimeout);
                }
            }
        }
    }

    private boolean thereIsAUserInPlay(HttpServletRequest httpServletRequest)
    {
        try
        {
            if (userManager.getRemoteUsername(httpServletRequest) != null)
            {
                return true;
            }
            if (httpServletRequest.getRemoteUser() != null)
            {
                return true;
            }
        } 
        catch (Exception e)
        {
            log.error("Error occurred when figuring out if the session has a user, assuming there is no user.", e);
        }
        return false;
    }

}
