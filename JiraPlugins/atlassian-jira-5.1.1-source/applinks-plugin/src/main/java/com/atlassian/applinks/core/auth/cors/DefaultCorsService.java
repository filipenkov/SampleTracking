package com.atlassian.applinks.core.auth.cors;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.auth.types.CorsAuthenticationProvider;
import com.atlassian.applinks.core.auth.oauth.RequestUtil;
import com.atlassian.applinks.spi.auth.AuthenticationConfigurationManager;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @since 3.7
 */
public class DefaultCorsService implements CorsService
{
    public static final String KEY_ALLOWS_CREDENTIALS = "allowsCredentials";

    private final AuthenticationConfigurationManager configurationManager;
    private final ApplicationLinkService linkService;

    public DefaultCorsService(ApplicationLinkService applicationLinkService,
                              AuthenticationConfigurationManager authenticationConfigurationManager)
    {
        configurationManager = authenticationConfigurationManager;
        linkService = applicationLinkService;
    }

    public boolean allowsCredentials(ApplicationLink link)
    {
        Map<String, String> configuration = configurationManager.getConfiguration(link.getId(), CorsAuthenticationProvider.class);

        return (configuration != null && "true".equals(configuration.get(KEY_ALLOWS_CREDENTIALS)));
    }

    public void disableCredentials(ApplicationLink link)
    {
        configurationManager.unregisterProvider(link.getId(), CorsAuthenticationProvider.class);
    }

    public void enableCredentials(ApplicationLink link)
    {
        Map<String, String> configuration = ImmutableMap.of(KEY_ALLOWS_CREDENTIALS, "true");

        configurationManager.registerProvider(link.getId(), CorsAuthenticationProvider.class, configuration);
    }

    public Collection<ApplicationLink> getApplicationLinksByOrigin(String origin)
    {
        return getApplicationLinksByUri(URI.create(origin).normalize());
    }

    public Collection<ApplicationLink> getApplicationLinksByUri(URI uri)
    {
        List<ApplicationLink> matches = new ArrayList<ApplicationLink>();
        for (ApplicationLink link : linkService.getApplicationLinks())
        {
            if (matchesOrigin(uri, link.getRpcUrl()))
            {
                matches.add(link);
            }
        }
        return matches;
    }

    public Collection<ApplicationLink> getRequiredApplicationLinksByOrigin(String origin)
    {
        Collection<ApplicationLink> links = getApplicationLinksByOrigin(origin);
        if (CollectionUtils.isEmpty(links))
        {
            throw new IllegalArgumentException("Origin [" + origin + "] is required to match at least one ApplicationLink");
        }
        return links;
    }

    /**
     * Compares the origin URI to the link URI to determine if they match.
     * <p/>
     * Per the CORS spec, the origin URI only contains data in the form {@code scheme://host[:port]}. As a result, any
     * context information on the link URI is <i>ignored</i> by this comparison.
     *
     * @param origin the CORS origin
     * @param link   the application link
     * @return {@code true} if the origin's scheme, host and port match the link's
     */
    private boolean matchesOrigin(URI origin, URI link)
    {
        link = link.normalize();

        //Comparing host (highest likelihood for difference), port, scheme (lowest likelihood for difference)
        return StringUtils.equalsIgnoreCase(origin.getHost(), link.getHost()) &&
                normalizePort(origin) == normalizePort(link) &&
                StringUtils.equalsIgnoreCase(origin.getScheme(), link.getScheme());
    }

    /**
     * Normalizes the port on the provided URI. If no port was specified directly, the default port for the URI's
     * scheme is returned instead.
     *
     * @param uri the URI to normalize the port for
     * @return the normalized port, or -1 if no default port is available for the URI's scheme
     * @see com.atlassian.applinks.core.auth.oauth.RequestUtil#getDefaultPort(String)
     */
    private int normalizePort(URI uri)
    {
        int port = uri.getPort();
        if (port == -1)
        {
            port = RequestUtil.getDefaultPort(uri.getScheme());
        }
        return port;
    }
}
