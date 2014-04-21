package com.atlassian.applinks.core;

import org.osgi.framework.Version;

public interface AppLinkPluginUtil
{

    /**
     * @return the AppLinks plugin key
     */
    String getPluginKey();

    /**
     * @return the {@link Version} of the AppLinks plugin
     */
    Version getVersion();
}
