package com.atlassian.jira.whatsnew.conditions;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.webfragment.conditions.AbstractJiraCondition;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.jira.whatsnew.WhatsNewManager;
import com.atlassian.jira.whatsnew.access.WhatsNewAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Displays a What's New web-fragment if:
 *
 * 1. the user isn't currently on a login or logout screen
 * 2. the screen is the first non-login screen of the user's session
 * 3. the user has not flagged that they don't want to see What's New
 */
public class ShowWhatsNewCondition extends AbstractJiraCondition
{
    private static final Logger log = LoggerFactory.getLogger(ShowWhatsNewCondition.class);

    private static final String SESSION_ALREADY_STARTED = "jira.first.page.in.session";
    private final WhatsNewManager whatsNewManager;
    private final WhatsNewAccess whatsNewAccess;
    private final JiraAuthenticationContext authenticationContext;

    public ShowWhatsNewCondition(final WhatsNewManager whatsNewManager, final WhatsNewAccess whatsNewAccess,
                                 final JiraAuthenticationContext authenticationContext)
    {
        this.whatsNewManager = whatsNewManager;
        this.whatsNewAccess = whatsNewAccess;
        this.authenticationContext = authenticationContext;
    }

    @Override
    public boolean shouldDisplay(final User dud, final JiraHelper dud2)
    {
        final User user = authenticationContext.getLoggedInUser();
        if(user == null || !whatsNewAccess.isGrantedTo(user))
        {
            return false;
        }
        final HttpServletRequest req = ExecutingHttpRequest.get();
        final HttpSession session = req.getSession();

        // Don't show the dialog after the first page of a session
        if ("true".equals(session.getAttribute(SESSION_ALREADY_STARTED)))
        {
            return false;
        }
        session.setAttribute(SESSION_ALREADY_STARTED, "true");

        // 2. Don't show the dialog if the user doesn't want it
        return whatsNewManager.isShownForUser(user, true);
    }
}
