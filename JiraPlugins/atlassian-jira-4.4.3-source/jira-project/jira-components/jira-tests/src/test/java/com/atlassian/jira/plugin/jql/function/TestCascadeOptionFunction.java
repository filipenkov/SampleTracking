package com.atlassian.jira.plugin.jql.function;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.action.issue.customfields.option.MockOption;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.impl.CascadingSelectCFType;
import com.atlassian.jira.issue.customfields.impl.TextCFType;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.managers.SearchHandlerManager;
import com.atlassian.jira.jql.operand.QueryLiteral;
import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.query.QueryCreationContextImpl;
import com.atlassian.jira.jql.util.JqlCascadingSelectLiteralUtil;
import com.atlassian.jira.jql.util.JqlSelectOptionsUtil;
import com.atlassian.jira.security.auth.trustedapps.MockI18nHelper;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operator.Operator;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @since v4.0
 */
public class TestCascadeOptionFunction extends MockControllerTestCase
{
    private CustomFieldManager customFieldManager;
    private SearchHandlerManager searchHandlerManager;
    private JqlSelectOptionsUtil jqlSelectOptionsUtil;
    final private String clauseName = "cascade";
    final private String functionName = "CascadeOption";
    private String customFieldId = "1000";
    private CustomField customField;
    private JqlCascadingSelectLiteralUtil jqlCascadingSelectLiteralUtil;
    private com.opensymphony.user.User theUser = null;
    private QueryCreationContext queryCreationContext;

    @Before
    public void setUp() throws Exception
    {
        customFieldManager = mockController.getMock(CustomFieldManager.class);
        searchHandlerManager = mockController.getMock(SearchHandlerManager.class);
        jqlSelectOptionsUtil = mockController.getMock(JqlSelectOptionsUtil.class);
        customField = mockController.getMock(CustomField.class);
        jqlCascadingSelectLiteralUtil = mockController.getMock(JqlCascadingSelectLiteralUtil.class);
        queryCreationContext = new QueryCreationContextImpl(theUser);
    }

    @Test
    public void testValidateNoFields() throws Exception
    {
        FunctionOperand functionOperand = new FunctionOperand(functionName, "parent", "child");
        TerminalClause clause = createClause(functionOperand);

        searchHandlerManager.getFieldIds((User) theUser, clauseName);
        mockController.setReturnValue(Collections.emptyList());

        mockController.replay();

        final CascadeOptionFunction function = createFunction(null, null);

        MessageSet result = function.validate(theUser, functionOperand, clause);

        assertTrue(result.getErrorMessages().contains(String.format("jira.jql.function.cascade.option.not.cascade.field %s %s", clauseName, functionName)));
        mockController.verify();
    }

    @Test
    public void testValidateWrongCustomFieldType() throws Exception
    {
        FunctionOperand functionOperand = new FunctionOperand(functionName, "parent", "child");
        TerminalClause clause = createClause(functionOperand);

        expectClauseResolvesToCustomField(TextCFType.class);

        mockController.replay();

        final CascadeOptionFunction function = createFunction(null, null);

        MessageSet result = function.validate(theUser, functionOperand, clause);

        assertTrue(result.getErrorMessages().contains(String.format("jira.jql.function.cascade.option.not.cascade.field %s %s", clauseName, functionName)));
        mockController.verify();
    }

    @Test
    public void testValidateWrongArgsSize() throws Exception
    {
        FunctionOperand functionOperand = new FunctionOperand(functionName, "parent", "child", "blarg");
        TerminalClause clause = createClause(functionOperand);

        expectClauseResolvesToCustomField();

        mockController.replay();

        final CascadeOptionFunction function = createFunction(null, null);

        MessageSet result = function.validate(theUser, functionOperand, clause);

        assertTrue(result.getErrorMessages().contains(String.format("jira.jql.function.cascade.option.incorrect.args %s", functionName)));
        mockController.verify();
    }

    @Test
    public void testValidateEmptyArgs() throws Exception
    {
        FunctionOperand functionOperand = new FunctionOperand(functionName);
        TerminalClause clause = createClause(functionOperand);

        expectClauseResolvesToCustomField();

        mockController.replay();

        final CascadeOptionFunction function = createFunction(null, null);

        MessageSet result = function.validate(theUser, functionOperand, clause);

        assertTrue(result.getErrorMessages().contains(String.format("jira.jql.function.cascade.option.incorrect.args %s", functionName)));
        mockController.verify();
    }

    @Test
    public void testValidateParentArgIsNotParent() throws Exception
    {
        FunctionOperand functionOperand = new FunctionOperand(functionName, "child", "parent");
        TerminalClause clause = createClause(functionOperand);

        expectClauseResolvesToCustomField();

        final MockOption parentOption = createParentOption(100L);
        final MockOption childOption = createChildOption(parentOption, 200L);
        parentOption.setChildOptions(CollectionBuilder.newBuilder(childOption).asList());

        jqlSelectOptionsUtil.getOptions(customField, createLiteral("child"), true);
        mockController.setReturnValue(CollectionBuilder.<Option>newBuilder(
                childOption
        ).asList());

        mockController.replay();

        final CascadeOptionFunction function = createFunction(null, null);

        MessageSet result = function.validate(theUser, functionOperand, clause);

        assertTrue(result.getErrorMessages().contains(String.format("jira.jql.function.cascade.option.not.parent %s %s", functionName, "child")));
        mockController.verify();
    }

    @Test
    public void testValidateSingleParentAndChildHappyPath() throws Exception
    {
        FunctionOperand functionOperand = new FunctionOperand(functionName, "parent", "child");
        TerminalClause clause = createClause(functionOperand);

        expectClauseResolvesToCustomField();

        final MockOption parentOption = createParentOption(100L);
        final MockOption childOption = createChildOption(parentOption, 200L);
        parentOption.setChildOptions(CollectionBuilder.newBuilder(childOption).asList());

        jqlSelectOptionsUtil.getOptions(customField, createLiteral("parent"), true);
        mockController.setReturnValue(CollectionBuilder.<Option>newBuilder(
                parentOption
        ).asList());

        jqlSelectOptionsUtil.getOptions(customField, createLiteral("child"), true);
        mockController.setReturnValue(CollectionBuilder.<Option>newBuilder(
                childOption
        ).asList());

        mockController.replay();

        final CascadeOptionFunction function = createFunction(null, null);

        MessageSet result = function.validate(theUser, functionOperand, clause);

        assertFalse(result.hasAnyMessages());
        mockController.verify();
    }

    @Test
    public void testValidateSingleParentAndNoneChild() throws Exception
    {
        FunctionOperand functionOperand = new FunctionOperand(functionName, "parent", "none");
        TerminalClause clause = createClause(functionOperand);

        expectClauseResolvesToCustomField();

        final MockOption parentOption = createParentOption(100L);
        final MockOption childOption = createChildOption(parentOption, 200L);
        parentOption.setChildOptions(CollectionBuilder.newBuilder(childOption).asList());

        jqlSelectOptionsUtil.getOptions(customField, createLiteral("parent"), true);
        mockController.setReturnValue(CollectionBuilder.<Option>newBuilder(
                parentOption
        ).asList());

        mockController.replay();

        final CascadeOptionFunction function = createFunction(null, null);

        MessageSet result = function.validate(theUser, functionOperand, clause);

        assertFalse(result.hasAnyMessages());
        mockController.verify();
    }

    @Test
    public void testValidateNoneAsParentWithChild() throws Exception
    {
        FunctionOperand functionOperand = new FunctionOperand(functionName, "none", "child");
        TerminalClause clause = createClause(functionOperand);

        expectClauseResolvesToCustomField();

        replay();

        final CascadeOptionFunction function = createFunction(null, null);

        MessageSet result = function.validate(theUser, functionOperand, clause);

        assertTrue(result.getErrorMessages().contains(String.format("jira.jql.function.cascade.option.not.parent %s %s", functionName, "none")));
    }

    @Test
    public void testValidateNoneAsParentOnItsOwn() throws Exception
    {
        FunctionOperand functionOperand = new FunctionOperand(functionName, "none");
        TerminalClause clause = createClause(functionOperand);

        expectClauseResolvesToCustomField();

        replay();

        final CascadeOptionFunction function = createFunction(null, null);

        MessageSet result = function.validate(theUser, functionOperand, clause);

        assertFalse(result.hasAnyMessages());
    }

    @Test
    public void testValidateNoneValueParent() throws Exception
    {
        FunctionOperand functionOperand = new FunctionOperand(functionName, "\"none\"");
        TerminalClause clause = createClause(functionOperand);

        expectClauseResolvesToCustomField();

        final MockOption parentOption = createParentOption(100L);

        expect(jqlSelectOptionsUtil.getOptions(customField, createLiteral("none"), true))
                .andReturn(CollectionBuilder.<Option>newBuilder(parentOption).asList());

        replay();

        final CascadeOptionFunction function = createFunction(null, null);

        MessageSet result = function.validate(theUser, functionOperand, clause);

        assertFalse(result.hasAnyMessages());
    }

    @Test
    public void testValidateSingleParentAndNoneValueChild() throws Exception
    {
        FunctionOperand functionOperand = new FunctionOperand(functionName, "parent", "\"none\"");
        TerminalClause clause = createClause(functionOperand);

        expectClauseResolvesToCustomField();

        final MockOption parentOption = createParentOption(100L);
        final MockOption childOption = createChildOption(parentOption, 200L);
        parentOption.setChildOptions(CollectionBuilder.newBuilder(childOption).asList());

        jqlSelectOptionsUtil.getOptions(customField, createLiteral("parent"), true);
        mockController.setReturnValue(CollectionBuilder.<Option>newBuilder(
                parentOption
        ).asList());

        jqlSelectOptionsUtil.getOptions(customField, createLiteral("none"), true);
        mockController.setReturnValue(CollectionBuilder.<Option>newBuilder(
                childOption
        ).asList());

        mockController.replay();

        final CascadeOptionFunction function = createFunction(null, null);

        MessageSet result = function.validate(theUser, functionOperand, clause);

        assertFalse(result.hasAnyMessages());
        mockController.verify();
    }

    @Test
    public void testValidateParentAndChildButChildIsNotChildOfParent() throws Exception
    {
        FunctionOperand functionOperand = new FunctionOperand(functionName, "parent", "child");
        TerminalClause clause = createClause(functionOperand);

        expectClauseResolvesToCustomField();

        final MockOption parentOption = createParentOption(100L);
        final MockOption childOption = createChildOption(parentOption, 200L);

        jqlSelectOptionsUtil.getOptions(customField, createLiteral("parent"), true);
        mockController.setReturnValue(CollectionBuilder.<Option>newBuilder(
                parentOption
        ).asList());

        jqlSelectOptionsUtil.getOptions(customField, createLiteral("child"), true);
        mockController.setReturnValue(CollectionBuilder.<Option>newBuilder(
                childOption
        ).asList());

        mockController.replay();

        final CascadeOptionFunction function = createFunction(null, null);

        MessageSet result = function.validate(theUser, functionOperand, clause);

        assertTrue(result.getErrorMessages().contains(String.format("jira.jql.function.cascade.option.parent.children.doesnt.match %s %s %s", "child", "parent", functionName)));
        mockController.verify();
    }

    @Test
    public void testValidateSingleParentHappyPath() throws Exception
    {
        FunctionOperand functionOperand = new FunctionOperand(functionName, "parent");
        TerminalClause clause = createClause(functionOperand);

        expectClauseResolvesToCustomField();

        final MockOption parentOption = createParentOption(100L);

        jqlSelectOptionsUtil.getOptions(customField, createLiteral("parent"), true);
        mockController.setReturnValue(CollectionBuilder.<Option>newBuilder(
                parentOption
        ).asList());


        mockController.replay();

        final CascadeOptionFunction function = createFunction(null, null);

        MessageSet result = function.validate(theUser, functionOperand, clause);

        assertFalse(result.hasAnyMessages());
        mockController.verify();
    }

    @Test
    public void testGetValuesSingleParentAndChildHappyPath() throws Exception
    {
        FunctionOperand functionOperand = new FunctionOperand(functionName, "parent", "child");
        TerminalClause clause = createClause(functionOperand);

        expectClauseResolvesToCustomField();

        final MockOption parentOption = createParentOption(100L);
        final MockOption childOption = createChildOption(parentOption, 200L);

        parentOption.setChildOptions(Collections.<Option>singletonList(childOption));

        jqlSelectOptionsUtil.getOptions(customField, createLiteral("parent"), true);
        mockController.setReturnValue(CollectionBuilder.<Option>newBuilder(
                parentOption
        ).asList());

        jqlSelectOptionsUtil.getOptions(customField, createLiteral("child"), true);
        mockController.setReturnValue(CollectionBuilder.<Option>newBuilder(
                childOption
        ).asList());

        mockController.replay();
        final CascadeOptionFunction function = createFunction(Collections.<Option>singleton(childOption), Collections.<Option>emptySet());

        final List<QueryLiteral> values = function.getValues(queryCreationContext, functionOperand, clause);

        mockController.verify();
    }

    @Test
    public void testGetValuesSingleParentHappyPath() throws Exception
    {
        FunctionOperand functionOperand = new FunctionOperand(functionName, "parent");
        TerminalClause clause = createClause(functionOperand);

        expectClauseResolvesToCustomField();

        final MockOption parentOption = createParentOption(100L);

        jqlSelectOptionsUtil.getOptions(customField, createLiteral("parent"), true);
        mockController.setReturnValue(CollectionBuilder.<Option>newBuilder(
                parentOption
        ).asList());

        mockController.replay();
        final CascadeOptionFunction function = createFunction(Collections.<Option>singleton(parentOption), Collections.<Option>emptySet());

        function.getValues(queryCreationContext, functionOperand, clause);

        mockController.verify();
    }

    @Test
    public void testGetValuesNoneParentHappyPath() throws Exception
    {
        FunctionOperand functionOperand = new FunctionOperand(functionName, "none");
        TerminalClause clause = createClause(functionOperand);

        expectClauseResolvesToCustomField();

        replay();
        final CascadeOptionFunction function = createFunction(null, null);

        final List<QueryLiteral> result = function.getValues(queryCreationContext, functionOperand, clause);
        assertEquals(1, result.size());
        assertEquals(new QueryLiteral(), result.get(0));
    }

    @Test
    public void testGetValuesNoneParentWithChild() throws Exception
    {
        FunctionOperand functionOperand = new FunctionOperand(functionName, "none", "child");
        TerminalClause clause = createClause(functionOperand);

        expectClauseResolvesToCustomField();

        replay();
        final CascadeOptionFunction function = createFunction(null, null);

        final List<QueryLiteral> result = function.getValues(queryCreationContext, functionOperand, clause);
        assertEquals(0, result.size());
    }

    @Test
    public void testGetValuesSingleParentOverrideSecurity() throws Exception
    {
        queryCreationContext = new QueryCreationContextImpl(theUser, true);
        FunctionOperand functionOperand = new FunctionOperand(functionName, "parent");
        TerminalClause clause = createClause(functionOperand);

        final CustomFieldType selectCFType = mockController.getMock(CascadingSelectCFType.class);
        customField.getCustomFieldType();
        mockController.setReturnValue(selectCFType);

        searchHandlerManager.getFieldIds(clauseName);
        mockController.setReturnValue(Collections.singletonList(customFieldId));

        customFieldManager.getCustomFieldObject(customFieldId);
        mockController.setReturnValue(customField);

        final MockOption parentOption = createParentOption(100L);

        jqlSelectOptionsUtil.getOptions(customField, createLiteral("parent"), true);
        mockController.setReturnValue(CollectionBuilder.<Option>newBuilder(
                parentOption
        ).asList());

        mockController.replay();
        final CascadeOptionFunction function = createFunction(Collections.<Option>singleton(parentOption), Collections.<Option>emptySet());

        function.getValues(queryCreationContext, functionOperand, clause);

        mockController.verify();
    }

    @Test
    public void testGetValuesBadArgs() throws Exception
    {
        FunctionOperand functionOperand = new FunctionOperand(functionName, "parent", "child", "blah");
        TerminalClause clause = createClause(functionOperand);

        expectClauseResolvesToCustomField();

        mockController.replay();
        final CascadeOptionFunction function = createFunction(null, null);

        final List<QueryLiteral> result = function.getValues(queryCreationContext, functionOperand, clause);
        final List<QueryLiteral> expectedResult = CollectionBuilder.<QueryLiteral>newBuilder().asList();

        assertEquals(expectedResult, result);
        mockController.verify();
    }

    @Test
    public void testGetValuesNoArgs() throws Exception
    {
        FunctionOperand functionOperand = new FunctionOperand(functionName);
        TerminalClause clause = createClause(functionOperand);

        expectClauseResolvesToCustomField();

        mockController.replay();
        final CascadeOptionFunction function = createFunction(null, null);

        final List<QueryLiteral> result = function.getValues(queryCreationContext, functionOperand, clause);
        final List<QueryLiteral> expectedResult = CollectionBuilder.<QueryLiteral>newBuilder().asList();

        assertEquals(expectedResult, result);
        mockController.verify();
    }

    @Test
    public void testGetValuesNoFields() throws Exception
    {
        FunctionOperand functionOperand = new FunctionOperand(functionName, "parent", "child");
        TerminalClause clause = createClause(functionOperand);

        expectClauseResolvesToCustomField(TextCFType.class);

        mockController.replay();
        final CascadeOptionFunction function = createFunction(null, null);

        final List<QueryLiteral> result = function.getValues(queryCreationContext, functionOperand, clause);
        final List<QueryLiteral> expectedResult = CollectionBuilder.<QueryLiteral>newBuilder().asList();

        assertEquals(expectedResult, result);
        mockController.verify();
    }

    @Test
    public void testGetValuesSingleParentAndNoneChild() throws Exception
    {
        FunctionOperand functionOperand = new FunctionOperand(functionName, "parent", "none");
        TerminalClause clause = createClause(functionOperand);

        expectClauseResolvesToCustomField();

        final MockOption parentOption = createParentOption(100L);
        final MockOption childOption = createChildOption(parentOption, 200L);

        parentOption.setChildOptions(Collections.<Option>singletonList(childOption));

        jqlSelectOptionsUtil.getOptions(customField, createLiteral("parent"), true);
        mockController.setReturnValue(CollectionBuilder.<Option>newBuilder(
                parentOption
        ).asList());


        mockController.replay();
        final CascadeOptionFunction function = createFunction(Collections.<Option>singleton(parentOption), Collections.<Option>singleton(childOption));

        function.getValues(queryCreationContext, functionOperand, clause);
        mockController.verify();
    }

    @Test
    public void testGetValuesSingleParentAndNoneValueChild() throws Exception
    {
        FunctionOperand functionOperand = new FunctionOperand(functionName, "parent", "\"none\"");
        TerminalClause clause = createClause(functionOperand);

        expectClauseResolvesToCustomField();

        final MockOption parentOption = createParentOption(100L);
        final MockOption childOption = createChildOption(parentOption, 200L);

        parentOption.setChildOptions(Collections.<Option>singletonList(childOption));

        jqlSelectOptionsUtil.getOptions(customField, createLiteral("parent"), true);
        mockController.setReturnValue(CollectionBuilder.<Option>newBuilder(
                parentOption
        ).asList());

        jqlSelectOptionsUtil.getOptions(customField, createLiteral("none"), true);
        mockController.setReturnValue(CollectionBuilder.<Option>newBuilder(
                childOption
        ).asList());

        mockController.replay();
        final CascadeOptionFunction function = createFunction(Collections.<Option>singleton(childOption), Collections.<Option>emptySet());

        function.getValues(queryCreationContext, functionOperand, clause);
        mockController.verify();
    }

    @Test
    public void testGetValuesNoneValueParent() throws Exception
    {
        FunctionOperand functionOperand = new FunctionOperand(functionName, "\"none\"");
        TerminalClause clause = createClause(functionOperand);

        expectClauseResolvesToCustomField();

        final MockOption parentOption = createParentOption(100L);

        expect(jqlSelectOptionsUtil.getOptions(customField, createLiteral("none"), true))
                .andReturn(CollectionBuilder.<Option>newBuilder(parentOption).asList());

        replay();
        final CascadeOptionFunction function = createFunction(Collections.<Option>singleton(parentOption), Collections.<Option>emptySet());

        function.getValues(queryCreationContext, functionOperand, clause);
    }

    @Test
    public void testGetMinimumNumberOfExpectedArguments() throws Exception
    {
        mockController.replay();
        final CascadeOptionFunction function = createFunction(null, null);

        assertEquals(1, function.getMinimumNumberOfExpectedArguments());
        mockController.verify();
    }

    @Test
    public void testDataType() throws Exception
    {
        mockController.replay();
        final CascadeOptionFunction function = createFunction(null, null);
        assertEquals(JiraDataTypes.CASCADING_OPTION, function.getDataType());
    }

    private void expectClauseResolvesToCustomField(Class<? extends CustomFieldType> type)
    {
        final CustomFieldType selectCFType = mockController.getMock(type);
        customField.getCustomFieldType();
        mockController.setReturnValue(selectCFType);

        searchHandlerManager.getFieldIds((User) theUser, clauseName);
        mockController.setReturnValue(Collections.singletonList(customFieldId));

        customFieldManager.getCustomFieldObject(customFieldId);
        mockController.setReturnValue(customField);
    }

    private void expectClauseResolvesToCustomField()
    {
        expectClauseResolvesToCustomField(CascadingSelectCFType.class);
    }

    private CascadeOptionFunction createFunction(final Collection<Option> expectedPositive, final Collection<Option> expectedNegative)
    {
        return new CascadeOptionFunction(jqlSelectOptionsUtil, searchHandlerManager, customFieldManager, jqlCascadingSelectLiteralUtil)
        {
            @Override
            protected I18nHelper getI18n()
            {
                return new MockI18nHelper();
            }

            @Override
            public String getFunctionName()
            {
                return functionName;
            }

            public JiraDataType getDataType()
            {
                return JiraDataTypes.CASCADING_OPTION;
            }

            @Override
            List<QueryLiteral> createLiterals(final Operand operand, final Collection<Option> positiveOptions, final Collection<Option> negativeOptions)
            {
                if (expectedPositive != null)
                {
                    assertEquals(expectedPositive, positiveOptions);
                }

                if (expectedNegative != null)
                {
                    assertEquals(expectedNegative, negativeOptions);
                }

                return Collections.emptyList();
            }
        };
    }

    private static MockOption createParentOption(final long optionId)
    {
        return new MockOption(null, Collections.emptyList(), null, null, null, optionId);
    }

    private static MockOption createChildOption(final MockOption parentOption, final long optionId)
    {
        return new MockOption(parentOption, null, null, null, null, optionId);
    }

    private TerminalClauseImpl createClause(final FunctionOperand functionOperand)
    {
        return new TerminalClauseImpl(clauseName, Operator.IN, functionOperand);
    }
}
