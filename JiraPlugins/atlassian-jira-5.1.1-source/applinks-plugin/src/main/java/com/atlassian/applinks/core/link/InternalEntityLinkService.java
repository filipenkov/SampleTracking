package com.atlassian.applinks.core.link;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.spi.link.MutatingEntityLinkService;

/**
 * Internal interface for the {@link MutatingEntityLinkService} that exposes
 * functionality that is available to applinks-core only.
 *
 * @since   3.0
 */
public interface InternalEntityLinkService extends MutatingEntityLinkService {

    /**
     * Moves all {@link com.atlassian.applinks.api.EntityLink}s from one
     * {@link com.atlassian.applinks.api.ApplicationLink} to another
     * {@link com.atlassian.applinks.api.ApplicationLink}.
     * This is used when an applink's server id changes.
     *
     * @param from
     * @param to
     */
    void migrateEntityLinks(final ApplicationLink from, final ApplicationLink to);
}
