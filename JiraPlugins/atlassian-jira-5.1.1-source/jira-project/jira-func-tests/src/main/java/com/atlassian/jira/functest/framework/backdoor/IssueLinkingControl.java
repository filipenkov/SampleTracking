package com.atlassian.jira.functest.framework.backdoor;

import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.sun.jersey.api.client.WebResource;

/**
 * Allows you to enable/disable issue links.
 *
 * @since v5.0.4
 */
public class IssueLinkingControl extends BackdoorControl<IssueLinkingControl>
{
    public IssueLinkingControl(JIRAEnvironmentData environmentData)
    {
        super(environmentData);
    }
    
    public boolean isEnabled()
    {
        return get(createResource(), Boolean.class);
    }

    public boolean enable()
    {
        return post(createResource(), true, Boolean.class);
    }

    public boolean disable()
    {
        return post(createResource(), false, Boolean.class);
    }

    @Override
    public WebResource createResource()
    {
        return super.createResource().path("issueLinking");
    }

    public void createIssueLinkType(String name, String outward, String inward) {
        get(createResource().path("create")
                .queryParam("name", name)
                .queryParam("outward", outward)
                .queryParam("inward", inward)
        );
    }

    public void createIssueLinkType(String name, String outward, String inward, String style)
    {
        get(createResource().path("create")
                .queryParam("name", name)
                .queryParam("outward", outward)
                .queryParam("inward", inward)
                .queryParam("style", style)
        );
    }
}
