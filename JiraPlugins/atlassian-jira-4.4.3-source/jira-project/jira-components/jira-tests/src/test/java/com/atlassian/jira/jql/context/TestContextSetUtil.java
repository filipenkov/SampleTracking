package com.atlassian.jira.jql.context;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import static org.junit.Assert.*;

import com.atlassian.jira.util.collect.CollectionBuilder;

import java.util.Collections;

/**
 * @since v4.0
 */
public class TestContextSetUtil extends MockControllerTestCase
{
    @Test
    public void testIntersectionEmptySet() throws Exception
    {
        ContextSetUtil contextSetUtil = new ContextSetUtil();
        final ClauseContext clauseContext = contextSetUtil.intersect(Collections.<ClauseContext>emptySet());
        assertNotNull(clauseContext);
        assertTrue(clauseContext.getContexts().isEmpty());
    }

    @Test
    public void testIntersectionNoMatchingByProject() throws Exception
    {
        final ProjectIssueTypeContextImpl projectIssueTypeContext1 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L),
                                                                                                     new IssueTypeContextImpl("11"));
        final ProjectIssueTypeContextImpl projectIssueTypeContext2 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(11L),
                                                                                                     new IssueTypeContextImpl("11"));
        ClauseContext context1 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1).asListOrderedSet());
        ClauseContext context2 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext2).asListOrderedSet());

        final ContextSetUtil contextSetUtil = new ContextSetUtil();
        mockController.replay();
        ClauseContext result = contextSetUtil.intersect(CollectionBuilder.newBuilder(context1, context2).asListOrderedSet());
        assertTrue(result.getContexts().isEmpty());
    }

    @Test
    public void testIntersectionNoMatchingIssueType() throws Exception
    {
        final ProjectIssueTypeContextImpl projectIssueTypeContext1 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L),
                                                                                                     new IssueTypeContextImpl("10"));
        final ProjectIssueTypeContextImpl projectIssueTypeContext2 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(11L),
                                                                                                     new IssueTypeContextImpl("11"));
        ClauseContext context1 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1).asListOrderedSet());
        ClauseContext context2 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext2).asListOrderedSet());
        
        final ContextSetUtil contextSetUtil = new ContextSetUtil();
        mockController.replay();
        ClauseContext result = contextSetUtil.intersect(CollectionBuilder.newBuilder(context1, context2).asListOrderedSet());
        assertTrue(result.getContexts().isEmpty());
    }

    @Test
    public void testIntersectionOneMatching() throws Exception
    {
        final ProjectIssueTypeContextImpl projectIssueTypeContext1 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L),
                                                                                                     new IssueTypeContextImpl("10"));
        final ProjectIssueTypeContextImpl projectIssueTypeContext2 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L),
                                                                                                     new IssueTypeContextImpl("10"));

        ClauseContext context1 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1).asListOrderedSet());
        ClauseContext context2 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext2).asListOrderedSet());

        final ContextSetUtil contextSetUtil = new ContextSetUtil();
        mockController.replay();
        ClauseContext result = contextSetUtil.intersect(CollectionBuilder.newBuilder(context1, context2).asListOrderedSet());
        assertEquals(1, result.getContexts().size());
        assertEquals(projectIssueTypeContext1, result.getContexts().iterator().next());
    }

    @Test
    public void testIntersectionOneMatchingPromotedElementType() throws Exception
    {
        final ProjectIssueTypeContextImpl projectIssueTypeContext1 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L),
                                                                                                     new IssueTypeContextImpl("10"));
        final ProjectIssueTypeContextImpl projectIssueTypeContext2 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L),
                                                                                                     new IssueTypeContextImpl("10"));

        ClauseContext context1 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1).asListOrderedSet());
        ClauseContext context2 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext2).asListOrderedSet());

        final ContextSetUtil contextSetUtil = new ContextSetUtil();
        mockController.replay();
        ClauseContext result = contextSetUtil.intersect(CollectionBuilder.newBuilder(context1, context2).asListOrderedSet());
        assertEquals(1, result.getContexts().size());
        assertEquals(projectIssueTypeContext1, result.getContexts().iterator().next());
    }

    @Test
    public void testIntersectionComplex() throws Exception
    {
        final ProjectIssueTypeContextImpl projectIssueTypeContext1 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L),
                                                                                                     new IssueTypeContextImpl("10"));
        final ProjectIssueTypeContextImpl projectIssueTypeContext2 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L),
                                                                                                     new IssueTypeContextImpl("10"));

        final ProjectIssueTypeContextImpl projectIssueTypeContext3 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(11L),
                                                                                                     new IssueTypeContextImpl("11"));
        final ProjectIssueTypeContextImpl projectIssueTypeContext4 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(11L),
                                                                                                     new IssueTypeContextImpl("11"));

        final ProjectIssueTypeContextImpl projectIssueTypeContext5 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(11L),
                                                                                                     new IssueTypeContextImpl("11"));

        ClauseContext context1 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1, projectIssueTypeContext3).asListOrderedSet());
        ClauseContext context2 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext2, projectIssueTypeContext4).asListOrderedSet());

        final ContextSetUtil contextSetUtil = new ContextSetUtil();
        mockController.replay();
        ClauseContext result = contextSetUtil.intersect(CollectionBuilder.newBuilder(context1, context2).asListOrderedSet());
        assertEquals(2, result.getContexts().size());
        assertTrue(result.getContexts().contains(projectIssueTypeContext1));
        assertTrue(result.getContexts().contains(projectIssueTypeContext5));
    }

    @Test
    public void testIntersectionOneMatchingOneEmpty() throws Exception
    {
        final ProjectIssueTypeContextImpl projectIssueTypeContext1 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L),
                                                                                                     new IssueTypeContextImpl("10"));

        ClauseContext context1 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1).asListOrderedSet());
        ClauseContext context2 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder().asListOrderedSet());

        final ContextSetUtil contextSetUtil = new ContextSetUtil();
        mockController.replay();
        ClauseContext result = contextSetUtil.intersect(CollectionBuilder.newBuilder(context1, context2).asListOrderedSet());
        assertTrue(result.getContexts().isEmpty());
    }

    @Test
    public void testIntersectionOneMatchingOneEmptyOtherWayAround() throws Exception
    {
        final ProjectIssueTypeContextImpl projectIssueTypeContext1 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L),
                                                                                                     new IssueTypeContextImpl("10"));

        ClauseContext context1 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder().asListOrderedSet());
        ClauseContext context2 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1).asListOrderedSet());

        final ContextSetUtil contextSetUtil = new ContextSetUtil();
        mockController.replay();
        ClauseContext result = contextSetUtil.intersect(CollectionBuilder.newBuilder(context1, context2).asListOrderedSet());
        assertTrue(result.getContexts().isEmpty());
    }

    @Test
    public void testIntersectionPreCondition() throws Exception
    {
        final ProjectIssueTypeContextImpl projectIssueTypeContext1 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L),
                                                                                                     new IssueTypeContextImpl("10"));

        ClauseContext context1 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1).asListOrderedSet());
        ClauseContext context2 = null;

        try
        {
            final ContextSetUtil contextSetUtil = new ContextSetUtil();
            mockController.replay();
            contextSetUtil.intersect(CollectionBuilder.newBuilder(context1, context2).asListOrderedSet());
            fail("Should throw IAE");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
    }

    @Test
    public void testUnionNoEmptySet() throws Exception
    {
        ContextSetUtil contextSetUtil = new ContextSetUtil();
        final ClauseContext clauseContext = contextSetUtil.union(Collections.<ClauseContext>emptySet());
        assertNotNull(clauseContext);
        assertTrue(clauseContext.getContexts().isEmpty());
    }

    @Test
    public void testUnionNoMatchingByProject() throws Exception
    {
        final ProjectIssueTypeContextImpl projectIssueTypeContext1 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L),
                                                                                                     new IssueTypeContextImpl("11"));
        final ProjectIssueTypeContextImpl projectIssueTypeContext2 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(11L),
                                                                                                     new IssueTypeContextImpl("11"));
        ClauseContext context1 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1).asListOrderedSet());
        ClauseContext context2 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext2).asListOrderedSet());

        final ContextSetUtil contextSetUtil = new ContextSetUtil();
        mockController.replay();
        ClauseContext result = contextSetUtil.union(CollectionBuilder.newBuilder(context1, context2).asListOrderedSet());
        assertEquals(2, result.getContexts().size());
        assertTrue(result.getContexts().contains(projectIssueTypeContext1));
        assertTrue(result.getContexts().contains(projectIssueTypeContext2));
    }

    @Test
    public void testUnionNoMatchingIssueType() throws Exception
    {
        final ProjectIssueTypeContextImpl projectIssueTypeContext1 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L),
                                                                                                     new IssueTypeContextImpl("10"));
        final ProjectIssueTypeContextImpl projectIssueTypeContext2 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(11L),
                                                                                                     new IssueTypeContextImpl("11"));
        ClauseContext context1 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1).asListOrderedSet());
        ClauseContext context2 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext2).asListOrderedSet());

        final ContextSetUtil contextSetUtil = new ContextSetUtil();
        mockController.replay();
        ClauseContext result = contextSetUtil.union(CollectionBuilder.newBuilder(context1, context2).asListOrderedSet());
        assertEquals(2, result.getContexts().size());
        assertTrue(result.getContexts().contains(projectIssueTypeContext1));
        assertTrue(result.getContexts().contains(projectIssueTypeContext2));
    }

    @Test
    public void testUnionOneMatching() throws Exception
    {
        final ProjectIssueTypeContextImpl projectIssueTypeContext1 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L),
                                                                                                     new IssueTypeContextImpl("10"));
        final ProjectIssueTypeContextImpl projectIssueTypeContext2 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L),
                                                                                                     new IssueTypeContextImpl("10"));

        ClauseContext context1 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1).asListOrderedSet());
        ClauseContext context2 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext2).asListOrderedSet());

        final ContextSetUtil contextSetUtil = new ContextSetUtil();
        mockController.replay();
        ClauseContext result = contextSetUtil.union(CollectionBuilder.newBuilder(context1, context2).asListOrderedSet());
        assertEquals(1, result.getContexts().size());
        assertEquals(projectIssueTypeContext1, result.getContexts().iterator().next());
    }

    @Test
    public void testUnionOneMatchingOneAll() throws Exception
    {
        final ProjectIssueTypeContextImpl projectIssueTypeContext1 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L),
                                                                                                     new IssueTypeContextImpl("10"));
        final ProjectIssueTypeContext projectIssueTypeContext2 = ProjectIssueTypeContextImpl.createGlobalContext();

        ClauseContext context1 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1).asListOrderedSet());
        ClauseContext context2 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext2).asListOrderedSet());

        final ContextSetUtil contextSetUtil = new ContextSetUtil();
        mockController.replay();
        ClauseContext result = contextSetUtil.union(CollectionBuilder.newBuilder(context1, context2).asListOrderedSet());

        ClauseContext expected = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1, projectIssueTypeContext2).asListOrderedSet());
        assertEquals(2, result.getContexts().size());
        assertEquals(expected, result);
    }

    @Test
    public void testUnionTwoAllIssueType() throws Exception
    {
        final ProjectIssueTypeContextImpl projectIssueTypeContext1 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L), AllIssueTypesContext.INSTANCE);
        final ProjectIssueTypeContext projectIssueTypeContext2 = ProjectIssueTypeContextImpl.createGlobalContext();

        ClauseContext context1 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1).asListOrderedSet());
        ClauseContext context2 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext2).asListOrderedSet());

        final ContextSetUtil contextSetUtil = new ContextSetUtil();
        mockController.replay();
        ClauseContext result = contextSetUtil.union(CollectionBuilder.newBuilder(context1, context2).asListOrderedSet());

        ClauseContext expected = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1, projectIssueTypeContext2).asListOrderedSet());
        assertEquals(2, result.getContexts().size());
        assertEquals(expected, result);
    }

    @Test
    public void testUnionTwoAllProject() throws Exception
    {
        final ProjectIssueTypeContextImpl projectIssueTypeContext1 = new ProjectIssueTypeContextImpl(AllProjectsContext.INSTANCE, new IssueTypeContextImpl("10"));
        final ProjectIssueTypeContextImpl projectIssueTypeContext2 = new ProjectIssueTypeContextImpl(AllProjectsContext.INSTANCE, new IssueTypeContextImpl("10"));

        ClauseContext context1 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1).asListOrderedSet());
        ClauseContext context2 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext2).asListOrderedSet());

        final ContextSetUtil contextSetUtil = new ContextSetUtil();
        mockController.replay();
        ClauseContext result = contextSetUtil.union(CollectionBuilder.newBuilder(context1, context2).asListOrderedSet());

        ClauseContext expected = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1).asListOrderedSet());
        assertEquals(1, result.getContexts().size());
        assertEquals(expected, result);
    }

    @Test
    public void testUnionTwoAllIssueTypeSameProject() throws Exception
    {
        final ProjectIssueTypeContextImpl projectIssueTypeContext1 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L), AllIssueTypesContext.INSTANCE);
        final ProjectIssueTypeContextImpl projectIssueTypeContext2 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L), AllIssueTypesContext.INSTANCE);

        ClauseContext context1 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1).asListOrderedSet());
        ClauseContext context2 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext2).asListOrderedSet());

        final ContextSetUtil contextSetUtil = new ContextSetUtil();
        mockController.replay();
        ClauseContext result = contextSetUtil.union(CollectionBuilder.newBuilder(context1, context2).asListOrderedSet());

        ClauseContext expected = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1).asListOrderedSet());
        assertEquals(1, result.getContexts().size());
        assertEquals(expected, result);
    }

    @Test
    public void testIntersectionOneMatchingOneAll() throws Exception
    {
        final ProjectIssueTypeContextImpl projectIssueTypeContext1 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L),
                                                                                                     new IssueTypeContextImpl("10"));
        final ProjectIssueTypeContext projectIssueTypeContext2 = ProjectIssueTypeContextImpl.createGlobalContext();

        ClauseContext context1 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1).asListOrderedSet());
        ClauseContext context2 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext2).asListOrderedSet());

        final ContextSetUtil contextSetUtil = new ContextSetUtil();
        mockController.replay();
        ClauseContext result = contextSetUtil.intersect(CollectionBuilder.newBuilder(context1, context2).asListOrderedSet());

        ClauseContext expected = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1).asListOrderedSet());
        assertEquals(1, result.getContexts().size());
        assertEquals(expected, result);
    }

    @Test
    public void testIntersectionOneMatchingOneAllOtherOrder() throws Exception
    {
        final ProjectIssueTypeContextImpl projectIssueTypeContext1 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L), AllIssueTypesContext.INSTANCE);
        final ProjectIssueTypeContextImpl projectIssueTypeContext2 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L), new IssueTypeContextImpl("10"));

        ClauseContext context1 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1).asListOrderedSet());
        ClauseContext context2 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext2).asListOrderedSet());

        final ContextSetUtil contextSetUtil = new ContextSetUtil();
        mockController.replay();
        ClauseContext result = contextSetUtil.intersect(CollectionBuilder.newBuilder(context1, context2).asListOrderedSet());

        ClauseContext expected = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L), new IssueTypeContextImpl("10"))).asListOrderedSet());
        assertEquals(1, result.getContexts().size());
        assertEquals(expected, result);
    }

    @Test
    public void testIntersectionTwoAllIssueType() throws Exception
    {
        final ProjectIssueTypeContextImpl projectIssueTypeContext1 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L), AllIssueTypesContext.INSTANCE);
        final ProjectIssueTypeContext projectIssueTypeContext2 = ProjectIssueTypeContextImpl.createGlobalContext();

        ClauseContext context1 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1).asListOrderedSet());
        ClauseContext context2 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext2).asListOrderedSet());

        final ContextSetUtil contextSetUtil = new ContextSetUtil();
        mockController.replay();
        ClauseContext result = contextSetUtil.intersect(CollectionBuilder.newBuilder(context1, context2).asListOrderedSet());

        ClauseContext expected = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1).asListOrderedSet());
        assertEquals(1, result.getContexts().size());
        assertEquals(expected, result);
    }

    @Test
    public void testIntersectionTwoAllProject() throws Exception
    {
        final ProjectIssueTypeContextImpl projectIssueTypeContext1 = new ProjectIssueTypeContextImpl(AllProjectsContext.INSTANCE, new IssueTypeContextImpl("10"));
        final ProjectIssueTypeContextImpl projectIssueTypeContext2 = new ProjectIssueTypeContextImpl(AllProjectsContext.INSTANCE, new IssueTypeContextImpl("10"));

        ClauseContext context1 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1).asListOrderedSet());
        ClauseContext context2 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext2).asListOrderedSet());

        final ContextSetUtil contextSetUtil = new ContextSetUtil();
        mockController.replay();
        ClauseContext result = contextSetUtil.intersect(CollectionBuilder.newBuilder(context1, context2).asListOrderedSet());

        ClauseContext expected = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1).asListOrderedSet());
        assertEquals(1, result.getContexts().size());
        assertEquals(expected, result);
    }

    @Test
    public void testIntersectionTwoAllIssueTypeSameProject() throws Exception
    {
        final ProjectIssueTypeContextImpl projectIssueTypeContext1 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L), AllIssueTypesContext.INSTANCE);
        final ProjectIssueTypeContextImpl projectIssueTypeContext2 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L), AllIssueTypesContext.INSTANCE);

        ClauseContext context1 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1).asListOrderedSet());
        ClauseContext context2 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext2).asListOrderedSet());

        final ContextSetUtil contextSetUtil = new ContextSetUtil();
        mockController.replay();
        ClauseContext result = contextSetUtil.intersect(CollectionBuilder.newBuilder(context1, context2).asListOrderedSet());

        ClauseContext expected = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1).asListOrderedSet());
        assertEquals(1, result.getContexts().size());
        assertEquals(expected, result);
    }

    @Test
    public void testUnionOneMatchingPromotedElementType() throws Exception
    {
        final ProjectIssueTypeContextImpl projectIssueTypeContext1 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L),
                                                                                                     new IssueTypeContextImpl("10"));
        final ProjectIssueTypeContextImpl projectIssueTypeContext2 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L),
                                                                                                     new IssueTypeContextImpl("10"));

        ClauseContext context1 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1).asListOrderedSet());
        ClauseContext context2 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext2).asListOrderedSet());

        final ContextSetUtil contextSetUtil = new ContextSetUtil();
        mockController.replay();
        ClauseContext result = contextSetUtil.union(CollectionBuilder.newBuilder(context1, context2).asListOrderedSet());
        assertEquals(1, result.getContexts().size());
        assertEquals(projectIssueTypeContext1, result.getContexts().iterator().next());
    }

    @Test
    public void testUnionComplex() throws Exception
    {
        final ProjectIssueTypeContextImpl projectIssueTypeContext1 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L),
                                                                                                     new IssueTypeContextImpl("10"));
        final ProjectIssueTypeContextImpl projectIssueTypeContext2 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L),
                                                                                                     new IssueTypeContextImpl("10"));

        final ProjectIssueTypeContextImpl projectIssueTypeContext3 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(11L),
                                                                                                     new IssueTypeContextImpl("11"));
        final ProjectIssueTypeContextImpl projectIssueTypeContext4 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(11L),
                                                                                                     new IssueTypeContextImpl("11"));
        final ProjectIssueTypeContextImpl projectIssueTypeContext5 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(12L),
                                                                                                     new IssueTypeContextImpl("12"));

        final ProjectIssueTypeContextImpl newHigherOrderProjectIssueTypeContext = new ProjectIssueTypeContextImpl(new ProjectContextImpl(11L),
                                                                                                                  new IssueTypeContextImpl("11"));

        ClauseContext context1 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1, projectIssueTypeContext3).asListOrderedSet());
        ClauseContext context2 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext2, projectIssueTypeContext4, projectIssueTypeContext5).asListOrderedSet());

        final ContextSetUtil contextSetUtil = new ContextSetUtil();
        mockController.replay();
        ClauseContext result = contextSetUtil.union(CollectionBuilder.newBuilder(context1, context2).asListOrderedSet());
        assertEquals(3, result.getContexts().size());
        assertTrue(result.getContexts().contains(projectIssueTypeContext1));
        assertTrue(result.getContexts().contains(projectIssueTypeContext5));
        assertTrue(result.getContexts().contains(newHigherOrderProjectIssueTypeContext));
    }

    @Test
    public void testUnionOneMatchingOneEmpty() throws Exception
    {
        final ProjectIssueTypeContextImpl projectIssueTypeContext1 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L),
                                                                                                     new IssueTypeContextImpl("10"));

        ClauseContext context1 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1).asListOrderedSet());
        ClauseContext context2 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder().asListOrderedSet());

        final ContextSetUtil contextSetUtil = new ContextSetUtil();
        mockController.replay();
        ClauseContext result = contextSetUtil.union(CollectionBuilder.newBuilder(context1, context2).asListOrderedSet());
        assertEquals(1, result.getContexts().size());
        assertTrue(result.getContexts().contains(projectIssueTypeContext1));
    }

    @Test
    public void testUnionOneMatchingOneEmptyOtherWayAround() throws Exception
    {
        final ProjectIssueTypeContextImpl projectIssueTypeContext1 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L),
                                                                                                     new IssueTypeContextImpl("10"));

        ClauseContext context1 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder().asListOrderedSet());
        ClauseContext context2 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1).asListOrderedSet());

        final ContextSetUtil contextSetUtil = new ContextSetUtil();
        mockController.replay();
        ClauseContext result = contextSetUtil.union(CollectionBuilder.newBuilder(context1, context2).asListOrderedSet());
        assertEquals(1, result.getContexts().size());
        assertTrue(result.getContexts().contains(projectIssueTypeContext1));
    }

    @Test
    public void testUnionPreCondition() throws Exception
    {
        final ProjectIssueTypeContextImpl projectIssueTypeContext1 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L),
                                                                                                     new IssueTypeContextImpl("10"));

        ClauseContext context1 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext1).asListOrderedSet());
        ClauseContext context2 = null;

        try
        {
            final ContextSetUtil contextSetUtil = new ContextSetUtil();
            mockController.replay();
            contextSetUtil.union(CollectionBuilder.newBuilder(context1, context2).asListOrderedSet());
            fail("Should throw IAE");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
    }
}
