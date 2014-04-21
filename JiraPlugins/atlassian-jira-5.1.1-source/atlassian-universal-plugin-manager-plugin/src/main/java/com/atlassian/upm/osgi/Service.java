package com.atlassian.upm.osgi;

import javax.annotation.Nullable;

public interface Service
{
    /**
     * Fetch the bundle providing the service
     *
     * @return the bundle providing the service
     */
    Bundle getBundle();

    /**
     * Fetch the bundles using the service
     *
     * @return the bundles using the service
     */
    Iterable<Bundle> getUsingBundles();

    /**
     * Fetch the interface names under which the service has been registered
     *
     * @return the service interface names
     */
    Iterable<String> getObjectClasses();

    /**
     * Fetch the description of the service
     *
     * @return the service description
     */
    @Nullable
    String getDescription();

    /**
     * Fetch the id of the service
     *
     * @return the service id
     */
    long getId();

    /**
     * Fetch the persistent identifier of the service
     *
     * @return the service persistent identifier
     */
    Iterable<String> getPid();

    /**
     * Fetch the ranking of the service
     *
     * @return the service ranking
     */
    int getRanking();

    /**
     * Fetch the vendor of the service
     *
     * @return the service vendor
     */
    @Nullable
    String getVendor();
}
