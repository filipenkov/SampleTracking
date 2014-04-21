package com.atlassian.security.auth.trustedapps.seraph.filter;

import com.atlassian.security.auth.trustedapps.filter.AuthenticationController;
import com.atlassian.seraph.auth.RoleMapper;
import com.atlassian.seraph.filter.BaseLoginFilter;
import com.mockobjects.dynamic.C;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.servlet.MockHttpServletRequest;
import junit.framework.TestCase;

import java.security.Principal;

/**
 * Tests for {@link SeraphAuthenticationController}
 */
public class TestSeraphAuthenticationController extends TestCase
{
    private AuthenticationController authenticationController;

    private Mock mockRoleMapper;

    protected void setUp() throws Exception
    {
        mockRoleMapper = new Mock(RoleMapper.class);
        authenticationController = new SeraphAuthenticationController((RoleMapper) mockRoleMapper.proxy());
    }

    protected void tearDown() throws Exception
    {
        mockRoleMapper = null;
        authenticationController = null;
    }

    public void testCreateSeraphAuthenticationControllerWithNullRoleMapperThrowsIllegalArgumentException()
    {
        try
        {
            new SeraphAuthenticationController(null);
            fail("Should throw exception");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
    }

    public void testShouldAttemptAuthenticationWithSeraphAuthenticationStatusAttributeNotPresent()
    {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addExpectedGetAttributeName(BaseLoginFilter.OS_AUTHSTATUS_KEY);
        request.setupGetAttribute(null);
        assertTrue(authenticationController.shouldAttemptAuthentication(request));
    }

    public void testShouldAttemptAuthenticationWithSeraphAuthenticationStatusAttributePresent()
    {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addExpectedGetAttributeName(BaseLoginFilter.OS_AUTHSTATUS_KEY);
        request.setupGetAttribute("some.value");
        assertFalse(authenticationController.shouldAttemptAuthentication(request));
    }

    public void testCanLoginWithRoleMapperDenyingLogin()
    {
        canLogin(false);
    }

    public void testCanLoginWithRoleMapperAllowingLogin()
    {
        canLogin(true);
    }

    private void canLogin(boolean roleMapperCanLogin)
    {
        final MockPrincipal principal = new MockPrincipal();
        final MockHttpServletRequest request = new MockHttpServletRequest();

        mockRoleMapper.matchAndReturn("canLogin", C.eq(principal, request), roleMapperCanLogin);
        assertEquals(roleMapperCanLogin, authenticationController.canLogin(principal, request));
    }

    private static class MockPrincipal implements Principal
    {
        public String getName()
        {
            return "principal";
        }
    }
}
