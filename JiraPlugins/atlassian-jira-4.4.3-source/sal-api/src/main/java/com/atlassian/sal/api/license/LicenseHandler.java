package com.atlassian.sal.api.license;

/**
 * Interface into the license system for the individual application
 *
 * @since 2.0
 */
public interface LicenseHandler
{
    /**
     * Sets the license string for the currently running application
     *
     * @param license The license string
     * @throws IllegalArgumentException if the license string is not a valid license
     */
    void setLicense(String license);
}
