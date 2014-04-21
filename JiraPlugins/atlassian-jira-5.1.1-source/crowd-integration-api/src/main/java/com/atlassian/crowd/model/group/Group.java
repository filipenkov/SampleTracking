package com.atlassian.crowd.model.group;

import com.atlassian.crowd.model.DirectoryEntity;

/**
 * Represents a group.
 */
public interface Group extends DirectoryEntity, Comparable<Group>
{
    /**
     * @return the type of the group.
     */
    GroupType getType();

    /**
     * @return <code>true<code> if and only if the user is allowed to authenticate.
     */
    boolean isActive();

    /**
     * @return description of the group or <code>null</code> if there is no description.
     */
    String getDescription();
}
