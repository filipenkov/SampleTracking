package com.atlassian.security.auth.trustedapps;

import javax.servlet.http.HttpServletRequest;

/**
 * Represents a trusted remote application.
 * This object used to verify a request claiming to have come from this application.
 * Such a request is required to have an encrypted certificate and application ID.
 * The certificate decryption and validation is a responsibility of this object.
 */
public interface TrustedApplication extends Application
{
	/**
	 * This method decodes and validates the received certificate.
	 * 
	 * @param certificate - certificate string claiming to have come from this application
	 * 
	 * @return {@link ApplicationCertificate} object if validation succeeds
	 * 
	 * @throws InvalidCertificateException - if either decryption or validation fails
	 */
	ApplicationCertificate decode(EncryptedCertificate certificate, HttpServletRequest request) throws InvalidCertificateException;

    /**
     * @since   v2.2
     * @return  the conditions associated with this application. Requests
     * that do not meet these conditions will not be authenticated.
     */
    RequestConditions getRequestConditions();


    /**
     * Returns the name of the trusted application. This method has been added for UAL,
     * because JIRA and Confluence store the URL of the application by default as the name and UAL
     * is interested in the URL of the application, to be able to create an application link for
     * existing trusted application configurations.
     *
     * @since v2.3
     *
     * @return The name of the trusted application.
     *         Can be null.
     */
    String getName();
}
