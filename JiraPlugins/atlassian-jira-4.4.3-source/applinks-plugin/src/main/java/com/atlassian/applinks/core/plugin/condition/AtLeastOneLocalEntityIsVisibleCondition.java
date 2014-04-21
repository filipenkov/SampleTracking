package com.atlassian.applinks.core.plugin.condition;

import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.applinks.host.spi.InternalHostApplication;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;
import com.google.common.collect.Iterables;

import java.util.Map;

/** 
 * Condition that passes if there is at least one local entity visible to the currently logged in user.
 *
 * param: type - (optional) the fully qualified classname of a subclass of {@link ApplicationType}
 *
 * @since 3.0
 */
public class AtLeastOneLocalEntityIsVisibleCondition implements Condition
{
    private final InternalHostApplication hostApplication;

    public AtLeastOneLocalEntityIsVisibleCondition(final InternalHostApplication hostApplication)
    {
        this.hostApplication = hostApplication;
    }

    public void init(final Map<String, String> stringStringMap) throws PluginParseException
    {
    }

    public boolean shouldDisplay(final Map<String, Object> stringObjectMap)
    {
        return !Iterables.isEmpty(hostApplication.getLocalEntities());
    }
}
