package com.atlassian.crowd.directory.hybrid;

import com.atlassian.crowd.directory.InternalRemoteDirectory;
import com.atlassian.crowd.exception.*;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.group.GroupTemplate;
import com.atlassian.crowd.model.group.InternalDirectoryGroup;

/**
 * Manages local group creation and mutation.
 * <p/>
 * A local group is an accessible group that does NOT exist
 * in LDAP.  If local groups is enabled, then all mutation
 * operations execute on local groups.
 * <p/>
 * Any group in the internal directory section of a
 * {@link com.atlassian.crowd.directory.DbCachingRemoteDirectory}
 * with the shadow attribute set to "false" is an local group.
 */
public class LocalGroupHandler extends InternalGroupHandler
{
    public LocalGroupHandler(InternalRemoteDirectory internalDirectory)
    {
        super(internalDirectory);
    }

    /**
     * Finds a local group.
     *
     * @param groupName name of group.
     * @return local group.
     * @throws GroupNotFoundException local group with supplied name does not exist.
     * @throws com.atlassian.crowd.exception.OperationFailedException underlying directory implementation failed to execute the operation.
     */
    public Group findLocalGroup(String groupName) throws GroupNotFoundException, OperationFailedException
    {
        InternalDirectoryGroup group = getInternalDirectory().findGroupByName(groupName);

        if (group.isLocal())
        {
            return group;
        }
        else
        {
            throw new GroupNotFoundException(groupName);
        }
    }

    /**
     * Creates a local group with the supplied template.
     * <p/>
     * NOTE: if a local group with the same name of groupTemplate already exists,
     * then the underlying directory may throw a ConstraintViolationException or the
     * like (in accordance with the InternalDirectory.addGroup implementations).
     *
     * @param groupTemplate group to add.
     * @return added group.
     * @throws com.atlassian.crowd.exception.InvalidGroupException
     *                                group already exists, either as a local group or as a shadow, with the same name.
     * @throws com.atlassian.crowd.exception.OperationFailedException underlying directory implementation failed to execute the operation.
     */
    public Group createLocalGroup(GroupTemplate groupTemplate)
            throws InvalidGroupException, OperationFailedException, DirectoryNotFoundException
    {
        return getInternalDirectory().addLocalGroup(groupTemplate);
    }

    /**
     * Updates a local group.
     * <p/>
     * If the group found is shadow group or does not exist, a GroupNotFoundException will be thrown.
     *
     * @param groupTemplate group to update.
     * @return updated group.
     * @throws com.atlassian.crowd.exception.OperationFailedException underlying directory implementation failed to execute the operation.
     */
    public Group updateLocalGroup(GroupTemplate groupTemplate)
            throws OperationFailedException, GroupNotFoundException, ReadOnlyGroupException, InvalidGroupException
    {
        findLocalGroup(groupTemplate.getName()); // throws gnfe if no local group with supplied name found

        return getInternalDirectory().updateGroup(groupTemplate);
    }

    public void addUserToLocalGroup(String username, String groupName)
            throws OperationFailedException, GroupNotFoundException, ReadOnlyGroupException, UserNotFoundException
    {
        findLocalGroup(groupName);
        getInternalDirectory().addUserToGroup(username, groupName);
    }

    public void addLocalGroupToLocalGroup(String childGroup, String parentGroup)
            throws OperationFailedException, InvalidMembershipException, GroupNotFoundException, ReadOnlyGroupException
    {
        findLocalGroup(childGroup);
        findLocalGroup(parentGroup);

        getInternalDirectory().addGroupToGroup(childGroup, parentGroup);
    }

    public void removeUserFromLocalGroup(String username, String groupName)
            throws OperationFailedException, GroupNotFoundException, MembershipNotFoundException, ReadOnlyGroupException, UserNotFoundException
    {
        findLocalGroup(groupName); // throws gnfe

        getInternalDirectory().removeUserFromGroup(username, groupName); // throws unfe, mnfe
    }

    public void removeLocalGroupFromLocalGroup(String childGroup, String parentGroup)
            throws OperationFailedException, InvalidMembershipException, GroupNotFoundException, MembershipNotFoundException, ReadOnlyGroupException
    {
        findLocalGroup(childGroup);
        findLocalGroup(parentGroup);

        getInternalDirectory().removeGroupFromGroup(childGroup, parentGroup);
    }
}
