package com.atlassian.crowd.embedded.spi;

import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.exception.MembershipNotFoundException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.search.query.membership.MembershipQuery;
import com.atlassian.crowd.util.BatchResult;

import java.util.Collection;
import java.util.List;

public interface MembershipDao
{
    /**
     * Determines whether the user is a direct member of the group.
     *
     * @param directoryId the directory to perform the operation
     * @param userName user
     * @param groupName group
     * @return true if the user is a direct member of the group
     */
    boolean isUserDirectMember(long directoryId, String userName, String groupName);

    /**
     * Determines whether the group is a direct member of the (supposedly) parent group.
     *
     * @param directoryId the directory to perform the operation
     * @param childGroup child group
     * @param parentGroup parent group
     * @return true if the group is a direct member of the (supposedly) parent group
     */
    boolean isGroupDirectMember(long directoryId, String childGroup, String parentGroup);

    /**
     * Adds user as a member of group.
     *
     * @param directoryId the directory to perform the operation
     * @param userName user
     * @param groupName group
     * @throws UserNotFoundException if the user does not exist
     * @throws GroupNotFoundException if the group does not exist
     */
    void addUserToGroup(long directoryId, String userName, String groupName) throws UserNotFoundException, GroupNotFoundException;

    /**
     * Adds all the given users into the given group.
     *
     * @param directoryId the directory to perform the operation
     * @param userNames the collection of users
     * @param groupName group
     * @throws GroupNotFoundException if the group does not exist
     *
     * @return result containing both successful and failed users
     */
    BatchResult<String> addAllUsersToGroup(long directoryId, Collection<String> userNames, String groupName) throws GroupNotFoundException;

    /**
     * Adds group as a child of the (supposedly) parent group.
     *
     * @param directoryId the directory to perform the operation
     * @param childGroup the (supposedly) child group
     * @param parentGroup parent group
     * @throws GroupNotFoundException if either child or parent group is not found
     */
    void addGroupToGroup(long directoryId, String childGroup, String parentGroup) throws GroupNotFoundException;

    /**
     * Removes user as a member of the given group.
     *
     * @param directoryId the directory to perform the operation
     * @param userName user
     * @param groupName group
     * @throws UserNotFoundException if the user does not exist
     * @throws GroupNotFoundException if the group does not exist
     * @throws MembershipNotFoundException if the user is not a member of the said group
     */
    void removeUserFromGroup(long directoryId, String userName, String groupName) throws UserNotFoundException, GroupNotFoundException, MembershipNotFoundException;

    /**
     * Removes group from the parent group.
     *
     * @param directoryId the directory to perform the operation
     * @param childGroup child group
     * @param parentGroup parent group
     * @throws GroupNotFoundException if either child or parent group does not exist
     * @throws MembershipNotFoundException if the membership relationship between the child and parent group does not exist
     */
    void removeGroupFromGroup(long directoryId, String childGroup, String parentGroup) throws GroupNotFoundException, MembershipNotFoundException;

    /**
     * Search for memberships by the given criteria.
     *
     * @param directoryId the directory to perform the operation
     * @param query criteria
     * @return list (can be empty but never null) of memberships which match the given criteria
     */
    <T> List<T> search(long directoryId, MembershipQuery<T> query);
}
