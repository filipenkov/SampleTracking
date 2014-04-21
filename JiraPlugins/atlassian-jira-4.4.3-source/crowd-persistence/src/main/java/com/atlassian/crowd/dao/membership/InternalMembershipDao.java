package com.atlassian.crowd.dao.membership;

import com.atlassian.crowd.embedded.spi.MembershipDao;
import com.atlassian.crowd.model.membership.InternalMembership;
import com.atlassian.crowd.util.BatchResult;

import java.util.Set;

/**
 * Manages persistence of {@link InternalMembership}.
 */
public interface InternalMembershipDao extends MembershipDao
{
    /**
     * Removes all members of the given group name.
     *
     * @param directoryId Directory id.
     * @param groupName Group name.
     */
    void removeGroupMembers(long directoryId, String groupName);

    /**
     * Removes all relationships from parent groups to the given group name.
     *
     * @param directoryId Directory id.
     * @param groupName Group name.
     */
    void removeGroupMemberships(long directoryId, String groupName);

    /**
     * Removes all {@link com.atlassian.crowd.model.membership.MembershipType#GROUP_USER} relationships
     * from parent groups to the given username.
     *
     * @param directoryId Directory id.
     * @param username Username.
     */
    void removeUserMemberships(long directoryId, String username);

    /**
     * Removes all relationships the the given directory identified by directory id.
     *
     * @param directoryId Directory id.
     */
    void removeAllRelationships(long directoryId);

    /**
     * Removes all user relationships the the given directory identified by directory id.
     *
     * @param directoryId Directory id.
     */
    void removeAllUserRelationships(long directoryId);

    /**
     * Renames username in all relationships.
     *
     * @param directoryId Directory id.
     * @param oldName Old username.
     * @param newName New username.
     */
    void renameUserRelationships(long directoryId, String oldName, String newName);

    /**
     * Renames group name in all relationships.
     *
     * @param directoryId Directory id.
     * @param oldName Old group name.
     * @param newName New group name.
     */
    void renameGroupRelationships(long directoryId, String oldName, String newName);

    /**
     * Bulk add of memberships.
     *
     * @param memberships Set of memberships.
     * @return Batch result.
     */
    BatchResult<InternalMembership> addAll(Set<InternalMembership> memberships);
}
