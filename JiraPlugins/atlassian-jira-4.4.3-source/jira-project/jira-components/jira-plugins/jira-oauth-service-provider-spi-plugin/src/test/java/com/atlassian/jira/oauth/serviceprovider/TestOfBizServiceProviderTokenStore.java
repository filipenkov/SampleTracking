package com.atlassian.jira.oauth.serviceprovider;

import com.atlassian.jira.local.testutils.MultiTenantContextTestUtils;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizListIterator;
import com.atlassian.jira.propertyset.JiraPropertySetFactory;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.util.OSUserConverter;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.serviceprovider.ServiceProviderConsumerStore;
import com.atlassian.oauth.serviceprovider.ServiceProviderToken;
import com.atlassian.oauth.serviceprovider.StoreException;
import com.atlassian.oauth.util.RSAKeys;
import com.opensymphony.module.propertyset.PropertySet;
import junit.framework.TestCase;
import org.ofbiz.core.entity.GenericModelException;
import org.ofbiz.core.entity.GenericValue;

import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PublicKey;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.atlassian.oauth.serviceprovider.ServiceProviderToken.Version;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reportMatcher;
import static org.easymock.EasyMock.verify;

public class TestOfBizServiceProviderTokenStore extends TestCase
{
    private Consumer consumer;
    private Principal user;


    protected void setUp() throws Exception
    {
        super.setUp();
        MultiTenantContextTestUtils.setupMultiTenantSystem();
        final PublicKey key = RSAKeys.generateKeyPair().getPublic();
        consumer = Consumer.key("www.google.com").name("iGoogle").publicKey(key).build();
        user = new Principal()
        {
            public String getName()
            {
                return "admin";
            }
        };
    }

    public void testPut() throws NoSuchAlgorithmException
    {
        final OfBizDelegator mockDelegator = createMock(OfBizDelegator.class);
        final UserUtil mockUserUtil = createMock(UserUtil.class);
        final ServiceProviderConsumerStore mockConsumerStore = createMock(ServiceProviderConsumerStore.class);
        final JiraPropertySetFactory mockPropertySetFactory = createMock(JiraPropertySetFactory.class);

        final PropertySet mockPs = createMock(PropertySet.class);
        final PropertySet mockPs2 = createMock(PropertySet.class);

        //first storing an ACCESS token
        expect(mockDelegator.findByAnd("OAuthServiceProviderToken", MapBuilder.<String, Object>newBuilder().
                add("token", "23134123412413").toMap())).
                andReturn(Collections.<GenericValue>emptyList());
        expect(mockDelegator.createValue(eq("OAuthServiceProviderToken"), eqOfBizMapArg(MapBuilder.<String, Object>newBuilder().
                add("created", null).
                add("callback", null).
                add("ttl", 2592000000L).
                add("auth", "AUTHORIZED").
                add("token", "23134123412413").
                add("verifier", null).
                add("tokenSecret", "adfasdfasdfsdf").
                add("tokenType", "ACCESS").
                add("consumerKey", "www.google.com").
                add("username", "admin").
                add("version", null).
                toMap()))).andReturn(new MockGenericValue("OAuthServiceProviderToken", MapBuilder.<String, Object>newBuilder().add("id", 10000L).toMap()));
        expect(mockPropertySetFactory.buildCachingPropertySet("OAuthServiceProviderToken", 10000L, true)).
                andReturn(mockPs).once();

        expect(mockPs.getKeys()).andReturn(Collections.emptyList()).once();
        mockPs.setText("prop1", "val1");
        mockPs.setText("prop2", "val2");

        //then we store a REQUEST token
        expect(mockDelegator.findByAnd("OAuthServiceProviderToken", MapBuilder.<String, Object>newBuilder().
                add("token", "44444444").toMap())).
                andReturn(Collections.<GenericValue>emptyList());
        expect(mockDelegator.createValue(eq("OAuthServiceProviderToken"), eqOfBizMapArg(MapBuilder.<String, Object>newBuilder().
                add("created", null).
                add("ttl", 600000L).
                add("token", "44444444").
                add("callback", null).
                add("tokenSecret", "adfasdfasdfsdf").
                add("tokenType", "REQUEST").
                add("consumerKey", "www.google.com").
                add("verifier", "sssh...something secret").
                add("auth", "AUTHORIZED").
                add("username", "admin").
                add("version", "V_1_0_A").
                toMap()))).andReturn(new MockGenericValue("OAuthServiceProviderToken", MapBuilder.<String, Object>newBuilder().add("id", 10010L).toMap()));
        expect(mockPropertySetFactory.buildCachingPropertySet("OAuthServiceProviderToken", 10010L, true)).
                andReturn(mockPs2).once();
        expect(mockPs2.getKeys()).andReturn(Collections.emptyList()).once();

        replay(mockDelegator, mockUserUtil, mockConsumerStore, mockPropertySetFactory, mockPs, mockPs2);
        final AtomicBoolean getCalled = new AtomicBoolean(false);
        final OfBizServiceProviderTokenStore store = new OfBizServiceProviderTokenStore(mockDelegator, mockUserUtil, mockConsumerStore, mockPropertySetFactory)
        {
            @Override
            public ServiceProviderToken get(final String token) throws StoreException
            {
                //after the put we read it back from the DB  don't really care what it returns, just that it gets
                //called!
                getCalled.set(true);
                return null;
            }
        };
        try
        {
            store.put(null);
            fail("Should have thrown exception!");
        }
        catch (RuntimeException e)
        {
            //yay!
        }

        final Map<String, String> props = MapBuilder.<String, String>newBuilder().add("prop1", "val1").add("prop2", "val2").toMap();
        final ServiceProviderToken token = ServiceProviderToken.newAccessToken("23134123412413").
                tokenSecret("adfasdfasdfsdf").
                consumer(consumer).
                authorizedBy(user).
                properties(props).build();

        //lets try a token without props
        final ServiceProviderToken token2 = ServiceProviderToken.newRequestToken("44444444").
                tokenSecret("adfasdfasdfsdf").
                consumer(consumer).
                verifier("sssh...something secret").
                version(Version.V_1_0_A).
                authorizedBy(user).build();

        store.put(token);
        store.put(token2);
        assertTrue(getCalled.get());

        verify(mockDelegator, mockUserUtil, mockConsumerStore, mockPropertySetFactory, mockPs, mockPs2);
    }

    public void testGet()
    {
        final OfBizDelegator mockDelegator = createMock(OfBizDelegator.class);
        final UserUtil mockUserUtil = createMock(UserUtil.class);
        final ServiceProviderConsumerStore mockConsumerStore = createMock(ServiceProviderConsumerStore.class);
        final JiraPropertySetFactory mockPropertySetFactory = createMock(JiraPropertySetFactory.class);

        final PropertySet mockPs = createMock(PropertySet.class);
        final PropertySet mockPs2 = createMock(PropertySet.class);

        expect(mockDelegator.findByAnd("OAuthServiceProviderToken", MapBuilder.<String, Object>newBuilder().
                add("token", "nonexistant").toMap())).
                andReturn(Collections.<GenericValue>emptyList());

        expect(mockDelegator.findByAnd("OAuthServiceProviderToken", MapBuilder.<String, Object>newBuilder().
                add("token", "23134123412413").toMap())).
                andReturn(CollectionBuilder.<GenericValue>newBuilder(
                        new MockGenericValue("OAuthServiceProviderToken", MapBuilder.<String, Object>newBuilder().
                                add("id", 10000L).
                                add("created", new Timestamp(System.currentTimeMillis())).
                                add("token", "23134123412413").
                                add("tokenSecret", "adfasdfasdfsdf").
                                add("tokenType", "ACCESS").
                                add("consumerKey", "www.google.com").
                                add("username", "admin").
                                add("ttl", 2592000000L).
                                add("version", "V_1_0_A").
                                toMap())).asList());
        expect(mockConsumerStore.get("www.google.com")).andReturn(consumer).once();
        expect(mockPropertySetFactory.buildCachingPropertySet("OAuthServiceProviderToken", 10000L, true)).
                andReturn(mockPs).once();
        expect(mockPs.getKeys()).andReturn(CollectionBuilder.newBuilder("prop1", "prop2").asList()).once();
        expect(mockPs.getText("prop1")).andReturn("val1").once();
        expect(mockPs.getText("prop2")).andReturn("val2").once();

        replay(mockDelegator, mockUserUtil, mockConsumerStore, mockPropertySetFactory, mockPs, mockPs2);
        final OfBizServiceProviderTokenStore store = new OfBizServiceProviderTokenStore(mockDelegator, mockUserUtil, mockConsumerStore, mockPropertySetFactory)
        {
            @Override
            Principal getUser(final String username)
            {
                return user;
            }
        };
        try
        {
            store.get(null);
            fail("Should have thrown exception!");
        }
        catch (RuntimeException e)
        {
            //yay!
        }

        final Map<String, String> props = MapBuilder.<String, String>newBuilder().add("prop1", "val1").add("prop2", "val2").toMap();
        final ServiceProviderToken token = ServiceProviderToken.newAccessToken("23134123412413").
                tokenSecret("adfasdfasdfsdf").
                consumer(consumer).
                authorizedBy(user).
                version(Version.V_1_0_A).
                properties(props).build();

        //lets try a token without props
        final ServiceProviderToken token2 = ServiceProviderToken.newRequestToken("44444444").
                tokenSecret("adfasdfasdfsdf").
                consumer(consumer).
                verifier("sssh...something secret").
                version(Version.V_1_0_A).
                authorizedBy(user).build();

        final ServiceProviderToken resultToken = store.get("nonexistant");
        assertNull(resultToken);

        final ServiceProviderToken accessToken = store.get("23134123412413");
        assertTokenEquals(token, accessToken);

        verify(mockDelegator, mockUserUtil, mockConsumerStore, mockPropertySetFactory, mockPs, mockPs2);
    }

    public void testGetAccessTokensForUserNone()
    {
        final OfBizDelegator mockDelegator = createMock(OfBizDelegator.class);
        final UserUtil mockUserUtil = createMock(UserUtil.class);
        final ServiceProviderConsumerStore mockConsumerStore = createMock(ServiceProviderConsumerStore.class);
        final JiraPropertySetFactory mockPropertySetFactory = createMock(JiraPropertySetFactory.class);

        expect(mockDelegator.findByAnd("OAuthServiceProviderToken", MapBuilder.<String, Object>newBuilder()
                .add("username", "dontexist")
                .add("tokenType", "ACCESS")
                .toMap())).andReturn(Collections.<GenericValue>emptyList());

        replay(mockDelegator, mockUserUtil, mockConsumerStore, mockPropertySetFactory);
        final OfBizServiceProviderTokenStore store = new OfBizServiceProviderTokenStore(mockDelegator, mockUserUtil, mockConsumerStore, mockPropertySetFactory);

        Iterable<ServiceProviderToken> accessTokensForUser = store.getAccessTokensForUser("dontexist");
        assertFalse(accessTokensForUser.iterator().hasNext());

        verify(mockDelegator, mockUserUtil, mockConsumerStore, mockPropertySetFactory);
    }

    public void testGetAccessTokensForUser()
    {
        final OfBizDelegator mockDelegator = createMock(OfBizDelegator.class);
        final UserUtil mockUserUtil = createMock(UserUtil.class);
        final ServiceProviderConsumerStore mockConsumerStore = createMock(ServiceProviderConsumerStore.class);
        final JiraPropertySetFactory mockPropertySetFactory = createMock(JiraPropertySetFactory.class);
        final PropertySet mockPs = createMock(PropertySet.class);

        expect(mockPs.getKeys()).andReturn(Collections.emptyList());

        expect(mockUserUtil.getUser("admin")).andReturn(OSUserConverter.convertToOSUser(new MockUser("admin")));

        expect(mockConsumerStore.get("www.google.com")).andReturn(consumer);

        expect(mockPropertySetFactory.buildCachingPropertySet("OAuthServiceProviderToken", 10000L, true)).andReturn(mockPs);

        expect(mockDelegator.findByAnd("OAuthServiceProviderToken", MapBuilder.<String, Object>newBuilder()
                .add("username", "admin")
                .add("tokenType", "ACCESS")
                .toMap())).
                andReturn(CollectionBuilder.<GenericValue>newBuilder(new MockGenericValue("OAuthServiceProviderToken", MapBuilder.<String, Object>newBuilder().
                        add("id", 10000L).
                        add("created", new Timestamp(System.currentTimeMillis())).
                        add("token", "23134123412413").
                        add("tokenSecret", "adfasdfasdfsdf").
                        add("tokenType", "ACCESS").
                        add("consumerKey", "www.google.com").
                        add("username", "admin").
                        add("ttl", 604800000L).
                        toMap())).asList());

        replay(mockDelegator, mockUserUtil, mockConsumerStore, mockPropertySetFactory, mockPs);
        final OfBizServiceProviderTokenStore store = new OfBizServiceProviderTokenStore(mockDelegator, mockUserUtil, mockConsumerStore, mockPropertySetFactory);

        Iterable<ServiceProviderToken> accessTokensForUser = store.getAccessTokensForUser("admin");
        assertTrue(accessTokensForUser.iterator().hasNext());
        ServiceProviderToken token = accessTokensForUser.iterator().next();
        assertEquals("admin", token.getUser().getName());
        assertEquals("23134123412413", token.getToken());
        assertEquals("adfasdfasdfsdf", token.getTokenSecret());
        assertTrue(token.isAccessToken());
        assertEquals("www.google.com", token.getConsumer().getKey());

        verify(mockDelegator, mockUserUtil, mockConsumerStore, mockPropertySetFactory, mockPs);
    }

    public void testRemoveExpiredTokens() throws GenericModelException
    {
        final OfBizDelegator mockDelegator = createMock(OfBizDelegator.class);
        final UserUtil mockUserUtil = createMock(UserUtil.class);
        final ServiceProviderConsumerStore mockConsumerStore = createMock(ServiceProviderConsumerStore.class);
        final JiraPropertySetFactory mockPropertySetFactory = createMock(JiraPropertySetFactory.class);

        final OfBizListIterator listIterator = createMock(OfBizListIterator.class);
        expect(listIterator.next()).andReturn(new MockGenericValue("OAuthServiceProviderToken", MapBuilder.newBuilder()
                .add("id", 10000L)
                .add("created", new Timestamp(System.currentTimeMillis() - 1800))
                .add("ttl", 3600L).toMap()));
        //expired
        expect(listIterator.next()).andReturn(new MockGenericValue("OAuthServiceProviderToken", MapBuilder.newBuilder()
                .add("id", 10010L)
                .add("created", new Timestamp(System.currentTimeMillis() - 3800))
                .add("ttl", 3600L).toMap()));
        //expired
        expect(listIterator.next()).andReturn(new MockGenericValue("OAuthServiceProviderToken", MapBuilder.newBuilder()
                .add("id", 10020L)
                .add("created", new Timestamp(System.currentTimeMillis() - 2500))
                .add("ttl", 2000L).toMap()));
        expect(listIterator.next()).andReturn(null);
        listIterator.close();

        expect(mockDelegator.findListIteratorByCondition("OAuthServiceProviderToken", null, null,
                CollectionBuilder.newBuilder("id", "created", "ttl").asList(), null, null)).andReturn(listIterator);

        expect(mockDelegator.removeByOr("OAuthServiceProviderToken", "id", CollectionBuilder.newBuilder(10010L, 10020L).asList())).andReturn(2);

        replay(mockDelegator, mockUserUtil, mockConsumerStore, mockPropertySetFactory, listIterator);
        final OfBizServiceProviderTokenStore store = new OfBizServiceProviderTokenStore(mockDelegator, mockUserUtil, mockConsumerStore, mockPropertySetFactory);

        store.removeExpiredTokens();

        verify(mockDelegator, mockUserUtil, mockConsumerStore, mockPropertySetFactory, listIterator);
    }


    public void testRemove()
    {
        final OfBizDelegator mockDelegator = createMock(OfBizDelegator.class);
        final UserUtil mockUserUtil = createMock(UserUtil.class);
        final ServiceProviderConsumerStore mockConsumerStore = createMock(ServiceProviderConsumerStore.class);
        final JiraPropertySetFactory mockPropertySetFactory = createMock(JiraPropertySetFactory.class);

        final PropertySet mockPs = createMock(PropertySet.class);
        final PropertySet mockPs2 = createMock(PropertySet.class);

        final MockGenericValue mockGenericValue = new MockGenericValue("OAuthServiceProviderToken", MapBuilder.<String, Object>newBuilder().
                add("id", 10000L).
                add("created", null).
                add("token", "23134123412413").
                add("tokenSecret", "adfasdfasdfsdf").
                add("tokenType", "ACCESS").
                add("consumerKey", "www.google.com").
                add("username", "admin").
                toMap());
        expect(mockDelegator.findByAnd("OAuthServiceProviderToken", MapBuilder.<String, Object>newBuilder().
                add("token", "23134123412413").toMap())).
                andReturn(CollectionBuilder.<GenericValue>newBuilder(
                        mockGenericValue).asList());
        expect(mockDelegator.removeValue(mockGenericValue)).andReturn(1);

        expect(mockPropertySetFactory.buildCachingPropertySet("OAuthServiceProviderToken", 10000L, true)).
                andReturn(mockPs).once();
        expect(mockPs.getKeys()).andReturn(CollectionBuilder.newBuilder("prop1", "prop2").asList()).once();
        mockPs.remove("prop1");
        mockPs.remove("prop2");

        replay(mockDelegator, mockUserUtil, mockConsumerStore, mockPropertySetFactory, mockPs, mockPs2);
        final OfBizServiceProviderTokenStore store = new OfBizServiceProviderTokenStore(mockDelegator, mockUserUtil, mockConsumerStore, mockPropertySetFactory);
        try
        {
            store.remove(null);
            fail("Should have thrown exception!");
        }
        catch (RuntimeException e)
        {
            //yay!
        }

        store.remove("23134123412413");

        verify(mockDelegator, mockUserUtil, mockConsumerStore, mockPropertySetFactory, mockPs, mockPs2);
    }

    private void assertTokenEquals(ServiceProviderToken expected, ServiceProviderToken result)
    {
        assertEquals(expected.getToken(), result.getToken());
        assertEquals(expected.getTokenSecret(), result.getTokenSecret());
        assertEquals(expected.isAccessToken(), result.isAccessToken());
        assertEquals(expected.hasBeenAuthorized(), result.hasBeenAuthorized());
        assertEquals(expected.isRequestToken(), result.isRequestToken());
        assertEquals(expected.getConsumer().getKey(), result.getConsumer().getKey());
        assertEquals(expected.getUser(), result.getUser());
        assertEquals(expected.getVerifier(), result.getVerifier());
        assertEquals(expected.getTimeToLive(), result.getTimeToLive());
        assertEquals(expected.getProperties(), result.getProperties());
        assertEquals(expected.getVersion(), result.getVersion());
    }

    private static Map<String, Object> eqOfBizMapArg(Map<String, Object> in)
    {
        reportMatcher(new OfBizMapArgsEqual(in));
        return null;
    }
}
