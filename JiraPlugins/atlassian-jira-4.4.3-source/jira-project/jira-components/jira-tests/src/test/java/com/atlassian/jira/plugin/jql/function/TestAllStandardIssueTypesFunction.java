package com.atlassian.jira.plugin.jql.function;

import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.issue.issuetype.MockIssueType;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.plugin.jql.operand.MockJqlFunctionModuleDescriptor;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @since v4.0
 */
public class TestAllStandardIssueTypesFunction extends MockControllerTestCase
{
    private JqlFunctionModuleDescriptor moduleDescriptor;
    private TerminalClause terminalClause = null;

    @Before
    public void setUp() throws Exception
    {
        moduleDescriptor = MockJqlFunctionModuleDescriptor.create("standardIssueTypes", true);
    }

    @Test
    public void testDataType() throws Exception
    {
        AllStandardIssueTypesFunction handler = mockController.instantiate(AllStandardIssueTypesFunction.class);
        assertEquals(JiraDataTypes.ISSUE_TYPE, handler.getDataType());        
    }

    @Test
    public void testBadConstructor() throws Exception
    {
        final ConstantsManager constantsManager = mockController.getMock(ConstantsManager.class);
        final SubTaskManager subTaskManager = mockController.getMock(SubTaskManager.class);

        mockController.replay();

        try
        {
            new AllStandardIssueTypesFunction(null, subTaskManager);
            fail("Exception expected");
        }
        catch (IllegalArgumentException expected) {}

        try
        {
            new AllStandardIssueTypesFunction(constantsManager, null);
            fail("Expected exception");
        }
        catch (IllegalArgumentException expected) {}
    }

    @Test
    public void testGetValues()
    {
        final ConstantsManager constantsManager = mockController.getMock(ConstantsManager.class);
        constantsManager.getRegularIssueTypeObjects();
        mockController.setReturnValue(CollectionBuilder.newBuilder(new MockIssueType("1", null), new MockIssueType("2", null), new MockIssueType("nlah", null)).asList());

        AllStandardIssueTypesFunction handler = mockController.instantiate(AllStandardIssueTypesFunction.class);
        final FunctionOperand operand = new FunctionOperand("blarg!");
        final List<QueryLiteral> queryLiteralList = handler.getValues(null, operand, terminalClause);
        assertEquals(3, queryLiteralList.size());
        assertEquals("1", queryLiteralList.get(0).getStringValue());
        assertEquals("2", queryLiteralList.get(1).getStringValue());
        assertEquals("nlah", queryLiteralList.get(2).getStringValue());

        assertEquals(operand, queryLiteralList.get(0).getSourceOperand());
        assertEquals(operand, queryLiteralList.get(1).getSourceOperand());
        assertEquals(operand, queryLiteralList.get(2).getSourceOperand());


        mockController.verify();
    }

    @Test
    public void testValidateWithArgs()
    {
        final ConstantsManager constantsManager = mockController.getMock(ConstantsManager.class);

        final SubTaskManager subTaskManager = mockController.getMock(SubTaskManager.class);
        mockController.replay();

        AllStandardIssueTypesFunction handler = new AllStandardIssueTypesFunction(constantsManager, subTaskManager)
        {
            @Override
            protected I18nHelper getI18n()
            {
                return new MockI18nBean();
            }
        };

        FunctionOperand operand = new FunctionOperand("testfunc", CollectionBuilder.newBuilder("arg").asList());
        MessageSet messageSet = handler.validate(null, operand, terminalClause);
        assertTrue(messageSet.hasAnyErrors());

        mockController.verify();
    }

    @Test
    public void testValidateWithNoArgsSubTasksDisabled()
    {
        final ConstantsManager constantsManager = mockController.getMock(ConstantsManager.class);

        final SubTaskManager subTaskManager = mockController.getMock(SubTaskManager.class);
        subTaskManager.isSubTasksEnabled();
        mockController.setReturnValue(false);
        mockController.replay();

        AllStandardIssueTypesFunction handler = new AllStandardIssueTypesFunction(constantsManager, subTaskManager)
        {
            @Override
            protected I18nHelper getI18n()
            {
                return new MockI18nBean();
            }
        };

        handler.init(moduleDescriptor);

        FunctionOperand operand = new FunctionOperand("testfunc", Collections.<String>emptyList());
        MessageSet messageSet = handler.validate(null, operand, terminalClause);
        assertTrue(messageSet.hasAnyErrors());

        mockController.verify();
    }

    @Test
    public void testValidateWithNoArgsSubTasksEnabled()
    {
        final ConstantsManager constantsManager = mockController.getMock(ConstantsManager.class);

        final SubTaskManager subTaskManager = mockController.getMock(SubTaskManager.class);
        subTaskManager.isSubTasksEnabled();
        mockController.setReturnValue(true);
        mockController.replay();

        AllStandardIssueTypesFunction handler = new AllStandardIssueTypesFunction(constantsManager, subTaskManager)
        {
            @Override
            protected I18nHelper getI18n()
            {
                return new MockI18nBean();
            }
        };

        FunctionOperand operand = new FunctionOperand("testfunc", Collections.<String>emptyList());
        MessageSet messageSet = handler.validate(null, operand, terminalClause);
        assertFalse(messageSet.hasAnyErrors());

        mockController.verify();
    }

    @Test
    public void testGetMinimumNumberOfExpectedArguments()
    {
        final ConstantsManager constantsManager = mockController.getMock(ConstantsManager.class);

        final SubTaskManager subTaskManager = mockController.getMock(SubTaskManager.class);
        mockController.replay();

        AllStandardIssueTypesFunction handler = new AllStandardIssueTypesFunction(constantsManager, subTaskManager);

        assertEquals(0, handler.getMinimumNumberOfExpectedArguments());
        mockController.verify();
    }

}
