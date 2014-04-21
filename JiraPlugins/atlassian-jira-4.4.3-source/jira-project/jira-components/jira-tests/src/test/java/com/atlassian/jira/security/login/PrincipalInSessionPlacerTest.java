package com.atlassian.jira.security.login;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.CoreFeatures;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import mock.servlet.MockHttpServletRequest;
import mock.servlet.MockHttpSession;
import mock.servlet.MockPrincipal;
import org.junit.Before;
import org.junit.Test;

import java.security.Principal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

/**
 */
public class PrincipalInSessionPlacerTest extends MockControllerTestCase
{

    private MockHttpServletRequest httpServletRequest;
    private Principal principalSkinner;
    private MockHttpSession httpSession;
    private FeatureManager featureManager;

    @Before
    public void setUp() throws Exception
    {
        httpSession = new MockHttpSession();
        httpServletRequest = new MockHttpServletRequest(httpSession);
        principalSkinner = new MockPrincipal("skinner");

        featureManager = getMock(FeatureManager.class);

        ComponentAccessor.initialiseWorker(new MockComponentWorker()
                .addMock(FeatureManager.class, featureManager));
    }

    @Test
    public void testStrategyWhenNotStudio() throws Exception
    {
        assertStrategy(false);
    }

    @Test
    public void testStrategyWhenStudio() throws Exception
    {
        assertStrategy(true);
    }

    private void assertStrategy(final boolean isStudio)
    {
        expect(featureManager.isEnabled(CoreFeatures.ON_DEMAND)).andReturn(isStudio);

        PrincipalInSessionPlacer inSessionPlacer = instantiate(PrincipalInSessionPlacer.class);
        inSessionPlacer.putPrincipalInSessionContext(httpServletRequest, principalSkinner);

        final Principal actualPrincipal = (Principal) httpSession.getAttribute(DefaultAuthenticator.LOGGED_IN_KEY);
        if (isStudio)
        {
            assertNotSame(principalSkinner, actualPrincipal);
        }
        else
        {

            assertSame(principalSkinner, actualPrincipal);
        }
        assertEquals(actualPrincipal.getName(), principalSkinner.getName());
    }
}