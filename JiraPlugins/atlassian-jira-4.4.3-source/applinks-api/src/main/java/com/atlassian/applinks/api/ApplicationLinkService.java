package com.atlassian.applinks.api;

import com.atlassian.applinks.api.event.ApplicationLinksIDChangedEvent;

/**
 * Provides methods for retrieving {@link ApplicationLink}s representing linked applications (e.g. JIRA, Confluence,
 * etc.)
 *
 * @since   v3.0
 */
public interface ApplicationLinkService
{
    /**
     * Retrieves an {@link ApplicationLink} by its {@link ApplicationId}. Use this method only if you know the
     * {@link ApplicationId} of an existing {@link ApplicationLink}. If you storing an {@link ApplicationId} for
     * future look-ups using this method, you should listen for the {@link ApplicationLinksIDChangedEvent} to ensure
     * your stored {@link ApplicationId} is kept current.
     *
     * @param id the {@link ApplicationId} of a stored {@link ApplicationLink}
     * @return the {@link ApplicationLink} specified by the id, or {@code null} if it does not exist
     * @throws TypeNotInstalledException if the specified {@link ApplicationLink}'s {@link ApplicationType} is
     * not currently installed.
     */
    ApplicationLink getApplicationLink(ApplicationId id) throws TypeNotInstalledException;

    /**
     * Retrieves all {@link ApplicationLink}s.
     *
     * @return an {@link Iterable} of stored {@link ApplicationLink}s, of all
     * {@link ApplicationType}s.
     */
    Iterable<ApplicationLink> getApplicationLinks();

    /**
     * Retrieves all {@link ApplicationLink}s of a particular {@link ApplicationType}.
     *
     * @param type the {@link Class} of the {@link ApplicationType}s to return
     * @return an {@link Iterable} containing all stored {@link ApplicationLink}s of the specified type.
     *         The primary {@link ApplicationLink} is the first link in the list.
     */
    Iterable<ApplicationLink> getApplicationLinks(Class<? extends ApplicationType> type);

    /**
     * Retrieves the <strong>primary</strong> {@link ApplicationLink} of a particular {@link ApplicationType}. This
     * method should be used when you are implementing an integration feature that requires just <em>one</em> remote
     * entity, for example: determining which linked JIRA project to create an issue in, or which linked Confluence
     * space to create a page in. Features that require <em>all</em> {@link ApplicationLink}s of a particular
     * {@link ApplicationType} (like aggregating activity or searching) should use {@link #getApplicationLinks(Class)}.
     *
     * @return the primary {@link ApplicationLink} of the specified type
     * @param type  an application type (e.g. "jira")
     */
    ApplicationLink getPrimaryApplicationLink(Class<? extends ApplicationType> type);

}
