/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.search.parameters.filter;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.Predicate;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class NoBrowsePermissionPredicate implements Predicate<GenericValue>
{
    private final User searcher;
    private final PermissionManager permissionManager;

    public NoBrowsePermissionPredicate(User searcher)
    {
        this(searcher, ComponentAccessor.getPermissionManager());
    }

    NoBrowsePermissionPredicate(final User searcher, final PermissionManager permissionManager)
    {
        this.searcher = searcher;
        this.permissionManager = notNull("permissionManager", permissionManager);
    }

    /**
     * @return true if the user does not have permission to browse this issue; false otherwise.
     */
    public boolean evaluate(GenericValue issue)
    {
        return !permissionManager.hasPermission(Permissions.BROWSE, issue, searcher);
    }
}
