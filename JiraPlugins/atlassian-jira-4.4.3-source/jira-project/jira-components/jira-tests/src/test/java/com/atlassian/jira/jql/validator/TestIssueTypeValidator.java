package com.atlassian.jira.jql.validator;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.IssueTypeResolver;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;
import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;

/**
 * @since v4.0
 */
public class TestIssueTypeValidator extends MockControllerTestCase
{
    @Test
    public void testRawValuesDelegateNotCalledWithOperatorProblem() throws Exception
    {
        final TerminalClauseImpl clause = new TerminalClauseImpl("test", Operator.GREATER_THAN, 12L);

        final JqlOperandResolver mockJqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        mockController.replay();

        final MockControl mockSupportedOperatorsValidatorControl = MockClassControl.createControl(SupportedOperatorsValidator.class);
        final SupportedOperatorsValidator mockSupportedOperatorsValidator = (SupportedOperatorsValidator) mockSupportedOperatorsValidatorControl.getMock();
        mockSupportedOperatorsValidator.validate(null, clause);
        final MessageSetImpl messageSet = new MessageSetImpl();
        messageSet.addErrorMessage("blah blah");
        mockSupportedOperatorsValidatorControl.setReturnValue(messageSet);
        mockSupportedOperatorsValidatorControl.replay();

        final MockControl mockRawValuesExistValidatorControl = MockClassControl.createControl(RawValuesExistValidator.class);
        final RawValuesExistValidator mockRawValuesExistValidator = (RawValuesExistValidator) mockRawValuesExistValidatorControl.getMock();
        mockRawValuesExistValidatorControl.replay();

        final IssueTypeValidator issueTypeValidator = new IssueTypeValidator(null, mockJqlOperandResolver)
        {
            @Override
            SupportedOperatorsValidator getSupportedOperatorsValidator()
            {
                return mockSupportedOperatorsValidator;
            }

            @Override
            RawValuesExistValidator getRawValuesValidator(final IssueTypeResolver issueTypeResolver, final JqlOperandResolver operandSupport)
            {
                return mockRawValuesExistValidator;
            }
        };

        issueTypeValidator.validate(null, clause);

        mockSupportedOperatorsValidatorControl.verify();
        mockRawValuesExistValidatorControl.verify();
        mockController.verify();
    }

    @Test
    public void testRawValuesDelegateCalledWithNoOperatorProblem() throws Exception
    {
        final TerminalClauseImpl clause = new TerminalClauseImpl("test", Operator.GREATER_THAN, 12L);

        final JqlOperandResolver mockJqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        mockController.replay();

        final MockControl mockSupportedOperatorsValidatorControl = MockClassControl.createControl(SupportedOperatorsValidator.class);
        final SupportedOperatorsValidator mockSupportedOperatorsValidator = (SupportedOperatorsValidator) mockSupportedOperatorsValidatorControl.getMock();
        mockSupportedOperatorsValidator.validate(null, clause);
        mockSupportedOperatorsValidatorControl.setReturnValue(new MessageSetImpl());
        mockSupportedOperatorsValidatorControl.replay();

        final MockControl mockRawValuesExistValidatorControl = MockClassControl.createControl(RawValuesExistValidator.class);
        final RawValuesExistValidator mockRawValuesExistValidator = (RawValuesExistValidator) mockRawValuesExistValidatorControl.getMock();
        mockRawValuesExistValidator.validate(null, clause);
        mockRawValuesExistValidatorControl.setReturnValue(new MessageSetImpl());
        mockRawValuesExistValidatorControl.replay();

        final IssueTypeValidator issueTypeValidator = new IssueTypeValidator(null, mockJqlOperandResolver)
        {
            @Override
            SupportedOperatorsValidator getSupportedOperatorsValidator()
            {
                return mockSupportedOperatorsValidator;
            }

            @Override
            RawValuesExistValidator getRawValuesValidator(final IssueTypeResolver issueTypeResolver, final JqlOperandResolver operandSupport)
            {
                return mockRawValuesExistValidator;
            }
        };

        issueTypeValidator.validate(null, clause);

        mockSupportedOperatorsValidatorControl.verify();
        mockRawValuesExistValidatorControl.verify();
        mockController.verify();
    }
}
