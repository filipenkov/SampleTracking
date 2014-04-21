package com.atlassian.jira.issue.search.searchers.transformer;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.customfields.view.CustomFieldParamsImpl;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.Query;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;
import com.opensymphony.user.User;
import mock.user.MockOSUser;

import java.util.List;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

public class TestCustomFieldLabelsSearchInputTransformer extends ListeningTestCase
{
    private User user = new MockOSUser("admin");

    @Test
    public void testGetClauseFromParams()
    {
        final String clauseName = "testName";
        final CustomFieldInputHelper mockCustomFieldInputHelper = createMock(CustomFieldInputHelper.class);
        ClauseNames clauseNames = new ClauseNames("cf[10000]");
        
        replay(mockCustomFieldInputHelper);
        CustomFieldLabelsSearchInputTransformer inputTransformer = new CustomFieldLabelsSearchInputTransformer(null, null, mockCustomFieldInputHelper, clauseNames)
        {
            @Override
            protected String getClauseName(final User searcher, final ClauseNames clauseNames)
            {
                return clauseName;
            }
        };

        CustomFieldParams params = new CustomFieldParamsImpl(null, null);

        TerminalClause clause = (TerminalClause) inputTransformer.getClauseFromParams(user, params);
        assertNull(clause);

        params.put(null, CollectionBuilder.newBuilder("dUde").asList());
        clause = (TerminalClause) inputTransformer.getClauseFromParams(user, params);
        assertNotNull(clause);
        assertEquals("testName", clause.getName());
        assertEquals(Operator.EQUALS, clause.getOperator());
        assertEquals("dUde", ((SingleValueOperand)clause.getOperand()).getStringValue());

        params.put(null, CollectionBuilder.newBuilder("dUde multi VALUE").asList());
        clause = (TerminalClause) inputTransformer.getClauseFromParams(user, params);
        assertNotNull(clause);
        assertEquals("testName", clause.getName());
        assertEquals(Operator.IN, clause.getOperator());
        final List<Operand> operators = ((MultiValueOperand) clause.getOperand()).getValues();
        assertTrue(operators.contains(new SingleValueOperand("dUde")));
        assertTrue(operators.contains(new SingleValueOperand("multi")));
        assertTrue(operators.contains(new SingleValueOperand("VALUE")));
        assertEquals(3, operators.size());

        verify(mockCustomFieldInputHelper);
    }

    @Test
    public void testGetParamsFromSR()
    {
        final CustomFieldInputHelper mockCustomFieldInputHelper = createMock(CustomFieldInputHelper.class);
        ClauseNames clauseNames = new ClauseNames("cf[10000]");

        replay(mockCustomFieldInputHelper);
        CustomFieldLabelsSearchInputTransformer inputTransformer = new CustomFieldLabelsSearchInputTransformer(null, null, mockCustomFieldInputHelper, clauseNames);

        Query query = JqlQueryBuilder.newClauseBuilder().customField(10000L).eq("boO").buildQuery();
        CustomFieldParams params = inputTransformer.getParamsFromSearchRequest(user, query, null);
        assertEquals(CollectionBuilder.list("boO"), params.getValuesForNullKey());

        query = JqlQueryBuilder.newClauseBuilder().customField(10000L).is().empty().buildQuery();
        params = inputTransformer.getParamsFromSearchRequest(user, query, null);
        assertNull(params);

        query = JqlQueryBuilder.newClauseBuilder().customField(10000L).in().strings("DUDE", "boO", "Moo").buildQuery();
        params = inputTransformer.getParamsFromSearchRequest(user, query, null);
        assertEquals(CollectionBuilder.list("DUDE", "boO", "Moo"), params.getValuesForNullKey());


        verify(mockCustomFieldInputHelper);
    }
}
