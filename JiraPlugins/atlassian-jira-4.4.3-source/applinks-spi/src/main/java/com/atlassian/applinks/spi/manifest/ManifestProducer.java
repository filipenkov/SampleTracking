package com.atlassian.applinks.spi.manifest;

import com.atlassian.applinks.spi.Manifest;

import java.net.URI;

/**
 * Each application type implements and registers its own manifest retriever
 * component.
 *
 * @since v3.0
 */
public interface ManifestProducer
{
    /**
     * <p>
     * During {@link com.atlassian.applinks.api.ApplicationLink} registration via the administrative UI, the user is prompted to choose what type
     * of remote application they are linking to. If selected, this {@link com.atlassian.applinks.spi.application.NonAppLinksApplicationType}
     *  will be asked to provide a manifest for the {@link URI} provided by the user.
     * </p>
     *
     * <p>
     * The {@link Manifest} can either be created completely locally with pre-determined values, or with some
     * interaction with the remote application. A local implementation is far simpler to implement, but has the drawback
     * of allowing users to configure spurious, non-functional {@link com.atlassian.applinks.api.ApplicationLink}s.
     * </p>
     *
     * <p>
     * Considerations:
     * <ul>
     * <li>{@link Manifest#getId()} should return a consistent, unique value for a particular application instance.
     * {@link com.atlassian.applinks.spi.application.ApplicationIdUtil} has been provided to assist in generating a consistent id for a given base url.
     * Implementations that do not retrieve a unique ID from the remote application should <em>always</em> delegate to
     * {@link com.atlassian.applinks.spi.application.ApplicationIdUtil}.</li>
     * <li>The {@link ManifestNotFoundException} may be thrown to indicate that communication with the remote application
     * failed, causing the implementation to be unable to create a {@link Manifest} object.
     * Implementations that synthesise the {@link Manifest} locally should have no need of this exception.</li>
     * </ul>
     * </p>
     *
     *
     * @param url the base url of the remote application to create or retrieve
     * a {@link Manifest} from.
     * @return the created or retrieved {@link Manifest}. Never returns null.
     * @throws ManifestNotFoundException if no manifest could be obtained.
     */
    Manifest getManifest(final URI url) throws ManifestNotFoundException;

    /**
     * <p>
     * Returns the current state of the application located at the specified
     * Url. Since each {@link com.atlassian.applinks.spi.manifest.ManifestProducer}
     * is bound to a specific {@link com.atlassian.applinks.api.ApplicationType},
     * this method must implement the characteristics of that application type
     * to determine whether it is online or not.
     * </p>
     * <p>
     * Implementations could check for the presence of a known rest endpoint,
     * or anything else that would confirm the application at the specified Url
     * is of the correct type.
     * </p>
     * <p>
     * For applications that have no feasible way of detecting their state,
     * this method should return
     * {@link com.atlassian.applinks.spi.manifest.ApplicationStatus#AVAILABLE}.
     * </p>
     *
     * @param url   baseUrl of the peer.
     * @return
     */
    ApplicationStatus getStatus(final URI url);
}
