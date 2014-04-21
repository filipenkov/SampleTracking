package com.atlassian.jira.jql.validator;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.util.GroupCustomFieldIndexValueConverter;
import com.atlassian.jira.security.auth.trustedapps.MockI18nHelper;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operator.Operator;
import com.opensymphony.user.User;
import org.easymock.EasyMock;

import java.util.List;

/**
 * @since v4.0
 */
public class TestGroupCustomFieldValidator extends MockControllerTestCase
{
    private JqlOperandResolver jqlOperandResolver;
    private GroupCustomFieldIndexValueConverter groupCustomFieldIndexValueConverter;

    @Before
    public void setUp() throws Exception
    {
        jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        groupCustomFieldIndexValueConverter = mockController.getMock(GroupCustomFieldIndexValueConverter.class);
    }

    @Test
    public void testOperatorChecks() throws Exception
    {

        final IndexValuesValidator indexValuesValidator = mockController.getMock(IndexValuesValidator.class);
        indexValuesValidator.validate((User)EasyMock.isNull(), EasyMock.isA(TerminalClause.class));
        mockController.setDefaultReturnValue(new MessageSetImpl());

        List<Operator> valid = CollectionBuilder.<Operator>newBuilder(Operator.EQUALS, Operator.IS, Operator.IN, Operator.IS_NOT, Operator.NOT_IN, Operator.NOT_EQUALS).asList();

        mockController.replay();
        final GroupCustomFieldValidator validator = new GroupCustomFieldValidator(jqlOperandResolver, groupCustomFieldIndexValueConverter)
        {
            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nHelper();
            }

            @Override
            IndexValuesValidator getIndexValuesValidator(final GroupCustomFieldIndexValueConverter groupCustomFieldIndexValueConverter)
            {
                return indexValuesValidator;
            }
        };

        for (Operator operator : valid)
        {
            final TerminalClauseImpl clause = new TerminalClauseImpl("group", operator, "blah");
            final MessageSet result = validator.validate(null, clause);
            assertFalse(result.hasAnyErrors());
        }

        mockController.verify();
    }

    @Test
    public void testAddErrorFunction() throws Exception
    {
        final FunctionOperand fop = new FunctionOperand("function");
        TerminalClause clause = new TerminalClauseImpl("field", Operator.EQUALS, fop);
        jqlOperandResolver.isFunctionOperand(clause.getOperand());
        mockController.setReturnValue(true);
        mockController.replay();
        GroupCustomFieldValidator validator = new GroupCustomFieldValidator(jqlOperandResolver, groupCustomFieldIndexValueConverter)
        {
            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nHelper();
            }
        };


        final IndexValuesValidator indexValuesValidator = validator.getIndexValuesValidator(groupCustomFieldIndexValueConverter);

        MessageSet messageSet = new MessageSetImpl();
        indexValuesValidator.addError(messageSet, null, clause, new QueryLiteral(fop, "group"));

        assertTrue(messageSet.hasAnyErrors());
        assertTrue(messageSet.getErrorMessages().contains("jira.jql.clause.invalid.group.value.function function field"));
        mockController.verify();
    }

    @Test
    public void testAddErrorValue() throws Exception
    {
        final FunctionOperand fop = new FunctionOperand("function");
        TerminalClause clause = new TerminalClauseImpl("field", Operator.EQUALS, "blah");
        jqlOperandResolver.isFunctionOperand(fop);
        mockController.setReturnValue(false);
        mockController.replay();
        GroupCustomFieldValidator validator = new GroupCustomFieldValidator(jqlOperandResolver, groupCustomFieldIndexValueConverter)
        {
            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nHelper();
            }
        };


        final IndexValuesValidator indexValuesValidator = validator.getIndexValuesValidator(groupCustomFieldIndexValueConverter);

        MessageSet messageSet = new MessageSetImpl();
        indexValuesValidator.addError(messageSet, null, clause, new QueryLiteral(fop, "group"));

        assertTrue(messageSet.hasAnyErrors());
        assertTrue(messageSet.getErrorMessages().contains("jira.jql.clause.invalid.group.value field group"));
        mockController.verify();
    }

}
