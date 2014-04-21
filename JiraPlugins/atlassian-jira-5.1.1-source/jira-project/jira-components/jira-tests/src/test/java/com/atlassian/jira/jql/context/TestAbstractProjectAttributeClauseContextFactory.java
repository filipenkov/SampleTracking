package com.atlassian.jira.jql.context;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import com.atlassian.jira.jql.resolver.IndexInfoResolver;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @since v4.0
 */
public class TestAbstractProjectAttributeClauseContextFactory extends MockControllerTestCase
{
    private IndexInfoResolver indexInfoResolver;
    private JqlOperandResolver jqlOperandResolver;
    private PermissionManager permissionManager;

    @Before
    public void setUp() throws Exception
    {
        indexInfoResolver = mockController.getMock(IndexInfoResolver.class);
        jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        permissionManager = mockController.getMock(PermissionManager.class);
    }

    @Test
    public void testIsNegationOperator() throws Exception
    {
        MyAbstractProjectAttributeClauseContextFactory factory = mockController.instantiateAndReplay(MyAbstractProjectAttributeClauseContextFactory.class);

        assertTrue(factory.isNegationOperator(Operator.NOT_EQUALS));
        assertTrue(factory.isNegationOperator(Operator.NOT_IN));
        assertTrue(factory.isNegationOperator(Operator.IS_NOT));

        assertFalse(factory.isNegationOperator(Operator.IS));
        assertFalse(factory.isNegationOperator(Operator.EQUALS));
        assertFalse(factory.isNegationOperator(Operator.IN));
        assertFalse(factory.isNegationOperator(Operator.GREATER_THAN));
        assertFalse(factory.isNegationOperator(Operator.GREATER_THAN_EQUALS));
        assertFalse(factory.isNegationOperator(Operator.LESS_THAN));
        assertFalse(factory.isNegationOperator(Operator.LESS_THAN_EQUALS));
    }

    @Test
    public void testIsRelationalOperator() throws Exception
    {
        MyAbstractProjectAttributeClauseContextFactory factory = mockController.instantiateAndReplay(MyAbstractProjectAttributeClauseContextFactory.class);

        assertTrue(factory.isRelationalOperator(Operator.GREATER_THAN));
        assertTrue(factory.isRelationalOperator(Operator.GREATER_THAN_EQUALS));
        assertTrue(factory.isRelationalOperator(Operator.LESS_THAN));
        assertTrue(factory.isRelationalOperator(Operator.LESS_THAN_EQUALS));

        assertFalse(factory.isRelationalOperator(Operator.IS_NOT));
        assertFalse(factory.isRelationalOperator(Operator.NOT_EQUALS));
        assertFalse(factory.isRelationalOperator(Operator.NOT_IN));
        assertFalse(factory.isRelationalOperator(Operator.IS));
        assertFalse(factory.isRelationalOperator(Operator.EQUALS));
        assertFalse(factory.isRelationalOperator(Operator.IN));
    }

    @Test
    public void testGetIdsLongValue() throws Exception
    {
        QueryLiteral literal = createLiteral(10L);
        indexInfoResolver.getIndexedValues(10L);
        mockController.setReturnValue(Collections.singletonList("10"));
        indexInfoResolver.getIndexedValues(10L);
        mockController.setReturnValue(Collections.emptyList());

        mockController.replay();
        MyAbstractProjectAttributeClauseContextFactory factory = new MyAbstractProjectAttributeClauseContextFactory(indexInfoResolver, jqlOperandResolver, permissionManager);

        List<Long> result = factory.getIds(literal);
        assertEquals(1, result.size());
        assertTrue(result.contains(10L));
        result = factory.getIds(literal);
        assertTrue(result.isEmpty());

        mockController.verify();
    }

    @Test
    public void testGetIdsFromStringValue() throws Exception
    {
        QueryLiteral literal = createLiteral("test");

        indexInfoResolver.getIndexedValues("test");
        mockController.setReturnValue(Collections.singletonList("10"));
        indexInfoResolver.getIndexedValues("test");
        mockController.setReturnValue(Collections.emptyList());

        mockController.replay();
        MyAbstractProjectAttributeClauseContextFactory factory = new MyAbstractProjectAttributeClauseContextFactory(indexInfoResolver, jqlOperandResolver, permissionManager);

        List<Long> result = factory.getIds(literal);
        assertEquals(1, result.size());
        assertTrue(result.contains(10L));
        result = factory.getIds(literal);
        assertTrue(result.isEmpty());

        mockController.verify();
    }

    @Test
    public void testGetContextsForProject() throws Exception
    {
        MockProject project1 = new MockProject(10, "test1");
        permissionManager.hasPermission(Permissions.BROWSE, project1,(User) null);
        mockController.setReturnValue(true);
        mockController.replay();

        MyAbstractProjectAttributeClauseContextFactory factory = new MyAbstractProjectAttributeClauseContextFactory(indexInfoResolver, jqlOperandResolver, permissionManager);
        final Set<ProjectIssueTypeContext> result = factory.getContextsForProject(null, project1);
        Set<ProjectIssueTypeContext> expectedResult = CollectionBuilder.<ProjectIssueTypeContext>newBuilder(new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L), AllIssueTypesContext.INSTANCE)).asListOrderedSet();
        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testGetContextsForNullProject() throws Exception
    {
        MockProject project1 = null;
        mockController.replay();

        MyAbstractProjectAttributeClauseContextFactory factory = new MyAbstractProjectAttributeClauseContextFactory(indexInfoResolver, jqlOperandResolver, permissionManager);
        final Set<ProjectIssueTypeContext> result = factory.getContextsForProject(null, project1);
        Set<ProjectIssueTypeContext> expectedResult = CollectionBuilder.<ProjectIssueTypeContext>newBuilder().asListOrderedSet();
        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testGetContextsForProjectNoPerm() throws Exception
    {
        MockProject project1 = new MockProject(10, "test1");
        permissionManager.hasPermission(Permissions.BROWSE, project1,(User) null);
        mockController.setReturnValue(false);
        mockController.replay();

        MyAbstractProjectAttributeClauseContextFactory factory = new MyAbstractProjectAttributeClauseContextFactory(indexInfoResolver, jqlOperandResolver, permissionManager);
        final Set<ProjectIssueTypeContext> result = factory.getContextsForProject(null, project1);
        Set<ProjectIssueTypeContext> expectedResult = CollectionBuilder.<ProjectIssueTypeContext>newBuilder().asListOrderedSet();
        assertEquals(expectedResult, result);

        mockController.verify();
    }
    
    @Test
    public void testGetClauseContextNotEmpty() throws Exception
    {
        final SingleValueOperand operand = new SingleValueOperand("blarg");
        final TerminalClauseImpl clause = new TerminalClauseImpl("blarg", Operator.GREATER_THAN, operand);
        final ClauseContext context = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(new ProjectIssueTypeContextImpl(new ProjectContextImpl(2l), new IssueTypeContextImpl("IT2"))).asListOrderedSet());

        jqlOperandResolver.isEmptyOperand(operand);
        mockController.setReturnValue(false);
        

        mockController.replay();

        MyAbstractProjectAttributeClauseContextFactory factory = new MyAbstractProjectAttributeClauseContextFactory(indexInfoResolver, jqlOperandResolver, permissionManager)
        {
            @Override
            ClauseContext getContextFromClause(final User searcher, final TerminalClause terminalClause)
            {
                return context;
            }
        };

        final ClauseContext result = factory.getClauseContext(null, clause);
        assertEquals(context, result);

        mockController.verify();
    }

    @Test
    public void testGetClauseContextNotEmptyReturnsEmptyContextFromClause() throws Exception
    {
        final SingleValueOperand operand = new SingleValueOperand("blarg");
        final TerminalClauseImpl clause = new TerminalClauseImpl("blarg", Operator.GREATER_THAN, operand);
        final ClauseContext expectedContext = ClauseContextImpl.createGlobalClauseContext();

        jqlOperandResolver.isEmptyOperand(operand);
        mockController.setReturnValue(false);

        mockController.replay();

        MyAbstractProjectAttributeClauseContextFactory factory = new MyAbstractProjectAttributeClauseContextFactory(indexInfoResolver, jqlOperandResolver, permissionManager);

        final ClauseContext result = factory.getClauseContext(null, clause);
        assertEquals(expectedContext, result);

        mockController.verify();
    }

    @Test
    public void testGetClauseContextEmpty() throws Exception
    {
        final SingleValueOperand operand = new SingleValueOperand("blarg");
        final TerminalClauseImpl clause = new TerminalClauseImpl("blarg", Operator.GREATER_THAN, operand);
        final ClauseContext context = ClauseContextImpl.createGlobalClauseContext();

        jqlOperandResolver.isEmptyOperand(operand);
        mockController.setReturnValue(true);


        mockController.replay();

        MyAbstractProjectAttributeClauseContextFactory factory = new MyAbstractProjectAttributeClauseContextFactory(indexInfoResolver, jqlOperandResolver, permissionManager);

        final ClauseContext result = factory.getClauseContext(null, clause);
        assertEquals(context, result);

        mockController.verify();
    }

    public static class MyAbstractProjectAttributeClauseContextFactory extends AbstractProjectAttributeClauseContextFactory<Version>
    {
        public MyAbstractProjectAttributeClauseContextFactory(IndexInfoResolver indexInfoResolver, final JqlOperandResolver jqlOperandResolver, final PermissionManager permissionManager)
        {
            super(indexInfoResolver, jqlOperandResolver, permissionManager);
        }

        ClauseContext getContextFromClause(final User searcher, final TerminalClause terminalClause)
        {
            return new ClauseContextImpl(Collections.<ProjectIssueTypeContext>emptySet());
        }
    }

}
