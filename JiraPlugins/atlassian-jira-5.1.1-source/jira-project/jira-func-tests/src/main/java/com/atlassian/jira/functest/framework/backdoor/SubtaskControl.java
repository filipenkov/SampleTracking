package com.atlassian.jira.functest.framework.backdoor;

import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.sun.jersey.api.client.WebResource;

/**
 * Allows you to enable/disable sub-tasks.
 *
 * @since v5.0.1
 */
public class SubtaskControl extends BackdoorControl<SubtaskControl>
{
    public SubtaskControl(JIRAEnvironmentData environmentData) 
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
        return createResource().path("subtask");
    }
}
