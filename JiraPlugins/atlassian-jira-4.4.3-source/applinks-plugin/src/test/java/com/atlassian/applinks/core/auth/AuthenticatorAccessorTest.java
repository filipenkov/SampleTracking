package com.atlassian.applinks.core.auth;

import com.atlassian.applinks.api.auth.AuthenticationProvider;
import com.atlassian.applinks.api.auth.ImpersonatingAuthenticationProvider;
import com.atlassian.applinks.api.auth.NonImpersonatingAuthenticationProvider;
import com.atlassian.applinks.core.MockApplicationLink;
import com.atlassian.applinks.core.MockAuthenticationProviderModuleDescriptor;
import com.atlassian.applinks.core.plugin.AuthenticationProviderModuleDescriptor;
import com.atlassian.applinks.spi.auth.AuthenticationProviderPluginModule;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.module.ModuleFactory;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AuthenticatorAccessorTest
{
    private static final MockApplicationLink LINK = new MockApplicationLink();

    PluginAccessor pluginAccessor;
    AuthenticatorAccessor authenticatorAccessor;
    ImpersonatingAuthenticationProvider impersonatingAuthenticationProvider;
    NonImpersonatingAuthenticationProvider nonImpersonatingAuthenticationProvider;

    @Before
    public void setUp()
    {
        pluginAccessor = mock(PluginAccessor.class);
        impersonatingAuthenticationProvider = mock(ImpersonatingAuthenticationProvider.class);
        nonImpersonatingAuthenticationProvider = mock(NonImpersonatingAuthenticationProvider.class);

        authenticatorAccessor = new AuthenticatorAccessor(pluginAccessor);
    }

    @Test
    public void testGetAuthenticators()
    {
        setUpAuthenticators();
        assertEquals(impersonatingAuthenticationProvider, authenticatorAccessor.getAuthenticationProvider(LINK, ImpersonatingAuthenticationProvider.class));
        assertEquals(nonImpersonatingAuthenticationProvider, authenticatorAccessor.getAuthenticationProvider(LINK, NonImpersonatingAuthenticationProvider.class));
    }

    @Test
    public void testGetAuthenticatorsBySuperClass()
    {
        setUpAuthenticators();
        assertEquals(nonImpersonatingAuthenticationProvider, authenticatorAccessor.getAuthenticationProvider(LINK, AuthenticationProvider.class));
    }

    @Test
    public void testGetUnregisteredAuthenticator()
    {
        setUpAuthenticators();
        assertNull(authenticatorAccessor.getAuthenticationProvider(LINK, new AuthenticationProvider() {}.getClass()));
    }

    private void setUpAuthenticators()
    {
        final ModuleFactory moduleFactory = mock(ModuleFactory.class);
        final AuthenticationProviderPluginModule nonImpersonatingAuthenticationProviderPluginModule = mock(AuthenticationProviderPluginModule.class);
        when(nonImpersonatingAuthenticationProviderPluginModule.getAuthenticationProvider(LINK)).thenReturn(nonImpersonatingAuthenticationProvider);

        final AuthenticationProviderPluginModule impersonatingAuthenticationProviderPluginModule = mock(AuthenticationProviderPluginModule.class);
        when(impersonatingAuthenticationProviderPluginModule.getAuthenticationProvider(LINK)).thenReturn(impersonatingAuthenticationProvider);

        when(pluginAccessor.getEnabledModuleDescriptorsByClass(AuthenticationProviderModuleDescriptor.class))
        .thenReturn(Lists.<AuthenticationProviderModuleDescriptor>newArrayList(
            new MockAuthenticationProviderModuleDescriptor(moduleFactory, nonImpersonatingAuthenticationProviderPluginModule),
            new MockAuthenticationProviderModuleDescriptor(moduleFactory, impersonatingAuthenticationProviderPluginModule)
        ));
    }

}
