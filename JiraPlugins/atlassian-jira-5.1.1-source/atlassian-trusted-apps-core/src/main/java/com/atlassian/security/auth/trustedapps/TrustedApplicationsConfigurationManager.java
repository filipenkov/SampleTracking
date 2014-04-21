package com.atlassian.security.auth.trustedapps;

/**
 * <p>
 * An implementation of this component is provided by the host application.
 * Use it to add or delete Trusted Applications.
 * </p>
 *
 * @since v2.2
 */
public interface TrustedApplicationsConfigurationManager
{
    /**
     * Retrieve the application certificate from some other application, over HTTP. Will look for the certificate at
     * <code>${baseUrl}/admin/appTrustCertificate</code>.
     *
     * @param baseUrl
     *            the base URL of the application to be queried
     * @return the retrieved application certificate
     * @throws com.atlassian.security.auth.trustedapps.ApplicationRetriever.RetrievalException
     *             if there are problems with the certificate retrieved from the remote server or the server cannot be
     *             contacted
     * @throws RuntimeException
     *             if there are problems retrieving the certificate from the remote server
     */
    Application getApplicationCertificate(String baseUrl) throws ApplicationRetriever.RetrievalException;

    /**
     * Adds the specified Trusted Application. If an application with the
     * specified ID already exists, the existing record will be replaced
     * silently.
     *
     * @param conditions the conditions that incoming requests must meet in order
     * to be accepted.
     * @param app
     * @return  the newly created or updated Trusted Application.
     */
    TrustedApplication addTrustedApplication(Application app, RequestConditions conditions);

    /**
     * Removes the specified Trusted Application.
     *
     * @param id    the ID of the trusted application.
     * @return  {@code true} if the Trusted Application with the specified ID
     * was found and removed, {@code false} if the specified ID was not found.
     */
    boolean deleteApplication(String id);

    /**
     * @return  all configured Trusted Applications.
     */
    Iterable<TrustedApplication> getTrustedApplications();
}
