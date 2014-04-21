package com.atlassian.crowd.directory;

import java.util.Collections;

import com.atlassian.crowd.exception.UnsupportedCrowdApiException;
import com.atlassian.crowd.model.group.Membership;
import com.atlassian.crowd.service.client.ClientProperties;
import com.atlassian.crowd.service.client.CrowdClient;
import com.atlassian.crowd.service.factory.CrowdClientFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RemoteCrowdDirectoryTest
{
    @Mock
    private CrowdClient client;
    
    private CrowdClientFactory factory = new CrowdClientFactory()
    {
        @Override
        public CrowdClient newInstance(ClientProperties clientProperties)
        {
            return client;
        }
        
        @Override
        public CrowdClient newInstance(String url, String applicationName, String applicationPassword)
        {
            return client;
        }
    };
    
    @Test
    public void getMembershipsUsesCrowdClientCall() throws Exception
    {
        Iterable<Membership> expected = Collections.emptyList();
        when(client.getMemberships()).thenReturn(expected);
        
        RemoteCrowdDirectory rcd = new RemoteCrowdDirectory(factory);
        rcd.setAttributes(Collections.<String, String>emptyMap());
        
        assertSame(expected, rcd.getMemberships());
        verify(client).getMemberships();
    }
    
    @Test
    public void getMembershipsFallsBackWhenApiNotAvailable() throws Exception
    {
        when(client.getMemberships()).thenThrow(new UnsupportedCrowdApiException("0", "testing"));
        
        RemoteCrowdDirectory rcd = new RemoteCrowdDirectory(factory);
        rcd.setAttributes(Collections.<String, String>emptyMap());
        Iterable<Membership> memberships = rcd.getMemberships();
        
        assertEquals(DirectoryMembershipsIterable.class, memberships.getClass());
    }
}
