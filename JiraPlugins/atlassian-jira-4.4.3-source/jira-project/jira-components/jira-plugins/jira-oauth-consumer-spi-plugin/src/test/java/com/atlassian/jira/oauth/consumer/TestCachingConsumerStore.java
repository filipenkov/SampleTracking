package com.atlassian.jira.oauth.consumer;

import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.consumer.core.ConsumerServiceStore;
import com.atlassian.oauth.util.RSAKeys;
import junit.framework.TestCase;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

public class TestCachingConsumerStore extends TestCase
{

    public void testGet() throws NoSuchAlgorithmException
    {
        final ConsumerServiceStore delegateStore = createMock(ConsumerServiceStore.class);

        Consumer consumer = Consumer.key("www.google.com")
                .name("iGoogle")
                .description("Google home page")
                .callback(URI.create("http://www.google.com"))
                .signatureMethod(Consumer.SignatureMethod.HMAC_SHA1)
                .build();

        ConsumerServiceStore.ConsumerAndSecret cas = new ConsumerServiceStore.ConsumerAndSecret("myGoogleService", consumer, "shared secret string");

        expect(delegateStore.get("nonexistent")).andReturn(null).times(2);
        expect(delegateStore.get("myGoogleService")).andReturn(cas).once();
        expect(delegateStore.getByKey("somenonexistentkey!")).andReturn(null).once();

        replay(delegateStore);
        CachingConsumerStore cachingStore = new CachingConsumerStore(delegateStore);

        try
        {
            cachingStore.get(null);
            fail("should have thrown exception");
        }
        catch (RuntimeException e)
        {
            //yay
        }

        //this should not got into the cache, and we should keep hitting the underlying DB store.
        ConsumerServiceStore.ConsumerAndSecret casNonExistent = cachingStore.get("nonexistent");
        assertNull(casNonExistent);
        casNonExistent = cachingStore.get("nonexistent");
        assertNull(casNonExistent);

        //this should not got into the cache, and we should keep hitting the underlying DB store.
        ConsumerServiceStore.ConsumerAndSecret returnedCas = cachingStore.get("myGoogleService");
        assertCasEquals(consumer, returnedCas);
        //this call should be cached!
        returnedCas = cachingStore.get("myGoogleService");
        assertCasEquals(consumer, returnedCas);

        // this call should also be cached by now!
        returnedCas = cachingStore.getByKey("www.google.com");
        assertCasEquals(consumer, returnedCas);

        final ConsumerServiceStore.ConsumerAndSecret cacheMissCas = cachingStore.getByKey("somenonexistentkey!");
        assertNull(cacheMissCas);

        verify(delegateStore);
    }
//
//    public void testPut()
//    {
//        final ConsumerServiceStore delegateStore = createMock(ConsumerServiceStore.class);
//
//        Consumer consumer = Consumer.key("www.google.com")
//                .name("iGoogle")
//                .description("Google home page")
//                .callback(URI.create("http://www.google.com"))
//                .signatureMethod(Consumer.SignatureMethod.HMAC_SHA1)
//                .build();
//
//        ConsumerServiceStore.ConsumerAndSecret cas = new ConsumerServiceStore.ConsumerAndSecret("myGoogleService", consumer, "shared secret string");
//
//        delegateStore.put("myGoogleService", cas);
//        expect(delegateStore.get("myGoogleService")).andReturn(cas).once();
//
//        delegateStore.put("myGoogleService", cas);
//        expect(delegateStore.getByKey("www.google.com")).andReturn(cas).times(2);
//
//        expect(delegateStore.get("myGoogleService")).andReturn(cas).once();
//
//        replay(delegateStore);
//        CachingConsumerStore cachingStore = new CachingConsumerStore(delegateStore);
//
//        try
//        {
//            cachingStore.put(null, null);
//            fail("should have thrown exception");
//        }
//        catch (RuntimeException e)
//        {
//            //yay
//        }
//
//        cachingStore.put("myGoogleService", cas);
//        //this should hit the delegate store
//        ConsumerServiceStore.ConsumerAndSecret returnedCas = cachingStore.get("myGoogleService");
//        assertCasEquals(consumer, returnedCas);
//        //this should be cached.
//        returnedCas = cachingStore.get("myGoogleService");
//        assertCasEquals(consumer, returnedCas);
//        //this should also be cached!
//        returnedCas = cachingStore.getByKey("www.google.com");
//        assertCasEquals(consumer, returnedCas);
//
//        //now update the same entry.  This should clear the cache.
//        cachingStore.put("myGoogleService", cas);
//        //no longer cached
//        returnedCas = cachingStore.getByKey("www.google.com");
//        assertCasEquals(consumer, returnedCas);
//        //still not cached since we need the service name in order to cache!
//        returnedCas = cachingStore.getByKey("www.google.com");
//        assertCasEquals(consumer, returnedCas);
//        //Now we're cached.
//        returnedCas = cachingStore.get("myGoogleService");
//        assertCasEquals(consumer, returnedCas);
//        returnedCas = cachingStore.get("myGoogleService");
//        assertCasEquals(consumer, returnedCas);
//        returnedCas = cachingStore.getByKey("www.google.com");
//        assertCasEquals(consumer, returnedCas);
//
//        verify(delegateStore);
//    }

    public void testGetAll() throws NoSuchAlgorithmException
    {
        final ConsumerServiceStore delegateStore = createMock(ConsumerServiceStore.class);

        final PublicKey key = RSAKeys.generateKeyPair().getPublic();
        Consumer consumer = Consumer.key("www.google.com")
                .name("iGoogle")
                .description("Google home page")
                .callback(URI.create("http://www.google.com"))
                .signatureMethod(Consumer.SignatureMethod.RSA_SHA1)
                .publicKey(key).build();
        Consumer consumer2 = Consumer.key("refimpl")
                .name("Refimpl")
                .signatureMethod(Consumer.SignatureMethod.RSA_SHA1)
                .publicKey(key).build();

        expect(delegateStore.getAllServiceProviders()).andReturn(
                CollectionBuilder.<Consumer>newBuilder(consumer, consumer2).asList()
        );
        replay(delegateStore);
        CachingConsumerStore cachingStore = new CachingConsumerStore(delegateStore);
        final Iterable<Consumer> iterable = cachingStore.getAllServiceProviders();
        final List<Consumer> list = convertIterableToList(iterable);
        assertEquals(2, list.size());

        assertConsumersEqual(consumer, list.get(0));
        assertConsumersEqual(consumer2, list.get(1));

        verify(delegateStore);
    }

    private <T> List<T> convertIterableToList(final Iterable<T> iterable)
    {
        final List<T> ret = new ArrayList<T>();
        for (T entry : iterable)
        {
            ret.add(entry);
        }
        return ret;
    }

    private void assertConsumersEqual(Consumer expected, Consumer result)
    {
        assertEquals(expected.getKey(), result.getKey());
        assertEquals(expected.getDescription(), result.getDescription());
        assertEquals(expected.getName(), result.getName());
        assertEquals(expected.getCallback(), result.getCallback());
        assertEquals(expected.getPublicKey(), result.getPublicKey());
    }

    private void assertCasEquals(final Consumer consumer, final ConsumerServiceStore.ConsumerAndSecret returnedCas)
    {
        assertConsumersEqual(consumer, returnedCas.getConsumer());
        assertEquals("shared secret string", returnedCas.getSharedSecret());
        assertNull(returnedCas.getPrivateKey());
    }
}
