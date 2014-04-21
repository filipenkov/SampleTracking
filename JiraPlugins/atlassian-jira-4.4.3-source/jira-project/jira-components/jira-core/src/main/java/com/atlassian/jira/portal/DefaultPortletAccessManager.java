package com.atlassian.jira.portal;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.portlet.PortletModuleDescriptor;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginAccessor;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * An implementation of PortletAccessManager
 *
 * @since v3.13
 */
public class DefaultPortletAccessManager implements PortletAccessManager
{
    private static final Logger log = Logger.getLogger(DefaultPortletAccessManager.class);
    private static final String SYSTEM_PORTLET_PACKAGE = "com.atlassian.jira.plugin.system.portlets";

    private final PermissionManager permissionManager;
    private final PluginAccessor pluginAccessor;

    public DefaultPortletAccessManager(final PermissionManager permissionManager, final PluginAccessor pluginAccessor)
    {
        Assertions.notNull("permissionManager", permissionManager);
        Assertions.notNull("pluginAccessor", pluginAccessor);

        this.permissionManager = permissionManager;
        this.pluginAccessor = pluginAccessor;
    }

    public Portlet getPortlet(final User user, final String portletKey)
    {
        Assertions.notNull("user", user);
        Assertions.notNull("portletKey", portletKey);

        Portlet portlet = getPortletImpl(portletKey);
        if ((portlet != null) && !canUserSeePortlet(user, portlet))
        {
            portlet = null;
        }
        return portlet;
    }

    @Override
    public Portlet getPortlet(com.opensymphony.user.User user, String portletKey)
    {
        return getPortlet((User) user, portletKey);
    }

    public Portlet getPortlet(final String portletKey)
    {
        Assertions.notNull("portletKey", portletKey);
        return getPortletImpl(portletKey);
    }

    public Collection<Portlet> getAllPortlets()
    {
        return pluginAccessor.getEnabledModulesByClass(Portlet.class);
    }

    public Collection<Portlet> getVisiblePortlets(final User user)
    {
        final Collection<Portlet> portlets = getAllPortlets();
        final List<Portlet> userPortlets = new ArrayList<Portlet>();

        for (final Portlet portlet : portlets)
        {
            if (canUserSeePortlet(user, portlet))
            {
                userPortlets.add(portlet);
            }
        }
        return userPortlets;
    }

    @Override
    public Collection<Portlet> getVisiblePortlets(com.opensymphony.user.User user)
    {
        return getVisiblePortlets((User) user);
    }

    public boolean canUserSeePortlet(final User user, final String portletKey)
    {
        final Portlet portlet = getPortlet(user, portletKey);
        return portlet != null;
    }

    @Override
    public boolean canUserSeePortlet(com.opensymphony.user.User user, String portletKey)
    {
        return canUserSeePortlet((User) user, portletKey);
    }

    public boolean canUserSeePortlet(final User user, final Portlet portlet)
    {
        Assertions.notNull("portlet", portlet);

        if (portlet.hasPermission())
        {
            if (Permissions.isGlobalPermission(portlet.getPermission()))
            {
                return permissionManager.hasPermission(portlet.getPermission(), user);
            }
            else
            {
                try
                {
                    return permissionManager.hasProjects(portlet.getPermission(), user);
                }
                catch (final Exception e)
                {
                    log.error(e, e);
                    return false;
                }
            }
        }
        else
        {
            return true;
        }
    }

    @Override
    public boolean canUserSeePortlet(com.opensymphony.user.User user, Portlet portlet)
    {
        return canUserSeePortlet((User) user, portlet);
    }

    private Portlet getPortletImpl(final String portletKey)
    {
        if (StringUtils.isEmpty(portletKey))
        {
            return null;
        }
        else
        {
            String id = portletKey;
            /*
            We might have an old portlet here - like 'PROJECTSTATS' that is now
            a system portlet loaded as a plugin. Let's try to be nice to old people
            and find their portlet!
            */
            if (id.indexOf(':') < 0)
            {
                id = SYSTEM_PORTLET_PACKAGE + ":" + id.toLowerCase();
            }

            final ModuleDescriptor descriptor = pluginAccessor.getEnabledPluginModule(id);

            if ((descriptor != null) && (descriptor instanceof PortletModuleDescriptor))
            {
                return (Portlet) descriptor.getModule();
            }
            else
            {
                log.debug("Could not find portlet with ID: " + id);
            }
        }

        return null;
    }
}
