package com.atlassian.jira.oauth.consumer;

import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.consumer.core.ConsumerServiceStore;
import com.atlassian.oauth.util.RSAKeys;
import junit.framework.TestCase;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.easymock.EasyMock.eq;
import org.easymock.EasyMock;
import org.ofbiz.core.entity.GenericValue;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TestOfBizConsumerStore extends TestCase
{

    public void testGet()
    {
        final OfBizDelegator mockDelegator = createMock(OfBizDelegator.class);

        final GenericValue consumerGv = new MockGenericValue("OAuthConsumer",
                MapBuilder.newBuilder()
                        .add("service", "google")
                        .add("consumerKey", "iGoogle")
                        .add("signatureMethod", "HMAC_SHA1")
                        .add("sharedSecret", "shhhh...")
                        .add("name", "myGoogle").toMap());

        final List<GenericValue> gvList = CollectionBuilder.newBuilder(consumerGv).asList();
        expect(mockDelegator.findByAnd("OAuthConsumer", MapBuilder.<String, Object>newBuilder()
                .add("service", "google").toMap())).andReturn(gvList);
        expect(mockDelegator.findByAnd("OAuthConsumer", MapBuilder.<String, Object>newBuilder()
                .add("service", "dontexist").toMap())).andReturn(Collections.<GenericValue>emptyList());

        replay(mockDelegator);

        OfBizConsumerStore store = new OfBizConsumerStore(mockDelegator);
        ConsumerServiceStore.ConsumerAndSecret cas = store.get("google");
        //should only contain one consumer.  The host conumser should not be part of this list.
        assertEquals("google", cas.getServiceName());
        assertEquals("iGoogle", cas.getConsumer().getKey());
        assertEquals("shhhh...", cas.getSharedSecret());
        assertEquals("myGoogle", cas.getConsumer().getName());

        ConsumerServiceStore.ConsumerAndSecret casnonexistant = store.get("dontexist");
        assertNull(casnonexistant);

        verify(mockDelegator);
    }

    public void testGetByKey()
    {
        final OfBizDelegator mockDelegator = createMock(OfBizDelegator.class);

        final GenericValue consumerGv = new com.atlassian.jira.mock.ofbiz.MockGenericValue("OAuthConsumer",
                MapBuilder.newBuilder()
                        .add("service", "google")
                        .add("consumerKey", "iGoogle")
                        .add("signatureMethod", "HMAC_SHA1")
                        .add("sharedSecret", "shhhh...")
                        .add("name", "myGoogle").toMap());

        final List<GenericValue> gvList = CollectionBuilder.newBuilder(consumerGv).asList();
        expect(mockDelegator.findByAnd("OAuthConsumer", MapBuilder.<String, Object>newBuilder()
                .add("consumerKey", "iGoogle").toMap())).andReturn(gvList);
        expect(mockDelegator.findByAnd("OAuthConsumer", MapBuilder.<String, Object>newBuilder()
                .add("consumerKey", "dontexist").toMap())).andReturn(Collections.<GenericValue>emptyList());

        replay(mockDelegator);

        OfBizConsumerStore store = new OfBizConsumerStore(mockDelegator);
        ConsumerServiceStore.ConsumerAndSecret cas = store.getByKey("iGoogle");
        //should only contain one consumer.  The host conumser should not be part of this list.
        assertEquals("google", cas.getServiceName());
        assertEquals("iGoogle", cas.getConsumer().getKey());
        assertEquals("shhhh...", cas.getSharedSecret());
        assertEquals("myGoogle", cas.getConsumer().getName());

        ConsumerServiceStore.ConsumerAndSecret casnonexistant = store.getByKey("dontexist");
        assertNull(casnonexistant);

        verify(mockDelegator);
    }

    public void testPutInvalid() throws NoSuchAlgorithmException
    {
        OfBizConsumerStore store = new OfBizConsumerStore(null);

        final KeyPair pair = RSAKeys.generateKeyPair();
        final Consumer consumer = Consumer.key("iGoogle").name("myGoogle").description("The googles").
                signatureMethod(Consumer.SignatureMethod.RSA_SHA1).publicKey(pair.getPublic()).build();
        ConsumerServiceStore.ConsumerAndSecret cas = new ConsumerServiceStore.ConsumerAndSecret("blah", consumer, pair.getPrivate());
        try
        {
            store.put(null, cas);
            fail("Should have thrown exception");
        }
        catch (Exception e)
        {
            //yay
        }

        try
        {
            store.put("myservice", null);
            fail("Should have thrown exception");
        }
        catch (Exception e)
        {
            //yay
        }
    }

    public void testPutNew() throws NoSuchAlgorithmException
    {
        final KeyPair pair = RSAKeys.generateKeyPair();
        final Consumer consumer = Consumer.key("iGoogle").name("myGoogle").description("The googles").
                signatureMethod(Consumer.SignatureMethod.RSA_SHA1).publicKey(pair.getPublic()).build();
        final ConsumerServiceStore.ConsumerAndSecret cas = new ConsumerServiceStore.ConsumerAndSecret("google", consumer, pair.getPrivate());

        final OfBizDelegator mockDelegator = createMock(OfBizDelegator.class);

        expect(mockDelegator.findByAnd("OAuthConsumer", MapBuilder.<String, Object>newBuilder().
                add("service", "google").toMap())).andReturn(Collections.<GenericValue>emptyList());
        final GenericValue consumerGv = new MockGenericValue("OAuthConsumer",
                MapBuilder.newBuilder()
                        .add("service", "google")
                        .add("consumerKey", "iGoogle")
                        .add("signatureMethod", "HMAC_SHA1")
                        .add("sharedSecret", "shhhh...")
                        .add("name", "myGoogle").toMap());

        expect(mockDelegator.createValue(eq("OAuthConsumer"), eqOfBizMapArg(MapBuilder.<String, Object>newBuilder()
                .add("service", "google")
                .add("consumerKey", "iGoogle")
                .add("signatureMethod", "RSA_SHA1")
                .add("name", "myGoogle")
                .add("publicKey", RSAKeys.toPemEncoding(pair.getPublic()))
                .add("privateKey", RSAKeys.toPemEncoding(pair.getPrivate()))
                .add("description", "The googles")
                .add("callback", null)
                .add("created", null)
                .toMap()))).andReturn(consumerGv);

        replay(mockDelegator);

        OfBizConsumerStore store = new OfBizConsumerStore(mockDelegator);
        store.put("google", cas);

        verify(mockDelegator);
    }

    public void testPutExisting() throws NoSuchAlgorithmException
    {
        final KeyPair pair = RSAKeys.generateKeyPair();
        final Consumer consumer = Consumer.key("iGoogle2").name("myGoogle2").description("The googles2").
                signatureMethod(Consumer.SignatureMethod.RSA_SHA1).publicKey(pair.getPublic()).build();
        final ConsumerServiceStore.ConsumerAndSecret cas = new ConsumerServiceStore.ConsumerAndSecret("google", consumer, pair.getPrivate());

        final OfBizDelegator mockDelegator = createMock(OfBizDelegator.class);

        final com.atlassian.jira.oauth.consumer.MockGenericValue consumerGv =
                new com.atlassian.jira.oauth.consumer.MockGenericValue("OAuthConsumer",
                        MapBuilder.newBuilder()
                                .add("service", "google")
                                .add("consumerKey", "iGoogle")
                                .add("signatureMethod", "HMAC_SHA1")
                                .add("sharedSecret", "shhhh...")
                                .add("name", "myGoogle").toMap());
        consumerGv.expectedFields = MapBuilder.<String, Object>newBuilder()
                .add("service", "google")
                .add("consumerKey", "iGoogle2")
                .add("signatureMethod", "RSA_SHA1")
                .add("name", "myGoogle2")
                .add("publicKey", RSAKeys.toPemEncoding(pair.getPublic()))
                .add("privateKey", RSAKeys.toPemEncoding(pair.getPrivate()))
                .add("description", "The googles2")
                .add("callback", null)
                .add("created", null)
                .toMap();

        expect(mockDelegator.findByAnd("OAuthConsumer", MapBuilder.<String, Object>newBuilder().
                add("service", "google").toMap())).andReturn(
                CollectionBuilder.newBuilder((GenericValue) consumerGv).asList());

        replay(mockDelegator);

        OfBizConsumerStore store = new OfBizConsumerStore(mockDelegator);
        store.put("google", cas);
        assertTrue(consumerGv.storeCalled.get());

        verify(mockDelegator);
    }

    public void testGetAllServiceProviders()
    {
        final OfBizDelegator mockDelegator = createMock(OfBizDelegator.class);

        final GenericValue hostGv = new MockGenericValue("OAuthConsumer",
                MapBuilder.newBuilder().add("service", "__HOST_SERVICE__").toMap());
        final GenericValue consumerGv = new MockGenericValue("OAuthConsumer",
                MapBuilder.newBuilder()
                        .add("service", "google")
                        .add("consumerKey", "iGoogle")
                        .add("signatureMethod", "HMAC_SHA1")
                        .add("sharedSecret", "shhhh...")
                        .add("name", "myGoogle").toMap());

        final List<GenericValue> gvList = CollectionBuilder.newBuilder(hostGv, consumerGv).asList();
        expect(mockDelegator.findAll("OAuthConsumer")).andReturn(gvList);

        replay(mockDelegator);

        OfBizConsumerStore store = new OfBizConsumerStore(mockDelegator);
        Iterable<Consumer> allServiceProviders = store.getAllServiceProviders();
        //should only contain one consumer.  The host conumser should not be part of this list.
        Iterator<Consumer> iterator = allServiceProviders.iterator();
        assertEquals("myGoogle", iterator.next().getName());
        assertFalse(iterator.hasNext());

        verify(mockDelegator);
    }

    private static Map<String, Object> eqOfBizMapArg(Map<String, Object> in)
    {
        EasyMock.reportMatcher(new OfBizMapArgsEqual(in));
        return null;
    }

}
