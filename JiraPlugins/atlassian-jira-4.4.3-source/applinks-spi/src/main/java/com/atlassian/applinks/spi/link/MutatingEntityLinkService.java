package com.atlassian.applinks.spi.link;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.applinks.api.EntityLink;
import com.atlassian.applinks.api.EntityLinkService;
import com.atlassian.applinks.api.EntityType;
import com.atlassian.applinks.api.TypeNotInstalledException;

/**
 * @since 3.0
 */
public interface MutatingEntityLinkService extends EntityLinkService
{
    /**
     * Create an {@link EntityLink} from an entity in the local application to an entity in the remote application.
     *
     * @param localKey the key of a local entity to create a link <em>from</em> (JIRA project, Confluence space, etc.),
     * for example "JRA", "CONF".
     * @param localType the {@link Class} of the local entity {@link EntityType} to create a link <em>from</em>
     * @param entityLink an {@link EntityLink} object initialised with the details of the target entity in the remote
     * application to link to. Use {@link EntityLinkBuilderFactory} to create the {@link EntityLink} object.
     * @return the created {@link EntityLink} object
     * @see #getEntityLinkBuilderFactory()
     * @see #addReciprocatedEntityLink(String, Class, EntityLink)
     */
    EntityLink addEntityLink(String localKey, Class<? extends EntityType> localType, EntityLink entityLink);

    /**
     * Create an {@link EntityLink} from an entity in the local application to an entity in the remote application, and
     * a reciprocal link from the entity in the remote application back to the local entity. Note that the local
     * outgoing entity link will not be created if the link back from the remote application can not be established for
     * any reason.
     *
     * @param localKey the key of a local entity to create a link <em>from</em> (JIRA project, Confluence space, etc.),
     * for example "JRA", "CONF".
     * @param localType the {@link Class} of the local entity {@link EntityType} to create a link <em>from</em>
     * @param entityLink an {@link EntityLink} object initialised with the details of the target entity in the remote
     * application to link to. Use {@link EntityLinkBuilderFactory} to create the {@link EntityLink} object.
     * @return the created {@link EntityLink} object
     * @throws CredentialsRequiredException if the currently logged in user does not have credentials stored for the
     * authenticator configured for the remote application
     * @throws ReciprocalActionException if the reciprocal link could not be created back from the remote application
     * @see #getEntityLinkBuilderFactory()
     * @see #addEntityLink(String, Class, EntityLink)
     */
    EntityLink addReciprocatedEntityLink(String localKey, Class<? extends EntityType> localType, EntityLink entityLink)
            throws ReciprocalActionException, CredentialsRequiredException;

    /**
     * Delete an {@link EntityLink} from an entity in the local application to an entity in the remote application.
     *
     * @param localKey the key of a local entity to that is the source of the link to be deleted,
     * for example "JRA", "CONF".
     * @param localType the {@link Class} of the local entity {@link EntityType} that is the source of the link to be
     * deleted
     * @param entityLink an {@link EntityLink} representing the entity in the remote application that is the target of
     * the link.
     * @return true if the link was successfully deleted, false otherwise
     * @see #deleteReciprocatedEntityLink(String, Class, EntityLink)
     */
    boolean deleteEntityLink(String localKey, Class<? extends EntityType> localType, EntityLink entityLink);

    /**
     * Delete an {@link EntityLink} from an entity in the local application to an entity in the remote application after
     * first deleting the reciprocal link from the entity in the remote application back to the local entity. Note that
     * the local outgoing entity link will not be deleted if the link back from the remote application could not be
     * deleted for any reason.
     *
     * @param localKey the key of a local entity to that is the source of the link to be deleted, for example "JRA",
     * "CONF".
     * @param localType the {@link Class} of the local entity {@link EntityType} that is the source of the link to be
     * deleted
     * @param entityLink an {@link EntityLink} representing the entity in the remote application that is the target of
     * the link.
     * @return true if the link was successfully deleted, false otherwise
     * @throws CredentialsRequiredException if the currently logged in user does not have credentials stored for the
     * authenticator configured for the remote application
     * @throws ReciprocalActionException if the reciprocal link could not be deleted from the remote application
     * @see #deleteEntityLink(String, Class, EntityLink)
     */
    boolean deleteReciprocatedEntityLink(String localKey, Class<? extends EntityType> localType, EntityLink entityLink)
            throws ReciprocalActionException, CredentialsRequiredException;

    /**
     * Delete all {@link EntityLink}s targeting entities in the application instance specified by the supplied
     * {@link ApplicationLink}
     *
     * @param link an established {@link ApplicationLink}
     */
    void deleteEntityLinksFor(ApplicationLink link);

    /**
     * Make the specified {@link EntityLink} the <em>primary</em> link of its {@link EntityType} for the specified local
     * entity
     *
     * @param localKey the key of a local entity to that is the source of the link to be deleted, for example "JRA",
     * "CONF".
     * @param localType the {@link Class} of the local entity {@link EntityType} that is the source of the link to be
     * deleted
     * @param entityLink an {@link EntityLink} representing the entity in the remote application that is the target of
     * the link.
     * @return the supplied {@link EntityLink}, with its primary status updated
     * @see #getPrimaryEntityLink(Object, Class)
     */
    EntityLink makePrimary(String localKey, Class<? extends EntityType> localType, EntityLink entityLink);

    /**
     * Retrieve an existing {@link EntityLink} that links the specified local entity to the specified remote entity, or
     * return null if no link matching linking the specified entities exists.
     *
     * @param localKey the key of a local entity to that is the source of the link to be retrieved, for example "JRA",
     * "CONF".
     * @param localType the {@link Class} of the local entity {@link EntityType} that is the source of the link to be
     * retrieved
     * @param remoteKey the key of the remote entity to that is the target of the link to be retrieved, for example "JRA",
     * "CONF".
     * @param remoteType the {@link Class} of the remote entity {@link EntityType} that is the target of the link to be
     * retrieved
     * @param applicationId the {@link ApplicationId} of the application containing the entity that is the target of
     * the link to be retrieved
     * @return the specified {@link EntityLink} if it exists, or null otherwise.
     */
    EntityLink getEntityLink(
            String localKey, Class<? extends EntityType> localType,
            String remoteKey, Class<? extends EntityType> remoteType,
            ApplicationId applicationId);

    /**
     * Retrieves all configured entity links for a configured application link.
     *
     * @param applicationLink an established {@link ApplicationLink}, cannot be null.
     *
     * @return all {@link EntityLink}s for this application.
     * @throws TypeNotInstalledException if the application type is not installed
     *
     */
    Iterable<EntityLink> getEntityLinksForApplicationLink(ApplicationLink applicationLink) throws TypeNotInstalledException;

    /**
     * Retrieves all {@link EntityLink}s from a local entity, filtered by the target entity's {@link EntityType}.
     *
     * @param localKey the key of a local entity to that is the source of the links to be retrieved, for example "JRA",
     * "CONF".
     * @param localType the {@link Class} of the local entity {@link EntityType} that is the source of the links to be
     * @param type the {@link Class} of the remote entities {@link EntityType} to retrieve established
     * {@link EntityLink}s for
     * @return an {@link Iterable} containing {@link EntityLink}s associated with the specified entity, of the
     * specified type
     */
    Iterable<EntityLink> getEntityLinksForKey(String localKey, Class<? extends EntityType> localType, Class<? extends EntityType> type);

    /**
     * Retrieves all {@link EntityLink}s from a local entity.
     *
     * @param localKey the key of a local entity to that is the source of the links to be retrieved, for example "JRA",
     * "CONF".
     * @param localType the {@link Class} of the local entity {@link EntityType} that is the source of the links to be
     * @return an {@link Iterable} containing {@link EntityLink}s associated with the specified entity
     */
    Iterable<EntityLink> getEntityLinksForKey(String localKey, Class<? extends EntityType> localType);

    /**
     * @param localKey the key of an entity local to this application, for which to retrieve a link for (e.g. JRA, CONF)
     * @param localType the {@link Class} of the local entity {@link EntityType} that is the source of the link
     * @param type the type of {@link EntityLink} to retrieve (e.g. fisheye-repository)
     * @return the <em>primary</em> entity link of the specified type, or null if no remote entities of the specified
     * type have been linked
     */
    EntityLink getPrimaryEntityLinkForKey(String localKey, Class<? extends EntityType> localType, Class<? extends EntityType> type);

    /**
     * @return an {@link EntityLinkBuilderFactory} for creating {@link EntityLink}s
     */
    EntityLinkBuilderFactory getEntityLinkBuilderFactory();

}
