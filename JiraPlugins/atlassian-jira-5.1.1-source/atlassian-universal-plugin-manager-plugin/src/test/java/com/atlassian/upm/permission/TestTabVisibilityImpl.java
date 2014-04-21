package com.atlassian.upm.permission;

import com.atlassian.upm.rest.resources.permission.PermissionEnforcer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import junit.framework.TestCase;

import static com.atlassian.upm.permission.Permission.GET_AUDIT_LOG;
import static com.atlassian.upm.permission.Permission.GET_AVAILABLE_PLUGINS;
import static com.atlassian.upm.permission.Permission.GET_OSGI_STATE;
import static com.atlassian.upm.permission.Permission.GET_PRODUCT_UPDATE_COMPATIBILITY;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TestTabVisibilityImpl extends TestCase
{
    private @Mock PermissionEnforcer permissionEnforcer;
    private UpmVisibilityImpl tabVisibility;

    @Before
    public void setUp()
    {
        tabVisibility = new UpmVisibilityImpl(permissionEnforcer);
    }

    @Test
    public void testManageExistingIsVisibleIfAdmin()
    {
        when(permissionEnforcer.isAdmin()).thenReturn(true);
        assertTrue(tabVisibility.isManageExistingVisible());
    }

    @Test
    public void testManageExistingIsNotVisibleIfNotAdmin()
    {
        when(permissionEnforcer.isAdmin()).thenReturn(false);
        assertFalse(tabVisibility.isManageExistingVisible());
    }

    @Test
    public void testUpdateIsVisibleIfHasUpdatePermission()
    {
        when(permissionEnforcer.hasPermission(GET_AVAILABLE_PLUGINS)).thenReturn(true);
        assertTrue(tabVisibility.isUpdateVisible());
    }

    @Test
    public void testUpdateIsNotVisibleIfNotHasUpdatePermission()
    {
        when(permissionEnforcer.hasPermission(GET_AVAILABLE_PLUGINS)).thenReturn(false);
        assertFalse(tabVisibility.isUpdateVisible());
    }

    @Test
    public void testInstallIsVisibleIfHasGetAvailablePermission()
    {
        when(permissionEnforcer.hasPermission(GET_AVAILABLE_PLUGINS)).thenReturn(true);
        assertTrue(tabVisibility.isInstallVisible());
    }

    @Test
    public void testInstallIsNotVisibleIfNotHasGetAvailablePermission()
    {
        when(permissionEnforcer.hasPermission(GET_AVAILABLE_PLUGINS)).thenReturn(false);
        assertFalse(tabVisibility.isInstallVisible());
    }

    @Test
    public void testCompatibilityIsVisibleIfHasGetUpdatePermission()
    {
        when(permissionEnforcer.hasPermission(GET_PRODUCT_UPDATE_COMPATIBILITY)).thenReturn(true);
        assertTrue(tabVisibility.isCompatibilityVisible());
    }

    @Test
    public void testCompatibilityIsNotVisibleIfNotHasGetUpdatePermission()
    {
        when(permissionEnforcer.hasPermission(GET_PRODUCT_UPDATE_COMPATIBILITY)).thenReturn(false);
        assertFalse(tabVisibility.isCompatibilityVisible());
    }

    @Test
    public void testOsgiIsVisibleIfHasGetOsgiStatePermission()
    {
        when(permissionEnforcer.hasPermission(GET_OSGI_STATE)).thenReturn(true);
        assertTrue(tabVisibility.isOsgiVisible());
    }

    @Test
    public void testOsgiIsNotVisibleIfNotHasGetOsgiStatePermission()
    {
        when(permissionEnforcer.hasPermission(GET_OSGI_STATE)).thenReturn(false);
        assertFalse(tabVisibility.isOsgiVisible());
    }

    @Test
    public void testAuditLogIsVisibleIfHasAuditLogPermission()
    {
        when(permissionEnforcer.hasPermission(GET_AUDIT_LOG)).thenReturn(true);
        assertTrue(tabVisibility.isAuditLogVisible());
    }

    @Test
    public void testAuditLogIsNotVisibleIfNotHasAuditLogPermission()
    {
        when(permissionEnforcer.hasPermission(GET_AUDIT_LOG)).thenReturn(false);
        assertFalse(tabVisibility.isAuditLogVisible());
    }
}
