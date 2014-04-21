package com.atlassian.jira.issue.customfields.searchers.transformer;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.customfields.view.CustomFieldParamsImpl;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.issue.transport.impl.FieldValuesHolderImpl;
import com.atlassian.jira.mock.controller.MockController;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.OrClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;
import com.atlassian.crowd.embedded.api.User;
import org.easymock.EasyMock;

/**
 * @since v4.0
 */
public class TestAbstractSingleValueCustomFieldSearchInputTransformer extends MockControllerTestCase
{
    final String id = "cf[1090]";
    private User theUser = null;
    private CustomField customField;
    private CustomFieldInputHelper customFieldInputHelper;

    @Before
    public void setUp() throws Exception
    {
        customField = mockController.getMock(CustomField.class);
        customFieldInputHelper = getMock(CustomFieldInputHelper.class);

        EasyMock.expect(customField.getName()).andStubReturn("ABC");
        EasyMock.expect(customFieldInputHelper.getUniqueClauseName((com.opensymphony.user.User) theUser, id, "ABC")).andStubReturn(id);
    }

    @Test
    public void testGetSearchClause() throws Exception
    {
        final String value = "value";

        _testGetSearchClause(null, new CustomFieldParamsImpl());
        _testGetSearchClause(new TerminalClauseImpl(id, Operator.EQUALS, value), new CustomFieldParamsImpl(customField, value));
        _testGetSearchClause(null, null);
        _testGetSearchClause(null, new CustomFieldParamsImpl(customField));
        _testGetSearchClause(null, new CustomFieldParamsImpl(customField, CollectionBuilder.newBuilder(value, value+"1").asList()));
    }

    @Test
    public void testdoRelevantClausesFitFilterForm() throws Exception
    {
        final String value = "value";
        _testDoRelevantClausesFitFilterForm(false, null, new QueryImpl(new TerminalClauseImpl(id, Operator.EQUALS, new MultiValueOperand(value, value))));
        _testDoRelevantClausesFitFilterForm(true, new SingleValueOperand(value), new QueryImpl(new TerminalClauseImpl(id, Operator.EQUALS, value)));
        _testDoRelevantClausesFitFilterForm(false, null, new QueryImpl(new TerminalClauseImpl(id, Operator.NOT_EQUALS, value)));
        _testDoRelevantClausesFitFilterForm(true, null, null);
        _testDoRelevantClausesFitFilterForm(true, null, new QueryImpl());
        _testDoRelevantClausesFitFilterForm(true, null, new QueryImpl(new TerminalClauseImpl("blarg", Operator.EQUALS, value)));
        _testDoRelevantClausesFitFilterForm(false, null, new QueryImpl(new OrClause(new TerminalClauseImpl(id, Operator.EQUALS, value), new TerminalClauseImpl(id, Operator.EQUALS, value))));
    }

    private void _testDoRelevantClausesFitFilterForm(final boolean isValid, final SingleValueOperand value, Query query)
    {
        mockController.replay();

        final AbstractSingleValueCustomFieldSearchInputTransformer transformer = new AbstractSingleValueCustomFieldSearchInputTransformer(customField, new ClauseNames(id), id, customFieldInputHelper)
        {
            public boolean doRelevantClausesFitFilterForm(final com.opensymphony.user.User searcher, final Query query, final SearchContext searchContext)
            {
                return false;
            }

            protected CustomFieldParams getParamsFromSearchRequest(final com.opensymphony.user.User searcher, final Query query, final SearchContext searchContext)
            {
                return null;
            }
        };

        final NavigatorConversionResult result = transformer.convertForNavigator(query);

        assertTrue(result.fitsNavigator() == isValid);
        assertEquals(value, result.getValue());

        mockController.verify();
        mockController.onTestEnd();
        mockController = new MockController();
    }
    
    private void _testGetSearchClause(Clause expected, CustomFieldParams params)
    {
        FieldValuesHolder holder = new FieldValuesHolderImpl(MapBuilder.newBuilder().add(id, params).toHashMap());
        mockController.replay();

        final AbstractSingleValueCustomFieldSearchInputTransformer transformer = new AbstractSingleValueCustomFieldSearchInputTransformer(customField, new ClauseNames(id), id, customFieldInputHelper)
        {
            public boolean doRelevantClausesFitFilterForm(final com.opensymphony.user.User searcher, final Query query, final SearchContext searchContext)
            {
                return false;
            }

            protected CustomFieldParams getParamsFromSearchRequest(final com.opensymphony.user.User searcher, final Query query, final SearchContext searchContext)
            {
                return null;
            }
        };

        final Clause result = transformer.getSearchClause(null, holder);
        assertEquals(expected, result);

        mockController.verify();
        mockController.onTestEnd();
        mockController = new MockController();
    }
}
