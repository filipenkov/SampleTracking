package com.atlassian.jira.jql.validator;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.action.issue.customfields.option.MockOption;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import com.atlassian.jira.jql.util.JqlSelectOptionsUtil;
import com.atlassian.jira.security.auth.trustedapps.MockI18nHelper;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;
import com.opensymphony.user.User;

import java.util.Collections;

/**
 * @since v4.0
 */
public class TestSelectCustomFieldValidator extends MockControllerTestCase
{
    private CustomField customField;
    private SupportedOperatorsValidator supportedOperatorsValidator;
    private JqlSelectOptionsUtil jqlSelectOptionsUtil;
    private TerminalClauseImpl clause;
    private Operand operand;
    private String field;
    private JqlOperandResolver jqlOperandResolver;
    private I18nHelper.BeanFactory beanFactory;
    private User theUser;

    @Before
    public void setUp() throws Exception
    {
        customField = mockController.getMock(CustomField.class);
        supportedOperatorsValidator = mockController.getMock(SupportedOperatorsValidator.class);
        jqlSelectOptionsUtil = mockController.getMock(JqlSelectOptionsUtil.class);
        jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        beanFactory = mockController.getMock(I18nHelper.BeanFactory.class);
        beanFactory.getInstance((User)null);
        mockController.setDefaultReturnValue(new MockI18nHelper());
        operand = new SingleValueOperand("value");
        field = "field";
        clause = new TerminalClauseImpl(field, Operator.LESS_THAN_EQUALS, operand);
    }

    @Test
    public void testValidateInvalidOperator() throws Exception
    {
        MessageSet messageSet = new MessageSetImpl();
        messageSet.addErrorMessage("BUGGER!");

        supportedOperatorsValidator.validate(theUser, clause);
        mockController.setReturnValue(messageSet);

        mockController.replay();
        final SelectCustomFieldValidator validator = createValidator();
        final MessageSet result = validator.validate(theUser, clause);

        assertEquals(messageSet, result);
        mockController.verify();
    }

    @Test
    public void testValidateNoValues() throws Exception
    {
        final MessageSetImpl emptyMessageSet = new MessageSetImpl();
        supportedOperatorsValidator.validate(theUser, clause);
        mockController.setReturnValue(emptyMessageSet);

        jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        jqlOperandResolver.getValues(theUser, operand, clause);
        mockController.setReturnValue(Collections.emptyList());
        
        mockController.replay();
        final SelectCustomFieldValidator validator = createValidator();
        final MessageSet result = validator.validate(theUser, clause);

        assertFalse(result.hasAnyMessages());

        mockController.verify();
    }

    @Test
    public void testValidateNullValues() throws Exception
    {
        final MessageSetImpl emptyMessageSet = new MessageSetImpl();
        supportedOperatorsValidator.validate(theUser, clause);
        mockController.setReturnValue(emptyMessageSet);

        jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        jqlOperandResolver.getValues(theUser, operand, clause);
        mockController.setReturnValue(null);

        mockController.replay();
        final SelectCustomFieldValidator validator = createValidator();
        final MessageSet result = validator.validate(theUser, clause);

        assertFalse(result.hasAnyMessages());

        mockController.verify();
    }

    @Test
    public void testValidateSearcherNoVisibleOptions() throws Exception
    {
        QueryLiteral literal = createLiteral("value");

        supportedOperatorsValidator.validate(theUser, clause);
        mockController.setReturnValue(new MessageSetImpl());

        jqlOperandResolver.getValues(theUser, operand, clause);
        mockController.setReturnValue(CollectionBuilder.<QueryLiteral>newBuilder(literal).asList());

        jqlOperandResolver.isFunctionOperand(operand);
        mockController.setReturnValue(false);

        jqlSelectOptionsUtil.getOptions(customField, (User)null, literal, false);
        mockController.setReturnValue(Collections.emptyList());

        mockController.replay();

        final SelectCustomFieldValidator validator = createValidator();
        final MessageSet result = validator.validate(theUser, clause);

        assertTrue(result.hasAnyMessages());
        assertTrue(result.getErrorMessages().contains("jira.jql.clause.select.option.does.not.exist value " + field));

        mockController.verify();
    }

    @Test
    public void testValidateSearcherNoVisibleOptionsFunction() throws Exception
    {
        QueryLiteral literal = createLiteral("value");

        supportedOperatorsValidator.validate(theUser, clause);
        mockController.setReturnValue(new MessageSetImpl());

        jqlOperandResolver.getValues(theUser, operand, clause);
        mockController.setReturnValue(CollectionBuilder.<QueryLiteral>newBuilder(literal).asList());

        jqlOperandResolver.isFunctionOperand(operand);
        mockController.setReturnValue(true);

        jqlSelectOptionsUtil.getOptions(customField, (User)null, literal, false);
        mockController.setReturnValue(Collections.emptyList());

        mockController.replay();

        final SelectCustomFieldValidator validator = createValidator();
        final MessageSet result = validator.validate(theUser, clause);

        assertTrue(result.hasAnyMessages());
        assertTrue(result.getErrorMessages().contains("jira.jql.clause.select.option.does.not.exist.function SingleValueOperand " + field));

        mockController.verify();
    }

    @Test
    public void testValidateSearcherOneVisibleOption() throws Exception
    {
        QueryLiteral literal = createLiteral("value1");

        final MockOption option1 = new MockOption(null, null, null, "value", null, 10L);

        supportedOperatorsValidator.validate(theUser, clause);
        mockController.setReturnValue(new MessageSetImpl());

        jqlOperandResolver.getValues(theUser, operand, clause);
        mockController.setReturnValue(CollectionBuilder.<QueryLiteral>newBuilder(literal).asList());

        jqlSelectOptionsUtil.getOptions(customField, (User)null, literal, false);
        mockController.setReturnValue(CollectionBuilder.<Option>newBuilder(option1).asList());

        mockController.replay();

        final SelectCustomFieldValidator validator = createValidator();
        final MessageSet result = validator.validate(theUser, clause);

        assertFalse(result.hasAnyMessages());

        mockController.verify();
    }

    @Test
    public void testValidateSearcherOneEmpty() throws Exception
    {
        QueryLiteral literal = createLiteral("value1");

        supportedOperatorsValidator.validate(theUser, clause);
        mockController.setReturnValue(new MessageSetImpl());

        jqlOperandResolver.getValues(theUser, operand, clause);
        mockController.setReturnValue(CollectionBuilder.<QueryLiteral>newBuilder(literal).asList());

        jqlSelectOptionsUtil.getOptions(customField, (User)null, literal, false);
        mockController.setReturnValue(CollectionBuilder.<Option>newBuilder((Option)null).asList());

        mockController.replay();

        final SelectCustomFieldValidator validator = createValidator();
        final MessageSet result = validator.validate(theUser, clause);

        assertFalse(result.hasAnyMessages());

        mockController.verify();
    }

    @Test
    public void testValidateSearcherOneEmptyLiteral() throws Exception
    {
        QueryLiteral literal = new QueryLiteral();

        supportedOperatorsValidator.validate(theUser, clause);
        mockController.setReturnValue(new MessageSetImpl());

        jqlOperandResolver.getValues(theUser, operand, clause);
        mockController.setReturnValue(CollectionBuilder.<QueryLiteral>newBuilder(literal).asList());

        mockController.replay();

        final SelectCustomFieldValidator validator = createValidator();
        final MessageSet result = validator.validate(theUser, clause);

        assertFalse(result.hasAnyMessages());

        mockController.verify();
    }

    private SelectCustomFieldValidator createValidator()
    {
        return new SelectCustomFieldValidator(customField, jqlSelectOptionsUtil, jqlOperandResolver, beanFactory)
        {
            @Override
            SupportedOperatorsValidator getSupportedOperatorsValidator()
            {
                return supportedOperatorsValidator;
            }
        };
    }
}
