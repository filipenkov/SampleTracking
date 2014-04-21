package com.atlassian.jira.oauth.serviceprovider;

import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.Token;
import com.atlassian.oauth.serviceprovider.ServiceProviderToken;
import com.atlassian.oauth.serviceprovider.ServiceProviderTokenStore;
import static com.atlassian.oauth.serviceprovider.ServiceProviderToken.Version;
import junit.framework.TestCase;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.security.Principal;
import java.util.Map;

public class TestCachingServiceProviderTokenStore extends TestCase
{

    public void testPutAndGet()
    {
        Consumer consumer = Consumer.key("www.google.com").name("iGoogle").signatureMethod(Consumer.SignatureMethod.HMAC_SHA1).build();
        Principal user = new Principal()
        {
            public String getName()
            {
                return "admin";
            }
        };
        final Map<String, String> props = MapBuilder.<String, String>newBuilder().add("prop1", "val1").add("prop2", "val2").toMap();

        ServiceProviderToken token = ServiceProviderToken.newRequestToken("mytoken")
                .tokenSecret("ssh...it's secret")
                .consumer(consumer)
                .authorizedBy(user)
                .verifier("sssshhh...itssecret")
                .properties(props)
                .version(Version.V_1_0_A)
                .build();
        //update existing token.
        ServiceProviderToken tokenUpdated = ServiceProviderToken.newAccessToken("mytoken")
                .tokenSecret("This is a new secret!!")
                .consumer(consumer)
                .authorizedBy(user)
                .build();
        final ServiceProviderTokenStore mockDelegateStore = createMock(ServiceProviderTokenStore.class);

        expect(mockDelegateStore.put(token)).andReturn(token);

        expect(mockDelegateStore.put(tokenUpdated)).andReturn(tokenUpdated);

        expect(mockDelegateStore.get("missingtoken")).andReturn(null).once();

        replay(mockDelegateStore);
        CachingServiceProviderTokenStore cachingStore = new CachingServiceProviderTokenStore(mockDelegateStore);
        cachingStore.put(token);
        //try to get the same token a couple of times.  Should only hit the store once!
        ServiceProviderToken resultToken = cachingStore.get("mytoken");
        assertTokenEquals(token, resultToken);
        resultToken = cachingStore.get("mytoken");
        assertTokenEquals(token, resultToken);
        resultToken = cachingStore.get("mytoken");
        assertTokenEquals(token, resultToken);
        resultToken = cachingStore.get("mytoken");
        assertTokenEquals(token, resultToken);

        cachingStore.put(tokenUpdated);
        final Token updatedResultToken = cachingStore.get("mytoken");
        assertEquals(tokenUpdated, updatedResultToken);

        //now try getting a token that doesn't exist.
        final Token missingToken = cachingStore.get("missingtoken");
        assertNull(missingToken);

        verify(mockDelegateStore);
    }

    public void testRemove()
    {
        Consumer consumer = Consumer.key("www.google.com").name("iGoogle").signatureMethod(Consumer.SignatureMethod.HMAC_SHA1).build();
        Principal user = new Principal()
        {
            public String getName()
            {
                return "admin";
            }
        };

        final Map<String, String> props = MapBuilder.<String, String>newBuilder().add("prop1", "val1").add("prop2", "val2").toMap();

        ServiceProviderToken token = ServiceProviderToken.newAccessToken("mytoken")
                .tokenSecret("ssh...it's secret")
                .consumer(consumer)
                .authorizedBy(user)
                .properties(props)
                .build();
        final ServiceProviderTokenStore mockDelegateStore = createMock(ServiceProviderTokenStore.class);

        expect(mockDelegateStore.put(token)).andReturn(token);

        mockDelegateStore.remove("mytoken");
        expect(mockDelegateStore.get("mytoken")).andReturn(null).once();

        mockDelegateStore.remove("dontexist");
        expect(mockDelegateStore.get("dontexist")).andReturn(null).once();

        replay(mockDelegateStore);
        CachingServiceProviderTokenStore cachingStore = new CachingServiceProviderTokenStore(mockDelegateStore);
        cachingStore.put(token);
        //try to get the same token a couple of times.  Should only hit the store once!
        ServiceProviderToken resultToken = cachingStore.get("mytoken");
        assertTokenEquals(token, resultToken);

        //now remove the token
        cachingStore.remove("mytoken");

        //and get it again
        final Token deletedToken = cachingStore.get("mytoken");
        assertNull(deletedToken);

        //remove a token that doesn't exist.
        cachingStore.remove("dontexist");
        final Token dontexistToken = cachingStore.get("dontexist");
        assertNull(dontexistToken);

        verify(mockDelegateStore);
    }

    private void assertTokenEquals(final ServiceProviderToken expected, final ServiceProviderToken resultToken)
    {
        assertEquals(expected.getToken(), resultToken.getToken());
        assertEquals(expected.getTokenSecret(), resultToken.getTokenSecret());
        assertEquals(expected.getConsumer(), resultToken.getConsumer());
        assertEquals(expected.getProperties(), resultToken.getProperties());
        assertEquals(expected.getUser(), resultToken.getUser());
    }

}
