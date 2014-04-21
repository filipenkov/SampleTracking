package com.atlassian.crowd.embedded.ofbiz;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.ofbiz.db.OfBizHelper;
import com.atlassian.crowd.embedded.spi.DirectoryDao;
import com.atlassian.crowd.event.migration.XMLRestoreFinishedEvent;
import com.atlassian.crowd.exception.MembershipNotFoundException;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.membership.MembershipType;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.crowd.search.query.membership.MembershipQuery;
import org.ofbiz.core.entity.DelegatorInterface;
import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.atlassian.crowd.embedded.impl.IdentifierUtils.toLowerCase;
import static com.atlassian.crowd.embedded.ofbiz.PrimitiveMap.builder;
import static com.atlassian.crowd.embedded.ofbiz.PrimitiveMap.of;
import static com.atlassian.crowd.model.membership.MembershipType.GROUP_GROUP;
import static com.atlassian.crowd.model.membership.MembershipType.GROUP_USER;
import static org.ofbiz.core.entity.EntityUtil.getOnly;


public class OfBizInternalMembershipDao implements InternalMembershipDao
{
    private final OfBizHelper ofBiz;
    private final DirectoryDao directoryDao;

    // The groups that a user belongs to
    private final ConcurrentMap<MembershipKey, Set<String>> parentsCache = new ConcurrentHashMap<MembershipKey, Set<String>>();
    // The users that belongs to a group
    private final ConcurrentMap<MembershipKey, Set<String>> childrenCache = new ConcurrentHashMap<MembershipKey, Set<String>>();

    public OfBizInternalMembershipDao(final DelegatorInterface genericDelegator, final DirectoryDao directoryDao)
    {
        this.ofBiz = new OfBizHelper(genericDelegator);
        this.directoryDao = directoryDao;
        buildCache();
    }

    public boolean isUserDirectMember(final long directoryId, final String userName, final String groupName)
    {
        // Try with the case we have been given.
        // This is an optimisation that should work in most JIRA instances where group and user names are really all lower case.
        Set<String> parents = parentsCache.get(MembershipKey.getKeyPreserveCase(directoryId, userName, MembershipType.GROUP_USER));
        if (parents == null)
        {
            parents = parentsCache.get(MembershipKey.getKey(directoryId, userName, MembershipType.GROUP_USER));
        }
        if (parents == null)
        {
            return false;
        }
        // Look first for the case we have.  Then try for the lower case.
        // This is an optimisation that should work in most JIRA instances where group and user names are really all lower case.
        if (parents.contains(groupName))
        {
            return true;
        }
        return parents.contains(toLowerCase(groupName));
    }

    public boolean isGroupDirectMember(final long directoryId, final String childGroup, final String parentGroup)
    {
        return isDirectMember(directoryId, GROUP_GROUP, parentGroup, childGroup);
    }

    private boolean isDirectMember(final long directoryId, final MembershipType membershipType, final String parentName, final String childName)
    {
        final GenericValue genericValue = getOnly(ofBiz.findByAnd("Membership",
            builder().put("directoryId", directoryId).putCaseInsensitive("lowerChildName", childName).putCaseInsensitive("lowerParentName",
                parentName).put("membershipType", membershipType.name()).build()));

        return genericValue != null;
    }

    public synchronized void addUserToGroup(final long directoryId, final IdName user, final IdName group)
    {
        createMembership(directoryId, GROUP_USER, group, user);
        addMembershipToParentCache(directoryId, user.getName(), group.getName(), MembershipType.GROUP_USER);
        addMembershipToChildCache(directoryId, user.getName(), group.getName(), MembershipType.GROUP_USER);
    }

    private void createMembership(final long directoryId, final MembershipType membershipType, final IdName parent, final IdName child)
    {
        ofBiz.createValue("Membership", builder().put("directoryId", directoryId).put("childId", child.getId()).put("childName",
            child.getName()).putCaseInsensitive("lowerChildName", child.getName()).put("parentId", parent.getId()).put("parentName",
            parent.getName()).putCaseInsensitive("lowerParentName", parent.getName()).put("membershipType", membershipType.name()).build());
    }

    public void addGroupToGroup(final long directoryId, final IdName child, final IdName parent)
    {
        createMembership(directoryId, GROUP_GROUP, parent, child);
        addMembershipToParentCache(directoryId, child.getName(), parent.getName(), MembershipType.GROUP_GROUP);
        addMembershipToChildCache(directoryId, child.getName(), parent.getName(), MembershipType.GROUP_GROUP);
    }

    public synchronized void removeUserFromGroup(final long directoryId, final IdName user, final IdName group)
            throws MembershipNotFoundException
    {
        removeMembership(directoryId, GROUP_USER, group, user);
        removeMembershipFromParentCache(directoryId, user.getName(), group.getName(), MembershipType.GROUP_USER);
        removeMembershipFromChildCache(directoryId, user.getName(), group.getName(), MembershipType.GROUP_USER);
    }

    private void removeMembership(final long directoryId, final MembershipType membershipType, final IdName parent, final IdName child)
            throws MembershipNotFoundException
    {
        final GenericValue membershipGenericValue = EntityUtil.getOnly(ofBiz.findByAnd("Membership", builder().put("directoryId",
            directoryId).put("childId", child.getId()).put("parentId", parent.getId()).put("membershipType", membershipType.name()).build()));

        if (membershipGenericValue == null)
        {
            throw new MembershipNotFoundException(child.getName(), parent.getName());
        }
        ofBiz.removeValue(membershipGenericValue);
    }

    public void removeGroupFromGroup(final long directoryId, final IdName childGroup, final IdName parentGroup)
            throws MembershipNotFoundException
    {
        removeMembership(directoryId, GROUP_GROUP, parentGroup, childGroup);
        removeMembershipFromParentCache(directoryId, childGroup.getName(), parentGroup.getName(), MembershipType.GROUP_GROUP);
        removeMembershipFromChildCache(directoryId, childGroup.getName(), parentGroup.getName(), MembershipType.GROUP_GROUP);
    }

    public void removeAllMembersFromGroup(final Group group)
    {
        ofBiz.removeByAnd(MembershipEntity.ENTITY, builder().put(MembershipEntity.DIRECTORY_ID, group.getDirectoryId()).put(MembershipEntity.PARENT_NAME, group.getName()).build());
        removeGroupParentFromAllCaches(group.getDirectoryId(), group.getName());
    }

    public void removeAllGroupMemberships(final Group group)
    {
        ofBiz.removeByAnd(MembershipEntity.ENTITY, builder().put(MembershipEntity.DIRECTORY_ID, group.getDirectoryId())
                .put(MembershipEntity.MEMBERSHIP_TYPE, GROUP_GROUP.name()).put(MembershipEntity.CHILD_NAME, group.getName()).build());
        removeGroupChildFromAllCaches(group.getDirectoryId(), group.getName());
    }

    public void removeAllUserMemberships(final User user)
    {
        ofBiz.removeByAnd(MembershipEntity.ENTITY, builder().put(MembershipEntity.DIRECTORY_ID, user.getDirectoryId())
                .put(MembershipEntity.MEMBERSHIP_TYPE, GROUP_USER.name()).put(MembershipEntity.CHILD_NAME, user.getName()).build());
        removeUserFromAllCaches(user.getDirectoryId(), user.getName());
    }

    public List<String> search(final long directoryId, final MembershipQuery query)
    {
        // Optimisation to use the cache if we can.  This is possible if there are no bounding restrictions
        if (canUseCacheSearch(query))
        {
            return searchCache(directoryId, query);
        }

        final PrimitiveMap.Builder filter = builder();
        filter.put("directoryId", directoryId);
        if (query.isFindChildren())
        {
            filter.putCaseInsensitive("lowerParentName", query.getEntityNameToMatch());
        }
        else
        {
            filter.putCaseInsensitive("lowerChildName", query.getEntityNameToMatch());
        }

        if (query.getEntityToReturn().equals(EntityDescriptor.user()) || query.getEntityToMatch().equals(EntityDescriptor.user()))
        {
            filter.put("membershipType", GROUP_USER.name());
        }
        else
        {
            filter.put("membershipType", GROUP_GROUP.name());
        }

        final List<GenericValue> memberships = findMemberships(filter.build());
        final List<String> entityNames = new ArrayList<String>(memberships.size());
        for (GenericValue membership : memberships)
        {
            entityNames.add(query.isFindChildren() ? membership.getString("childName") : membership.getString("parentName"));
        }

        return entityNames;
    }

    /**
     * Search the cache to satisfy ths query.
     * We assume the caller has established this is valid to do.
     * @param directoryId Directory
     * @param query  Query
     * @return List of results
     */
    private List<String> searchCache(final long directoryId, final MembershipQuery<?> query)
    {
        MembershipType type;
        if (query.getEntityToReturn().equals(EntityDescriptor.user()) || query.getEntityToMatch().equals(EntityDescriptor.user()))
        {
            type = MembershipType.GROUP_USER;
        }
        else
        {
            type = MembershipType.GROUP_GROUP;
        }
        if (query.isFindChildren()) {
            // This is get members of group
            return getChildrenOfGroupFromCache(directoryId, query.getEntityNameToMatch(), type);

        }
        else
        {
            // This is get groups a user is a member of
            return getParentsForMemberFromCache(directoryId, query.getEntityNameToMatch(), type);
        }

    }

    /**
     * Find the groups a user is a member of from the cache.
     *
     * @param directoryId  The directory Id
     * @param userName  The user name
     * @return A list of lower-case Group names
     */
    private List<String> getParentsForMemberFromCache(final long directoryId, final String userName, final MembershipType type)
    {
        // Try with the case we have been given.
        // This is an optimisation that should work in most JIRA instances where group and user names are really all lower case.
        Set<String> parents = parentsCache.get(MembershipKey.getKeyPreserveCase(directoryId, userName, type));
        if (parents == null)
        {
            parents = parentsCache.get(MembershipKey.getKey(directoryId, userName, type));
        }
        List<String> groupNames = new ArrayList<String>();
        if (parents != null)
        {
            for (String parent : parents)
            {
                groupNames.add(parent);
            }
        }
        return groupNames;
    }

    /**
     * Get the Members of a Group from the cache.
     * @param directoryId The directory Id
     * @param groupName The group name
     * @return A list of lower-case member names.
     */
    private List<String> getChildrenOfGroupFromCache(final long directoryId, final String groupName, final MembershipType type)
    {
        // Try with the case we have been given.
        // This is an optimisation that should work in most JIRA instances where group and user names are really all lower case.
        Set<String> children = childrenCache.get(MembershipKey.getKeyPreserveCase(directoryId, groupName, type));
        if (children == null)
        {
            children = childrenCache.get(MembershipKey.getKey(directoryId, groupName, type));
        }
        List<String> userNames = new ArrayList<String>();
        if (children != null)
        {
            for (String child : children)
            {
                userNames.add(child);
            }
        }
        return userNames;
    }

    /**
     * Test if this query can be satisfied from the cache.
     * @param query Query
     * @return True if can be saisfied from the cache
     */
    private boolean canUseCacheSearch(final MembershipQuery<?> query)
    {
        // If there are search limits return false
        if (query.getStartIndex() != 0 || query.getMaxResults() != EntityQuery.ALL_RESULTS)
        {
            return false;
        }
        return true;
    }

    private List<GenericValue> findMemberships(final Map<String, Object> filter)
    {
        return ofBiz.findByAnd("Membership", filter);
    }

    /**
     * Invoked by {@link OfBizCacheFlushingManager} to ensure caches are being flushed in the right order on
     * {@link XMLRestoreFinishedEvent}
     */
    public synchronized void flushCache()
    {
        parentsCache.clear();
        childrenCache.clear();
        buildCache();
    }

    private void buildCache()
    {
        // Load all User memberships. Order by username
        for (Directory directory : directoryDao.findAll())
        {
            List<GenericValue> genericValues = findMemberships(of(UserEntity.DIRECTORY_ID, directory.getId()));
            for (GenericValue gvMembership : genericValues)
            {
                Long directoryId = gvMembership.getLong(MembershipEntity.DIRECTORY_ID);
                String groupName = gvMembership.getString(MembershipEntity.PARENT_NAME);
                String userName = gvMembership.getString(MembershipEntity.CHILD_NAME);
                MembershipType type = MembershipType.valueOf(gvMembership.getString(MembershipEntity.MEMBERSHIP_TYPE));

                fastAddUserMembershipToUserCache(directoryId, userName, groupName, type);
                fastAddUserMembershipToGroupCache(directoryId, userName, groupName, type);
            }
        }

        // replace the cache set values with immutable versions
        for (Map.Entry<MembershipKey, Set<String>> setEntry : childrenCache.entrySet())
        {
            childrenCache.put(setEntry.getKey(), Collections.unmodifiableSet(setEntry.getValue()));
        }
        for (Map.Entry<MembershipKey, Set<String>> setEntry : parentsCache.entrySet())
        {
            parentsCache.put(setEntry.getKey(), Collections.unmodifiableSet(setEntry.getValue()));
        }
    }

    /**
     * This is for the initial build of the cache only.
     * It is used because the standard method for adding to the cache is too slow when initialising large systems.
     * Do not call from anywhere but buildCache().
     * @param directoryId Directory ID
     * @param userName User name
     * @param groupName Group name
     */
    private void fastAddUserMembershipToGroupCache(final Long directoryId, final String userName, final String groupName, final MembershipType type)
    {
        MembershipKey key = MembershipKey.getKey(directoryId.longValue(), groupName, type);
        // Get existing value
        Set<String> children = childrenCache.get(key);
        if (children == null)
        {
            children = new HashSet<String>();
            childrenCache.put(key, children);
        }
        // add the new group that this user belongs to
        children.add(toLowerCase(userName));
    }

    /**
     * This is for the initial build of the cache only.
     * It is used because the standard method for adding to the cache is too slow when initialising large systems.
     * Do not call from anywhere but buildCache().
     * @param directoryId Directory ID
     * @param userName User name
     * @param groupName Group name
     */
    private void fastAddUserMembershipToUserCache(final Long directoryId, final String userName, final String groupName, final MembershipType type)
    {
        MembershipKey key = MembershipKey.getKey(directoryId.longValue(), userName, type);
        // Get existing value
        Set<String> parents = parentsCache.get(key);
        if (parents == null)
        {
            parents = new HashSet<String>();
            parentsCache.put(key, parents);
        }
        // add the new group that this user belongs to
        parents.add(toLowerCase(groupName));
    }

    private void addMembershipToParentCache(final Long directoryId, final String userName, final String groupName, final MembershipType type)
    {
        MembershipKey key = MembershipKey.getKey(directoryId.longValue(), userName, type);
        // Create the new Set of groups this user belongs to
        HashSet<String> parents = new HashSet<String>();
        // Get existing value
        Set<String> existingParents = parentsCache.get(key);
        if (existingParents != null)
        {
            parents.addAll(existingParents);
        }
        // add the new group that this user belongs to
        parents.add(toLowerCase(groupName));
        // put an immutable version of this Set in the cache
        parentsCache.put(key, Collections.unmodifiableSet(parents));
    }

    private void addMembershipToChildCache(final Long directoryId, final String userName, final String groupName, final MembershipType type)
    {
        MembershipKey key = MembershipKey.getKey(directoryId.longValue(), groupName, type);
        // Create the new Set of users belonging to this group
        HashSet<String> children = new HashSet<String>();
        // Get existing value
        Set<String> existingChildren = childrenCache.get(key);
        if (existingChildren != null)
        {
            children.addAll(existingChildren);
        }
        // add the new group that this user belongs to
        children.add(toLowerCase(userName));
        // put an immutable version of this Set in the cache
        childrenCache.put(key, Collections.unmodifiableSet(children));
    }

    private void removeMembershipFromParentCache(final Long directoryId, final String userName, final String groupName, final MembershipType type)
    {
        MembershipKey key = MembershipKey.getKey(directoryId.longValue(), userName, type);
        // Create the new Set of groups this user belongs to
        HashSet<String> parents = new HashSet<String>();
        // Get existing value
        Set<String> existingParents = parentsCache.get(key);
        if (existingParents != null)
        {
            parents.addAll(existingParents);
        }
        // remove the group that this user no longer belongs to
        parents.remove(toLowerCase(groupName));
        if (parents.size() == 0)
        {
            // remove the collection from the cache
            parentsCache.remove(key);
        }
        else
        {
            // put an immutable version of this Set in the cache
            parentsCache.put(key, Collections.unmodifiableSet(parents));
        }
    }

    private void removeMembershipFromChildCache(final Long directoryId, final String userName, final String groupName, final MembershipType type)
    {
        MembershipKey key = MembershipKey.getKey(directoryId.longValue(), groupName, type);
        // Create the new Set of groups this user belongs to
        HashSet<String> children = new HashSet<String>();
        // Get existing value
        Set<String> existingChildren = childrenCache.get(key);
        if (existingChildren != null)
        {
            children.addAll(existingChildren);
        }
        // remove the group that this user no longer belongs to
        children.remove(toLowerCase(userName));
        if (children.size() == 0)
        {
            // remove the collection from the cache
            childrenCache.remove(key);
        }
        else
        {
            // put an immutable version of this Set in the cache
            childrenCache.put(key, Collections.unmodifiableSet(children));
        }
    }

    private void removeUserFromAllCaches(final Long directoryId, final String userName)
    {
        List<String> groups = getParentsForMemberFromCache(directoryId, userName, MembershipType.GROUP_USER);
        for (String groupName : groups)
        {
            removeMembershipFromParentCache(directoryId, userName, groupName, MembershipType.GROUP_USER);
            removeMembershipFromChildCache(directoryId, userName, groupName, MembershipType.GROUP_USER);
        }
    }

    private void removeGroupParentFromAllCaches(final Long directoryId, final String groupName)
    {
        List<String> users = getChildrenOfGroupFromCache(directoryId, groupName, MembershipType.GROUP_USER);
        for (String userName : users)
        {
            removeMembershipFromParentCache(directoryId, userName, groupName, MembershipType.GROUP_USER);
            removeMembershipFromChildCache(directoryId, userName, groupName, MembershipType.GROUP_USER);
        }
        List<String> childGroups = getChildrenOfGroupFromCache(directoryId, groupName, MembershipType.GROUP_GROUP);
        for (String childName : childGroups)
        {
            removeMembershipFromParentCache(directoryId, childName, groupName, MembershipType.GROUP_GROUP);
            removeMembershipFromChildCache(directoryId, childName, groupName, MembershipType.GROUP_GROUP);
        }
    }

    private void removeGroupChildFromAllCaches(final Long directoryId, final String groupName)
    {
        List<String> parentGroups = getParentsForMemberFromCache(directoryId, groupName, MembershipType.GROUP_GROUP);
        for (String parentName : parentGroups)
        {
            removeMembershipFromParentCache(directoryId, groupName, parentName, MembershipType.GROUP_GROUP);
            removeMembershipFromChildCache(directoryId, groupName, parentName, MembershipType.GROUP_GROUP);
        }
    }

}
