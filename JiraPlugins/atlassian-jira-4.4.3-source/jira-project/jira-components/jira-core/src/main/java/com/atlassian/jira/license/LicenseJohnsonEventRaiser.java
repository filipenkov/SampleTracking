package com.atlassian.jira.license;

import javax.servlet.ServletContext;

/**
 * This can raise its Johnson with events related to licensing
 *
 * @since v4.0
 */
public interface LicenseJohnsonEventRaiser
{
    /**
     * This is the key used to indicate that the license is invalid to Johnson
     */
    String LICENSE_INVALID = "license-invalid";
    /**
     * This is the key used to indicate that the license is too old to Johnson
     */
    String LICENSE_TOO_OLD = "license-too-old";

    /**
     * Checks whether the license is too old for this JIRA instance, and raise a Johnson {@link #LICENSE_TOO_OLD} event if it is the case.
     * @param servletContext the current servlet context
     * @param licenseDetails the current license details
     * @return {@code true} if the license is too old and the Johnson event has been raised.
     */
    boolean checkLicenseIsTooOldForBuild(ServletContext servletContext, LicenseDetails licenseDetails);

    /**
     * Checks whether the license is invalid for this JIRA instance, and raise a Johnson {@link #LICENSE_INVALID} event if it is the case.
     * @param servletContext the current servlet context
     * @param licenseDetails the current license details
     * @return {@code true} if the license is invalid and the Johnson event has been raised.
     */
    boolean checkLicenseIsInvalid(ServletContext servletContext, LicenseDetails licenseDetails);
}
