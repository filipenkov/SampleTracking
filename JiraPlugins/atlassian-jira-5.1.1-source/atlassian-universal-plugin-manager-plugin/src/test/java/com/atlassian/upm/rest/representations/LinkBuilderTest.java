package com.atlassian.upm.rest.representations;

import java.net.URI;

import com.atlassian.upm.PluginAccessorAndController;
import com.atlassian.upm.permission.Permission;
import com.atlassian.upm.rest.UpmUriBuilder;
import com.atlassian.upm.rest.async.AsynchronousTaskManager;
import com.atlassian.upm.rest.resources.permission.PermissionEnforcer;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import static com.atlassian.integrationtesting.ApplicationPropertiesImpl.getStandardApplicationProperties;
import static com.atlassian.upm.permission.Permission.GET_AUDIT_LOG;
import static com.atlassian.upm.permission.Permission.GET_AVAILABLE_PLUGINS;
import static com.atlassian.upm.permission.Permission.GET_OSGI_STATE;
import static com.atlassian.upm.permission.Permission.GET_PRODUCT_UPDATE_COMPATIBILITY;
import static com.atlassian.upm.permission.Permission.MANAGE_AUDIT_LOG;
import static com.atlassian.upm.permission.Permission.MANAGE_PLUGIN_INSTALL;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Theories.class)
public class LinkBuilderTest
{
    @DataPoints public static TestLink[] testLinks = TestLink.values();

    PluginAccessorAndController pluginAccessorAndController;
    AsynchronousTaskManager asynchronousTaskManager;
    PermissionEnforcer permissionEnforcer;

    private static UpmUriBuilder uriBuilder = new UpmUriBuilder(getStandardApplicationProperties());
    private static LinkBuilder linkBuilder;

    @Before
    public void setUp() throws Exception
    {
        pluginAccessorAndController = mock(PluginAccessorAndController.class);
        asynchronousTaskManager = mock(AsynchronousTaskManager.class);
        permissionEnforcer = mock(PermissionEnforcer.class);
        linkBuilder = new LinkBuilder(uriBuilder, pluginAccessorAndController, asynchronousTaskManager, permissionEnforcer);
    }

    @Theory
    public void assertThatUriIsCorrectWhenAllowed(TestLink testLink)
    {
        when(permissionEnforcer.hasPermission(testLink.getPermission())).thenReturn(true);
        assertThat(testLink.buildPermissionedUri(), is(equalTo(testLink.buildUri())));
    }

    @Theory
    public void assertThatUriIsNullWhenDisallowed(TestLink testLink)
    {
        when(permissionEnforcer.hasPermission(testLink.getPermission())).thenReturn(false);
        assertThat(testLink.buildPermissionedUri(), is(nullValue()));
    }

    /**
     * The root uri is always allowed and therefore cannot be tested similar to the other {@code TestLink}s.
     */
    @Test
    public void assertThatRootUriIsCorrect()
    {
        assertThat(linkBuilder.buildPermissionedUris().get("upmUriRoot"), is(equalTo(uriBuilder.buildInstalledPluginCollectionUri())));
    }


    /**
     * The pac-status uri is always allowed and therefore cannot be tested similar to the other {@code TestLink}s.
     */
    @Test
    public void assertThatPacStatusUriIsCorrect()
    {
        assertThat(linkBuilder.buildPermissionedUris().get("upmUriPacStatus"), is(equalTo(uriBuilder.buildPacStatusUri())));
    }

    enum TestLink
    {
        AVAILABLE_PLUGINS
            {
                @Override
                public URI buildPermissionedUri()
                {
                    return linkBuilder.buildPermissionedUris().get("upmUriAvailable");
                }

                @Override
                public URI buildUri()
                {
                    return uriBuilder.buildAvailablePluginCollectionUri();
                }

                @Override
                public Permission getPermission()
                {
                    return GET_AVAILABLE_PLUGINS;
                }
            },
        FEATURED_PLUGINS
            {
                @Override
                public URI buildPermissionedUri()
                {
                    return linkBuilder.buildPermissionedUris().get("upmUriFeatured");
                }

                @Override
                public URI buildUri()
                {
                    return uriBuilder.buildFeaturedPluginCollectionUri();
                }

                @Override
                public Permission getPermission()
                {
                    return GET_AVAILABLE_PLUGINS;
                }
            },
        SUPPORTED_PLUGINS
            {
                @Override
                public URI buildPermissionedUri()
                {
                    return linkBuilder.buildPermissionedUris().get("upmUriSupported");
                }

                @Override
                public URI buildUri()
                {
                    return uriBuilder.buildSupportedPluginCollectionUri();
                }

                @Override
                public Permission getPermission()
                {
                    return GET_AVAILABLE_PLUGINS;
                }
            },
        POPULAR_PLUGINS
            {
                @Override
                public URI buildPermissionedUri()
                {
                    return linkBuilder.buildPermissionedUris().get("upmUriPopular");
                }

                @Override
                public URI buildUri()
                {
                    return uriBuilder.buildPopularPluginCollectionUri();
                }

                @Override
                public Permission getPermission()
                {
                    return GET_AVAILABLE_PLUGINS;
                }
            },
        UPDATES
            {
                @Override
                public URI buildPermissionedUri()
                {
                    return linkBuilder.buildPermissionedUris().get("upmUriUpdates");
                }

                @Override
                public URI buildUri()
                {
                    return uriBuilder.buildInstalledPluginCollectionUri();
                }

                @Override
                public Permission getPermission()
                {
                    return GET_AVAILABLE_PLUGINS;
                }
            },
        PRODUCT_UPDATES
            {
                @Override
                public URI buildPermissionedUri()
                {
                    return linkBuilder.buildPermissionedUris().get("upmUriProductUpdates");
                }

                @Override
                public URI buildUri()
                {
                    return uriBuilder.buildProductUpdatesUri();
                }

                @Override
                public Permission getPermission()
                {
                    return GET_PRODUCT_UPDATE_COMPATIBILITY;
                }
            },
        VIEW_SAFE_MODE
            {
                @Override
                public URI buildPermissionedUri()
                {
                    return linkBuilder.buildPermissionedUris().get("upmUriSafeMode");
                }

                @Override
                public URI buildUri()
                {
                    return uriBuilder.buildSafeModeUri();
                }

                @Override
                public Permission getPermission()
                {
                    return Permission.GET_SAFE_MODE;
                }
            },
        AUDIT_LOG_FEED
            {
                @Override
                public URI buildPermissionedUri()
                {
                    return linkBuilder.buildPermissionedUris().get("upmUriAuditLog");
                }

                @Override
                public URI buildUri()
                {
                    return uriBuilder.buildAuditLogFeedUri();
                }

                @Override
                public Permission getPermission()
                {
                    return GET_AUDIT_LOG;
                }
            },
        AUDIT_LOG_PURGE_AFTER
            {
                @Override
                public URI buildPermissionedUri()
                {
                    return linkBuilder.buildPermissionedUris().get("upmUriPurgeAfter");
                }

                @Override
                public URI buildUri()
                {
                    return uriBuilder.buildAuditLogPurgeAfterUri();
                }

                @Override
                public Permission getPermission()
                {
                    return GET_AUDIT_LOG;
                }
            },
        MANAGE_AUDIT_LOG_PURGE_AFTER
            {
                @Override
                public URI buildPermissionedUri()
                {
                    return linkBuilder.buildPermissionedUris().get("upmUriManagePurgeAfter");
                }

                @Override
                public URI buildUri()
                {
                    return uriBuilder.buildAuditLogPurgeAfterUri();
                }

                @Override
                public Permission getPermission()
                {
                    return MANAGE_AUDIT_LOG;
                }
            },
        OSGI_BUNDLES
            {
                @Override
                public URI buildPermissionedUri()
                {
                    return linkBuilder.buildPermissionedUris().get("upmUriOsgiBundles");
                }

                @Override
                public URI buildUri()
                {
                    return uriBuilder.buildOsgiBundleCollectionUri();
                }

                @Override
                public Permission getPermission()
                {
                    return GET_OSGI_STATE;
                }
            },
        OSGI_SERVICES
            {
                @Override
                public URI buildPermissionedUri()
                {
                    return linkBuilder.buildPermissionedUris().get("upmUriOsgiServices");
                }

                @Override
                public URI buildUri()
                {
                    return uriBuilder.buildOsgiServiceCollectionUri();
                }

                @Override
                public Permission getPermission()
                {
                    return GET_OSGI_STATE;
                }
            },
        OSGI_PACKAGES
            {
                @Override
                public URI buildPermissionedUri()
                {
                    return linkBuilder.buildPermissionedUris().get("upmUriOsgiPackages");
                }

                @Override
                public URI buildUri()
                {
                    return uriBuilder.buildOsgiPackageCollectionUri();
                }

                @Override
                public Permission getPermission()
                {
                    return GET_OSGI_STATE;
                }
            },
        INSTALL
            {
                @Override
                public URI buildPermissionedUri()
                {
                    return linkBuilder.buildPermissionedUris().get("upmUriInstall");
                }

                @Override
                public URI buildUri()
                {
                    return uriBuilder.buildInstalledPluginCollectionUri();
                }

                @Override
                public Permission getPermission()
                {
                    return MANAGE_PLUGIN_INSTALL;
                }
            };

        abstract Permission getPermission();

        abstract URI buildPermissionedUri();

        abstract URI buildUri();
    }
}
