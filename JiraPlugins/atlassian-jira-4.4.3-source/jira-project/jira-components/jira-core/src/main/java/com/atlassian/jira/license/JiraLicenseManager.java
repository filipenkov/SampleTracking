package com.atlassian.jira.license;

/**
 * This manager is used to perform Licence releated tasks using the spanking brand new Licencing 2.0
 *
 * @since v4.0
 */
public interface JiraLicenseManager
{
    /**
     * Gets the server ID of the JIRA instance, creates it if it doesn't already exists.
     *
     * @return the server ID for this JIRA instance.
     */
    String getServerId();

    /**
     * Gets the current license details of this instance.
     *
     * @return the JIRA license of this instance, or {@link NullLicenseDetails#NULL_LICENSE_DETAILS} if it doesn't
     *         exists.
     * @throws com.atlassian.extras.common.LicenseException if the stored license string cannot be decoded
     */
    LicenseDetails getLicense();

    /**
     * This will decode the given string into a {@link LicenseDetails} object.  It is assumed that the string is valid.
     * You will wear the consequences if it is not.
     *
     * @param licenseString the input string
     * @return the JIRA license for the given string or {@link NullLicenseDetails#NULL_LICENSE_DETAILS} if it is blank
     * @throws com.atlassian.extras.common.LicenseException if the stored license string cannot be decoded
     */
    LicenseDetails getLicense(String licenseString);

    /**
     * This returns true if the provided licence string can be decoded into a valid licence
     *
     * @param licenseString the input string
     * @return true if it is can be decoded and false otherwise
     */
    boolean isDecodeable(String licenseString);

    /**
     * Sets the current license of this instance.
     *
     * @return the JIRA license of this instance, this shouldn't be null if the {@code license} is valid.
     */
    LicenseDetails setLicense(String license);

    /**
     * This will confirm that user has agreed to proceed under Evaluation terms, typically when the license is too old
     * for the current JIRA build.
     *
     * @param userName the name of the user that amde the confirmation
     */
    void confirmProceedUnderEvaluationTerms(String userName);
}
