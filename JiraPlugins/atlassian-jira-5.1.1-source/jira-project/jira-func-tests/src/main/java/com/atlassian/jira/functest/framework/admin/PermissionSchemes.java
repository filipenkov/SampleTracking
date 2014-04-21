package com.atlassian.jira.functest.framework.admin;

/**
 * Actions to be performed on the permission schemes in JIRA's administration.
 *
 * @since v4.0
 */
public interface PermissionSchemes
{
    /**
     * Navigates to the Default Permission Scheme.
     * @return the Default Permission Scheme to operate on.
     */
    PermissionScheme defaultScheme();

    /**
     * Navigates to the scheme with the specified name.
     * @param schemeName the permission scheme name.
     * @return the Permission Scheme with the given name.
     */
    PermissionScheme scheme(String schemeName);

    /**
     * Represents a permission scheme that actions can be carried out on
     */
    interface PermissionScheme
    {
        void grantPermissionToGroup(int permission, String groupName);

        void removePermission(int permission, String groupName);
    }
}

