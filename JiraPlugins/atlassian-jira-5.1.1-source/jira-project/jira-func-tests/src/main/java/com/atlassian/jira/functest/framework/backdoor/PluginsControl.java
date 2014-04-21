package com.atlassian.jira.functest.framework.backdoor;

import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.sun.jersey.api.client.WebResource;

public class PluginsControl extends BackdoorControl<PluginsControl>
{
    public PluginsControl(JIRAEnvironmentData environmentData)
    {
        super(environmentData);
    }

    public void disablePlugin(final String pluginKey)
    {
        WebResource resource = createResource().path("plugins/disable")
                .queryParam("key", pluginKey);
        get(resource);
    }
}
