package com.atlassian.administration.quicksearch.rest;

import com.atlassian.administration.quicksearch.spi.UserContext;

/**
 * Provides admin links from remote applications.
 *
 * @since 1.0
 */
public interface RemoteAdminLinksProvider {

    LocationBean getDefaultRemoteAdminLinks(UserContext context);

    LocationBean getRemoteAdminLinksFor(String location, UserContext userContext);
}
