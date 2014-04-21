package com.opensymphony.user.provider.crowd;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.user.UserPropertyManager;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.user.provider.ProfileProvider;

public class EmbeddedCrowdProfileProvider extends EmbeddedCrowdAbstractProvider implements ProfileProvider
{
    public PropertySet getPropertySet(final String name)
    {
        User user = getCrowdService().getUser(name);
        return getUserPropertyManager().getPropertySet(user);
    }

    private UserPropertyManager getUserPropertyManager()
    {
        return ComponentAccessor.getUserPropertyManager();
    }

    public boolean handles(final String name)
    {
        if (name == null)
        {
            return false;
        }
        try
        {
            User user = getCrowdService().getUser(name);
            return user != null;
        }
        catch (Exception e)
        {
            logger.error("Could not determine if we handle: " + name, e);
            return false;
        }
    }
}
