package com.atlassian.jira.welcome.constraints;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.welcome.access.WelcomeScreenAccess;
import com.atlassian.studio.haup.api.SupportedApplication;
import com.atlassian.studio.haup.api.UserApplicationAccessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Specifies an {@link WelcomeScreenAccess.Constraint} that restricts access to the Welcome screen
 * to those who have permission to create projects across all of OnDemand.</p>
 *
 * <p>JIRA Access is determined through the {@link UserApplicationAccessService}.</p>
 *
 * <p>Anonymous access is <em>not</em> restricted by this constraint.</p>
 *
 * @see UserApplicationAccessService
 */
public class UserCanAccessAllApps implements WelcomeScreenAccess.Constraint
{
    private static final Logger log = LoggerFactory.getLogger(UserCanAccessAllApps.class);    
    private final UserApplicationAccessService userApplicationAccessService;

    public UserCanAccessAllApps(final UserApplicationAccessService userApplicationAccessService)
    {
        this.userApplicationAccessService = userApplicationAccessService;
    }

    /**
     * Whether this constraint has been met for the specified user.
     *
     * @param user {@inheritDoc}
     * @return {@inheritDoc}
     */
    public boolean apply(final User user)
    {
        log.debug("Checking user '{}' can access all apps", (null == user) ? "nobody" : user.getName());

        if (null == user)
        {
            return false;
        }

        String remoteUsername = user.getName();
        for (SupportedApplication supportedApplication : userApplicationAccessService.getEnabledApplications())
        {
            if (supportedApplication != null && !userApplicationAccessService.hasAccess(remoteUsername, supportedApplication))
            {
                log.debug("User '{}' does not have access to '{}'; returning false.", user.getName(), supportedApplication.getI18nKey());
                return false;
            }
        }
        log.debug("User '{}' is allowed access to all {} running apps", user.getName(), userApplicationAccessService.getEnabledApplications().size());
        return true;
    }
}
