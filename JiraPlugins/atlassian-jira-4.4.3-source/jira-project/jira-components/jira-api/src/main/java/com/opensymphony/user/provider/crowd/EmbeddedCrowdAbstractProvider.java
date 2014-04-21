package com.opensymphony.user.provider.crowd;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.core.util.StaticCrowdServiceFactory;
import com.atlassian.jira.component.ComponentAccessor;
import com.opensymphony.user.provider.UserProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;

public abstract class EmbeddedCrowdAbstractProvider implements UserProvider
{
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Get a fresh version of the Crowd Read Write service from pico.
     * This is done because UserManager is a singleton and otherwise may cache a stale version, especially in func tests.
     *
     * @return fresh version of the Crowd Read Write service from pico.
     */
    protected CrowdService getCrowdService()
    {
        return ComponentAccessor.getComponentOfType(CrowdService.class);
    }

    public boolean create(final String name)
    {
        return true;
    }

    public void flushCaches()
    {
        // can't do much
    }

    public boolean init(final Properties properties)
    {
        // currently do no special initialisation
        return true;
    }

    public List<String> list()
    {
        return null;
    }

    public boolean remove(final String name)
    {
        return true;
    }
}
