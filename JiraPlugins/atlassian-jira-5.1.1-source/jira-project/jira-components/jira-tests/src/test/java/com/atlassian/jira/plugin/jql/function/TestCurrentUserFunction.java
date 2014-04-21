package com.atlassian.jira.plugin.jql.function;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.query.QueryCreationContextImpl;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for the {@link com.atlassian.jira.plugin.jql.function.CurrentUserFunction}.
 *
 * @since v4.0
 */
public class TestCurrentUserFunction extends ListeningTestCase
{
    private User user;
    private QueryCreationContext queryCreationContext;
    private TerminalClause terminalClause =null;

    @Before
    public void setUp() throws Exception
    {
        user = new MockUser("wgibson", "William Gibson", "wgibson@neuromancer.net");
        queryCreationContext = new QueryCreationContextImpl(user);
    }

    @Test
    public void testDataType() throws Exception
    {
        final CurrentUserFunction currentUserFunction = new CurrentUserFunction();
        assertEquals(JiraDataTypes.USER, currentUserFunction.getDataType());        
    }

    @Test
    public void testValidateTooMayArguments()
    {
        final CurrentUserFunction currentUserFunction = new CurrentUserFunction()
        {
            @Override
            protected I18nHelper getI18n()
            {
                return new MockI18nBean();
            }
        };
        FunctionOperand function = new FunctionOperand("currentUser", EasyList.build("badArgument"));
        final MessageSet errorCollection = currentUserFunction.validate(user, function, terminalClause);
        assertTrue(errorCollection.hasAnyErrors());
        assertEquals("Function 'currentUser' expected '0' arguments but received '1'.", errorCollection.getErrorMessages().iterator().next());
    }

    @Test
    public void testGetValuesHappyPath()
    {
        final CurrentUserFunction currentUserFunction = new CurrentUserFunction();
        FunctionOperand function = new FunctionOperand("currentUser", Collections.<String>emptyList());
        final List<QueryLiteral> values = currentUserFunction.getValues(queryCreationContext, function, terminalClause);
        assertEquals(1, values.size());
        assertEquals(user.getName(), values.get(0).getStringValue());
        assertEquals(function, values.get(0).getSourceOperand());
    }

    @Test
    public void testGetValuesNullContext()
    {
        final CurrentUserFunction currentUserFunction = new CurrentUserFunction();
        FunctionOperand function = new FunctionOperand("currentUser", Collections.<String>emptyList());
        assertEquals(Collections.<QueryLiteral>emptyList(), currentUserFunction.getValues(null, function, terminalClause));
    }

    @Test
    public void testGetValuesNullUserInContext()
    {
        final CurrentUserFunction currentUserFunction = new CurrentUserFunction();
        FunctionOperand function = new FunctionOperand("currentUser", Collections.<String>emptyList());
        assertEquals(Collections.<QueryLiteral>emptyList(), currentUserFunction.getValues(new QueryCreationContextImpl(null), function, terminalClause));
    }

    @Test
    public void testGetMinimumNumberOfExpectedArguments()
    {
        final CurrentUserFunction currentUserFunction = new CurrentUserFunction();
        assertEquals(0, currentUserFunction.getMinimumNumberOfExpectedArguments());
    }

}
