package com.atlassian.crowd.embedded.core.util;

import com.atlassian.crowd.embedded.api.CrowdService;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * <p>This class is a convenience class to access a {@link com.atlassian.crowd.embedded.api.CrowdService} in a static way.</p>
 * <p><strong>NB:</strong> it shouldn't be used in production code, and is here only to help with backward compatibility, i.e. with OS User and Atlassian User.</p>
 */
public class StaticCrowdServiceFactory implements CrowdServiceFactory
{
    /**
     * A static reference to the {@link com.atlassian.crowd.embedded.api.CrowdService}
     */
    private static CrowdService crowdService;

    public StaticCrowdServiceFactory(CrowdService crowdService)
    {
        this.crowdService = checkNotNull(crowdService);
    }

    public static CrowdService getCrowdService()
    {
        return crowdService;
    }
}
