package com.atlassian.applinks.core.auth.oauth;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.serviceprovider.ServiceProviderConsumerStore;
import com.atlassian.oauth.serviceprovider.ServiceProviderTokenStore;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DefaultServiceProviderStoreServiceTest extends TestCase
{
    private ServiceProviderStoreService providerStoreService;
    private ServiceProviderConsumerStore serviceProviderConsumerStore;
    private ServiceProviderTokenStore serviceProviderTokenStore;

    @Before
    protected void setUp() throws Exception
    {
        serviceProviderConsumerStore = mock(ServiceProviderConsumerStore.class);
        serviceProviderTokenStore = mock(ServiceProviderTokenStore.class);
        providerStoreService = new DefaultServiceProviderStoreService(serviceProviderConsumerStore, serviceProviderTokenStore);
    }

    @Test
    public void testAddConsumer() throws Exception
    {
        final Consumer consumer = Consumer.key("Refapp1").name("Atlassian Reference Application").signatureMethod(Consumer.SignatureMethod.HMAC_SHA1).build();
        final ApplicationLink applicationLink = mock(ApplicationLink.class);
        when(applicationLink.getProperty("oauth.incoming.consumerkey")).thenReturn(null);
        when(serviceProviderConsumerStore.get(consumer.getKey())).thenReturn(null);
        providerStoreService.addConsumer(consumer, applicationLink);
        verify(serviceProviderConsumerStore).put(consumer);
    }

    @Test
    public void testAddConsumerConsumerForApplinkExists() throws Exception
    {
        final Consumer consumer = Consumer.key("Refapp1").name("Atlassian Reference Application").signatureMethod(Consumer.SignatureMethod.HMAC_SHA1).build();
        final ApplicationLink applicationLink = mock(ApplicationLink.class);
        when(applicationLink.getProperty("oauth.incoming.consumerkey")).thenReturn("Refapp1");
        when(serviceProviderConsumerStore.get(consumer.getKey())).thenReturn(consumer);
        providerStoreService.addConsumer(consumer, applicationLink);
        verify(serviceProviderConsumerStore).put(consumer);
        verify(applicationLink).putProperty("oauth.incoming.consumerkey", consumer.getKey());
    }

    @Test
    public void testAddConsumerConsumerAlreadyRegistered() throws Exception
    {
        final Consumer consumer = Consumer.key("Refapp1").name("Atlassian Reference Application").signatureMethod(Consumer.SignatureMethod.HMAC_SHA1).build();
        final Consumer consumer2 = Consumer.key("Refapp2").name("Atlassian Reference Application").signatureMethod(Consumer.SignatureMethod.HMAC_SHA1).build();
        final ApplicationLink applicationLink = mock(ApplicationLink.class);
        when(applicationLink.getProperty("oauth.incoming.consumerkey")).thenReturn(consumer2.getKey());
        when(serviceProviderConsumerStore.get(consumer.getKey())).thenReturn(consumer);
        providerStoreService.addConsumer(consumer, applicationLink);
        // should accept the call to addConsumer() so we can upgrade old OAuth consumer reg's to UAL
    }

    @Test
    public void testRemoveConsumer() throws Exception
    {
        final Consumer consumer = Consumer.key("Refapp1").name("Atlassian Reference Application").signatureMethod(Consumer.SignatureMethod.HMAC_SHA1).build();
        when(serviceProviderConsumerStore.get(consumer.getKey())).thenReturn(consumer);
        final ApplicationLink applicationLink = mock(ApplicationLink.class);
        when(applicationLink.getProperty("oauth.incoming.consumerkey")).thenReturn(consumer.getKey());
        when(applicationLink.removeProperty("oauth.incoming.consumerkey")).thenReturn(consumer.getKey());
        providerStoreService.removeConsumer(applicationLink);
        verify(serviceProviderTokenStore).removeByConsumer(consumer.getKey());
        verify(serviceProviderConsumerStore).remove(consumer.getKey());
    }

    @Test
    public void testRemoveConsumerNoConsumerConfigured() throws Exception
    {
        final Consumer consumer = Consumer.key("Refapp1").name("Atlassian Reference Application").signatureMethod(Consumer.SignatureMethod.HMAC_SHA1).build();
        when(serviceProviderConsumerStore.get(consumer.getKey())).thenReturn(consumer);
        final ApplicationLink applicationLink = mock(ApplicationLink.class);
        when(applicationLink.getProperty("oauth.incoming.consumerkey")).thenReturn(null);
        try
        {
            providerStoreService.removeConsumer(applicationLink);
            fail("Store should have thrown an exception because no consumer is for this application link configured.");
        }
        catch (IllegalStateException e)
        {
            assertTrue(e.getMessage().startsWith("No consumer configured for application link 'Mock for ApplicationLink"));
        }
    }

}
