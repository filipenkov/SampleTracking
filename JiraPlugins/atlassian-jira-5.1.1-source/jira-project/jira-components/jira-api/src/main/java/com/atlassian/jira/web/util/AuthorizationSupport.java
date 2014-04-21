package com.atlassian.jira.web.util;

import org.ofbiz.core.entity.GenericValue;

/**
 * Groups a collection of authorization checks used by JIRA's view layer.
 *
 * @since v4.3
 * @see com.atlassian.jira.web.action.JiraWebActionSupport
 */
public interface AuthorizationSupport
{
    boolean isHasPermission(String permName);

    boolean isHasPermission(int permissionsId);

    boolean isHasIssuePermission(String permName, GenericValue issue);

    boolean isHasIssuePermission(int permissionsId, GenericValue issue);

    boolean isHasProjectPermission(String permName, GenericValue project);

    boolean isHasProjectPermission(int permissionsId, GenericValue project);

    /**
     * Returns true if remote user has permission over given entity, false otherwise.
     *
     * @param permName permission type
     * @param entity   entity to check the permission for, e.g. project, issue
     *
     * @return true if remote user has permission over given entity, false otherwise
     *
     * @deprecated since 4.3. Please use either {@link #isHasIssuePermission(String, org.ofbiz.core.entity.GenericValue)}, {@link
     *             #isHasIssuePermission(int, org.ofbiz.core.entity.GenericValue)} or {@link #isHasProjectPermission(String, org.ofbiz.core.entity.GenericValue)},
     *             {@link #isHasProjectPermission(int, org.ofbiz.core.entity.GenericValue)}.
     */
    @Deprecated
    boolean isHasPermission(String permName, GenericValue entity);
}
