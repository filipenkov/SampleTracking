package com.atlassian.upm.osgi;

import javax.annotation.Nullable;

/**
 * Accesses the services installed in an OSGi container
 */
public interface ServiceAccessor
{
    /**
     * Fetch all of the installed services
     *
     * @return the installed services
     */
    Iterable<Service> getServices();

    /**
     * Fetch a specific service designated by the specified {@code serviceId}
     *
     * @param serviceId the ID of the service to fetch
     * @return a {@code Service} with the specified ID, or null if no such service exists
     */
    @Nullable
    Service getService(long serviceId);
}
