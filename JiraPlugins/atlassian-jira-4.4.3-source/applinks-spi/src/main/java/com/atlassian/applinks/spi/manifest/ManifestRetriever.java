package com.atlassian.applinks.spi.manifest;

import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.applinks.spi.Manifest;

import java.net.URI;

/**
 * @since   3.0
 */
public interface ManifestRetriever
{
    /**
     * @param uri the base url of the application to retrieve a {@link Manifest} from
     * @return the {@link Manifest} of the remote application, will never return null
     * @throws ManifestNotFoundException if the remote application did not provide a manifest for any reason (e.g.
     * remote server was down, remote server does not support UAL, REST end-point is disabled in the remote server etc.)
     */
    Manifest getManifest(URI uri) throws ManifestNotFoundException;

    /**
     * @param uri the base url of the application to retrieve a {@link Manifest} from
     * @param type the type of the target application - this application type's {@link ManifestProducer} specified by
     * it's plugin descriptor will be used to retrieve/generate a suitable manifest
     * @return the {@link Manifest} of the remote application, will never return null
     * @throws ManifestNotFoundException if the remote application did not provide a manifest for any reason (e.g.
     * remote server was down, remote server does not support UAL, REST end-point is disabled in the remote server etc.)
     *
     * @see ManifestProducer#getManifest(URI)
     */
    Manifest getManifest(URI uri, ApplicationType type) throws ManifestNotFoundException;

    /**
     * @param uri the base url of the application to retrieve {@link ApplicationStatus} from
     * @param type the type of the target application - this application type's {@link ManifestProducer} specified by
     * it's plugin descriptor will be used to retrieve/generate the {@link ApplicationStatus} object
     * @return the current state of the application located at the specified base url
     *
     * @see ManifestProducer#getStatus(URI)
     */
    ApplicationStatus getApplicationStatus(URI uri, ApplicationType type);
}
