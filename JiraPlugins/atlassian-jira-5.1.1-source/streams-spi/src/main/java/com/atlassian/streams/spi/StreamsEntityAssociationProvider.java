package com.atlassian.streams.spi;

import java.net.URI;

import com.atlassian.streams.api.common.Option;

/**
 * Allows a provider to define an association between {@link EntityIdentifier}s and
 * entity URIs.
 * <p>
 * For instance, the JIRA provider may define that a JIRA issue URI such as
 * "http://hostname/browser/BUGS-123" should be associated with two
 * {@link EntityIdentifier}s:  one for the JIRA issue key "BUGS-123" and one for
 * the JIRA project "BUGS".
 * <p>
 * You only need to implement this interface in a provider plugin if you wish to allow
 * externally generated activity items to be filtered by these properties.
 * 
 * @since 5.0
 */
public interface StreamsEntityAssociationProvider
{
    /**
     * Returns all possible {@link EntityIdentifier}s that are associated with the given
     * entity URI, or with the URI of any of its parent entities. Only {@link EntityIdentifier}s
     * known by this {@link StreamsActivityProvider} are returned.
     * <p>
     * If multiple identifiers are returned, the <em>first</em> one will be used as the
     * activity target when generating a feed in Atom or activitystrea.ms format.
     * <p>
     * This method does not need to check any user's permissions for the entity;
     * {@link #getUserViewPermission(String, EntityIdentifier)} or
     * {@link #getUserEditPermission(String, EntityIdentifier)} will be called for
     * that purpose when appropriate.
     * 
     * @param target  an entity URI; must not be null
     * @return  an {@link Iterable} of zero or more {@link EntityIdentifier}s
     */
    Iterable<EntityIdentifier> getEntityIdentifiers(URI target);
    
    /**
     * Attempts to construct a canonical URI for the given {@link EntityIdentifier}, based
     * on its {@link EntityIdentifier#getType() type} and {@link EntityIdentifier#getValue() value}.
     * 
     * @param identifier  an {@link EntityIdentifier}; must not be null
     * @return  an {@link Option} containing the absolute URI of the entity, or
     *   {@link Option#none()} if the entity is unknown to this provider
     */
    Option<URI> getEntityURI(EntityIdentifier identifier);
    
    /**
     * Returns the key of the filter option, if any, that can be used for matching
     *   this entity; e.g. {@link StandardStreamsFilterOption#PROJECT_KEY}
     * @param identifier  an {@link EntityIdentifier}; must not be null
     * @return  an {@link Option} containing the filter key, or {@link Option#none()} if the
     *   entity is unknown or not associated with any filter option
     */
    Option<String> getFilterKey(EntityIdentifier identifier);
    
    /**
     * Checks whether the current user is allowed to view content related to this entity.  If
     * not, the user will not see third-party activities that are associated with the entity.
     * @param identifier  the entity identifier
     * @return  an {@link Option} containing {@code true} if the user has permission or
     *   {@code false} if the user does not have permission; or {@link Option#none()} if
     *   this provider is unable to determine the answer (e.g. the entity is unknown)
     */
    Option<Boolean> getCurrentUserViewPermission(EntityIdentifier identifier);
    
    /**
     * Checks whether the current user is allowed to edit content related to this entity.  If
     * not, the user will not be able to associate a third-party activity with the entity.
     * <p>
     * If the host application does not distinguish between view and edit permissions for
     * this type of entity, the method should return the same as
     * {@link #getCurrentUserViewPermission(EntityIdentifier)}.
     * @param identifier  the entity identifier
     * @return  an {@link Option} containing {@code true} if the user has permission or
     *   {@code false} if the user does not have permission; or {@link Option#none()} if
     *   this provider is unable to determine the answer (e.g. the entity is unknown)
     */
    Option<Boolean> getCurrentUserEditPermission(EntityIdentifier identifier);
}
