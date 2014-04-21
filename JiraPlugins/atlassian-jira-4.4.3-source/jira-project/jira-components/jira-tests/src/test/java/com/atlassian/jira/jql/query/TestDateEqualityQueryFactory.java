package com.atlassian.jira.jql.query;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;

import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.util.JqlDateSupport;
import com.atlassian.jira.util.LuceneUtils;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.operator.Operator;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * @since v4.0
 */
public class TestDateEqualityQueryFactory extends MockControllerTestCase
{
    private static final Date A_DATE_LOWER = createDate(2005, 11, 25, 0);
    private static final String A_DATE_LOWER_STRING = LuceneUtils.dateToString(A_DATE_LOWER);
    private static final Date B_DATE_LOWER = createDate(2008, 12, 2, 5);
    private static final String B_DATE_LOWER_STRING = "imlow";
    private JqlDateSupport jqlDateSupport;

    @Before
    public void setUp() throws Exception
    {
        jqlDateSupport = mockController.getMock(JqlDateSupport.class);
    }

    @Test
    public void testEquals() throws Exception
    {
        final JqlDateSupport dateSupport = mockController.getMock(JqlDateSupport.class);
        dateSupport.convertToDate("Value1");
        mockController.setReturnValue(A_DATE_LOWER);
        dateSupport.getIndexedValue(A_DATE_LOWER);
        mockController.setReturnValue(A_DATE_LOWER_STRING);

        assertQueryHappy(dateSupport, Operator.EQUALS, "dateField:20051124130000");

        mockController.verify();
    }

    @Test
    public void testNotEquals() throws Exception
    {
        final JqlDateSupport dateSupport = mockController.getMock(JqlDateSupport.class);
        dateSupport.convertToDate("Value1");
        mockController.setReturnValue(A_DATE_LOWER);
        dateSupport.getIndexedValue(A_DATE_LOWER);
        mockController.setReturnValue(A_DATE_LOWER_STRING);

        assertQueryHappy(dateSupport, Operator.NOT_EQUALS, "dateField:[* TO 20051124130000} dateField:{20051124130000 TO *]");

        mockController.verify();
    }

    @Test
    public void testInMultiple() throws Exception
    {
        final JqlDateSupport dateSupport = mockController.getMock(JqlDateSupport.class);
        dateSupport.convertToDate("Value1");
        mockController.setReturnValue(A_DATE_LOWER);
        dateSupport.convertToDate("Value2");
        mockController.setReturnValue(B_DATE_LOWER);

        dateSupport.getIndexedValue(A_DATE_LOWER);
        mockController.setReturnValue(A_DATE_LOWER_STRING);

        dateSupport.getIndexedValue(B_DATE_LOWER);
        mockController.setReturnValue(B_DATE_LOWER_STRING);

        final DateEqualityQueryFactory factory = new DateEqualityQueryFactory(dateSupport);
        mockController.replay();
        final QueryFactoryResult query = factory.createQueryForMultipleValues("dateField", Operator.IN, CollectionBuilder.newBuilder(
            createLiteral("Value1"), createLiteral("Value2")).asList());
        assertNotNull(query);
        assertEquals("dateField:20051124130000 dateField:imlow", query.getLuceneQuery().toString(""));

        mockController.verify();
    }

    @Test
    public void testInSingle() throws Exception
    {
        final JqlDateSupport dateSupport = mockController.getMock(JqlDateSupport.class);
        dateSupport.convertToDate("Value1");
        mockController.setReturnValue(A_DATE_LOWER);

        dateSupport.getIndexedValue(A_DATE_LOWER);
        mockController.setReturnValue(A_DATE_LOWER_STRING);

        final DateEqualityQueryFactory factory = new DateEqualityQueryFactory(dateSupport);
        mockController.replay();

        final QueryFactoryResult query = factory.createQueryForMultipleValues("dateField", Operator.IN, CollectionBuilder.newBuilder(
            createLiteral("Value1")).asList());
        assertNotNull(query);
        assertEquals("dateField:20051124130000", query.getLuceneQuery().toString(""));

        mockController.verify();
    }

    @Test
    public void testNotInMultiple() throws Exception
    {
        final JqlDateSupport dateSupport = mockController.getMock(JqlDateSupport.class);
        dateSupport.convertToDate("Value1");
        mockController.setReturnValue(A_DATE_LOWER);
        dateSupport.convertToDate("Value2");
        mockController.setReturnValue(B_DATE_LOWER);

        dateSupport.getIndexedValue(A_DATE_LOWER);
        mockController.setReturnValue(A_DATE_LOWER_STRING);

        dateSupport.getIndexedValue(B_DATE_LOWER);
        mockController.setReturnValue(B_DATE_LOWER_STRING);

        final DateEqualityQueryFactory factory = new DateEqualityQueryFactory(dateSupport);
        mockController.replay();
        final QueryFactoryResult query = factory.createQueryForMultipleValues("dateField", Operator.NOT_IN, CollectionBuilder.newBuilder(
            createLiteral("Value1"), createLiteral("Value2")).asList());
        assertNotNull(query);
        assertFalse(query.mustNotOccur());
        assertEquals("+(dateField:[* TO 20051124130000} dateField:{20051124130000 TO *]) +(dateField:[* TO imlow} dateField:{imlow TO *])",
            query.getLuceneQuery().toString());

        mockController.verify();
    }

    @Test
    public void testNotInSingle() throws Exception
    {
        final JqlDateSupport dateSupport = mockController.getMock(JqlDateSupport.class);
        dateSupport.convertToDate("Value1");
        mockController.setReturnValue(A_DATE_LOWER);

        dateSupport.getIndexedValue(A_DATE_LOWER);
        mockController.setReturnValue(A_DATE_LOWER_STRING);

        final DateEqualityQueryFactory factory = new DateEqualityQueryFactory(dateSupport);
        mockController.replay();

        final QueryFactoryResult query = factory.createQueryForMultipleValues("dateField", Operator.NOT_IN, CollectionBuilder.newBuilder(
            createLiteral("Value1")).asList());
        assertNotNull(query);
        assertFalse(query.mustNotOccur());
        assertEquals("dateField:[* TO 20051124130000} dateField:{20051124130000 TO *]", query.getLuceneQuery().toString());

        mockController.verify();
    }

    @Test
    public void testSingleValueWithBadOperators() throws Exception
    {
        mockController.replay();

        _testSingleValueWithBadOperator(Operator.GREATER_THAN);
        _testSingleValueWithBadOperator(Operator.GREATER_THAN_EQUALS);
        _testSingleValueWithBadOperator(Operator.LESS_THAN);
        _testSingleValueWithBadOperator(Operator.LESS_THAN_EQUALS);
        _testSingleValueWithBadOperator(Operator.IN);
        _testSingleValueWithBadOperator(Operator.IS);

        mockController.verify();
    }

    private void _testSingleValueWithBadOperator(final Operator operator) throws Exception
    {
        final DateEqualityQueryFactory factory = new DateEqualityQueryFactory(jqlDateSupport);

        final QueryFactoryResult result = factory.createQueryForSingleValue("testField", operator, CollectionBuilder.newBuilder(
            createLiteral("Value1")).asList());
        assertEquals(QueryFactoryResult.createFalseResult(), result);
    }

    @Test
    public void testMultiValueWithBadOperators() throws Exception
    {
        mockController.replay();

        _testMultiValueWithBadOperator(Operator.GREATER_THAN);
        _testMultiValueWithBadOperator(Operator.GREATER_THAN_EQUALS);
        _testMultiValueWithBadOperator(Operator.LESS_THAN);
        _testMultiValueWithBadOperator(Operator.LESS_THAN_EQUALS);
        _testMultiValueWithBadOperator(Operator.EQUALS);
        _testMultiValueWithBadOperator(Operator.NOT_EQUALS);
        _testMultiValueWithBadOperator(Operator.IS);
        _testMultiValueWithBadOperator(Operator.IS_NOT);
        _testMultiValueWithBadOperator(Operator.LIKE);
        _testMultiValueWithBadOperator(Operator.NOT_LIKE);

        mockController.verify();
    }

    @Test
    public void testCreateQueryForSingleValueNoDates() throws Exception
    {
        final List<QueryLiteral> literals = CollectionBuilder.newBuilder(createLiteral("Value1")).asList();

        jqlDateSupport.convertToDate("Value1");
        mockController.setReturnValue(null);

        mockController.replay();

        final DateEqualityQueryFactory factory = new DateEqualityQueryFactory(jqlDateSupport);

        final QueryFactoryResult result = factory.createQueryForSingleValue("testField", Operator.EQUALS, literals);
        assertEquals(QueryFactoryResult.createFalseResult(), result);

        mockController.verify();
    }

    private void _testMultiValueWithBadOperator(final Operator operator) throws Exception
    {
        final DateEqualityQueryFactory factory = new DateEqualityQueryFactory(jqlDateSupport);

        final QueryFactoryResult result = factory.createQueryForMultipleValues("testField", operator, CollectionBuilder.newBuilder(
            createLiteral("Value1")).asList());
        assertEquals(QueryFactoryResult.createFalseResult(), result);
    }

    @Test
    public void testHandlesOperator() throws Exception
    {
        mockController.replay();

        final DateEqualityQueryFactory factory = new DateEqualityQueryFactory(jqlDateSupport);

        assertFalse(factory.handlesOperator(Operator.GREATER_THAN));
        assertFalse(factory.handlesOperator(Operator.GREATER_THAN_EQUALS));
        assertFalse(factory.handlesOperator(Operator.LESS_THAN));
        assertFalse(factory.handlesOperator(Operator.LESS_THAN_EQUALS));
        assertFalse(factory.handlesOperator(Operator.LIKE));
        assertFalse(factory.handlesOperator(Operator.NOT_LIKE));

        assertTrue(factory.handlesOperator(Operator.EQUALS));
        assertTrue(factory.handlesOperator(Operator.NOT_EQUALS));
        assertTrue(factory.handlesOperator(Operator.IS));
        assertTrue(factory.handlesOperator(Operator.IS_NOT));
        assertTrue(factory.handlesOperator(Operator.IN));
        assertTrue(factory.handlesOperator(Operator.NOT_IN));

        mockController.verify();
    }

    @Test
    public void testCreateQueryForEmptyOperand() throws Exception
    {
        mockController.replay();

        final DateEqualityQueryFactory equalityQueryFactory = new DateEqualityQueryFactory(jqlDateSupport);

        QueryFactoryResult emptyQuery = equalityQueryFactory.createQueryForEmptyOperand("test", Operator.EQUALS);
        assertFalse(emptyQuery.mustNotOccur());
        assertEquals("-nonemptyfieldids:test +visiblefieldids:test", emptyQuery.getLuceneQuery().toString());
        emptyQuery = equalityQueryFactory.createQueryForEmptyOperand("test", Operator.IS);
        assertFalse(emptyQuery.mustNotOccur());
        assertEquals("-nonemptyfieldids:test +visiblefieldids:test", emptyQuery.getLuceneQuery().toString());

        mockController.verify();
    }

    @Test
    public void testCreateQueryForEmptyOperandNotEmptyOperators() throws Exception
    {
        mockController.replay();

        final DateEqualityQueryFactory equalityQueryFactory = new DateEqualityQueryFactory(jqlDateSupport);
        QueryFactoryResult emptyQuery = equalityQueryFactory.createQueryForEmptyOperand("test", Operator.NOT_EQUALS);
        assertFalse(emptyQuery.mustNotOccur());
        assertEquals("nonemptyfieldids:test", emptyQuery.getLuceneQuery().toString());
        emptyQuery = equalityQueryFactory.createQueryForEmptyOperand("test", Operator.IS_NOT);
        assertFalse(emptyQuery.mustNotOccur());
        assertEquals("nonemptyfieldids:test", emptyQuery.getLuceneQuery().toString());

        mockController.verify();
    }

    @Test
    public void testCreateQueryForEmptyOperandBadOperator() throws Exception
    {
        mockController.replay();

        final DateEqualityQueryFactory equalityQueryFactory = new DateEqualityQueryFactory(jqlDateSupport);

        final QueryFactoryResult result = equalityQueryFactory.createQueryForEmptyOperand("test", Operator.LIKE);
        assertEquals(QueryFactoryResult.createFalseResult(), result);

        mockController.verify();
    }

    @Test
    public void testEqualsEmpty() throws Exception
    {
        final QueryLiteral literal = new QueryLiteral();

        final JqlDateSupport dateSupport = mockController.getMock(JqlDateSupport.class);

        mockController.replay();
        final DateEqualityQueryFactory factory = new DateEqualityQueryFactory(dateSupport);
        final QueryFactoryResult result = factory.createQueryForSingleValue("dateField", Operator.EQUALS, Collections.singletonList(literal));

        final Query expectedQuery = getEmptyQueryForField("dateField");

        assertEquals(expectedQuery, result.getLuceneQuery());
        assertFalse(result.mustNotOccur());

        mockController.verify();
    }

    @Test
    public void testNotEqualsEmpty() throws Exception
    {
        final QueryLiteral literal = new QueryLiteral();

        final JqlDateSupport dateSupport = mockController.getMock(JqlDateSupport.class);

        mockController.replay();
        final DateEqualityQueryFactory factory = new DateEqualityQueryFactory(dateSupport);
        final QueryFactoryResult result = factory.createQueryForSingleValue("dateField", Operator.NOT_EQUALS, Collections.singletonList(literal));

        final TermQuery expectedQuery = new TermQuery(new Term(DocumentConstants.ISSUE_NON_EMPTY_FIELD_IDS, "dateField"));

        assertEquals(expectedQuery, result.getLuceneQuery());
        assertFalse(result.mustNotOccur());

        mockController.verify();
    }

    @Test
    public void testInWithEmpty() throws Exception
    {
        final QueryLiteral emptyLiteral = new QueryLiteral();
        final QueryLiteral longLiteral = createLiteral(10L);

        final JqlDateSupport dateSupport = mockController.getMock(JqlDateSupport.class);
        dateSupport.convertToDate(10L);
        final Date date = new Date();
        mockController.setReturnValue(date);

        dateSupport.getIndexedValue(date);
        mockController.setReturnValue("10");

        mockController.replay();
        final DateEqualityQueryFactory factory = new DateEqualityQueryFactory(dateSupport);
        final QueryFactoryResult result = factory.createQueryForMultipleValues("dateField", Operator.IN, CollectionBuilder.newBuilder(emptyLiteral,
            longLiteral).asList());

        final BooleanQuery expectedQuery = new BooleanQuery();
        expectedQuery.add(getEmptyQueryForField("dateField"), BooleanClause.Occur.SHOULD);
        expectedQuery.add(new TermQuery(new Term("dateField", "10")), BooleanClause.Occur.SHOULD);

        assertEquals(expectedQuery, result.getLuceneQuery());
        assertFalse(result.mustNotOccur());

        mockController.verify();
    }

    @Test
    public void testInWithSingleEmpty() throws Exception
    {
        final QueryLiteral emptyLiteral = new QueryLiteral();

        final JqlDateSupport dateSupport = mockController.getMock(JqlDateSupport.class);

        mockController.replay();
        final DateEqualityQueryFactory factory = new DateEqualityQueryFactory(dateSupport);
        final QueryFactoryResult result = factory.createQueryForMultipleValues("dateField", Operator.IN,
            CollectionBuilder.newBuilder(emptyLiteral).asList());

        final Query expectedQuery = getEmptyQueryForField("dateField");

        assertEquals(expectedQuery, result.getLuceneQuery());
        assertFalse(result.mustNotOccur());

        mockController.verify();
    }

    @Test
    public void testNotInWithEmpty() throws Exception
    {
        final QueryLiteral emptyLiteral = new QueryLiteral();
        final QueryLiteral longLiteral = createLiteral(10L);

        final JqlDateSupport dateSupport = mockController.getMock(JqlDateSupport.class);
        dateSupport.convertToDate(10L);
        final Date date = new Date();
        mockController.setReturnValue(date);

        dateSupport.getIndexedValue(date);
        mockController.setReturnValue("10");

        mockController.replay();
        final DateEqualityQueryFactory factory = new DateEqualityQueryFactory(dateSupport);
        final QueryFactoryResult result = factory.createQueryForMultipleValues("dateField", Operator.NOT_IN, CollectionBuilder.newBuilder(
            emptyLiteral, longLiteral).asList());

        final BooleanQuery expectedQuery = new BooleanQuery();
        expectedQuery.add(new TermQuery(new Term(DocumentConstants.ISSUE_NON_EMPTY_FIELD_IDS, "dateField")), BooleanClause.Occur.SHOULD);

        final BooleanQuery combined = new BooleanQuery();
        combined.add(new TermRangeQuery("dateField", null, "10", true, false), BooleanClause.Occur.SHOULD);
        combined.add(new TermRangeQuery("dateField", "10", null, false, true), BooleanClause.Occur.SHOULD);

        expectedQuery.add(combined, BooleanClause.Occur.MUST);

        assertEquals(expectedQuery, result.getLuceneQuery());
        assertFalse(result.mustNotOccur());

        mockController.verify();
    }

    @Test
    public void testNotInWithSingleEmpty() throws Exception
    {
        final QueryLiteral emptyLiteral = new QueryLiteral();

        final JqlDateSupport dateSupport = mockController.getMock(JqlDateSupport.class);

        mockController.replay();
        final DateEqualityQueryFactory factory = new DateEqualityQueryFactory(dateSupport);
        final QueryFactoryResult result = factory.createQueryForMultipleValues("dateField", Operator.NOT_IN, CollectionBuilder.newBuilder(
            emptyLiteral).asList());

        final Query expectedQuery = new TermQuery(new Term(DocumentConstants.ISSUE_NON_EMPTY_FIELD_IDS, "dateField"));

        assertEquals(expectedQuery, result.getLuceneQuery());
        assertFalse(result.mustNotOccur());

        mockController.verify();
    }

    protected void assertQueryHappy(final JqlDateSupport support, final Operator op, final String luceneQuery)
    {
        final DateEqualityQueryFactory factory = new DateEqualityQueryFactory(support);
        mockController.replay();

        final QueryFactoryResult query = factory.createQueryForSingleValue("dateField", op,
            CollectionBuilder.newBuilder(createLiteral("Value1")).asList());
        assertFalse(query.mustNotOccur());
        assertNotNull(query.getLuceneQuery());
        assertEquals(luceneQuery, query.getLuceneQuery().toString(""));
    }

    protected static Date createDate(final int year, final int month, final int day, final int hour)
    {
        // Note: hardcoded timezone for unit tests to be consistent in all environments
        final Calendar expectedCal = Calendar.getInstance(TimeZone.getTimeZone("Australia/Sydney"));
        expectedCal.set(year, month - 1, day, hour, 0, 0);
        expectedCal.set(Calendar.MILLISECOND, 0);
        return expectedCal.getTime();
    }

    private BooleanQuery getEmptyQueryForField(final String field)
    {
        final TermQuery emptyTermQuery = new TermQuery(new Term(DocumentConstants.ISSUE_NON_EMPTY_FIELD_IDS, field));
        final TermQuery visibilityTermQuery = new TermQuery(new Term(DocumentConstants.ISSUE_VISIBLE_FIELD_IDS, field));
        final BooleanQuery expectedQuery = new BooleanQuery();
        expectedQuery.add(emptyTermQuery, BooleanClause.Occur.MUST_NOT);
        expectedQuery.add(visibilityTermQuery, BooleanClause.Occur.MUST);
        return expectedQuery;
    }

}
