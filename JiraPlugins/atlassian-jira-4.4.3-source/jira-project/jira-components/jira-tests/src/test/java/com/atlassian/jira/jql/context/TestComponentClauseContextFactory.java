package com.atlassian.jira.jql.context;

import com.atlassian.jira.bc.project.component.MockProjectComponent;
import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.opensymphony.user.User;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import com.atlassian.jira.jql.resolver.ComponentResolver;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;
import org.apache.commons.collections.CollectionUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @since v4.0
 */
public class TestComponentClauseContextFactory extends MockControllerTestCase
{
    private JqlOperandResolver jqlOperandResolver;
    private ComponentResolver componentResolver;
    private ProjectManager projectManager;
    private PermissionManager permissionManager;
    private User theUser = null;

    @Before
    public void setUp() throws Exception
    {
        jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        final ProjectComponentManager componentManager = mockController.getMock(ProjectComponentManager.class);
        componentResolver = new ComponentResolver(componentManager);
        mockController.addObjectInstance(componentResolver);
        projectManager = mockController.getMock(ProjectManager.class);
        permissionManager = mockController.getMock(PermissionManager.class);
    }

    @Test
    public void testGetContextFromClauseSingleEmptyValuePositiveOperator() throws Exception
    {
        final Operand operand = EmptyOperand.EMPTY;
        final TerminalClauseImpl clause = new TerminalClauseImpl("blarg", Operator.IS, operand);

        jqlOperandResolver.getValues(theUser, operand, clause);
        mockController.setReturnValue(CollectionBuilder.<QueryLiteral>newBuilder(new QueryLiteral()).asList());

        mockController.replay();
        ComponentClauseContextFactory factory = new ComponentClauseContextFactory(jqlOperandResolver, componentResolver, projectManager, permissionManager);
        final ClauseContext result = factory.getContextFromClause(theUser, clause);
        ClauseContext expectedResult = ClauseContextImpl.createGlobalClauseContext();

        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testGetContextFromClauseSingleEmptyValueNegativeOperator() throws Exception
    {
        final Operand operand = EmptyOperand.EMPTY;
        final TerminalClauseImpl clause = new TerminalClauseImpl("blarg", Operator.IS_NOT, operand);

        jqlOperandResolver.getValues(theUser, operand, clause);
        mockController.setReturnValue(CollectionBuilder.<QueryLiteral>newBuilder(new QueryLiteral()).asList());

        mockController.replay();
        ComponentClauseContextFactory factory = new ComponentClauseContextFactory(jqlOperandResolver, componentResolver, projectManager, permissionManager);
        final ClauseContext result = factory.getContextFromClause(theUser, clause);
        ClauseContext expectedResult = ClauseContextImpl.createGlobalClauseContext();

        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testGetContextFromClauseSingleValueEqualityOperand() throws Exception
    {
        final MockProject project = new MockProject(1234L);

        projectManager.getProjectObj(1234L);
        mockController.setReturnValue(project);
        final SingleValueOperand operand = new SingleValueOperand("blarg");
        final TerminalClauseImpl clause = new TerminalClauseImpl("blarg", Operator.EQUALS, operand);

        jqlOperandResolver.getValues(theUser, operand, clause);
        mockController.setReturnValue(CollectionBuilder.newBuilder(createLiteral("blarg")).asList());

        final ProjectComponent component = new MockProjectComponent(10L, "component", project.getId());

        ComponentResolver componentResolver = new ComponentResolver(mockController.getMock(ProjectComponentManager.class))
        {
            @Override
            public ProjectComponent get(final Long id)
            {
                return component;
            }
        };
        final Set<ProjectIssueTypeContext> issueTypeContexts = CollectionBuilder.<ProjectIssueTypeContext>newBuilder(new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L), new IssueTypeContextImpl("it"))).asListOrderedSet();

        mockController.replay();
        ComponentClauseContextFactory factory = new ComponentClauseContextFactory(jqlOperandResolver, componentResolver, projectManager, permissionManager)
        {
            @Override
            List<Long> getIds(final QueryLiteral literal)
            {
                return CollectionBuilder.newBuilder(10L).asList();
            }

            @Override
            Set<ProjectIssueTypeContext> getContextsForProject(final User searcher, final Project project)
            {
                return issueTypeContexts;
            }
        };

        final ClauseContext result = factory.getContextFromClause(theUser, clause);
        ClauseContext expectedResult = new ClauseContextImpl(issueTypeContexts);

        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testGetContextFromClauseSingleValueNegationOperand() throws Exception
    {
        final SingleValueOperand operand = new SingleValueOperand("blarg");
        final TerminalClauseImpl clause = new TerminalClauseImpl("blarg", Operator.NOT_EQUALS, operand);

        jqlOperandResolver.getValues(theUser, operand, clause);
        mockController.setReturnValue(CollectionBuilder.newBuilder(createLiteral(10L)).asList());

        final MockProject project1 = new MockProject(1234L);
        final MockProject project2 = new MockProject(5678L);
        final MockProject project3 = new MockProject(9876L);

        projectManager.getProjectObj(5678L);
        mockController.setReturnValue(project2);
        projectManager.getProjectObj(9876L);
        mockController.setReturnValue(project3);

        final ProjectComponent excludedComponent = new MockProjectComponent(10L, "excludedComponent", project1.getId());
        final ProjectComponent component1 = new MockProjectComponent(15L, "component1", project2.getId());
        final ProjectComponent component2 = new MockProjectComponent(20L, "component2", project3.getId());

        ComponentResolver componentResolver = new ComponentResolver(mockController.getMock(ProjectComponentManager.class))
        {
            @Override
            public ProjectComponent get(final Long id)
            {
                if (id.equals(10L))
                {
                    return excludedComponent;
                }
                return null;
            }

            @Override
            public Collection<ProjectComponent> getAll()
            {
                return CollectionBuilder.newBuilder(excludedComponent, component1, component2).asList();
            }
        };

        final Set<ProjectIssueTypeContext> issueTypeContexts1 = CollectionBuilder.<ProjectIssueTypeContext>newBuilder(new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L), new IssueTypeContextImpl("it"))).asListOrderedSet();
        final Set<ProjectIssueTypeContext> issueTypeContexts2 = CollectionBuilder.<ProjectIssueTypeContext>newBuilder(new ProjectIssueTypeContextImpl(new ProjectContextImpl(50L), new IssueTypeContextImpl("it2"))).asListOrderedSet();

        mockController.replay();
        ComponentClauseContextFactory factory = new ComponentClauseContextFactory(jqlOperandResolver, componentResolver, projectManager, permissionManager)
        {
            @Override
            List<Long> getIds(final QueryLiteral literal)
            {
                return CollectionBuilder.newBuilder(10L).asList();
            }

            @Override
            Set<ProjectIssueTypeContext> getContextsForProject(final User searcher, final Project project)
            {
                if (project.getId().equals(5678L))
                {
                    return issueTypeContexts1;
                }
                else if (project.getId().equals(9876L))
                {
                    return issueTypeContexts2;
                }
                return null;
            }
        };

        final ClauseContext result = factory.getContextFromClause(theUser, clause);
        ClauseContext expectedResult = new ClauseContextImpl(new HashSet<ProjectIssueTypeContext>(CollectionUtils.union(issueTypeContexts1, issueTypeContexts2)));

        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testGetContextFromClauseMultiValueEqualityOperand() throws Exception
    {
        final SingleValueOperand operand = new SingleValueOperand("blarg");
        final TerminalClauseImpl clause = new TerminalClauseImpl("blarg", Operator.EQUALS, operand);

        jqlOperandResolver.getValues(theUser, operand, clause);
        mockController.setReturnValue(CollectionBuilder.newBuilder(createLiteral(10L), createLiteral(20L)).asList());

        final MockProject project1 = new MockProject(5678L);
        final MockProject project2 = new MockProject(9876L);

        projectManager.getProjectObj(5678L);
        mockController.setReturnValue(project1);
        projectManager.getProjectObj(9876L);
        mockController.setReturnValue(project2);
        
        final ProjectComponent component1 = new MockProjectComponent(10L, "component", project1.getId());
        final ProjectComponent component2 = new MockProjectComponent(20L, "component", project2.getId());

        ComponentResolver componentResolver = new ComponentResolver(mockController.getMock(ProjectComponentManager.class))
        {
            @Override
            public ProjectComponent get(final Long id)
            {
                if (id.equals(10L))
                {
                    return component1;
                } else if (id.equals(20L))
                {
                    return component2;
                }
                return null;
            }
        };

        final Set<ProjectIssueTypeContext> issueTypeContexts1 = CollectionBuilder.<ProjectIssueTypeContext>newBuilder(new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L), new IssueTypeContextImpl("it"))).asListOrderedSet();
        final Set<ProjectIssueTypeContext> issueTypeContexts2 = CollectionBuilder.<ProjectIssueTypeContext>newBuilder(new ProjectIssueTypeContextImpl(new ProjectContextImpl(20L), new IssueTypeContextImpl("it2"))).asListOrderedSet();

        mockController.replay();
        ComponentClauseContextFactory factory = new ComponentClauseContextFactory(jqlOperandResolver, componentResolver, projectManager, permissionManager)
        {
            @Override
            List<Long> getIds(final QueryLiteral literal)
            {
                return CollectionBuilder.newBuilder(literal.getLongValue()).asList();
            }

            @Override
            Set<ProjectIssueTypeContext> getContextsForProject(final User searcher, final Project project)
            {
                if (project.getId().equals(5678L))
                {
                    return issueTypeContexts1;
                }
                else if (project.getId().equals(9876L))
                {
                    return issueTypeContexts2;
                }
                return null;
            }
        };

        final ClauseContext result = factory.getContextFromClause(theUser, clause);
        ClauseContext expectedResult = new ClauseContextImpl(new HashSet<ProjectIssueTypeContext>(CollectionUtils.union(issueTypeContexts1, issueTypeContexts2)));

        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testGetContextFromClauseMultieValueNegationOperand() throws Exception
    {
        final SingleValueOperand operand = new SingleValueOperand("blarg");
        final TerminalClauseImpl clause = new TerminalClauseImpl("blarg", Operator.NOT_EQUALS, operand);

        jqlOperandResolver.getValues(theUser, operand, clause);
        mockController.setReturnValue(CollectionBuilder.newBuilder(createLiteral(10L)).asList());

        final MockProject project1 = new MockProject(1234L);
        final MockProject project2 = new MockProject(5678L);
        final MockProject project3 = new MockProject(9876L);

        projectManager.getProjectObj(5678L);
        mockController.setReturnValue(project2);

        final ProjectComponent excludedComponent1 = new MockProjectComponent(10L, "excludedComponent1", project1.getId());
        final ProjectComponent excludedComponent2 = new MockProjectComponent(20L, "excludedComponent2", project3.getId());
        final ProjectComponent component1 = new MockProjectComponent(15L, "component1", project2.getId());

        ComponentResolver componentResolver = new ComponentResolver(mockController.getMock(ProjectComponentManager.class))
        {
            @Override
            public ProjectComponent get(final Long id)
            {
                if (id.equals(10L))
                {
                    return excludedComponent1;
                }
                if (id.equals(20L))
                {
                    return excludedComponent2;
                }
                return null;
            }

            @Override
            public Collection<ProjectComponent> getAll()
            {
                return CollectionBuilder.newBuilder(excludedComponent1, component1, excludedComponent2).asList();
            }
        };

        final Set<ProjectIssueTypeContext> issueTypeContexts1 = CollectionBuilder.<ProjectIssueTypeContext>newBuilder(new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L), new IssueTypeContextImpl("it"))).asListOrderedSet();

        mockController.replay();
        ComponentClauseContextFactory factory = new ComponentClauseContextFactory(jqlOperandResolver, componentResolver, projectManager, permissionManager)
        {
            @Override
            List<Long> getIds(final QueryLiteral literal)
            {
                return CollectionBuilder.newBuilder(10L, 20L).asList();
            }

            @Override
            Set<ProjectIssueTypeContext> getContextsForProject(final User searcher, final Project project)
            {
                if (project.getId().equals(5678L))
                {
                    return issueTypeContexts1;
                }
                return null;
            }
        };

        final ClauseContext result = factory.getContextFromClause(theUser, clause);
        ClauseContext expectedResult = new ClauseContextImpl(issueTypeContexts1);

        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testGetContextFromClauseNullLiterals() throws Exception
    {
        final EmptyOperand operand = EmptyOperand.EMPTY;

        final TerminalClauseImpl clause = new TerminalClauseImpl("blarg", Operator.EQUALS, operand);
        jqlOperandResolver.getValues(theUser, operand, clause);
        mockController.setReturnValue(null);

        ComponentClauseContextFactory factory = mockController.instantiateAndReplay(ComponentClauseContextFactory.class);

        final ClauseContext result = factory.getContextFromClause(theUser, clause);
        ClauseContext expectedResult = ClauseContextImpl.createGlobalClauseContext();

        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testGetContextFromClauseNoLiterals() throws Exception
    {
        final EmptyOperand operand = EmptyOperand.EMPTY;

        final TerminalClauseImpl clause = new TerminalClauseImpl("blarg", Operator.EQUALS, operand);
        jqlOperandResolver.getValues(theUser, operand, clause);
        mockController.setReturnValue(CollectionBuilder.newBuilder().asList());

        ComponentClauseContextFactory factory = mockController.instantiateAndReplay(ComponentClauseContextFactory.class);

        final ClauseContext result = factory.getContextFromClause(theUser, clause);
        ClauseContext expectedResult = ClauseContextImpl.createGlobalClauseContext();

        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testGetContextFromClauseInvalidOperator() throws Exception
    {
        final SingleValueOperand operand = new SingleValueOperand("blarg");
        final TerminalClauseImpl clause = new TerminalClauseImpl("blarg", Operator.LESS_THAN, operand);

        mockController.replay();
        ComponentClauseContextFactory factory = new ComponentClauseContextFactory(jqlOperandResolver, componentResolver, projectManager, permissionManager);

        final ClauseContext result = factory.getContextFromClause(theUser, clause);
        ClauseContext expectedResult = ClauseContextImpl.createGlobalClauseContext();

        assertEquals(expectedResult, result);

        mockController.verify();
    }
}
