package com.atlassian.jira.issue.customfields.searchers.transformer;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.managers.SearchHandlerManager;
import com.atlassian.jira.jql.ClauseHandler;
import com.atlassian.jira.util.collect.CollectionBuilder;
import org.easymock.EasyMock;

import java.util.Collections;

/**
 * @since v4.0
 */
public class TestDefaultCustomFieldInputHelper extends MockControllerTestCase
{
    private SearchHandlerManager searchHandlerManager;
    private User searcher;
    private DefaultCustomFieldInputHelper helper;

    @Before
    public void setUp() throws Exception
    {
        searchHandlerManager = getMock(SearchHandlerManager.class);
        searcher = null;
    }

    @Test
    public void testGetClauseNameNameIsUnique() throws Exception
    {
        final String fieldName = "ABC";
        final String primaryName = "cf[10000]";
        final ClauseHandler clauseHandler = getMock(ClauseHandler.class);
        CustomField customField = mockController.getMock(CustomField.class);

        EasyMock.expect(searchHandlerManager.getClauseHandler(searcher, fieldName)).andReturn(Collections.singleton(clauseHandler));
        EasyMock.expect(customField.getName()).andStubReturn(fieldName);

        replay();

        helper = new DefaultCustomFieldInputHelper(searchHandlerManager);

        final String result = helper.getUniqueClauseName(searcher, primaryName, fieldName);
        assertEquals(fieldName, result);
    }

    @Test
    public void testGetClauseNameNameIsNotUnique() throws Exception
    {
        final String fieldName = "ABC";
        final String primaryName = "cf[10000]";
        final ClauseHandler clauseHandler1 = EasyMock.createMock(ClauseHandler.class);
        final ClauseHandler clauseHandler2 = EasyMock.createMock(ClauseHandler.class);
        CustomField customField = mockController.getMock(CustomField.class);

        EasyMock.expect(searchHandlerManager.getClauseHandler(searcher, fieldName)).andReturn(CollectionBuilder.list(clauseHandler1, clauseHandler2));
        EasyMock.expect(customField.getName()).andStubReturn(fieldName);

        replay(clauseHandler1, clauseHandler2);

        helper = new DefaultCustomFieldInputHelper(searchHandlerManager);

        final String result = helper.getUniqueClauseName(searcher, primaryName, fieldName);
        assertEquals(primaryName, result);

        verify(clauseHandler1, clauseHandler2);
    }

    @Test
    public void testGetClauseNameNameIsSystemFieldName() throws Exception
    {
        final String fieldName = "project";
        final String primaryName = "cf[10000]";
        CustomField customField = mockController.getMock(CustomField.class);

        EasyMock.expect(customField.getName()).andStubReturn(fieldName);

        replay();

        helper = new DefaultCustomFieldInputHelper(searchHandlerManager);

        final String result = helper.getUniqueClauseName(searcher, primaryName, fieldName);
        assertEquals(primaryName, result);
    }

    @Test
    public void testGetClauseNameNameIsCustomFieldId() throws Exception
    {
        final String fieldName = "cf[12345]";
        final String primaryName = "cf[10000]";
        CustomField customField = mockController.getMock(CustomField.class);

        EasyMock.expect(customField.getName()).andStubReturn(fieldName);

        replay();

        helper = new DefaultCustomFieldInputHelper(searchHandlerManager);

        final String result = helper.getUniqueClauseName(searcher, primaryName, fieldName);
        assertEquals(primaryName, result);
    }
}
