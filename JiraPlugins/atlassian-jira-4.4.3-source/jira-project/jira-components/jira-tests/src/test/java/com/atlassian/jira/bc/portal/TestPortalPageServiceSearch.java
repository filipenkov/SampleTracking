package com.atlassian.jira.bc.portal;

import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.user.MockCrowdService;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.MockProviderAccessor;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.MockJiraServiceContext;
import com.atlassian.jira.portal.PortalPage;
import com.atlassian.jira.portal.PortalPageManager;
import com.atlassian.jira.sharing.ShareTypeValidatorUtils;
import com.atlassian.jira.sharing.search.PrivateShareTypeSearchParameter;
import com.atlassian.jira.sharing.search.SharedEntitySearchParameters;
import com.atlassian.jira.sharing.search.SharedEntitySearchParametersBuilder;
import com.atlassian.jira.sharing.search.SharedEntitySearchResult;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.collect.MockCloseableIterable;
import com.opensymphony.user.User;

import java.util.Collections;

/**
 * Test the searching methods on the {@link DefaultPortalPageService}.
 * 
 * @since v3.13
 */
public class TestPortalPageServiceSearch extends MockControllerTestCase
{
    private static final String USERNAME_FRED = "fred";

    private PortalPageManager portalPageManager;
    private User user;
    private UserUtil userUtil;
    private ShareTypeValidatorUtils shareTypeValidatorUtils;

    @Before
    public void setUp() throws Exception
    {

        portalPageManager = mockController.getMock(PortalPageManager.class);
        shareTypeValidatorUtils = mockController.getMock(ShareTypeValidatorUtils.class);
        userUtil = mockController.getMock(UserUtil.class);

        final MockProviderAccessor mpa = new MockProviderAccessor();
        user = new User("admin", mpa, new MockCrowdService());
    }

    /**
     * Make sure a simple search gets passed through to the manager.
     */
    @Test
    public void testSearchParameters()
    {
        final SharedEntitySearchParametersBuilder parameters = new SharedEntitySearchParametersBuilder();
        final SharedEntitySearchParameters sharedEntitySearchParameters = parameters.toSearchParameters();

        final SharedEntitySearchResult<PortalPage> expectedResult = new SharedEntitySearchResult<PortalPage>(new MockCloseableIterable<PortalPage>(Collections.<PortalPage>emptyList()), true, 100);

        portalPageManager.search(sharedEntitySearchParameters, user, 0, 10);
        mockController.setReturnValue(expectedResult);

        final JiraServiceContext ctx = createContext();
        final PortalPageService service = createPortalPageService();
        final SharedEntitySearchResult actualResult = service.search(ctx, sharedEntitySearchParameters, 0, 10);

        assertSame(expectedResult, actualResult);

        mockController.verify();
    }

    /**
     * Make sure an anonymous search gets passed through to the manager.
     */
    @Test
    public void testSearchNullUser()
    {
        final SharedEntitySearchParametersBuilder parameters = new SharedEntitySearchParametersBuilder();
        final SharedEntitySearchParameters sharedEntitySearchParameters = parameters.toSearchParameters();

        final SharedEntitySearchResult<PortalPage> expectedResult = new SharedEntitySearchResult<PortalPage>(new MockCloseableIterable<PortalPage>(Collections.<PortalPage>emptyList()), true, 0);

        portalPageManager.search(sharedEntitySearchParameters, null, 0, 10);
        mockController.setReturnValue(expectedResult);

        final JiraServiceContext ctx = createContext(null);
        final PortalPageService service = createPortalPageService();
        final SharedEntitySearchResult actualResult = service.search(ctx, sharedEntitySearchParameters, 0, 10);

        assertSame(expectedResult, actualResult);

        mockController.verify();
    }

    /**
     * Null parameters should result in error.
     */
    @Test
    public void testSearchParametersNullParameters()
    {
        final PortalPageService service = createPortalPageService();

        try
        {
            service.search(createContext(), null, 0, 10);
            fail("Should not accept null parameters.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }

        mockController.verify();
    }

    /**
     * Invalid pageOffset should result in an error.
     */
    @Test
    public void testSearchInvalidPosition()
    {
        final PortalPageService service = createPortalPageService();

        try
        {
            service.search(createContext(), new SharedEntitySearchParametersBuilder().toSearchParameters(), -1, 10);
            fail("Should not accept invalid position.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }

        mockController.verify();
    }

    /**
     * Width of zero should result in error.
     */
    @Test
    public void testSearchZeroWidth()
    {
        final PortalPageService service = createPortalPageService();

        try
        {
            service.search(createContext(), new SharedEntitySearchParametersBuilder().toSearchParameters(), 0, 0);
            fail("Should not accept zero width.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }

        mockController.verify();
    }

    /**
     * Negative width should result in an error.
     */
    @Test
    public void testSearchInvalidWidth()
    {
        final PortalPageService service = createPortalPageService();

        try
        {
            service.search(createContext(), new SharedEntitySearchParametersBuilder().toSearchParameters(), 0, -1);
            fail("Should not accept invalid width.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }

        mockController.verify();
    }

    /**
     * Null service context should result in an error.
     */
    @Test
    public void testSearchNullCtx()
    {
        final PortalPageService service = createPortalPageService();

        try
        {
            service.search(null, new SharedEntitySearchParametersBuilder().toSearchParameters(), 0, 10);
            fail("Should not accept null context.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }

        mockController.verify();
    }

    /**
     * Should have error on invalid user.
     */
    @Test
    public void testValidateSearchAnyShareTypeInvalidUser()
    {
        userUtil.userExists(USERNAME_FRED);
        mockController.setReturnValue(false);

        final JiraServiceContext ctx = createContext(null);
        final PortalPageService service = createPortalPageService();

        final SharedEntitySearchParametersBuilder searchTemplate = new SharedEntitySearchParametersBuilder();
        searchTemplate.setUserName(USERNAME_FRED);

        service.validateForSearch(ctx, searchTemplate.toSearchParameters());
        assertTrue(ctx.getErrorCollection().hasAnyErrors());
        assertEquals(ctx.getErrorCollection().getErrors().get("searchOwnerUserName"), "The user 'fred' does not exist.");

        mockController.verify();
    }

    @Test
    public void testValidateSearchAnyShareTypeValidUser()
    {
        userUtil.userExists(USERNAME_FRED);
        mockController.setReturnValue(true);

        final JiraServiceContext ctx = createContext(null);
        final PortalPageService service = createPortalPageService();

        final SharedEntitySearchParametersBuilder searchTemplate = new SharedEntitySearchParametersBuilder();
        searchTemplate.setUserName(USERNAME_FRED);

        service.validateForSearch(ctx, searchTemplate.toSearchParameters());
        assertFalse(ctx.getErrorCollection().hasAnyErrors());

        mockController.verify();
    }

    /**
     * Make the shares passed are validated.
     */
    @Test
    public void testValidateSearchCallsThroughToValidators()
    {
        final JiraServiceContext ctx = createContext(null);

        shareTypeValidatorUtils.isValidSearchParameter(ctx, PrivateShareTypeSearchParameter.PRIVATE_PARAMETER);
        mockController.setReturnValue(true);

        final SharedEntitySearchParametersBuilder searchTemplate = new SharedEntitySearchParametersBuilder();
        searchTemplate.setShareTypeParameter(PrivateShareTypeSearchParameter.PRIVATE_PARAMETER);

        final PortalPageService service = createPortalPageService();
        service.validateForSearch(ctx, searchTemplate.toSearchParameters());

        mockController.verify();
    }

    private JiraServiceContext createContext()
    {
        return createContext(user);
    }

    private JiraServiceContext createContext(final User user)
    {
        return new MockJiraServiceContext(user);
    }

    private PortalPageService createPortalPageService()
    {
        return mockController.instantiate(DefaultPortalPageService.class);
    }
}
