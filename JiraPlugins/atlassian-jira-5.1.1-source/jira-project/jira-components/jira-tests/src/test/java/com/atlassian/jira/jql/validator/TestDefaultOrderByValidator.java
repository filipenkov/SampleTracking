package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.NavigableField;
import com.atlassian.jira.issue.search.managers.SearchHandlerManager;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.query.order.OrderByImpl;
import com.atlassian.query.order.SearchSort;
import org.apache.lucene.search.FieldComparatorSource;
import org.apache.lucene.search.SortField;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestDefaultOrderByValidator extends MockControllerTestCase
{
    private SearchHandlerManager searchHandlerManager;
    private FieldManager fieldManager;
    private DefaultOrderByValidator defaultOrderByValidator;

    @Before
    public void setUp() throws Exception
    {
        searchHandlerManager = mockController.getMock(SearchHandlerManager.class);
        final I18nHelper.BeanFactory iBeanFactory = mockController.getMock(I18nHelper.BeanFactory.class);
        iBeanFactory.getInstance((User)null);
        mockController.setDefaultReturnValue(new MockI18nBean());
        fieldManager= mockController.getMock(FieldManager.class);

        defaultOrderByValidator = new DefaultOrderByValidator(searchHandlerManager, fieldManager, iBeanFactory);
    }

    @Test
    public void testNoSearchSorts() throws Exception
    {
        mockController.replay();
        final MessageSet messageSet = defaultOrderByValidator.validate(null, new OrderByImpl());
        assertNotNull(messageSet);
        assertFalse(messageSet.hasAnyMessages());

        mockController.verify();
    }

    @Test
    public void testSearchSortNoFieldIds() throws Exception
    {
        searchHandlerManager.getFieldIds(null, "test");
        mockController.setReturnValue(Collections.singletonList("testField"));

        fieldManager.isNavigableField("testField");
        mockController.setReturnValue(true);

        final NavigableField field = mockController.getMock(NavigableField.class);

        field.getSortFields(false);
        mockController.setReturnValue(Collections.singletonList(new SortField("notfound", SortField.STRING, false)));

        fieldManager.getNavigableField("testField");
        mockController.setReturnValue(field);

        searchHandlerManager.getFieldIds(null, "notfound");
        mockController.setReturnValue(Collections.emptyList());

        mockController.replay();
        final MessageSet messageSet = defaultOrderByValidator.validate(null, new OrderByImpl(new SearchSort("test"), new SearchSort("notfound")));
        assertNotNull(messageSet);
        assertTrue(messageSet.hasAnyMessages());
        assertEquals("Not able to sort using field 'notfound'.", messageSet.getErrorMessages().iterator().next());

        mockController.verify();
    }

    @Test
    public void testSearchSortNotNavigableField() throws Exception
    {
        searchHandlerManager.getFieldIds(null, "notfound");
        mockController.setReturnValue(Collections.singletonList("customfield10"));

        fieldManager.isNavigableField("customfield10");
        mockController.setReturnValue(false);

        mockController.replay();

        final MessageSet messageSet = defaultOrderByValidator.validate(null, new OrderByImpl(new SearchSort("notfound")));
        assertNotNull(messageSet);
        assertTrue(messageSet.hasAnyMessages());
        assertEquals("Field 'notfound' does not support sorting.", messageSet.getErrorMessages().iterator().next());

        mockController.verify();
    }

    @Test
    public void testSearchSortNavigableFieldHasNoSorter() throws Exception
    {
        searchHandlerManager.getFieldIds(null, "notfound");
        mockController.setReturnValue(Collections.singletonList("customfield10"));

        fieldManager.isNavigableField("customfield10");
        mockController.setReturnValue(true);

        final NavigableField field = mockController.getMock(NavigableField.class);
        field.getSortFields(false);
        mockController.setReturnValue(Collections.emptyList());

        fieldManager.getNavigableField("customfield10");
        mockController.setReturnValue(field);

        mockController.replay();

        final MessageSet messageSet = defaultOrderByValidator.validate(null, new OrderByImpl(new SearchSort("notfound")));
        assertNotNull(messageSet);
        assertTrue(messageSet.hasAnyMessages());
        assertEquals("Field 'notfound' does not support sorting.", messageSet.getErrorMessages().iterator().next());

        mockController.verify();
    }

    @Test
    public void testSearchSortDuplicateSorts() throws Exception
    {
        final NavigableField field = mockController.getMock(NavigableField.class);
        field.getSortFields(false);
        mockController.setReturnValue(Collections.singletonList(new SortField("notfound", SortField.STRING, false)));
        field.getSortFields(false);
        mockController.setReturnValue(Collections.singletonList(new SortField("notfound", SortField.STRING, false)));

        searchHandlerManager.getFieldIds(null, "test");
        mockController.setReturnValue(Collections.singletonList("testField"));


        fieldManager.isNavigableField("testField");
        mockController.setReturnValue(true);
        fieldManager.getNavigableField("testField");
        mockController.setReturnValue(field);

        fieldManager.isNavigableField("testField");
        mockController.setReturnValue(true);
        fieldManager.getNavigableField("testField");
        mockController.setReturnValue(field);


        searchHandlerManager.getFieldIds(null, "test");
        mockController.setReturnValue(Collections.singletonList("testField"));

        mockController.replay();
        final MessageSet messageSet = defaultOrderByValidator.validate(null, new OrderByImpl(new SearchSort("test"), new SearchSort("test")));
        assertNotNull(messageSet);
        assertTrue(messageSet.hasAnyMessages());
        assertEquals("The sort field 'test' is referenced multiple times in the JQL sort.", messageSet.getErrorMessages().iterator().next());

        mockController.verify();
    }

    @Test
    public void testSearchSortAliasedSorts() throws Exception
    {
        final NavigableField field = mockController.getMock(NavigableField.class);
        field.getSortFields(false);
        mockController.setReturnValue(Collections.singletonList(new SortField("notfound", SortField.STRING, false)));
        field.getSortFields(false);
        mockController.setReturnValue(Collections.singletonList(new SortField("notfound", SortField.STRING, false)));

        searchHandlerManager.getFieldIds(null, "test");
        mockController.setReturnValue(Collections.singletonList("testField"));

        searchHandlerManager.getFieldIds(null, "anothertest");
        mockController.setReturnValue(Collections.singletonList("testField"));

        fieldManager.isNavigableField("testField");
        mockController.setReturnValue(true);
        fieldManager.getNavigableField("testField");
        mockController.setReturnValue(field);

        fieldManager.isNavigableField("testField");
        mockController.setReturnValue(true);
        fieldManager.getNavigableField("testField");
        mockController.setReturnValue(field);


        mockController.replay();
        final MessageSet messageSet = defaultOrderByValidator.validate(null, new OrderByImpl(new SearchSort("test"), new SearchSort("anothertest")));
        assertNotNull(messageSet);
        assertTrue(messageSet.hasAnyMessages());
        assertEquals("The sort field 'anothertest' is referenced multiple times in the JQL sort. Field 'anothertest' is an alias for field 'test'.", messageSet.getErrorMessages().iterator().next());

        mockController.verify();
    }

    @Test
    public void testSearchSortHappyPath() throws Exception
    {
        final NavigableField field = mockController.getMock(NavigableField.class);
        field.getSortFields(false);
        mockController.setReturnValue(Collections.singletonList(new SortField("notfound", SortField.STRING, false)));
        field.getSortFields(false);
        mockController.setReturnValue(Collections.singletonList(new SortField("notfound", SortField.STRING, false)));

        searchHandlerManager.getFieldIds(null, "test");
        mockController.setReturnValue(Collections.singletonList("testField"));
        searchHandlerManager.getFieldIds(null, "anothertest");
        mockController.setReturnValue(Collections.singletonList("anotherField"));

        fieldManager.isNavigableField("testField");
        mockController.setReturnValue(true);
        fieldManager.getNavigableField("testField");
        mockController.setReturnValue(field);

        fieldManager.isNavigableField("anotherField");
        mockController.setReturnValue(true);
        fieldManager.getNavigableField("anotherField");
        mockController.setReturnValue(field);

        mockController.replay();
        final MessageSet messageSet = defaultOrderByValidator.validate(null, new OrderByImpl(new SearchSort("test"), new SearchSort("anothertest")));
        assertNotNull(messageSet);
        assertFalse(messageSet.hasAnyMessages());

        mockController.verify();
    }

}
