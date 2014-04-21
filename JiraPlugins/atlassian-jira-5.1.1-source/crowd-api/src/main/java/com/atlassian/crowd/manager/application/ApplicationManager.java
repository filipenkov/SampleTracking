package com.atlassian.crowd.manager.application;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.OperationType;
import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.exception.ApplicationNotFoundException;
import com.atlassian.crowd.exception.DirectoryNotFoundException;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.application.RemoteAddress;
import com.atlassian.crowd.search.query.entity.EntityQuery;

import java.util.List;
import java.util.Set;

/**
 * Application management API.
 */
public interface ApplicationManager
{
    /**
     * Will add the given Application to Crowd
     *
     * @param application the Application to add.
     * @return the added Application
     * @throws com.atlassian.crowd.exception.InvalidCredentialException
     *          if there was an error encrypting the Applications password
     */
    Application add(Application application) throws InvalidCredentialException;

    /**
     * Find an application by its ID.
     *
     * @param id database ID.
     * @return application object.
     * @throws ApplicationNotFoundException application with requested ID does not exist.
     */
    Application findById(long id) throws ApplicationNotFoundException;

    /**
     * Find an application by its name.
     *
     * @param name name of application.
     * @return application object.
     * @throws ApplicationNotFoundException application with requested name does not exist.
     */
    Application findByName(String name) throws ApplicationNotFoundException;

    /**
     * Remove an application.
     *
     * @param application application to remove.
     * @throws ApplicationManagerException if the remove operation is not permitted on the given application.
     */
    void remove(Application application) throws ApplicationManagerException;

    /**
     * Will remove a directory from an application.
     * This will also remove all other mapped objects.
     *
     * @param directory   the directory you wish to disociate
     * @param application the application you wish to apply this dissociation too
     * @throws ApplicationManagerException thrown if anything goes bad, updating the application
     */
    void removeDirectoryFromApplication(Directory directory, Application application) throws ApplicationManagerException;

    /**
     * Will add a DirectoryMapping between the passed in Application and Directory. This mapping will be added to the end of the current list of mappings.
     * If a directory mapping already exists for this association this call will simply operate as an update for the given mapping.
     *
     * @param application            the application in question
     * @param directory              the directory associated to the application
     * @param allowAllToAuthenticate to enable/disable the allow all to authenticate flag
     * @param operationTypes         The set of allowed operations for the given directory/application mapping
     * @throws ApplicationNotFoundException if the application could not be found
     * @throws DirectoryNotFoundException if the directory could not be found
     */
    void addDirectoryMapping(Application application, Directory directory, boolean allowAllToAuthenticate, OperationType... operationTypes)
            throws ApplicationNotFoundException, DirectoryNotFoundException;

    /**
     * Will update a directory mapping against the Application moving it to the selected position in the list of DirectoryMappings.
     *
     * @param application the application in question
     * @param directory   the directory associated to the application
     * @param position    This will recognise the need to shift a mapping either up or down in the. A -ve value will not result in an index shift
     * @throws ApplicationNotFoundException if the application could not be found
     * @throws DirectoryNotFoundException if the directory could not be found
     */
    void updateDirectoryMapping(Application application, Directory directory, int position)
            throws ApplicationNotFoundException, DirectoryNotFoundException;

    /**
     * Will update the Directory Mapping, setting to enable/disable allowing all users to authenticate for the given mapping (not taking group membership into consideration)
     *
     * @param application            the application in question
     * @param directory              the directory associated to the application
     * @param allowAllToAuthenticate to enable/disable the allow all to authenticate flag
     * @throws ApplicationNotFoundException if the application could not be found
     * @throws DirectoryNotFoundException if the directory could not be found
     */
    void updateDirectoryMapping(Application application, Directory directory, boolean allowAllToAuthenticate)
            throws ApplicationNotFoundException, DirectoryNotFoundException;

    /**
     * Will update a directory mapping against the Application moving it to the selected position in the list of DirectoryMappings.
     *
     * @param application            the application in question
     * @param directory              the directory associated to the application
     * @param allowAllToAuthenticate to enable/disable the allow all to authenticate flag
     * @param operationTypes         The set of allowed operations for the given directory/application mapping
     * @throws ApplicationNotFoundException if the application could not be found
     * @throws DirectoryNotFoundException if the directory could not be found
     */
    void updateDirectoryMapping(Application application, Directory directory, boolean allowAllToAuthenticate, Set<OperationType> operationTypes)
            throws ApplicationNotFoundException, DirectoryNotFoundException;

    /**
     * Will add a remote address to the current application
     *
     * @param application   the application to update
     * @param remoteAddress the remote address to add
     * @throws ApplicationNotFoundException if the application could not be found
     */
    void addRemoteAddress(Application application, RemoteAddress remoteAddress) throws ApplicationNotFoundException;

    /**
     * Will remove the passed in RemoteAddress from the application
     *
     * @param application   the application to update
     * @param remoteAddress the remote address to remove
     * @throws ApplicationNotFoundException if the application could not be found
     */
    void removeRemoteAddress(Application application, RemoteAddress remoteAddress) throws ApplicationNotFoundException;

    /**
     * Will add a group mapping for the given application + directory mapping.
     *
     * @param application the application to update
     * @param directory   the directory associated to the application
     * @param groupName   the group name to add
     * @throws ApplicationNotFoundException if the application could not be found
     */
    void addGroupMapping(Application application, Directory directory, String groupName) throws ApplicationNotFoundException;

    /**
     * Will remove a group mapping for the given application + directory mapping.
     *
     * @param application the application to update
     * @param directory   the directory associated to the application
     * @param groupName   the group name to remove
     */
    void removeGroupMapping(Application application, Directory directory, String groupName);

    /**
     * Updates an application's details.
     *
     * @param application modified application.
     * @return modified application.
     * @throws ApplicationManagerException error updating application, ie. if you try to rename a permanent application or try to deactivate the CROWD application.
     * @throws ApplicationNotFoundException if the application could not be found
     */
    Application update(Application application) throws ApplicationManagerException, ApplicationNotFoundException;

    /**
     * Takes an application with a List of credentials that are unencrypted, encrypts them
     * and then updates the application in the database with the encrypted credentials
     *
     * @param application        an application with unencrypted password credentials
     * @param passwordCredential unencrypted password.
     * @throws ApplicationManagerException not allowed to update.
     * @throws ApplicationNotFoundException if the application could not be found
     */
    void updateCredential(Application application, PasswordCredential passwordCredential)
            throws ApplicationManagerException, ApplicationNotFoundException;

    /**
     * Takes an application, which contains a hashed credential, and compares that
     * to a hash of the supplied testCredential.
     *
     * @param application application to authenticate as.
     * @param testCredential credentials to authenticate with.
     * @return <code>true</code> iff the testCredential matches the actual application credential.
     * @throws ApplicationNotFoundException if the application could not be found
     */
    boolean authenticate(Application application, PasswordCredential testCredential) throws ApplicationNotFoundException;

    /**
     * Search applications.
     *
     * @param query Application entity query.
     * @return list of Applications.
     */
    List<Application> search(EntityQuery query);

    /**
     * Retrieves all the applications in the Crowd system.
     *
     * @return List of all Applications.
     */
    List<Application> findAll();
}
