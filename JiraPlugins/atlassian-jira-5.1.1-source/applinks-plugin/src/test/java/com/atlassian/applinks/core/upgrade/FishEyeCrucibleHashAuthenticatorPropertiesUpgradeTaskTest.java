package com.atlassian.applinks.core.upgrade;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.core.property.ApplicationLinkProperties;
import com.atlassian.applinks.core.property.PropertyService;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Collections;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class FishEyeCrucibleHashAuthenticatorPropertiesUpgradeTaskTest
{
    private static final ApplicationId ID_0 = new ApplicationId("00000000-0000-0000-0000-000000000000");
    private static final ApplicationId ID_1 = new ApplicationId("11111111-1111-1111-1111-111111111111");

    private static final String BASIC_AUTH_PROVIDER_KEY = "com.atlassian.applinks.api.auth.types.BasicAuthenticationProvider";
    private static final String OAUTH_PROVIDER_KEY = "com.atlassian.applinks.api.auth.types.OAuthAuthenticationProvider";
    private static final String TRUSTED_APPS_PROVIDER_KEY = "com.atlassian.applinks.api.auth.types.TrustedAppsAuthenticationProvider";

    private static final String DEPRECATED_BASIC_AUTH_KEY_ID_0 = asAuthProviderKey(ID_0, BASIC_AUTH_PROVIDER_KEY);
    private static final String DEPRECATED_OAUTH_KEY_ID_0 = asAuthProviderKey(ID_0, OAUTH_PROVIDER_KEY);
    private static final String DEPRECATED_TRUSTED_KEY_ID_0 = asAuthProviderKey(ID_0, TRUSTED_APPS_PROVIDER_KEY);
    private static final String DEPRECATED_OAUTH_KEY_ID_1 = asAuthProviderKey(ID_1, OAUTH_PROVIDER_KEY);

    private static final String NEW_BASIC_AUTH_KEY_ID_0 = asAuthProviderKey(ID_0, hash(BASIC_AUTH_PROVIDER_KEY));
    private static final String NEW_OAUTH_KEY_ID_0 = asAuthProviderKey(ID_0, hash(OAUTH_PROVIDER_KEY));
    private static final String NEW_TRUSTED_KEY_ID_0 = asAuthProviderKey(ID_0, hash(TRUSTED_APPS_PROVIDER_KEY));
    private static final String NEW_OAUTH_KEY_ID_1 = asAuthProviderKey(ID_1, hash(OAUTH_PROVIDER_KEY));

    private static String asAuthProviderKey(ApplicationId id, String authProviderKeyOrHash)
    {
        return "applinks.admin." + id + ".auth." + authProviderKeyOrHash;
    }

    private static String hash(String string)
    {
        return DigestUtils.md5Hex(string);
    }

    FishEyeCrucibleHashAuthenticatorPropertiesUpgradeTask task;

    @Mock
    PropertyService propertyService;
    @Mock
    PluginSettingsFactory pluginSettingsFactory;
    @Mock
    PluginSettings pluginSettings;
    @Mock
    ApplicationLinkService applicationLinkService;
    @Mock
    I18nResolver i18nResolver;

    ApplicationLink appLink0;
    ApplicationLink appLink1;

    ApplicationLinkProperties appLinkProps0;
    ApplicationLinkProperties appLinkProps1;

    @Before
    public void setUp()
    {
        initMocks(this);
        when(pluginSettingsFactory.createGlobalSettings()).thenReturn(pluginSettings);

        appLink0 = mock(ApplicationLink.class);
        appLink1 = mock(ApplicationLink.class);

        when(appLink0.getId()).thenReturn(ID_0);
        when(appLink0.toString()).thenReturn("AppLink 0");
        when(appLink1.getId()).thenReturn(ID_1);
        when(appLink1.toString()).thenReturn("AppLink 1");

        appLinkProps0 = mock(ApplicationLinkProperties.class);
        when(propertyService.getApplicationLinkProperties(ID_0)).thenReturn(appLinkProps0);
        appLinkProps1 = mock(ApplicationLinkProperties.class);
        when(propertyService.getApplicationLinkProperties(ID_1)).thenReturn(appLinkProps1);

        task = new FishEyeCrucibleHashAuthenticatorPropertiesUpgradeTask(
                propertyService, pluginSettingsFactory, applicationLinkService, i18nResolver);
    }

    @Test
    public void testUpgradeApplicationLinks() throws Exception
    {
        when(applicationLinkService.getApplicationLinks()).thenReturn(newArrayList(appLink0, appLink1));
        when(appLinkProps0.getProviderKeys()).thenReturn(newArrayList(BASIC_AUTH_PROVIDER_KEY, TRUSTED_APPS_PROVIDER_KEY, OAUTH_PROVIDER_KEY));
        when(appLinkProps1.getProviderKeys()).thenReturn(newArrayList(OAUTH_PROVIDER_KEY));

        final Map<String, String> basicAuthProps = ImmutableMap.of("ba0", "true");
        final Map<String, String> oauthProps = ImmutableMap.of("oa0", "yes");
        final Map<String, String> trustedProps = ImmutableMap.of("ta0", "affirmative");

        final Map<String, String> moreOAuthProps = ImmutableMap.of("oa1", "aye");

        when(pluginSettings.get(DEPRECATED_BASIC_AUTH_KEY_ID_0)).thenReturn(basicAuthProps);
        when(pluginSettings.get(DEPRECATED_OAUTH_KEY_ID_0)).thenReturn(oauthProps);
        when(pluginSettings.get(DEPRECATED_TRUSTED_KEY_ID_0)).thenReturn(trustedProps);

        when(pluginSettings.get(DEPRECATED_OAUTH_KEY_ID_1)).thenReturn(moreOAuthProps);

        assertTrue(task.doUpgrade().isEmpty());

        verify(appLinkProps0).getProviderKeys();
        verify(appLinkProps0).setProviderConfig(BASIC_AUTH_PROVIDER_KEY, basicAuthProps);
        verify(appLinkProps0).setProviderConfig(OAUTH_PROVIDER_KEY, oauthProps);
        verify(appLinkProps0).setProviderConfig(TRUSTED_APPS_PROVIDER_KEY, trustedProps);
        verify(appLinkProps1).getProviderKeys();
        verify(appLinkProps1).setProviderConfig(OAUTH_PROVIDER_KEY, moreOAuthProps);

        verify(pluginSettings).remove(DEPRECATED_BASIC_AUTH_KEY_ID_0);
        verify(pluginSettings).remove(DEPRECATED_OAUTH_KEY_ID_0);
        verify(pluginSettings).remove(DEPRECATED_TRUSTED_KEY_ID_0);
        verify(pluginSettings).remove(DEPRECATED_OAUTH_KEY_ID_1);

        verifyNoMoreInteractions(appLinkProps0, appLinkProps1);
    }

    @Test
    public void testUpgradeNoApplicationLinks() throws Exception
    {
        when(applicationLinkService.getApplicationLinks()).thenReturn(Collections.<ApplicationLink>emptyList());

        assertTrue(task.doUpgrade().isEmpty());

        verifyZeroInteractions(propertyService, i18nResolver); // i18n resolver indicates no error messages resolved
    }

    @Test
    public void testUpgradeNoConfiguredAuthenticationProviders() throws Exception
    {
        when(applicationLinkService.getApplicationLinks()).thenReturn(Collections.<ApplicationLink>emptyList());

        when(appLinkProps0.getProviderKeys()).thenReturn(Collections.<String>emptyList());
        when(appLinkProps1.getProviderKeys()).thenReturn(Collections.<String>emptyList());

        assertTrue(task.doUpgrade().isEmpty());

        verifyZeroInteractions(i18nResolver); // i18n resolver indicates no error messages resolved
    }

    @Test
    public void testUpgradeKeyIsAlreadyUpgraded() throws Exception
    {
        when(applicationLinkService.getApplicationLinks()).thenReturn(newArrayList(appLink0, appLink1));
        when(appLinkProps0.getProviderKeys()).thenReturn(newArrayList(BASIC_AUTH_PROVIDER_KEY, TRUSTED_APPS_PROVIDER_KEY, OAUTH_PROVIDER_KEY));
        when(appLinkProps1.getProviderKeys()).thenReturn(newArrayList(OAUTH_PROVIDER_KEY));

        when(pluginSettings.get(DEPRECATED_BASIC_AUTH_KEY_ID_0)).thenReturn(null);
        when(pluginSettings.get(DEPRECATED_OAUTH_KEY_ID_0)).thenReturn(null);
        when(pluginSettings.get(DEPRECATED_TRUSTED_KEY_ID_0)).thenReturn(null);
        when(pluginSettings.get(DEPRECATED_OAUTH_KEY_ID_1)).thenReturn(null);

        assertTrue(task.doUpgrade().isEmpty());

        verify(appLinkProps0).getProviderKeys();
        verify(appLinkProps1).getProviderKeys();

        verify(pluginSettings).get(DEPRECATED_BASIC_AUTH_KEY_ID_0);
        verify(pluginSettings).get(DEPRECATED_OAUTH_KEY_ID_0);
        verify(pluginSettings).get(DEPRECATED_TRUSTED_KEY_ID_0);
        verify(pluginSettings).get(DEPRECATED_OAUTH_KEY_ID_1);

        verify(pluginSettings).remove(DEPRECATED_BASIC_AUTH_KEY_ID_0);
        verify(pluginSettings).remove(DEPRECATED_OAUTH_KEY_ID_0);
        verify(pluginSettings).remove(DEPRECATED_TRUSTED_KEY_ID_0);
        verify(pluginSettings).remove(DEPRECATED_OAUTH_KEY_ID_1);


        verifyNoMoreInteractions(appLinkProps0, appLinkProps1, pluginSettings);
    }

    @Test
    public void testUpgradeGetPluginSettingsThrowsException() throws Exception
    {
        when(applicationLinkService.getApplicationLinks()).thenReturn(newArrayList(appLink0, appLink1));
        when(appLinkProps0.getProviderKeys()).thenReturn(newArrayList(BASIC_AUTH_PROVIDER_KEY, TRUSTED_APPS_PROVIDER_KEY, OAUTH_PROVIDER_KEY));
        when(appLinkProps1.getProviderKeys()).thenReturn(newArrayList(OAUTH_PROVIDER_KEY));

        final Map<String, String> trustedProps = ImmutableMap.of("ta0", "affirmative");

        when(pluginSettings.get(DEPRECATED_BASIC_AUTH_KEY_ID_0)).thenThrow(new IllegalStateException());
        when(pluginSettings.get(DEPRECATED_OAUTH_KEY_ID_0)).thenThrow(new IllegalArgumentException());
        when(pluginSettings.get(DEPRECATED_TRUSTED_KEY_ID_0)).thenReturn(trustedProps);
        when(pluginSettings.get(DEPRECATED_OAUTH_KEY_ID_1)).thenThrow(new UnsupportedOperationException());

        assertTrue(task.doUpgrade().isEmpty());

        // the one key that doesn't throw an exception should still be upgraded
        verify(appLinkProps0).setProviderConfig(TRUSTED_APPS_PROVIDER_KEY, trustedProps);

        verify(appLinkProps0).getProviderKeys();
        verify(appLinkProps1).getProviderKeys();

        verify(pluginSettings).get(DEPRECATED_BASIC_AUTH_KEY_ID_0);
        verify(pluginSettings).get(DEPRECATED_OAUTH_KEY_ID_0);
        verify(pluginSettings).get(DEPRECATED_TRUSTED_KEY_ID_0);
        verify(pluginSettings).get(DEPRECATED_OAUTH_KEY_ID_1);

        verify(pluginSettings).remove(DEPRECATED_BASIC_AUTH_KEY_ID_0);
        verify(pluginSettings).remove(DEPRECATED_OAUTH_KEY_ID_0);
        verify(pluginSettings).remove(DEPRECATED_TRUSTED_KEY_ID_0);
        verify(pluginSettings).remove(DEPRECATED_OAUTH_KEY_ID_1);

        verifyNoMoreInteractions(appLinkProps0, appLinkProps1, pluginSettings);
    }

    @Test
    public void testUpgradePluginSettingsRemoveThrowsException() throws Exception
    {
        when(applicationLinkService.getApplicationLinks()).thenReturn(newArrayList(appLink0, appLink1));
        when(appLinkProps0.getProviderKeys()).thenReturn(newArrayList(BASIC_AUTH_PROVIDER_KEY, TRUSTED_APPS_PROVIDER_KEY, OAUTH_PROVIDER_KEY));
        when(appLinkProps1.getProviderKeys()).thenReturn(newArrayList(OAUTH_PROVIDER_KEY));

        final Map<String, String> basicAuthProps = ImmutableMap.of("ba0", "true");
        final Map<String, String> oauthProps = ImmutableMap.of("oa0", "yes");
        final Map<String, String> trustedProps = ImmutableMap.of("ta0", "affirmative");

        final Map<String, String> moreOAuthProps = ImmutableMap.of("oa1", "aye");

        when(pluginSettings.get(DEPRECATED_BASIC_AUTH_KEY_ID_0)).thenReturn(basicAuthProps);
        when(pluginSettings.get(DEPRECATED_OAUTH_KEY_ID_0)).thenReturn(oauthProps);
        when(pluginSettings.get(DEPRECATED_TRUSTED_KEY_ID_0)).thenReturn(trustedProps);

        when(pluginSettings.get(DEPRECATED_OAUTH_KEY_ID_1)).thenReturn(moreOAuthProps);

        when(pluginSettings.remove(DEPRECATED_BASIC_AUTH_KEY_ID_0)).thenThrow(new IllegalArgumentException());
        when(pluginSettings.remove(DEPRECATED_OAUTH_KEY_ID_0)).thenThrow(new NullPointerException());
        when(pluginSettings.remove(DEPRECATED_TRUSTED_KEY_ID_0)).thenThrow(new ArrayIndexOutOfBoundsException());
        when(pluginSettings.remove(DEPRECATED_OAUTH_KEY_ID_1)).thenThrow(new IllegalStateException());

        assertTrue(task.doUpgrade().isEmpty());

        verify(appLinkProps0).getProviderKeys();
        verify(appLinkProps0).setProviderConfig(BASIC_AUTH_PROVIDER_KEY, basicAuthProps);
        verify(appLinkProps0).setProviderConfig(OAUTH_PROVIDER_KEY, oauthProps);
        verify(appLinkProps0).setProviderConfig(TRUSTED_APPS_PROVIDER_KEY, trustedProps);
        verify(appLinkProps1).getProviderKeys();
        verify(appLinkProps1).setProviderConfig(OAUTH_PROVIDER_KEY, moreOAuthProps);

        // we should still attempt to remove all upgraded properties even if each independently an exception
        verify(pluginSettings).remove(DEPRECATED_BASIC_AUTH_KEY_ID_0);
        verify(pluginSettings).remove(DEPRECATED_OAUTH_KEY_ID_0);
        verify(pluginSettings).remove(DEPRECATED_TRUSTED_KEY_ID_0);
        verify(pluginSettings).remove(DEPRECATED_OAUTH_KEY_ID_1);

        verifyNoMoreInteractions(appLinkProps0, appLinkProps1);
    }

}
