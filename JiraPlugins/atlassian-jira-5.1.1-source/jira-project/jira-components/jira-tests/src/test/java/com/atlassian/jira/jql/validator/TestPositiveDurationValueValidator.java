package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operand.MockOperandHandler;
import com.atlassian.jira.jql.operand.OperandHandler;
import com.atlassian.jira.jql.util.JqlTimetrackingDurationSupport;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.security.auth.trustedapps.MockI18nHelper;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @since v4.0
 */
public class TestPositiveDurationValueValidator extends ListeningTestCase
{
    private static final String FIELD_NAME = "field";

    private User theUser = null;
    private PositiveDurationValueValidator validator;
    private JqlTimetrackingDurationSupport converter;

    @Before
    public void setUp() throws Exception
    {
        converter = Mockito.mock(JqlTimetrackingDurationSupport.class);
        validator = createValidator();
    }

    @After
    public void tearDown() throws Exception
    {
        validator = null;
    }

    @Test
    public void testNullArgsInConstructor() throws Exception
    {
        try
        {
            new IntegerValueValidator(null);
            fail("Exception expected");
        }
        catch (IllegalArgumentException expected) {}
    }

    @Test
    public void testValidateLongs() throws Exception
    {
        assertInvalid(-999L);
        assertValid(0L);
        assertValid(999L);
    }

    @Test
    public void testValidateValidStrings() throws Exception
    {
        Mockito.when(converter.validate("-999")).thenReturn(true);
        assertValid("-999");
    }

    @Test
    public void testValidateInvalidStrings() throws Exception
    {
        Mockito.when(converter.validate("-999")).thenReturn(false);
        assertInvalid("-999");
    }

    @Test
    public void testValidateStringsFromFunction() throws Exception
    {
        final MockOperandHandler handler = new MockOperandHandler(false, false, true);
        handler.add("0", "-35m");
        validator = createValidator(handler);

        final MessageSet messageSet = validator.validate(null, new TerminalClauseImpl(FIELD_NAME, Operator.EQUALS, "ignored"));
        assertTrue(messageSet.hasAnyMessages());
        assertEquals(1, messageSet.getErrorMessages().size());
        String error = messageSet.getErrorMessages().iterator().next();
        assertEquals("jira.jql.clause.positive.duration.format.invalid.from.func SingleValueOperand field", error);
    }

    @Test
    public void testValidateEmptyLiteral() throws Exception
    {
        validator = createValidator();

        final MessageSet messageSet = validator.validate(null, new TerminalClauseImpl(FIELD_NAME, Operator.IS, EmptyOperand.EMPTY));
        assertFalse(messageSet.hasAnyMessages());
    }

    private void assertValid(long operand)
    {
        final MessageSet messageSet = validator.validate(theUser, createClause(operand));
        assertFalse(messageSet.hasAnyErrors());
    }

    private void assertValid(String operand)
    {
        final MessageSet messageSet = validator.validate(theUser, createClause(operand));
        assertFalse(messageSet.hasAnyErrors());
    }

    private void assertInvalid(long operand)
    {
        final MessageSet messageSet = validator.validate(theUser, createClause(operand));
        assertTrue(messageSet.hasAnyErrors());
        assertEquals(1, messageSet.getErrorMessages().size());
        assertTrue(messageSet.getErrorMessages().iterator().next().startsWith("jira.jql.clause.positive.duration.format.invalid"));
    }

    private void assertInvalid(String operand)
    {
        final MessageSet messageSet = validator.validate(theUser, createClause(operand));
        assertTrue(messageSet.hasAnyErrors());
        assertEquals(1, messageSet.getErrorMessages().size());
        assertTrue(messageSet.getErrorMessages().iterator().next().startsWith("jira.jql.clause.positive.duration.format.invalid"));
    }

    private TerminalClauseImpl createClause(final String operand)
    {
        return new TerminalClauseImpl(FIELD_NAME, Operator.EQUALS, operand);
    }

    private TerminalClauseImpl createClause(final long operand)
    {
        return new TerminalClauseImpl(FIELD_NAME, Operator.EQUALS, operand);
    }

    private PositiveDurationValueValidator createValidator(OperandHandler... handlers)
    {
        final MockJqlOperandResolver mockJqlOperandSupport;
        if (handlers == null || handlers.length == 0)
        {
            mockJqlOperandSupport = MockJqlOperandResolver.createSimpleSupport();
        }
        else
        {
            mockJqlOperandSupport = new MockJqlOperandResolver();
            OperandHandler handler = handlers[0];
            mockJqlOperandSupport.addHandler(SingleValueOperand.OPERAND_NAME, handler);
            mockJqlOperandSupport.addHandler(MultiValueOperand.OPERAND_NAME, handler);
        }

        return new PositiveDurationValueValidator(mockJqlOperandSupport, converter)
        {
            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nHelper();
            }
        };
    }
}
