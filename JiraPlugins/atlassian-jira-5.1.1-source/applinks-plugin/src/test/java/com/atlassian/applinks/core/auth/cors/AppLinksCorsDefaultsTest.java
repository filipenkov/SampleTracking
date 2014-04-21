package com.atlassian.applinks.core.auth.cors;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.plugins.rest.common.security.descriptor.CorsDefaults;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.when;

public class AppLinksCorsDefaultsTest
{
    private static final String ATLASSIAN_COM = "http://www.atlassian.com";
    private static final String ID = UUID.randomUUID().toString();
    private static final String LOCALHOST = "http://localhost:9090";
    private static final String UNI_EDU = "http://www.uni.edu:8080";

    @Test
    public void testAllowsCredentials()
    {
        ApplicationLink atlassian = mock(ApplicationLink.class);

        CorsService service = mock(CorsService.class);
        when(service.allowsCredentials(same(atlassian))).thenReturn(true, false);
        when(service.getRequiredApplicationLinksByOrigin(eq(ATLASSIAN_COM)))
                .thenReturn(Arrays.asList(atlassian));

        CorsDefaults defaults = new AppLinksCorsDefaults(service);
        assertTrue(defaults.allowsCredentials(ATLASSIAN_COM));
        assertFalse(defaults.allowsCredentials(ATLASSIAN_COM));
    }

    @Test
    public void testAllowsCredentialsWithMultipleLinks()
    {
        ApplicationLink confluence = mock(ApplicationLink.class);
        ApplicationLink jira = mock(ApplicationLink.class);
        
        CorsService service = mock(CorsService.class);
        when(service.allowsCredentials(same(confluence))).thenReturn(false, true, true);
        when(service.allowsCredentials(same(jira))).thenReturn(false, true);
        when(service.getRequiredApplicationLinksByOrigin(eq(LOCALHOST)))
                .thenReturn(Arrays.asList(confluence, jira));

        CorsDefaults defaults = new AppLinksCorsDefaults(service);
        assertFalse(defaults.allowsCredentials(LOCALHOST)); //Confluence says no, no check for JIRA
        assertFalse(defaults.allowsCredentials(LOCALHOST)); //Confluence says yes, JIRA says no
        assertTrue(defaults.allowsCredentials(LOCALHOST)); //Confluence and JIRA both say yes
    }

    @Test
    public void testAllowsOrigin()
    {
        ApplicationLink atlassian = mock(ApplicationLink.class);
        
        CorsService service = mock(CorsService.class);
        when(service.getApplicationLinksByOrigin(eq(ATLASSIAN_COM))).thenReturn(Arrays.asList(atlassian));
        when(service.getApplicationLinksByOrigin(eq(UNI_EDU))).thenReturn(Collections.<ApplicationLink>emptyList());

        CorsDefaults defaults = new AppLinksCorsDefaults(service);
        assertTrue(defaults.allowsOrigin(ATLASSIAN_COM)); //Implicit port
        assertFalse(defaults.allowsOrigin(UNI_EDU));
    }

    @Test
    public void testGetAllowedRequestHeaders()
    {
        ApplicationId atlassianId = new ApplicationId(ID);
        
        ApplicationLink atlassian = mock(ApplicationLink.class);
        when(atlassian.getId()).thenReturn(atlassianId);

        CorsService service = mock(CorsService.class);
        when(service.getRequiredApplicationLinksByOrigin(eq(ATLASSIAN_COM))).thenReturn(Arrays.asList(atlassian));
        when(service.allowsCredentials(same(atlassian))).thenReturn(Boolean.FALSE, Boolean.TRUE);

        CorsDefaults defaults = new AppLinksCorsDefaults(service);
        Set<String> headers = defaults.getAllowedRequestHeaders(ATLASSIAN_COM);
        assertNotNull(headers);
        assertTrue(headers.isEmpty());
        headers = defaults.getAllowedRequestHeaders(ATLASSIAN_COM);
        assertNotNull(headers);
        assertEquals(1, headers.size());
        assertEquals("Authorization", headers.iterator().next());
    }

    @Test
    public void testGetAllowedResponseHeaders()
    {
        ApplicationLink atlassian = mock(ApplicationLink.class);

        CorsService service = mock(CorsService.class);
        when(service.getRequiredApplicationLinksByOrigin(eq(ATLASSIAN_COM))).thenReturn(Arrays.asList(atlassian));

        CorsDefaults defaults = new AppLinksCorsDefaults(service);
        assertSame(Collections.emptySet(), defaults.getAllowedResponseHeaders(ATLASSIAN_COM));
    }
}
