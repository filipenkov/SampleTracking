package com.atlassian.jira.upgrade.tasks.jql;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.plugin.jql.function.MembersOfFunction;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operator.Operator;

/**
 * @since v4.0
 */
public class TestUserGroupParameterCustomFieldClauseXmlHandler extends ListeningTestCase
{
    @Test
    public void testIsSafeToNamifyValue() throws Exception
    {
        UserGroupParameterCustomFieldClauseXmlHandler xmlHandler = new UserGroupParameterCustomFieldClauseXmlHandler();
        assertFalse(xmlHandler.isSafeToNamifyValue());
    }

    @Test
    public void testCreateClause() throws Exception
    {
        UserGroupParameterCustomFieldClauseXmlHandler xmlHandler = new UserGroupParameterCustomFieldClauseXmlHandler();
        Clause expectedResult = new TerminalClauseImpl("field", Operator.IN, new FunctionOperand(MembersOfFunction.FUNCTION_MEMBERSOF, "group"));
        final Clause result = xmlHandler.createClause("field", "group");
        assertEquals(expectedResult, result);
    }
}
