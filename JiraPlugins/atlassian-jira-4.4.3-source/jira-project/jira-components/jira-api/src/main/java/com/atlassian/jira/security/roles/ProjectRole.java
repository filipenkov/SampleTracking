package com.atlassian.jira.security.roles;

/**
 * A way to group users (@see RoleActors) with projects. An example would be a global role called "testers". If you
 * have a project X and a project Y, you would then be able to configure different RoleActors in the "testers" role
 * for project X than for project Y. You can use ProjectRole objects as the target of Notification and Permission
 * schemes.
 */
public interface ProjectRole
{
    /**
     * Will return the unique identifier for this project role.
     *
     * @return Long the unique id for this proejct role.
     */
    Long getId();

    /**
     * Will get the description of this role, null if not set.
     *
     * @return name or null if not set
     */
    String getName();

    /**
     * Will get the description of this role, null if not set.
     *
     * @return description or null if not set
     */
    String getDescription();
}
