package com.atlassian.security.auth.trustedapps;

import java.util.Date;

/**
 * Default implementation is simply a data container.
 */
public class DefaultApplicationCertificate implements ApplicationCertificate
{
	private final String applicationID;
	private final String userName;
	private final long creationDate;
	
	public DefaultApplicationCertificate(String applicationID, String userName, long creationDate)
	{
        Null.not("applicationID", applicationID);
        Null.not("userName", userName);

        this.applicationID = applicationID;
		this.userName = userName;
		this.creationDate = creationDate;
	}
	
	public String getApplicationID()
	{
		return applicationID;
	}

	public Date getCreationTime()
	{
		return new Date(creationDate);
	}

	public String getUserName()
	{
		return userName;
	}
}