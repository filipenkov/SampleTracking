package com.atlassian.jira.jql.permission;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.managers.SearchHandlerManager;
import com.atlassian.jira.jql.ClauseHandler;
import com.atlassian.jira.jql.MockClauseHandler;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.NotClause;
import com.atlassian.query.clause.OrClause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;
import org.easymock.classextension.EasyMock;

import java.util.Collections;
import java.util.List;

/**
 * Test for {@link com.atlassian.jira.jql.permission.ClauseSanitisingVisitor}.
 *
 * @since v4.0
 */
public class TestClauseSanitisingVisitor extends MockControllerTestCase
{
    private User theUser;

    @Before
    public void setUp() throws Exception
    {
        theUser = new MockUser("fred");
    }

    @Test
    public void testConstructor() throws Exception
    {
        final SearchHandlerManager searchHandlerManager = mockController.getMock(SearchHandlerManager.class);
        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);

        mockController.replay();

        try
        {
            new ClauseSanitisingVisitor(null, jqlOperandResolver, null);
            fail("Expected exception");
        }
        catch (IllegalArgumentException expected) {}

        try
        {
            new ClauseSanitisingVisitor(searchHandlerManager, null, null);
            fail("Expected exception");
        }
        catch (IllegalArgumentException expected) {}
    }

    @Test
    public void testAndClause() throws Exception
    {
        final SearchHandlerManager manager = mockController.getMock(SearchHandlerManager.class);
        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);

        mockController.replay();

        final Clause expectedClause = new AndClause(new TerminalClauseImpl("field", Operator.EQUALS, "Value"));
        ClauseSanitisingVisitor visitor = new ClauseSanitisingVisitor(manager, jqlOperandResolver, null)
        {
            @Override
            List<Clause> sanitiseChildren(final Clause parentClause)
            {
                assertEquals(expectedClause, parentClause);
                return Collections.singletonList(parentClause);
            }
        };

        final Clause sanitisedClause = expectedClause.accept(visitor);

        assertEquals(new AndClause(expectedClause), sanitisedClause);

        mockController.verify();
    }

    @Test
    public void testOrClause() throws Exception
    {
        final SearchHandlerManager manager = mockController.getMock(SearchHandlerManager.class);
        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);

        mockController.replay();

        final Clause expectedClause = new OrClause(new TerminalClauseImpl("field", Operator.EQUALS, "Value"));
        ClauseSanitisingVisitor visitor = new ClauseSanitisingVisitor(manager, jqlOperandResolver, null)
        {
            @Override
            List<Clause> sanitiseChildren(final Clause parentClause)
            {
                assertEquals(expectedClause, parentClause);
                return Collections.singletonList(parentClause);
            }
        };

        final Clause sanitisedClause = expectedClause.accept(visitor);

        assertEquals(new OrClause(expectedClause), sanitisedClause);

        mockController.verify();
    }

    @Test
    public void testNotClause() throws Exception
    {
        final SearchHandlerManager manager = mockController.getMock(SearchHandlerManager.class);
        final Clause notChildClause = mockController.getMock(Clause.class);

        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);

        ClauseSanitisingVisitor visitor = new ClauseSanitisingVisitor(manager, jqlOperandResolver, null);
        EasyMock.expect(notChildClause.accept(visitor)).andReturn(notChildClause);

        mockController.replay();

        NotClause notClause = new NotClause(notChildClause);

        final Clause sanitisedClause = notClause.accept(visitor);
        assertEquals(notClause, sanitisedClause);

        mockController.verify();
    }

    @Test
    public void testSanitiseChildren() throws Exception
    {
        final SearchHandlerManager manager = mockController.getMock(SearchHandlerManager.class);
        final Clause childClause = mockController.getMock(Clause.class);

        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);

        ClauseSanitisingVisitor visitor = new ClauseSanitisingVisitor(manager, jqlOperandResolver, null);
        EasyMock.expect(childClause.accept(visitor)).andReturn(childClause);

        mockController.replay();

        AndClause andClause = new AndClause(childClause);

        final List<Clause> sanitisedChildren = visitor.sanitiseChildren(andClause);
        assertEquals(1, sanitisedChildren.size());
        assertEquals(childClause, sanitisedChildren.get(0));

        mockController.verify();
    }

    @Test
    public void testSanitiseOperandDoesNotChange() throws Exception
    {
        final SearchHandlerManager manager = mockController.getMock(SearchHandlerManager.class);

        final SingleValueOperand inputOperand = new SingleValueOperand("HSP");
        final TerminalClause clause = new TerminalClauseImpl("project", Operator.EQUALS, inputOperand);

        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);

        final DefaultOperandSanitisingVisitor operandVisitor = new DefaultOperandSanitisingVisitor(jqlOperandResolver, theUser)
        {
            @Override
            public Operand visit(final SingleValueOperand singleValueOperand)
            {
                assertEquals(inputOperand, singleValueOperand);
                return singleValueOperand;
            }
        };

        final ClauseSanitisingVisitor visitor = new ClauseSanitisingVisitor(manager, jqlOperandResolver, theUser)
        {
            @Override
            DefaultOperandSanitisingVisitor createOperandVisitor(final User user)
            {
                return operandVisitor;
            }
        };

        mockController.replay();

        final Clause result = visitor.sanitiseOperands(clause);
        assertSame(result, clause);

        mockController.verify();
    }

    @Test
    public void testSanitiseOperandChangesToSingle() throws Exception
    {
        final SearchHandlerManager manager = mockController.getMock(SearchHandlerManager.class);

        final SingleValueOperand inputOperand = new SingleValueOperand("HSP");
        final SingleValueOperand outputOperand = new SingleValueOperand(10000L);

        final TerminalClause clause = new TerminalClauseImpl("project", Operator.EQUALS, inputOperand);
        final TerminalClause expectedClause = new TerminalClauseImpl("project", Operator.EQUALS, outputOperand);

        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);

        final DefaultOperandSanitisingVisitor operandVisitor = new DefaultOperandSanitisingVisitor(jqlOperandResolver, theUser)
        {
            @Override
            public Operand visit(final SingleValueOperand singleValueOperand)
            {
                assertEquals(inputOperand, singleValueOperand);
                return outputOperand;
            }
        };

        final ClauseSanitisingVisitor visitor = new ClauseSanitisingVisitor(manager, jqlOperandResolver, theUser)
        {
            @Override
            DefaultOperandSanitisingVisitor createOperandVisitor(final User user)
            {
                return operandVisitor;
            }
        };

        mockController.replay();

        final Clause result = visitor.sanitiseOperands(clause);
        assertNotSame(result, clause);
        assertEquals(result, expectedClause);

        mockController.verify();
    }

    @Test
    public void testSanitiseOperandChangesToMulti() throws Exception
    {
        final SearchHandlerManager manager = mockController.getMock(SearchHandlerManager.class);

        final SingleValueOperand inputOperand = new SingleValueOperand("HSP");
        final SingleValueOperand outputOperand = new SingleValueOperand(10000L);

        final TerminalClause clause = new TerminalClauseImpl("project", Operator.IN, new MultiValueOperand(inputOperand));
        final TerminalClause expectedClause = new TerminalClauseImpl("project", Operator.IN, new MultiValueOperand(outputOperand));

        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);

        final DefaultOperandSanitisingVisitor operandVisitor = new DefaultOperandSanitisingVisitor(jqlOperandResolver, theUser)
        {
            @Override
            public Operand visit(final SingleValueOperand singleValueOperand)
            {
                assertEquals(inputOperand, singleValueOperand);
                return outputOperand;
            }
        };

        final ClauseSanitisingVisitor visitor = new ClauseSanitisingVisitor(manager, jqlOperandResolver, theUser)
        {
            @Override
            DefaultOperandSanitisingVisitor createOperandVisitor(final User user)
            {
                return operandVisitor;
            }
        };

        mockController.replay();

        final Clause result = visitor.sanitiseOperands(clause);
        assertNotSame(result, clause);
        assertEquals(result, expectedClause);

        mockController.verify();
    }

    @Test
    public void testTerminalClauseNoSearchHandlers() throws Exception
    {
        final TerminalClause input = new TerminalClauseImpl("field", Operator.EQUALS, "Value");
        final SearchHandlerManager manager = mockController.getMock(SearchHandlerManager.class);
        EasyMock.expect(manager.getClauseHandler((User) null, "field")).andReturn(Collections.<ClauseHandler>emptyList());

        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);

        mockController.replay();

        ClauseSanitisingVisitor visitor = new ClauseSanitisingVisitor(manager, jqlOperandResolver, null)
        {
            @Override
            TerminalClause sanitiseOperands(final TerminalClause clause)
            {
                return clause;
            }
        };

        final Clause clause = input.accept(visitor);
        assertSame(clause, input);

        mockController.verify();
    }

    @Test
    public void testTerminalClauseOneSearchHandler() throws Exception
    {
        final TerminalClause input = new TerminalClauseImpl("field", Operator.EQUALS, "Value");
        final SearchHandlerManager manager = mockController.getMock(SearchHandlerManager.class);

        final ClausePermissionHandler permissionHandler = mockController.getMock(ClausePermissionHandler.class);
        EasyMock.expect(permissionHandler.sanitise(null, input)).andReturn(input);

        final ClauseHandler handler = new MockClauseHandler(null, null, permissionHandler, null);
        EasyMock.expect(manager.getClauseHandler((User) null, "field")).andReturn(Collections.singletonList(handler));

        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);

        mockController.replay();

        ClauseSanitisingVisitor visitor = new ClauseSanitisingVisitor(manager, jqlOperandResolver, null)
        {
            @Override
            TerminalClause sanitiseOperands(final TerminalClause clause)
            {
                return clause;
            }
        };

        final Clause clause = input.accept(visitor);
        assertSame(clause, input);

        mockController.verify();
    }

    @Test
    public void testTerminalClauseTwoSearchHandlersSame() throws Exception
    {
        final TerminalClause input1 = new TerminalClauseImpl("field", Operator.EQUALS, "Value");
        final TerminalClause input2 = new TerminalClauseImpl("field", Operator.EQUALS, "Value");
        final SearchHandlerManager manager = mockController.getMock(SearchHandlerManager.class);

        final ClausePermissionHandler permissionHandler = mockController.getMock(ClausePermissionHandler.class);
        EasyMock.expect(permissionHandler.sanitise(null, input1)).andReturn(input1);
        EasyMock.expect(permissionHandler.sanitise(null, input1)).andReturn(input2);

        final ClauseHandler handler1 = new MockClauseHandler(null, null, permissionHandler, null);
        final ClauseHandler handler2 = new MockClauseHandler(null, null, permissionHandler, null);

        EasyMock.expect(manager.getClauseHandler((User) null, "field")).andReturn(CollectionBuilder.newBuilder(handler1, handler2).asList());

        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);

        mockController.replay();

        ClauseSanitisingVisitor visitor = new ClauseSanitisingVisitor(manager, jqlOperandResolver, null)
        {
            @Override
            TerminalClause sanitiseOperands(final TerminalClause clause)
            {
                return clause;
            }
        };

        final Clause clause = input1.accept(visitor);
        assertEquals(input1, clause);

        mockController.verify();
    }

    @Test
    public void testTerminalClauseTwoSearchHandlersDifferent() throws Exception
    {
        final TerminalClause input1 = new TerminalClauseImpl("field", Operator.EQUALS, "Value1");
        final TerminalClause input2 = new TerminalClauseImpl("field", Operator.EQUALS, "Value2");
        final SearchHandlerManager manager = mockController.getMock(SearchHandlerManager.class);

        final ClausePermissionHandler permissionHandler = mockController.getMock(ClausePermissionHandler.class);
        EasyMock.expect(permissionHandler.sanitise(null, input1)).andReturn(input1);
        EasyMock.expect(permissionHandler.sanitise(null, input1)).andReturn(input2);

        final ClauseHandler handler1 = new MockClauseHandler(null, null, permissionHandler, null);
        final ClauseHandler handler2 = new MockClauseHandler(null, null, permissionHandler, null);

        EasyMock.expect(manager.getClauseHandler((User) null, "field")).andReturn(CollectionBuilder.newBuilder(handler1, handler2).asList());

        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);

        mockController.replay();

        ClauseSanitisingVisitor visitor = new ClauseSanitisingVisitor(manager, jqlOperandResolver, null)
        {
            @Override
            TerminalClause sanitiseOperands(final TerminalClause clause)
            {
                return clause;
            }
        };

        final Clause expected = new OrClause(input1, input2);
        final Clause clause = input1.accept(visitor);
        assertEquals(expected, clause);

        mockController.verify();
    }

}
