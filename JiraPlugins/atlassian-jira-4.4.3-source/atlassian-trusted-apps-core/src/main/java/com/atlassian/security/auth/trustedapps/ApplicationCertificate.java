package com.atlassian.security.auth.trustedapps;

import java.util.Date;

/**
 * Represents the certificate received by the filter from a trusted client.
 */
public interface ApplicationCertificate
{
	public Date getCreationTime();
	public String getUserName();
	public String getApplicationID();
}
