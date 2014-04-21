package com.atlassian.jira.jql.validator;

import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.OperandHandler;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.resolver.NameResolver;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;
import com.opensymphony.user.User;
import org.junit.Test;

import java.util.Collections;

import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit test for {@link com.atlassian.jira.jql.validator.DataValuesExistValidator}.
 *
 * @since v4.0
 */
public class TestDataValuesExistValidator extends MockControllerTestCase
{
    private final String value = "value";
    private final String clause = "clause";
    private User theUser = null;

    @Test
    public void testLookupFailureStringValue()
    {
        final SingleValueOperand singleValueOperand = new SingleValueOperand(value);

        final NameResolver nameResolver = mockController.getMock(NameResolver.class);
        nameResolver.nameExists(value);
        mockController.setReturnValue(false);
        mockController.replay();


        DataValuesExistValidator clauseValidator = new DataValuesExistValidator(MockJqlOperandResolver.createSimpleSupport(), nameResolver)
        {
            protected I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }
        };

        TerminalClause priorityClause = new TerminalClauseImpl(clause, Operator.EQUALS, singleValueOperand);
        final MessageSet messages = clauseValidator.validate(theUser, priorityClause);
        assertTrue(messages.hasAnyErrors());
        assertEquals(String.format("The value '%s' does not exist for the field '%s'.", value, clause), messages.getErrorMessages().iterator().next());
    }

    @Test
    public void testLookupFailureStringValueFromFunction()
    {
        final SingleValueOperand singleValueOperand = new SingleValueOperand(value);
        TerminalClause priorityClause = new TerminalClauseImpl(clause, Operator.EQUALS, singleValueOperand);

        final OperandHandler operandHandler = mockController.getMock(OperandHandler.class);
        operandHandler.getValues(isA(QueryCreationContext.class), eq(singleValueOperand), eq(priorityClause));
        mockController.setReturnValue(Collections.singletonList(createLiteral(value)));
        operandHandler.isFunction();
        mockController.setReturnValue(true);
        final NameResolver nameResolver = mockController.getMock(NameResolver.class);
        nameResolver.nameExists(value);
        mockController.setReturnValue(false);

        mockController.replay();

        final JqlOperandResolver mockJqlOperandResolver = new MockJqlOperandResolver().addHandler(SingleValueOperand.OPERAND_NAME, operandHandler);

        DataValuesExistValidator clauseValidator = new DataValuesExistValidator(mockJqlOperandResolver, nameResolver)
        {
            protected I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }
        };

        final MessageSet messages = clauseValidator.validate(theUser, priorityClause);
        assertTrue(messages.hasAnyErrors());
        assertEquals(String.format("A value provided by the function 'SingleValueOperand' is invalid for the field '%s'.", clause), messages.getErrorMessages().iterator().next());
        mockController.verify();
    }

    @Test
    public void testLookupFailureLongValue()
    {
        final long id = 12345L;
        final SingleValueOperand singleValueOperand = new SingleValueOperand(id);

        final NameResolver nameResolver = mockController.getMock(NameResolver.class);
        nameResolver.idExists(id);
        mockController.setReturnValue(false);
        mockController.replay();

        DataValuesExistValidator clauseValidator = new DataValuesExistValidator(MockJqlOperandResolver.createSimpleSupport(), nameResolver)
        {
            protected I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }
        };
        TerminalClause priorityClause = new TerminalClauseImpl(clause, Operator.EQUALS, singleValueOperand);
        final MessageSet messages = clauseValidator.validate(theUser, priorityClause);
        assertTrue(messages.hasAnyErrors());
        assertEquals(String.format("A value with ID '%d' does not exist for the field '%s'.", id, clause), messages.getErrorMessages().iterator().next());
    }

    @Test
    public void testLookupFailureLongValueFromFunction()
    {
        final SingleValueOperand singleValueOperand = new SingleValueOperand(12345L);
        TerminalClause priorityClause = new TerminalClauseImpl(clause, Operator.EQUALS, singleValueOperand);

        final OperandHandler operandHandler = mockController.getMock(OperandHandler.class);
        operandHandler.getValues(isA(QueryCreationContext.class), eq(singleValueOperand), eq(priorityClause));
        mockController.setReturnValue(Collections.singletonList(createLiteral(12345L)));
        operandHandler.isFunction();
        mockController.setReturnValue(true);
        final NameResolver nameResolver = mockController.getMock(NameResolver.class);
        nameResolver.idExists(12345L);
        mockController.setReturnValue(false);
        mockController.replay();

        final JqlOperandResolver mockJqlOperandResolver = new MockJqlOperandResolver().addHandler(SingleValueOperand.OPERAND_NAME, operandHandler);

        DataValuesExistValidator clauseValidator = new DataValuesExistValidator(mockJqlOperandResolver, nameResolver)
        {
            protected I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }
        };

        final MessageSet messages = clauseValidator.validate(theUser, priorityClause);
        assertTrue(messages.hasAnyErrors());
        assertEquals(String.format("A value provided by the function 'SingleValueOperand' is invalid for the field '%s'.", clause), messages.getErrorMessages().iterator().next());
        mockController.verify();
    }

    @Test
    public void testLookupLongAsName()
    {
        final MultiValueOperand operand = new MultiValueOperand(111L, 123L);
        TerminalClause priorityClause = new TerminalClauseImpl(clause, Operator.IN, operand);

        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        jqlOperandResolver.isValidOperand(operand);
        mockController.setReturnValue(true);
        jqlOperandResolver.getValues(theUser, operand, priorityClause);
        mockController.setReturnValue(CollectionBuilder.newBuilder(createLiteral(111L), createLiteral(123L)).asList());

        final NameResolver nameResolver = mockController.getMock(NameResolver.class);
        nameResolver.idExists(111L);
        mockController.setReturnValue(true);
        nameResolver.idExists(123L);
        mockController.setReturnValue(true);
        mockController.replay();

        DataValuesExistValidator clauseValidator = new DataValuesExistValidator(jqlOperandResolver, nameResolver)
        {
            protected I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }
        };

        MessageSet errorCollection = clauseValidator.validate(theUser, priorityClause);
        assertFalse(errorCollection.hasAnyMessages());
    }

    @Test
    public void testNoOperandHandler()
    {
        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        expect(jqlOperandResolver.isValidOperand(this.<Operand>anyObject())).andReturn(false);

        final NameResolver nameResolver = mockController.getMock(NameResolver.class);
        mockController.replay();

        DataValuesExistValidator clauseValidator = new DataValuesExistValidator(jqlOperandResolver, nameResolver)
        {
            protected I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }
        };

        TerminalClause priorityClause = new TerminalClauseImpl(clause, Operator.IN, new MultiValueOperand(value, value+"1"));
        MessageSet errorCollection = clauseValidator.validate(theUser, priorityClause);
        assertFalse(errorCollection.hasAnyMessages());
    }

    @Test
    public void testHappyPath()
    {
        final MultiValueOperand operand = new MultiValueOperand(value, value + "1");
        TerminalClause priorityClause = new TerminalClauseImpl(clause, Operator.IN, operand);

        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        jqlOperandResolver.isValidOperand(operand);
        mockController.setReturnValue(true);
        jqlOperandResolver.getValues(theUser, operand, priorityClause);
        mockController.setReturnValue(CollectionBuilder.newBuilder(createLiteral(value), createLiteral(value + "1")).asList());

        final NameResolver nameResolver = mockController.getMock(NameResolver.class);
        nameResolver.nameExists(value);
        mockController.setReturnValue(true);
        nameResolver.nameExists(value+"1");
        mockController.setReturnValue(true);
        mockController.replay();
        DataValuesExistValidator clauseValidator = new DataValuesExistValidator(jqlOperandResolver, nameResolver)
        {
            protected I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }
        };
        MessageSet errorCollection = clauseValidator.validate(theUser, priorityClause);
        assertFalse(errorCollection.hasAnyMessages());
    }

    @Test
    public void testNullArgs()
    {
        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        final NameResolver nameResolver = mockController.getMock(NameResolver.class);
        mockController.replay();

        try
        {
            new DataValuesExistValidator(jqlOperandResolver, null);
            fail("expected exception");
        }
        catch (IllegalArgumentException expected)
        {

        }
        try
        {
            new DataValuesExistValidator(null, nameResolver);
            fail("expected exception");
        }
        catch (IllegalArgumentException expected)
        {

        }
    }
}
