package com.atlassian.applinks.api;

/**
 * <p>
 * Provides methods for retrieving entities from linked applications that are associated with local
 * entities (e.g. JIRA projects, Confluence spaces, etc.).
 * </p>
 * <br />
 * <p>
 * The {@code Object} typed first parameters of this interface's methods are Strings and/or domain objects that
 * represent project entities from the API of each Atlassian host application. This is specific to each application
 * that the Unified Application Links plugin is deployed to.
 * </p>
 * <br />
 * <p>
 * For example, in <strong>FishEye/Crucible</strong>:
 * <blockquote><pre>
 *   RepositoryHandle repHandle = repositoryManager.getRepository("my-source");
 *   {@link Iterable}&lt;{@link EntityLink}&gt; entityLinkService.{@link #getEntityLinks}(repHandle);
 * </blockquote></pre>
 * Is equivalent to:
* <blockquote><pre>
 *   RepsoitoryData repData = repositoryService.getRepository("my-source");
 *   {@link Iterable}&lt;{@link EntityLink}&gt; entityLinkService.{@link #getEntityLinks}(repData);
 * </blockquote></pre>
 * Both will return an {@link Iterable} of {@link EntityLink}s that are linked from the "my-source" FishEye repository.
 * </p>
 * <br /><br />
 * <p>
 * Whereas:
 * <blockquote><pre>
 *   Project project = projectManager.getProjectByKey("CR-MYSRC");
 *   {@link Iterable}&lt;{@link EntityLink}&gt; entityLinkService.{@link #getEntityLinks}(project);
 * </blockquote></pre>
 * will return an {@link Iterable} of {@link EntityLink}s that are linked from the CR-MYSRC FishEye/Crucible project.
 * </p>
 * <br /><br />
 * <p>
 * In <strong>JIRA</strong> and <strong>Confluence</strong>, where there is only one type of entity which can be linked, you can instead simply provide a
 * String identifier for the entity, for example:
 * <blockquote><pre>
 *   {@link Iterable}&lt;{@link EntityLink}&gt; entityLinkService.{@link #getEntityLinks}("JRA");
 * </blockquote></pre>
 * will return an {@link Iterable} of {@link EntityLink}s that are linked from the JRA JIRA project.
 * </p>
 *
 * @since 3.0.
 *
 */
public interface EntityLinkService
{

    /**
     * Returns the {@link EntityLink}s that are visible to the context user.
     *
     * @param entity an application specific entity domain object, see class javadoc for more details
     * @param type the type of {@link EntityLink}s to retrieve (e.g. fisheye-repository)
     * @return an {@link Iterable} containing {@link EntityLink}s associated with the specified entity, of the specified
     * type and are visible to the context user
     */
    Iterable<EntityLink> getEntityLinks(Object entity, Class<? extends EntityType> type);

    /**
     * Returns the {@link EntityLink}s that are visible to the context user.
     * 
     * @param entity an application specific entity domain object, see class javadoc for more details
     * @return an {@link Iterable} containing {@link EntityLink}s associated with the specified entity and are visible
     * to the context user
     */
    Iterable<EntityLink> getEntityLinks(Object entity);

    /**
     * There are exactly zero or one <em>primary</em> {@link EntityLink}s of each type configured for each local entity.
     * If any links of the specified type exist, exactly one of them will be primary.
     * @param entity an application specific entity domain object, see class javadoc for more details
     * @param type the type of primary {@link EntityLink} to retrieve (e.g. fisheye-repository)
     * @return the <em>primary</em> entity link of the specified type, or null if no remote entities of the specified
     * type have been linked
     */
    EntityLink getPrimaryEntityLink(Object entity, Class<? extends EntityType> type);

}
