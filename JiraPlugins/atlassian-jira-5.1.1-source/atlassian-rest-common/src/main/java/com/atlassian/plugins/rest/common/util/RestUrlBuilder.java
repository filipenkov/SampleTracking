package com.atlassian.plugins.rest.common.util;

import javax.ws.rs.core.Response;
import java.net.URI;

/**
 * @since   2.2
 */
public interface RestUrlBuilder
{
    URI getURI(Response resource);

    <T> T getUrlFor(URI baseUri, Class<T> resourceClass);
}
