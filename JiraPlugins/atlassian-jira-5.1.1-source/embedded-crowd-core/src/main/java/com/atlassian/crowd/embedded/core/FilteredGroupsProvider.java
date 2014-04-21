package com.atlassian.crowd.embedded.core;

import java.util.Set;

/**
 * Provide groups to be filtered through this interface.
 */
public interface FilteredGroupsProvider
{
    /**
     * Provides names of groups.
     *
     * @return a set of groups (not null).
     */
    Set<String> getGroups();
}
