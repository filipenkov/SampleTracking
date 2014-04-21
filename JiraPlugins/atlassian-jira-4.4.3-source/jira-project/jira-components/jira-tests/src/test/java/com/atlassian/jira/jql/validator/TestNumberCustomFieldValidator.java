package com.atlassian.jira.jql.validator;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.util.NumberIndexValueConverter;
import com.atlassian.jira.security.auth.trustedapps.MockI18nHelper;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;
import com.opensymphony.user.User;

/**
 * @since v4.0
 */
public class TestNumberCustomFieldValidator extends MockControllerTestCase
{
    private JqlOperandResolver jqlOperandResolver;
    private NumberIndexValueConverter numberIndexValueConverter;

    @Before
    public void setUp() throws Exception
    {
        jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        numberIndexValueConverter = mockController.getMock(NumberIndexValueConverter.class);
    }

    @Test
    public void testAddErrorFunction() throws Exception
    {
        final FunctionOperand fop = new FunctionOperand("function");
        TerminalClause clause = new TerminalClauseImpl("field", Operator.EQUALS, fop);
        jqlOperandResolver.isFunctionOperand(fop);
        mockController.setReturnValue(true);
        mockController.replay();
        NumberCustomFieldValidator validator = new NumberCustomFieldValidator(jqlOperandResolver, numberIndexValueConverter)
        {
            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nHelper();
            }
        };


        final IndexValuesValidator indexValuesValidator = validator.getIndexValuesValidator(numberIndexValueConverter);

        MessageSet messageSet = new MessageSetImpl();
        indexValuesValidator.addError(messageSet, null, clause, new QueryLiteral(fop, "10a"));

        assertTrue(messageSet.hasAnyErrors());
        assertTrue(messageSet.getErrorMessages().contains("jira.jql.clause.invalid.number.value.function function field"));
        mockController.verify();
    }

    @Test
    public void testAddErrorValue() throws Exception
    {
        final SingleValueOperand operand = new SingleValueOperand("10a");
        TerminalClause clause = new TerminalClauseImpl("field", Operator.EQUALS, "blah");
        jqlOperandResolver.isFunctionOperand(operand);
        mockController.setReturnValue(false);
        mockController.replay();
        NumberCustomFieldValidator validator = new NumberCustomFieldValidator(jqlOperandResolver, numberIndexValueConverter)
        {
            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nHelper();
            }
        };


        final IndexValuesValidator indexValuesValidator = validator.getIndexValuesValidator(numberIndexValueConverter);

        MessageSet messageSet = new MessageSetImpl();
        indexValuesValidator.addError(messageSet, null, clause, new QueryLiteral(operand, "10a"));

        assertTrue(messageSet.hasAnyErrors());
        assertTrue(messageSet.getErrorMessages().contains("jira.jql.clause.invalid.number.value field 10a"));
        mockController.verify();
    }
}
