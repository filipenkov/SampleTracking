package com.atlassian.applinks.host.spi;

import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.applinks.api.EntityLinkService;
import com.atlassian.applinks.api.EntityType;
import com.atlassian.applinks.api.application.jira.JiraProjectEntityType;
import com.atlassian.applinks.api.auth.AuthenticationProvider;
import com.atlassian.applinks.host.util.InstanceNameGenerator;
import com.atlassian.applinks.spi.util.TypeAccessor;

import java.net.URI;

/**
 * Injectable component interface that is implemented by the host application
 * and contains the application specific logic to determine the application's
 * capabilities.
 * <p/>
 * This component is injected into each applinks-core module that requires
 * knowledge of the host application (such as the manifest publisher).
 * <p/>
 * Custom (non-Atlassian) host applications must publish a public component
 * that implements this interface.
 * <p/>
 * TODO: compute and use a user db hash? See https://studio.atlassian.com/browse/APL-259
 *
 * @since 3.0
 */
public interface InternalHostApplication extends HostApplication
{

    /**
     * @return an absolute {@link URI} used as the base for constructing links to help pages. e.g.
     * {@code http://docs.atlassian.com/fisheye/docs-023} or {@code http://confluence.atlassian.com/display/APPLINKS}.
     * The returned {@link URI} should not have a trailing slash.
     */
    URI getDocumentationBaseUrl();

    /**
     * @return the name of this application instance, e.g. "My JIRA Server". If this application type doesn't support
     * customising the name of the instance, implementations should delegate to {@link InstanceNameGenerator} to
     * generate a name from the application's base URL.
     */
    String getName();

    /**
     * @return the {@link ApplicationType} for this application instance. Implementations should delegate to the
     * {@link TypeAccessor} to resolve an instance of the desired type.
     */
    ApplicationType getType();

    /**
     * @return an {@link Iterable} of {@link AuthenticationProvider} classes enumerating the <strong>inbound</strong>
     * authentication methods supported by this application (used to authenticate requests made to this application
     * instance).
     */
    Iterable<Class<? extends AuthenticationProvider>> getSupportedInboundAuthenticationTypes();

/**
     * @return an {@link Iterable} of {@link AuthenticationProvider} classes enumerating the <strong>outbound</strong>
     * authentication methods supported by this application (used to authenticate requests made by this application
     * instance to remote applications).
     */
    Iterable<Class<? extends AuthenticationProvider>> getSupportedOutboundAuthenticationTypes();

    /**
     * @return an {@link Iterable} containing an {@link EntityReference} for every entity in the local instance visible
     * to the currently logged in user. Note, the implementation <strong>must perform a permission check</strong> and
     * return only entities visible the context user (who may be anonymous).
     */
    Iterable<EntityReference> getLocalEntities();

    /**
     * @param key  the key of an entity local to this application (e.g. JRA, CONF)
     * @param type the class of the {@link EntityType} of the entity (e.g. {@link JiraProjectEntityType})
     * @return true, if the specified entity exists, false otherwise. Note, the implementation <strong>must perform a
     * permission check</strong> and return true if, and only if, the specified entity exists and is visible to the
     * context user (who may be anonymous).
     */
    boolean doesEntityExist(String key, Class<? extends EntityType> type);

    /**
     * @param domainObject an entity domain object from the application's API (e.g. com.atlassian.jira.project.Project,
     * com.atlassian.confluence.spaces.Space). Implementations are free to choose which objects supported by this class,
     * but the complete list should be maintained on the {@link EntityLinkService} javadoc.
     * @return an {@link EntityReference} initialised with the key and type of the supplied domain object. This method
     * need not perform any permission checking. Implementations should delegate to the {@link TypeAccessor} to
     * resolve an instance of the desired {@link EntityType}.
     */
    EntityReference toEntityReference(Object domainObject);

    /**
     * @param key the key of a local entity (e.g. "JRA", "FECRUDEV", "CR-BAM")
     * @param type the class of the {@link EntityType} of the entity (e.g. {@link JiraProjectEntityType})
     * @return an {@link EntityReference} initialised with the key and type of the supplied domain object. This method
     * need not perform any permission checking. Implementations should delegate to the {@link TypeAccessor} to
     * resolve an instance of the specified {@link EntityType}.
     */
    EntityReference toEntityReference(String key, Class<? extends EntityType> type);

    /**
     * @param entityReference an {@link EntityReference} representing an entity contained in the local application
     * instance.
     * @return {@code true} if the current user has permission to link or unlink the specified {@link EntityReference}
     * to other entities, {@code false} otherwise. Typically this method will return true if the current user is a
     * global administrator, or a 'project administrator' for the specified entity.
     */
    boolean canManageEntityLinksFor(EntityReference entityReference);

    /**
     * @return  {@code true} if the host application allows public signup,
     * {@code false} otherwise.
     */
    boolean hasPublicSignup();
}
