package com.atlassian.crowd.directory;

import com.atlassian.crowd.directory.DirectoryCacheChangeOperations.AddRemoveSets;
import com.atlassian.crowd.directory.DirectoryCacheChangeOperations.AddUpdateSets;
import com.atlassian.crowd.directory.DirectoryCacheChangeOperations.GroupsToAddUpdateReplace;
import com.atlassian.crowd.directory.ldap.cache.DirectoryCache;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.group.GroupTemplate;
import com.atlassian.crowd.model.group.GroupType;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.model.user.UserTemplate;
import com.atlassian.crowd.model.user.UserTemplateWithCredentialAndAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DirectoryCacheImplUsingChangeOperations implements DirectoryCache
{
    private static final Logger logger = LoggerFactory.getLogger(DirectoryCacheImplUsingChangeOperations.class);

    private final DirectoryCacheChangeOperations dc;
    
    public DirectoryCacheImplUsingChangeOperations(DirectoryCacheChangeOperations dc)
    {
        this.dc = dc;
    }
    
    public void addOrUpdateCachedUsers(final List<? extends User> remoteUsers, final Date syncStartDate)
        throws OperationFailedException
    {
        TimerStack.push();
        try
        {
            Set<UserTemplateWithCredentialAndAttributes> usersToAdd;
            Set<UserTemplate> usersToUpdate;
        
            TimerStack.push();
            try
            {
                AddUpdateSets<UserTemplateWithCredentialAndAttributes, UserTemplate> result =
                        dc.getUsersToAddAndUpdate(remoteUsers, syncStartDate);
                usersToAdd = result.getToAddSet();
                usersToUpdate = result.getToUpdateSet();
            }
            finally
            {
                logger.info(TimerStack.pop("scanned and compared [ " + remoteUsers.size() + " ] users for update in DB cache in [ {0} ]"));
            }
        
            dc.addUsers(usersToAdd);
            dc.updateUsers(usersToUpdate);
        }
        finally
        {
            logger.info(TimerStack.pop("synchronised [ " + remoteUsers.size() + " ] users in [ {0} ]"));
        }
    }
    
    public void deleteCachedUsers(Set<String> usernames) throws OperationFailedException
    {
        dc.deleteCachedUsers(usernames);
    }

    public void addOrUpdateCachedGroups(final List<? extends Group> remoteGroups, final Date syncStartDate) throws OperationFailedException
    {
         logger.info("scanning [ {} ] groups to add or update", remoteGroups.size());

        TimerStack.push();
        try
        {
            GroupsToAddUpdateReplace addUpdateReplace = dc.findGroupsToUpdate(remoteGroups, syncStartDate);
            
            logger.debug("replacing [ {} ] groups", addUpdateReplace.groupsToReplace.size());
            dc.removeGroups(addUpdateReplace.groupsToReplace.keySet());
            
            Set<GroupTemplate> allToAdd = new HashSet<GroupTemplate>();
            allToAdd.addAll(addUpdateReplace.groupsToAdd);
            allToAdd.addAll(addUpdateReplace.groupsToReplace.values());
            dc.addGroups(allToAdd);
            
            dc.updateGroups(addUpdateReplace.groupsToUpdate);
        }
        finally
        {
            logger.info(TimerStack.pop("synchronized [ " + remoteGroups.size() + " ] groups in [ {0} ]"));
        }
    }
    
    public void deleteCachedGroupsNotIn(final GroupType groupType, final List<? extends Group> remoteGroups, final Date syncStartDate) throws OperationFailedException
    {
        dc.deleteCachedGroupsNotIn(groupType, remoteGroups, syncStartDate);
    }
    
    public void syncUserMembersForGroup(Group group, Collection<String> remoteUsers) throws OperationFailedException
    {
        TimerStack.push();
        try
        {
            // If this is a LEGACY_ROLE with the same name as a GROUP, then ignore it
            if (dc.ignoreGroupOnSynchroniseMemberships(group))
            {
                return;
            }

            AddRemoveSets<String> addRemove = dc.findUserMembershipForGroupChanges(group, remoteUsers);

            logger.debug("adding [ " + addRemove.toAdd.size() + " ] users to group [ " + group.getName() + " ]");
            logger.debug("removing [ " + addRemove.toRemove.size() + " ] users from group [ " + group.getName() + " ]");

            dc.addUserMembershipsForGroup(group, addRemove.toAdd);
            dc.removeUserMembershipsForGroup(group, addRemove.toRemove);

        }
        finally
        {
            logger.debug(TimerStack.pop("synchronised [ " + remoteUsers.size() + " ] user members for group [ " + group.getName() + " ] in [ {0} ]"));
        }
    }

    public void syncGroupMembersForGroup(Group parentGroup, Collection<String> remoteGroups) throws OperationFailedException
    {
        // If this is a LEGACY_ROLE with the same name as a GROUP, then ignore it
        if (dc.ignoreGroupOnSynchroniseMemberships(parentGroup))
        {
            return;
        }
        TimerStack.push();
        try
        {
            AddRemoveSets<String> addRemove = dc.findGroupMembershipForGroupChanges(parentGroup, remoteGroups);


            logger.debug("adding [ " + addRemove.toAdd.size() + " ] group members from group [ " + parentGroup.getName() + " ]");
            logger.debug("removing [ " + addRemove.toRemove.size() + " ] group members to group [ " + parentGroup.getName() + " ]");

            dc.addGroupMembershipsForGroup(parentGroup, addRemove.toAdd);
            dc.removeGroupMembershipsForGroup(parentGroup, addRemove.toRemove);
        }
        finally
        {
            logger.debug(TimerStack.pop("synchronised [ " + remoteGroups.size() + " ] group members for group [ " + parentGroup.getName() + " ] in [ {0} ]"));
        }
    }
    
    public void deleteCachedGroups(Set<String> groupnames) throws OperationFailedException
    {
        dc.deleteCachedGroups(groupnames);
    }
    
    public void deleteCachedUsersNotIn(List<? extends User> users, Date syncStartDate) throws OperationFailedException
    {
        dc.deleteCachedUsersNotIn(users, syncStartDate);
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Event operations
    // -----------------------------------------------------------------------------------------------------------------

    public void addOrUpdateCachedUser(User user) throws OperationFailedException
    {
        dc.addOrUpdateCachedUser(user);
    }

    public void deleteCachedUser(String username) throws OperationFailedException
    {
        dc.deleteCachedUser(username);
    }

    public void addOrUpdateCachedGroup(Group group) throws OperationFailedException
    {
        dc.addOrUpdateCachedGroup(group);
    }

    public void deleteCachedGroup(String groupName) throws OperationFailedException
    {
        dc.deleteCachedGroup(groupName);
    }

    public void addUserToGroup(String username, String groupName) throws OperationFailedException
    {
        dc.addUserToGroup(username, groupName);
    }

    public void removeUserFromGroup(String username, String groupName) throws OperationFailedException
    {
        dc.removeUserFromGroup(username, groupName);
    }

    public void addGroupToGroup(String childGroup, String parentGroup) throws OperationFailedException
    {
        dc.addGroupToGroup(childGroup, parentGroup);
    }

    public void removeGroupFromGroup(String childGroup, String parentGroup) throws OperationFailedException
    {
        dc.removeGroupFromGroup(childGroup, parentGroup);
    }

    public void syncGroupMembershipsForUser(String childUsername, Set<String> parentGroupNames) throws OperationFailedException
    {
        dc.syncGroupMembershipsForUser(childUsername, parentGroupNames);
    }

    public void syncGroupMembershipsAndMembersForGroup(String groupName, Set<String> parentGroupNames, Set<String> childGroupNames) throws OperationFailedException
    {
        dc.syncGroupMembershipsAndMembersForGroup(groupName, parentGroupNames, childGroupNames);
    }
}
