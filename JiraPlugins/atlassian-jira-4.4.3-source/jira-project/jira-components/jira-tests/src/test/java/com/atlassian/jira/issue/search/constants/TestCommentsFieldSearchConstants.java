package com.atlassian.jira.issue.search.constants;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.query.operator.Operator;
import com.atlassian.jira.local.ListeningTestCase;

import java.util.EnumSet;

/**
 * Test for {@link com.atlassian.jira.issue.search.constants.CommentsFieldSearchConstants}.
 *
 * @since v4.0
 */
public class TestCommentsFieldSearchConstants extends ListeningTestCase
{
    @Test
    public void testConstants() throws Exception
    {
        CommentsFieldSearchConstants constants = CommentsFieldSearchConstants.getInstance();
        assertEquals(new ClauseNames("comment"), constants.getJqlClauseNames());
        assertEquals("body", constants.getUrlParameter());
    }

    @Test
    public void testSupportedOperators() throws Exception
    {
        assertEquals(EnumSet.of(Operator.LIKE, Operator.NOT_LIKE), CommentsFieldSearchConstants.getInstance().getSupportedOperators());
    }
}
