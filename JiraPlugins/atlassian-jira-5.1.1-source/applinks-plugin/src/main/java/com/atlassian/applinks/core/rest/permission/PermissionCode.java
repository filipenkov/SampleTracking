package com.atlassian.applinks.core.rest.permission;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.EntityLink;
import com.atlassian.applinks.api.auth.AuthenticationProvider;

/**
 * Enumeration of possible responses from permission checking resources.
 */
public enum PermissionCode
{
    /**
     * Indicates the request was processed anonymously in the remote application, suggesting that authentication failed
     * or was not attempted
     */
    NO_AUTHENTICATION,

    /**
     * Indicates that the authenticated user does not have permission to perform the specified action
     */
    NO_PERMISSION,

    /**
     * Indicates that the target of the intended action (e.g. an {@link ApplicationLink} or {@link EntityLink}) does
     * not exist
     */
    MISSING,

    /**
     * Indicates that the permission check could not occur as there is no outbound {@link AuthenticationProvider}
     * configured for the specified server
     */
    NO_AUTHENTICATION_CONFIGURED,

    /**
     * Indicates that the permission check in the remote server could not be performed as the server was uncontactable
     */
    NO_CONNECTION,

    /**
     * Indicates that the request could not be processed as there are no credentials stored locally for the context user
     */
    CREDENTIALS_REQUIRED,

    /**
     * Indicates that the request had credentials specified, but they failed to authenticate in the remote application
     */
    AUTHENTICATION_FAILED,

    /**
     * Indicates that the authenticated user is allowed to perform the specified action
     */
    ALLOWED
}
