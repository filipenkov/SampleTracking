/*
 * Copyright (c) 2002-2006
 * All rights reserved.
 */

package com.atlassian.jira.security;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.security.login.LoginLoggers;
import com.atlassian.jira.security.login.LoginManager;
import com.atlassian.seraph.auth.RoleMapper;
import com.atlassian.seraph.config.SecurityConfig;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Map;

/**
 * A Seraph RoleMapper which maps from group membership to JIRA permissions via a permission scheme. Eg, the permission
 * scheme typically allocates members of the "jira-users" group the {@link Permissions#USE} role.
 */
public class JiraRoleMapper implements RoleMapper
{
    private static final Logger loggerSecurityEvents = LoginLoggers.LOGIN_SECURITY_EVENTS;

    public boolean hasRole(Principal user, HttpServletRequest request, String role)
    {
        final int permissionType = Permissions.getType(role);
        if (permissionType == -1)
        {
            throw new IllegalArgumentException("Unknown role '" + role + "'");
        }
        final boolean hasRole = getPermissionManager().hasPermission(permissionType, (User) user);
        if (!hasRole)
        {
            final String userName = user == null ? "anonymous" : user.getName();
            loggerSecurityEvents.warn("The user '" + userName + "' is NOT AUTHORIZED to perform this request");
        }
        return hasRole;
    }

    public boolean canLogin(Principal user, HttpServletRequest request)
    {
        final boolean canLogin = getLoginManager().authorise((User) user, request);
        if (! canLogin)
        {
            final String userName = user == null ? "anonymous" : user.getName();
            loggerSecurityEvents.warn("The user '" + userName + "' is NOT AUTHORIZED to perform to login for this request");
        }
        return canLogin;
    }

    ///CLOVER:OFF
    public void init(Map map, SecurityConfig securityConfig)
    {
    }

    LoginManager getLoginManager()
    {
        return ComponentManager.getComponentInstanceOfType(LoginManager.class);
    }

    PermissionManager getPermissionManager()
    {
        return ComponentManager.getComponentInstanceOfType(PermissionManager.class);
    }
    ///CLOVER:ON
}
