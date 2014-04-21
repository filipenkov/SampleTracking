package com.atlassian.jira.upgrade.tasks.jql;

import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.managers.SearchHandlerManager;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.query.order.OrderBy;
import com.atlassian.query.order.OrderByImpl;
import com.atlassian.query.order.SearchSort;
import com.atlassian.query.order.SortOrder;
import electric.xml.Document;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test for {@link com.atlassian.jira.upgrade.tasks.jql.DefaultOrderByXmlHandler}.
 *
 * @since v4.0
 */
public class TestDefaultOrderByXmlHandler extends MockControllerTestCase
{

    private SearchHandlerManager searchHandlerManager;
    private DefaultOrderByXmlHandler defaultOrderByXmlHandler;

    @Before
    public void setUp() throws Exception
    {
        searchHandlerManager = mockController.getMock(SearchHandlerManager.class);

        defaultOrderByXmlHandler = new DefaultOrderByXmlHandler(searchHandlerManager);
    }

    @Test
    public void testGetOrderByBadSortClass() throws Exception
    {
        mockController.replay();
        Document doc = new Document("<sort class='blah'><whatamidoinghere field='FIELD' order='HI'/></sort>");

        final OrderByXmlHandler.OrderByConversionResults orderByConversionResults = defaultOrderByXmlHandler.getOrderByFromXml(doc.getElements());
        assertTrue(orderByConversionResults.getConvertedOrderBy().getSearchSorts().isEmpty());
        mockController.verify();
    }

    @Test
    public void testGetOrderByFieldIsBlank() throws Exception
    {
        searchHandlerManager.getJqlClauseNames("myField");
        mockController.setReturnValue(Collections.singletonList(new ClauseNames("field")));
        mockController.replay();
        final String myFieldXml = getSortXml("myField", SortOrder.ASC.name());
        final String badXml = getSortXml("", SortOrder.ASC.name());
        Document doc = new Document("<sorts>" + myFieldXml + badXml + "</sorts>");

        OrderBy expectedOrderBy = new OrderByImpl(new SearchSort("field", SortOrder.ASC));

        final OrderByXmlHandler.OrderByConversionResults orderByConversionResults = defaultOrderByXmlHandler.getOrderByFromXml(doc.getRoot().getElements());
        assertFalse(orderByConversionResults.getConversionErrors().isEmpty());
        assertEquals("We were unable to upgrade a part of saved filter 'me' that relates to ordering, as we could not interpret the stored value. This error means that the filter will no longer sort the results in the intended way. To fix this, please edit the filter manually and specify the correct sorting criteria.", orderByConversionResults.getConversionErrors().iterator().next().getMessage(new MockI18nBean(), "me"));
        assertEquals(expectedOrderBy, orderByConversionResults.getConvertedOrderBy());
        mockController.verify();
    }

    @Test
    public void testGetOrderByOrderIsNull() throws Exception
    {
        searchHandlerManager.getJqlClauseNames("myField");
        mockController.setReturnValue(Collections.singletonList(new ClauseNames("field")));
        mockController.replay();
        Document doc = new Document("<sort class='com.atlassian.jira.issue.search.SearchSort'><searchSort field='myField'/></sort>");

        OrderBy expectedOrderBy = new OrderByImpl(new SearchSort("field"));

        final OrderByXmlHandler.OrderByConversionResults orderByConversionResults = defaultOrderByXmlHandler.getOrderByFromXml(doc.getElements());
        assertTrue(orderByConversionResults.getConversionErrors().isEmpty());
        assertEquals(expectedOrderBy, orderByConversionResults.getConvertedOrderBy());
        mockController.verify();
    }

    @Test
    public void testGetOrderByOrderIsBadValue() throws Exception
    {
        searchHandlerManager.getJqlClauseNames("myField");
        mockController.setReturnValue(Collections.singletonList(new ClauseNames("field")));
        mockController.replay();
        Document doc = new Document(getSortXml("myField", "reallyHigh"));

        OrderBy expectedOrderBy = new OrderByImpl(new SearchSort("field", SortOrder.ASC));

        final OrderByXmlHandler.OrderByConversionResults orderByConversionResults = defaultOrderByXmlHandler.getOrderByFromXml(doc.getElements());
        assertTrue(orderByConversionResults.getConversionErrors().isEmpty());
        assertEquals(expectedOrderBy, orderByConversionResults.getConvertedOrderBy());
        mockController.verify();
    }

    @Test
    public void testGetOrderByClauseNameDoesNotExistForField() throws Exception
    {
        searchHandlerManager.getJqlClauseNames("myField");
        mockController.setReturnValue(Collections.emptyList());
        mockController.replay();
        Document doc = new Document(getSortXml("myField", "reallyHigh"));

        final OrderByXmlHandler.OrderByConversionResults orderByConversionResults = defaultOrderByXmlHandler.getOrderByFromXml(doc.getElements());
        assertFalse(orderByConversionResults.getConversionErrors().isEmpty());
        assertEquals("We were unable to upgrade a part of saved filter 'me' that relates to ordering, as we could not map the field 'myField' to a clause name.  This error means that the filter will no longer sort the results in the intended way. To fix this, please edit the filter manually and specify the correct sorting criteria.", orderByConversionResults.getConversionErrors().iterator().next().getMessage(new MockI18nBean(), "me"));
        assertTrue(orderByConversionResults.getConvertedOrderBy().getSearchSorts().isEmpty());
        mockController.verify();
    }

    @Test
    public void testGetOrderByHappyPathTwoSame() throws Exception
    {
        searchHandlerManager.getJqlClauseNames("myField");
        mockController.setReturnValue(Collections.singletonList(new ClauseNames("field")));
        mockController.replay();
        final String myFieldXml = getSortXml("myField", SortOrder.ASC.name());
        final String otherFieldXml = getSortXml("myField", SortOrder.DESC.name());
        Document doc = new Document("<sorts>" + myFieldXml + otherFieldXml + "</sorts>");

        OrderBy expectedOrderBy = new OrderByImpl(new SearchSort("field", SortOrder.ASC));

        final OrderByXmlHandler.OrderByConversionResults orderByConversionResults = defaultOrderByXmlHandler.getOrderByFromXml(doc.getRoot().getElements());
        assertTrue(orderByConversionResults.getConversionErrors().isEmpty());
        assertEquals(expectedOrderBy, orderByConversionResults.getConvertedOrderBy());
        mockController.verify();
    }

    @Test
    public void testGetOrderByHappyPathOneSort() throws Exception
    {
        searchHandlerManager.getJqlClauseNames("myField");
        mockController.setReturnValue(Collections.singletonList(new ClauseNames("field")));
        mockController.replay();
        Document doc = new Document(getSortXml("myField", SortOrder.ASC.name()));

        OrderBy expectedOrderBy = new OrderByImpl(new SearchSort("field", SortOrder.ASC));

        final OrderByXmlHandler.OrderByConversionResults orderByConversionResults = defaultOrderByXmlHandler.getOrderByFromXml(doc.getElements());
        assertTrue(orderByConversionResults.getConversionErrors().isEmpty());
        assertEquals(expectedOrderBy, orderByConversionResults.getConvertedOrderBy());
        mockController.verify();
    }

    @Test
    public void testGetOrderByHappyPathTwoSorts() throws Exception
    {
        searchHandlerManager.getJqlClauseNames("myField");
        mockController.setReturnValue(Collections.singletonList(new ClauseNames("field")));
        searchHandlerManager.getJqlClauseNames("myOtherField");
        mockController.setReturnValue(Collections.singletonList(new ClauseNames("otherField")));
        mockController.replay();
        final String myFieldXml = getSortXml("myField", SortOrder.ASC.name());
        final String otherFieldXml = getSortXml("myOtherField", SortOrder.DESC.name());
        Document doc = new Document("<sorts>" + myFieldXml + otherFieldXml + "</sorts>");

        // The reason we reverse this is that the sorts used to be stored in reverse order, we want to change this
        // when we convert to an OrderBy
        OrderBy expectedOrderBy = new OrderByImpl(new SearchSort("otherField", SortOrder.DESC),
                new SearchSort("field", SortOrder.ASC));

        final OrderByXmlHandler.OrderByConversionResults orderByConversionResults = defaultOrderByXmlHandler.getOrderByFromXml(doc.getRoot().getElements());
        assertTrue(orderByConversionResults.getConversionErrors().isEmpty());
        assertEquals(expectedOrderBy, orderByConversionResults.getConvertedOrderBy());
        mockController.verify();
    }

    private static String getSortXml(final String field, final String order)
    {
        return getSortXml(field, order, "com.atlassian.jira.issue.search.SearchSort");
    }

    private static String getSortXml(final String field, final String order, final String klass)
    {
        return String.format("<sort class='%s'><searchSort field='%s' order='%s'/></sort>", klass, field, order);
    }
}
