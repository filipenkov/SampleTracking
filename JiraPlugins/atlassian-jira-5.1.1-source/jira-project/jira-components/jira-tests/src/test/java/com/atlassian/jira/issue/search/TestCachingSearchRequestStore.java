package com.atlassian.jira.issue.search;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.sharing.IndexableSharedEntity;
import com.atlassian.jira.sharing.SharedEntityAccessor.RetrievalDescriptor;
import com.atlassian.jira.user.MockGroup;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.collect.EnclosedIterable;
import com.atlassian.jira.util.collect.MockCloseableIterable;
import com.atlassian.query.QueryImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test for {@link CachingSearchRequestStore}.
 *
 * @since v3.13
 */
public class TestCachingSearchRequestStore extends MockControllerTestCase
{
    private SearchRequestStore deletegateStore;
    private SearchRequestStore cachingStore;

    private User adminUser;
    private User fredUser;
    private Group group;

    private SearchRequest searchRequest1;
    private SearchRequest searchRequest2;
    private SearchRequest searchRequest3;
    private SearchRequest searchRequest4;

    @Before
    public void setUp() throws Exception
    {
        deletegateStore = (SearchRequestStore) mockController.getMock(SearchRequestStore.class);
        cachingStore = new CachingSearchRequestStore(deletegateStore);

        adminUser = new MockUser("admin");
        group = new MockGroup("admin");
        fredUser = new MockUser("fredUser");

        searchRequest1 = new SearchRequest(new QueryImpl(), adminUser.getName(), null, null, 1L, 0L);
        searchRequest2 = new SearchRequest(new QueryImpl(), adminUser.getName(), null, null, 2L, 0L);
        searchRequest3 = new SearchRequest(new QueryImpl(), adminUser.getName(), null, null, 3L, 0L);
        searchRequest4 = new SearchRequest(new QueryImpl(), fredUser.getName(), null, null, 4L, 0L);
    }

    @After
    public void tearDown() throws Exception
    {

    }

    /**
     * This should be a simple pass through.
     */
    @Test
    public void testGetAllRequests()
    {
        deletegateStore.getAllRequests();
        mockController.setReturnValue(Collections.EMPTY_LIST);

        deletegateStore.getAllRequests();
        mockController.setReturnValue(Collections.EMPTY_LIST);

        mockController.replay();

        assertEquals(Collections.EMPTY_LIST, cachingStore.getAllRequests());
        assertEquals(Collections.EMPTY_LIST, cachingStore.getAllRequests());

        mockController.verify();
    }

    /**
     * This should be a simple pass through.
     */
    @Test
    public void testGetRequests()
    {
        deletegateStore.getAllRequests();
        mockController.setReturnValue(Collections.EMPTY_LIST);

        deletegateStore.getAllRequests();
        mockController.setReturnValue(Collections.EMPTY_LIST);

        mockController.replay();

        assertEquals(Collections.EMPTY_LIST, cachingStore.getAllRequests());
        assertEquals(Collections.EMPTY_LIST, cachingStore.getAllRequests());

        mockController.verify();
    }

    /**
     * This should be a simple pass through.
     */
    @Test
    public void testGet()
    {
        final MockCloseableIterable expectedValue = new MockCloseableIterable(Collections.EMPTY_LIST);

        MockRetrievalDescriptor expectedRetrievalDescriptor = new MockRetrievalDescriptor();

        deletegateStore.get(expectedRetrievalDescriptor);
        mockController.setReturnValue(expectedValue);

        deletegateStore.get(expectedRetrievalDescriptor);
        mockController.setReturnValue(expectedValue);

        deletegateStore.get(expectedRetrievalDescriptor);
        mockController.setReturnValue(expectedValue);

        mockController.replay();

        assertSame(expectedValue, cachingStore.get(expectedRetrievalDescriptor));
        assertSame(expectedValue, cachingStore.get(expectedRetrievalDescriptor));
        assertSame(expectedValue, cachingStore.get(expectedRetrievalDescriptor));

        mockController.verify();
    }

    /**
     * This should be a simple pass through.
     */
    @Test
    public void testAll()
    {
        final MockCloseableIterable<SearchRequest> expectedValue = new MockCloseableIterable<SearchRequest>(Collections.<SearchRequest>emptyList());

        deletegateStore.getAll();
        mockController.setReturnValue(expectedValue);

        deletegateStore.getAll();
        mockController.setReturnValue(expectedValue);

        mockController.replay();

        assertSame(expectedValue, cachingStore.getAll());
        assertSame(expectedValue, cachingStore.getAll());

        mockController.verify();
    }

    /**
     * This should be a simple pass through.
     */
    @Test
    public void testGetAllIndexableSharedEntities()
    {
        final MockCloseableIterable<IndexableSharedEntity<SearchRequest>> expectedValue =
                new MockCloseableIterable<IndexableSharedEntity<SearchRequest>>(Collections.<IndexableSharedEntity<SearchRequest>>emptyList());

        deletegateStore.getAllIndexableSharedEntities();
        mockController.setReturnValue(expectedValue);

        deletegateStore.getAllIndexableSharedEntities();
        mockController.setReturnValue(expectedValue);

        mockController.replay();

        assertSame(expectedValue, cachingStore.getAllIndexableSharedEntities());
        assertSame(expectedValue, cachingStore.getAllIndexableSharedEntities());

        mockController.verify();
    }

    /**
     * Check that we can cache the owned search.
     */
    @Test
    public void testGetAllOwnedSearchRequest()
    {
        final List expectedRequests = EasyList.build(searchRequest1, searchRequest2, searchRequest3);

        deletegateStore.getSearchRequest(searchRequest1.getId());
        mockController.setReturnValue(searchRequest1);

        deletegateStore.getSearchRequest(searchRequest4.getId());
        mockController.setReturnValue(searchRequest4);

        deletegateStore.getAllOwnedSearchRequests(adminUser);
        mockController.setReturnValue(expectedRequests);

        mockController.replay();

        //put a value in the cache for the test.
        cachingStore.getSearchRequest(searchRequest1.getId());
        cachingStore.getSearchRequest(searchRequest4.getId());

        //this first call should not be cached and should delegate to getAllOwnedSearchRequests.
        assertEqualsNotSame(expectedRequests, cachingStore.getAllOwnedSearchRequests(adminUser));

        //these calls will now be cached.
        assertEqualsNotSame(expectedRequests, cachingStore.getAllOwnedSearchRequests(adminUser));
        assertEqualsNotSame(expectedRequests, cachingStore.getAllOwnedSearchRequests(adminUser));
        assertEqualsNotSame(expectedRequests, cachingStore.getAllOwnedSearchRequests(adminUser));

        //these should not also be cached.
        assertEqualsNotSame(searchRequest2, cachingStore.getSearchRequest(searchRequest2.getId()));
        assertEqualsNotSame(searchRequest3, cachingStore.getSearchRequest(searchRequest3.getId()));

        mockController.verify();
    }

    /**
     * Check that we can get the owned search when a null value from the store is returned.
     */
    @Test
    public void testGetAllOwnedSearchRequestNullList()
    {
        deletegateStore.getAllOwnedSearchRequests(adminUser);
        mockController.setReturnValue(null, 3);

        mockController.replay();

        //failure should not be cached.
        assertNull(cachingStore.getAllOwnedSearchRequests(adminUser));
        assertNull(cachingStore.getAllOwnedSearchRequests(adminUser));
        assertNull(cachingStore.getAllOwnedSearchRequests(adminUser));

        mockController.verify();
    }

    /**
     * Check that exception is propgated and the results is not stored.
     */
    @Test
    public void testGetAllOwnedSearchRequestError()
    {
        deletegateStore.getAllOwnedSearchRequests(adminUser);
        mockController.setThrowable(new RuntimeException("testGetAllOwnedSearchRequestError"));

        deletegateStore.getAllOwnedSearchRequests(adminUser);
        mockController.setReturnValue(null);

        mockController.replay();

        try
        {
            cachingStore.getAllOwnedSearchRequests(adminUser);
            fail("Expecting an error.");
        }
        catch (RuntimeException expected)
        {
        }

        //these calls will now be cached.
        assertNull(cachingStore.getAllOwnedSearchRequests(adminUser));

        mockController.verify();
    }

    /**
     * This should be a simple call through.
     */
    @Test
    public void testGetRequestByAuthorAndName()
    {
        deletegateStore.getRequestByAuthorAndName(adminUser, "struff");
        mockController.setReturnValue(searchRequest1);

        deletegateStore.getRequestByAuthorAndName(null, "abc");
        mockController.setReturnValue(null);

        mockController.replay();

        assertSame(searchRequest1, cachingStore.getRequestByAuthorAndName(adminUser, "struff"));
        assertNull(cachingStore.getRequestByAuthorAndName(null, "abc"));

        mockController.verify();
    }

    /**
     * Test the we can get a particular request when it exists.
     */
    @Test
    public void testGetSearchRequest()
    {
        deletegateStore.getSearchRequest(searchRequest1.getId());
        mockController.setReturnValue(searchRequest1);
        deletegateStore.getSearchRequest(searchRequest2.getId());
        mockController.setReturnValue(searchRequest2);
        deletegateStore.getSearchRequest(searchRequest4.getId());
        mockController.setReturnValue(searchRequest4);

        mockController.replay();

        //this should call through because it is not in the cache.
        assertEqualsNotSame(searchRequest1, cachingStore.getSearchRequest(searchRequest1.getId()));

        //this should call through because it is not in the cache.
        assertEqualsNotSame(searchRequest2, cachingStore.getSearchRequest(searchRequest2.getId()));

        //these calls should be cached.
        assertEqualsNotSame(searchRequest1, cachingStore.getSearchRequest(searchRequest1.getId()));
        assertEqualsNotSame(searchRequest1, cachingStore.getSearchRequest(searchRequest1.getId()));
        assertEqualsNotSame(searchRequest1, cachingStore.getSearchRequest(searchRequest1.getId()));
        assertEqualsNotSame(searchRequest2, cachingStore.getSearchRequest(searchRequest2.getId()));
        assertEqualsNotSame(searchRequest2, cachingStore.getSearchRequest(searchRequest2.getId()));
        assertEqualsNotSame(searchRequest2, cachingStore.getSearchRequest(searchRequest2.getId()));
        assertEqualsNotSame(searchRequest2, cachingStore.getSearchRequest(searchRequest2.getId()));

        //this call should be direct through.
        assertEqualsNotSame(searchRequest4, cachingStore.getSearchRequest(searchRequest4.getId()));

        //these should be cached.
        assertEqualsNotSame(searchRequest4, cachingStore.getSearchRequest(searchRequest4.getId()));
        assertEqualsNotSame(searchRequest4, cachingStore.getSearchRequest(searchRequest4.getId()));
        assertEqualsNotSame(searchRequest4, cachingStore.getSearchRequest(searchRequest4.getId()));
        assertEqualsNotSame(searchRequest2, cachingStore.getSearchRequest(searchRequest2.getId()));
        assertEqualsNotSame(searchRequest1, cachingStore.getSearchRequest(searchRequest1.getId()));

        mockController.verify();
    }

    /**
     * Test the we can't get a particular request when it does exist. We don't
     */
    @Test
    public void testGetSearchRequestDoesNotExist()
    {
        deletegateStore.getSearchRequest(searchRequest1.getId());
        mockController.setReturnValue(null);
        deletegateStore.getSearchRequest(searchRequest2.getId());
        mockController.setReturnValue(searchRequest2);
        deletegateStore.getSearchRequest(searchRequest4.getId());
        mockController.setReturnValue(searchRequest4);
        deletegateStore.getSearchRequest(searchRequest1.getId());
        mockController.setReturnValue(null);

        mockController.replay();

        //this should call through because it is not in the cache.
        assertNull(null, cachingStore.getSearchRequest(searchRequest1.getId()));

        //this should call through because it is not in the cache.
        assertEqualsNotSame(searchRequest2, cachingStore.getSearchRequest(searchRequest2.getId()));

        //these calls should be cached.
        assertEqualsNotSame(searchRequest2, cachingStore.getSearchRequest(searchRequest2.getId()));
        assertEqualsNotSame(searchRequest2, cachingStore.getSearchRequest(searchRequest2.getId()));
        assertEqualsNotSame(searchRequest2, cachingStore.getSearchRequest(searchRequest2.getId()));
        assertEqualsNotSame(searchRequest2, cachingStore.getSearchRequest(searchRequest2.getId()));

        //this call should be direct through.
        assertEqualsNotSame(searchRequest4, cachingStore.getSearchRequest(searchRequest4.getId()));

        //these should be cached.
        assertEqualsNotSame(searchRequest4, cachingStore.getSearchRequest(searchRequest4.getId()));
        assertEqualsNotSame(searchRequest4, cachingStore.getSearchRequest(searchRequest4.getId()));
        assertEqualsNotSame(searchRequest4, cachingStore.getSearchRequest(searchRequest4.getId()));
        assertEqualsNotSame(searchRequest2, cachingStore.getSearchRequest(searchRequest2.getId()));

        //this will not be cached.
        assertNull(cachingStore.getSearchRequest(searchRequest1.getId()));

        mockController.verify();
    }

    /**
     * Test the we can't get a particular request when it does exist. We don't
     */
    @Test
    public void testGetSearchRequestRuntimeException()
    {
        deletegateStore.getSearchRequest(searchRequest1.getId());
        mockController.setThrowable(new RuntimeException());
        deletegateStore.getSearchRequest(searchRequest2.getId());
        mockController.setReturnValue(searchRequest2);
        deletegateStore.getSearchRequest(searchRequest1.getId());
        mockController.setReturnValue(searchRequest1);

        mockController.replay();

        try
        {
            cachingStore.getSearchRequest(searchRequest1.getId());
            fail("Expecting an exception to be thrown by the store.");
        }
        catch (RuntimeException expected)
        {
            //ignored.
        }

        //this should call through because it is not in the cache.
        assertEqualsNotSame(searchRequest2, cachingStore.getSearchRequest(searchRequest2.getId()));

        //this should call through because it is not in the cache.
        assertEqualsNotSame(searchRequest1, cachingStore.getSearchRequest(searchRequest1.getId()));

        //these calls should be cached.
        assertEqualsNotSame(searchRequest2, cachingStore.getSearchRequest(searchRequest2.getId()));
        assertEqualsNotSame(searchRequest2, cachingStore.getSearchRequest(searchRequest2.getId()));
        assertEqualsNotSame(searchRequest2, cachingStore.getSearchRequest(searchRequest2.getId()));
        assertEqualsNotSame(searchRequest2, cachingStore.getSearchRequest(searchRequest2.getId()));
        assertEqualsNotSame(searchRequest1, cachingStore.getSearchRequest(searchRequest1.getId()));
        assertEqualsNotSame(searchRequest1, cachingStore.getSearchRequest(searchRequest1.getId()));
        assertEqualsNotSame(searchRequest1, cachingStore.getSearchRequest(searchRequest1.getId()));

        mockController.verify();
    }

    /**
     * Does a created portal page get cached.
     */
    @Test
    public void testCreate()
    {
        deletegateStore.create(searchRequest1);
        mockController.setReturnValue(searchRequest3);

        mockController.replay();

        //add the search request. Should now be in the cache.
        assertEqualsNotSame(searchRequest3, cachingStore.create(searchRequest1));

        //all of these calls should be cached.
        assertEqualsNotSame(searchRequest3, cachingStore.getSearchRequest(searchRequest3.getId()));
        assertEqualsNotSame(searchRequest3, cachingStore.getSearchRequest(searchRequest3.getId()));
        assertEqualsNotSame(searchRequest3, cachingStore.getSearchRequest(searchRequest3.getId()));

        mockController.verify();
    }

    /**
     * Check what happens when the portal page is not created by the store..
     */
    @Test
    public void testCreateNullReturn()
    {
        deletegateStore.create(searchRequest1);
        mockController.setReturnValue(null);

        deletegateStore.getSearchRequest(searchRequest1.getId());
        mockController.setReturnValue(searchRequest1);

        mockController.replay();

        //add the search request. Should not be in he cache because of failure.
        assertNull(cachingStore.create(searchRequest1));

        //this call goes to the cache.
        assertEqualsNotSame(searchRequest1, cachingStore.getSearchRequest(searchRequest1.getId()));

        //these should be cached.
        assertEqualsNotSame(searchRequest1, cachingStore.getSearchRequest(searchRequest1.getId()));
        assertEqualsNotSame(searchRequest1, cachingStore.getSearchRequest(searchRequest1.getId()));

        mockController.verify();
    }

    /**
     * Check what happens when the portal page is not created by the store..
     */
    @Test
    public void testCreateException()
    {
        deletegateStore.create(searchRequest1);
        mockController.setThrowable(new RuntimeException("testCreateException"));

        deletegateStore.getSearchRequest(searchRequest1.getId());
        mockController.setReturnValue(searchRequest1);

        mockController.replay();

        try
        {
            cachingStore.create(searchRequest1);
            fail("Expecting an exception to be thrown by the store.");
        }
        catch (RuntimeException expected)
        {

        }

        //this should not be cached.
        assertEqualsNotSame(searchRequest1, cachingStore.getSearchRequest(searchRequest1.getId()));

        //this should now be cached.
        assertEqualsNotSame(searchRequest1, cachingStore.getSearchRequest(searchRequest1.getId()));
        assertEqualsNotSame(searchRequest1, cachingStore.getSearchRequest(searchRequest1.getId()));

        mockController.verify();
    }

    /**
     * Test the update when the page is not in the cache.
     */
    @Test
    public void testUpdateNotInCache()
    {
        deletegateStore.update(searchRequest1);
        mockController.setReturnValue(searchRequest3);

        mockController.replay();

        //add the search request. Should now be in the cache.
        assertEqualsNotSame(searchRequest3, cachingStore.update(searchRequest1));

        //all of these calls should be cached.
        assertEqualsNotSame(searchRequest3, cachingStore.getSearchRequest(searchRequest3.getId()));
        assertEqualsNotSame(searchRequest3, cachingStore.getSearchRequest(searchRequest3.getId()));
        assertEqualsNotSame(searchRequest3, cachingStore.getSearchRequest(searchRequest3.getId()));

        mockController.verify();
    }

    /**
     * Test the update when the page is already in the cache.
     */
    @Test
    public void testUpdateInCache()
    {
        deletegateStore.getSearchRequest(searchRequest1.getId());
        mockController.setReturnValue(searchRequest1);

        deletegateStore.update(searchRequest1);
        mockController.setReturnValue(searchRequest1);

        mockController.replay();

        //put a value in the cache for the test.
        cachingStore.getSearchRequest(searchRequest1.getId());

        //add the search request. Should now be in the cache.
        assertEqualsNotSame(searchRequest1, cachingStore.update(searchRequest1));

        //all of these calls should be cached.
        assertEqualsNotSame(searchRequest1, cachingStore.getSearchRequest(searchRequest1.getId()));
        assertEqualsNotSame(searchRequest1, cachingStore.getSearchRequest(searchRequest1.getId()));
        assertEqualsNotSame(searchRequest1, cachingStore.getSearchRequest(searchRequest1.getId()));

        mockController.verify();
    }

    /**
     * Check what happens when the delegate store fails with null return.
     */
    @Test
    public void testUpdateFailsWithNull()
    {
        deletegateStore.getSearchRequest(searchRequest1.getId());
        mockController.setReturnValue(searchRequest1);

        deletegateStore.update(searchRequest1);
        mockController.setReturnValue(null);

        deletegateStore.getSearchRequest(searchRequest1.getId());
        mockController.setReturnValue(null);

        mockController.replay();

        //put a value in the cache for the test.
        cachingStore.getSearchRequest(searchRequest1.getId());

        //this call should fail.
        assertNull(cachingStore.update(searchRequest1));

        //this call should not be cached.
        assertNull(cachingStore.getSearchRequest(searchRequest1.getId()));

        mockController.verify();
    }

    /**
     * Check what happens when the delegate store fails with null return.
     */
    @Test
    public void testUpdateFailsWithException()
    {
        deletegateStore.getSearchRequest(searchRequest1.getId());
        mockController.setReturnValue(searchRequest1);

        deletegateStore.update(searchRequest1);
        mockController.setThrowable(new RuntimeException("testUpdateFailsWithException"));

        deletegateStore.getSearchRequest(searchRequest1.getId());
        mockController.setReturnValue(null);

        mockController.replay();

        //put a value in the cache for the test.
        cachingStore.getSearchRequest(searchRequest1.getId());

        try
        {
            cachingStore.update(searchRequest1);
            fail("This exception should be thrown.");
        }
        catch (RuntimeException expected)
        {
        }

        //this call should not be cached.
        assertNull(cachingStore.getSearchRequest(searchRequest1.getId()));

        mockController.verify();
    }

    /**
     * Check what happens when try to save a search whose user has changed.
     */
    @Test
    public void testUpdateChangedUserName()
    {
        SearchRequest expectedRequest = new SearchRequest(searchRequest1);
        expectedRequest.setOwnerUserName(fredUser.getName());

        deletegateStore.getAllOwnedSearchRequests(adminUser);
        mockController.setReturnValue(EasyList.build(searchRequest1, searchRequest2, searchRequest3));

        deletegateStore.getAllOwnedSearchRequests(fredUser);
        mockController.setReturnValue(EasyList.build(searchRequest4));

        deletegateStore.update(expectedRequest);
        mockController.setReturnValue(expectedRequest);

        mockController.replay();

        //put a value in the cache for the test.
        cachingStore.getAllOwnedSearchRequests(adminUser);
        cachingStore.getAllOwnedSearchRequests(fredUser);

        //this call should work.
        assertEqualsNotSame(expectedRequest, cachingStore.update(expectedRequest));

        //all of these calls should be cache.
        assertEqualsNotSame(searchRequest2, cachingStore.getSearchRequest(searchRequest2.getId()));
        assertEqualsNotSame(searchRequest3, cachingStore.getSearchRequest(searchRequest3.getId()));
        assertEqualsNotSame(searchRequest4, cachingStore.getSearchRequest(searchRequest4.getId()));
        assertEqualsNotSame(expectedRequest, cachingStore.getSearchRequest(expectedRequest.getId()));

        assertEqualsNotSame(EasyList.build(searchRequest2, searchRequest3), cachingStore.getAllOwnedSearchRequests(adminUser));
        assertEqualsNotSame(EasyList.build(expectedRequest, searchRequest4), cachingStore.getAllOwnedSearchRequests(fredUser));

        mockController.verify();

    }

    /**
     * Make sure that the adjust favourite count works when entity not in the cache.
     */
    @Test
    public void testAdjustFavouriteCountNotInCache()
    {
        deletegateStore.adjustFavouriteCount(searchRequest1.getId(), -10);
        mockController.setReturnValue(searchRequest1);

        mockController.replay();

        //adjust the search request.
        assertEqualsNotSame(searchRequest1, cachingStore.adjustFavouriteCount(searchRequest1.getId(), -10));

        //all of these calls should be cached.
        assertEqualsNotSame(searchRequest1, cachingStore.getSearchRequest(searchRequest1.getId()));
        assertEqualsNotSame(searchRequest1, cachingStore.getSearchRequest(searchRequest1.getId()));
        assertEqualsNotSame(searchRequest1, cachingStore.getSearchRequest(searchRequest1.getId()));

        mockController.verify();
    }

    /**
     * Make sure that the adjust favourite count works when entity is in the cache.
     */
    @Test
    public void testAdjustFavouriteCountInCache()
    {
        deletegateStore.adjustFavouriteCount(searchRequest2.getId(), 10);
        mockController.setReturnValue(searchRequest2);

        mockController.replay();

        //adjust the search request.
        assertEqualsNotSame(searchRequest2, cachingStore.adjustFavouriteCount(searchRequest2.getId(), 10));

        //all of these calls should be cached.
        assertEqualsNotSame(searchRequest2, cachingStore.getSearchRequest(searchRequest2.getId()));
        assertEqualsNotSame(searchRequest2, cachingStore.getSearchRequest(searchRequest2.getId()));
        assertEqualsNotSame(searchRequest2, cachingStore.getSearchRequest(searchRequest2.getId()));

        mockController.verify();
    }

    /**
     * Make sure that the adjust favourite count works when wrapped store returns null.
     */
    @Test
    public void testAdjustFavouriteFailWithNull()
    {
        deletegateStore.getSearchRequest(searchRequest3.getId());
        mockController.setReturnValue(searchRequest3);

        deletegateStore.adjustFavouriteCount(searchRequest3.getId(), Integer.MAX_VALUE);
        mockController.setReturnValue(null);

        deletegateStore.getSearchRequest(searchRequest3.getId());
        mockController.setReturnValue(searchRequest3);

        mockController.replay();

        //prime the cache for the test.
        deletegateStore.getSearchRequest(searchRequest3.getId());

        //adjust should fail.
        assertNull(cachingStore.adjustFavouriteCount(searchRequest3.getId(), Integer.MAX_VALUE));

        //this call should not be cached.
        assertEqualsNotSame(searchRequest3, cachingStore.getSearchRequest(searchRequest3.getId()));

        mockController.verify();
    }

    /**
     * Make sure that the adjust favourite count works when wrapped store throws runtime exception.
     */
    @Test
    public void testAdjustFavouriteFailWithRuntimeException()
    {
        deletegateStore.getSearchRequest(searchRequest3.getId());
        mockController.setReturnValue(searchRequest3);

        deletegateStore.adjustFavouriteCount(searchRequest3.getId(), Integer.MAX_VALUE);
        mockController.setThrowable(new RuntimeException("testAdjustFavouriteFailWithRuntimeException"));

        deletegateStore.getSearchRequest(searchRequest3.getId());
        mockController.setReturnValue(searchRequest3);

        mockController.replay();

        //prime the cache for the test.
        deletegateStore.getSearchRequest(searchRequest3.getId());

        //adjust should fail.
        try
        {
            cachingStore.adjustFavouriteCount(searchRequest3.getId(), Integer.MAX_VALUE);
            fail("Exception should have been thrown.");
        }
        catch (Exception expected)
        {

        }

        //this call should not be cached.
        assertEqualsNotSame(searchRequest3, cachingStore.getSearchRequest(searchRequest3.getId()));

        mockController.verify();
    }

    /**
     * Make sure we can delete a search request that is not in the cache.
     */
    @Test
    public void testDeleteNotInCache()
    {
        deletegateStore.delete(searchRequest4.getId());

        deletegateStore.getSearchRequest(searchRequest4.getId());
        mockController.setReturnValue(searchRequest4);

        mockController.replay();

        cachingStore.delete(searchRequest4.getId());

        //this should not be cached.
        assertEqualsNotSame(searchRequest4, cachingStore.getSearchRequest(searchRequest4.getId()));
    }

    /**
     * Make sure we can delete a search request that is in the cache.
     */
    @Test
    public void testDeleteInCache()
    {
        deletegateStore.getAllOwnedSearchRequests(adminUser);
        mockController.setReturnValue(EasyList.build(searchRequest1, searchRequest2));

        deletegateStore.delete(searchRequest2.getId());

        deletegateStore.getSearchRequest(searchRequest2.getId());
        mockController.setReturnValue(searchRequest2);

        mockController.replay();

        //prime the cache.
        cachingStore.getAllOwnedSearchRequests(adminUser);

        //execute the tests.        
        cachingStore.delete(searchRequest2.getId());

        //these should be cached.
        assertEqualsNotSame(searchRequest1, cachingStore.getSearchRequest(searchRequest1.getId()));
        assertEqualsNotSame(EasyList.build(searchRequest1), cachingStore.getAllOwnedSearchRequests(adminUser));

        //this should not be cached.
        assertEqualsNotSame(searchRequest2, cachingStore.getSearchRequest(searchRequest2.getId()));

        mockController.verify();
    }

    /**
     * This should be a straight call through.
     */
    @Test
    public void testGetSearchRequestsProject()
    {
        final EnclosedIterable expectedIterable = new MockCloseableIterable(EasyList.build(searchRequest1, searchRequest4));
        final Project project = new MockProject();

        deletegateStore.getSearchRequests(project);
        mockController.setReturnValue(expectedIterable);

        mockController.replay();

        assertEquals(expectedIterable, cachingStore.getSearchRequests(project));

        mockController.verify();
    }

    /**
     * This should be a straight call through.
     */
    @Test
    public void testGetSearchRequestsGroup()
    {
        final EnclosedIterable expectedIterable = new MockCloseableIterable(EasyList.build(searchRequest4, searchRequest2));

        deletegateStore.getSearchRequests(group);
        mockController.setReturnValue(expectedIterable);
        mockController.replay();

        assertEquals(expectedIterable, cachingStore.getSearchRequests(group));

        mockController.verify();
    }

    private void assertEqualsNotSame(final Collection expectedCollection, final Collection actualCollection)
    {
        //we can't use equals here because we can't be sure of return order.
        assertTrue("Collections where not of the correct size.", expectedCollection.size() == actualCollection.size());
        assertTrue("Collections did not contain the same elements.", expectedCollection.containsAll(actualCollection));

        for (Iterator expectedIterator = expectedCollection.iterator(), actualIterator = actualCollection.iterator(); expectedIterator.hasNext();)
        {
            assertNotSame(expectedIterator.next(), actualIterator.next());
        }
    }

    private void assertEqualsNotSame(final SearchRequest expectedRequest, final SearchRequest actualRequest)
    {
        assertNotSame(expectedRequest, actualRequest);
        assertEquals(expectedRequest, actualRequest);
    }

    private static class MockRetrievalDescriptor implements RetrievalDescriptor
    {
        public List /* <Long> */getIds()
        {
            return null;
        }

        public boolean preserveOrder()
        {
            return false;
        }
    }
}
