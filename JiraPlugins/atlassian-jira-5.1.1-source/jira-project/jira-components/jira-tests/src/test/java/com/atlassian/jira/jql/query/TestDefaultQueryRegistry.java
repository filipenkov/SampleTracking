package com.atlassian.jira.jql.query;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.managers.SearchHandlerManager;
import com.atlassian.jira.jql.ClauseHandler;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Test for {@link com.atlassian.jira.jql.query.DefaultQueryRegistry}.
 *
 * @since v4.0
 */
public class TestDefaultQueryRegistry extends MockControllerTestCase
{
    private final User theUser = null;
    private QueryCreationContext queryCreationContext;

    @Before
    public void setUp() throws Exception
    {
        queryCreationContext = new QueryCreationContextImpl(theUser);
    }

    @Test
    public void testConstructor()
    {
        mockController.replay();
        try
        {
            new DefaultQueryRegistry(null);
            fail("Should not accept null default registry.");
        }
        catch (final IllegalArgumentException expected)
        {}
        mockController.verify();
    }

    @Test
    public void testGetClauseQueryFactory() throws Exception
    {
        final ClauseQueryFactory clauseQueryFactory = mockController.getMock(ClauseQueryFactory.class);
        final ClauseHandler clauseHandler = mockController.getMock(ClauseHandler.class);
        clauseHandler.getFactory();
        mockController.setReturnValue(clauseQueryFactory);

        final List<ClauseHandler> expectedHandler = Collections.singletonList(clauseHandler);

        final SearchHandlerManager searchHandlerManager = mockController.getMock(SearchHandlerManager.class);
        final String clauseName = "name";
        searchHandlerManager.getClauseHandler(theUser, clauseName);
        mockController.setReturnValue(expectedHandler);

        final DefaultQueryRegistry queryRegistry = mockController.instantiate(DefaultQueryRegistry.class);
        assertEquals(Collections.singletonList(clauseQueryFactory), new ArrayList<ClauseQueryFactory>(queryRegistry.getClauseQueryFactory(
            queryCreationContext, new TerminalClauseImpl(clauseName, Operator.IN, "value"))));

        mockController.verify();
    }

    @Test
    public void testGetClauseQueryFactoryOverrideSecurity() throws Exception
    {
        queryCreationContext = new QueryCreationContextImpl(theUser, true);

        final ClauseQueryFactory clauseQueryFactory = mockController.getMock(ClauseQueryFactory.class);
        final ClauseHandler clauseHandler = mockController.getMock(ClauseHandler.class);
        EasyMock.expect(clauseHandler.getFactory()).andReturn(clauseQueryFactory);

        final List<ClauseHandler> expectedHandler = Collections.singletonList(clauseHandler);

        final SearchHandlerManager searchHandlerManager = mockController.getMock(SearchHandlerManager.class);
        final String clauseName = "name";
        EasyMock.expect(searchHandlerManager.getClauseHandler(clauseName)).andReturn(expectedHandler);

        final DefaultQueryRegistry queryRegistry = mockController.instantiate(DefaultQueryRegistry.class);
        assertEquals(Collections.singletonList(clauseQueryFactory), new ArrayList<ClauseQueryFactory>(queryRegistry.getClauseQueryFactory(
            queryCreationContext, new TerminalClauseImpl(clauseName, Operator.IN, "value"))));

        mockController.verify();
    }

    @Test
    public void testGetClauseQueryFactoryBadArgs() throws Exception
    {
        final DefaultQueryRegistry queryRegistry = mockController.instantiate(DefaultQueryRegistry.class);

        try
        {
            queryRegistry.getClauseQueryFactory(queryCreationContext, null);
            fail("Should not accept a null clause.");
        }
        catch (final IllegalArgumentException expected)
        {}

        mockController.verify();
    }
}
