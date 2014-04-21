package com.atlassian.jira.oauth.serviceprovider;

import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.serviceprovider.ServiceProviderConsumerStore;
import com.atlassian.oauth.util.RSAKeys;
import junit.framework.TestCase;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.verify;
import static org.easymock.EasyMock.replay;

import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

public class TestCachingServiceProviderConsumerStore extends TestCase
{
    public void testPut() throws NoSuchAlgorithmException
    {
        final PublicKey key = RSAKeys.generateKeyPair().getPublic();
        Consumer consumer = Consumer.key("www.google.com").name("iGoogle").publicKey(key).build();
        Consumer consumer2 = Consumer.key("refimpl").name("iGoogle").publicKey(key).build();

        final ServiceProviderConsumerStore mockConsumerStore = createMock(ServiceProviderConsumerStore.class);

        mockConsumerStore.put(consumer);
        //now only 1 call for get should go through to the delegate store.  All other calls should hit the cache!
        expect(mockConsumerStore.get("www.google.com")).andReturn(consumer).once();

        mockConsumerStore.put(consumer2);
        expect(mockConsumerStore.get("refimpl")).andReturn(consumer2).once();
        mockConsumerStore.put(consumer2);
        expect(mockConsumerStore.get("refimpl")).andReturn(consumer2).once();

        replay(mockConsumerStore);
        final CachingServiceProviderConsumerStore store = new CachingServiceProviderConsumerStore(mockConsumerStore);

        store.put(consumer);
        store.get("www.google.com");
        store.get("www.google.com");
        store.get("www.google.com");
        store.get("www.google.com");

        //now check put invalidates cache
        store.put(consumer2);
        store.get("refimpl");
        store.get("refimpl");
        store.put(consumer2);
        store.get("refimpl");
        store.get("refimpl");

        verify(mockConsumerStore);
    }

    public void testGetAllNotCached() throws NoSuchAlgorithmException
    {
        final PublicKey key = RSAKeys.generateKeyPair().getPublic();
        Consumer consumer = Consumer.key("www.google.com").name("iGoogle").publicKey(key).build();

        final ServiceProviderConsumerStore mockConsumerStore = createMock(ServiceProviderConsumerStore.class);

        List<Consumer> list = new ArrayList<Consumer>();
        list.add(consumer);
        Iterable<Consumer> result = new ArrayList<Consumer>(list);


        //now only 1 call for get should go through to the delegate store.  All other calls should hit the cache!
        expect(mockConsumerStore.getAll()).andReturn(result).times(3);

        replay(mockConsumerStore);
        final CachingServiceProviderConsumerStore store = new CachingServiceProviderConsumerStore(mockConsumerStore);

        store.getAll();
        store.getAll();
        store.getAll();

        verify(mockConsumerStore);
    }

    public void testRemoveNotImplemented() throws NoSuchAlgorithmException
    {
        final PublicKey key = RSAKeys.generateKeyPair().getPublic();
        Consumer consumer = Consumer.key("www.google.com").name("iGoogle").publicKey(key).build();

        final ServiceProviderConsumerStore mockConsumerStore = createMock(ServiceProviderConsumerStore.class);

        mockConsumerStore.put(consumer);
        //currently the call for get will result in another call to the delegate store to get the consumer.
        //what should really happen is that a call to remove is made to the delegate store first!
        expect(mockConsumerStore.get("www.google.com")).andReturn(consumer).times(1);

        mockConsumerStore.remove("www.google.com");

        expect(mockConsumerStore.get("www.google.com")).andReturn(null).times(1);

        replay(mockConsumerStore);
        final CachingServiceProviderConsumerStore store = new CachingServiceProviderConsumerStore(mockConsumerStore);

        store.put(consumer);
        Consumer updatedConsumer = store.get("www.google.com");
        assertNotNull(updatedConsumer);
        updatedConsumer = store.get("www.google.com");
        assertNotNull(updatedConsumer);
        updatedConsumer = store.get("www.google.com");
        assertNotNull(updatedConsumer);
        updatedConsumer = store.get("www.google.com");
        assertNotNull(updatedConsumer);

        store.remove("www.google.com");
        Consumer removedConsumer = store.get("www.google.com");
        assertNull(removedConsumer);

        verify(mockConsumerStore);
    }
}
