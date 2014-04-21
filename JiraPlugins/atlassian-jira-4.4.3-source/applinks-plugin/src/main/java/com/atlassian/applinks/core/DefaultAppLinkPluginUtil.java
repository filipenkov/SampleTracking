package com.atlassian.applinks.core;

import com.atlassian.plugin.osgi.util.OsgiHeaderUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;

public class DefaultAppLinkPluginUtil implements AppLinkPluginUtil
{
    private final String pluginKey;
    private final Version version;

    public DefaultAppLinkPluginUtil(final BundleContext bundleContext)
    {
        final Bundle bundle = bundleContext.getBundle();
        pluginKey = OsgiHeaderUtil.getPluginKey(bundle);
        version = bundle.getVersion();
    }

    public String getPluginKey()
    {
        return pluginKey;
    }

    public Version getVersion()
    {
        return version;
    }
}
