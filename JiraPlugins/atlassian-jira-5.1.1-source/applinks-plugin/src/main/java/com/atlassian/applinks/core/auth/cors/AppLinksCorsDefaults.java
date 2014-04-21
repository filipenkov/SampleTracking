package com.atlassian.applinks.core.auth.cors;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.plugins.rest.common.security.descriptor.CorsDefaults;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.collections.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * Exposes {@link ApplicationLink}s as a mechanism for configuring support for Cross-Origin Resource Sharing (CORS)
 * requests. By default, the presence of an Application Link enables CORS requests without credentials. Requests with
 * credentials must be explicitly enabled in the CORS inbound authentication settings.
 *
 * @since 3.7
 */
public class AppLinksCorsDefaults implements CorsDefaults
{
    private final CorsService corsService;

    public AppLinksCorsDefaults(CorsService corsService)
    {
        this.corsService = corsService;
    }

    /**
     * Searches for an {@link ApplicationLink} associated with the specified origin URL and returns {@code true} if
     * any are found; otherwise, {@code false}.
     * <p/>
     * This implementation means that, by default, CORS requests are allowed for any origin which has an Application
     * Link present.
     * 
     * @param uri the URL of the CORS origin
     * @return {@code true} if an Application Link exists for the provided URL; otherwise, {@code false}
     */
    public boolean allowsOrigin(String uri)
    {
        return CollectionUtils.isNotEmpty(corsService.getApplicationLinksByOrigin(uri));
    }

    /**
     * Determines whether CORS requests with credentials have been enabled for the {@link ApplicationLink}(s) associated
     * with the provided URL. Per the interface contract of {@link CorsDefaults#allowsCredentials(String)}, it is
     * expected that the URL provided has already passed the {@link #allowsOrigin(String)} check.
     * <p/>
     * Note: Because CORS Origins only include scheme, host and port, it is possible that multiple links will match the
     * provided URL. When multiple links match, credentialed requests must explicitly be allowed for <i>all</i> of them
     * or it will not be allowed for any of them (even if it is explicitly allowed for some). This all-or-nothing
     * approach is taken to make credentialed requests as secure as possible.
     *
     * @param uri the URL of the allowed CORS origin
     * @return {@code true} if credentialed requests have been explicitly allowed by the Application Link(s);
     *         otherwise, {@code false}
     * @throws IllegalArgumentException Thrown if the URL provided is not an allowed origin.
     */
    public boolean allowsCredentials(String uri) throws IllegalArgumentException
    {
        Collection<ApplicationLink> links = corsService.getRequiredApplicationLinksByOrigin(uri);
        for (ApplicationLink link : links)
        {
            if (!corsService.allowsCredentials(link))
            {
                return false;
            }
        }

        return true;
    }

    /**
     * Retrieves the set authentication headers allowed on a browser request by the {@link ApplicationLink}(s)
     * associated with the provided URL.
     * <p/>
     * This corresponds to {@code Access-Control-Allow-Headers}. The presence of authentication headers not contained
     * in the returned set will cause the CORS preflight check to fail.
     * <p/>
     * Note: Because CORS Origins only include scheme, host and port, it is possible that multiple links will match the
     * provided URL. When multiple links match, the returned set of headers is the union of the headers supported by
     * all of the matching links, since it is not possible to know in advance which specific link is in play.
     *
     * @param uri the URL of the allowed CORS origin
     * @return a set containing 0 or more allowed headers, based on the configuration of any matching links
     * @throws IllegalArgumentException Thrown if the URL provided is not an allowed origin
     */
    public Set<String> getAllowedRequestHeaders(String uri) throws IllegalArgumentException
    {
        //Note: Inbound authorization types are all always enabled, so if credentials are allowed in the first place
        //      the Authorization header is fair game for OAuth or Basic access. It's important to remember here that
        //      it is not a remote Application Link that is going to be making the CORS request; it is the browser.
        //      That means the configuration of the outbound authentication types for the remote side of the link may
        //      not ever come into play during the CORS exchange.
        return allowsCredentials(uri) ? ImmutableSet.of("Authorization") : Collections.<String>emptySet();
    }

    /**
     * Retrieves the set of authentication headers which the browser should expose to the executing script based on the
     * {@link ApplicationLink}(s) associated with the provided URL.
     * <p/>
     * This corresponds to {@code Access-Control-Expose-Headers}. Any authentication headers returned by the resource
     * requested which are not in the returned set will be filtered by the browser.
     * <p/>
     * Note: Because CORS Origins only include scheme, host and port, it is possible that multiple links will match the
     * provided URL. When multiple links match, the returned set of headers is the union of the headers supported by
     * all of the matching links, since it is not possible to know in advance which specific link is in play.
     *
     * @param uri the URL of the allowed CORS origin
     * @return an empty set
     * @throws IllegalArgumentException Thrown if the URL provided is not an allowed origin
     */
    public Set<String> getAllowedResponseHeaders(String uri) throws IllegalArgumentException
    {
        //This check currently only really validates that the origin has been allowed. I'm not aware of specific headers
        //that might be returned by the underlying resource that we want the browser to expose to the client.
        corsService.getRequiredApplicationLinksByOrigin(uri);

        return Collections.emptySet();
    }
}

