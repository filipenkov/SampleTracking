package com.atlassian.jira.jql.validator;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.security.auth.trustedapps.MockI18nHelper;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;
import com.opensymphony.user.User;

/**
 * @since v4.0
 */
public class TestAbstractTimeTrackingValidator extends MockControllerTestCase
{
    private MockJqlOperandResolver jqlOperandResolver;
    private ApplicationProperties applicationProperties;

    @Before
    public void setUp() throws Exception
    {
        jqlOperandResolver = MockJqlOperandResolver.createSimpleSupport();
        applicationProperties = mockController.getMock(ApplicationProperties.class);
    }

    @Test
    public void testTimeTrackingOff() throws Exception
    {
        final User theUser = null;

        final TerminalClauseImpl clause = new TerminalClauseImpl("test", Operator.GREATER_THAN, 12L);

        final SupportedOperatorsValidator mockSupportedOperatorsValidator = mockController.getMock(SupportedOperatorsValidator.class);
        final PositiveDurationValueValidator mockPositiveDurationValueValidator = mockController.getMock(PositiveDurationValueValidator.class);

        applicationProperties.getOption(APKeys.JIRA_OPTION_TIMETRACKING);
        mockController.setDefaultReturnValue(false);

        final AbstractTimeTrackingValidator validator = new AbstractTimeTrackingValidator(new MockJqlOperandResolver(), applicationProperties)
        {
            @Override
            SupportedOperatorsValidator getSupportedOperatorsValidator()
            {
                return mockSupportedOperatorsValidator;
            }

            @Override
            PositiveDurationValueValidator getPositiveDurationValueValidator(final JqlOperandResolver operandResolver)
            {
                return mockPositiveDurationValueValidator;
            }

            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nHelper();
            }
        };

        mockController.replay();

        final MessageSet result = validator.validate(theUser, clause);
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

        final PositiveDurationValueValidator mockPositiveDurationValueValidator = mockController.getMock(PositiveDurationValueValidator.class);

        applicationProperties.getOption(APKeys.JIRA_OPTION_TIMETRACKING);
        mockController.setDefaultReturnValue(true);

        final AbstractTimeTrackingValidator abstractTimeTrackingValidator = new AbstractTimeTrackingValidator(jqlOperandResolver, applicationProperties)
        {
            @Override
            SupportedOperatorsValidator getSupportedOperatorsValidator()
            {
                return mockSupportedOperatorsValidator;
            }

            @Override
            PositiveDurationValueValidator getPositiveDurationValueValidator(final JqlOperandResolver operandSupport)
            {
                return mockPositiveDurationValueValidator;
            }
        };

        mockController.replay();

        abstractTimeTrackingValidator.validate(theUser, clause);

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

        final PositiveDurationValueValidator mockPositiveDurationValueValidator = mockController.getMock(PositiveDurationValueValidator.class);
        expect(mockPositiveDurationValueValidator.validate(theUser, clause))
                .andReturn(messageSet);

        applicationProperties.getOption(APKeys.JIRA_OPTION_TIMETRACKING);
        mockController.setDefaultReturnValue(true);

        final AbstractTimeTrackingValidator abstractTimeTrackingValidator = new AbstractTimeTrackingValidator(jqlOperandResolver, applicationProperties)
        {
            @Override
            SupportedOperatorsValidator getSupportedOperatorsValidator()
            {
                return mockSupportedOperatorsValidator;
            }

            @Override
            PositiveDurationValueValidator getPositiveDurationValueValidator(final JqlOperandResolver operandSupport)
            {
                return mockPositiveDurationValueValidator;
            }
        };

        mockController.replay();

        abstractTimeTrackingValidator.validate(theUser, clause);

        mockController.verify();
    }
}
