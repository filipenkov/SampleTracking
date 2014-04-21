package com.atlassian.security.auth.trustedapps;

import java.security.Principal;


/**
 * Given the  trusted application certificate this object will lookup the Principal that made this request
 */
public interface UserResolver
{
	/**
	 * Returns the user that made this request or null if this application does not have such a user.
	 * 
	 * @param certificate of the trusted application making this request 
	 * @return user making the request or null
	 */
	public Principal resolve(ApplicationCertificate certificate);
}
