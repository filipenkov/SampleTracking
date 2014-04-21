package com.atlassian.applinks.spi.link;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkRequestFactory;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.applinks.api.TypeNotInstalledException;
import com.atlassian.applinks.api.auth.AuthenticationProvider;
import com.atlassian.applinks.spi.auth.AuthenticationConfigurationException;
import com.atlassian.applinks.spi.auth.AuthenticationScenario;
import com.atlassian.applinks.spi.manifest.ManifestNotFoundException;
import com.atlassian.applinks.spi.manifest.ManifestRetriever;
import com.atlassian.applinks.spi.util.TypeAccessor;
import com.atlassian.sal.api.net.ResponseException;

import java.net.URI;

/**
 * This interface adds methods for adding, updating and deleting {@link ApplicationLink}s.
 *
 * @since 3.0
 */
public interface MutatingApplicationLinkService extends ApplicationLinkService
{
    /**
     * Register an outgoing {@link ApplicationLink}
     * @param id the {@link ApplicationId} of the application to create a link to
     * @param type the {@link ApplicationType} of the link (use the {@link TypeAccessor} to access registered {@link ApplicationType}s)
     * @param details an {@link ApplicationLinkDetails} object initialised with the {@link ApplicationLink}'s details. Note that
     * another name may be used if an application link with this name already exists.
     * @return the created {@link ApplicationLink}
     */
    MutableApplicationLink addApplicationLink(ApplicationId id, ApplicationType type, ApplicationLinkDetails details);

    /**
     * Delete the outgoing and incoming {@link ApplicationLink}s to and from a linked application. If the (incoming)
     * link deletion in the remote application fails, the local (outgoing) link will not be deleted. Use
     * {@link #deleteApplicationLink(ApplicationLink)} to delete just the outgoing {@link ApplicationLink} if such
     * behaviour is desired.
     * @param link the {@link ApplicationLink} to delete
     * @throws ReciprocalActionException if there was an issue deleting the link from the remote application
     * @throws CredentialsRequiredException if a request to delete the remote application link could not be created as
     * the current user has not provided credentials for the remote application. See
     * {@link ApplicationLinkRequestFactory#createRequest} for more details.
     */
    void deleteReciprocatedApplicationLink(ApplicationLink link) throws ReciprocalActionException, CredentialsRequiredException;

    /**
     * Deletes an outgoing {@link ApplicationLink}
     * @param link the {@link ApplicationLink} to delete
     */
    void deleteApplicationLink(ApplicationLink link);

    /**
     * @param id the {@link ApplicationId} of a stored {@link ApplicationLink}
     * @return a mutable {@link MutableApplicationLink} object, specified by the id, or {@code null} if it does not exist
     * @throws TypeNotInstalledException if the specified {@link ApplicationLink}'s {@link ApplicationType} is
     * not currently installed.
     */
    MutableApplicationLink getApplicationLink(ApplicationId id) throws TypeNotInstalledException;

    /**
     * Make the specified {@link ApplicationLink} the primary link of its {@link ApplicationType}. Note that exactly
     * one primary link of each type is allowed, so this method will remove the primary flag from the current primary
     * link of the link's type.
     * @param id the {@link ApplicationId} of a stored {@link ApplicationLink}
     * @throws TypeNotInstalledException if the specified {@link ApplicationLink}'s {@link ApplicationType} is
     * not currently installed.
     */
    void makePrimary(ApplicationId id) throws TypeNotInstalledException;

    /**
     * Changes the value of an application link's ID.
     * This operation does NOT emit an event.
     *
     * @throws IllegalArgumentException if {@code oldId} does not exist.
     */
    void changeApplicationId(ApplicationId oldId, ApplicationId newId) throws TypeNotInstalledException;

    /**
     * Creates an application link from this application to a remote application. Similar to addApplicationLink but
     * doesn't retrieves or automatically generates the {@link ApplicationId} for you (depending on the
     * {@link ApplicationType}'s {@link ManifestRetriever} implementation.
     *
     * @param type The type of the remote application
     * @param linkDetails The details of the link to create
     * @return The newly created application link
     * @throws ManifestNotFoundException
     *
     * @since 3.4
     */
    ApplicationLink createApplicationLink(ApplicationType type, ApplicationLinkDetails linkDetails) throws ManifestNotFoundException;

    /**
     * Creates a reciprocal application link from a remote application to this application.
     *
     * @param remoteRpcUrl The base URI of the remote application, for RPC purposes
     * @param customLocalRpcUrl The base URI of the local application, for display purposes, or {@code null} to use the configured base URL
     * @param username The username of an administrator of the remote system
     * @param password The password of the administrator on the remote system
     * @throws NotAdministratorException if the supplied credentials are not those of an administrator on the remote system
     * @throws LinkCreationResponseException if the link creation response from the remote system was invalid
     * @throws AuthenticationResponseException if the authentication response from the remote system was invalid
     * @throws RemoteErrorListException if the remote system reported one or more errors
     *
     * @since 3.4
     */
    void createReciprocalLink(URI remoteRpcUrl, URI customLocalRpcUrl, String username, String password) throws ReciprocalActionException;

    /**
     * Determines whether a set of credentials belong to an administrator of a remote application.
     *
     * @param url The base URI of the remote application
     * @param username A username to check
     * @param password A password to check
     * @return {@code true} if the supplied credentials belong to administator of the remote application. {@code false} otherwise.
     * @throws ResponseException if the response from the remote application was invalid
     *
     * @since 3.4
     */
    boolean isAdminUserInRemoteApplication(URI url, String username, String password)
            throws ResponseException;

    /**
     * Configures authentication on an application link
     *
     * @param applicationLink The application link to configure
     * @param authenticationScenario holder for details that are used to determine the most appropriate
     * {@link AuthenticationProvider}, which will be configured by this method (e.g. if
     * {@link AuthenticationScenario#isCommonUserBase()} and {@link AuthenticationScenario#isTrusted()} are both true,
     * Trusted Applications will be configured).
     * @param username The username of an administrator on the remote application
     * @param password The password of the administrator on the remote application
     * @throws AuthenticationConfigurationException if authentication could not be configured
     *
     * @since 3.4
     */
    void configureAuthenticationForApplicationLink(ApplicationLink applicationLink,
                                                   AuthenticationScenario authenticationScenario,
                                                   String username, String password) throws AuthenticationConfigurationException;

    /**
     * Returns the URI of the application link resource exposed to a remote application.
     *
     * @param id The ID of the remote application
     * @return the URI
     */
    URI createSelfLinkFor(final ApplicationId id);

    /**
     * Checks whether an application link already exists with this name
     * @param name Name of the application link
     * @param id Applink to be excluded from the result. If null, it means no link will be excluded.
     * @return true if an Applink already exist with this name and another 'id'.
     */
    public boolean isNameInUse(final String name, final ApplicationId id);
}
