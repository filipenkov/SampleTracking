package com.atlassian.jira.oauth.serviceprovider;

import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.util.RSAKeys;
import junit.framework.TestCase;
import org.easymock.EasyMock;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import org.ofbiz.core.entity.GenericValue;

import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TestOfBizServiceProviderConsumerStore extends TestCase
{
    public void testPut() throws NoSuchAlgorithmException
    {
        final OfBizDelegator delegator = createMock(OfBizDelegator.class);

        final PublicKey key = RSAKeys.generateKeyPair().getPublic();
        final String keyString = RSAKeys.toPemEncoding(key);

        //try to get the google consumer as part of the first put().  Doesn't exist in the database yet!
        expect(delegator.findByAnd("OAuthServiceProviderConsumer", MapBuilder.<String, Object>newBuilder().add("consumerKey", "www.google.com").toMap())).
                andReturn(Collections.<GenericValue>emptyList());

        //create the google consumer since it doesn't exist yet.

        expect(delegator.createValue(eq("OAuthServiceProviderConsumer"), eqOfBizMapArg(MapBuilder.<String, Object>newBuilder().
                add("created", null).add("publicKey", keyString).add("description", "").
                add("consumerKey", "www.google.com").add("name", "iGoogle").add("callback", null).toMap()))).andReturn(null);

        //now we call get() the consumer exists in the data.
        expect(delegator.findByAnd("OAuthServiceProviderConsumer", MapBuilder.<String, Object>newBuilder().add("consumerKey", "www.google.com").toMap())).
                andReturn(CollectionBuilder.<GenericValue>newBuilder(
                        new MockGenericValue("OAuthServiceProviderConsumer",
                                MapBuilder.newBuilder().
                                        add("publicKey", keyString).add("description", "").add("callback", null).
                                        add("consumerKey", "www.google.com").add("name", "iGoogle").
                                        toMap())).asList());

        //try to get the refimpl consumer as part of the first put.  Doesn't exist in the database yet!
        expect(delegator.findByAnd("OAuthServiceProviderConsumer", MapBuilder.<String, Object>newBuilder().add("consumerKey", "refi:1212").toMap())).
                andReturn(Collections.<GenericValue>emptyList());
        //create the consumer in the database.
        expect(delegator.createValue(eq("OAuthServiceProviderConsumer"), eqOfBizMapArg(MapBuilder.<String, Object>newBuilder().
                add("created", null).add("publicKey", keyString).add("description", "This is a long description!!!").
                add("consumerKey", "refi:1212").add("name", "Refimpl").add("callback", "http://some.url.com/").toMap()))).andReturn(null);
        //now we call get().  The refimpl exists.
        expect(delegator.findByAnd("OAuthServiceProviderConsumer", MapBuilder.<String, Object>newBuilder().add("consumerKey", "refi:1212").toMap())).
                andReturn(CollectionBuilder.<GenericValue>newBuilder(
                        new MockGenericValue("OAuthServiceProviderConsumer",
                                MapBuilder.newBuilder().
                                        add("publicKey", keyString).add("description", "This is a long description!!!").add("callback", "http://some.url.com/").
                                        add("consumerKey", "refi:1212").add("name", "Refimpl").
                                        toMap())).asList());

        //update the google consumer.  This should call store on the GV rather than 'create'
        final MockGenericValue consumerGVtoUpdate = new MockGenericValue("OAuthServiceProviderConsumer",
                MapBuilder.newBuilder().
                        add("publicKey", keyString).add("description", "").add("callback", null).
                        add("consumerKey", "www.google.com").add("name", "iGoogle").
                        toMap());
        expect(delegator.findByAnd("OAuthServiceProviderConsumer", MapBuilder.<String, Object>newBuilder().add("consumerKey", "www.google.com").toMap())).
                andReturn(CollectionBuilder.<GenericValue>newBuilder(
                        consumerGVtoUpdate).asList());
        consumerGVtoUpdate.expectedFields = MapBuilder.<String, Object>newBuilder().add("consumerKey", "www.google.com").
                add("created", null).add("name", "MyGoogle").add("publicKey", keyString).add("description", "").add("callback", "http://my.url.com/").toMap();
        //finally get the updated consumer with its updated fields.
        expect(delegator.findByAnd("OAuthServiceProviderConsumer", MapBuilder.<String, Object>newBuilder().add("consumerKey", "www.google.com").toMap())).
                andReturn(CollectionBuilder.<GenericValue>newBuilder(
                        new MockGenericValue("OAuthServiceProviderConsumer",
                                MapBuilder.newBuilder().
                                        add("publicKey", keyString).add("description", "").add("callback", "http://my.url.com/").
                                        add("consumerKey", "www.google.com").add("name", "MyGoogle").
                                        toMap())).asList());

        expect(delegator.findByAnd("OAuthServiceProviderConsumer", MapBuilder.<String, Object>newBuilder().add("consumerKey", "blargh").toMap())).
                andReturn(Collections.<GenericValue>emptyList());

        replay(delegator);
        final OfBizServiceProviderConsumerStore store = new OfBizServiceProviderConsumerStore(delegator);

        try
        {
            store.put(null);
            fail("Should have thrown exception!");
        }
        catch (RuntimeException e)
        {
            //yay!
        }

        Consumer consumer = Consumer.key("www.google.com").name("iGoogle").publicKey(key).build();
        store.put(consumer);

        final Consumer google = store.get("www.google.com");
        assertConsumersEqual(consumer, google);

        Consumer another = Consumer.key("refi:1212").name("Refimpl").publicKey(key).callback(URI.create("http://some.url.com/")).description("This is a long description!!!").build();
        store.put(another);

        final Consumer refimpl = store.get("refi:1212");
        assertConsumersEqual(refimpl, another);

        //try updating an existing conumser
        Consumer myGoogle = Consumer.key(google.getKey()).name("MyGoogle").callback(URI.create("http://my.url.com/")).publicKey(key).build();
        store.put(myGoogle);
        Consumer updatedGoogle = store.get("www.google.com");
        assertConsumersEqual(myGoogle, updatedGoogle);
        assertTrue(consumerGVtoUpdate.storeCalled.get());

        try
        {
            store.get(null);
            fail("Should have thrown exception getting null!");
        }
        catch (RuntimeException e)
        {
            //yay!
        }

        final Consumer nullConsumer = store.get("blargh");
        assertNull(nullConsumer);

        verify(delegator);
    }

    public void testAllEmpty() throws NoSuchAlgorithmException
    {
        final OfBizDelegator delegator = createMock(OfBizDelegator.class);

        expect(delegator.findAll("OAuthServiceProviderConsumer")).andReturn(Collections.<GenericValue>emptyList());

        replay(delegator);
        final OfBizServiceProviderConsumerStore store = new OfBizServiceProviderConsumerStore(delegator);

        //test retrieving with nothing
        Iterable<Consumer> iterable = store.getAll();
        List<Consumer> list = convertIterableToList(iterable);
        assertEquals(0, list.size());

        verify(delegator);
    }

    public void testAll() throws NoSuchAlgorithmException
    {
        final OfBizDelegator delegator = createMock(OfBizDelegator.class);

        final PublicKey key = RSAKeys.generateKeyPair().getPublic();
        final String keyString = RSAKeys.toPemEncoding(key);

        expect(delegator.findAll("OAuthServiceProviderConsumer")).andReturn(
                CollectionBuilder.<GenericValue>newBuilder(
                        new MockGenericValue("OAuthServiceProviderConsumer", MapBuilder.newBuilder().
                                add("publicKey", keyString).add("description", null).add("callback", "http://my.url.com/").
                                add("consumerKey", "www.google.com").add("name", "MyGoogle").
                                toMap()),
                        new MockGenericValue("OAuthServiceProviderConsumer", MapBuilder.newBuilder().
                                add("publicKey", keyString).add("description", "some other service").add("callback", "http://www.refimpl/").
                                add("consumerKey", "refimpl:12112").add("name", "Refimpl").
                                toMap())).asList());

        Consumer consumer = Consumer.key("www.google.com").name("MyGoogle").publicKey(key).callback(URI.create("http://my.url.com/")).build();
        Consumer consumer2 = Consumer.key("refimpl:12112").name("Refimpl").publicKey(key).callback(URI.create("http://www.refimpl/")).description("some other service").build();

        replay(delegator);
        final OfBizServiceProviderConsumerStore store = new OfBizServiceProviderConsumerStore(delegator);

        //test retrieving with nothing
        Iterable<Consumer> iterable = store.getAll();
        List<Consumer> list = convertIterableToList(iterable);
        assertEquals(2, list.size());


        final Consumer result1 = list.get(0);
        final Consumer result2 = list.get(1);
        //ordering isn't guaranteed.
        if (result1.getKey().equals("www.google.com"))
        {
            assertConsumersEqual(consumer, result1);
            assertConsumersEqual(consumer2, result2);
        }
        else
        {
            assertConsumersEqual(consumer, result2);
            assertConsumersEqual(consumer2, result1);
        }

        verify(delegator);
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

    private static Map<String, Object> eqOfBizMapArg(Map<String, Object> in)
    {
        EasyMock.reportMatcher(new OfBizMapArgsEqual(in));
        return null;
    }

    private void assertConsumersEqual(Consumer expected, Consumer result)
    {
        assertEquals(expected.getKey(), result.getKey());
        String expectedDescrption = expected.getDescription();
        if(expected.getDescription() == null)
        {
            expectedDescrption = "";
        }
        assertEquals(expectedDescrption, result.getDescription());
        assertEquals(expected.getName(), result.getName());
        assertEquals(expected.getCallback(), result.getCallback());
        assertEquals(expected.getPublicKey(), result.getPublicKey());
    }

}
