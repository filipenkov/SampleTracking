package com.atlassian.applinks.core.auth;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.auth.AuthenticationProvider;
import com.atlassian.applinks.api.auth.types.OAuthAuthenticationProvider;
import com.atlassian.applinks.core.plugin.AuthenticationProviderModuleDescriptor;
import com.atlassian.applinks.spi.auth.AuthenticationConfigurationException;
import com.atlassian.applinks.spi.auth.AuthenticationScenario;
import com.atlassian.applinks.spi.auth.AutoConfiguringAuthenticatorProviderPluginModule;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.sal.testresources.net.MockRequestFactory;
import com.google.common.collect.Lists;
import junit.framework.TestCase;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * @since v3.0
 */
public class AuthenticationConfiguratorTest extends TestCase
{
    private final ApplicationLink link1 = mock(ApplicationLink.class);
    private final MockRequestFactory requestFactory = new MockRequestFactory();

    private PluginAccessor accessor;
    private AutoConfiguringAuthenticatorProviderPluginModule trusted;
    private AutoConfiguringAuthenticatorProviderPluginModule oauth;

    @Override
    protected void setUp() throws Exception
    {
        when(link1.getId()).thenReturn(new ApplicationId("11111111-1111-1111-1111-111111111111"));

        // simulates trusted apps:
        trusted = mock(AutoConfiguringAuthenticatorProviderPluginModule.class);
        when(trusted.isApplicable(Matchers.<AuthenticationScenario>anyObject(), Matchers.<ApplicationLink>anyObject()))
                .thenAnswer(new Answer()
                {
                    public Object answer(InvocationOnMock invocationOnMock) throws Throwable
                    {
                        final AuthenticationScenario scenario = (AuthenticationScenario) invocationOnMock.getArguments()[0];
                        return scenario.isCommonUserBase() && scenario.isTrusted();
                    }
                });

        AuthenticationProviderModuleDescriptor taDescriptor = mock(AuthenticationProviderModuleDescriptor.class);
        when(taDescriptor.getWeight()).thenReturn(1);
        when(taDescriptor.getModule()).thenReturn(trusted);

        // simulates oauth apps:
        oauth = mock(AutoConfiguringAuthenticatorProviderPluginModule.class);
        when(oauth.isApplicable(Matchers.<AuthenticationScenario>anyObject(), Matchers.<ApplicationLink>anyObject()))
                .thenAnswer(new Answer()
                {
                    public Object answer(InvocationOnMock invocationOnMock) throws Throwable
                    {
                        final AuthenticationScenario scenario = (AuthenticationScenario) invocationOnMock.getArguments()[0];
                        return scenario.isTrusted();
                    }
                });

        AuthenticationProviderModuleDescriptor oauthDescriptor = mock(AuthenticationProviderModuleDescriptor.class);
        when(oauthDescriptor.getWeight()).thenReturn(2);
        when(oauthDescriptor.getModule()).thenReturn(oauth);

        accessor = mock(PluginAccessor.class);
        when(accessor.getEnabledModuleDescriptorsByClass(AuthenticationProviderModuleDescriptor.class))
                .thenReturn(Lists.newArrayList(oauthDescriptor, taDescriptor));
    }

    public void testConfigureAuthenticationForApplicationLink() throws Exception
    {
        final AuthenticationConfigurator configurator = new AuthenticationConfigurator(accessor);
        final boolean configured = configurator.configureAuthenticationForApplicationLink(
                link1, new MockAuthenticationScenario(true, true), requestFactory);

        verify(trusted, atMost(1)).isApplicable(Matchers.<AuthenticationScenario>anyObject(), eq(link1));
        verify(oauth, atMost(1)).isApplicable(Matchers.<AuthenticationScenario>anyObject(), eq(link1));
        verify(trusted, times(1)).enable(eq(requestFactory), eq(link1));
        verify(oauth, never()).enable(eq(requestFactory), eq(link1));
        assertTrue(configured);
    }

    public void testNonSyncUserBases() throws Exception
    {
        final AuthenticationConfigurator configurator = new AuthenticationConfigurator(accessor);
        final boolean configured = configurator.configureAuthenticationForApplicationLink(
                link1, new MockAuthenticationScenario(false, true), requestFactory);

        verify(trusted, atMost(1)).isApplicable(Matchers.<AuthenticationScenario>anyObject(), eq(link1));
        verify(oauth, atMost(1)).isApplicable(Matchers.<AuthenticationScenario>anyObject(), eq(link1));
        verify(oauth, times(1)).enable(eq(requestFactory), eq(link1));
        verify(trusted, never()).enable(eq(requestFactory), eq(link1));
        assertTrue(configured);
    }

    public void testNoAutoConfiguration() throws Exception
    {
        final AuthenticationConfigurator configurator = new AuthenticationConfigurator(accessor);
        final boolean configured = configurator.configureAuthenticationForApplicationLink(
                link1, new MockAuthenticationScenario(false, false), requestFactory);
        assertFalse(configured);
    }

    public void testAutoConfigurationFailed() throws Exception
    {
        when(oauth.getAuthenticationProviderClass()).thenAnswer(new Answer<Class<? extends AuthenticationProvider>>()
        {
            public Class<? extends AuthenticationProvider> answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                return OAuthAuthenticationProvider.class;
            }
        });
        doThrow(new AuthenticationConfigurationException("Failed")).when(oauth).enable(requestFactory, link1);

        final AuthenticationConfigurator configurator = new AuthenticationConfigurator(accessor);
        try
        {
            configurator.configureAuthenticationForApplicationLink(link1,
                    new MockAuthenticationScenario(false, true), requestFactory);
            fail("Should have thrown " + AuthenticationConfigurationException.class +
                    ", because OAuth auto-configuration failed and trusted apps is not applicable.");
        }
        catch(AuthenticationConfigurationException ex)
        {
            assertEquals("No authentication provider configured and one or " +
                    "more authentication provider threw an exception during " +
                    "auto-configuration.", ex.getMessage());
        }
    }



    private static class MockAuthenticationScenario implements AuthenticationScenario
    {
        private final boolean commonUserBase;
        private final boolean trusted;

        private MockAuthenticationScenario(boolean commonUserBase, boolean trusted)
        {
            this.commonUserBase = commonUserBase;
            this.trusted = trusted;
        }

        public boolean isCommonUserBase()
        {
            return commonUserBase;
        }

        public boolean isTrusted()
        {
            return trusted;
        }
    }
}
