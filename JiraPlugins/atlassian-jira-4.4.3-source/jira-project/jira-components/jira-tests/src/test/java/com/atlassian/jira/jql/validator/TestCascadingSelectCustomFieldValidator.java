package com.atlassian.jira.jql.validator;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.opensymphony.user.User;
import com.atlassian.jira.action.issue.customfields.option.MockOption;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.jql.operand.QueryLiteral;
import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import com.atlassian.jira.jql.util.JqlSelectOptionsUtil;
import com.atlassian.jira.security.auth.trustedapps.MockI18nHelper;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

/**
 * @since v4.0
 */
public class TestCascadingSelectCustomFieldValidator extends MockControllerTestCase
{
    private CustomField customField;
    private JqlSelectOptionsUtil jqlSelectOptionsUtil;
    private MockJqlOperandResolver jqlOperandResolver;
    private I18nHelper.BeanFactory beanFactory;
    private SupportedOperatorsValidator operatorsValidator;

    @Before
    public void setUp() throws Exception
    {
        customField = mockController.getMock(CustomField.class);
        jqlSelectOptionsUtil = mockController.getMock(JqlSelectOptionsUtil.class);
        jqlOperandResolver = MockJqlOperandResolver.createSimpleSupport();
        beanFactory = mockController.getMock(I18nHelper.BeanFactory.class);
        beanFactory.getInstance((User)null);
        mockController.setDefaultReturnValue(new MockI18nHelper());

        operatorsValidator = new SupportedOperatorsValidator()
        {
            @Override
            public MessageSet validate(final User searcher, final TerminalClause terminalClause)
            {
                return new MessageSetImpl();
            }
        };
    }

    @Test
    public void testValidateBadOperator() throws Exception
    {
        TerminalClause clause = new TerminalClauseImpl("blah", Operator.LESS_THAN, "blah");

        final MessageSetImpl messageSet = new MessageSetImpl();
        messageSet.addErrorMessage("o no!!");
        final SupportedOperatorsValidator operatorsValidator = mockController.getMock(SupportedOperatorsValidator.class);
        operatorsValidator.validate(null, clause);
        mockController.setReturnValue(messageSet);

        mockController.replay();
        final CascadingSelectCustomFieldValidator validator = new CascadingSelectCustomFieldValidator(customField, jqlSelectOptionsUtil, jqlOperandResolver, beanFactory)
        {
            @Override
            SupportedOperatorsValidator getSupportedOperatorsValidator()
            {
                return operatorsValidator;
            }
        };

        final MessageSet result = validator.validate(null, clause);
        assertTrue(result.getErrorMessages().contains("o no!!"));
        
        mockController.verify();
    }

    @Test
    public void testValidateEmptyOperand() throws Exception
    {
        TerminalClause clause = new TerminalClauseImpl("blah", Operator.LESS_THAN, EmptyOperand.EMPTY);

        mockController.replay();
        final CascadingSelectCustomFieldValidator validator = new CascadingSelectCustomFieldValidator(customField, jqlSelectOptionsUtil, jqlOperandResolver, beanFactory)
        {
            @Override
            SupportedOperatorsValidator getSupportedOperatorsValidator()
            {
                return operatorsValidator;
            }
        };

        final MessageSet result = validator.validate(null, clause);

        assertFalse(result.hasAnyMessages());

        mockController.verify();
    }

    @Test
    public void testValidateLiteralHasNoOptions() throws Exception
    {
        final QueryLiteral literal = createLiteral("blah");
        TerminalClause clause = new TerminalClauseImpl("blah", Operator.LESS_THAN, new SingleValueOperand(literal));

        jqlSelectOptionsUtil.getOptions(customField, (User)null, literal, true);
        mockController.setReturnValue(CollectionBuilder.<Option>newBuilder().asList());

        mockController.replay();
        final CascadingSelectCustomFieldValidator validator = new CascadingSelectCustomFieldValidator(customField, jqlSelectOptionsUtil, jqlOperandResolver, beanFactory)
        {
            @Override
            SupportedOperatorsValidator getSupportedOperatorsValidator()
            {
                return operatorsValidator;
            }
        };

        final MessageSet result = validator.validate(null, clause);

        assertTrue(result.getErrorMessages().contains("jira.jql.clause.select.option.does.not.exist blah blah"));

        mockController.verify();
    }

    @Test
    public void testValidateLiteralHasOneVisible() throws Exception
    {
        final QueryLiteral literal = createLiteral("blah");
        TerminalClause clause = new TerminalClauseImpl("blah", Operator.LESS_THAN, new SingleValueOperand(literal));
        final MockOption option = new MockOption(null, null, null, null, null, 10L);

        jqlSelectOptionsUtil.getOptions(customField, (User)null, literal, true);
        mockController.setReturnValue(CollectionBuilder.<Option>newBuilder(option).asList());

        mockController.replay();
        final CascadingSelectCustomFieldValidator validator = new CascadingSelectCustomFieldValidator(customField, jqlSelectOptionsUtil, jqlOperandResolver, beanFactory)
        {
            @Override
            SupportedOperatorsValidator getSupportedOperatorsValidator()
            {
                return operatorsValidator;
            }
        };

        final MessageSet result = validator.validate(null, clause);

        assertFalse(result.hasAnyMessages());

        mockController.verify();
    }

    @Test
    public void testValidateNegativeLiteral() throws Exception
    {
        final QueryLiteral literal = createLiteral(-555L);
        TerminalClause clause = new TerminalClauseImpl("blah", Operator.LESS_THAN, new SingleValueOperand(literal));
        final MockOption option1 = new MockOption(null, null, null, null, null, 10L);
        final MockOption option2 = new MockOption(null, null, null, null, null, 10L);

        jqlSelectOptionsUtil.getOptions(customField, (User)null, literal, true);
        mockController.setReturnValue(CollectionBuilder.newBuilder(option2).asList());

        jqlSelectOptionsUtil.getOptions(customField, (User) null, createLiteral(-literal.getLongValue()), true);
        mockController.setReturnValue(CollectionBuilder.<Option>newBuilder(option1).asList());

        mockController.replay();
        final CascadingSelectCustomFieldValidator validator = new CascadingSelectCustomFieldValidator(customField, jqlSelectOptionsUtil, jqlOperandResolver, beanFactory)
        {
            @Override
            SupportedOperatorsValidator getSupportedOperatorsValidator()
            {
                return operatorsValidator;
            }
        };

        final MessageSet result = validator.validate(null, clause);

        assertFalse(result.hasAnyMessages());

        mockController.verify();
    }
}
