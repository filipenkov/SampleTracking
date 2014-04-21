package com.atlassian.jira.issue.search.util;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.managers.SearchHandlerManager;
import com.atlassian.jira.jql.ClauseHandler;
import com.atlassian.jira.jql.ClauseInformation;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.context.ClauseContextFactory;
import com.atlassian.jira.jql.permission.ClausePermissionHandler;
import com.atlassian.jira.jql.query.ClauseQueryFactory;
import com.atlassian.jira.jql.validator.ClauseValidator;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;
import com.atlassian.query.order.OrderBy;
import com.atlassian.query.order.OrderByImpl;
import com.atlassian.query.order.SearchSort;
import com.atlassian.query.order.SortOrder;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @since v4.0
 */
public class TestSearchSortUtilImpl extends MockControllerTestCase
{
    private SearchHandlerManager searchHandlerManager;
    private FieldManager fieldManager;
    private SearchSortUtilImpl searchSortUtil;
    private User theUser = null;

    @Before
    public void setUp() throws Exception
    {
        searchHandlerManager = mockController.getMock(SearchHandlerManager.class);
        fieldManager = mockController.getMock(FieldManager.class);

        searchSortUtil = new SearchSortUtilImpl(searchHandlerManager, fieldManager);
    }

    @Test
    public void testGetSearchSortsNullQuery() throws Exception
    {
        mockController.replay();
        final List<SearchSort> sorts = searchSortUtil.getSearchSorts(null);

        assertNotNull(sorts);
        assertEquals(1, sorts.size());
        assertEquals(SearchSortUtilImpl.DEFAULT_KEY_SORT, sorts.get(0));

        mockController.verify();
    }

    @Test
    public void testGetSearchSortsNullOrderBy() throws Exception
    {
        mockController.replay();
        final List<SearchSort> sorts = searchSortUtil.getSearchSorts(new QueryImpl(null, null, null));

        assertNull(sorts);
        mockController.verify();
    }

    @Test
    public void testGetSearchSortsEmptyOrderByWhereClauseContainsText() throws Exception
    {
        mockController.replay();
        final List<SearchSort> sorts = searchSortUtil.getSearchSorts(new QueryImpl(new TerminalClauseImpl("test", Operator.LIKE, "blah")));

        assertTrue(sorts.isEmpty());

        mockController.verify();
    }

    @Test
    public void testGetSearchSortsHappyPath() throws Exception
    {
        mockController.replay();
        final SearchSort mySort = new SearchSort("Test", SortOrder.DESC);
        final List<SearchSort> sorts = searchSortUtil.getSearchSorts(new QueryImpl(null, new OrderByImpl(mySort), null));

        assertNotNull(sorts);
        assertEquals(1, sorts.size());
        assertEquals(mySort, sorts.get(0));

        mockController.verify();
    }

    @Test
    public void testGetOrderByClauseNoSorts() throws Exception
    {
        Map params = new HashMap();

        mockController.replay();

        final OrderBy generatedOrderBy = searchSortUtil.getOrderByClause(params);

        assertEquals(0, generatedOrderBy.getSearchSorts().size());

        mockController.verify();
    }

    @Test
    public void testGetOrderByClauseUnevenParameters() throws Exception
    {
        Map params = new HashMap();
        final String testField1 = "testField1";
        final String testField2 = "testField2";
        params.put(SearchSortUtil.SORTER_FIELD, new String[]{testField1, testField2});
        params.put(SearchSortUtil.SORTER_ORDER, new String[]{"ASC"});

        searchHandlerManager.getJqlClauseNames(testField1);
        mockController.setReturnValue(Collections.singleton(new ClauseNames("testClause1")));

        fieldManager.isNavigableField(testField1);
        mockController.setReturnValue(true);

        mockController.replay();

        final OrderBy expectedOrderBy = new OrderByImpl(new SearchSort("testClause1", SortOrder.ASC));
        final OrderBy generatedOrderBy = searchSortUtil.getOrderByClause(params);

        assertEquals(expectedOrderBy, generatedOrderBy);

        mockController.verify();
    }

    @Test
    public void testGetOrderByClauseOneNoMatchingClause() throws Exception
    {
        Map params = new HashMap();
        final String testField1 = "testField1";
        final String testField2 = "testField2";
        params.put(SearchSortUtil.SORTER_FIELD, new String[]{testField1, testField2});
        params.put(SearchSortUtil.SORTER_ORDER, new String[]{"ASC", "DESC"});

        searchHandlerManager.getJqlClauseNames(testField1);
        mockController.setReturnValue(Collections.singleton(new ClauseNames("testClause1")));
        searchHandlerManager.getJqlClauseNames(testField2);
        mockController.setReturnValue(Collections.emptyList());

        fieldManager.isNavigableField(testField1);
        mockController.setReturnValue(true);

        mockController.replay();

        final OrderBy expectedOrderBy = new OrderByImpl(new SearchSort("testClause1", SortOrder.ASC));
        final OrderBy generatedOrderBy = searchSortUtil.getOrderByClause(params);

        assertEquals(expectedOrderBy, generatedOrderBy);

        mockController.verify();
    }

    @Test
    public void testGetOrderByClauseOneNotNavigable() throws Exception
    {
        Map params = new HashMap();
        final String testField1 = "testField1";
        final String testField2 = "testField2";
        params.put(SearchSortUtil.SORTER_FIELD, new String[]{testField1, testField2});
        params.put(SearchSortUtil.SORTER_ORDER, new String[]{"ASC", "DESC"});

        searchHandlerManager.getJqlClauseNames(testField1);
        mockController.setReturnValue(Collections.singleton(new ClauseNames("testClause1")));
        searchHandlerManager.getJqlClauseNames(testField2);
        mockController.setReturnValue(Collections.singleton(new ClauseNames("testClause2")));

        fieldManager.isNavigableField(testField1);
        mockController.setReturnValue(true);
        fieldManager.isNavigableField(testField2);
        mockController.setReturnValue(false);

        mockController.replay();

        final OrderBy expectedOrderBy = new OrderByImpl(new SearchSort("testClause1", SortOrder.ASC));
        final OrderBy generatedOrderBy = searchSortUtil.getOrderByClause(params);

        assertEquals(expectedOrderBy, generatedOrderBy);

        mockController.verify();
    }

    @Test
    public void testGetOrderByClauseHappyPath() throws Exception
    {
        Map params = new HashMap();
        final String testField1 = "testField1";
        final String testField2 = "testField2";
        params.put(SearchSortUtil.SORTER_FIELD, new String[]{testField1, testField2});
        params.put(SearchSortUtil.SORTER_ORDER, new String[]{"ASC", "DESC"});

        searchHandlerManager.getJqlClauseNames(testField1);
        mockController.setReturnValue(Collections.singleton(new ClauseNames("testClause1")));
        searchHandlerManager.getJqlClauseNames(testField2);
        mockController.setReturnValue(Collections.singleton(new ClauseNames("testClause2")));

        fieldManager.isNavigableField(testField1);
        mockController.setReturnValue(true);
        fieldManager.isNavigableField(testField2);
        mockController.setReturnValue(true);

        mockController.replay();

        final OrderBy expectedOrderBy = new OrderByImpl(new SearchSort("testClause1", SortOrder.ASC), new SearchSort("testClause2", SortOrder.DESC));
        final OrderBy generatedOrderBy = searchSortUtil.getOrderByClause(params);

        assertEquals(expectedOrderBy, generatedOrderBy);

        mockController.verify();
    }

    @Test
    public void testConcatSearchSortsHappyPath() throws Exception
    {
        final String test1 = "test1";
        final String test2 = "test2";
        final String test3 = "test3";
        final String test4 = "test4";
        final SearchSort sort1 = new SearchSort(test1, SortOrder.ASC);
        final SearchSort sort2 = new SearchSort(test2, SortOrder.ASC);
        final SearchSort sort3 = new SearchSort(test3, SortOrder.ASC);
        final SearchSort sort4 = new SearchSort(test4, SortOrder.ASC);
        final Collection<SearchSort> oldSorts = CollectionBuilder.newBuilder(sort1, sort2).asCollection();
        final Collection<SearchSort> newSorts = CollectionBuilder.newBuilder(sort3, sort4).asCollection();

        mockController.replay();

        final List<SearchSort> expectedSorts = CollectionBuilder.newBuilder(sort3, sort4, sort1).asList();

        final List<SearchSort> generatedSorts = searchSortUtil.concatSearchSorts(newSorts, oldSorts, 3);

        assertEquals(expectedSorts, generatedSorts);

        mockController.verify();
    }

    @Test
    public void testConcatSearchSortsNullOldSorts() throws Exception
    {
        final String test1 = "test1";
        final String test2 = "test2";
        final String test3 = "test3";
        final String test4 = "test4";
        final SearchSort sort1 = new SearchSort(test1, SortOrder.ASC);
        final SearchSort sort2 = new SearchSort(test2, SortOrder.ASC);
        final SearchSort sort3 = new SearchSort(test3, SortOrder.ASC);
        final SearchSort sort4 = new SearchSort(test4, SortOrder.ASC);
        final Collection<SearchSort> oldSorts = null;
        final Collection<SearchSort> newSorts = CollectionBuilder.newBuilder(sort3, sort4, sort1, sort2).asCollection();

        mockController.replay();

        final List<SearchSort> expectedSorts = CollectionBuilder.newBuilder(sort3, sort4, sort1).asList();

        final List<SearchSort> generatedSorts = searchSortUtil.concatSearchSorts(newSorts, oldSorts, 3);

        assertEquals(expectedSorts, generatedSorts);

        mockController.verify();
    }

    @Test
    public void testConcatSearchSortsNoSorts() throws Exception
    {
        mockController.replay();

        assertTrue(searchSortUtil.concatSearchSorts(Collections.<SearchSort>emptyList(), Collections.<SearchSort>emptyList(), 3).isEmpty());

        mockController.verify();
    }

    @Test
    public void testConcatSearchSortsNullNewSorts() throws Exception
    {
        mockController.replay();

        try
        {
            searchSortUtil.concatSearchSorts(null, Collections.<SearchSort>emptyList(), 3);
            fail("Should throw IAE");
        }
        catch (IllegalArgumentException e)
        {
            //expected
        }

        mockController.verify();
    }

    @Test
    public void testMergeSearchSortsHappyPath() throws Exception
    {
        final String test1 = "test1";
        final String test2 = "test2";
        final String test3 = "test3";
        final String test4 = "test4";
        final SearchSort sort1 = new SearchSort(test1, SortOrder.ASC);
        final SearchSort sort2 = new SearchSort(test2, SortOrder.ASC);
        final SearchSort sort3 = new SearchSort(test3, SortOrder.ASC);
        final SearchSort sort4 = new SearchSort(test4, SortOrder.ASC);
        final Collection<SearchSort> oldSorts = CollectionBuilder.newBuilder(sort1, sort2).asCollection();
        final Collection<SearchSort> newSorts = CollectionBuilder.newBuilder(sort3, sort4).asCollection();

        expect(searchHandlerManager.getClauseHandler((User) null, test3))
                .andReturn(Collections.singleton(createClauseHandler(test3)));

        expect(searchHandlerManager.getClauseHandler((User) null, test4))
                .andReturn(Collections.singleton(createClauseHandler(test4)));

        expect(searchHandlerManager.getClauseHandler((User) null, test1))
                .andReturn(Collections.singleton(createClauseHandler(test1)));

        mockController.replay();

        searchSortUtil = new SearchSortUtilImpl(searchHandlerManager, fieldManager)
        {
            @Override
            Collection<SearchSort> convertNewSortsToKeepOldSortNames(final User user, final Collection<SearchSort> newSorts, final Collection<SearchSort> oldSorts)
            {
                return newSorts;
            }
        };

        final List<SearchSort> expectedSorts = CollectionBuilder.newBuilder(sort3, sort4, sort1).asList();

        final List<SearchSort> generatedSorts = searchSortUtil.mergeSearchSorts(null, newSorts, oldSorts, 3);

        assertEquals(expectedSorts, generatedSorts);

        mockController.verify();
    }

    @Test
    public void testMergeSearchSortsResolvesToMultipleFieldsWithOverlap() throws Exception
    {
        // clause1 maps to [field1, field2]
        // clause2 maps to [field2, field3]
        // clause3 maps to [field3, field4]
        final String clause1 = "clause1";
        final String clause2 = "clause2";
        final String clause3 = "clause3";
        final String field1 = "field1";
        final String field2 = "field2";
        final String field3 = "field3";
        final String field4 = "field4";
        final SearchSort inSort1 = new SearchSort(clause1, SortOrder.ASC);
        final SearchSort inSort2 = new SearchSort(clause2, SortOrder.DESC);
        final SearchSort inSort3 = new SearchSort(clause3, SortOrder.ASC);
        final SearchSort outSort1 = new SearchSort(clause1, SortOrder.ASC);
        final SearchSort outSort2 = new SearchSort(field3, SortOrder.DESC);
        final SearchSort outSort3 = new SearchSort(field4, SortOrder.ASC);
        final Collection<SearchSort> oldSorts = null;
        final Collection<SearchSort> newSorts = CollectionBuilder.newBuilder(inSort1, inSort2, inSort3).asCollection();

        expect(searchHandlerManager.getClauseHandler((User) null, clause1))
                .andReturn(CollectionBuilder.newBuilder(createClauseHandler(field2, clause1), createClauseHandler(field1, clause1)).asList());

        expect(searchHandlerManager.getClauseHandler((User) null, clause2))
                .andReturn(CollectionBuilder.newBuilder(createClauseHandler(field3, clause2), createClauseHandler(field2, clause2)).asList());

        expect(searchHandlerManager.getClauseHandler((User) null, clause3))
                .andReturn(CollectionBuilder.newBuilder(createClauseHandler(field3, clause3), createClauseHandler(field4, clause3)).asList());

        mockController.replay();

        final List<SearchSort> expectedSorts = CollectionBuilder.newBuilder(outSort1, outSort2, outSort3).asList();

        final List<SearchSort> generatedSorts = searchSortUtil.mergeSearchSorts(null, newSorts, oldSorts, Integer.MAX_VALUE);

        assertEquals(expectedSorts, generatedSorts);

        mockController.verify();
    }

    @Test
    public void testMergeSearchSortsTwoCustomFieldsWithSameDisplayNameOneExplicitOneBoth() throws Exception
    {
        // "NF" is the display name for both custom fields
        // incoming sorts are cf[10005] DESC, NF ASC
        // outgoing should be cf[10005] DESC, cf[10004] ASC
        final String clause1 = "cf[10005]";
        final String clause2 = "cf[10004]";
        final String clause3 = "NF";
        final SearchSort sort1 = new SearchSort(clause1, SortOrder.DESC);
        final SearchSort sort2 = new SearchSort(clause2, SortOrder.ASC);
        final SearchSort sort3 = new SearchSort(clause3, SortOrder.ASC);
        final Collection<SearchSort> oldSorts = Collections.singleton(sort3);
        final Collection<SearchSort> newSorts = Collections.singleton(sort1);

        expect(searchHandlerManager.getClauseHandler((User) null, clause1))
                .andReturn(CollectionBuilder.newBuilder(createClauseHandler(clause1, clause3)).asList());

        expect(searchHandlerManager.getClauseHandler((User) null, clause3))
                .andReturn(CollectionBuilder.newBuilder(createClauseHandler(clause1, clause3), createClauseHandler(clause2, clause3)).asList());

        mockController.replay();

        searchSortUtil = new SearchSortUtilImpl(searchHandlerManager, fieldManager)
        {
            @Override
            Collection<SearchSort> convertNewSortsToKeepOldSortNames(final User user, final Collection<SearchSort> newSorts, final Collection<SearchSort> oldSorts)
            {
                return newSorts;
            }
        };

        final List<SearchSort> expectedSorts = CollectionBuilder.newBuilder(sort1, sort2).asList();

        final List<SearchSort> generatedSorts = searchSortUtil.mergeSearchSorts(null, newSorts, oldSorts, Integer.MAX_VALUE);

        assertEquals(expectedSorts, generatedSorts);

        mockController.verify();
    }

    @Test
    public void testMergeSearchSortsThreeCustomFieldsTwoWithSameDisplayNameBothUseDisplay() throws Exception
    {
        // "CSF" is the display name for one custom field [10005]
        // "NF" is the display name for two custom fields [10004, 10003]
        // the display names should not be "split"
        final String clause1 = "CSF";
        final String clause2 = "NF";
        final String primary1 = "cf[10005]";
        final String primary2 = "cf[10004]";
        final String primary3 = "cf[10003]";
        final SearchSort sort1 = new SearchSort(clause1, SortOrder.DESC);
        final SearchSort sort2 = new SearchSort(clause2, SortOrder.ASC);
        final Collection<SearchSort> newSorts = Collections.singleton(sort1);
        final Collection<SearchSort> oldSorts = Collections.singleton(sort2);

        expect(searchHandlerManager.getClauseHandler((User) null, clause1))
                .andReturn(CollectionBuilder.newBuilder(createClauseHandler(primary1, clause1)).asList());

        expect(searchHandlerManager.getClauseHandler((User) null, clause2))
                .andReturn(CollectionBuilder.newBuilder(createClauseHandler(primary2, clause2), createClauseHandler(primary3, clause2)).asList());

        mockController.replay();

        searchSortUtil = new SearchSortUtilImpl(searchHandlerManager, fieldManager)
        {
            @Override
            Collection<SearchSort> convertNewSortsToKeepOldSortNames(final User user, final Collection<SearchSort> newSorts, final Collection<SearchSort> oldSorts)
            {
                return newSorts;
            }
        };

        final List<SearchSort> expectedSorts = CollectionBuilder.newBuilder(sort1, sort2).asList();

        final List<SearchSort> generatedSorts = searchSortUtil.mergeSearchSorts(null, newSorts, oldSorts, Integer.MAX_VALUE);

        assertEquals(expectedSorts, generatedSorts);

        mockController.verify();
    }

    @Test
    public void testMergeSearchSortsResolvesToSameFieldInNewSorts() throws Exception
    {
        final String clause1 = "clause1";
        final String clause2 = "clause2";
        final String clause3 = "clause3";
        final String clause4 = "clause4";
        final String field1 = "field1";
        final String field2 = "field2";
        final String field3 = "field3";

        final SearchSort sort1 = new SearchSort(clause1, SortOrder.ASC);
        final SearchSort sort2 = new SearchSort(clause2, SortOrder.ASC);
        final SearchSort sort3 = new SearchSort(clause3, SortOrder.ASC);
        final SearchSort sort4 = new SearchSort(clause4, SortOrder.ASC);
        final Collection<SearchSort> oldSorts = null;
        final Collection<SearchSort> newSorts = CollectionBuilder.newBuilder(sort1, sort2, sort3, sort4).asCollection();

        expect(searchHandlerManager.getClauseHandler((User) null, clause1))
                .andReturn(CollectionBuilder.newBuilder(createClauseHandler(field1, clause1, clause2)).asList());

        expect(searchHandlerManager.getClauseHandler((User) null, clause2))
                .andReturn(CollectionBuilder.newBuilder(createClauseHandler(field1, clause1, clause2)).asList());

        expect(searchHandlerManager.getClauseHandler((User) null, clause3))
                .andReturn(CollectionBuilder.newBuilder(createClauseHandler(field2, clause3)).asList());

        expect(searchHandlerManager.getClauseHandler((User) null, clause4))
                .andReturn(CollectionBuilder.newBuilder(createClauseHandler(field3, clause4)).asList());

        mockController.replay();

        final List<SearchSort> expectedSorts = CollectionBuilder.newBuilder(sort1, sort3, sort4).asList();

        final List<SearchSort> generatedSorts = searchSortUtil.mergeSearchSorts(null, newSorts, oldSorts, Integer.MAX_VALUE);

        assertEquals(expectedSorts, generatedSorts);

        mockController.verify();
    }

    @Test
    public void testMergeSearchSortsResolvesToSameField() throws Exception
    {
        final String clause1 = "clause1";
        final String clause2 = "clause2";
        final String clause3 = "clause3";
        final String clause4 = "clause4";
        final String field1 = "field1";
        final String field2 = "field2";
        final String field3 = "field3";

        final SearchSort sort1 = new SearchSort(clause1, SortOrder.ASC);
        final SearchSort sort2 = new SearchSort(clause2, SortOrder.ASC);
        final SearchSort sort3 = new SearchSort(clause3, SortOrder.ASC);
        final SearchSort sort4 = new SearchSort(clause4, SortOrder.ASC);
        final Collection<SearchSort> oldSorts = CollectionBuilder.newBuilder(sort1, sort2).asCollection();
        final Collection<SearchSort> newSorts = CollectionBuilder.newBuilder(sort3, sort4).asCollection();

        expect(searchHandlerManager.getClauseHandler((User) null, clause3))
                .andReturn(CollectionBuilder.newBuilder(createClauseHandler(field2, clause3)).asList());

        expect(searchHandlerManager.getClauseHandler((User) null, clause4))
                .andReturn(CollectionBuilder.newBuilder(createClauseHandler(field1, clause1, clause4)).asList());

        expect(searchHandlerManager.getClauseHandler((User) null, clause1))
                .andReturn(CollectionBuilder.newBuilder(createClauseHandler(field1, clause1, clause4)).asList());

        expect(searchHandlerManager.getClauseHandler((User) null, clause2))
                .andReturn(CollectionBuilder.newBuilder(createClauseHandler(field3, clause2)).asList());

        mockController.replay();

        searchSortUtil = new SearchSortUtilImpl(searchHandlerManager, fieldManager)
        {
            @Override
            Collection<SearchSort> convertNewSortsToKeepOldSortNames(final User user, final Collection<SearchSort> newSorts, final Collection<SearchSort> oldSorts)
            {
                return newSorts;
            }
        };

        final List<SearchSort> expectedSorts = CollectionBuilder.newBuilder(sort3, sort4, sort2).asList();

        final List<SearchSort> generatedSorts = searchSortUtil.mergeSearchSorts(null, newSorts, oldSorts, 3);

        assertEquals(expectedSorts, generatedSorts);

        mockController.verify();
    }

    @Test
    public void testMergeSearchSortsNoSorts() throws Exception
    {
        mockController.replay();

        assertTrue(searchSortUtil.mergeSearchSorts(null, Collections.<SearchSort>emptyList(), Collections.<SearchSort>emptyList(), 3).isEmpty());

        mockController.verify();
    }

    @Test
    public void testMergeSearchSortsNullNewSorts() throws Exception
    {
        mockController.replay();

        try
        {
            searchSortUtil.mergeSearchSorts(null, null, Collections.<SearchSort>emptyList(), 3);
            fail("Should throw IAE");
        }
        catch (IllegalArgumentException e)
        {
            //expected
        }

        mockController.verify();
    }

    @Test
    public void testConvertNewSortsToKeepOldSortNamesOneNewOneOldDoConversion() throws Exception
    {
        final String clause1 = "clause1";
        final String clause2 = "clause2";

        final Collection<SearchSort> newSorts = Collections.singletonList(new SearchSort(clause1, SortOrder.ASC));
        final Collection<SearchSort> oldSorts = Collections.singletonList(new SearchSort(clause2, SortOrder.DESC));

        expect(searchHandlerManager.getClauseHandler(theUser, clause1))
                .andReturn(Collections.singleton(createClauseHandler(clause1, clause2)));

        expect(searchHandlerManager.getClauseHandler(theUser, clause2))
                .andReturn(Collections.singleton(createClauseHandler(clause1, clause2)));

        mockController.replay();

        final Collection<SearchSort> result = searchSortUtil.convertNewSortsToKeepOldSortNames(theUser, newSorts, oldSorts);
        assertEquals(1, result.size());
        assertEquals(new SearchSort(clause2, SortOrder.ASC), result.iterator().next());
    }

    @Test
    public void testConvertNewSortsToKeepOldSortNamesOneNewOneOldAlreadySameField() throws Exception
    {
        final String clause1 = "clause1";
        final String clause2 = "clause2";

        final Collection<SearchSort> newSorts = Collections.singletonList(new SearchSort(clause1, SortOrder.ASC));
        final Collection<SearchSort> oldSorts = Collections.singletonList(new SearchSort(clause1, SortOrder.DESC));

        expect(searchHandlerManager.getClauseHandler(theUser, clause1))
                .andReturn(Collections.singleton(createClauseHandler(clause1, clause2))).times(2);

        mockController.replay();

        final Collection<SearchSort> result = searchSortUtil.convertNewSortsToKeepOldSortNames(theUser, newSorts, oldSorts);
        assertEquals(newSorts, result);
    }

    @Test
    public void testConvertNewSortsToKeepOldSortNamesOneNewOneOldDifferentClauseNames() throws Exception
    {
        final String clause1 = "clause1";
        final String clause2 = "clause2";

        final Collection<SearchSort> newSorts = Collections.singletonList(new SearchSort(clause1, SortOrder.ASC));
        final Collection<SearchSort> oldSorts = Collections.singletonList(new SearchSort(clause2, SortOrder.DESC));

        expect(searchHandlerManager.getClauseHandler(theUser, clause1))
                .andReturn(Collections.singleton(createClauseHandler(clause1)));

        expect(searchHandlerManager.getClauseHandler(theUser, clause2))
                .andReturn(Collections.singleton(createClauseHandler(clause2)));

        mockController.replay();

        final Collection<SearchSort> result = searchSortUtil.convertNewSortsToKeepOldSortNames(theUser, newSorts, oldSorts);
        assertEquals(newSorts, result);
    }

    @Test
    public void testConvertNewSortsToKeepOldSortNamesNewReturnsMultipleClauseHandlers() throws Exception
    {
        final String clause1 = "clause1";
        final String clause2 = "clause2";

        final Collection<SearchSort> newSorts = Collections.singletonList(new SearchSort(clause1, SortOrder.ASC));
        final Collection<SearchSort> oldSorts = Collections.singletonList(new SearchSort(clause2, SortOrder.DESC));

        expect(searchHandlerManager.getClauseHandler(theUser, clause1))
                .andReturn(CollectionBuilder.newBuilder(createClauseHandler(clause1, clause2), createClauseHandler(clause1, clause2)).asList());

        mockController.replay();

        final Collection<SearchSort> result = searchSortUtil.convertNewSortsToKeepOldSortNames(theUser, newSorts, oldSorts);
        assertEquals(newSorts, result);
    }

    @Test
    public void testConvertNewSortsToKeepOldSortNamesOldReturnsMultipleClauseHandlers() throws Exception
    {
        final String clause1 = "clause1";
        final String clause2 = "clause2";

        final Collection<SearchSort> newSorts = Collections.singletonList(new SearchSort(clause1, SortOrder.ASC));
        final Collection<SearchSort> oldSorts = Collections.singletonList(new SearchSort(clause2, SortOrder.DESC));

        expect(searchHandlerManager.getClauseHandler(theUser, clause1))
                .andReturn(CollectionBuilder.newBuilder(createClauseHandler(clause1, clause2)).asList());

        expect(searchHandlerManager.getClauseHandler(theUser, clause2))
                .andReturn(CollectionBuilder.newBuilder(createClauseHandler(clause1, clause2), createClauseHandler(clause1, clause2)).asList());

        mockController.replay();

        final Collection<SearchSort> result = searchSortUtil.convertNewSortsToKeepOldSortNames(theUser, newSorts, oldSorts);
        assertEquals(newSorts, result);
    }

    private static ClauseHandler createClauseHandler(final String primaryName, final String... names)
    {
        return new ClauseHandler()
        {
            public ClauseInformation getInformation()
            {
                return new ClauseInformation()
                {
                    public ClauseNames getJqlClauseNames()
                    {
                        return new ClauseNames(primaryName, names);
                    }

                    public String getIndexField()
                    {
                        throw new UnsupportedOperationException();
                    }

                    public String getFieldId()
                    {
                        throw new UnsupportedOperationException();
                    }

                    public Set<Operator> getSupportedOperators()
                    {
                        return OperatorClasses.TEXT_OPERATORS;
                    }

                    public JiraDataType getDataType()
                    {
                        return JiraDataTypes.TEXT;
                    }
                };
            }

            public ClauseQueryFactory getFactory()
            {
                throw new UnsupportedOperationException();
            }

            public ClauseValidator getValidator()
            {
                throw new UnsupportedOperationException();
            }

            public ClausePermissionHandler getPermissionHandler()
            {
                throw new UnsupportedOperationException();
            }

            public ClauseContextFactory getClauseContextFactory()
            {
                throw new UnsupportedOperationException();
            }
        };
    }
}
