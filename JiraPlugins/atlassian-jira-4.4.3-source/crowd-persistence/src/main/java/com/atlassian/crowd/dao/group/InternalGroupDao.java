package com.atlassian.crowd.dao.group;

import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.embedded.spi.GroupDao;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.group.InternalGroup;
import com.atlassian.crowd.model.group.InternalGroupAttribute;
import com.atlassian.crowd.model.group.InternalGroupWithAttributes;
import com.atlassian.crowd.util.persistence.hibernate.batch.BatchResultWithIdReferences;

import java.util.Collection;
import java.util.Set;

/**
 * Persistance methods necessary to modify an {@link com.atlassian.crowd.directory.InternalDirectory}
 * {@link Group group}.
 */
public interface InternalGroupDao extends GroupDao
{
    /**
     * Finds internal group by directory id and group name.
     *
     * @param directoryId Directory id.
     * @param groupName Group name.
     * @return An internal group.
     * @throws GroupNotFoundException If the group cannot be found.
     */
    InternalGroup findByName(long directoryId, String groupName) throws GroupNotFoundException;

    /**
     * Finds group attributes of the given group identified by group id.
     *
     * @param groupId Group id.
     * @return Set of group attributes.
     */
    Set<InternalGroupAttribute> findGroupAttributes(long groupId);

    /**
     * Removes all groups from a directory.
     *
     * @param directoryId Directory Id.
     */
    void removeAll(long directoryId);

    /**
     * Bulk add of groups and their attributes.
     *
     * @param groups Groups with attributes.
     * @return Batch result.
     */
    BatchResultWithIdReferences<Group> addAll(Collection<InternalGroupWithAttributes> groups);

    /**
     * Bulk find of groups using SQL disjunction.
     *
     * @param directoryId the directory to search for the groups.
     * @param groupnames  names of groups to find
     * @return collection of found groups.
     */
    Collection<InternalGroup> findByNames(long directoryId, Collection<String> groupnames);
}
