package com.atlassian.applinks.core.auth.cors;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.auth.AuthenticationProvider;
import com.atlassian.applinks.api.auth.types.CorsAuthenticationProvider;
import com.atlassian.applinks.core.auth.oauth.RequestUtil;
import com.atlassian.applinks.host.spi.HostApplication;
import com.atlassian.applinks.spi.auth.AuthenticationDirection;
import com.atlassian.applinks.spi.auth.AuthenticationProviderPluginModule;
import org.osgi.framework.Version;

import javax.servlet.http.HttpServletRequest;

/**
 * @since 3.7
 */
public class CorsAuthenticationProviderPluginModule implements AuthenticationProviderPluginModule
{
    public static final String SERVLET_LOCATION = "/plugins/servlet/applinks/auth/conf/cors/";
    
    private final HostApplication hostApplication;

    public CorsAuthenticationProviderPluginModule(HostApplication hostApplication)
    {
        this.hostApplication = hostApplication;
    }

    /**
     * Currently, support for creating outbound CORS requests using this module is not implemented.
     * <p/>
     * Note: Because CORS is a browser-based technology, an implementation for this is not likely to be necessary.
     * Applications will not directly be making requests to each other; the browser will be making requests from web
     * pages returned by one application which access other applications.
     * 
     * @param link ignored
     * @return always {@code null}
     */
    public AuthenticationProvider getAuthenticationProvider(ApplicationLink link)
    {
        return null;
    }

    /**
     * @return {@link CorsAuthenticationProvider}
     */
    public Class<? extends AuthenticationProvider> getAuthenticationProviderClass()
    {
        return CorsAuthenticationProvider.class;
    }

    /**
     * For {@link AuthenticationDirection#INBOUND inbound} configuration, returns the URL to access the
     * {@link CorsAuthServlet} on the local application. Fpr {@link AuthenticationDirection#OUTBOUND outbound}
     * configuration, returns the URL to access the {@link CorsAuthServlet} on the remote application if the
     * remote application supports the required version of AppLinks.
     *
     * @param link      the Application Link to retrieve the configuration URL for
     * @param version   the version of AppLinks supported by the remote system
     * @param direction the authentication direction (inbound or outbound)
     * @param request   the incoming request
     * @return the local configuration servlet URL for inbound; the remote configuration servlet URL for outbound if
     *         the remote application supports AppLinks 3.7 or higher; or {@code null}
     */
    public String getConfigUrl(ApplicationLink link, Version version,
                               AuthenticationDirection direction, HttpServletRequest request)
    {
        String url = null;
        if (AuthenticationDirection.INBOUND == direction)
        {
            url = RequestUtil.getBaseURLFromRequest(request, hostApplication.getBaseUrl()) + SERVLET_LOCATION + link.getId();
        }
        else if (isCorsSupportedOn(version))
        {
            url = link.getDisplayUrl() + SERVLET_LOCATION + hostApplication.getId();
        }
        return url;
    }

    private boolean isCorsSupportedOn(Version version)
    {
        boolean supported = false;
        if (version != null)
        {
            int check = (version.getMajor() * 10) + version.getMinor();

            //CORS support was introduced in AppLinks 3.7.0
            supported = check >= 37;
        }
        return supported;
    }
}
