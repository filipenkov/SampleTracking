package com.atlassian.jira.plugin.ext.bamboo.service;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkRequestFactory;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.jira.plugin.ext.bamboo.model.PlanKeys;
import com.atlassian.jira.plugin.ext.bamboo.model.PlanResultKey;
import com.atlassian.jira.plugin.ext.bamboo.model.RestResult;
import junit.framework.TestCase;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BambooRestServiceImplTest extends TestCase
{

    public void testResponseBodyCapturedOnError() throws Exception
    {
        final String responseBody = "Custom Response Body";

        BambooRestService service = new BambooRestServiceImpl()
        {
            @NotNull
            @Override
            public BambooRestResponse executePostRequest(@NotNull ApplicationLinkRequestFactory authenticatedRequestFactory, @NotNull String url, @NotNull Map<String, String> params, int timeout) throws CredentialsRequiredException
            {
                return new BambooRestResponse(400, "BAD", responseBody, new ArrayList<String>());
            }
        };

        ApplicationLinkRequestFactory requestFactory = mock(ApplicationLinkRequestFactory.class);
        ApplicationLink link = mock(ApplicationLink.class);
        when(link.createAuthenticatedRequestFactory()).thenReturn(requestFactory);
        RestResult<PlanResultKey> restResult = service.triggerPlan(link, PlanKeys.getPlanKey("BAM-MAIN"), null, new HashMap<String, String>());
        List<String> errors = restResult.getErrors();
        assertNull(restResult.getResult());
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains(responseBody));
    }

    public void testStatusMessageReturnedIfNoBody() throws Exception
    {
        BambooRestService service = new BambooRestServiceImpl()
        {
            @NotNull
            @Override
            public BambooRestResponse executePostRequest(@NotNull ApplicationLinkRequestFactory authenticatedRequestFactory, @NotNull String url, @NotNull Map<String, String> params, int timeout) throws CredentialsRequiredException
            {
                return new BambooRestResponse(400, "BAD ERROR", "", new ArrayList<String>());
            }
        };

        ApplicationLinkRequestFactory requestFactory = mock(ApplicationLinkRequestFactory.class);
        ApplicationLink link = mock(ApplicationLink.class);
        when(link.createAuthenticatedRequestFactory()).thenReturn(requestFactory);
        RestResult<PlanResultKey> restResult = service.triggerPlan(link, PlanKeys.getPlanKey("BAM-MAIN"), null, new HashMap<String, String>());
        List<String> errors = restResult.getErrors();
        assertNull(restResult.getResult());
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("BAD ERROR"));
    }

    public void testCredentialsRequiredMessagePropagated()
    {
        final ApplicationLinkRequestFactory requestFactory = mock(ApplicationLinkRequestFactory.class);
        final ApplicationLink link = mock(ApplicationLink.class);
        when(link.createAuthenticatedRequestFactory()).thenReturn(requestFactory);

        BambooRestService service = new BambooRestServiceImpl()
        {
            @NotNull
            @Override
            public BambooRestResponse executePostRequest(@NotNull ApplicationLinkRequestFactory authenticatedRequestFactory, @NotNull String url, @NotNull Map<String, String> params, int timeout) throws CredentialsRequiredException
            {
                throw new CredentialsRequiredException(requestFactory, "NEED TO LOG IN");
            }
        };

        try
        {
            service.triggerPlan(link, PlanKeys.getPlanKey("BAM-MAIN"), null, new HashMap<String, String>());
            fail("Credentials Required Exception Should have been thrown");
        }
        catch (CredentialsRequiredException e)
        {
            // this is good
        }
    }
}
