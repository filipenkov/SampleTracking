package com.atlassian.jira.jql.context;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.issuetype.MockIssueType;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.util.JqlIssueSupport;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import java.util.Arrays;
import java.util.Collections;

/**
 * @since v4.0
 */
public class TestIssueIdClauseContextFactory extends MockControllerTestCase
{
    private User theUser = null;

    @Test
    public void testGetClauseContextRelational() throws Exception
    {
        final SingleValueOperand operand = new SingleValueOperand("blah");
        final TerminalClauseImpl clause = new TerminalClauseImpl("blah", Operator.GREATER_THAN, operand);

        final MockIssue issue1 = new MockIssue(1L);
        issue1.setProjectObject(new MockProject(222L));
        final MockIssue issue2 = new MockIssue(12345L);
        issue2.setProjectObject(new MockProject(888L));

        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        final JqlIssueSupport jqlIssueSupport = mockController.getMock(JqlIssueSupport.class);

        expect(jqlOperandResolver.getValues(theUser, operand, clause)).andReturn(Arrays.asList(createLiteral("key-10"), createLiteral(12345L)));

        expect(jqlIssueSupport.getIssues("key-10", null)).andReturn(Collections.<Issue>singletonList(issue1));
        expect(jqlIssueSupport.getIssue(12345L, null)).andReturn(issue2);

        mockController.replay();

        IssueIdClauseContextFactory factory = new IssueIdClauseContextFactory(jqlIssueSupport, jqlOperandResolver, true);
        final ClauseContext result = factory.getClauseContext(theUser, clause);
        ClauseContext expectedResult = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(
                new ProjectIssueTypeContextImpl(new ProjectContextImpl(222L), AllIssueTypesContext.INSTANCE),
                new ProjectIssueTypeContextImpl(new ProjectContextImpl(888L), AllIssueTypesContext.INSTANCE)
            ).asSet());

        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testGetClauseContextRelationalNotAllowed() throws Exception
    {
        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        final JqlIssueSupport jqlIssueSupport = mockController.getMock(JqlIssueSupport.class);

        ClauseContext expectedResult = ClauseContextImpl.createGlobalClauseContext();

        mockController.replay();

        for (Operator operator : OperatorClasses.RELATIONAL_ONLY_OPERATORS)
        {
            IssueIdClauseContextFactory factory = new IssueIdClauseContextFactory(jqlIssueSupport, jqlOperandResolver, false);
            final ClauseContext result = factory.getClauseContext(theUser, new TerminalClauseImpl("blah", operator, "blah"));

            assertEquals(expectedResult, result);
        }

        mockController.verify();
    }

    @Test
    public void testGetClauseContext() throws Exception
    {
        final SingleValueOperand operand = new SingleValueOperand("blah");
        final TerminalClauseImpl clause = new TerminalClauseImpl("blah", Operator.EQUALS, operand);

        final MockProject mockProject1 = new MockProject(10L, "test");
        final MockProject mockProject2 = new MockProject(11L, "test");
        final MockIssueType mockIssueType1 = new MockIssueType("10", "test");
        final MockIssueType mockIssueType2 = new MockIssueType("11", "test");

        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        final JqlIssueSupport jqlIssueSupport = mockController.getMock(JqlIssueSupport.class);

        expect(jqlOperandResolver.getValues(theUser, operand, clause)).andReturn(Arrays.asList(createLiteral("key"), createLiteral(10L)));

        Issue issue1 = mockController.getMock(Issue.class);
        expect(issue1.getProjectObject()).andReturn(mockProject1);
        expect(issue1.getIssueTypeObject()).andReturn(mockIssueType1);

        Issue issue2 = mockController.getMock(Issue.class);
        expect(issue2.getProjectObject()).andReturn(mockProject2);
        expect(issue2.getIssueTypeObject()).andReturn(mockIssueType2);

        expect(jqlIssueSupport.getIssues("key", null)).andReturn(Collections.singletonList(issue1));
        expect(jqlIssueSupport.getIssue(10L, null)).andReturn(issue2);

        mockController.replay();

        IssueIdClauseContextFactory factory = new IssueIdClauseContextFactory(jqlIssueSupport, jqlOperandResolver, true);
        final ClauseContext result = factory.getClauseContext(theUser, clause);
        ClauseContext expectedResult = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(
                new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L), new IssueTypeContextImpl("10")),
                new ProjectIssueTypeContextImpl(new ProjectContextImpl(11L), new IssueTypeContextImpl("11"))
            ).asSet());

        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testGetClauseContextIneqaulity() throws Exception
    {
        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        final JqlIssueSupport jqlIssueSupport = mockController.getMock(JqlIssueSupport.class);

        ClauseContext expectedResult = ClauseContextImpl.createGlobalClauseContext();

        mockController.replay();

        IssueIdClauseContextFactory factory = new IssueIdClauseContextFactory(jqlIssueSupport, jqlOperandResolver, true);
        final ClauseContext result = factory.getClauseContext(theUser, new TerminalClauseImpl("blah", Operator.NOT_EQUALS, "blah"));

        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testGetClauseContextRelationalBadId() throws Exception
    {
        final SingleValueOperand singleValueOperand = new SingleValueOperand(10L);
        final TerminalClauseImpl clause = new TerminalClauseImpl("blah", Operator.GREATER_THAN, singleValueOperand);

        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        final JqlIssueSupport jqlIssueSupport = mockController.getMock(JqlIssueSupport.class);

        expect(jqlOperandResolver.getValues(theUser, singleValueOperand, clause)).andReturn(Arrays.asList(createLiteral(10L)));
        expect(jqlIssueSupport.getIssue(10L, null)).andReturn(null);

        mockController.replay();

        IssueIdClauseContextFactory factory = new IssueIdClauseContextFactory(jqlIssueSupport, jqlOperandResolver, true);
        final ClauseContext result = factory.getClauseContext(theUser, clause);
        ClauseContext expectedResult = ClauseContextImpl.createGlobalClauseContext();

        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testGetClauseContextRelationalBadKey() throws Exception
    {
        final SingleValueOperand singleValueOperand = new SingleValueOperand(10L);
        final TerminalClauseImpl clause = new TerminalClauseImpl("blah", Operator.GREATER_THAN, singleValueOperand);

        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        final JqlIssueSupport jqlIssueSupport = mockController.getMock(JqlIssueSupport.class);

        expect(jqlOperandResolver.getValues(theUser, singleValueOperand, clause)).andReturn(Arrays.asList(createLiteral("badkey")));
        expect(jqlIssueSupport.getIssues("badkey", null)).andReturn(Collections.<Issue>emptyList());

        mockController.replay();

        IssueIdClauseContextFactory factory = new IssueIdClauseContextFactory(jqlIssueSupport, jqlOperandResolver, true);
        final ClauseContext result = factory.getClauseContext(theUser, clause);
        ClauseContext expectedResult = ClauseContextImpl.createGlobalClauseContext();

        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testGetClauseContextSingleEmptyLiteral() throws Exception
    {
        final Operand operand = EmptyOperand.EMPTY;
        final TerminalClauseImpl clause = new TerminalClauseImpl("blah", Operator.IS, operand);
        final IssueIdClauseContextFactory factory = new IssueIdClauseContextFactory(getMock(JqlIssueSupport.class), getMock(JqlOperandResolver.class), false);

        mockController.replay();

        final ClauseContext result = factory.getClauseContext(theUser, clause);
        final ClauseContext expectedResult = ClauseContextImpl.createGlobalClauseContext();

        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testGetClauseContextSingleInvalidEmptyLiteral() throws Exception
    {
        final TerminalClauseImpl clause = new TerminalClauseImpl("blah", Operator.IS, new SingleValueOperand("illegal"));
        final IssueIdClauseContextFactory factory = new IssueIdClauseContextFactory(getMock(JqlIssueSupport.class), getMock(JqlOperandResolver.class), false);

        mockController.replay();

        final ClauseContext result = factory.getClauseContext(theUser, clause);
        final ClauseContext expectedResult = ClauseContextImpl.createGlobalClauseContext();

        assertEquals(expectedResult, result);

        mockController.verify();
    }
}
