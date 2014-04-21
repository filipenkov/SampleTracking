package com.atlassian.jira.issue.search.searchers.transformer;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.bc.EntityNotFoundException;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.util.collect.CollectionBuilder;

import java.util.Set;

/**
 * @since v4.0
 */
public class TestComponentSearchContextVisibilityChecker extends MockControllerTestCase
{
    private ProjectComponentManager componentManager;
    private SearchContext searchContext;

    @Before
    public void setUp() throws Exception
    {
        componentManager = mockController.getMock(ProjectComponentManager.class);
        searchContext = mockController.getMock(SearchContext.class);
    }

    @Test
    public void testVisibleInContextIsVisible() throws Exception
    {
        searchContext.getProjectIds();
        mockController.setReturnValue(CollectionBuilder.newBuilder(10L).asList());

        componentManager.findProjectIdForComponent(5L);
        mockController.setReturnValue(10L);

        componentManager.findProjectIdForComponent(7L);
        mockController.setReturnValue(15L);

        mockController.replay();
        final ComponentSearchContextVisibilityChecker checker = new ComponentSearchContextVisibilityChecker(componentManager);
        final Set<String> result = checker.FilterOutNonVisibleInContext(searchContext, CollectionBuilder.newBuilder("5", "7", "ab").asList());
        assertEquals(CollectionBuilder.newBuilder("5").asSet(), result);
        mockController.verify();
    }

    @Test
    public void testVisibleInContextNoProjects() throws Exception
    {
        searchContext.getProjectIds();
        mockController.setReturnValue(CollectionBuilder.newBuilder().asList());

        mockController.replay();
        final ComponentSearchContextVisibilityChecker checker = new ComponentSearchContextVisibilityChecker(componentManager);
        final Set<String> result = checker.FilterOutNonVisibleInContext(searchContext, CollectionBuilder.newBuilder("5", "7", "ab").asList());
        assertEquals(CollectionBuilder.newBuilder().asSet(), result);
        mockController.verify();
    }
    
    @Test
    public void testVisibleInContextTwoProjects() throws Exception
    {
        searchContext.getProjectIds();
        mockController.setReturnValue(CollectionBuilder.newBuilder(10L, 20L).asList());

        mockController.replay();
        final ComponentSearchContextVisibilityChecker checker = new ComponentSearchContextVisibilityChecker(componentManager);
        final Set<String> result = checker.FilterOutNonVisibleInContext(searchContext, CollectionBuilder.newBuilder("5", "7", "ab").asList());
        assertEquals(CollectionBuilder.newBuilder().asSet(), result);
        mockController.verify();
    }

    @Test
    public void testVisibleInContextExcecption() throws Exception
    {
        searchContext.getProjectIds();
        mockController.setReturnValue(CollectionBuilder.newBuilder(10L).asList());

        componentManager.findProjectIdForComponent(5L);
        mockController.setThrowable(new EntityNotFoundException());

        mockController.replay();
        final ComponentSearchContextVisibilityChecker checker = new ComponentSearchContextVisibilityChecker(componentManager);
        final Set<String> result = checker.FilterOutNonVisibleInContext(searchContext, CollectionBuilder.newBuilder("5").asList());
        assertTrue(result.isEmpty());
        mockController.verify();
    }
}
