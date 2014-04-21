package com.atlassian.jira.issue.search;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectImpl;
import com.atlassian.jira.sharing.IndexableSharedEntity;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.sharing.SharedEntityAccessor;
import com.atlassian.jira.util.EasyList;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.collect.CollectionUtil;
import com.atlassian.jira.util.collect.EnclosedIterable;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Test for {@link com.atlassian.jira.issue.search.OfBizSearchRequestStore} with real data.
 */

public class TestOfBizSearchRequestStoreData extends LegacyJiraMockTestCase
{
    private static final class Table
    {
        static final String NAME = OfBizSearchRequestStore.Table.NAME;
    }

    private static final String USER = "TestOfBizSearchRequestStoreData_user";
    private static final String BOB = "TestOfBizSearchRequestStoreData_bob";

    private OfBizDelegator delegator;
    private SearchRequestStore store;
    private SearchService searchService;
    private User user;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        delegator = ComponentManager.getComponentInstanceOfType(OfBizDelegator.class);
        searchService = ComponentManager.getComponentInstanceOfType(SearchService.class);
        store = new OfBizSearchRequestStore(delegator, null, searchService)
        {
            @Override
            Query getSearchQueryFromGv(final GenericValue searchRequestGv)
            {
                return new QueryImpl();
            }
        };
        user = UtilsForTests.getTestUser(TestOfBizSearchRequestStoreData.USER);
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        UtilsForTests.cleanOFBiz();
        delegator = null;
        store = null;
        user = null;
        searchService = null;
        UtilsForTests.cleanUsers();
        UtilsForTests.cleanOFBiz();
    }

    public void testContructorWithNullDelegator()
    {
        try
        {
            new OfBizSearchRequestStore(null, null, null);
            fail("Should not accept null delegator.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }
    }

    public void testGetAllOwnedSearchRequestsNullUser()
    {
        assertTrue(store.getAllOwnedSearchRequests(null).isEmpty());
    }

    public void testGetAllOwnedSearchRequestsNoSRs()
    {
        assertTrue(store.getAllOwnedSearchRequests(user).isEmpty());
    }

    public void testGetAllOwnedSearchRequestsNoMatched()
    {
        UtilsForTests.getTestEntity(Table.NAME, EasyMap.build("author", TestOfBizSearchRequestStoreData.BOB));
        UtilsForTests.getTestEntity(Table.NAME, EasyMap.build("author", TestOfBizSearchRequestStoreData.BOB));
        UtilsForTests.getTestEntity(Table.NAME, EasyMap.build("author", "nick"));

        assertTrue(store.getAllOwnedSearchRequests(user).isEmpty());
    }

    public void testGetAllOwnedSearchRequestsAllMatched() throws Exception
    {
        final GenericValue gv1 = UtilsForTests.getTestEntity(Table.NAME, EasyMap.build("author", TestOfBizSearchRequestStoreData.USER, "name", "one",
            "request", "<xml/>", "description", "test desc", "id", 12345L, "favCount", 0L));
        final GenericValue gv2 = UtilsForTests.getTestEntity(Table.NAME, EasyMap.build("author", TestOfBizSearchRequestStoreData.USER, "name", "two",
            "request", "<xml/>", "description", "test desc", "id", 23456L, "favCount", 0L));
        final GenericValue gv3 = UtilsForTests.getTestEntity(Table.NAME, EasyMap.build("author", TestOfBizSearchRequestStoreData.USER, "name",
            "three", "request", "<xml/>", "description", "test desc", "id", 34567L, "favCount", 0L));

        final Collection<SearchRequest> expectedResults = EasyList.build(getSearchRequestFromGV(gv1), getSearchRequestFromGV(gv3), getSearchRequestFromGV(gv2));

        final Collection<SearchRequest> result = store.getAllOwnedSearchRequests(user);
        assertFalse(result.isEmpty());
        assertEquals(expectedResults, result);
    }

    private SearchRequest getSearchRequestFromGV(GenericValue srGv)
    {
        return new SearchRequest(new QueryImpl(), srGv.getString("author"), srGv.getString("name"),
                srGv.getString("description"), srGv.getLong("id"), srGv.getLong("favCount"));
    }

    public void testGetAllOwnedSearchRequestsSomeMatched() throws Exception
    {
        final GenericValue gv1 = UtilsForTests.getTestEntity(Table.NAME, EasyMap.build("author", TestOfBizSearchRequestStoreData.USER, "name", "one",
            "request", "<xml/>", "description", "test desc", "id", 12345L, "favCount", 0L));
        UtilsForTests.getTestEntity(Table.NAME, EasyMap.build("author", TestOfBizSearchRequestStoreData.BOB, "name", "two", "request", "<xml/>", "description", "test desc", "id", 34567L, "favCount", 0L));
        final GenericValue gv3 = UtilsForTests.getTestEntity(Table.NAME, EasyMap.build("author", TestOfBizSearchRequestStoreData.USER, "name",
            "three", "request", "<xml/>", "description", "test desc", "id", 23456L, "favCount", 0L));

        final Collection<SearchRequest> expectedResults = EasyList.build(getSearchRequestFromGV(gv1), getSearchRequestFromGV(gv3));

        final Collection<SearchRequest> result = store.getAllOwnedSearchRequests(user);
        assertFalse(result.isEmpty());
        assertEquals(expectedResults, result);
    }

    public void testGetRequestByAuthorAndNameNullUserNullName()
    {
        final SearchRequest sr = store.getRequestByAuthorAndName(null, null);
        assertNull(sr);
    }

    public void testGetRequestByAuthorAndNameNullName()
    {
        UtilsForTests.getTestEntity(Table.NAME, EasyMap.build("author", TestOfBizSearchRequestStoreData.USER, "name", "one", "request", "<xml/>"));
        final SearchRequest sr = store.getRequestByAuthorAndName(user, null);
        assertNull(sr);
    }

    public void testGetRequestByAuthorAndNameUserHasNone()
    {
        UtilsForTests.getTestEntity(Table.NAME, EasyMap.build("author", TestOfBizSearchRequestStoreData.BOB, "name", "one", "request", "<xml/>"));
        UtilsForTests.getTestEntity(Table.NAME, EasyMap.build("author", TestOfBizSearchRequestStoreData.BOB, "name", "one", "request", "<xml/>"));

        final SearchRequest sr = store.getRequestByAuthorAndName(user, "one");
        assertNull(sr);

    }

    public void testGetRequestByAuthorAndNameMultiMatch()
    {
        UtilsForTests.getTestEntity(Table.NAME, EasyMap.build("author", TestOfBizSearchRequestStoreData.USER, "name", "one", "request", "<xml/>"));
        UtilsForTests.getTestEntity(Table.NAME, EasyMap.build("author", TestOfBizSearchRequestStoreData.USER, "name", "one", "request", "<xml/>"));
        try
        {
            store.getRequestByAuthorAndName(user, "one");
            fail("Should fail with multiple in the database");
        }
        catch (final IllegalArgumentException e)
        {
            // expected
        }
    }

    public void testGetRequestByAuthorAndNameMultiMatchDiffUsers() throws Exception
    {
        final GenericValue gv1 = UtilsForTests.getTestEntity(Table.NAME, EasyMap.build("author", TestOfBizSearchRequestStoreData.USER, "name", "one",
            "request", "<xml/>", "description", "test desc", "id", 12345L, "favCount", 0L));
        UtilsForTests.getTestEntity(Table.NAME, EasyMap.build("author", TestOfBizSearchRequestStoreData.BOB, "name", "one", "request", "<xml/>", "description", "test desc", "id", 23456L, "favCount", 0L));

        final SearchRequest sr = store.getRequestByAuthorAndName(user, "one");
        assertNotNull(sr);
        assertEquals(getSearchRequestFromGV(gv1), sr);
    }

    public void testGetRequestByAuthorAndNameUserHasMultiDifferent() throws Exception
    {
        final GenericValue gv1 = UtilsForTests.getTestEntity(Table.NAME, EasyMap.build("author", TestOfBizSearchRequestStoreData.USER, "name", "one",
            "request", "<xml/>", "description", "test desc", "id", 12345L, "favCount", 0L));
        UtilsForTests.getTestEntity(Table.NAME, EasyMap.build("author", TestOfBizSearchRequestStoreData.USER, "name", "two", "request", "<xml/>"));

        final SearchRequest sr = store.getRequestByAuthorAndName(user, "one");
        assertNotNull(sr);
        assertEquals(getSearchRequestFromGV(gv1), sr);
    }

    public void testGetSearchRequestNullId()
    {
        final SearchRequest sr = store.getSearchRequest(null);
        assertNull(sr);
    }

    public void testGetSearchRequestIDNoExist()
    {
        final SearchRequest sr = store.getSearchRequest(new Long(1));
        assertNull(sr);
    }

    public void testGetSearchRequestIDNoExistSomeDo()
    {
        UtilsForTests.getTestEntity(Table.NAME, EasyMap.build("author", TestOfBizSearchRequestStoreData.USER, "name", "one", "request", "<xml/>"));

        final SearchRequest sr = store.getSearchRequest(new Long(986));
        assertNull(sr);
    }

    public void testGetSearchRequestIDExists() throws Exception
    {
        final GenericValue gv1 = UtilsForTests.getTestEntity(Table.NAME, EasyMap.build("author", TestOfBizSearchRequestStoreData.USER, "name", "one",
            "request", "<xml/>", "description", "test desc", "id", 12345L, "favCount", 0L));

        final SearchRequest expectedSR = getSearchRequestFromGV(gv1);
        final SearchRequest sr = store.getSearchRequest(expectedSR.getId());
        assertNotNull(sr);
        assertEquals(expectedSR, sr);
    }

    public void testSaveNullRequest()
    {
        try
        {
            store.create(null);
            fail("can not save null search request");
        }
        catch (final IllegalArgumentException e)
        {
            // expected
        }
    }

    public void testSaveHappy() throws Exception
    {
        final SearchRequest sr = new SearchRequest(new QueryImpl(null, null, "project = 123"), user.getName(), "one", "description one", null, 0L);
        final SearchRequest result = store.create(sr);
        assertNotNull(result);

        final List<GenericValue> stored = delegator.findAll(Table.NAME);
        assertEquals(1, stored.size());
        final GenericValue storedGV = stored.iterator().next();
        assertEquals(sr.getName(), storedGV.getString("name"));
        assertEquals(sr.getDescription(), storedGV.getString("description"));
        assertEquals(new Long(0), storedGV.getLong("favCount"));

        final SearchRequest sr2 = new SearchRequest(new QueryImpl(null, null, "blah = blee"), user.getName(), "one222", "description one222", null, 0L);
        final SearchRequest result2 = store.create(sr2);
        assertNotNull(result2);

        final List<GenericValue> stored2 = delegator.findAll(Table.NAME);
        assertEquals(2, stored2.size());
        final Iterator<GenericValue> iterator = stored2.iterator();
        iterator.next();
        final GenericValue storedGV2 = iterator.next();
        assertEquals(sr2.getName(), storedGV2.getString("name"));
        assertEquals(sr2.getDescription(), storedGV2.getString("description"));
        assertEquals(new Long(0), storedGV.getLong("favCount"));
    }

    public void testUpdateNullRequest()
    {
        try
        {
            store.update(null);
            fail("Can not update null request");
        }
        catch (final IllegalArgumentException e)
        {
            // expected
        }
    }

    public void testUpdateHappy() throws Exception
    {
        final GenericValue gv1 = UtilsForTests.getTestEntity(Table.NAME, EasyMap.build("author", TestOfBizSearchRequestStoreData.USER, "name", "one",
            "description", "hello", "request", "<xml/>", "description", "test desc", "id", 12345L, "favCount", 0L));

        final SearchRequest expectedSR = getSearchRequestFromGV(gv1);
        final SearchRequest sr = store.getSearchRequest(expectedSR.getId());

        sr.setName("new Name");
        sr.setDescription("new Description");
        sr.setQuery(new QueryImpl(null, null, "project = 1234"));
        assertEquals(new Long(0), sr.getFavouriteCount());

        final SearchRequest result = store.update(sr);
        assertNotNull(result);
        assertEquals("new Name", result.getName());
        assertEquals("new Description", result.getDescription());
        assertEquals(new Long(0), sr.getFavouriteCount());
    }

    public void testUpdateChangedFavCount() throws Exception
    {
        final GenericValue gv1 = UtilsForTests.getTestEntity(Table.NAME, EasyMap.build("author", TestOfBizSearchRequestStoreData.USER, "name", "one",
            "description", "hello", "request", "<xml/>", "id", 12345L, "favCount", 0L));

        final SearchRequest expectedSR = getSearchRequestFromGV(gv1);
        final SearchRequest sr = store.getSearchRequest(expectedSR.getId());

        sr.setName("new Name");
        sr.setDescription("new Description");
        assertEquals(new Long(0), sr.getFavouriteCount());
        sr.setFavouriteCount(new Long(2));
        sr.setQuery(new QueryImpl(null, null, "project = 123"));
        gv1.set("favCount", new Long(3));
        gv1.store();

        final SearchRequest result = store.update(sr);
        assertNotNull(result);
        assertEquals("new Name", result.getName());
        assertEquals("new Description", result.getDescription());
        assertEquals(new Long(3), result.getFavouriteCount());
    }

    public void testRemoveIdNotExist()
    {
        try
        {
            store.delete(new Long(133));
            fail("can not delete request where id does not exist");
        }
        catch (final DataAccessException e)
        {
            // expected
        }
    }

    public void testRemoveHappy()
    {
        final GenericValue gv1 = UtilsForTests.getTestEntity(Table.NAME, EasyMap.build("author", TestOfBizSearchRequestStoreData.USER, "name", "one",
            "request", "<xml/>"));

        store.delete(gv1.getLong("id"));

        final List<GenericValue> stored = delegator.findAll(Table.NAME);
        assertTrue(stored.isEmpty());
    }

    public void testRemoveOneOfMany()
    {
        final GenericValue gv1 = UtilsForTests.getTestEntity(Table.NAME, EasyMap.build("author", TestOfBizSearchRequestStoreData.USER, "name", "one",
            "request", "<xml/>"));
        final GenericValue gv2 = UtilsForTests.getTestEntity(Table.NAME, EasyMap.build("author", TestOfBizSearchRequestStoreData.USER, "name", "one",
            "request", "<xml/>"));
        final GenericValue gv3 = UtilsForTests.getTestEntity(Table.NAME, EasyMap.build("author", TestOfBizSearchRequestStoreData.USER, "name", "one",
            "request", "<xml/>"));

        final List<GenericValue> expected = EasyList.build(gv2, gv3);
        store.delete(gv1.getLong("id"));

        final List<GenericValue> stored = delegator.findAll(Table.NAME);
        assertFalse(stored.isEmpty());
        assertEquals(expected, stored);
    }

    public void testGetSearchRequestsForProjectNullProject()
    {
        try
        {
            store.getSearchRequests((Project) null);
            fail("can not get requests for null project");
        }
        catch (final IllegalArgumentException e)
        {
            // expected
        }
    }

    public void testGetSearchRequestsForProjectWithNoRequests()
    {
        final GenericValue projectGv1 = UtilsForTests.getTestEntity("Project", EasyMap.build("name", "TEST", "key", "TEST"));
        final Project project = new ProjectImpl(projectGv1);

        final EnclosedIterable<SearchRequest> results = store.getSearchRequests(project);
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    public void testGetSearchRequestsForProjectWithSomeRequests() throws Exception
    {
        final GenericValue projectGv1 = UtilsForTests.getTestEntity("Project", EasyMap.build("name", "TEST", "key", "TEST"));
        final Project project = new ProjectImpl(projectGv1);

        final GenericValue gv1 = UtilsForTests.getTestEntity(Table.NAME, EasyMap.build("author", TestOfBizSearchRequestStoreData.USER, "name", "one",
            "request", "<xml/>", "project", project.getId(), "description", "test desc", "id", 12345L, "favCount", 0L));
        final GenericValue gv3 = UtilsForTests.getTestEntity(Table.NAME, EasyMap.build("author", TestOfBizSearchRequestStoreData.USER, "name", "one",
            "request", "<xml/>", "project", project.getId(), "description", "test desc", "id", 23456L, "favCount", 0L));

        final Collection<SearchRequest> expected = EasyList.build(getSearchRequestFromGV(gv1), getSearchRequestFromGV(gv3));
        final EnclosedIterable<SearchRequest> results = store.getSearchRequests(project);
        assertNotNull(results);
        assertEquals(expected, new EnclosedIterable.ListResolver<SearchRequest>().get(results));
    }

    /**
    * Make sure updating the favourite count works.
    *
    * @throws SearchException throw up to indicate some form of error.
    */
    public void testAdjustFavouriteCount() throws SearchException
    {
        final GenericValue srGv = UtilsForTests.getTestEntity(Table.NAME, EasyMap.build("author",
                TestOfBizSearchRequestStoreData.USER, "name", "one", "request", "<xml/>"));
        SearchRequest searchRequest = new SearchRequest(new QueryImpl(), TestOfBizSearchRequestStoreData.USER, "one", null, srGv.getLong("id"), 0L);

        // the initial value should be zero.
        assertEquals(0, searchRequest.getFavouriteCount().longValue());

        // increment the value +10.
        searchRequest = store.adjustFavouriteCount(searchRequest.getId(), 10);
        assertEquals(10, searchRequest.getFavouriteCount().longValue());

        // decrement the value to 4.
        searchRequest = store.adjustFavouriteCount(searchRequest.getId(), -6);
        assertEquals(4, searchRequest.getFavouriteCount().longValue());

        // decrement the value to 0.
        searchRequest = store.adjustFavouriteCount(searchRequest.getId(), -100);
        assertEquals(0, searchRequest.getFavouriteCount().longValue());
    }

    public void test_get_Empty()
    {
        final EnclosedIterable<SearchRequest> iterable = store.get(new RetrievalDescriptor(Collections.<Long> emptyList()));
        assertNotNull(iterable);
        assertTrue(iterable.isEmpty());
    }

    /**
     * Calling get with preserver order set to false means the returned objects can be any order but must all be there
     */
    public void test_get_NoOrder()
    {
        addGV(10000, "fred", "sr1", "sr1 description");
        addGV(10001, "fred", "sr2", "sr2 description");
        addGV(10002, "mary", "sr1", "sr1 description");
        addGV(10003, "mary", "sr2", "sr2 description");

        final List<Long> expectedList = EasyList.build(10003L, 10000L, 10001L);
        final EnclosedIterable<SearchRequest> iterable = store.get(new RetrievalDescriptor(expectedList, false));
        assertNotNull(iterable);
        assertEquals(3, iterable.size());
        //
        // order cant be guaranteed here
        assertIterableHasAllIds(iterable, new long[] { 10001, 10000, 10003 });
    }

    /**
     * Calling get with preserver order set to true means the returned objects MUST be in id list order
     */
    public void test_get_WithOrder()
    {
        addGV(10000, "fred", "sr1", "sr1 description");
        addGV(10001, "fred", "sr2", "sr2 description");
        addGV(10002, "mary", "sr1", "sr1 description");
        addGV(10003, "mary", "sr2", "sr2 description");

        final List<Long> expectedList = EasyList.build(10003L, 10000L, 10001L);

        final EnclosedIterable<SearchRequest> iterable = store.get(new RetrievalDescriptor(expectedList, true));
        assertNotNull(iterable);
        assertEquals(3, iterable.size());
        //
        // order CAN be guaranteed here
        final List<Long> actualList = toIdList(iterable);
        assertEquals(expectedList, actualList);
    }

    public void testGetAll()
    {
        EnclosedIterable<SearchRequest> iterable = store.getAll();
        assertNotNull(iterable);
        assertTrue(iterable.isEmpty());

        addGV(10000, "fred", "sr1", "sr1 description");
        addGV(10001, "fred", "sr2", "sr2 description");
        addGV(10002, "mary", "sr1", "sr1 description");
        addGV(10003, "mary", "sr2", "sr2 description");

        iterable = store.getAll();
        assertNotNull(iterable);
        assertEquals(4, iterable.size());
        assertIterableHasAllIds(iterable, new long[] { 10000, 10001, 10002, 10003 });
    }

    public void testGetAllIndexableSharedEntities()
    {
        EnclosedIterable<IndexableSharedEntity<SearchRequest>> iterable = store.getAllIndexableSharedEntities();
        assertNotNull(iterable);
        assertTrue(iterable.isEmpty());

        addGV(10000, "fred", "sr1", "sr1 description");
        addGV(10001, "fred", "sr2", "sr2 description");
        addGV(10002, "mary", "sr1", "sr1 description");
        addGV(10003, "mary", "sr2", "sr2 description");

        iterable = store.getAllIndexableSharedEntities();
        assertNotNull(iterable);
        assertEquals(4, iterable.size());

        final long[] expectedIds = new long[] { 10000, 10001, 10002, 10003 };
        assertEquals(expectedIds.length, iterable.size());
        assertCollectionHasAllIds(new EnclosedIterable.ListResolver<IndexableSharedEntity<SearchRequest>>().get(iterable), expectedIds);
    }

    private GenericValue addGV(final long id, final String username, final String name, final String description)
    {
        final Map<String, Comparable<?>> map = new HashMap<String, Comparable<?>>();
        map.put("id", id);
        map.put("name", name);
        map.put("description", description);
        map.put("author", username);
        map.put("favCount", 0L);
        return UtilsForTests.getTestEntity(Table.NAME, map);
    }

    private void assertIterableHasAllIds(final EnclosedIterable<SearchRequest> iterable, final long[] expectedIds)
    {
        assertEquals(expectedIds.length, iterable.size());
        assertCollectionHasAllIds(new EnclosedIterable.ListResolver<SearchRequest>().get(iterable), expectedIds);
    }

    private void assertCollectionHasAllIds(final Collection<? extends SharedEntity> collection, final long[] expectedIds)
    {
        final Set<Long> foundIds = new HashSet<Long>();
        for (final SharedEntity sr : collection)
        {
            for (final long id : expectedIds)
            {
                if (sr.getId() == id)
                {
                    if (foundIds.contains(id))
                    {
                        fail("The id : " + id + " was already found in the collection.  Ids must be unique");
                    }
                    else
                    {
                        foundIds.add(id);
                    }
                }
            }
        }
        if (foundIds.size() != expectedIds.length)
        {
            fail("Failed to find specified all ids");
        }
    }

    private List<Long> toIdList(final EnclosedIterable<SearchRequest> iterable)
    {
        assertNotNull(iterable);
        return CollectionUtil.transform(new EnclosedIterable.ListResolver<SearchRequest>().get(iterable), new Function<SearchRequest, Long>()
        {
            public Long get(final SearchRequest input)
            {
                return input.getId();
            }
        });
    }

    private class RetrievalDescriptor implements SharedEntityAccessor.RetrievalDescriptor
    {

        private final List<Long> ids;

        private final boolean preserverOrder;

        RetrievalDescriptor(final List<Long> ids)
        {
            this(ids, false);
        }

        RetrievalDescriptor(final List<Long> ids, final boolean preserverOrder)
        {
            this.ids = ids;
            this.preserverOrder = preserverOrder;
        }

        public List<Long> getIds()
        {
            return ids;
        }

        public boolean preserveOrder()
        {
            return preserverOrder;
        }
    }
}
