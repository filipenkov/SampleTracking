package com.atlassian.crowd.embedded.ofbiz;

import com.atlassian.crowd.embedded.ofbiz.db.DataAccessException;
import com.atlassian.crowd.embedded.spi.GroupDao;
import com.atlassian.crowd.embedded.spi.MembershipDao;
import com.atlassian.crowd.embedded.spi.UserDao;
import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.exception.MembershipNotFoundException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.query.membership.MembershipQuery;
import com.atlassian.crowd.util.BatchResult;
import org.ofbiz.core.entity.GenericEntityException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class implements the MembershipDao from the Crowd Embedded SPI, but needs to delegate most work to the
 * internal DAO to avoid circular dependencies with the User & Groupp DAOs.
 */
public class OfBizDelegatingMembershipDao implements MembershipDao
{
    private final InternalMembershipDao membershipDao;
    private final OfBizGroupDao groupDao;
    private final OfBizUserDao userDao;

    public OfBizDelegatingMembershipDao(final InternalMembershipDao membershipDao, final UserDao userDao, final GroupDao groupDao)
    {
        this.membershipDao = membershipDao;
        this.groupDao = (OfBizGroupDao) groupDao;
        this.userDao = (OfBizUserDao) userDao;
    }

    public BatchResult<String> addAllUsersToGroup(long directoryId, Collection<String> userNames, String groupName) throws GroupNotFoundException
    {
        BatchResult<String> result = new BatchResult<String>(userNames.size());

        for (String userName : userNames)
        {
            try
            {
                addUserToGroup(directoryId, userName, groupName);
                result.addSuccess(userName);
            }
            catch (UserNotFoundException e)
            {
                result.addFailure(userName);
            }
            catch (DataAccessException e)
            {
                // If we come across any database errors we want to continue processing all other users
                result.addFailure(userName);
            }
        }

        return result;
    }

    public boolean isUserDirectMember(final long directoryId, final String userName, final String groupName)
    {
        return membershipDao.isUserDirectMember(directoryId, userName, groupName);
    }

    public boolean isGroupDirectMember(final long directoryId, final String childGroup, final String parentGroup)
    {
        return membershipDao.isGroupDirectMember(directoryId, childGroup, parentGroup);
    }

    public void addUserToGroup(final long directoryId, final String user, final String group)
            throws UserNotFoundException, GroupNotFoundException
    {
        final IdName idUser = userDao.findByName(directoryId, user);
        final IdName idGroup = groupDao.findByName(directoryId, group);
        membershipDao.addUserToGroup(directoryId, idUser, idGroup);
    }

    public void addGroupToGroup(final long directoryId, final String child, final String parent)
            throws GroupNotFoundException
    {
        final IdName idChild = groupDao.findByName(directoryId, child);
        final IdName idParent = groupDao.findByName(directoryId, parent);

        membershipDao.addGroupToGroup(directoryId, idChild, idParent);
    }

    public void removeUserFromGroup(final long directoryId, final String user, final String group)
            throws UserNotFoundException, GroupNotFoundException, MembershipNotFoundException
    {
        final IdName idUser = userDao.findByName(directoryId, user);
        final IdName idGroup = groupDao.findByName(directoryId, group);
        membershipDao.removeUserFromGroup(directoryId, idUser, idGroup);
    }

    public void removeGroupFromGroup(final long directoryId, final String child, final String parent)
            throws GroupNotFoundException, MembershipNotFoundException
    {
        final IdName idChild = groupDao.findByName(directoryId, child);
        final IdName idParent = groupDao.findByName(directoryId, parent);
        membershipDao.removeGroupFromGroup(directoryId, idChild, idParent);
    }

    public <T> List<T> search(final long directoryId, final MembershipQuery<T> query)
    {
        return result(directoryId, query, membershipDao.search(directoryId, query));
    }

    public void flushCache()
    {
        membershipDao.flushCache();
    }


    @SuppressWarnings("unchecked")
    private <T> List<T> result(final long directoryId, final MembershipQuery<T> query, final List<String> entityNames)
            throws IllegalStateException
    {
        // If the required return type is String just return the list we have been given.
        if (query.getReturnType().isAssignableFrom(String.class))
        {
            ArrayList<String> typedResults = new ArrayList<String>(entityNames.size());
            for (String name : entityNames)
            {
                // Transform from the lower case name to the case preserving name
                if (query.getEntityToReturn().equals(EntityDescriptor.user()))
                {
                    try
                    {
                        final OfBizUser ofBizUser = userDao.findByName(directoryId, name);
                        typedResults.add(ofBizUser.getName());
                    }
                    catch (UserNotFoundException ex)
                    {
                    }
                }
                if (query.getEntityToReturn().equals(EntityDescriptor.group()))
                {
                    try
                    {
                        final OfBizGroup ofBizGroup = groupDao.findByName(directoryId, name);
                        typedResults.add(ofBizGroup.getName());
                    }
                    catch (GroupNotFoundException ex)
                    {
                    }
                }
            }
            return (List<T>) typedResults;
        }
        // If the required return type is User.class then transform to User Objects
        if (query.getReturnType().isAssignableFrom(User.class))
        {
            ArrayList<User> typedResults = new ArrayList<User>(entityNames.size());
            for (String userName : entityNames)
            {
                // Transform from the lower case name to the case preserving name
                try
                {
                    typedResults.add(userDao.findByName(directoryId, userName));
                }
                catch (UserNotFoundException ex)
                {
                }
            }
            return (List<T>) typedResults;
        }
        // If the required return type is Group.class then transform to Group Objects
        if (query.getReturnType().isAssignableFrom(Group.class))
        {
            ArrayList<Group> typedResults = new ArrayList<Group>(entityNames.size());
            for (String groupName : entityNames)
            {
                // Transform from the lower case name to the case preserving name
                try
                {
                    typedResults.add(groupDao.findByName(directoryId, groupName));
                }
                catch (GroupNotFoundException ex)
                {
                }

            }
            return (List<T>) typedResults;
        }
        throw new IllegalArgumentException("Class type '" + query.getReturnType() + "' for return values is not 'String', 'User' or 'Group'");

    }


}
