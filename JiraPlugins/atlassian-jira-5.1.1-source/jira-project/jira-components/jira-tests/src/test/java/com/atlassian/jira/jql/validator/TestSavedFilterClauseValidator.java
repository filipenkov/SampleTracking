package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.MockSearchRequest;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.resolver.SavedFilterResolver;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.jira.web.session.SessionSearchObjectManagerFactory;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.util.Collections;

import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestSavedFilterClauseValidator extends MockControllerTestCase
{
    private JqlOperandResolver jqlOperandResolver;
    private SavedFilterResolver filterResolver;
    private SavedFilterCycleDetector savedFilterCycleDetector;
    private User theUser = null;
    private boolean overrideSecurity = false;
    private SessionSearchObjectManagerFactory sessionSearchObjectManagerFactory;

    @Before
    public void setUp() throws Exception
    {
        jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        filterResolver = mockController.getMock(SavedFilterResolver.class);
        savedFilterCycleDetector = mockController.getMock(SavedFilterCycleDetector.class);
        sessionSearchObjectManagerFactory = mockController.getMock(SessionSearchObjectManagerFactory.class);
    }

    @Test
    public void testValidateEmpty() throws Exception
    {
        final Operand operand = EmptyOperand.EMPTY;
        final QueryLiteral queryLiteral = new QueryLiteral();

        TerminalClause clause = new TerminalClauseImpl("test", Operator.EQUALS, operand);
        jqlOperandResolver.getValues(theUser, operand, clause);
        mockController.setReturnValue(CollectionBuilder.newBuilder(queryLiteral).asList());
        jqlOperandResolver.isFunctionOperand(operand);
        mockController.setReturnValue(false);

        mockController.replay();

        final SavedFilterClauseValidator filterClauseValidator = new SavedFilterClauseValidator(filterResolver, jqlOperandResolver, savedFilterCycleDetector, sessionSearchObjectManagerFactory)
        {
            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }

            @Override
            Long getSearchFilterId()
            {
                return null;
            }
        };

        final MessageSet messageSet = filterClauseValidator.validate(theUser, clause);
        assertTrue(messageSet.hasAnyMessages());
        assertEquals("The field 'test' does not support searching for EMPTY values.", messageSet.getErrorMessages().iterator().next());
        mockController.verify();
    }

    @Test
    public void testValidateEmptyFromFunction() throws Exception
    {
        final Operand operand = new FunctionOperand("generateEmpty");
        final QueryLiteral queryLiteral = new QueryLiteral(operand);

        TerminalClause clause = new TerminalClauseImpl("test", Operator.EQUALS, operand);
        jqlOperandResolver.getValues(theUser, operand, clause);
        mockController.setReturnValue(CollectionBuilder.newBuilder(queryLiteral).asList());
        jqlOperandResolver.isFunctionOperand(operand);
        mockController.setReturnValue(true);

        mockController.replay();

        final SavedFilterClauseValidator filterClauseValidator = new SavedFilterClauseValidator(filterResolver, jqlOperandResolver, savedFilterCycleDetector, sessionSearchObjectManagerFactory)
        {
            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }

            @Override
            Long getSearchFilterId()
            {
                return null;
            }
        };

        final MessageSet messageSet = filterClauseValidator.validate(theUser, clause);
        assertTrue(messageSet.hasAnyMessages());
        assertEquals("The field 'test' does not support EMPTY values provided by function 'generateEmpty'.", messageSet.getErrorMessages().iterator().next());
        mockController.verify();
    }

    @Test
    public void testValidateHappyPathMultipleFilters() throws Exception
    {
        final SingleValueOperand filter1Operand = new SingleValueOperand("filter1");
        final SingleValueOperand filter2Operand = new SingleValueOperand("filter2");
        final SingleValueOperand filter3Operand = new SingleValueOperand(123L);

        final MultiValueOperand operand = new MultiValueOperand(CollectionBuilder.newBuilder(filter1Operand, filter2Operand, filter3Operand).asList());
        TerminalClause clause = new TerminalClauseImpl("test", Operator.IN, operand);
        jqlOperandResolver.getValues(theUser, operand, clause);
        final QueryLiteral queryLiteral1 = createLiteral("filter1");
        final QueryLiteral queryLiteral2 = createLiteral("filter2");
        final QueryLiteral queryLiteral3 = createLiteral(123L);
        mockController.setReturnValue(CollectionBuilder.newBuilder(queryLiteral1, queryLiteral2, queryLiteral3).asList());

        filterResolver.getSearchRequest(theUser, Collections.singletonList(queryLiteral1));
        final MockSearchRequest filter1 = new MockSearchRequest("filter1");
        mockController.setReturnValue(Collections.singletonList(filter1));
        filterResolver.getSearchRequest(theUser, Collections.singletonList(queryLiteral2));
        final MockSearchRequest filter2 = new MockSearchRequest("filter2");
        mockController.setReturnValue(Collections.singletonList(filter2));
        filterResolver.getSearchRequest(theUser, Collections.singletonList(queryLiteral3));
        final MockSearchRequest filter3 = new MockSearchRequest("filter3");
        mockController.setReturnValue(Collections.singletonList(filter3));

        savedFilterCycleDetector.containsSavedFilterReference(theUser, overrideSecurity, filter1, null);
        mockController.setReturnValue(false);
        savedFilterCycleDetector.containsSavedFilterReference(theUser, overrideSecurity, filter2, null);
        mockController.setReturnValue(false);
        savedFilterCycleDetector.containsSavedFilterReference(theUser, overrideSecurity, filter3, null);
        mockController.setReturnValue(false);
        
        mockController.replay();

        final SavedFilterClauseValidator filterClauseValidator = new SavedFilterClauseValidator(filterResolver, jqlOperandResolver, savedFilterCycleDetector, sessionSearchObjectManagerFactory)
        {
            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }

            @Override
            Long getSearchFilterId()
            {
                return null;
            }
        };
        final MessageSet messageSet = filterClauseValidator.validate(theUser, clause);
        assertFalse(messageSet.hasAnyMessages());
        mockController.verify();
    }

    @Test
    public void testValidateMultipleFiltersOneContainsCycle() throws Exception
    {
        final SingleValueOperand filter1Operand = new SingleValueOperand("filter1");
        final SingleValueOperand filter2Operand = new SingleValueOperand("filter2");
        final SingleValueOperand filter3Operand = new SingleValueOperand(123L);

        final MultiValueOperand operand = new MultiValueOperand(CollectionBuilder.newBuilder(filter1Operand, filter2Operand, filter3Operand).asList());
        TerminalClause clause = new TerminalClauseImpl("test", Operator.IN, operand);
        jqlOperandResolver.getValues(theUser, operand, clause);
        final QueryLiteral queryLiteral1 = createLiteral("filter1");
        final QueryLiteral queryLiteral2 = createLiteral("filter2");
        final QueryLiteral queryLiteral3 = createLiteral(123L);
        mockController.setReturnValue(CollectionBuilder.newBuilder(queryLiteral1, queryLiteral2, queryLiteral3).asList());

        filterResolver.getSearchRequest(theUser, Collections.singletonList(queryLiteral1));
        final MockSearchRequest filter1 = new MockSearchRequest("filter1");
        mockController.setReturnValue(Collections.singletonList(filter1));
        filterResolver.getSearchRequest(theUser, Collections.singletonList(queryLiteral2));
        final MockSearchRequest filter2 = new MockSearchRequest("filter2");
        mockController.setReturnValue(Collections.singletonList(filter2));
        filterResolver.getSearchRequest(theUser, Collections.singletonList(queryLiteral3));
        final MockSearchRequest filter3 = new MockSearchRequest("filter3");
        filter3.setName("filter3");
        mockController.setReturnValue(Collections.singletonList(filter3));

        savedFilterCycleDetector.containsSavedFilterReference(theUser, overrideSecurity, filter1, null);
        mockController.setReturnValue(false);
        savedFilterCycleDetector.containsSavedFilterReference(theUser, overrideSecurity, filter2, null);
        mockController.setReturnValue(false);
        savedFilterCycleDetector.containsSavedFilterReference(theUser, overrideSecurity, filter3, null);
        mockController.setReturnValue(true);

        mockController.replay();

        final SavedFilterClauseValidator filterClauseValidator = new SavedFilterClauseValidator(filterResolver, jqlOperandResolver, savedFilterCycleDetector, sessionSearchObjectManagerFactory)
        {
            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }

            @Override
            Long getSearchFilterId()
            {
                return null;
            }
        };
        final MessageSet messageSet = filterClauseValidator.validate(theUser, clause);
        assertTrue(messageSet.hasAnyMessages());
        assertEquals("Field 'test' with value '123' matches filter 'filter3' and causes a cyclical reference, this query can not be executed and should be edited.", messageSet.getErrorMessages().iterator().next());
        mockController.verify();
    }

    @Test
    public void testErrorFindingFilterByName() throws Exception
    {
        final SingleValueOperand filter1Operand = new SingleValueOperand("filter1");
        final SingleValueOperand filter2Operand = new SingleValueOperand("filter2");
        final SingleValueOperand filter3Operand = new SingleValueOperand(123L);

        final MultiValueOperand operand = new MultiValueOperand(CollectionBuilder.newBuilder(filter1Operand, filter2Operand, filter3Operand).asList());
        TerminalClause clause = new TerminalClauseImpl("test", Operator.IN, operand);
        jqlOperandResolver.getValues(theUser, operand, clause);
        final QueryLiteral queryLiteral1 = new QueryLiteral(filter1Operand, "filter1");
        final QueryLiteral queryLiteral2 = new QueryLiteral(filter2Operand, "filter2");
        final QueryLiteral queryLiteral3 = new QueryLiteral(filter3Operand, 123L);
        mockController.setReturnValue(CollectionBuilder.newBuilder(queryLiteral1, queryLiteral2, queryLiteral3).asList());
        jqlOperandResolver.isFunctionOperand(filter2Operand);
        mockController.setReturnValue(false);

        filterResolver.getSearchRequest(theUser, Collections.singletonList(queryLiteral1));
        final MockSearchRequest filter1 = new MockSearchRequest("filter1");
        mockController.setReturnValue(Collections.singletonList(filter1));
        filterResolver.getSearchRequest(theUser, Collections.singletonList(queryLiteral2));
        mockController.setReturnValue(Collections.emptyList());
        filterResolver.getSearchRequest(theUser, Collections.singletonList(queryLiteral3));
        final MockSearchRequest filter3 = new MockSearchRequest("filter3");
        mockController.setReturnValue(Collections.singletonList(filter3));

        savedFilterCycleDetector.containsSavedFilterReference(theUser, overrideSecurity, filter1, null);
        mockController.setReturnValue(false);
        savedFilterCycleDetector.containsSavedFilterReference(theUser, overrideSecurity, filter3, null);
        mockController.setReturnValue(false);

        mockController.replay();

        final SavedFilterClauseValidator filterClauseValidator = new SavedFilterClauseValidator(filterResolver, jqlOperandResolver, savedFilterCycleDetector, sessionSearchObjectManagerFactory)
        {
            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }

            @Override
            Long getSearchFilterId()
            {
                return null;
            }
        };
        final MessageSet messageSet = filterClauseValidator.validate(theUser, clause);
        assertTrue(messageSet.hasAnyMessages());
        assertEquals("The value 'filter2' does not exist for the field 'test'.", messageSet.getErrorMessages().iterator().next());
        mockController.verify();
    }

    @Test
    public void testErrorFindingFilterByNameFromFunction() throws Exception
    {
        final SingleValueOperand filter1Operand = new SingleValueOperand("filter1");
        final SingleValueOperand filter2Operand = new SingleValueOperand("filter2");
        final SingleValueOperand filter3Operand = new SingleValueOperand(123L);

        final MultiValueOperand operand = new MultiValueOperand(CollectionBuilder.newBuilder(filter1Operand, filter2Operand, filter3Operand).asList());
        TerminalClause clause = new TerminalClauseImpl("test", Operator.IN, operand);
        jqlOperandResolver.getValues(theUser, operand, clause);
        final QueryLiteral queryLiteral1 = new QueryLiteral(filter1Operand, "filter1");
        final QueryLiteral queryLiteral2 = new QueryLiteral(filter2Operand, "filter2");
        final QueryLiteral queryLiteral3 = new QueryLiteral(filter3Operand, 123L);
        mockController.setReturnValue(CollectionBuilder.newBuilder(queryLiteral1, queryLiteral2, queryLiteral3).asList());
        jqlOperandResolver.isFunctionOperand(filter2Operand);
        mockController.setReturnValue(true);

        filterResolver.getSearchRequest(theUser, Collections.singletonList(queryLiteral1));
        final MockSearchRequest filter1 = new MockSearchRequest("filter1");
        mockController.setReturnValue(Collections.singletonList(filter1));
        filterResolver.getSearchRequest(theUser, Collections.singletonList(queryLiteral2));
        mockController.setReturnValue(Collections.emptyList());
        filterResolver.getSearchRequest(theUser, Collections.singletonList(queryLiteral3));
        final MockSearchRequest filter3 = new MockSearchRequest("filter3");
        mockController.setReturnValue(Collections.singletonList(filter3));

        savedFilterCycleDetector.containsSavedFilterReference(theUser, overrideSecurity, filter1, null);
        mockController.setReturnValue(false);
        savedFilterCycleDetector.containsSavedFilterReference(theUser, overrideSecurity, filter3, null);
        mockController.setReturnValue(false);

        mockController.replay();

        final SavedFilterClauseValidator filterClauseValidator = new SavedFilterClauseValidator(filterResolver, jqlOperandResolver, savedFilterCycleDetector, sessionSearchObjectManagerFactory)
        {
            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }

            @Override
            Long getSearchFilterId()
            {
                return null;
            }
        };
        final MessageSet messageSet = filterClauseValidator.validate(theUser, clause);
        assertTrue(messageSet.hasAnyMessages());
        assertEquals("A value provided by the function 'SingleValueOperand' is invalid for the field 'test'.", messageSet.getErrorMessages().iterator().next());
        mockController.verify();
    }

    @Test
    public void testErrorFindingFilterById() throws Exception
    {
        final SingleValueOperand filter1Operand = new SingleValueOperand("filter1");
        final SingleValueOperand filter2Operand = new SingleValueOperand("filter2");
        final SingleValueOperand filter3Operand = new SingleValueOperand(123L);

        final MultiValueOperand operand = new MultiValueOperand(CollectionBuilder.newBuilder(filter1Operand, filter2Operand, filter3Operand).asList());
        TerminalClause clause = new TerminalClauseImpl("test", Operator.IN, operand);
        jqlOperandResolver.getValues(theUser, operand, clause);
        final QueryLiteral queryLiteral1 = new QueryLiteral(filter1Operand, "filter1");
        final QueryLiteral queryLiteral2 = new QueryLiteral(filter2Operand, "filter2");
        final QueryLiteral queryLiteral3 = new QueryLiteral(filter3Operand, 123L);
        mockController.setReturnValue(CollectionBuilder.newBuilder(queryLiteral1, queryLiteral2, queryLiteral3).asList());
        jqlOperandResolver.isFunctionOperand(filter3Operand);
        mockController.setReturnValue(false);

        filterResolver.getSearchRequest(theUser, Collections.singletonList(queryLiteral1));
        final MockSearchRequest filter1 = new MockSearchRequest("filter1");
        mockController.setReturnValue(Collections.singletonList(filter1));
        filterResolver.getSearchRequest(theUser, Collections.singletonList(queryLiteral2));
        final MockSearchRequest filter2 = new MockSearchRequest("filter2");
        mockController.setReturnValue(Collections.singletonList(filter2));
        filterResolver.getSearchRequest(theUser, Collections.singletonList(queryLiteral3));
        mockController.setReturnValue(Collections.emptyList());

        savedFilterCycleDetector.containsSavedFilterReference(theUser, overrideSecurity, filter1, null);
        mockController.setReturnValue(false);
        savedFilterCycleDetector.containsSavedFilterReference(theUser, overrideSecurity, filter2, null);
        mockController.setReturnValue(false);
        
        mockController.replay();

        final SavedFilterClauseValidator filterClauseValidator = new SavedFilterClauseValidator(filterResolver, jqlOperandResolver, savedFilterCycleDetector, sessionSearchObjectManagerFactory)
        {
            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }

            @Override
            Long getSearchFilterId()
            {
                return null;
            }
        };
        final MessageSet messageSet = filterClauseValidator.validate(theUser, clause);
        assertTrue(messageSet.hasAnyMessages());
        assertEquals("A value with ID '123' does not exist for the field 'test'.", messageSet.getErrorMessages().iterator().next());
        mockController.verify();

    }

    @Test
    public void testErrorFindingFilterByIdFromFunction() throws Exception
    {
        final SingleValueOperand filter1Operand = new SingleValueOperand("filter1");
        final SingleValueOperand filter2Operand = new SingleValueOperand("filter2");
        final SingleValueOperand filter3Operand = new SingleValueOperand(123L);

        final MultiValueOperand operand = new MultiValueOperand(CollectionBuilder.newBuilder(filter1Operand, filter2Operand, filter3Operand).asList());
        TerminalClause clause = new TerminalClauseImpl("test", Operator.IN, operand);
        jqlOperandResolver.getValues(theUser, operand, clause);
        final QueryLiteral queryLiteral1 = new QueryLiteral(filter1Operand, "filter1");
        final QueryLiteral queryLiteral2 = new QueryLiteral(filter2Operand, "filter2");
        final QueryLiteral queryLiteral3 = new QueryLiteral(filter3Operand, 123L);
        mockController.setReturnValue(CollectionBuilder.newBuilder(queryLiteral1, queryLiteral2, queryLiteral3).asList());
        jqlOperandResolver.isFunctionOperand(filter3Operand);
        mockController.setReturnValue(true);

        filterResolver.getSearchRequest(theUser, Collections.singletonList(queryLiteral1));
        final MockSearchRequest filter1 = new MockSearchRequest("filter1");
        mockController.setReturnValue(Collections.singletonList(filter1));
        filterResolver.getSearchRequest(theUser, Collections.singletonList(queryLiteral2));
        final MockSearchRequest filter2 = new MockSearchRequest("filter2");
        mockController.setReturnValue(Collections.singletonList(filter2));
        filterResolver.getSearchRequest(theUser, Collections.singletonList(queryLiteral3));
        mockController.setReturnValue(Collections.emptyList());

        savedFilterCycleDetector.containsSavedFilterReference(theUser, overrideSecurity, filter1, null);
        mockController.setReturnValue(false);
        savedFilterCycleDetector.containsSavedFilterReference(theUser, overrideSecurity, filter2, null);
        mockController.setReturnValue(false);

        mockController.replay();

        final SavedFilterClauseValidator filterClauseValidator = new SavedFilterClauseValidator(filterResolver, jqlOperandResolver, savedFilterCycleDetector, sessionSearchObjectManagerFactory)
        {
            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }

            @Override
            Long getSearchFilterId()
            {
                return null;
            }
        };
        final MessageSet messageSet = filterClauseValidator.validate(theUser, clause);
        assertTrue(messageSet.hasAnyMessages());
        assertEquals("A value provided by the function 'SingleValueOperand' is invalid for the field 'test'.", messageSet.getErrorMessages().iterator().next());
        mockController.verify();
    }

    @Test
    public void testInvalidOperator() throws Exception
    {
        final SupportedOperatorsValidator operatorsValidator = mockController.getMock(SupportedOperatorsValidator.class);
        operatorsValidator.validate(theUser, null);
        final MessageSetImpl messageSet = new MessageSetImpl();
        messageSet.addErrorMessage("dude");
        mockController.setReturnValue(messageSet);
        mockController.replay();
        final SavedFilterClauseValidator filterClauseValidator = new SavedFilterClauseValidator(null, null, null, sessionSearchObjectManagerFactory)
        {
            @Override
            SupportedOperatorsValidator getSupportedOperatorsValidator()
            {
                return operatorsValidator;
            }

            @Override
            Long getSearchFilterId()
            {
                return null;
            }
        };
        final MessageSet foundSet = filterClauseValidator.validate(theUser, null);
        assertTrue(foundSet.hasAnyMessages());
    }

}
