package com.atlassian.crowd.dao.application;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.exception.ApplicationNotFoundException;
import com.atlassian.crowd.exception.DirectoryNotFoundException;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.application.RemoteAddress;
import com.atlassian.crowd.embedded.api.OperationType;
import com.atlassian.crowd.search.query.entity.EntityQuery;

import java.util.List;
import java.util.Set;

/**
 * Manages persistence of {@link Application}.
 */
public interface ApplicationDAO
{
    /**
     * Finds application by application id.
     *
     * @param id Application id.
     * @return Application.
     * @throws ApplicationNotFoundException If application of the specified id does not exist.
     */
    Application findById(long id) throws ApplicationNotFoundException;

    /**
     * Finds application by application name, in a case-insensitive way.
     *
     * @param name Application name.
     * @return Application.
     * @throws ApplicationNotFoundException If application of the specified name does not exist.
     */
    Application findByName(String name) throws ApplicationNotFoundException;

    /**
     * Will only create the core attributes to an application, i.e. this will not create directory mappings (thus group mappings).
     * @param application The application.
     * @param passwordCredential Credential.
     * @return The added application.
     */
    Application add(Application application, PasswordCredential passwordCredential);

    /**
     * Will only update the core attributes to an application, i.e. this will not update directory mappings (thus group mappings).
     * remote address' or permissions.
     * @param application The application.
     * @return The added application.
     * @throws ApplicationNotFoundException If the application could not be found.
     */
    Application update(Application application) throws ApplicationNotFoundException;

    /**
     * Updates credential of the given application.
     *
     * @param application The application.
     * @param passwordCredential The new credential.
     * @throws ApplicationNotFoundException If the application could not be found.
     */
    void updateCredential(Application application, PasswordCredential passwordCredential)
            throws ApplicationNotFoundException;

    /**
     * Removes the application. All its aliases will also be removed.
     *
     * @param application The application.
     */
    void remove(Application application);

    /**
     * Will search for all the applications which qualify for the given {@link com.atlassian.crowd.search.query.entity.EntityQuery}.
     *
     * @param query Entity query of type {@link com.atlassian.crowd.search.EntityDescriptor#application()}.
     * @return A list of applications (could be empty).
     */
    List<Application> search(EntityQuery<Application> query);

    /**
     * Adds a mapping between application and directory. Both are identified by ids.
     *
     * @param applicationId Application id.
     * @param directoryId Directory id.
     * @param allowAllToAuthenticate True if all users in the directory are allowed to authenticate against the application.
     * @param operationTypes The collection of permissible operation types.
     * @throws DirectoryNotFoundException if the directory specified by the directoryId does not exist.
     * @throws ApplicationNotFoundException if the application specified by the applicationId does not exist.
     */
    void addDirectoryMapping(long applicationId, long directoryId, boolean allowAllToAuthenticate, OperationType... operationTypes)
            throws DirectoryNotFoundException, ApplicationNotFoundException;

    /**
     * Associates a remote address to the given application.
     *
     * @param applicationId Application id.
     * @param remoteAddress Remote address.
     * @throws ApplicationNotFoundException If the application cannot be found.
     */
    void addRemoteAddress(long applicationId, RemoteAddress remoteAddress) throws ApplicationNotFoundException;

    /**
     * Dissociates  the given remote address from the given application.
     *
     * @param applicationId Application id.
     * @param remoteAddress Remote address, not null.
     * @throws ApplicationNotFoundException If the application cannot be found.
     */
    void removeRemoteAddress(long applicationId, RemoteAddress remoteAddress) throws ApplicationNotFoundException;

    /**
     * Removes a mapping between application and directory. Both are identified by ids.
     *
     * @param applicationId Application id.
     * @param directoryId Directory id.
     * @throws ApplicationNotFoundException If the application cannot be found.
     */
    void removeDirectoryMapping(long applicationId, long directoryId) throws ApplicationNotFoundException;

    /**
     * Removes all the mappings associated with the given directory identified by directory id.
     *
     * @param directoryId Directory id.
     */
    void removeDirectoryMappings(long directoryId);

    /**
     * Adds a group mapping.
     *
     * @param applicationId Application id.
     * @param directoryId Directory id.
     * @param groupName Group name.
     * @throws ApplicationNotFoundException If the application cannot be found.
     */
    void addGroupMapping(long applicationId, long directoryId, String groupName) throws ApplicationNotFoundException;

    /**
     * Removes a group mapping.
     *
     * @param applicationId Application id.
     * @param directoryId Directory id.
     * @param groupName Group name.
     */
    void removeGroupMapping(long applicationId, long directoryId, String groupName);

    /**
     * Removes group mappings.
     *
     * @param directoryId Directory id.
     * @param groupName Group name.
     */
    void removeGroupMappings(long directoryId, String groupName);

    /**
     * Renames group mappings.
     *
     * @param directoryId Directory id.
     * @param oldGroupName old group name.
     * @param newGroupName new group name.
     */
    void renameGroupMappings(long directoryId, String oldGroupName, String newGroupName);

    /**
     * Updates the ordering of directory mappings for an application.
     * This method only has the effect of changing the order of the mapped directories.
     * @param applicationId Application id
     * @param directoryId Directory id
     * @param position New position in the order of directories for this given directory.
     * Positions in the list of directories are absolute and zero based.
     * @throws ApplicationNotFoundException if the application could not be found
     * @throws DirectoryNotFoundException if the directory could not be found
     */
    void updateDirectoryMapping(long applicationId, long directoryId, int position)
            throws ApplicationNotFoundException, DirectoryNotFoundException;

    /**
     * Finds all applications that are authorised for authentication
     * given the directory id and group memberships of a user.
     *
     * @param directoryId directory id of the user.
     * @param groupNames group memberships of the user in the particular directory.
     * @return list of applications the user is authorised to authenticate with.
     */
    List<Application> findAuthorisedApplications(long directoryId, List<String> groupNames);

    /**
     * Updates a directory mapping.
     *
     * @param applicationId Application id
     * @param directoryId Directory id.
     * @param allowAllToAuthenticate True, if all users are allowed to authenticate.
     * @throws ApplicationNotFoundException If the application cannot be found.
     * @throws DirectoryNotFoundException If the directory cannot be found.
     */
    void updateDirectoryMapping(long applicationId, long directoryId, boolean allowAllToAuthenticate)
            throws ApplicationNotFoundException, DirectoryNotFoundException;

    /**
     * Updates a directory mapping.
     *
     * @param applicationId Application id
     * @param directoryId Directory id.
     * @param allowAllToAuthenticate True, if all users are allowed to authenticate.
     * @param operationTypes the set of permissible operation types.
     * @throws ApplicationNotFoundException If the application cannot be found.
     * @throws DirectoryNotFoundException If the directory cannot be found.
     */
    void updateDirectoryMapping(long applicationId, long directoryId, boolean allowAllToAuthenticate, Set<OperationType> operationTypes)
            throws ApplicationNotFoundException, DirectoryNotFoundException;
}
