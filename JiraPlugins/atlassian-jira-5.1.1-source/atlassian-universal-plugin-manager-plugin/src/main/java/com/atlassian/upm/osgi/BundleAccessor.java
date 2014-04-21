package com.atlassian.upm.osgi;

import javax.annotation.Nullable;

/**
 * Accesses the bundles installed in an OSGi container
 */
public interface BundleAccessor
{
    /**
     * Fetch all of the installed bundles
     *
     * @return the installed bundles
     */
    Iterable<Bundle> getBundles();

    /**
     * Fetch all installed bundles matching a specified search term
     *
     * @param term the term to search bundles for (or null for all bundles)
     * @return the installed bundles matching the search term
     */
    Iterable<Bundle> getBundles(@Nullable String term);

    /**
     * Fetch a specific bundle designated by the specified {@code bundleId}
     *
     * @param bundleId the ID of the bundle to fetch
     * @return a {@code Bundle} with the specified ID, or null if no such bundle exists
     */
    @Nullable
    Bundle getBundle(long bundleId);
}
