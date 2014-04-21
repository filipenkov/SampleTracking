package com.atlassian.applinks.core.auth;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkRequestFactory;
import com.atlassian.applinks.api.auth.Anonymous;
import com.atlassian.applinks.api.auth.AuthenticationProvider;
import com.atlassian.applinks.api.auth.ImpersonatingAuthenticationProvider;
import com.atlassian.applinks.api.auth.NonImpersonatingAuthenticationProvider;
import com.atlassian.sal.api.net.Request;
import com.atlassian.sal.api.net.RequestFactory;
import com.atlassian.sal.api.user.UserManager;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;

import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ApplicationLinkRequestFactoryFactoryImplTest
{
    private static final String USERNAME = "username";

    RequestFactory delegateFactory;
    UserManager userManager;

    AuthenticatorAccessor authenticatorAccessor;
    ApplicationLink link;

    ImpersonatingAuthenticationProvider impersonatingAuthenticationProvider;
    ApplicationLinkRequestFactory impersonatingRequestFactory;

    NonImpersonatingAuthenticationProvider nonImpersonatingAuthenticationProvider;
    ApplicationLinkRequestFactory nonImpersonatingRequestFactory;

    ApplicationLinkRequestFactoryFactoryImpl factoryFactory;

    @Before
    public void setUp()
    {
        delegateFactory = mock(RequestFactory.class);
        userManager = mock(UserManager.class);
        authenticatorAccessor = mock(AuthenticatorAccessor.class);

        impersonatingAuthenticationProvider = mock(ImpersonatingAuthenticationProvider.class);
        impersonatingRequestFactory = mock(ApplicationLinkRequestFactory.class);
        when(impersonatingAuthenticationProvider.getRequestFactory(USERNAME)).thenReturn(impersonatingRequestFactory);
        when(impersonatingAuthenticationProvider.getRequestFactory(null)).thenReturn(impersonatingRequestFactory);

        nonImpersonatingAuthenticationProvider = mock(NonImpersonatingAuthenticationProvider.class);
        nonImpersonatingRequestFactory = mock(ApplicationLinkRequestFactory.class);
        when(nonImpersonatingAuthenticationProvider.getRequestFactory()).thenReturn(nonImpersonatingRequestFactory);

        // over-ride toString() to make error messages a bit nicer
        when(delegateFactory.toString()).thenReturn("Anonymous");
        when(impersonatingRequestFactory.toString()).thenReturn("Impersonating");
        when(nonImpersonatingRequestFactory.toString()).thenReturn("Non-Impersonating");

        link = mock(ApplicationLink.class);

        factoryFactory = new ApplicationLinkRequestFactoryFactoryImpl(delegateFactory, userManager, authenticatorAccessor);
    }

    @Test
    public void testOverridingAnonymous() throws Exception
    {
        assertNonWrappedFactoryReturnedForGetAnonymousRequestFactory();
    }

    @Test
    public void testOverridingImpersonatingAuthenticator() throws Exception
    {
        when(authenticatorAccessor.getAuthenticationProvider(link, ImpersonatingAuthenticationProvider.class))
                .thenReturn(impersonatingAuthenticationProvider);

        assertWrappedFactoryReturnedForSpecifiedClass(impersonatingRequestFactory, ImpersonatingAuthenticationProvider.class);
    }

    @Test
    public void testOverridingNonImpersonatingAuthenticator() throws Exception
    {
        when(authenticatorAccessor.getAuthenticationProvider(link, NonImpersonatingAuthenticationProvider.class))
                .thenReturn(nonImpersonatingAuthenticationProvider);

        assertWrappedFactoryReturnedForSpecifiedClass(nonImpersonatingRequestFactory, NonImpersonatingAuthenticationProvider.class);
    }

    @Test
    public void testOverridingUnconfiguredProvider() throws Exception
    {
        when(authenticatorAccessor.getAuthenticationProvider(link, NonImpersonatingAuthenticationProvider.class))
                .thenReturn(null);
        when(authenticatorAccessor.getAuthenticationProvider(link, ImpersonatingAuthenticationProvider.class))
                .thenReturn(impersonatingAuthenticationProvider);

        assertNull(factoryFactory.getApplicationLinkRequestFactory(link, NonImpersonatingAuthenticationProvider.class));
    }

    @Test
    public void testDefaultToImpersonatingAuthenticatorWhenLoggedInUser() throws Exception
    {
        when(authenticatorAccessor.getAuthenticationProvider(link, NonImpersonatingAuthenticationProvider.class))
                .thenReturn(nonImpersonatingAuthenticationProvider); //shouldn't be called
        when(authenticatorAccessor.getAuthenticationProvider(link, ImpersonatingAuthenticationProvider.class))
                .thenReturn(impersonatingAuthenticationProvider);
        when(userManager.getRemoteUsername()).thenReturn(USERNAME);

        assertWrappedFactoryReturned(impersonatingRequestFactory);
    }

    @Test
    public void testDefaultToNonImpersonatingAuthenticatorWhenNoLoggedInUser() throws Exception
    {
        when(authenticatorAccessor.getAuthenticationProvider(link, NonImpersonatingAuthenticationProvider.class))
                .thenReturn(nonImpersonatingAuthenticationProvider);
        when(authenticatorAccessor.getAuthenticationProvider(link, ImpersonatingAuthenticationProvider.class))
                .thenReturn(impersonatingAuthenticationProvider); //shouldn't be called
        when(userManager.getRemoteUsername()).thenReturn(null);

        assertWrappedFactoryReturned(nonImpersonatingRequestFactory);
    }

    @Test
    public void testDefaultToNonImpersonatingAuthenticatorWhenNoImpersonatingRequestAuthenticatorConfigured() throws Exception
    {
        when(authenticatorAccessor.getAuthenticationProvider(link, NonImpersonatingAuthenticationProvider.class))
                .thenReturn(nonImpersonatingAuthenticationProvider);
        when(authenticatorAccessor.getAuthenticationProvider(link, ImpersonatingAuthenticationProvider.class))
                .thenReturn(null);
        when(userManager.getRemoteUsername()).thenReturn(USERNAME);

        assertWrappedFactoryReturned(nonImpersonatingRequestFactory);

        when(userManager.getRemoteUsername()).thenReturn(null); // behaviour here should be consistent regardless of context user

        assertWrappedFactoryReturned(nonImpersonatingRequestFactory);
    }

    @Test
    public void testDefaultToAnonymousWhenNoLoggedInUserAndNoRequestAuthenticatorConfigured() throws Exception
    {
        when(authenticatorAccessor.getAuthenticationProvider(link, NonImpersonatingAuthenticationProvider.class))
                .thenReturn(null);
        when(authenticatorAccessor.getAuthenticationProvider(link, ImpersonatingAuthenticationProvider.class))
                .thenReturn(impersonatingAuthenticationProvider);
        when(userManager.getRemoteUsername()).thenReturn(null);

        assertNonWrappedFactoryReturned();
    }

    @Test
    public void testDefaultToAnonymousWhenNoAuthenticatorsConfigured() throws Exception
    {
        when(authenticatorAccessor.getAuthenticationProvider(link, NonImpersonatingAuthenticationProvider.class))
                .thenReturn(null);
        when(authenticatorAccessor.getAuthenticationProvider(link, ImpersonatingAuthenticationProvider.class))
                .thenReturn(null);
        when(userManager.getRemoteUsername()).thenReturn(USERNAME);

        assertNonWrappedFactoryReturnedForGetAnonymousRequestFactory();

        when(userManager.getRemoteUsername()).thenReturn(null); // behaviour here should be consistent regardless of context user

        assertNonWrappedFactoryReturned();
    }

    @Test
    public void testUnsupportedAuthenticationProvider()
    {
        when(authenticatorAccessor.getAuthenticationProvider(link, AuthenticationProvider.class)).thenReturn(new AuthenticationProvider(){});

        try
        {
            factoryFactory.getApplicationLinkRequestFactory(link, AuthenticationProvider.class);
            fail("Should have thrown an " + IllegalArgumentException.class.getSimpleName());
        }
        catch (IllegalArgumentException e)
        {
            //expected
        }
    }

    @Test
    public void testAbsoluteUris() throws Exception
    {
        assertUri("http://absolute.net", "http://absolute.net");
        assertUri("http://localhost:5990/refapp", "http://localhost:5990/refapp");
    }

    @Test
    public void testRelativeUri() throws Exception
    {
        final URI rpcUrl = new URI("http://localhost:5990/refapp");
        when(link.getRpcUrl()).thenReturn(rpcUrl);
        assertUri(rpcUrl.toString() + "/rest/applinks/manifest", "/rest/applinks/manifest");
        assertUri(rpcUrl.toString() + "/rest/applinks/manifest", "rest/applinks/manifest"); // no leading slash
    }

    private void assertUri(final String expected, final String uri) throws Exception
    {
        final ApplicationLinkRequestFactory delegateFactory = mock(ApplicationLinkRequestFactory.class);
        new ApplicationLinkRequestFactoryFactoryImpl.AbsoluteURLRequestFactory(delegateFactory, link).createRequest(Request.MethodType.GET, uri);
        verify(delegateFactory).createRequest(Request.MethodType.GET, expected);
    }

    private void assertNonWrappedFactoryReturnedForGetAnonymousRequestFactory()
    {
        assertEquals(ApplicationLinkRequestFactoryFactoryImpl.SalRequestFactoryAdapter.class,
                ((ApplicationLinkRequestFactoryFactoryImpl.AbsoluteURLRequestFactory) factoryFactory
                .getApplicationLinkRequestFactory(link, Anonymous.class)).requestFactory.getClass());
    }

    private void assertNonWrappedFactoryReturned()
    {
        assertEquals(ApplicationLinkRequestFactoryFactoryImpl.SalRequestFactoryAdapter.class,
                ((ApplicationLinkRequestFactoryFactoryImpl.AbsoluteURLRequestFactory) factoryFactory
                .getApplicationLinkRequestFactory(link)).requestFactory.getClass());
    }

    private void assertWrappedFactoryReturned(final ApplicationLinkRequestFactory factory)
    {
        assertEquals(factory, ((ApplicationLinkRequestFactoryFactoryImpl.AbsoluteURLRequestFactory) factoryFactory.getApplicationLinkRequestFactory(link)).requestFactory);
    }

    private void assertWrappedFactoryReturnedForSpecifiedClass(final ApplicationLinkRequestFactory factory, final Class<? extends AuthenticationProvider> clazz)
    {
        assertEquals(factory, ((ApplicationLinkRequestFactoryFactoryImpl.AbsoluteURLRequestFactory) factoryFactory.getApplicationLinkRequestFactory(link, clazz)).requestFactory);
    }


}
