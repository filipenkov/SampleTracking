package com.atlassian.jira.sharing.index;

import com.atlassian.jira.index.Index;
import com.atlassian.jira.index.MockResult;
import com.atlassian.jira.issue.index.IndexException;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchRequestManager;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.portal.PortalPage;
import com.atlassian.jira.portal.PortalPageManager;
import com.atlassian.jira.sharing.IndexableSharedEntity;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.task.context.Contexts;
import com.atlassian.jira.util.FileFactory;
import com.atlassian.jira.util.collect.MockCloseableIterable;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.easymock.classextension.EasyMock.createNiceMock;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * A test case for DefaultSharedEntityIndexManager
 *
 * @since v3.13
 */
public class TestDefaultSharedEntityIndexManager extends MockControllerTestCase
{
    PortalPage portalPage1;
    SharedEntity indexableSearchRequest1;

    @Before
    public void setUp() throws Exception
    {
        portalPage1 = PortalPage.name("name").description("desc").owner("ownerName").build();
        indexableSearchRequest1 = new IndexableSharedEntity<SearchRequest>(1L, "name", "desc", SearchRequest.ENTITY_TYPE, "ownerName", 0L);
    }

    private void recordMocksForReIndexAll()
    {
        final Index.Result mockResult = new MockResult();
        final PortalPageManager portalPageManager = getMock(PortalPageManager.class);

        expect(portalPageManager.getAllIndexableSharedEntities()).andReturn(
            new MockCloseableIterable<SharedEntity>(Collections.<SharedEntity> singletonList(portalPage1)));

        final SearchRequestManager searchRequestManager = getMock(SearchRequestManager.class);
        expect(searchRequestManager.getAllIndexableSharedEntities()).andReturn(
            new MockCloseableIterable<SharedEntity>(Collections.singletonList(indexableSearchRequest1)));

        final SharedEntityIndexer indexer = getMock(SharedEntityIndexer.class);
        indexer.recreate(SearchRequest.ENTITY_TYPE);
        expect(indexer.index(indexableSearchRequest1)).andReturn(mockResult);

        indexer.recreate(PortalPage.ENTITY_TYPE);
        expect(indexer.index(portalPage1)).andReturn(mockResult);
    }

    @Test
    public void testReIndexAll() throws IndexException
    {
        recordMocksForReIndexAll();

        final DefaultSharedEntityIndexManager sharedEntityIndexManager = instantiate(DefaultSharedEntityIndexManager.class);

        final long time = sharedEntityIndexManager.reIndexAll(Contexts.nullContext());
        assertTrue(time >= 0);
    }

    @Test
    public void testReIndexAllThrowsIllegalArgForNullEvent() throws IndexException
    {
        final DefaultSharedEntityIndexManager sharedEntityIndexManager = instantiate(DefaultSharedEntityIndexManager.class);
        try
        {
            sharedEntityIndexManager.reIndexAll(null);
            fail("IllegalArg expected");
        }
        catch (final IllegalArgumentException ignore)
        {}
    }

    @Test
    public void testActivate() throws Exception
    {
        recordMocksForReIndexAll();

        final DefaultSharedEntityIndexManager sharedEntityIndexManager = instantiate(DefaultSharedEntityIndexManager.class);
        sharedEntityIndexManager.activate(Contexts.nullContext());
    }

    @Test
    public void testActivateThrowsIllegalArgForNullEvent() throws Exception
    {
        final DefaultSharedEntityIndexManager sharedEntityIndexManager = instantiate(DefaultSharedEntityIndexManager.class);
        try
        {
            sharedEntityIndexManager.activate(null);
            fail("IllegalArg expected");
        }
        catch (final IllegalArgumentException ignore)
        {}
    }

    @Test
    public void testShutdown()
    {
        final SharedEntityIndexer indexer = getMock(SharedEntityIndexer.class);
        indexer.shutdown(SearchRequest.ENTITY_TYPE);
        indexer.shutdown(PortalPage.ENTITY_TYPE);

        final DefaultSharedEntityIndexManager sharedEntityIndexManager = instantiate(DefaultSharedEntityIndexManager.class);
        sharedEntityIndexManager.shutdown();
    }

    @Test
    public void testDeactivate() throws Exception
    {
        final SharedEntityIndexer indexer = getMock(SharedEntityIndexer.class);
        expect(indexer.clear(SearchRequest.ENTITY_TYPE)).andReturn("testing");

        expect(indexer.clear(PortalPage.ENTITY_TYPE)).andReturn("testing");

        addObjectInstance(createNiceMock(FileFactory.class));
        final DefaultSharedEntityIndexManager sharedEntityIndexManager = instantiate(DefaultSharedEntityIndexManager.class);
        sharedEntityIndexManager.deactivate();
    }

    @Test
    public void testOptimize() throws Exception
    {
        final SharedEntityIndexer indexer = getMock(SharedEntityIndexer.class);
        expect(indexer.optimize(SearchRequest.ENTITY_TYPE)).andReturn(5L);
        expect(indexer.optimize(PortalPage.ENTITY_TYPE)).andReturn(2L);

        final DefaultSharedEntityIndexManager sharedEntityIndexManager = instantiate(DefaultSharedEntityIndexManager.class);
        final long actualTime = sharedEntityIndexManager.optimize();
        assertEquals(7, actualTime);
    }

    @Test
    public void testIsIndexingEnabled()
    {
        final DefaultSharedEntityIndexManager sharedEntityIndexManager = instantiate(DefaultSharedEntityIndexManager.class);
        assertTrue(sharedEntityIndexManager.isIndexingEnabled());
    }

    @Test
    public void testGetAllIndexPaths()
    {
        final List<String> expectedList = Lists.newArrayList("This should be returned");
        final SharedEntityIndexer indexer = getMock(SharedEntityIndexer.class);
        expect(indexer.getAllIndexPaths()).andReturn(expectedList);

        final DefaultSharedEntityIndexManager sharedEntityIndexManager = instantiate(DefaultSharedEntityIndexManager.class);
        final Collection<String> actualList = sharedEntityIndexManager.getAllIndexPaths();
        assertSame(expectedList, actualList);
    }
}
