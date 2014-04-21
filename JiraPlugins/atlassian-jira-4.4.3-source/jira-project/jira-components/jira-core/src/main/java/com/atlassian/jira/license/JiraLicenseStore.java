package com.atlassian.jira.license;

/**
 * A store to save license to a persistent location.
 *
 * @since v4.0
 */
public interface JiraLicenseStore
{
    /**
     * Retrieves the current license String from the persistent location.
     *
     * @return the current license String from the persistent location.
     */
    String retrieve();

    /**
     * Stores the given license String into the persistent location.
     *
     * @param licenseString the license String to store
     */
    void store(String licenseString);

    /**
     * Resets persisted information about license in use being too old for current build.
     */
    void resetOldBuildConfirmation();

    /**
     * This will confirm that user has agreed to proceed under Evaluation terms, typically when the license is too old
     * for the current JIRA build.
     *
     * @param userName the name of the user that amde the confirmation
     */
    void confirmProceedUnderEvaluationTerms(String userName);

    /**
     * Gets the server ID from the persistence backend
     *
     * @return the server ID, {@code null} if not found
     */
    String retrieveServerId();

    /**
     * Stored the server ID to the persistent backend
     *
     * @param serverId the server ID to store
     */
    void storeServerId(String serverId);
}
