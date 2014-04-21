package com.atlassian.applinks.core.auth.cors;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.auth.types.CorsAuthenticationProvider;
import com.atlassian.applinks.spi.auth.AuthenticationConfigurationManager;
import com.google.common.collect.ImmutableMap;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Test;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DefaultCorsServiceTest
{
    private static final Map<String, String> ALLOWED = ImmutableMap.of(DefaultCorsService.KEY_ALLOWS_CREDENTIALS, "true");
    private static final URI ATLASSIAN_COM = URI.create("http://www.atlassian.com");
    private static final URI CONFLUENCE = URI.create("http://localhost:9090/confluence");
    private static final Map<String, String> DISALLOWED = ImmutableMap.of(DefaultCorsService.KEY_ALLOWS_CREDENTIALS, "false");
    private static final URI EXAMPLE_ORG = URI.create("http://example.org");
    private static final String ID = UUID.randomUUID().toString();
    private static final URI JIRA = URI.create("http://localhost:9090/jira");
    private static final URI UNI_EDU = URI.create("http://www.uni.edu:8080");

    @Test
    @SuppressWarnings("unchecked")
    public void testAllowsCredentials()
    {
        ApplicationId atlassianId = new ApplicationId(ID);

        ApplicationLink atlassian = mock(ApplicationLink.class);
        when(atlassian.getId()).thenReturn(atlassianId);

        AuthenticationConfigurationManager manager = mock(AuthenticationConfigurationManager.class);
        when(manager.getConfiguration(same(atlassianId), same(CorsAuthenticationProvider.class)))
                .thenReturn(ALLOWED, DISALLOWED, null);

        CorsService corsService = new DefaultCorsService(null, manager);
        assertTrue(corsService.allowsCredentials(atlassian));
        assertFalse(corsService.allowsCredentials(atlassian));
        assertFalse(corsService.allowsCredentials(atlassian));
    }
    
    @Test
    public void testDisableCredentials()
    {
        ApplicationId atlassianId = new ApplicationId(ID);

        ApplicationLink atlassian = mock(ApplicationLink.class);
        when(atlassian.getId()).thenReturn(atlassianId);
        
        AuthenticationConfigurationManager manager = mock(AuthenticationConfigurationManager.class);
        
        CorsService corsService = new DefaultCorsService(null, manager);
        corsService.disableCredentials(atlassian);
        
        verify(manager).unregisterProvider(same(atlassianId), same(CorsAuthenticationProvider.class));
    }

    @Test
    public void testEnableCredentials()
    {
        ApplicationId atlassianId = new ApplicationId(ID);

        ApplicationLink atlassian = mock(ApplicationLink.class);
        when(atlassian.getId()).thenReturn(atlassianId);
        
        AuthenticationConfigurationManager manager = mock(AuthenticationConfigurationManager.class);
        
        CorsService corsService = new DefaultCorsService(null, manager);
        corsService.enableCredentials(atlassian);
        
        verify(manager).registerProvider(same(atlassianId), same(CorsAuthenticationProvider.class),
                argThat(new BaseMatcher<Map<String,String>>()
                {
                    public boolean matches(Object item)
                    {
                        Map<?, ?> configuration = (Map) item;
                        
                        return configuration != null &&
                                configuration.size() == 1 &&
                                "true".equals(configuration.get(DefaultCorsService.KEY_ALLOWS_CREDENTIALS));
                    }

                    public void describeTo(Description description)
                    {
                        description.appendText("matches({" + DefaultCorsService.KEY_ALLOWS_CREDENTIALS + "=true})");
                    }
                }));
    }

    @Test
    public void testGetApplicationLinksByOrigin()
    {
        ApplicationLink atlassian = mock(ApplicationLink.class);
        when(atlassian.getRpcUrl()).thenReturn(ATLASSIAN_COM);

        ApplicationLink example = mock(ApplicationLink.class);
        when(example.getRpcUrl()).thenReturn(EXAMPLE_ORG);

        ApplicationLink uni = mock(ApplicationLink.class);
        when(uni.getRpcUrl()).thenReturn(UNI_EDU);

        ApplicationLinkService service = mock(ApplicationLinkService.class);
        when(service.getApplicationLinks()).thenReturn(Arrays.asList(atlassian, example, uni));

        CorsService corsService = new DefaultCorsService(service, null);
        assertApplicationLinks(corsService.getApplicationLinksByOrigin("http://www.atlassian.com"), atlassian); //Implicit port
        assertApplicationLinks(corsService.getApplicationLinksByOrigin("http://www.atlassian.com:80"), atlassian); //Explicit default port
        assertApplicationLinks(corsService.getApplicationLinksByOrigin("http://www.atlassian.com:8080")); //Explicit mismatched port
        assertApplicationLinks(corsService.getApplicationLinksByOrigin("http://example.org/test"), example); //Context information is ignored
        assertApplicationLinks(corsService.getApplicationLinksByOrigin("http://www.uni.edu:8080"), uni); //Explicit port match
        assertApplicationLinks(corsService.getApplicationLinksByOrigin("http://www.uni.edu")); //Implicit port doesn't match
        assertApplicationLinks(corsService.getApplicationLinksByOrigin("http://somehost.xxx")); //Host is not found at all
    }

    @Test
    public void testGetApplicationLinksByUri()
    {
        ApplicationLink confluence = mock(ApplicationLink.class);
        when(confluence.getRpcUrl()).thenReturn(CONFLUENCE);
        
        ApplicationLink jira = mock(ApplicationLink.class);
        when(jira.getRpcUrl()).thenReturn(JIRA);

        ApplicationLinkService service = mock(ApplicationLinkService.class);
        when(service.getApplicationLinks()).thenReturn(Arrays.asList(confluence, jira));

        CorsService corsService = new DefaultCorsService(service, null);
        assertApplicationLinks(corsService.getApplicationLinksByUri(CONFLUENCE), confluence, jira);
        assertApplicationLinks(corsService.getApplicationLinksByUri(JIRA), confluence, jira);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetRequiredApplicationLinksByOrigin()
    {
        ApplicationLink atlassian = mock(ApplicationLink.class);
        when(atlassian.getRpcUrl()).thenReturn(ATLASSIAN_COM);

        ApplicationLinkService service = mock(ApplicationLinkService.class);
        when(service.getApplicationLinks()).thenReturn(Arrays.asList(atlassian));

        CorsService corsService = new DefaultCorsService(service, null);
        assertApplicationLinks(corsService.getRequiredApplicationLinksByOrigin(ATLASSIAN_COM.toString()), atlassian);
        corsService.getRequiredApplicationLinksByOrigin(EXAMPLE_ORG.toString());
    }

    private static void assertApplicationLinks(Collection<ApplicationLink> actual, ApplicationLink... expected)
    {
        assertNotNull(actual);
        if (expected == null || expected.length == 0)
        {
            assertTrue(actual.isEmpty());
        }
        else
        {
            assertArrayEquals(expected, actual.toArray());
        }
    }
}
