package com.atlassian.jira.jql.validator;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.After;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operand.MockOperandHandler;
import com.atlassian.jira.jql.operand.OperandHandler;
import com.atlassian.jira.security.auth.trustedapps.MockI18nHelper;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

/**
 * @since v4.0
 */
public class TestIntegerValueValidator extends ListeningTestCase
{
    private static final String FIELD_NAME = "field";
    
    private User theUser = null;
    private IntegerValueValidator validator;

    @Before
    public void setUp() throws Exception
    {
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
        assertValid(-999L);
        assertValid(0L);
        assertValid(999L);
    }

    @Test
    public void testValidateStrings() throws Exception
    {
        assertValid("-999");
        assertValid("0");
        assertValid("999");
        assertInvalid(" 999");
        assertInvalid("999 ");
        assertInvalid("-999.9");
        assertInvalid("0.0");
        assertInvalid("999.5");
        assertInvalid("99a");
        assertInvalid("a99");
        assertInvalid("aaa");
    }

    @Test
    public void testValidateEmptyOperand() throws Exception
    {
        validator = createValidator();

        final TerminalClauseImpl clause = new TerminalClauseImpl(FIELD_NAME, Operator.EQUALS, EmptyOperand.EMPTY);

        final MessageSet messageSet = validator.validate(null, clause);
        assertFalse(messageSet.hasAnyMessages());
    }

    @Test
    public void testValidateStringsFromFunction() throws Exception
    {
        final MockOperandHandler handler = new MockOperandHandler(false, false, true);
        handler.add("0", "abc");
        validator = createValidator(handler);

        final MessageSet messageSet = validator.validate(null, new TerminalClauseImpl(FIELD_NAME, Operator.EQUALS, "ignored"));
        assertTrue(messageSet.hasAnyMessages());
        assertEquals(1, messageSet.getErrorMessages().size());
        String error = messageSet.getErrorMessages().iterator().next();
        assertEquals("jira.jql.clause.integer.format.invalid.from.func SingleValueOperand field", error);
    }

    private void assertValid(long operand)
    {
        final MessageSet messageSet = validator.validate(theUser, new TerminalClauseImpl(FIELD_NAME, Operator.EQUALS, operand));
        assertFalse(messageSet.hasAnyErrors());
    }

    private void assertValid(String operand)
    {
        final MessageSet messageSet = validator.validate(theUser, createClause(operand));
        assertFalse(messageSet.hasAnyErrors());
    }

    private void assertInvalid(String operand)
    {
        final MessageSet messageSet = validator.validate(theUser, createClause(operand));
        assertTrue(messageSet.hasAnyErrors());
        assertEquals(1, messageSet.getErrorMessages().size());
        assertTrue(messageSet.getErrorMessages().iterator().next().startsWith("jira.jql.clause.integer.format.invalid"));
    }

    private TerminalClauseImpl createClause(final String operand)
    {
        return new TerminalClauseImpl(FIELD_NAME, Operator.EQUALS, operand);
    }

    private static IntegerValueValidator createValidator(OperandHandler... handlers)
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
        return new IntegerValueValidator(mockJqlOperandSupport)
        {
            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nHelper();
            }
        };
    }
}
