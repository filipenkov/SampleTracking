package com.atlassian.jira.sharing;

import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.user.MockCrowdService;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.MockProviderAccessor;
import com.atlassian.jira.index.Index;
import com.atlassian.jira.index.MockResult;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.sharing.SharedEntity.SharePermissions;
import com.atlassian.jira.sharing.index.SharedEntityIndexer;
import com.atlassian.jira.sharing.search.SharedEntitySearchParameters;
import com.atlassian.jira.sharing.search.SharedEntitySearchResult;
import com.atlassian.jira.sharing.search.SharedEntitySearcher;
import com.atlassian.jira.util.Consumer;
import com.atlassian.jira.util.EasyList;
import com.atlassian.jira.util.collect.MockCloseableIterable;

import com.opensymphony.user.User;

import java.util.concurrent.atomic.AtomicInteger;

public class TestDefaultSharePermissionReindexer extends MockControllerTestCase
{
    @Test
    public void testConstructorThrowsWithNullArg() throws Exception
    {
        try
        {
            new DefaultSharePermissionReindexer(null);
            fail("IllegalArgumentException expected");
        }
        catch (final IllegalArgumentException ignore)
        {}
    }

    @Test
    public void testReindexThrowsWithUnknown() throws Exception
    {
        @SuppressWarnings("unchecked")
        final AtomicInteger closed = new AtomicInteger();

        final MockProviderAccessor accessor = new MockProviderAccessor();
        final User user = new User("test", accessor, new MockCrowdService());

        final MockSharedEntity entity = new MockSharedEntity(new Long(1), SearchRequest.ENTITY_TYPE, user, SharePermissions.GLOBAL);
        final MockCloseableIterable<SharedEntity> results = new MockCloseableIterable<SharedEntity>(EasyList.<SharedEntity> build(entity))
        {
            @Override
            public void foreach(final Consumer<SharedEntity> sink)
            {
                closed.incrementAndGet();
                super.foreach(sink);
            }
        };
        final SharedEntitySearcher<SharedEntity> searcher = getMock(SharedEntitySearcher.class);
        final SharedEntitySearchResult<SharedEntity> searchResult = new SharedEntitySearchResult<SharedEntity>(results, false, 0);
        expect(searcher.search(this.<SharedEntitySearchParameters>anyObject())).andStubReturn(searchResult);

        final SharedEntityIndexer indexer = getMock(SharedEntityIndexer.class);
        indexer.getSearcher(SearchRequest.ENTITY_TYPE);
        mockController.setDefaultReturnValue(searcher);
        // indexer is called once for each entity type (we return the same entity in the test for the different entity searches)
        final Index.Result mockResult = new MockResult();
        expect(indexer.index(entity)).andStubReturn(mockResult);
        final SharePermissionReindexer reindexer = instantiate(DefaultSharePermissionReindexer.class);

        // now the test
        reindexer.reindex(SharePermissions.GLOBAL.getPermissionSet().iterator().next());
        assertEquals(2, closed.get());
    }

    @Test
    public void testReindexThrowsIllegalStateIfIndexerDoesntSupportKnownReindexableTypes() throws Exception
    {
        final SharedEntityIndexer indexer = getMock(SharedEntityIndexer.class);
        expect(indexer.getSearcher(this.<SharedEntity.TypeDescriptor<SharedEntity>>anyObject())).andStubReturn(null);
        final SharePermissionReindexer reindexer = instantiate(DefaultSharePermissionReindexer.class);
        try
        {
            reindexer.reindex(SharePermissions.GLOBAL.getPermissionSet().iterator().next());
            fail("IllegalStateException expected");
        }
        catch (final IllegalStateException ignore)
        {}
    }
}
