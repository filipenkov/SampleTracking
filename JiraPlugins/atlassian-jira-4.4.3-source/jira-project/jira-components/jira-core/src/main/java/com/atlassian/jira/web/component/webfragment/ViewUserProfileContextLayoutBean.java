package com.atlassian.jira.web.component.webfragment;

import com.opensymphony.user.User;

/**
 * Provides context when rendering user profile links
 *
 * @since v3.12
 */
public class ViewUserProfileContextLayoutBean implements ContextLayoutBean
{
    private final User profileUser;
    private final String actionName;


    public ViewUserProfileContextLayoutBean(User profileUser, String actionName)
    {
        this.profileUser = profileUser;
        this.actionName = actionName;
    }

    public User getProfileUser()
    {
        return profileUser;
    }

    public String getActionName()
    {
        return actionName;
    }
}
