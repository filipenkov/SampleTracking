package com.atlassian.applinks.core.auth.basic;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkRequestFactory;
import com.atlassian.applinks.api.auth.AuthenticationProvider;
import com.atlassian.applinks.api.auth.types.BasicAuthenticationProvider;
import com.atlassian.applinks.core.auth.oauth.RequestUtil;
import com.atlassian.applinks.host.spi.HostApplication;
import com.atlassian.applinks.host.spi.InternalHostApplication;
import com.atlassian.applinks.spi.auth.AuthenticationConfigurationManager;
import com.atlassian.applinks.spi.auth.AuthenticationDirection;
import com.atlassian.applinks.spi.auth.AuthenticationProviderPluginModule;
import com.atlassian.sal.api.net.RequestFactory;
import org.osgi.framework.Version;

import javax.servlet.http.HttpServletRequest;

/**
 * @since 3.0
 */
public class BasicAuthenticationProviderPluginModule implements AuthenticationProviderPluginModule
{
    private static final String SERVLET_LOCATION = "/plugins/servlet/applinks/auth/conf/basic/";
    private final AuthenticationConfigurationManager authenticationConfigurationManager;
    private final HostApplication hostApplication;
    private final RequestFactory requestFactory;

    public BasicAuthenticationProviderPluginModule(final AuthenticationConfigurationManager authenticationConfigurationManager,
                                                   final InternalHostApplication hostApplication,
                                                   final RequestFactory requestFactory)
    {
        this.authenticationConfigurationManager = authenticationConfigurationManager;
        this.hostApplication = hostApplication;
        this.requestFactory = requestFactory;
    }

    public BasicAuthenticationProvider getAuthenticationProvider(final ApplicationLink link)
    {
        BasicAuthenticationProvider provider = null;
        if (authenticationConfigurationManager.isConfigured(link.getId(), BasicAuthenticationProvider.class))
        {
            provider = new BasicAuthenticationProvider()
            {
                public ApplicationLinkRequestFactory getRequestFactory()
                {
                    return new BasicAuthRequestFactoryImpl(authenticationConfigurationManager, link, requestFactory);
                }
            };
        }
        return provider;
    }

    public String getConfigUrl(final ApplicationLink link, final Version applicationLinksVersion, final AuthenticationDirection direction, final HttpServletRequest request)
    {
        final String baseUrl;
        if (direction == AuthenticationDirection.INBOUND)
        {
            // return a link to the basic servlet on the remote host:
            if (link == null || applicationLinksVersion == null)
            {
                return null;
            }
            else
            {
                baseUrl = link.getDisplayUrl() + SERVLET_LOCATION + hostApplication.getId();
            }
        }
        else
        {
            // return a link to the local basic servlet on this host:
            baseUrl = RequestUtil.getBaseURLFromRequest(request, hostApplication.getBaseUrl()) + SERVLET_LOCATION + link.getId().toString();
        }
        return baseUrl;
    }

    public Class<? extends AuthenticationProvider> getAuthenticationProviderClass()
    {
        return BasicAuthenticationProvider.class;
    }
}