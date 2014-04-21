package com.atlassian.jira.jql.query;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import static com.atlassian.jira.util.collect.CollectionBuilder.list;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.util.JqlIssueKeySupport;
import com.atlassian.jira.jql.util.JqlIssueSupport;
import com.atlassian.jira.jql.validator.MockJqlOperandResolver;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;

import java.util.Collections;

/**
 * Test for {@link com.atlassian.jira.jql.query.IssueIdClauseQueryFactory}.
 *
 * @since v4.0
 */
public class TestIssueIdClauseQueryFactory extends MockControllerTestCase
{
    private JqlOperandResolver jqlOperandResolver;
    private JqlIssueKeySupport issueKeySupport;
    private JqlIssueSupport issueSupport;
    private final User theUser = null;
    private QueryCreationContext queryCreationContext;

    @Before
    public void setUp() throws Exception
    {
        jqlOperandResolver = MockJqlOperandResolver.createSimpleSupport();
        issueKeySupport = mockController.getMock(JqlIssueKeySupport.class);
        issueSupport = mockController.getMock(JqlIssueSupport.class);
        queryCreationContext = new QueryCreationContextImpl(theUser);
    }

    @Test
    public void testValidationIs() throws Exception
    {
        final String fieldName = "field";
        final SingleValueOperand singleValueOperand = new SingleValueOperand("value");
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.IS, singleValueOperand);

        replay();

        final IssueIdClauseQueryFactory idClauseQueryFactory = new IssueIdClauseQueryFactory(jqlOperandResolver, issueKeySupport, issueSupport);
        final QueryFactoryResult query = idClauseQueryFactory.getQuery(queryCreationContext, clause);

        assertEquals(query, QueryFactoryResult.createFalseResult());

        verify();
    }

    @Test
    public void testValidationIsNot() throws Exception
    {
        final String fieldName = "field";
        final SingleValueOperand singleValueOperand = new SingleValueOperand("value");
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.IS_NOT, singleValueOperand);

        replay();

        final IssueIdClauseQueryFactory idClauseQueryFactory = new IssueIdClauseQueryFactory(jqlOperandResolver, issueKeySupport, issueSupport);
        final QueryFactoryResult query = idClauseQueryFactory.getQuery(queryCreationContext, clause);

        assertEquals(query, QueryFactoryResult.createFalseResult());

        verify();
    }

    @Test
    public void testIsEmptyClause() throws Exception
    {
        final String fieldName = "field";
        final EmptyOperand emptyOperand = new EmptyOperand();

        replay();

        final IssueIdClauseQueryFactory idClauseQueryFactory = new IssueIdClauseQueryFactory(jqlOperandResolver, issueKeySupport, issueSupport);
        final QueryFactoryResult query = idClauseQueryFactory.getQuery(queryCreationContext, new TerminalClauseImpl(fieldName, Operator.IS,
            emptyOperand));

        assertFalse(query.mustNotOccur());
        assertEquals(new BooleanQuery(), query.getLuceneQuery());

        verify();
    }

    @Test
    public void testEqualsNoValues() throws Exception
    {
        final String fieldName = "equals";
        final SingleValueOperand singleValueOperand = new SingleValueOperand("value");
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.EQUALS, singleValueOperand);

        jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        expect(jqlOperandResolver.getValues(queryCreationContext, singleValueOperand, clause)).andReturn(null);

        replay();

        final IssueIdClauseQueryFactory idClauseQueryFactory = new IssueIdClauseQueryFactory(jqlOperandResolver, issueKeySupport, issueSupport);
        final QueryFactoryResult query = idClauseQueryFactory.getQuery(queryCreationContext, clause);

        assertEquals(new BooleanQuery(), query.getLuceneQuery());
        assertFalse(query.mustNotOccur());

        verify();
    }

    @Test
    public void testEqualsSingleValueId() throws Exception
    {
        final String fieldName = "equals";
        final SingleValueOperand singleValueOperand = new SingleValueOperand(1L);
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.EQUALS, singleValueOperand);

        replay();

        final IssueIdClauseQueryFactory idClauseQueryFactory = new IssueIdClauseQueryFactory(jqlOperandResolver, issueKeySupport, issueSupport);
        final QueryFactoryResult query = idClauseQueryFactory.getQuery(queryCreationContext, clause);

        final Query expectedQuery = new TermQuery(new Term(SystemSearchConstants.forIssueId().getIndexField(), "1"));

        assertEquals(expectedQuery, query.getLuceneQuery());
        assertFalse(query.mustNotOccur());

        verify();
    }

    @Test
    public void testEqualsMultipleValueId() throws Exception
    {
        final String fieldName = "equals";
        final Operand operand = new MultiValueOperand(1L, 2L);
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.EQUALS, operand);

        replay();

        final IssueIdClauseQueryFactory idClauseQueryFactory = new IssueIdClauseQueryFactory(jqlOperandResolver, issueKeySupport, issueSupport);
        final QueryFactoryResult query = idClauseQueryFactory.getQuery(queryCreationContext, clause);

        final BooleanQuery expectedQuery = new BooleanQuery();
        expectedQuery.add(new TermQuery(new Term(SystemSearchConstants.forIssueId().getIndexField(), "1")), BooleanClause.Occur.SHOULD);
        expectedQuery.add(new TermQuery(new Term(SystemSearchConstants.forIssueId().getIndexField(), "2")), BooleanClause.Occur.SHOULD);

        assertEquals(expectedQuery, query.getLuceneQuery());
        assertFalse(query.mustNotOccur());

        verify();
    }

    @Test
    public void testEqualsSingleValueKey() throws Exception
    {
        final String fieldName = "equals";
        final SingleValueOperand singleValueOperand = new SingleValueOperand("KEY1");
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.EQUALS, singleValueOperand);

        replay();

        final IssueIdClauseQueryFactory idClauseQueryFactory = new IssueIdClauseQueryFactory(jqlOperandResolver, issueKeySupport, issueSupport);
        final QueryFactoryResult query = idClauseQueryFactory.getQuery(queryCreationContext, clause);

        final Query expectedQuery = new TermQuery(new Term(SystemSearchConstants.forIssueKey().getIndexField(), "key1"));

        assertEquals(expectedQuery, query.getLuceneQuery());
        assertFalse(query.mustNotOccur());

        verify();
    }

    @Test
    public void testEqualsMultipleValueIdAndKey() throws Exception
    {
        final String fieldName = "equals";
        final Operand operand = new MultiValueOperand(createLiteral(147l), createLiteral("KEY"), createLiteral("Stra\u00dfe"), new QueryLiteral());
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.IN, operand);

        replay();

        final IssueIdClauseQueryFactory idClauseQueryFactory = new IssueIdClauseQueryFactory(jqlOperandResolver, issueKeySupport, issueSupport);
        final QueryFactoryResult query = idClauseQueryFactory.getQuery(queryCreationContext, clause);

        final BooleanQuery expectedQuery = new BooleanQuery();
        expectedQuery.add(new TermQuery(new Term(SystemSearchConstants.forIssueId().getIndexField(), "147")), BooleanClause.Occur.SHOULD);
        expectedQuery.add(new TermQuery(new Term(SystemSearchConstants.forIssueKey().getIndexField(), "key")), BooleanClause.Occur.SHOULD);
        expectedQuery.add(new TermQuery(new Term(SystemSearchConstants.forIssueKey().getIndexField(), "strasse")), BooleanClause.Occur.SHOULD);

        assertEquals(expectedQuery, query.getLuceneQuery());
        assertFalse(query.mustNotOccur());

        verify();
    }

    @Test
    public void testNotEqualsSingleValueKey() throws Exception
    {
        final String fieldName = "equals";
        final SingleValueOperand singleValueOperand = new SingleValueOperand("KEY1");
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.NOT_EQUALS, singleValueOperand);

        replay();

        final IssueIdClauseQueryFactory idClauseQueryFactory = new IssueIdClauseQueryFactory(jqlOperandResolver, issueKeySupport, issueSupport);
        final QueryFactoryResult query = idClauseQueryFactory.getQuery(queryCreationContext, clause);

        final Query expectedQuery = new TermQuery(new Term(SystemSearchConstants.forIssueKey().getIndexField(), "key1"));

        assertEquals(expectedQuery, query.getLuceneQuery());
        assertTrue(query.mustNotOccur());

        verify();
    }

    @Test
    public void testNotEqualsMultipleValueIdAndKey() throws Exception
    {
        final String fieldName = "notEquals";
        final Operand operand = new MultiValueOperand(createLiteral(147l), createLiteral("KEY"), createLiteral("Stra\u00dfe"), new QueryLiteral());
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.NOT_IN, operand);

        replay();

        final IssueIdClauseQueryFactory idClauseQueryFactory = new IssueIdClauseQueryFactory(jqlOperandResolver, issueKeySupport, issueSupport);
        final QueryFactoryResult query = idClauseQueryFactory.getQuery(queryCreationContext, clause);

        final BooleanQuery expectedQuery = new BooleanQuery();
        expectedQuery.add(new TermQuery(new Term(SystemSearchConstants.forIssueId().getIndexField(), "147")), BooleanClause.Occur.SHOULD);
        expectedQuery.add(new TermQuery(new Term(SystemSearchConstants.forIssueKey().getIndexField(), "key")), BooleanClause.Occur.SHOULD);
        expectedQuery.add(new TermQuery(new Term(SystemSearchConstants.forIssueKey().getIndexField(), "strasse")), BooleanClause.Occur.SHOULD);

        assertEquals(expectedQuery, query.getLuceneQuery());
        assertTrue(query.mustNotOccur());

        verify();
    }

    @Test
    public void testNotEqualsNoValues() throws Exception
    {
        final String fieldName = "notEquals";
        final SingleValueOperand singleValueOperand = new SingleValueOperand("value");
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.NOT_IN, singleValueOperand);

        jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        expect(jqlOperandResolver.getValues(queryCreationContext, singleValueOperand, clause)).andReturn(null);

        replay();

        final IssueIdClauseQueryFactory idClauseQueryFactory = new IssueIdClauseQueryFactory(jqlOperandResolver, issueKeySupport, issueSupport);
        final QueryFactoryResult query = idClauseQueryFactory.getQuery(queryCreationContext, clause);

        assertEquals(new BooleanQuery(), query.getLuceneQuery());
        assertFalse(query.mustNotOccur());

        verify();
    }

    @Test
    public void testLessThanSingleValue() throws Exception
    {
        final String fieldName = "lessThan";
        final String keyValue = "KEY-200";
        final SingleValueOperand singleValueOperand = new SingleValueOperand(keyValue);
        final MockIssue issue = new MockIssue(1L);
        issue.setKey(keyValue);
        issue.setProjectObject(new MockProject(78));
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.LESS_THAN, singleValueOperand);

        final JqlIssueSupport issueSupport = mockController.getMock(JqlIssueSupport.class);
        expect(issueSupport.getIssues(keyValue, (com.atlassian.crowd.embedded.api.User) null)).andReturn(Collections.<Issue> singletonList(issue));

        final JqlIssueKeySupport keySupport = mockController.getMock(JqlIssueKeySupport.class);
        expect(keySupport.parseKeyNum(keyValue)).andReturn(200L);

        replay();

        final IssueIdClauseQueryFactory idClauseQueryFactory = new IssueIdClauseQueryFactory(jqlOperandResolver, issueKeySupport, issueSupport);
        final QueryFactoryResult query = idClauseQueryFactory.getQuery(queryCreationContext, clause);

        final BooleanQuery expectedQuery = new BooleanQuery();
        expectedQuery.add(new TermRangeQuery("keynumpart_range", null, "0000000000005k", true, false), BooleanClause.Occur.MUST);
        expectedQuery.add(new TermQuery(new Term("projid", "78")), BooleanClause.Occur.MUST);

        assertEquals(expectedQuery, query.getLuceneQuery());
        assertFalse(query.mustNotOccur());

        verify();
    }

    @Test
    public void testLessThanSingleValueOverrideSecurity() throws Exception
    {
        queryCreationContext = new QueryCreationContextImpl(theUser, true);

        final String fieldName = "lessThan";
        final String keyValue = "KEY-200";
        final SingleValueOperand singleValueOperand = new SingleValueOperand(keyValue);
        final MockIssue issue = new MockIssue(1L);
        issue.setKey(keyValue);
        issue.setProjectObject(new MockProject(78));
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.LESS_THAN, singleValueOperand);

        final JqlIssueSupport issueSupport = mockController.getMock(JqlIssueSupport.class);
        expect(issueSupport.getIssues(keyValue)).andReturn(Collections.<Issue> singletonList(issue));

        final JqlIssueKeySupport keySupport = mockController.getMock(JqlIssueKeySupport.class);
        expect(keySupport.parseKeyNum(keyValue)).andReturn(200L);

        replay();

        final IssueIdClauseQueryFactory idClauseQueryFactory = new IssueIdClauseQueryFactory(jqlOperandResolver, issueKeySupport, issueSupport);
        final QueryFactoryResult query = idClauseQueryFactory.getQuery(queryCreationContext, clause);

        final BooleanQuery expectedQuery = new BooleanQuery();
        expectedQuery.add(new TermRangeQuery("keynumpart_range", null, "0000000000005k", true, false), BooleanClause.Occur.MUST);
        expectedQuery.add(new TermQuery(new Term("projid", "78")), BooleanClause.Occur.MUST);

        assertEquals(expectedQuery, query.getLuceneQuery());
        assertFalse(query.mustNotOccur());

        verify();
    }

    @Test
    public void testLessThanSingleEmptyValue() throws Exception
    {
        final String fieldName = "lessThan";
        final Operand operand = EmptyOperand.EMPTY;
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.LESS_THAN, operand);

        replay();

        final IssueIdClauseQueryFactory idClauseQueryFactory = new IssueIdClauseQueryFactory(jqlOperandResolver, issueKeySupport, issueSupport);
        final QueryFactoryResult query = idClauseQueryFactory.getQuery(queryCreationContext, clause);

        final BooleanQuery expectedQuery = new BooleanQuery();

        assertEquals(expectedQuery, query.getLuceneQuery());
        assertFalse(query.mustNotOccur());

        verify();
    }

    @Test
    public void testLessThanNoValues() throws Exception
    {
        final String fieldName = "lessThan";
        final String keyValue = "KEY-200";
        final SingleValueOperand singleValueOperand = new SingleValueOperand("value");
        final MockIssue issue = new MockIssue(1L);
        issue.setKey(keyValue);
        issue.setProjectObject(new MockProject(78));
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.LESS_THAN, singleValueOperand);

        jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        expect(jqlOperandResolver.getValues(queryCreationContext, singleValueOperand, clause)).andReturn(Collections.<QueryLiteral> emptyList());
        expect(jqlOperandResolver.isListOperand(singleValueOperand)).andReturn(false);

        replay();

        final IssueIdClauseQueryFactory idClauseQueryFactory = new IssueIdClauseQueryFactory(jqlOperandResolver, issueKeySupport, issueSupport);
        final QueryFactoryResult query = idClauseQueryFactory.getQuery(queryCreationContext, clause);

        assertEquals(new BooleanQuery(), query.getLuceneQuery());
        assertFalse(query.mustNotOccur());

        verify();
    }

    @Test
    public void testLessThanEqualsMultipleIssues() throws Exception
    {
        final String fieldName = "lessThan";
        final String keyValue1 = "KEY-243920849";
        final String keyValue2 = "MON-555544447";
        final long issueId = 10L;

        final SingleValueOperand singleValueOperand = new SingleValueOperand("value");
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.LESS_THAN_EQUALS, singleValueOperand);
        final MockIssue issue = new MockIssue(1L);
        issue.setKey(keyValue1);
        issue.setProjectObject(new MockProject(78));

        final MockIssue issue2 = new MockIssue(1L);
        issue2.setKey(keyValue1);
        issue2.setProjectObject(new MockProject(23));

        final MockIssue issue3 = new MockIssue(10L);
        issue3.setKey(keyValue2);
        issue3.setProjectObject(new MockProject(4));

        jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        expect(jqlOperandResolver.getValues(queryCreationContext, singleValueOperand, clause)).andReturn(
            list(createLiteral(keyValue1), createLiteral(issueId), new QueryLiteral()));
        expect(jqlOperandResolver.isListOperand(singleValueOperand)).andReturn(false);

        final JqlIssueSupport issueSupport = mockController.getMock(JqlIssueSupport.class);
        expect(issueSupport.getIssues(keyValue1, (com.atlassian.crowd.embedded.api.User) null)).andReturn(CollectionBuilder.<Issue> list(issue, issue2));

        expect(issueSupport.getIssue(issueId, (com.atlassian.crowd.embedded.api.User) null)).andReturn(issue3);

        final JqlIssueKeySupport keySupport = mockController.getMock(JqlIssueKeySupport.class);
        expect(keySupport.parseKeyNum(keyValue1)).andReturn(243920849L).times(2);

        expect(keySupport.parseKeyNum(keyValue2)).andReturn(555544447L);

        replay();

        final IssueIdClauseQueryFactory idClauseQueryFactory = new IssueIdClauseQueryFactory(jqlOperandResolver, issueKeySupport, issueSupport);
        final QueryFactoryResult query = idClauseQueryFactory.getQuery(queryCreationContext, clause);

        final BooleanQuery expectedQuery = new BooleanQuery();

        BooleanQuery subQuery = new BooleanQuery();
        subQuery.add(new TermRangeQuery("keynumpart_range", null, "000000004182j5", true, true), BooleanClause.Occur.MUST);
        subQuery.add(new TermQuery(new Term("projid", "78")), BooleanClause.Occur.MUST);

        expectedQuery.add(subQuery, BooleanClause.Occur.SHOULD);

        subQuery = new BooleanQuery();
        subQuery.add(new TermRangeQuery("keynumpart_range", null, "000000004182j5", true, true), BooleanClause.Occur.MUST);
        subQuery.add(new TermQuery(new Term("projid", "23")), BooleanClause.Occur.MUST);

        expectedQuery.add(subQuery, BooleanClause.Occur.SHOULD);

        subQuery = new BooleanQuery();
        subQuery.add(new TermRangeQuery("keynumpart_range", null, "0000000096r8u7", true, true), BooleanClause.Occur.MUST);
        subQuery.add(new TermQuery(new Term("projid", "4")), BooleanClause.Occur.MUST);

        expectedQuery.add(subQuery, BooleanClause.Occur.SHOULD);

        assertEquals(expectedQuery, query.getLuceneQuery());
        assertFalse(query.mustNotOccur());

        verify();
    }

    @Test
    public void testGreaterThanMultipleLiterals() throws Exception
    {
        final String fieldName = "testGreaterThanMultipleLiterals";
        final String keyValue = "KEY-4527549837489534";
        final String keyValue2 = "KEY-7";

        final SingleValueOperand singleValueOperand = new SingleValueOperand("value");
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.GREATER_THAN, singleValueOperand);

        final MockIssue issue = new MockIssue(1L);
        issue.setKey(keyValue);
        issue.setProjectObject(new MockProject(78));

        final MockIssue issue2 = new MockIssue(1L);
        issue2.setKey(keyValue2);
        issue2.setProjectObject(new MockProject(23));

        jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        expect(jqlOperandResolver.getValues(queryCreationContext, singleValueOperand, clause)).andReturn(
            list(createLiteral(keyValue), createLiteral(keyValue2), new QueryLiteral()));
        expect(jqlOperandResolver.isListOperand(singleValueOperand)).andReturn(false);

        final JqlIssueSupport issueSupport = mockController.getMock(JqlIssueSupport.class);
        expect(issueSupport.getIssues(keyValue, (com.atlassian.crowd.embedded.api.User) null)).andReturn(CollectionBuilder.<Issue> list(issue));
        expect(issueSupport.getIssues(keyValue2, (com.atlassian.crowd.embedded.api.User) null)).andReturn(CollectionBuilder.<Issue> list(issue2));

        final JqlIssueKeySupport keySupport = mockController.getMock(JqlIssueKeySupport.class);
        expect(keySupport.parseKeyNum(keyValue)).andReturn(4527549837489534L);
        expect(keySupport.parseKeyNum(keyValue2)).andReturn(7L);

        replay();

        final IssueIdClauseQueryFactory idClauseQueryFactory = new IssueIdClauseQueryFactory(jqlOperandResolver, issueKeySupport, issueSupport);
        final QueryFactoryResult query = idClauseQueryFactory.getQuery(queryCreationContext, clause);

        final BooleanQuery expectedQuery = new BooleanQuery();

        BooleanQuery subQuery = new BooleanQuery();
        subQuery.add(new TermRangeQuery("keynumpart_range", "00018kvrojd9q6", null, false, true), BooleanClause.Occur.MUST);
        subQuery.add(new TermQuery(new Term("projid", "78")), BooleanClause.Occur.MUST);

        expectedQuery.add(subQuery, BooleanClause.Occur.SHOULD);

        subQuery = new BooleanQuery();
        subQuery.add(new TermRangeQuery("keynumpart_range", "00000000000007", null, false, true), BooleanClause.Occur.MUST);
        subQuery.add(new TermQuery(new Term("projid", "23")), BooleanClause.Occur.MUST);

        expectedQuery.add(subQuery, BooleanClause.Occur.SHOULD);

        assertEquals(expectedQuery, query.getLuceneQuery());
        assertFalse(query.mustNotOccur());

        verify();
    }

    @Test
    public void testGreaterThanSingleEmptyValue() throws Exception
    {
        final String fieldName = "greaterThan";
        final Operand operand = EmptyOperand.EMPTY;
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.GREATER_THAN, operand);

        replay();

        final IssueIdClauseQueryFactory idClauseQueryFactory = new IssueIdClauseQueryFactory(jqlOperandResolver, issueKeySupport, issueSupport);
        final QueryFactoryResult query = idClauseQueryFactory.getQuery(queryCreationContext, clause);

        final BooleanQuery expectedQuery = new BooleanQuery();

        assertEquals(expectedQuery, query.getLuceneQuery());
        assertFalse(query.mustNotOccur());

        verify();
    }

    @Test
    public void testGreaterThanEqualsWithId() throws Exception
    {
        final String fieldName = "testGreaterThanEqualsWithId";
        final String keyValue = "KEY-4527549837489534";
        final long issueId = 3484L;

        final SingleValueOperand singleValueOperand = new SingleValueOperand("value");
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.GREATER_THAN_EQUALS, singleValueOperand);

        final MockIssue issue1 = new MockIssue(1L);
        issue1.setKey(keyValue);
        issue1.setProjectObject(new MockProject(78));

        final MockIssue issue2 = new MockIssue(2L);
        issue2.setKey(keyValue);
        issue2.setProjectObject(new MockProject(29));

        jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        expect(jqlOperandResolver.getValues(queryCreationContext, singleValueOperand, clause)).andReturn(
            list(createLiteral(keyValue), createLiteral(issueId)));
        expect(jqlOperandResolver.isListOperand(singleValueOperand)).andReturn(false);

        final JqlIssueSupport issueSupport = mockController.getMock(JqlIssueSupport.class);
        expect(issueSupport.getIssues(keyValue, (com.atlassian.crowd.embedded.api.User) null)).andReturn(CollectionBuilder.<Issue> list(issue1));

        expect(issueSupport.getIssue(issueId, (com.atlassian.crowd.embedded.api.User) null)).andReturn(issue2);

        final JqlIssueKeySupport keySupport = mockController.getMock(JqlIssueKeySupport.class);
        expect(keySupport.parseKeyNum(keyValue)).andReturn(4527549837489534L).times(2);

        replay();

        final IssueIdClauseQueryFactory idClauseQueryFactory = new IssueIdClauseQueryFactory(jqlOperandResolver, issueKeySupport, issueSupport);
        final QueryFactoryResult query = idClauseQueryFactory.getQuery(queryCreationContext, clause);

        final BooleanQuery expectedQuery = new BooleanQuery();
        BooleanQuery subQuery = new BooleanQuery();
        subQuery.add(new TermRangeQuery("keynumpart_range", "00018kvrojd9q6", null, true, true), BooleanClause.Occur.MUST);
        subQuery.add(new TermQuery(new Term("projid", "78")), BooleanClause.Occur.MUST);
        expectedQuery.add(subQuery, BooleanClause.Occur.SHOULD);

        subQuery = new BooleanQuery();
        subQuery.add(new TermRangeQuery("keynumpart_range", "00018kvrojd9q6", null, true, true), BooleanClause.Occur.MUST);
        subQuery.add(new TermQuery(new Term("projid", "29")), BooleanClause.Occur.MUST);
        expectedQuery.add(subQuery, BooleanClause.Occur.SHOULD);

        assertEquals(expectedQuery, query.getLuceneQuery());
        assertFalse(query.mustNotOccur());

        verify();
    }

    @Test
    public void testGreaterThanEqualsWithIdOverrideSecurity() throws Exception
    {
        queryCreationContext = new QueryCreationContextImpl(theUser, true);

        final String fieldName = "testGreaterThanEqualsWithId";
        final String keyValue = "KEY-4527549837489534";
        final long issueId = 3484L;

        final SingleValueOperand singleValueOperand = new SingleValueOperand(3484L);
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.GREATER_THAN_EQUALS, singleValueOperand);

        final MockIssue issue2 = new MockIssue(2L);
        issue2.setKey(keyValue);
        issue2.setProjectObject(new MockProject(29));

        final JqlIssueSupport issueSupport = mockController.getMock(JqlIssueSupport.class);

        expect(issueSupport.getIssue(issueId)).andReturn(issue2);

        final JqlIssueKeySupport keySupport = mockController.getMock(JqlIssueKeySupport.class);
        expect(keySupport.parseKeyNum(keyValue)).andReturn(4527549837489534L);

        replay();

        final IssueIdClauseQueryFactory idClauseQueryFactory = new IssueIdClauseQueryFactory(jqlOperandResolver, issueKeySupport, issueSupport);
        final QueryFactoryResult query = idClauseQueryFactory.getQuery(queryCreationContext, clause);

        final BooleanQuery expectedQuery = new BooleanQuery();
        expectedQuery.add(new TermRangeQuery("keynumpart_range", "00018kvrojd9q6", null, true, true), BooleanClause.Occur.MUST);
        expectedQuery.add(new TermQuery(new Term("projid", "29")), BooleanClause.Occur.MUST);

        assertEquals(expectedQuery, query.getLuceneQuery());
        assertFalse(query.mustNotOccur());

        verify();
    }

    @Test
    public void testListTypeWithRelationalOperator() throws Exception
    {
        final Operand operand = new MultiValueOperand("value", "value2");
        final TerminalClauseImpl clause = new TerminalClauseImpl("dontCare", Operator.LESS_THAN, operand);

        replay();

        final IssueIdClauseQueryFactory idClauseQueryFactory = new IssueIdClauseQueryFactory(jqlOperandResolver, issueKeySupport, issueSupport);
        final QueryFactoryResult query = idClauseQueryFactory.getQuery(queryCreationContext, clause);

        assertEquals(new BooleanQuery(), query.getLuceneQuery());
        assertFalse(query.mustNotOccur());

        verify();
    }

    @Test
    public void testBadOperator() throws Exception
    {
        final SingleValueOperand singleValueOperand = new SingleValueOperand("value");
        final TerminalClauseImpl clause = new TerminalClauseImpl("dontCare", Operator.LIKE, singleValueOperand);

        replay();

        final IssueIdClauseQueryFactory idClauseQueryFactory = new IssueIdClauseQueryFactory(jqlOperandResolver, issueKeySupport, issueSupport);
        final QueryFactoryResult query = idClauseQueryFactory.getQuery(queryCreationContext, clause);

        assertEquals(new BooleanQuery(), query.getLuceneQuery());
        assertFalse(query.mustNotOccur());

        verify();
    }
}
