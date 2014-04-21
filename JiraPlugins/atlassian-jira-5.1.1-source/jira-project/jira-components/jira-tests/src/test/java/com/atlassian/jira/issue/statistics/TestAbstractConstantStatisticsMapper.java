package com.atlassian.jira.issue.statistics;

import com.atlassian.jira.issue.resolution.MockResolution;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import org.junit.Test;
import static org.junit.Assert.*;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.search.MockJqlSearchRequest;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.query.Query;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operator.Operator;
import org.ofbiz.core.entity.GenericValue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Tests that the {@link com.atlassian.jira.issue.statistics.AbstractConstantStatisticsMapper} correctly modifies
 * {@link com.atlassian.jira.issue.search.SearchRequest}s to contain the
 * additional clauses required to link to the specific values given.
 *
 * Note: we are testing the modified clauses using Clause#toString() which may not be ideal.
 *
 * @since v4.0
 */
public class TestAbstractConstantStatisticsMapper extends MockControllerTestCase
{
    private static final String PROJECT_CLAUSE_NAME = "project";
    private static final String DOCUMENT_CONSTANT = "myResolution";
    private static final String ISSUE_TYPE_CONSTANT = "myResolution";
    private static final String ISSUE_FIELD_CONSTANT = "myResolution";

    private ConstantsManager mockConstantsManager;

    @Test
    public void testGetUrlSuffixForSomeConstant() throws Exception
    {
        mockConstantsManager = getMock(ConstantsManager.class);
        AbstractConstantStatisticsMapper mapper = new MyConstantStatisticsMapper();

        Query query = JqlQueryBuilder.newBuilder().where().addNumberCondition(PROJECT_CLAUSE_NAME, 88L).buildQuery();
        SearchRequest sr = new MockJqlSearchRequest(10000L, query);

        MockResolution value = new MockResolution("123", "fixed");

        replay();

        final SearchRequest actual = mapper.getSearchUrlSuffix(value, sr);

        Clause clause = actual.getQuery().getWhereClause();
        assertTrue(clause instanceof AndClause);
        AndClause and = (AndClause)clause;
        Set<Clause> clauses = new HashSet<Clause>(and.getClauses());
        assertEquals(clauses, new HashSet<Clause>(Arrays.asList(
                new TerminalClauseImpl(PROJECT_CLAUSE_NAME, Operator.EQUALS, 88L),
                new TerminalClauseImpl(ISSUE_FIELD_CONSTANT, Operator.EQUALS, "fixed"))));

        verify();
    }

    @Test
    public void testGetUrlSuffixForNullConstant() throws Exception
    {
        mockConstantsManager = getMock(ConstantsManager.class);
        AbstractConstantStatisticsMapper mapper = new MyConstantStatisticsMapper();

        Query query = JqlQueryBuilder.newBuilder().where().project(88L).buildQuery();
        SearchRequest sr = new MockJqlSearchRequest(10000L, query);

        replay();

        final SearchRequest actual = mapper.getSearchUrlSuffix(null, sr);

        Clause clause = actual.getQuery().getWhereClause();
        assertTrue(clause instanceof AndClause);
        AndClause and = (AndClause)clause;
        Set<Clause> clauses = new HashSet<Clause>(and.getClauses());
        assertEquals(clauses, new HashSet<Clause>(Arrays.asList(
                new TerminalClauseImpl(PROJECT_CLAUSE_NAME, Operator.EQUALS, 88L),
                new TerminalClauseImpl(ISSUE_FIELD_CONSTANT, Operator.IS, EmptyOperand.EMPTY))));

        verify();
    }

    @Test
    public void testGetUrlSuffixForNullSearchRequest() throws Exception
    {
        AbstractConstantStatisticsMapper mapper = new MyConstantStatisticsMapper();
        assertNull(mapper.getSearchUrlSuffix(null, null));
    }

    private class MyConstantStatisticsMapper extends AbstractConstantStatisticsMapper
    {
        protected MyConstantStatisticsMapper()
        {
            super(mockConstantsManager);
        }

        public String getDocumentConstant()
        {
            return DOCUMENT_CONSTANT;
        }

        protected String getConstantType()
        {
            return ISSUE_TYPE_CONSTANT;
        }

        protected String getIssueFieldConstant()
        {
            return ISSUE_FIELD_CONSTANT;
        }
    }
}
