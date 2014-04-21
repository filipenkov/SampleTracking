package com.atlassian.crowd.directory;

import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.model.LDAPDirectoryEntity;

/**
 * Contains methods specific to LDAP direcotories.
 */
public interface LDAPDirectory extends RemoteDirectory
{
    /**
     * Finds a directory entity (principal, group or role)
     * by their distinguished name.
     * <p/>
     * The object class of an entity is used to determine
     * the entity type.
     * <p/>
     * If an object represents both a group and role, then
     * the object is mapped to a group.
     *
     * @param dn          standardised disinguished name.
     * @param entityClass class of the entity to find (either {@link com.atlassian.crowd.model.user.LDAPUserWithAttributes} or {@link com.atlassian.crowd.model.group.LDAPGroupWithAttributes}).
     * @return directory entity corresponding to DN.
     * @throws UserNotFoundException if a user
     *                                 does not exist at the specified DN or the DN does not
     *                                 exist in the directory. This will also be thrown if
     *                                 the entity DOES exist but does not match the base DN
     *                                 or object filter for the entity type.
     * @throws GroupNotFoundException if a user
     *                                 does not exist at the specified DN or the DN does not
     *                                 exist in the directory. This will also be thrown if
     *                                 the entity DOES exist but does not match the base DN
     *                                 or object filter for the entity type.
     * @throws IllegalArgumentException if entityClass is not assignable from User or Group.
     * @throws OperationFailedException  if underlying directory implementation failed to execute the operation.
     */
    <T extends LDAPDirectoryEntity> T findEntityByDN(String dn, Class<T> entityClass) throws UserNotFoundException, GroupNotFoundException, OperationFailedException;
}
