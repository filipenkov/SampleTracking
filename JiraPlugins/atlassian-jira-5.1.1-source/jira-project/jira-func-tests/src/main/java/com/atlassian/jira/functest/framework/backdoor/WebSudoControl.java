package com.atlassian.jira.functest.framework.backdoor;

import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.sun.jersey.api.client.WebResource;

/**
 * Simple control for Web Sudo.
 *
 * @since v5.0.1
 */
public class WebSudoControl extends BackdoorControl<WebSudoControl>
{
    public WebSudoControl(JIRAEnvironmentData environmentData)
    {
        super(environmentData);
    }

    public boolean isEnabled()
    {
        return get(createSubtaskResource(), Boolean.class);
    }

    public boolean enable()
    {
        return post(createSubtaskResource(), true, Boolean.class);
    }

    public boolean disable()
    {
        return post(createSubtaskResource(), false, Boolean.class);
    }

    private WebResource createSubtaskResource()
    {
        return createResource().path("websudo");
    }
}
