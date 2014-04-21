package com.atlassian.jira.welcome.constraints;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.welcome.access.WelcomeScreenAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Specifies an {@link WelcomeScreenAccess.Constraint} that prevents display of the Welcome screen
 * if the logged in user in OnDemand is a system administrator.</p>
 */
public class UserIsNotSysAdmin implements WelcomeScreenAccess.Constraint
{
    private static final Logger log = LoggerFactory.getLogger(UserIsNotSysAdmin.class);
    private final PermissionManager permissionManager;

    public UserIsNotSysAdmin(PermissionManager permissionManager)
    {
        this.permissionManager = permissionManager;
    }

    public boolean apply(User user)
    {
        final boolean isSysAdmin = permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, user);
        log.debug("User = '{}', is sys_admin = '{}'", (null == user) ? "nobody" : user.getName(), isSysAdmin);
        return !isSysAdmin;
    }
}
