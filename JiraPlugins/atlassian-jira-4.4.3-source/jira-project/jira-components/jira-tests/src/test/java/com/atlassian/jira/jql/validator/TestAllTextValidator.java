package com.atlassian.jira.jql.validator;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;
import com.opensymphony.user.User;
import org.easymock.EasyMock;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @since v4.0
 */
public class TestAllTextValidator extends MockControllerTestCase
{
    private CommentValidator commentValidator;
    private AllTextValidator validator;
    private User theUser = null;

    @Before
    public void setUp() throws Exception
    {
        commentValidator = new CommentValidator(MockJqlOperandResolver.createSimpleSupport());
    }

    @Test
    public void testValidateBadOperator()
    {
        final TerminalClause clause = new TerminalClauseImpl("text", Operator.LIKE, "test");
        final MessageSetImpl messageSet = new MessageSetImpl();
        messageSet.addErrorMessage("something");

        final SupportedOperatorsValidator supportedOperatorsValidator = getMock(SupportedOperatorsValidator.class);
        EasyMock.expect(supportedOperatorsValidator.validate(theUser, clause))
                .andReturn(messageSet);

        validator = new AllTextValidator(commentValidator)
        {
            @Override
            SupportedOperatorsValidator getSupportedOperatorsValidator()
            {
                return supportedOperatorsValidator;
            }
        };

        replay();

        final MessageSet result = validator.validate(theUser, clause);
        assertEquals(messageSet, result);
    }

    @Test
    public void testValidateDelegateCalled()
    {
        final AtomicBoolean validatedCalled = new AtomicBoolean(false);
        final TerminalClause clause = new TerminalClauseImpl("text", Operator.LIKE, "test");
        final MessageSetImpl messageSet = new MessageSetImpl();

        final SupportedOperatorsValidator supportedOperatorsValidator = getMock(SupportedOperatorsValidator.class);
        EasyMock.expect(supportedOperatorsValidator.validate(theUser, clause))
                .andReturn(messageSet);

        commentValidator = new CommentValidator(MockJqlOperandResolver.createSimpleSupport())
        {
            @Override
            public MessageSet validate(final User searcher, final TerminalClause terminalClause)
            {
                validatedCalled.set(true);
                return messageSet;
            }
        };

        validator = new AllTextValidator(commentValidator)
        {
            @Override
            SupportedOperatorsValidator getSupportedOperatorsValidator()
            {
                return supportedOperatorsValidator;
            }
        };

        replay();

        final MessageSet result = validator.validate(theUser, clause);
        assertSame(messageSet, result);
        assertTrue(validatedCalled.get());
    }
}
