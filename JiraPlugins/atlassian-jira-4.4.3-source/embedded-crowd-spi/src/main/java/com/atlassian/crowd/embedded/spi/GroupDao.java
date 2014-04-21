package com.atlassian.crowd.embedded.spi;

import com.atlassian.crowd.exception.DirectoryNotFoundException;
import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.group.GroupWithAttributes;
import com.atlassian.crowd.model.group.InternalDirectoryGroup;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.crowd.util.BatchResult;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface GroupDao
{
    /**
     * Finds group by name.
     *
     * @param directoryId the ID of the directory to look for group
     * @param name group name
     * @return group
     * @throws GroupNotFoundException if the group does not exist
     */
    InternalDirectoryGroup findByName(long directoryId, String name) throws GroupNotFoundException;

    /**
     * Finds group by name. This is different from {@link #findByName(long, String)} in that it also returns
     * the group attributes associated with the retrieved group.
     *
     * @param directoryId the ID of the directory to look for group
     * @param name group name
     * @return group with attributes
     * @throws GroupNotFoundException if the group does not exist
     */
    GroupWithAttributes findByNameWithAttributes(long directoryId, String name) throws GroupNotFoundException;

    /**
     * Adds a new group.
     *
     * @param group group
     * @return the added group
     * @throws DirectoryNotFoundException if the directory specified in group object does not exist
     */
    Group add(Group group) throws DirectoryNotFoundException;

    /**
     * Add a new local group. A local group is a group that does not exist in the remote directory.
     * The implementation must take into account and also persist the fact that the group is only local.
     *
     * @param group group
     * @return the added group
     * @throws DirectoryNotFoundException if the directory specified in group object does not exist
     */
    Group addLocal(Group group) throws DirectoryNotFoundException;

    /**
     * Updates group.
     *
     * @param group group
     * @return the updated group
     * @throws GroupNotFoundException if the group does not exist
     */
    Group update(Group group) throws GroupNotFoundException;

    /**
     * Renames group.
     *
     * @param group group
     * @param newName the new name
     * @return group with new name
     * @throws GroupNotFoundException if the group does not exist
     */
    Group rename(Group group, String newName) throws GroupNotFoundException;

    /**
     * Stores attributes into group.
     * Any existing attributes matching the supplied attribute keys will be replaced.
     *
     * @param group group
     * @param attributes attributes
     * @throws GroupNotFoundException if the group does not exist
     */
    void storeAttributes(Group group, Map<String, Set<String>> attributes) throws GroupNotFoundException;

    /**
     * Remove attributes from group.
     *
     * @param group group
     * @param attributeName attribute to be removed
     * @throws GroupNotFoundException if the group does not exist
     */
    void removeAttribute(Group group, String attributeName) throws GroupNotFoundException;

    /**
     * Removes group.
     *
     * @param group group
     * @throws GroupNotFoundException if the group does not exist
     */
    void remove(Group group) throws GroupNotFoundException;

    /**
     * Searches for group based on the given criteria.
     *
     * @param directoryId directory to perform the search operation on
     * @param query criteria
     * @return list (could be empty) of groups which match the criteria
     */
    <T> List<T> search(long directoryId, EntityQuery<T> query);

    /**
     * Bulk add of groups. Will only add remote groups (ie. isLocal=false)
     * @param groups      to be added
     * @return a list of Groups that <b>failed</b> to be added
     * @throws com.atlassian.crowd.exception.DirectoryNotFoundException if the directory cannot be found
     */
    BatchResult<Group> addAll(Set<? extends Group> groups) throws DirectoryNotFoundException;

    /**
     * Removes all the given groups.
     *
     * @param directoryId directory to perform the operation
     * @param groupNames groups to be removed
     */
    void removeAllGroups(long directoryId, Set<String> groupNames);
}