package com.atlassian.jira.plugin.issuenav;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.plugin.issuenav.event.KickassExitIssuesEvent;
import com.atlassian.jira.plugin.issuenav.event.KickassTryIssuesEvent;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.opensymphony.module.propertyset.PropertySet;

import java.util.Date;

/**
 * Action that allows users to enable/disable kickass for themselves.
 *
 * @since v5.1
 */
public class TryKickAssAction extends JiraWebActionSupport
{
    private final UserPropertyManager userPropertyManager;
    private final EventPublisher eventPublisher;

    private boolean enable = false;

    public TryKickAssAction(final UserPropertyManager userPropertyManager, final EventPublisher eventPublisher)
    {
        this.userPropertyManager = userPropertyManager;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public String doDefault() throws Exception
    {
        final User loggedInUser = getLoggedInUser();
        if (loggedInUser != null)
        {
            PropertySet ps = userPropertyManager.getPropertySet(loggedInUser);
            ps.setBoolean(KickassRedirectFilter.TRY_KICKASS_PROPERTY, enable);
            if(!enable)
            {
                ps.setDate(KickassRedirectFilter.OPT_OUT_TIME, new Date());
            }
        }

        if (enable)
        {
            eventPublisher.publish(new KickassTryIssuesEvent());
            return getRedirect("/issues/");
        }
        else
        {
            eventPublisher.publish(new KickassExitIssuesEvent());
            return getRedirect("/secure/IssueNavigator.jspa?mode=show&createNew=true");
        }
    }

    public boolean isEnable()
    {
        return enable;
    }

    public void setEnable(final boolean enable)
    {
        this.enable = enable;
    }
}
