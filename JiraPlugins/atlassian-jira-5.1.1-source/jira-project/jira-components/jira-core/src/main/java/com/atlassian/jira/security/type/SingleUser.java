/*
 * Copyright (c) 2002-2006
 * All rights reserved.
 */

package com.atlassian.jira.security.type;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.permission.PermissionContext;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.util.UserManager;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericValue;

import java.util.Map;
import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;

public class SingleUser extends AbstractProjectsSecurityType
{
    public static final String DESC = "user";

    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final UserManager userManager;

    public SingleUser(final JiraAuthenticationContext jiraAuthenticationContext, final UserManager userManager)
    {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.userManager = userManager;
    }

    /**
     * Returns display name - i18ned admin.permission.types.single.user
     *
     * @return display name
     */
    public String getDisplayName()
    {
        return jiraAuthenticationContext.getI18nHelper().getText("admin.permission.types.single.user");
    }

    /**
     * Always returns {@link SingleUser#DESC}.
     *
     * @return Always returns {@link SingleUser#DESC}.
     */
    public String getType()
    {
        return DESC;
    }

    public void doValidation(final String key, final Map parameters, final JiraServiceContext jiraServiceContext)
    {
        if (!doValidation(key, parameters))
        {
            final String localisedMessage = jiraAuthenticationContext.getI18nHelper().getText("admin.permissions.errors.please.select.user");
            jiraServiceContext.getErrorCollection().addErrorMessage(localisedMessage);
        }
    }

    public boolean doValidation(final String key, final Map<String, String> parameters)
    {
        final String value = parameters.get(key);
        return ((value != null) && !StringUtils.isBlank(value) && (userManager.getUser(value) != null));
    }

    /**
     * Always returns false;
     *
     * @param entity   This parameter is ignored
     * @param argument This parameter is ignored
     * @return false
     */
    public boolean hasPermission(final GenericValue entity, final String argument)
    {
        return false;
    }

    /**
     * Determines if the single user is the same as the current user.
     * If it is not then false is returned.
     *
     * @param entity        Not needed for this implementation
     * @param argument      The user name that the check is based on
     * @param user          User to check the permission on. If it is null then the check is made on the current user
     * @param issueCreation This parameter is ignored
     * @return true if the user is the current user otherwise false
     * @see com.atlassian.jira.security.type.CurrentAssignee#hasPermission
     * @see com.atlassian.jira.security.type.CurrentReporter#hasPermission
     * @see com.atlassian.jira.security.type.ProjectLead#hasPermission
     * @see com.atlassian.jira.security.type.GroupDropdown#hasPermission
     */
    public boolean hasPermission(final GenericValue entity, final String argument, final com.atlassian.crowd.embedded.api.User user, final boolean issueCreation)
    {
        notNull("user", user);
        notNull("argument", argument);
        final User singleUser = userManager.getUser(argument);

        //if the single user is the current user then return true
        return (singleUser == null) ? false : singleUser.equals(user);
    }

    /**
     * Returns a single element set that contains a {@link User} object found
     * by the given user name.
     *
     * @param ctx      permission context
     * @param username user name string
     * @return single element set
     */
    public Set<User> getUsers(final PermissionContext ctx, final String username)
    {
        final User user = userManager.getUser(username);
        if (user != null)
        {
            return singleton(user);
        }
        return emptySet();
    }
}
