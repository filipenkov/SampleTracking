package com.atlassian.applinks.core.rest.client;

import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.applinks.api.EntityLink;
import com.atlassian.applinks.api.EntityType;
import com.atlassian.applinks.spi.link.ReciprocalActionException;

public class MockEntityLinkClient extends EntityLinkClient
{
    public MockEntityLinkClient()
    {
        super(null, null);
    }

    @Override
    public void createEntityLinkFrom(final EntityLink entityLink, final EntityType localType, final String localKey) throws ReciprocalActionException, CredentialsRequiredException
    {
        // do nothing
    }

    @Override
    public void deleteEntityLinkFrom(final EntityLink remoteEntity, final EntityType localType, final String localKey) throws ReciprocalActionException, CredentialsRequiredException
    {
        // do nothing
    }
}
