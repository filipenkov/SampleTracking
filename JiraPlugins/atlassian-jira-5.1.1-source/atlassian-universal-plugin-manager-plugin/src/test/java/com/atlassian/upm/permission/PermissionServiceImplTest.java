package com.atlassian.upm.permission;

import com.atlassian.sal.api.user.UserManager;
import com.atlassian.upm.PluginAccessorAndController;
import com.atlassian.upm.spi.Plugin;

import org.junit.Before;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import static com.atlassian.upm.Sys.UPM_ON_DEMAND;
import static com.atlassian.upm.permission.Permission.GET_AUDIT_LOG;
import static com.atlassian.upm.permission.Permission.GET_AVAILABLE_PLUGINS;
import static com.atlassian.upm.permission.Permission.GET_NOTIFICATIONS;
import static com.atlassian.upm.permission.Permission.GET_OSGI_STATE;
import static com.atlassian.upm.permission.Permission.GET_PLUGIN_MODULES;
import static com.atlassian.upm.permission.Permission.GET_PRODUCT_UPDATE_COMPATIBILITY;
import static com.atlassian.upm.permission.Permission.GET_SAFE_MODE;
import static com.atlassian.upm.permission.Permission.MANAGE_AUDIT_LOG;
import static com.atlassian.upm.permission.Permission.MANAGE_NOTIFICATIONS;
import static com.atlassian.upm.permission.Permission.MANAGE_PLUGIN_ENABLEMENT;
import static com.atlassian.upm.permission.Permission.MANAGE_PLUGIN_INSTALL;
import static com.atlassian.upm.permission.Permission.MANAGE_PLUGIN_LICENSE;
import static com.atlassian.upm.permission.Permission.MANAGE_PLUGIN_MODULE_ENABLEMENT;
import static com.atlassian.upm.permission.Permission.MANAGE_PLUGIN_UNINSTALL;
import static com.atlassian.upm.permission.Permission.MANAGE_SAFE_MODE;
import static com.atlassian.upm.permission.PermissionServiceImplTest.OnDemandPermissionScheme.ALWAYS_DENIED;
import static com.atlassian.upm.permission.PermissionServiceImplTest.OnDemandPermissionScheme.SYSADMIN_ONLY;
import static com.atlassian.upm.permission.PermissionServiceImplTest.OnDemandPermissionScheme.SYSADMIN_OR_ADMIN;
import static com.atlassian.upm.permission.PermissionServiceImplTest.OnDemandPermissionScheme.SYSADMIN_OR_IF_USER_INSTALLED;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(Theories.class)
public class PermissionServiceImplTest
{
    private static final String ADMIN_USER = "admin";
    private static final String SYSTEM_ADMIN_USER = "sysadmin";
    private static final String REGULAR_USER = "barney";

    @DataPoints public static TestPermission[] testPermissions = TestPermission.values();

    UserManager userManager;
    PluginAccessorAndController pluginAccessorAndController;
    PermissionService permissionService;
    Plugin plugin;
    Plugin.Module module;

    @Before
    public void setUp()
    {
        userManager = mock(UserManager.class);
        pluginAccessorAndController = mock(PluginAccessorAndController.class);

        when(userManager.isAdmin(SYSTEM_ADMIN_USER)).thenReturn(true);
        when(userManager.isSystemAdmin(SYSTEM_ADMIN_USER)).thenReturn(true);

        when(userManager.isAdmin(ADMIN_USER)).thenReturn(true);
        when(userManager.isSystemAdmin(ADMIN_USER)).thenReturn(false);

        when(userManager.isAdmin(REGULAR_USER)).thenReturn(false);
        when(userManager.isSystemAdmin(REGULAR_USER)).thenReturn(false);

        permissionService = new PermissionServiceImpl(userManager, pluginAccessorAndController);

        plugin = mock(Plugin.class);
        module = mock(Plugin.Module.class);

        when(pluginAccessorAndController.isUserInstalled(plugin)).thenReturn(true);
        when(module.getPlugin()).thenReturn(plugin);
    }

    @Theory
    public void verifyThatPermissionChecksCorrectUserManagerAdminMethod(TestPermission permission)
    {
        permissionService.hasPermission(anyString(), permission.getPermission());
        if (permission.requiresSystemAdmin())
        {
            verify(userManager).isSystemAdmin(anyString());
        }
        else
        {
            verify(userManager).isAdmin(anyString());
        }
    }

    @Theory
    public void assertThatSystemAdminHasPermissionToAllWithPlugin(TestPermission permission)
    {
        assertTrue(permissionService.hasPermission(SYSTEM_ADMIN_USER, permission.getPermission(), plugin));
    }

    @Theory
    public void assertThatAdminHasPermissionOnlyToPermissionsThatDoesNotRequireSystemAdminRoleWithPlugin(TestPermission permission)
    {
        if (permission.requiresSystemAdmin())
        {
            assertFalse(permissionService.hasPermission(ADMIN_USER, permission.getPermission(), plugin));
        }
        else
        {
            assertTrue(permissionService.hasPermission(ADMIN_USER, permission.getPermission(), plugin));
        }
    }

    @Theory
    public void assertThatSystemAdminHasPermissionToAllWithModule(TestPermission permission)
    {
        when(module.getCompleteKey()).thenReturn("module:key");
        assertTrue(permissionService.hasPermission(SYSTEM_ADMIN_USER, permission.getPermission(), module));
    }

    @Theory
    public void assertThatAdminHasPermissionOnlyToPermissionsThatDoesNotRequireSystemAdminRoleWithModule(TestPermission permission)
    {
        when(module.getCompleteKey()).thenReturn("module:key");
        if (permission.requiresSystemAdmin())
        {
            assertFalse(permissionService.hasPermission(ADMIN_USER, permission.getPermission(), module));
        }
        else
        {
            assertTrue(permissionService.hasPermission(ADMIN_USER, permission.getPermission(), module));
        }
    }

    @Theory
    public void assertThatConfluenceHtmlMacroRequiresSystemAdminForModuleEnable(TestPermission permission)
    {
        when(module.getCompleteKey()).thenReturn(PermissionServiceImpl.CONFLUENCE_MACROS_HTML);
        if (permission.requiresSystemAdmin() || permission.getPermission() == MANAGE_PLUGIN_MODULE_ENABLEMENT)
        {
            assertFalse(permissionService.hasPermission(ADMIN_USER, permission.getPermission(), module));
        }
        else
        {
            assertTrue(permissionService.hasPermission(ADMIN_USER, permission.getPermission(), module));
        }
    }

    @Theory
    public void assertThatConfluenceHtmlIncludeMacroRequiresSystemAdminForModuleEnable(TestPermission permission)
    {
        when(module.getCompleteKey()).thenReturn(PermissionServiceImpl.CONFLUENCE_MACROS_HTML_INCLUDE);
        if (permission.requiresSystemAdmin() || permission.getPermission() == MANAGE_PLUGIN_MODULE_ENABLEMENT)
        {
            assertFalse(permissionService.hasPermission(ADMIN_USER, permission.getPermission(), module));
        }
        else
        {
            assertTrue(permissionService.hasPermission(ADMIN_USER, permission.getPermission(), module));
        }
    }

    @Theory
    public void assertThatSystemAdminHasPermissionToAll(TestPermission permission)
    {
        assertTrue(permissionService.hasPermission(SYSTEM_ADMIN_USER, permission.getPermission()));
    }

    @Theory
    public void assertThatAdminHasPermissionOnlyToPermissionsThatDoesNotRequireSystemAdminRole(TestPermission permission)
    {
        if (permission.requiresSystemAdmin())
        {
            assertFalse(permissionService.hasPermission(ADMIN_USER, permission.getPermission()));
        }
        else
        {
            assertTrue(permissionService.hasPermission(ADMIN_USER, permission.getPermission()));
        }
    }

    @Theory
    public void assertThatRegularUserHasNoPermissionToAnything(TestPermission permission)
    {
        assertFalse(permissionService.hasPermission(REGULAR_USER, permission.getPermission()));
    }

    @Theory
    public void assertThatSystemAdminHasCorrectPermissionWhenOnDemand(TestPermission permission)
    {
        enterOnDemand();
        try
        {
            switch (permission.getOnDemandPermissionScheme())
            {
                case SYSADMIN_OR_ADMIN:
                case SYSADMIN_ONLY:
                case SYSADMIN_OR_IF_USER_INSTALLED:
                    assertTrue(permissionService.hasPermission(SYSTEM_ADMIN_USER, permission.getPermission(), plugin));
                    break;
                case ALWAYS_DENIED:
                    assertFalse(permissionService.hasPermission(SYSTEM_ADMIN_USER, permission.getPermission(), plugin));
                    break;
                default:
                    throw new RuntimeException("Did not handle On Demand permission type: " + permission.getOnDemandPermissionScheme());
            }
        }
        finally
        {
            exitOnDemand();
        }
    }

    @Theory
    public void assertThatAdminHasCorrectPermissionWhenOnDemand(TestPermission permission)
    {
        enterOnDemand();
        try
        {
            switch (permission.getOnDemandPermissionScheme())
            {
                case SYSADMIN_OR_ADMIN:
                case SYSADMIN_OR_IF_USER_INSTALLED:
                    assertTrue(permissionService.hasPermission(ADMIN_USER, permission.getPermission(), plugin));
                    break;
                case SYSADMIN_ONLY:
                case ALWAYS_DENIED:
                    assertFalse(permissionService.hasPermission(ADMIN_USER, permission.getPermission(), plugin));
                    break;
                default:
                    throw new RuntimeException("Did not handle On Demand permission type: " + permission.getOnDemandPermissionScheme());
            }
        }
        finally
        {
            exitOnDemand();
        }
    }

    @Theory
    public void assertThatAdminCannotEnableOrDisableRequiredPluginsOrModulesWhenOnDemand(TestPermission permission)
    {
        when(pluginAccessorAndController.isUserInstalled(plugin)).thenReturn(false);
        enterOnDemand();
        try
        {
            if (permission.getPermission() == MANAGE_PLUGIN_ENABLEMENT)
            {
                assertFalse(permissionService.hasPermission(ADMIN_USER, permission.getPermission(), plugin));
            }
            else if (permission.getPermission() == MANAGE_PLUGIN_MODULE_ENABLEMENT)
            {
                assertFalse(permissionService.hasPermission(ADMIN_USER, permission.getPermission(), module));
            }
        }
        finally
        {
            exitOnDemand();
        }
    }

    @Theory
    public void assertThatRegularUserHasNoPermissionToAnythingWhenOnDemand(TestPermission permission)
    {
        enterOnDemand();
        try
        {
            assertFalse(permissionService.hasPermission(REGULAR_USER, permission.getPermission(), plugin));
        }
        finally
        {
            exitOnDemand();
        }
    }

    private void enterOnDemand()
    {
        System.setProperty(UPM_ON_DEMAND, "true");
    }

    private void exitOnDemand()
    {
        System.setProperty(UPM_ON_DEMAND, "false");
    }

    enum TestPermission
    {
        // Updatable plugins
        GET_UPDATABLE_PLUGINS_PERMISSION
            {
                @Override
                Permission getPermission()
                {
                    return GET_AVAILABLE_PLUGINS;
                }

                @Override
                boolean requiresSystemAdmin()
                {
                    return true;
                }

                @Override
                OnDemandPermissionScheme getOnDemandPermissionScheme()
                {
                    return SYSADMIN_ONLY;
                }
            },

        // Available plugins
        GET_AVAILABLE_PLUGINS_PERMISSION
            {
                @Override
                Permission getPermission()
                {
                    return GET_AVAILABLE_PLUGINS;
                }

                @Override
                boolean requiresSystemAdmin()
                {
                    return true;
                }

                @Override
                OnDemandPermissionScheme getOnDemandPermissionScheme()
                {
                    return SYSADMIN_ONLY;
                }
            },
        MANAGE_PLUGIN_INSTALL_PERMISSION
            {
                @Override
                Permission getPermission()
                {
                    return MANAGE_PLUGIN_INSTALL;
                }

                @Override
                boolean requiresSystemAdmin()
                {
                    return true;
                }

                @Override
                OnDemandPermissionScheme getOnDemandPermissionScheme()
                {
                    return SYSADMIN_ONLY;
                }
            },

        // Installed plugins
        MANAGE_PLUGIN_UNINSTALL_PERMISSION
            {
                @Override
                Permission getPermission()
                {
                    return MANAGE_PLUGIN_UNINSTALL;
                }

                @Override
                boolean requiresSystemAdmin()
                {
                    return true;
                }

                @Override
                OnDemandPermissionScheme getOnDemandPermissionScheme()
                {
                    return SYSADMIN_ONLY;
                }
            },
        MANAGE_PLUGIN_ENABLEMENT_PERMISSION
            {
                @Override
                Permission getPermission()
                {
                    return MANAGE_PLUGIN_ENABLEMENT;
                }

                @Override
                boolean requiresSystemAdmin()
                {
                    return false;
                }

                @Override
                OnDemandPermissionScheme getOnDemandPermissionScheme()
                {
                    return SYSADMIN_OR_IF_USER_INSTALLED;
                }
            },

        // Plugin modules
        GET_PLUGIN_MODULES_PERMISSION
            {
                @Override
                Permission getPermission()
                {
                    return GET_PLUGIN_MODULES;
                }

                @Override
                boolean requiresSystemAdmin()
                {
                    return false;
                }

                @Override
                OnDemandPermissionScheme getOnDemandPermissionScheme()
                {
                    return SYSADMIN_OR_ADMIN;
                }
            },
        MANAGE_PLUGIN_MODULE_ENABLEMENT_PERMISSION
            {
                @Override
                Permission getPermission()
                {
                    return MANAGE_PLUGIN_MODULE_ENABLEMENT;
                }

                @Override
                boolean requiresSystemAdmin()
                {
                    return false;
                }

                @Override
                OnDemandPermissionScheme getOnDemandPermissionScheme()
                {
                    return SYSADMIN_OR_IF_USER_INSTALLED;
                }
            },

        // OSGi
        GET_OSGI_STATE_PERMISSION
            {
                @Override
                Permission getPermission()
                {
                    return GET_OSGI_STATE;
                }

                @Override
                boolean requiresSystemAdmin()
                {
                    return false;
                }

                @Override
                OnDemandPermissionScheme getOnDemandPermissionScheme()
                {
                    return SYSADMIN_ONLY;
                }
            },

        // Compatibility checking
        GET_PRODUCT_UPDATE_COMPATIBILITY_PERMISSION
            {
                @Override
                Permission getPermission()
                {
                    return GET_PRODUCT_UPDATE_COMPATIBILITY;
                }

                @Override
                boolean requiresSystemAdmin()
                {
                    return true;
                }

                @Override
                OnDemandPermissionScheme getOnDemandPermissionScheme()
                {
                    return SYSADMIN_ONLY;
                }
            },

        // Audit log
        GET_AUDIT_LOG_PERMISSION
            {
                @Override
                Permission getPermission()
                {
                    return GET_AUDIT_LOG;
                }

                @Override
                boolean requiresSystemAdmin()
                {
                    return false;
                }

                @Override
                OnDemandPermissionScheme getOnDemandPermissionScheme()
                {
                    return SYSADMIN_OR_ADMIN;
                }
            },
        MANAGE_AUDIT_LOG_PERMISSION
            {
                @Override
                Permission getPermission()
                {
                    return MANAGE_AUDIT_LOG;
                }

                @Override
                boolean requiresSystemAdmin()
                {
                    return true;
                }

                @Override
                OnDemandPermissionScheme getOnDemandPermissionScheme()
                {
                    return SYSADMIN_ONLY;
                }
            },

        //license
        MANAGE_LICENSE_PERMISSION
            {
                @Override
                Permission getPermission()
                {
                    return MANAGE_PLUGIN_LICENSE;
                }

                @Override
                boolean requiresSystemAdmin()
                {
                    return false;
                }

                @Override
                OnDemandPermissionScheme getOnDemandPermissionScheme()
                {
                    return ALWAYS_DENIED;
                }
            },

        //notifications
        GET_NOTIFICATIONS_PERMISSION
            {
                @Override
                Permission getPermission()
                {
                    return GET_NOTIFICATIONS;
                }

                @Override
                boolean requiresSystemAdmin()
                {
                    return false;
                }

                @Override
                OnDemandPermissionScheme getOnDemandPermissionScheme()
                {
                    return ALWAYS_DENIED;
                }
            },
        MANAGE_NOTIFICATIONS_PERMISSION
            {
                @Override
                Permission getPermission()
                {
                    return MANAGE_NOTIFICATIONS;
                }

                @Override
                boolean requiresSystemAdmin()
                {
                    return false;
                }

                @Override
                OnDemandPermissionScheme getOnDemandPermissionScheme()
                {
                    return ALWAYS_DENIED;
                }
            },

        // Safe mode
        GET_SAFE_MODE_PERMISSION
            {
                @Override
                Permission getPermission()
                {
                    return GET_SAFE_MODE;
                }

                @Override
                boolean requiresSystemAdmin()
                {
                    return false;
                }

                @Override
                OnDemandPermissionScheme getOnDemandPermissionScheme()
                {
                    return SYSADMIN_OR_ADMIN;
                }
            },
        MANAGE_SAFE_MODE_PERMISSION
            {
                @Override
                Permission getPermission()
                {
                    return MANAGE_SAFE_MODE;
                }

                @Override
                boolean requiresSystemAdmin()
                {
                    return false;
                }

                @Override
                OnDemandPermissionScheme getOnDemandPermissionScheme()
                {
                    return SYSADMIN_OR_ADMIN;
                }
            };

        abstract Permission getPermission();

        abstract boolean requiresSystemAdmin();

        abstract OnDemandPermissionScheme getOnDemandPermissionScheme();
    }

    enum OnDemandPermissionScheme
    {
        ALWAYS_DENIED, SYSADMIN_ONLY, SYSADMIN_OR_ADMIN, SYSADMIN_OR_IF_USER_INSTALLED;
    }
}
