package com.atlassian.jira.welcome.conditions;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.plugin.webfragment.conditions.AbstractJiraCondition;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.welcome.WelcomeUserPreferenceManager;
import com.atlassian.jira.welcome.access.WelcomeScreenAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Displays a Welcome message dialog if:
 *
 * 1. a user is logged in,
 * 2. they can create projects (i.e., they are an admin),
 * 3. there are no projects configured in JIRA, and
 * 4. they have not flagged that they don't want to see the Welcome screen.
 */
public class ShowWelcomeCondition extends AbstractJiraCondition
{
    private static final Logger log = LoggerFactory.getLogger(ShowWelcomeCondition.class);

    private final WelcomeUserPreferenceManager welcomeManager;
    private final JiraAuthenticationContext authenticationContext;
    private final PermissionManager permissionManager;
    private final ProjectService projectService;
    private final WelcomeScreenAccess welcomeScreenAccess;

    public ShowWelcomeCondition(final WelcomeUserPreferenceManager welcomeManager,
                                final JiraAuthenticationContext authenticationContext, 
                                final PermissionManager permissionManager,
                                final ProjectService projectService,
                                final WelcomeScreenAccess welcomeScreenAccess)
    {
        this.welcomeManager = welcomeManager;
        this.authenticationContext = authenticationContext;
        this.permissionManager = permissionManager;
        this.projectService = projectService;
        this.welcomeScreenAccess = welcomeScreenAccess;
    }

    @Override
    public boolean shouldDisplay(final User dud, final JiraHelper dud2)
    {
        // There needs to be a logged-in user
        final User user = authenticationContext.getLoggedInUser();
        if(user == null)
        {
            log.debug("don't show welcome screen; no user is logged in.");
            return false;
        }

        // Don't show the screen if they can't create projects
        final boolean isAdmin = permissionManager.hasPermission(Permissions.ADMINISTER, user);
        if (!isAdmin)
        {
            log.debug("don't show welcome screen; user was not an administrator.");
            return false;
        }

        // Don't show the dialog if the user doesn't want it
        boolean shownForUser = welcomeManager.isShownForUser(user);
        if (!shownForUser)
        {
            log.debug("don't show welcome screen; user has dismissed the welcome screen.");
            return false;
        }
        
        // Allow for other plugins or apps to affect the outcome
        if(!welcomeScreenAccess.isGrantedTo(user))
        {
            log.debug("don't show welcome screen; a condition injected via the welcome screen access SPI said not to.");
            return false;
        }

        // Show the dialog if there are no projects.
        if (getProjectCount(user) > 0)
        {
            log.debug("don't show welcome screen; there are projects in the system.");
            return false;
        }

        log.debug("showing welcome screen; conditions are perfect!");
        return true;
    }
    
    private int getProjectCount(final User user)
    {
        ServiceOutcome<List<Project>> serviceOutcome = projectService.getAllProjects(user);
        if (serviceOutcome.isValid())
        {
            return serviceOutcome.getReturnedValue().size();
        }

        return 0;
    }
}
