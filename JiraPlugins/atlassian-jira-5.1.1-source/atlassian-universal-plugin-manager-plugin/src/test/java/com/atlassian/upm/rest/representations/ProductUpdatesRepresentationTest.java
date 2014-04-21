package com.atlassian.upm.rest.representations;

import java.util.Collections;
import java.util.Date;

import com.atlassian.plugin.PluginRestartState;
import com.atlassian.plugins.domain.model.product.Product;
import com.atlassian.upm.PluginAccessorAndController;
import com.atlassian.upm.permission.Permission;
import com.atlassian.upm.rest.UpmUriBuilder;
import com.atlassian.upm.rest.async.AsynchronousTaskManager;
import com.atlassian.upm.rest.resources.permission.PermissionEnforcer;
import com.atlassian.upm.spi.Plugin;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.integrationtesting.ApplicationPropertiesImpl.getStandardApplicationProperties;
import static com.google.common.collect.Iterables.getOnlyElement;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProductUpdatesRepresentationTest
{
    @Mock Product product;
    @Mock PluginAccessorAndController pluginAccessorAndController;
    @Mock AsynchronousTaskManager asynchronousTaskManager;
    @Mock PermissionEnforcer permissionEnforcer;

    private UpmUriBuilder uriBuilder = new UpmUriBuilder(getStandardApplicationProperties());
    private LinkBuilder linkBuilder;

    @Before
    public void createProductUpdatePluginCompatibilityRepresentation()
    {
        when(product.getProduct()).thenReturn("PRODUCT");
        when(product.getVersionNumber()).thenReturn("0");
        when(product.getBuildNumber()).thenReturn(0L);
        when(permissionEnforcer.hasPermission(any(Permission.class), any(Plugin.class))).thenReturn(true);
        when(pluginAccessorAndController.getRestartState(any(Plugin.class))).thenReturn(PluginRestartState.NONE);

        this.linkBuilder = new LinkBuilder(uriBuilder, pluginAccessorAndController, asynchronousTaskManager, permissionEnforcer);
    }

    @Test
    public void assertThatProductReleasedThirteenDaysAgoIsRecent()
    {
        when(product.getReleaseDate()).thenReturn(daysAgo(13));
        ProductUpdatesRepresentation updatesRepresentation = getRepresentation();
        assertTrue(getOnlyElement(updatesRepresentation.getVersions()).isRecent());
    }

    @Test
    public void assertThatProductReleasedFifteenDaysAgoIsNotRecent()
    {
        when(product.getReleaseDate()).thenReturn(daysAgo(15));
        ProductUpdatesRepresentation updatesRepresentation = getRepresentation();
        assertFalse(getOnlyElement(updatesRepresentation.getVersions()).isRecent());
    }

    @Test
    public void assertThatProductWithNoReleaseDateIsNotRecent()
    {
        when(product.getReleaseDate()).thenReturn(null);
        ProductUpdatesRepresentation updatesRepresentation = getRepresentation();
        assertFalse(getOnlyElement(updatesRepresentation.getVersions()).isRecent());
    }

    private ProductUpdatesRepresentation getRepresentation()
    {
        return new ProductUpdatesRepresentation(uriBuilder, Collections.singleton(product), linkBuilder, null);
    }

    private static Date daysAgo(int days)
    {
        return new Date(new DateTime().minusDays(days).getMillis());
    }
}
