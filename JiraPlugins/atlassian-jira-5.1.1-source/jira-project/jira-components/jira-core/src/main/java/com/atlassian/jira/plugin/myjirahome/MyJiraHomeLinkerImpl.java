package com.atlassian.jira.plugin.myjirahome;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.atlassian.plugin.web.model.WebLink;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;

/**
 * Resolves the current My JIRA Home location by looking up the plugin and returning the rendered url. If the plugin is
 * not enabled, the {@link #DEFAULT_HOME} is returned.
 *
 * @since 5.1
 */
public class MyJiraHomeLinkerImpl implements MyJiraHomeLinker
{
    private final PluginAccessor pluginAccessor;
    private final MyJiraHomePreference myJiraHomePreference;

    public MyJiraHomeLinkerImpl(@Nonnull final PluginAccessor pluginAccessor, @Nonnull final MyJiraHomePreference myJiraHomePreference)
    {
        this.pluginAccessor = pluginAccessor;
        this.myJiraHomePreference = myJiraHomePreference;
    }

    @Nonnull
    @Override
    public String getHomeLink(@Nullable final User user)
    {
        final String completePluginModuleKey = myJiraHomePreference.findHome(user);
        try
        {
            if (!pluginAccessor.isPluginModuleEnabled(completePluginModuleKey))
            {
                return DEFAULT_HOME;
            }

            final WebLink link = getWebLinkFromWebItemModuleDescriptor(completePluginModuleKey);
            if (link != null)
            {
                return link.getRenderedUrl(Collections.<String, Object>emptyMap());
            }
            else
            {
                return DEFAULT_HOME;
            }
        }
        catch (IllegalArgumentException e)
        {
            return DEFAULT_HOME;
        }
    }

    @Nullable
    private WebLink getWebLinkFromWebItemModuleDescriptor(@Nonnull final String completePluginModuleKey)
    {
        final WebItemModuleDescriptor webItemModuleDescriptor = getWebItemModuleDescriptorFromKey(completePluginModuleKey);
        if (webItemModuleDescriptor != null)
        {
            return webItemModuleDescriptor.getLink();
        }
        else
        {
            return null;
        }
    }

    @Nullable
    private WebItemModuleDescriptor getWebItemModuleDescriptorFromKey(@Nonnull final String completePluginModuleKey)
    {
        final ModuleDescriptor<?> pluginModule = pluginAccessor.getPluginModule(completePluginModuleKey);
        if (pluginModule instanceof WebItemModuleDescriptor)
        {
            return (WebItemModuleDescriptor) pluginModule;
        }
        else
        {
            return null;
        }
    }
}
