package com.atlassian.applinks.core.rest.client;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.spi.link.ReciprocalActionException;

public class MockApplicationLinkClient extends ApplicationLinkClient
{
    public MockApplicationLinkClient()
    {
        super(null, null);
    }

    @Override
    public void deleteReciprocalLinkFrom(final ApplicationLink link) throws ReciprocalActionException
    {
        // do nothing
    }
}
