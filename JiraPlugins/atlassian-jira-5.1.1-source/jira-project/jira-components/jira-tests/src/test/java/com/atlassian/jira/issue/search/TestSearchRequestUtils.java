package com.atlassian.jira.issue.search;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.After;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.core.test.util.DuckTypeProxy;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.NavigableField;
import com.atlassian.jira.issue.search.managers.SearchHandlerManager;
import com.atlassian.jira.issue.search.searchers.IssueSearcher;
import com.atlassian.jira.issue.search.searchers.SearcherGroup;
import com.atlassian.jira.issue.search.util.SearchSortUtil;
import com.atlassian.jira.issue.search.util.SearchSortUtilImpl;
import com.atlassian.jira.jql.ClauseHandler;
import com.atlassian.jira.security.auth.trustedapps.MockI18nHelper;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.order.OrderByImpl;
import com.atlassian.query.order.SearchSort;
import com.atlassian.query.order.SortOrder;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @since v3.13.3
 */
public class TestSearchRequestUtils extends MockControllerTestCase
{
    private FieldManager fieldManager;
    private SearchHandlerManager searchHandlerManager;
    private SearchSortUtil searchSortUtil;
    private I18nHelper i18n;

    @Before
    public void setUp() throws Exception
    {
        fieldManager = mockController.getMock(FieldManager.class);
        searchHandlerManager = new SearchHandlerManager()
        {
            @Override
            public Collection<IssueSearcher<?>> getSearchers(final User searcher, final SearchContext context)
            {
                return null;
            }

            @Override
            public Collection<IssueSearcher<?>> getAllSearchers()
            {
                return null;
            }

            @Override
            public Collection<SearcherGroup> getSearcherGroups(final SearchContext searchContext)
            {
                return null;
            }

            @Override
            public IssueSearcher<?> getSearcher(final String id)
            {
                return null;
            }

            @Override
            public void refresh()
            {
            }

            @Override
            public Collection<ClauseHandler> getClauseHandler(final User user, final String jqlClauseName)
            {
                return null;
            }

            @Override
            public Collection<ClauseHandler> getClauseHandler(final String jqlClauseName)
            {
                return null;
            }

            @Override
            public Collection<ClauseNames> getJqlClauseNames(final String fieldId)
            {
                return null;
            }

            @Override
            public Collection<String> getFieldIds(final User searcher, final String jqlClauseName)
            {
                return Collections.singleton(jqlClauseName);
            }

            @Override
            public Collection<String> getFieldIds(final String jqlClauseName)
            {
                return Collections.singleton(jqlClauseName);
            }

            @Override
            public Collection<ClauseNames> getVisibleJqlClauseNames(final User searcher)
            {
                return null;
            }

            @Override
            public Collection<ClauseHandler> getVisibleClauseHandlers(final User searcher)
            {
                return null;
            }

            @Override
            public Collection<IssueSearcher<?>> getSearchersByClauseName(final User user, final String jqlClauseName, final SearchContext searchContext)
            {
                return null;
            }
        };

        searchSortUtil = new SearchSortUtilImpl(searchHandlerManager, fieldManager);
        componentAccessorWorker.registerMock(SearchSortUtil.class, searchSortUtil);
        i18n = new MockI18nHelper();
    }

    @After
    public void tearDown() throws Exception
    {

    }

    @Test
    public void testGetSearchSortDescriptionsNoSorts() throws Exception
    {
        final Field mockField = createMockNavigableField("key");
        fieldManager.getField("key");
        mockController.setReturnValue(mockField);

        mockController.replay();

        SearchRequest sr = new SearchRequest();
        final List list = SearchRequestUtils.getSearchSortDescriptions(sr, fieldManager, searchHandlerManager, searchSortUtil, i18n, null);
        assertEquals(1, list.size());

        mockController.verify();
    }

    @Test
    public void testGetSearchSortDescriptionsOneNonNavigableSort() throws Exception
    {
        final String fieldId = "goodField";
        SearchRequest sr = new SearchRequest();
        sr.setQuery(new QueryImpl(null, new OrderByImpl(new SearchSort(fieldId, SortOrder.DESC)), null));

        final Field mockField = createMockField(fieldId);

        fieldManager.getField(fieldId);
        mockController.setReturnValue(mockField);

        mockController.replay();

        final List list = SearchRequestUtils.getSearchSortDescriptions(sr, fieldManager, searchHandlerManager, searchSortUtil, i18n, null);
        assertEquals(1, list.size());

        assertEquals("goodField", list.get(0));

        mockController.verify();
    }

    @Test
    public void testGetSearchSortDescriptionsOneGoodSortDesc() throws Exception
    {
        final String fieldId = "goodField";
        SearchRequest sr = new SearchRequest();
        sr.setQuery(new QueryImpl(null, new OrderByImpl(new SearchSort(fieldId, SortOrder.DESC)), null));

        final Field mockField = createMockNavigableField(fieldId);

        fieldManager.getField(fieldId);
        mockController.setReturnValue(mockField);

        mockController.replay();

        final List list = SearchRequestUtils.getSearchSortDescriptions(sr, fieldManager, searchHandlerManager, searchSortUtil, i18n, null);
        assertEquals(1, list.size());

        assertEquals("goodField navigator.hidden.sortby.descending", list.get(0));

        mockController.verify();
    }

    @Test
    public void testGetSearchSortDescriptionsOneGoodSortAsc() throws Exception
    {
        final String fieldId = "goodField";
        SearchRequest sr = new SearchRequest();
        sr.setQuery(new QueryImpl(null, new OrderByImpl(new SearchSort(fieldId, SortOrder.ASC)), null));

        final Field mockField = createMockNavigableField(fieldId);

        fieldManager.getField(fieldId);
        mockController.setReturnValue(mockField);

        mockController.replay();

        final List list = SearchRequestUtils.getSearchSortDescriptions(sr, fieldManager, searchHandlerManager, searchSortUtil, i18n, null);
        assertEquals(1, list.size());

        assertEquals("goodField navigator.hidden.sortby.ascending", list.get(0));

        mockController.verify();
    }

    @Test
    public void testGetSearchSortDescriptionsOneBadSort() throws Exception
    {
        final Field mockField = null;
        final String fieldId = "badField";
        SearchRequest sr = new SearchRequest();
        sr.setQuery(new QueryImpl(null, new OrderByImpl(new SearchSort(fieldId, SortOrder.DESC)), null));

        fieldManager.getField(fieldId);
        mockController.setReturnValue(mockField);

        mockController.replay();

        final List list = SearchRequestUtils.getSearchSortDescriptions(sr, fieldManager, searchHandlerManager, searchSortUtil, i18n, null);
        assertTrue(list.isEmpty());

        mockController.verify();
    }

    @Test
    public void testGetSearchSortDescriptionsTwoGoodSorts() throws Exception
    {
        final String fieldId1 = "goodField1";
        final String fieldId2 = "goodField2";
        SearchRequest sr = new SearchRequest();
        sr.setQuery(new QueryImpl(null, new OrderByImpl(new SearchSort(fieldId2, SortOrder.DESC), new SearchSort(fieldId1, SortOrder.ASC)), null));

        final Field mockField1 = createMockNavigableField(fieldId1);

        final Field mockField2 = createMockNavigableField(fieldId2);

        fieldManager.getField(fieldId2);
        mockController.setReturnValue(mockField2);

        fieldManager.getField(fieldId1);
        mockController.setReturnValue(mockField1);

        mockController.replay();

        final List list = SearchRequestUtils.getSearchSortDescriptions(sr, fieldManager, searchHandlerManager, searchSortUtil, i18n, null);
        assertEquals(2, list.size());

        assertEquals("goodField2 navigator.hidden.sortby.descending, navigator.hidden.sortby.then", list.get(0));
        assertEquals("goodField1 navigator.hidden.sortby.ascending", list.get(1));

        mockController.verify();
    }

    @Test
    public void testGetSearchSortDescriptionsOneGoodSortOneBadSort() throws Exception
    {
        final String fieldId1 = "goodField1";
        final String fieldId2 = "goodField2";
        SearchRequest sr = new SearchRequest();
        sr.setQuery(new QueryImpl(null, new OrderByImpl(new SearchSort("DESC", fieldId2), new SearchSort("ASC", fieldId1)), null));

        final Field mockField1 = createMockNavigableField(fieldId1);
        final Field mockField2 = null;

        fieldManager.getField(fieldId2);
        mockController.setReturnValue(mockField2);

        fieldManager.getField(fieldId1);
        mockController.setReturnValue(mockField1);

        mockController.replay();

        final List list = SearchRequestUtils.getSearchSortDescriptions(sr, fieldManager, searchHandlerManager, searchSortUtil, i18n, null);
        assertEquals(1, list.size());

        assertEquals("goodField1 navigator.hidden.sortby.ascending", list.get(0));

        mockController.verify();
    }

    @Test
    public void testGetSearchSortDescriptionsOneGoodOneBadOneGoodSort() throws Exception
    {
        final String fieldId1 = "goodField1";
        final String fieldId2 = "goodField2";
        final String fieldId3 = "goodField3";
        SearchRequest sr = new SearchRequest();
        sr.setQuery(new QueryImpl(null, new OrderByImpl(new SearchSort("DESC", fieldId3), new SearchSort("DESC", fieldId2), new SearchSort("ASC", fieldId1)), null));

        final Field mockField1 = createMockNavigableField(fieldId1);
        final Field mockField2 = null;
        final Field mockField3 = createMockNavigableField(fieldId3);

        fieldManager.getField(fieldId3);
        mockController.setReturnValue(mockField3);

        fieldManager.getField(fieldId2);
        mockController.setReturnValue(mockField2);

        fieldManager.getField(fieldId1);
        mockController.setReturnValue(mockField1);

        mockController.replay();

        final List list = SearchRequestUtils.getSearchSortDescriptions(sr, fieldManager, searchHandlerManager, searchSortUtil, i18n, null);
        assertEquals(2, list.size());

        assertEquals("goodField3 navigator.hidden.sortby.descending, navigator.hidden.sortby.then", list.get(0));
        assertEquals("goodField1 navigator.hidden.sortby.ascending", list.get(1));

        mockController.verify();
    }

    @Test
    public void testGetSearchSortDescriptionsOneBadTwoGoodSort() throws Exception
    {
        final String fieldId1 = "goodField1";
        final String fieldId2 = "goodField2";
        final String fieldId3 = "goodField3";
        SearchRequest sr = new SearchRequest();
        sr.setQuery(new QueryImpl(null, new OrderByImpl(new SearchSort("DESC", fieldId3), new SearchSort("DESC", fieldId2), new SearchSort("ASC", fieldId1)), null));

        final Field mockField1 = null;
        final Field mockField2 = createMockNavigableField(fieldId2);
        final Field mockField3 = createMockNavigableField(fieldId3);

        fieldManager.getField(fieldId3);
        mockController.setReturnValue(mockField3);

        fieldManager.getField(fieldId2);
        mockController.setReturnValue(mockField2);

        fieldManager.getField(fieldId1);
        mockController.setReturnValue(mockField1);

        mockController.replay();

        final List list = SearchRequestUtils.getSearchSortDescriptions(sr, fieldManager, searchHandlerManager, searchSortUtil, i18n, null);
        assertEquals(2, list.size());

        assertEquals("goodField3 navigator.hidden.sortby.descending, navigator.hidden.sortby.then", list.get(0));
        assertEquals("goodField2 navigator.hidden.sortby.descending", list.get(1));

        mockController.verify();
    }

    private NavigableField createMockNavigableField(final String nameKey)
    {
        Object o = new Object()
        {
            public String getNameKey()
            {
                return nameKey;
            }
        };

        return (NavigableField) DuckTypeProxy.getProxy(NavigableField.class, o);
    }

    private Field createMockField(final String nameKey)
    {
        Object o = new Object()
        {
            public String getNameKey()
            {
                return nameKey;
            }
        };

        return (Field) DuckTypeProxy.getProxy(Field.class, o);
    }
}
