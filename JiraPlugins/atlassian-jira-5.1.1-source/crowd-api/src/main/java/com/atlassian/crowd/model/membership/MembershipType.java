package com.atlassian.crowd.model.membership;

/**
 * Type of membership relationship.
 */
public enum MembershipType
{
    /**
     * For user as member of group.
     */
    GROUP_USER,

    /**
     * For group as a nested member of another group.
     */
    GROUP_GROUP
}
