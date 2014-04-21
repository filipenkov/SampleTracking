package com.atlassian.jira.jql.validator;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.vote.DefaultVoteManager;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import com.atlassian.jira.jql.util.VotesIndexValueConverter;
import com.atlassian.jira.mock.controller.MockController;
import com.atlassian.jira.security.auth.trustedapps.MockI18nHelper;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import java.util.List;

/**
 * @since v4.0
 */
public class TestVotesValidator extends MockControllerTestCase
{
    private JqlOperandResolver jqlOperandResolver;
    private User theUser = null;

    @Test
    public void testVotingDisabled() throws Exception
    {
        TerminalClause clause = new TerminalClauseImpl("votes", Operator.EQUALS, 10L);

        jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        final DefaultVoteManager defaultVoteManager = mockController.getMock(DefaultVoteManager.class);

        defaultVoteManager.isVotingEnabled();
        mockController.setReturnValue(false);

        mockController.replay();

        final VotesValidator votesValidator = new VotesValidator(jqlOperandResolver, new VotesIndexValueConverter(), defaultVoteManager)
        {
            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nHelper();
            }
        };

        final MessageSet messageSet = votesValidator.validate(theUser, clause);

        assertTrue(messageSet.hasAnyMessages());
        assertTrue(messageSet.getErrorMessages().contains("jira.jql.clause.votes.disabled votes"));

        mockController.verify();
    }

    @Test
    public void testInvalidOperand() throws Exception
    {
        TerminalClause clause = new TerminalClauseImpl("votes", Operator.EQUALS, 10L);

        jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        final DefaultVoteManager defaultVoteManager = mockController.getMock(DefaultVoteManager.class);

        MessageSet messageSet = new MessageSetImpl();
        messageSet.addErrorMessage("error!");

        final SupportedOperatorsValidator operatorsValidator = mockController.getMock(SupportedOperatorsValidator.class);
        operatorsValidator.validate(theUser, clause);
        mockController.setReturnValue(messageSet);

        defaultVoteManager.isVotingEnabled();
        mockController.setReturnValue(true);

        mockController.replay();

        final VotesValidator votesValidator = new VotesValidator(jqlOperandResolver, new VotesIndexValueConverter(), defaultVoteManager)
        {
            @Override
            SupportedOperatorsValidator getSupportedOperatorsValidator()
            {
                return operatorsValidator;
            }
        };

        final MessageSet result = votesValidator.validate(theUser, clause);

        assertTrue(result.hasAnyMessages());
        assertTrue(result.getErrorMessages().contains("error!"));

        mockController.verify();
    }

    @Test
    public void testValidValue() throws Exception
    {
        checkValidation(CollectionBuilder.newBuilder(createLiteral(10L)).asList(), false);
        checkValidation(CollectionBuilder.newBuilder(createLiteral(0L)).asList(), false);
        checkValidation(CollectionBuilder.newBuilder(createLiteral("10")).asList(), false);
        checkValidation(CollectionBuilder.newBuilder(createLiteral("0")).asList(), false);
        checkValidation(CollectionBuilder.newBuilder(createLiteral("0100")).asList(), false);
        checkValidation(CollectionBuilder.newBuilder(createLiteral("10"), createLiteral(10L)).asList(), false);
    }

    @Test
    public void testNotValidValue() throws Exception
    {
        checkValidation(CollectionBuilder.newBuilder(new QueryLiteral()).asList(), false, "EMPTY");
        checkValidation(CollectionBuilder.newBuilder(createLiteral(-1L)).asList(), false, "-1");
        checkValidation(CollectionBuilder.newBuilder(createLiteral("-1")).asList(), false, "-1");
        checkValidation(CollectionBuilder.newBuilder(createLiteral("ab")).asList(), false, "ab");
        checkValidation(CollectionBuilder.newBuilder(new QueryLiteral()).asList(), true, "EMPTY");
        checkValidation(CollectionBuilder.newBuilder(createLiteral(-1L)).asList(), true, "-1");
        checkValidation(CollectionBuilder.newBuilder(createLiteral("-1")).asList(), true, "-1");
        checkValidation(CollectionBuilder.newBuilder(createLiteral("ab")).asList(), true, "ab");
        checkValidation(CollectionBuilder.newBuilder(createLiteral(10L), createLiteral("ab")).asList(), false, "ab");
        checkValidation(CollectionBuilder.newBuilder(createLiteral(-1L), createLiteral("10")).asList(), false, "-1");
        checkValidation(CollectionBuilder.newBuilder(createLiteral("ab"), createLiteral("-1"), createLiteral(-2L)).asList(), false, "ab", "-1", "-2");
    }

    private void checkValidation(final List<QueryLiteral> values, boolean isFunction, final String ... errorValues)
    {
        jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        final DefaultVoteManager defaultVoteManager = mockController.getMock(DefaultVoteManager.class);

        final Operand operand = new SingleValueOperand("");
        TerminalClause clause = new TerminalClauseImpl("votes", Operator.EQUALS, operand);

        jqlOperandResolver.getValues(theUser, operand, clause);
        mockController.setReturnValue(values);

        jqlOperandResolver.isFunctionOperand(operand);
        mockController.setDefaultReturnValue(isFunction);

        defaultVoteManager.isVotingEnabled();
        mockController.setReturnValue(true);


        mockController.replay();

        final VotesValidator votesValidator = new VotesValidator(jqlOperandResolver, new VotesIndexValueConverter(), defaultVoteManager)
        {
            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nHelper();
            }
        };

        final MessageSet messageSet = votesValidator.validate(theUser, clause);

        assertEquals(errorValues.length, messageSet.getErrorMessages().size());
        if (isFunction)
        {
            for (String errorValue : errorValues)
            {
                messageSet.getErrorMessages().contains("jira.jql.clause.invalid.votes.value.function " + errorValue + " name" );
            }
        }
        else
        {
            for (String errorValue : errorValues)
            {
                messageSet.getErrorMessages().contains("jira.jql.clause.invalid.votes.value " + errorValue);
            }
        }

        mockController.verify();
        mockController = new MockController();
    }
}
