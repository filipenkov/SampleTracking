package com.atlassian.jira.jql.query;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.jql.operand.QueryLiteral;
import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import com.atlassian.jira.jql.util.JqlDateSupport;
import com.atlassian.jira.util.LuceneUtils;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.operator.Operator;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * @since v4.0
 */
public class TestDateRelationalQueryFactory extends MockControllerTestCase
{
    private static final Date A_DATE_LOWER = createDate(2005, 11, 25, 0);
    private static final Date A_DATE_UPPER = createDate(2005, 11, 26, 0);
    private static final String A_DATE_LOWER_STRING = LuceneUtils.dateToString(A_DATE_LOWER);
    private static final String A_DATE_UPPER_STRING = LuceneUtils.dateToString(A_DATE_UPPER);
    private JqlDateSupport dateSupport;

    @Before
    public void setUp() throws Exception
    {
        dateSupport = mockController.getMock(JqlDateSupport.class);
    }

    @Test
    public void testCreateQueryForSingleValueEmpty() throws Exception
    {
        final DateRelationalQueryFactory factory = new DateRelationalQueryFactory(dateSupport);
        mockController.replay();

        final QueryFactoryResult query = factory.createQueryForSingleValue("dateField", Operator.LESS_THAN, CollectionBuilder.newBuilder(new QueryLiteral()).asList());
        assertNotNull(query);
        assertEquals("", query.getLuceneQuery().toString(""));
        assertFalse(query.mustNotOccur());

        mockController.verify();
    }

    @Test
    public void testCreateQueryForSingleValueNoDates() throws Exception
    {
        final List<QueryLiteral> literals = CollectionBuilder.newBuilder(createLiteral("Value1")).asList();

        dateSupport.convertToDate("Value1");
        mockController.setReturnValue(null);
        mockController.replay();

        final DateRelationalQueryFactory factory = new DateRelationalQueryFactory(dateSupport);

        final QueryFactoryResult result = factory.createQueryForSingleValue("dateField", Operator.LESS_THAN, literals);
        assertEquals(QueryFactoryResult.createFalseResult(), result);

        mockController.verify();
    }

    @Test
    public void testLessThanWithLong() throws Exception
    {
        dateSupport.convertToDate(1000L);
        mockController.setReturnValue(A_DATE_LOWER);
        dateSupport.getIndexedValue(A_DATE_LOWER);
        mockController.setReturnValue(A_DATE_LOWER_STRING);

        final DateRelationalQueryFactory factory = new DateRelationalQueryFactory(dateSupport);
        mockController.replay();

        final QueryFactoryResult query = factory.createQueryForSingleValue("dateField", Operator.LESS_THAN, CollectionBuilder.list(createLiteral(1000L)));
        assertNotNull(query);
        assertEquals("dateField:[* TO 20051124130000}", query.getLuceneQuery().toString(""));

        mockController.verify();
    }

    @Test
    public void testDoesNotSupportOperators() throws Exception
    {
        mockController.replay();

        _testDoesNotSupportOperator(Operator.EQUALS);
        _testDoesNotSupportOperator(Operator.NOT_EQUALS);
        _testDoesNotSupportOperator(Operator.LIKE);
        _testDoesNotSupportOperator(Operator.IN);
        _testDoesNotSupportOperator(Operator.IS);

        mockController.verify();
    }

    @Test
    public void testCreateForMultipleValues() throws Exception
    {
        mockController.replay();

        final DateRelationalQueryFactory factory = new DateRelationalQueryFactory(dateSupport);

        final QueryFactoryResult result = factory.createQueryForMultipleValues("testField", Operator.IN, CollectionBuilder.newBuilder(createLiteral(1000L)).asList());
        assertEquals(QueryFactoryResult.createFalseResult(), result);

        mockController.verify();
    }

    @Test
    public void testCreateForEmptyOperand() throws Exception
    {
        mockController.replay();

        final DateRelationalQueryFactory factory = new DateRelationalQueryFactory(dateSupport);

        final QueryFactoryResult result = factory.createQueryForEmptyOperand("testField", Operator.IN);
        assertEquals(QueryFactoryResult.createFalseResult(), result);

        mockController.verify();
    }

    private void _testDoesNotSupportOperator(Operator operator) throws Exception
    {
        final DateRelationalQueryFactory factory = new DateRelationalQueryFactory(dateSupport);

        final QueryFactoryResult result = factory.createQueryForSingleValue("testField", operator, CollectionBuilder.newBuilder(createLiteral(1000L)).asList());
        assertEquals(QueryFactoryResult.createFalseResult(), result);
    }

    @Test
    public void testLessThan() throws Exception
    {
        dateSupport.convertToDate("Value1");
        mockController.setReturnValue(A_DATE_LOWER);
        dateSupport.getIndexedValue(A_DATE_LOWER);
        mockController.setReturnValue(A_DATE_LOWER_STRING);

        assertQueryHappy(Operator.LESS_THAN, "dateField:[* TO 20051124130000}");

        mockController.verify();
    }

    @Test
    public void testLessThanEquals() throws Exception
    {
        dateSupport.convertToDate("Value1");
        mockController.setReturnValue(A_DATE_LOWER);
        dateSupport.getIndexedValue(A_DATE_LOWER);
        mockController.setReturnValue(A_DATE_UPPER_STRING);

        assertQueryHappy(Operator.LESS_THAN_EQUALS, "dateField:[* TO 20051125130000]");

        mockController.verify();
    }

    @Test
    public void testGreaterThan() throws Exception
    {
        dateSupport.convertToDate("Value1");
        mockController.setReturnValue(A_DATE_LOWER);
        dateSupport.getIndexedValue(A_DATE_LOWER);
        mockController.setReturnValue(A_DATE_UPPER_STRING);

        assertQueryHappy(Operator.GREATER_THAN, "dateField:{20051125130000 TO *]");

        mockController.verify();
    }

    @Test
    public void testGreaterThanEquals() throws Exception
    {
        dateSupport.convertToDate("Value1");
        mockController.setReturnValue(A_DATE_LOWER);
        dateSupport.getIndexedValue(A_DATE_LOWER);
        mockController.setReturnValue(A_DATE_LOWER_STRING);

        assertQueryHappy(Operator.GREATER_THAN_EQUALS, "dateField:[20051124130000 TO *]");

        mockController.verify();
    }

    private void assertQueryHappy(Operator op, String luceneQuery)
    {
        final DateRelationalQueryFactory factory = new DateRelationalQueryFactory(dateSupport);
        mockController.replay();

        final QueryFactoryResult query = factory.createQueryForSingleValue("dateField", op, CollectionBuilder.newBuilder(createLiteral("Value1")).asList());
        assertNotNull(query);
        assertEquals(luceneQuery, query.getLuceneQuery().toString(""));
    }

    protected static Date createDate(int year, int month, int day, int hour)
    {
        // Note: hardcoded timezone for unit tests to be consistent in all environments
        final Calendar expectedCal = Calendar.getInstance(TimeZone.getTimeZone("Australia/Sydney"));
        expectedCal.set(year, month - 1, day, hour, 0, 0);
        expectedCal.set(Calendar.MILLISECOND, 0);
        return expectedCal.getTime();
    }
}
