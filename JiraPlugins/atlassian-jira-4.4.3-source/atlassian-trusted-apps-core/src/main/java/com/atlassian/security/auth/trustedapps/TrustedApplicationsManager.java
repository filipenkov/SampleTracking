package com.atlassian.security.auth.trustedapps;


/**
 * A container for trusted application representations. Also contains a reference to this application.
 */
public interface TrustedApplicationsManager
{
	TrustedApplication getTrustedApplication(String id);
	CurrentApplication getCurrentApplication();
}
