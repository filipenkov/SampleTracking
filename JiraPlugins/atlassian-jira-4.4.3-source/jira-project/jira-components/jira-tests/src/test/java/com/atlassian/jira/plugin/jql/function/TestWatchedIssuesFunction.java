package com.atlassian.jira.plugin.jql.function;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.issue.watchers.WatchedIssuesAccessor;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.query.QueryCreationContextImpl;
import com.atlassian.jira.mock.plugin.jql.operand.MockJqlFunctionModuleDescriptor;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import mock.user.MockOSUser;

import java.util.Collections;
import java.util.List;

/**
 * @since v4.0
 */
public class TestWatchedIssuesFunction extends MockControllerTestCase
{
    private User theUser;
    private QueryCreationContext queryCreationContext;
    private final TerminalClause terminalClause = null;

    @Before
    public void setUp() throws Exception
    {
        theUser = new MockUser("fred");
        queryCreationContext = new QueryCreationContextImpl(theUser);
    }

    @Test
    public void testDataType() throws Exception
    {
        final WatchedIssuesFunction watchedIssuesFunction = mockController.instantiate(WatchedIssuesFunction.class);
        assertEquals(JiraDataTypes.ISSUE, watchedIssuesFunction.getDataType());
    }

    @Test
    public void testValidateWatchingDisabled() throws Exception
    {
        final WatchedIssuesAccessor watchedIssues = mockController.getMock(WatchedIssuesAccessor.class);
        watchedIssues.isWatchingEnabled();
        mockController.setReturnValue(false);

        final WatchedIssuesFunction watchedIssuesFunction = mockController.instantiate(WatchedIssuesFunction.class);
        watchedIssuesFunction.init(MockJqlFunctionModuleDescriptor.create("watchedIssues", true));

        final MessageSet messageSet = watchedIssuesFunction.validate(null, new FunctionOperand("watchedIssues"), terminalClause);
        assertTrue(messageSet.hasAnyErrors());
        assertEquals(1, messageSet.getErrorMessages().size());
        assertEquals("Function 'watchedIssues' cannot be called as watching issues is currently disabled.",
            messageSet.getErrorMessages().iterator().next());
    }

    @Test
    public void testValidateWrongArgs() throws Exception
    {
        final WatchedIssuesAccessor watchedIssues = mockController.getMock(WatchedIssuesAccessor.class);
        watchedIssues.isWatchingEnabled();
        mockController.setReturnValue(true);

        final WatchedIssuesFunction watchedIssuesFunction = mockController.instantiate(WatchedIssuesFunction.class);
        watchedIssuesFunction.init(MockJqlFunctionModuleDescriptor.create("watchedIssues", true));

        final MessageSet messageSet = watchedIssuesFunction.validate(new MockOSUser("bob"), new FunctionOperand(
            "watchedIssues", "badArg"), terminalClause);
        assertTrue(messageSet.hasAnyErrors());
        assertEquals(1, messageSet.getErrorMessages().size());
        assertEquals("Function 'watchedIssues' expected '0' arguments but received '1'.", messageSet.getErrorMessages().iterator().next());
    }

    @Test
    public void testValidateAnonymous() throws Exception
    {
        final WatchedIssuesAccessor watchedIssues = mockController.getMock(WatchedIssuesAccessor.class);
        watchedIssues.isWatchingEnabled();
        mockController.setReturnValue(true);

        final WatchedIssuesFunction watchedIssuesFunction = mockController.instantiate(WatchedIssuesFunction.class);
        watchedIssuesFunction.init(MockJqlFunctionModuleDescriptor.create("watchedIssues", true));

        final MessageSet messageSet = watchedIssuesFunction.validate(null, new FunctionOperand("watchedIssues"), terminalClause);
        assertTrue(messageSet.hasAnyErrors());
        assertEquals(1, messageSet.getErrorMessages().size());
        assertEquals("Function 'watchedIssues' cannot be called as anonymous user.", messageSet.getErrorMessages().iterator().next());
    }

    @Test
    public void testValidateHappyPath() throws Exception
    {
        final WatchedIssuesAccessor watchedIssues = mockController.getMock(WatchedIssuesAccessor.class);
        watchedIssues.isWatchingEnabled();
        mockController.setReturnValue(true);

        final WatchedIssuesFunction watchedIssuesFunction = mockController.instantiate(WatchedIssuesFunction.class);
        watchedIssuesFunction.init(MockJqlFunctionModuleDescriptor.create("watchedIssues", true));

        final MessageSet messageSet = watchedIssuesFunction.validate(new MockOSUser("bob"),
            new FunctionOperand("watchedIssues"), terminalClause);
        assertFalse(messageSet.hasAnyErrors());
    }

    @Test
    public void testGetWatchedIssuesDoNotOverrideSecurity() throws Exception
    {
        final Iterable<Long> watches = Collections.singletonList(55L);

        final WatchedIssuesAccessor watchedIssues = mockController.getMock(WatchedIssuesAccessor.class);
        watchedIssues.getWatchedIssueIds(theUser, theUser, WatchedIssuesAccessor.Security.RESPECT);
        mockController.setReturnValue(watches);

        final WatchedIssuesFunction watchedIssuesFunction = mockController.instantiate(WatchedIssuesFunction.class);

        final Iterable<Long> list = watchedIssuesFunction.getWatchedIssues(theUser, false);
        assertEquals(watches, list);
    }

    @Test
    public void testGetWatchedIssuesOverrideSecurity() throws Exception
    {
        final Iterable<Long> watches = Collections.singletonList(55L);

        final WatchedIssuesAccessor watchedIssues = mockController.getMock(WatchedIssuesAccessor.class);
        watchedIssues.getWatchedIssueIds(theUser, theUser, WatchedIssuesAccessor.Security.OVERRIDE);
        mockController.setReturnValue(watches);

        final WatchedIssuesFunction watchedIssuesFunction = mockController.instantiate(WatchedIssuesFunction.class);

        final Iterable<Long> list = watchedIssuesFunction.getWatchedIssues(theUser, true);
        assertEquals(watches, list);
    }

    @Test
    public void testGetValuesHappyPath() throws Exception
    {
        final List<Long> watches = Collections.singletonList(55L);

        final WatchedIssuesAccessor watchedIssues = mockController.getMock(WatchedIssuesAccessor.class);
        watchedIssues.getWatchedIssueIds(theUser, theUser, WatchedIssuesAccessor.Security.RESPECT);
        mockController.setReturnValue(watches);

        final WatchedIssuesFunction watchedIssuesFunction = mockController.instantiate(WatchedIssuesFunction.class);

        final FunctionOperand operand = new FunctionOperand("watchedIssues");
        final List<QueryLiteral> list = watchedIssuesFunction.getValues(queryCreationContext, operand, terminalClause);
        assertEquals(1, list.size());
        assertEquals(new Long(55), list.get(0).getLongValue());
        assertEquals(operand, list.get(0).getSourceOperand());
    }

    @Test
    public void testGetValuesAnonymous() throws Exception
    {
        final WatchedIssuesFunction watchedIssuesFunction = mockController.instantiate(WatchedIssuesFunction.class);

        final List<QueryLiteral> list = watchedIssuesFunction.getValues(new QueryCreationContextImpl(null), new FunctionOperand("watchedIssues"),
            terminalClause);
        assertTrue(list.isEmpty());
    }

    @Test
    public void testGetMinimumNumberOfExpectedArguments() throws Exception
    {
        final WatchedIssuesFunction watchedIssuesFunction = mockController.instantiate(WatchedIssuesFunction.class);

        assertEquals(0, watchedIssuesFunction.getMinimumNumberOfExpectedArguments());
    }
}
