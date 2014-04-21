package com.atlassian.upm.rest.representations;

import java.net.URI;
import java.util.Locale;
import java.util.Map;

import com.atlassian.plugins.domain.model.plugin.PluginVersion;
import com.atlassian.plugins.domain.model.product.Product;
import com.atlassian.upm.PluginAccessorAndController;
import com.atlassian.upm.permission.Permission;
import com.atlassian.upm.rest.UpmUriBuilder;
import com.atlassian.upm.rest.async.AsynchronousTaskManager;
import com.atlassian.upm.rest.resources.permission.PermissionEnforcer;
import com.atlassian.upm.spi.Plugin;

import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import static com.atlassian.integrationtesting.ApplicationPropertiesImpl.getStandardApplicationProperties;
import static com.atlassian.upm.permission.Permission.MANAGE_SAFE_MODE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test class for the {@code LinkBuilder} and its various permutations for different plugin representations.
 */
@RunWith(Theories.class)
public class PluginCollectionRepresentationLinksTest
{
    @DataPoints public static RepresentationLinksChooser[] REPRESENTATION_CHOOSERS = RepresentationLinksChooser.values();
    @DataPoints public static Boolean[] PERMISSION_ALLOWED = new Boolean[]{Boolean.TRUE, Boolean.FALSE};

    PluginAccessorAndController pluginAccessorAndController;
    AsynchronousTaskManager asynchronousTaskManager;
    LinkBuilder linkBuilder;
    static PermissionEnforcer permissionEnforcer;
    
    private UpmUriBuilder uriBuilder = new UpmUriBuilder(getStandardApplicationProperties());

    @Before
    public void prepareComponents()
    {
        pluginAccessorAndController = mock(PluginAccessorAndController.class);
        asynchronousTaskManager = mock(AsynchronousTaskManager.class);
        permissionEnforcer = mock(PermissionEnforcer.class);
        when(permissionEnforcer.hasPermission(any(Permission.class))).thenReturn(true);
        linkBuilder = new LinkBuilder(uriBuilder, pluginAccessorAndController, asynchronousTaskManager, permissionEnforcer);
    }

    @Theory
    public void assertThatChangesRequiringRestartLinkIsPresentWhenThereArePluginsRequiringRestart(
        RepresentationLinksChooser chooser)
    {
        when(pluginAccessorAndController.hasChangesRequiringRestart()).thenReturn(true);
        Map<String, URI> links = chooser.getRepresentationLinks(uriBuilder, pluginAccessorAndController,
            linkBuilder);
        assertThat(links.get("changes-requiring-restart"), is(notNullValue()));
    }

    @Theory
    public void assertThatChangesRequiringRestartLinkIsNotPresentWhenThereAreNoPluginsRequiringRestart(
        RepresentationLinksChooser chooser)
    {
        when(pluginAccessorAndController.hasChangesRequiringRestart()).thenReturn(false);
        Map<String, URI> links = chooser.getRepresentationLinks(uriBuilder, pluginAccessorAndController,
            linkBuilder);
        assertThat(links.get("changes-requiring-restart"), is(nullValue()));
    }

    @Theory
    public void assertThatPendingTasksLinkIsPresentWhenThereArePendingTasks(RepresentationLinksChooser chooser)
    {
        when(asynchronousTaskManager.hasPendingTasks()).thenReturn(true);
        Map<String, URI> links = chooser.getRepresentationLinks(uriBuilder, pluginAccessorAndController,
            linkBuilder);
        assertThat(links.get("pending-tasks"), is(notNullValue()));
    }

    @Theory
    public void assertThatPendingTasksLinkIsNotPresentWhenThereAreNoPendingTasks(RepresentationLinksChooser chooser)
    {
        when(asynchronousTaskManager.hasPendingTasks()).thenReturn(false);
        Map<String, URI> links = chooser.getRepresentationLinks(uriBuilder, pluginAccessorAndController,
            linkBuilder);
        assertThat(links.get("pending-tasks"), is(nullValue()));
    }

    @Theory
    public void assertThatEnterSafeModeLinkIsPresentWhenSystemIsNotInSafeMode(RepresentationLinksChooser chooser, Boolean permissionAllowed)
    {
        when(permissionEnforcer.hasPermission(MANAGE_SAFE_MODE)).thenReturn(permissionAllowed);
        when(pluginAccessorAndController.isSafeMode()).thenReturn(false);
        Map<String, URI> links = chooser.getRepresentationLinks(uriBuilder, pluginAccessorAndController,
            linkBuilder);
        assertThat(links.get("enter-safe-mode"), is(permissionAllowed ? notNullValue() : nullValue()));
    }

    @Theory
    public void assertThatExitSafeModeRestoreLinkIsPresentWhenSystemIsInSafeMode(RepresentationLinksChooser chooser, Boolean permissionAllowed)
    {
        when(permissionEnforcer.hasPermission(MANAGE_SAFE_MODE)).thenReturn(permissionAllowed);
        when(pluginAccessorAndController.isSafeMode()).thenReturn(true);
        Map<String, URI> links = chooser.getRepresentationLinks(uriBuilder, pluginAccessorAndController,
            linkBuilder);
        assertThat(links.get("exit-safe-mode-restore"), is(permissionAllowed ? notNullValue() : nullValue()));
    }

    @Theory
    public void assertThatExitSafeModeKeepLinkIsPresentWhenSystemIsInSafeMode(RepresentationLinksChooser chooser, Boolean permissionAllowed)
    {
        when(permissionEnforcer.hasPermission(MANAGE_SAFE_MODE)).thenReturn(permissionAllowed);
        when(pluginAccessorAndController.isSafeMode()).thenReturn(true);
        Map<String, URI> links = chooser.getRepresentationLinks(uriBuilder, pluginAccessorAndController,
            linkBuilder);
        assertThat(links.get("exit-safe-mode-keep"), is(permissionAllowed ? notNullValue() : nullValue()));
    }

    enum RepresentationLinksChooser
    {
        INSTALLED
            {
                @Override
                Map<String, URI> getRepresentationLinks(UpmUriBuilder uriBuilder,
                    PluginAccessorAndController pluginAccessorAndController, LinkBuilder linkBuilder)
                {
                    return new InstalledPluginCollectionRepresentation(pluginAccessorAndController,
                        uriBuilder, linkBuilder, permissionEnforcer, Locale.ENGLISH, ImmutableList.<Plugin>of(), null, "").getLinks();
                }
            },
        AVAILABLE
            {
                @Override
                Map<String, URI> getRepresentationLinks(UpmUriBuilder uriBuilder,
                    PluginAccessorAndController pluginAccessorAndController, LinkBuilder linkBuilder)
                {
                    return new AvailablePluginCollectionRepresentation(ImmutableList.<PluginVersion>of(),
                        uriBuilder, linkBuilder, null).getLinks();
                }
            },
        FEATURED
            {
                @Override
                Map<String, URI> getRepresentationLinks(UpmUriBuilder uriBuilder,
                    PluginAccessorAndController pluginAccessorAndController, LinkBuilder linkBuilder)
                {
                    return new FeaturedPluginCollectionRepresentation(ImmutableList.<PluginVersion>of(),
                        uriBuilder, linkBuilder, null).getLinks();
                }
            },
        POPULAR
            {
                @Override
                Map<String, URI> getRepresentationLinks(UpmUriBuilder uriBuilder,
                    PluginAccessorAndController pluginAccessorAndController, LinkBuilder linkBuilder)
                {
                    return new PopularPluginCollectionRepresentation(ImmutableList.<PluginVersion>of(),
                        uriBuilder, linkBuilder, null).getLinks();
                }
            },
        PRODUCT_UPDATES
            {
                @Override
                Map<String, URI> getRepresentationLinks(UpmUriBuilder uriBuilder,
                    PluginAccessorAndController pluginAccessorAndController, LinkBuilder linkBuilder)
                {
                    return new ProductUpdatesRepresentation(uriBuilder, ImmutableList.<Product>of(),
                        linkBuilder, null).getLinks();
                }
            };

        abstract Map<String, URI> getRepresentationLinks(UpmUriBuilder uriBuilder,
            PluginAccessorAndController pluginAccessorAndController, LinkBuilder linkBuilder);
    }
}
