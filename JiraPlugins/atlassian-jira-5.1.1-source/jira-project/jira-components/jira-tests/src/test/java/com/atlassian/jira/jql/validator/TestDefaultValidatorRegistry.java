package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.issue.search.managers.SearchHandlerManager;
import com.atlassian.jira.jql.ClauseHandler;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @since v4.0
 */
public class TestDefaultValidatorRegistry extends MockControllerTestCase
{
    @Test
    public void testConstrcutor()
    {
        mockController.replay();
        try
        {
            new DefaultValidatorRegistry(null, null, null);
            fail("Should not accept null default registry.");
        }
        catch (final IllegalArgumentException expected)
        {}
        mockController.verify();
    }

    @Test
    public void testGetClauseQueryFactory() throws Exception
    {
        final ClauseValidator clauseValidator = mockController.getMock(ClauseValidator.class);
        final ClauseHandler clauseHandler = mockController.getMock(ClauseHandler.class);
        clauseHandler.getValidator();
        mockController.setReturnValue(clauseValidator);

        final List<ClauseHandler> expectedHandlers = Collections.singletonList(clauseHandler);

        final SearchHandlerManager searchHandlerManager = mockController.getMock(SearchHandlerManager.class);
        final String clauseName = "name";
        searchHandlerManager.getClauseHandler((User) null, clauseName);
        mockController.setReturnValue(expectedHandlers);

        final DefaultValidatorRegistry validatorRegistry = mockController.instantiate(DefaultValidatorRegistry.class);
        assertEquals(Collections.singletonList(clauseValidator), new ArrayList<ClauseValidator>(validatorRegistry.getClauseValidator(null,
            new TerminalClauseImpl(clauseName, Operator.IN, "value"))));

        mockController.verify();
    }

    @Test
    public void testGetClauseQueryFactoryBadArgs() throws Exception
    {
        final DefaultValidatorRegistry validatorRegistry = mockController.instantiate(DefaultValidatorRegistry.class);

        try
        {
            validatorRegistry.getClauseValidator(null, (TerminalClause)null);
            fail("Should not accept a null clause.");
        }
        catch (final IllegalArgumentException expected)
        {}

        mockController.verify();
    }
}
