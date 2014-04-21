package com.atlassian.applinks.core.auth.cors;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.auth.types.CorsAuthenticationProvider;
import com.atlassian.applinks.host.spi.HostApplication;
import com.atlassian.applinks.spi.auth.AuthenticationDirection;
import com.atlassian.applinks.spi.auth.AuthenticationProviderPluginModule;
import org.junit.Test;
import org.osgi.framework.Version;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CorsAuthenticationProviderPluginModuleTest
{
    private static final String ID = UUID.randomUUID().toString();

    @Test
    public void testGetAuthenticationProvider() 
    {
        assertNull(new CorsAuthenticationProviderPluginModule(null).getAuthenticationProvider(null));
    }

    @Test
    public void testGetConfigUrlForInbound()
    {
        ApplicationId id = new ApplicationId(ID);

        ApplicationLink link = mock(ApplicationLink.class);
        when(link.getId()).thenReturn(id);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getContextPath()).thenReturn("/refapp");
        when(request.getScheme()).thenReturn("http");
        when(request.getServerName()).thenReturn("localhost");
        when(request.getServerPort()).thenReturn(5990);

        HostApplication application = mock(HostApplication.class);
        when(application.getBaseUrl()).thenReturn(URI.create("http://localhost:5990/refapp"));
        
        AuthenticationProviderPluginModule module = new CorsAuthenticationProviderPluginModule(application);
        assertEquals("http://localhost:5990/refapp" + CorsAuthenticationProviderPluginModule.SERVLET_LOCATION + ID,
                module.getConfigUrl(link, null, AuthenticationDirection.INBOUND, request));
    }

    @Test
    public void testGetConfigUrlForOutbound()
    {
        HostApplication application = mock(HostApplication.class);
        when(application.getId()).thenReturn(new ApplicationId(ID));

        ApplicationLink link = mock(ApplicationLink.class);
        when(link.getDisplayUrl()).thenReturn(URI.create("http://localhost:5992/refapp"));

        AuthenticationProviderPluginModule module = new CorsAuthenticationProviderPluginModule(application);
        assertEquals("http://localhost:5992/refapp" + CorsAuthenticationProviderPluginModule.SERVLET_LOCATION + ID,
                module.getConfigUrl(link, new Version(3, 7, 0), AuthenticationDirection.OUTBOUND, null));
    }

    @Test
    public void testGetAuthenticationProviderClass() 
    {
        assertSame(CorsAuthenticationProvider.class,
                new CorsAuthenticationProviderPluginModule(null).getAuthenticationProviderClass());
    }
}
