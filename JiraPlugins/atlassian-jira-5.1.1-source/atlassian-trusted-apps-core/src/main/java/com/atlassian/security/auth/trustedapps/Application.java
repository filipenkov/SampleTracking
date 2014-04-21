package com.atlassian.security.auth.trustedapps;

import java.security.PublicKey;

/**
 * Represents a base of an application that can participate in a trusted relationship.
 */
public interface Application
{
	/**
	 * Public key of this application. Public key will be sent to the remote server when requesting it to trust this application.
	 */
	PublicKey getPublicKey();

	/**
	 * @return Unique ID representing this application
	 */
	String getID();
}