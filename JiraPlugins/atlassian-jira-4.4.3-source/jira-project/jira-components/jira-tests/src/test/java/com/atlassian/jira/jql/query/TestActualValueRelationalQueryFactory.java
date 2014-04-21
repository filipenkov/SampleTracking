package com.atlassian.jira.jql.query;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.jql.operand.QueryLiteral;
import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import com.atlassian.jira.jql.util.JqlDurationSupportImpl;
import com.atlassian.query.operator.Operator;
import com.atlassian.jira.local.ListeningTestCase;

import java.util.Collections;

/**
 * @since v4.0
 */
public class TestActualValueRelationalQueryFactory extends ListeningTestCase
{
    private static final String FIELD_NAME = "durationField";

    @Test
    public void testDoesNotSupportOperators() throws Exception
    {
        _testDoesNotSupportOperator(Operator.EQUALS);
        _testDoesNotSupportOperator(Operator.NOT_EQUALS);
        _testDoesNotSupportOperator(Operator.LIKE);
        _testDoesNotSupportOperator(Operator.IN);
        _testDoesNotSupportOperator(Operator.IS);
    }

    private void _testDoesNotSupportOperator(Operator operator) throws Exception
    {
        final ActualValueRelationalQueryFactory factory = createFactory(FIELD_NAME);

        final QueryFactoryResult result = factory.createQueryForSingleValue("testField", operator, Collections.singletonList(createLiteral(1000L)));
        assertEquals(QueryFactoryResult.createFalseResult(), result);
    }

    @Test
    public void testCreateForMultipleValues() throws Exception
    {
        final ActualValueRelationalQueryFactory factory = createFactory(FIELD_NAME);
        final QueryFactoryResult result = factory.createQueryForMultipleValues("testField", Operator.IN, Collections.singletonList(createLiteral(1000L)));
        QueryFactoryResult expectedResult = QueryFactoryResult.createFalseResult();
        assertEquals(expectedResult, result);
    }

    @Test
    public void testCreateForEmptyOperand() throws Exception
    {
        final ActualValueRelationalQueryFactory factory = createFactory(FIELD_NAME);
        final QueryFactoryResult result = factory.createQueryForEmptyOperand("testField", Operator.IN);
        QueryFactoryResult expectedResult = QueryFactoryResult.createFalseResult();
        assertEquals(expectedResult, result);
    }

    @Test
    public void testCreateForSingleValueEmptyLiteral() throws Exception
    {
        final ActualValueRelationalQueryFactory factory = createFactory(FIELD_NAME);
        final QueryFactoryResult result = factory.createQueryForSingleValue("testField", Operator.LESS_THAN, Collections.singletonList(new QueryLiteral()));
        QueryFactoryResult expectedResult = QueryFactoryResult.createFalseResult();
        assertEquals(expectedResult, result);
    }

    @Test
    public void testCreateForSingleValueNoIndexValues() throws Exception
    {
        final ActualValueRelationalQueryFactory factory = new ActualValueRelationalQueryFactory(new JqlDurationSupportImpl()
        {
            @Override
            public String convertToIndexValue(final Long durationInMinutes)
            {
                return null;
            }

            @Override
            public String convertToIndexValue(final String durationString)
            {
                return null;
            }
        }, FIELD_NAME);
        final QueryFactoryResult result = factory.createQueryForSingleValue("testField", Operator.LESS_THAN, Collections.singletonList(createLiteral("XX")));
        QueryFactoryResult expectedResult = QueryFactoryResult.createFalseResult();
        assertEquals(expectedResult, result);
    }

    @Test
    public void testLessThanWithEmptyIndexValue() throws Exception
    {
        assertQueryHappy("-1", Operator.LESS_THAN, "+durationField:[* TO 1800} -durationField:-1", 30L);
        assertQueryHappy("-1", Operator.LESS_THAN, "+durationField:[* TO 1800} -durationField:-1", "30");
        assertQueryHappy("-1", Operator.LESS_THAN, "+durationField:[* TO -1800} -durationField:-1", -30L);
        assertQueryHappy("-1", Operator.LESS_THAN, "+durationField:[* TO -1800} -durationField:-1", "-30");
    }

    @Test
    public void testLessThan() throws Exception
    {
        assertQueryHappy(Operator.LESS_THAN, "durationField:[* TO 1800}", 30L);
        assertQueryHappy(Operator.LESS_THAN, "durationField:[* TO 1800}", "30");
        assertQueryHappy(Operator.LESS_THAN, "durationField:[* TO -1800}", -30L);
        assertQueryHappy(Operator.LESS_THAN, "durationField:[* TO -1800}", "-30");
    }

    @Test
    public void testLessThanEquals() throws Exception
    {
        assertQueryHappy(Operator.LESS_THAN_EQUALS, "durationField:[* TO 1800]", 30L);
        assertQueryHappy(Operator.LESS_THAN_EQUALS, "durationField:[* TO 1800]", "30");
        assertQueryHappy(Operator.LESS_THAN_EQUALS, "durationField:[* TO -1800]", -30L);
        assertQueryHappy(Operator.LESS_THAN_EQUALS, "durationField:[* TO -1800]", "-30");
    }

    @Test
    public void testGreaterThan() throws Exception
    {
        assertQueryHappy(Operator.GREATER_THAN, "durationField:{1800 TO *]", 30L);
        assertQueryHappy(Operator.GREATER_THAN, "durationField:{1800 TO *]", "30");
        assertQueryHappy(Operator.GREATER_THAN, "durationField:{-1800 TO *]", -30L);
        assertQueryHappy(Operator.GREATER_THAN, "durationField:{-1800 TO *]", "-30");
    }

    @Test
    public void testGreaterThanEquals() throws Exception
    {
        assertQueryHappy(Operator.GREATER_THAN_EQUALS, "durationField:[1800 TO *]", 30L);
        assertQueryHappy(Operator.GREATER_THAN_EQUALS, "durationField:[1800 TO *]", "30");
        assertQueryHappy(Operator.GREATER_THAN_EQUALS, "durationField:[-1800 TO *]", -30L);
        assertQueryHappy(Operator.GREATER_THAN_EQUALS, "durationField:[-1800 TO *]", "-30");
    }

    private void assertQueryHappy(Operator op, String luceneQuery, final Object value)
    {
        assertQueryHappy(null, op, luceneQuery, value);
    }

    private void assertQueryHappy(final String emptyIndexValue, Operator op, String luceneQuery, final Object value)
    {
        final ActualValueRelationalQueryFactory factory = createFactory(emptyIndexValue);

        QueryFactoryResult query = factory.createQueryForSingleValue(FIELD_NAME, op, Collections.singletonList(createQL(value)));
        assertFalse(query.mustNotOccur());
        assertNotNull(query.getLuceneQuery());
        assertEquals(luceneQuery, query.getLuceneQuery().toString());
    }

    private QueryLiteral createQL(Object value)
    {
        if (value instanceof String)
        {
            return createLiteral((String) value);
        }
        else if (value instanceof Long)
        {
            return createLiteral((Long) value);
        }
        throw new IllegalArgumentException();
    }

    private static ActualValueRelationalQueryFactory createFactory(final String emptyIndexValue)
    {
        return new ActualValueRelationalQueryFactory(new JqlDurationSupportImpl()
        {
            @Override
            public String convertToIndexValue(final Long durationInMinutes)
            {
                return String.valueOf(durationInMinutes * 60);
            }

            @Override
            public String convertToIndexValue(final String durationString)
            {
                return convertToIndexValue(new Long(durationString));
            }
        }, emptyIndexValue);
    }
}
