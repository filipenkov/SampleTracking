package com.atlassian.crowd.embedded.ofbiz;

import com.atlassian.crowd.exception.MembershipNotFoundException;
import com.atlassian.crowd.search.query.membership.MembershipQuery;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.user.User;

import java.util.List;

/**
 * This interface is used by OfBizDelegatingMembershipDao to avoid circular dependencies with the User and Group DAOs.
 *
 *
 */
public interface InternalMembershipDao
{
    boolean isUserDirectMember(long directoryId, String userName, String groupName);

    boolean isGroupDirectMember(long directoryId, String childGroup, String parentGroup);

    void addUserToGroup(long directoryId, IdName user, IdName group);

    void addGroupToGroup(long directoryId, IdName child, IdName parent);

    void removeAllMembersFromGroup(Group group);

    void removeAllGroupMemberships(Group group);

    void removeAllUserMemberships(User user);

    void removeUserFromGroup(long directoryId, IdName user, IdName group) throws MembershipNotFoundException;

    void removeGroupFromGroup(long directoryId, IdName childGroup, IdName parentGroup)
            throws MembershipNotFoundException;

    List<String> search(long directoryId, MembershipQuery query);

    void flushCache();
}
