package com.atlassian.jira.jql.validator;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.security.auth.trustedapps.MockI18nHelper;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;

/**
 * @since v4.0
 */
public class TestWorkRatioValidator extends MockControllerTestCase
{
    private ApplicationProperties applicationProperties;

    @Before
    public void setUp() throws Exception
    {
        applicationProperties = mockController.getMock(ApplicationProperties.class);
    }

    @Test
    public void testTimeTrackingOff() throws Exception
    {
        final User theUser = null;

        final TerminalClauseImpl clause = new TerminalClauseImpl("test", Operator.GREATER_THAN, 12L);

        final SupportedOperatorsValidator mockSupportedOperatorsValidator = mockController.getMock(SupportedOperatorsValidator.class);
        final IntegerValueValidator mockIntegerValueValidator = mockController.getMock(IntegerValueValidator.class);

        applicationProperties.getOption(APKeys.JIRA_OPTION_TIMETRACKING);
        mockController.setDefaultReturnValue(false);

        final WorkRatioValidator workRatioValidator = new WorkRatioValidator(new MockJqlOperandResolver(), applicationProperties)
        {
            @Override
            SupportedOperatorsValidator getSupportedOperatorsValidator()
            {
                return mockSupportedOperatorsValidator;
            }

            @Override
            IntegerValueValidator getIntegerValueValidator(final JqlOperandResolver operandSupport)
            {
                return mockIntegerValueValidator;
            }

            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nHelper();
            }
        };

        mockController.replay();

        final MessageSet result = workRatioValidator.validate(theUser, clause);
        assertTrue(result.getErrorMessages().contains("jira.jql.clause.timetracking.disabled test"));

        mockController.verify();
    }
    
    @Test
    public void testPositiveDurationDelegateNotCalledWithOperatorProblem() throws Exception
    {
        final User theUser = null;

        final TerminalClauseImpl clause = new TerminalClauseImpl("test", Operator.GREATER_THAN, 12L);
        final MessageSetImpl messageSet = new MessageSetImpl();
        messageSet.addErrorMessage("blah blah");

        final SupportedOperatorsValidator mockSupportedOperatorsValidator = mockController.getMock(SupportedOperatorsValidator.class);
        expect(mockSupportedOperatorsValidator.validate(theUser, clause))
                .andReturn(messageSet);

        final IntegerValueValidator mockIntegerValueValidator = mockController.getMock(IntegerValueValidator.class);

        applicationProperties.getOption(APKeys.JIRA_OPTION_TIMETRACKING);
        mockController.setDefaultReturnValue(true);

        final WorkRatioValidator workRatioValidator = new WorkRatioValidator(new MockJqlOperandResolver(), applicationProperties)
        {
            @Override
            SupportedOperatorsValidator getSupportedOperatorsValidator()
            {
                return mockSupportedOperatorsValidator;
            }

            @Override
            IntegerValueValidator getIntegerValueValidator(final JqlOperandResolver operandSupport)
            {
                return mockIntegerValueValidator;
            }
        };

        mockController.replay();

        workRatioValidator.validate(theUser, clause);

        mockController.verify();
    }

    @Test
    public void testPositiveDurationDelegateCalledWithNoOperatorProblem() throws Exception
    {
        final User theUser = null;

        final TerminalClauseImpl clause = new TerminalClauseImpl("test", Operator.GREATER_THAN, 12L);
        final MessageSetImpl messageSet = new MessageSetImpl();

        final SupportedOperatorsValidator mockSupportedOperatorsValidator = mockController.getMock(SupportedOperatorsValidator.class);
        expect(mockSupportedOperatorsValidator.validate(theUser, clause))
                .andReturn(messageSet);

        final IntegerValueValidator mockIntegerValueValidator = mockController.getMock(IntegerValueValidator.class);
        expect(mockIntegerValueValidator.validate(theUser, clause))
                .andReturn(messageSet);

        applicationProperties.getOption(APKeys.JIRA_OPTION_TIMETRACKING);
        mockController.setDefaultReturnValue(true);

        final WorkRatioValidator workRatioValidator = new WorkRatioValidator(new MockJqlOperandResolver(), applicationProperties)
        {
            @Override
            SupportedOperatorsValidator getSupportedOperatorsValidator()
            {
                return mockSupportedOperatorsValidator;
            }

            @Override
            IntegerValueValidator getIntegerValueValidator(final JqlOperandResolver operandSupport)
            {
                return mockIntegerValueValidator;
            }
        };

        mockController.replay();

        workRatioValidator.validate(theUser, clause);

        mockController.verify();
    }
}
