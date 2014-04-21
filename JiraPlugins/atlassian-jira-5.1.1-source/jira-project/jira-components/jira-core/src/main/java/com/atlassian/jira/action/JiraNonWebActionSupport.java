package com.atlassian.jira.action;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ManagerFactory;
import webwork.action.ActionContext;

import java.security.Principal;

/**
 * The superclass for all non-web actions in JIRA.
 * <p/>
 * This implements {@link SafeAction} and hence can receive any arbitrary map of parameters.
 */
public class JiraNonWebActionSupport extends JiraActionSupport implements SafeAction
{
    /**
     * Whether or not to dispatch an event for this action
     */
    private boolean dispatchEvent = true;

    /**
     * The remote user running this action
     */
    private User remoteUser;

    @Override
    public String execute() throws Exception
    {
        final String result = super.execute();

        if (INPUT.equals(result))
        {
            return ERROR;
        }
        else
        {
            return result;
        }
    }

    @Override
    public String doDefault() throws Exception
    {
        throw new UnsupportedOperationException("You cannot use default commands with non web actions");
    }

    public boolean isDispatchEvent()
    {
        return dispatchEvent;
    }

    public void setDispatchEvent(final boolean dispatchEvent)
    {
        this.dispatchEvent = dispatchEvent;
    }

    @Override
    public User getLoggedInUser()
    {
        if (remoteUser == null)
        {
            final Principal principal = ActionContext.getPrincipal();
            if (principal != null)
            {
                remoteUser = ManagerFactory.getUserManager().getUserObject(principal.getName());
            }
        }
        return remoteUser;
    }

    /**
     * @param remoteUser The Remote User
     * @deprecated This method should not be used. Since v4.3
     */
    public void setRemoteUser(final User remoteUser)
    {
        this.remoteUser = remoteUser;
    }
}
