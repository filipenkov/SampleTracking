package com.atlassian.upm.pac;

import com.atlassian.upm.Sys;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

public class PacAuditClientImpl implements PacAuditClient
{
    private final PacServiceFactory factory;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public PacAuditClientImpl(final PacServiceFactory factory)
    {
        this.factory = checkNotNull(factory, "factory");
    }

    private boolean isPacDisabled()
    {
        return Sys.isPacDisabled();
    }

    public void logPluginDisabled(String key, String version)
    {
        if (!isPacDisabled())
        {
            try
            {
                factory.getAuditService().pluginDisabled(key, version);
            }
            catch (Exception e)
            {
                logger.warn("Unable to log pluginDisabled event to PAC: " + e);
            }
        }
    }
    
    public void logPluginUninstalled(String key, String version)
    {
        if (!isPacDisabled())
        {
            try
            {
                factory.getAuditService().pluginUninstalled(key, version);
            }
            catch (Exception e)
            {
                logger.warn("Unable to log pluginUninstalled event to PAC: " + e);
            }
        }
    }
}
