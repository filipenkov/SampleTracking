package com.atlassian.applinks.api;

/**
 * Provides methods to bypass permission checking when retrieving entity links. In general, {@link EntityLinkService}
 * should be used instead of this interface.
 *
 * @since 3.6
 */
public interface SubvertedEntityLinkService extends EntityLinkService
{
    /**
     * @param entity an application specific entity domain object, see class javadoc for more details
     * @param type the type of {@link EntityLink}s to retrieve (e.g. fisheye-repository)
     * @return an {@link Iterable} containing {@link EntityLink}s associated with the specified entity, of the specified type
     * @since 3.6
     */
    Iterable<EntityLink> getEntityLinksNoPermissionCheck(Object entity, Class<? extends EntityType> type);

    /**
     * @param entity an application specific entity domain object, see class javadoc for more details
     * @return an {@link Iterable} containing {@link EntityLink}s associated with the specified entity
     * @since 3.6
     */
    Iterable<EntityLink> getEntityLinksNoPermissionCheck(Object entity);
}
