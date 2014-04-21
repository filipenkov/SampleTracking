package com.atlassian.jira.webtest.framework.core;

import com.atlassian.jira.webtest.framework.core.condition.AbstractTimedCondition;
import com.atlassian.jira.webtest.framework.core.query.TimedQuery;
import net.jcip.annotations.NotThreadSafe;
import org.apache.commons.lang.StringUtils;

import java.util.concurrent.TimeUnit;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.atlassian.jira.util.dbc.NumberAssertions.greaterThan;
import static com.atlassian.jira.util.lang.JiraStringUtils.asString;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Common assertions for {@link com.atlassian.jira.webtest.framework.core.query.TimedQuery} with meaningful error messages.
 *
 * @since 4.3
 */
public final class QueryAssertions
{
    private QueryAssertions()
    {
        throw new AssertionError("Don't instantiate me");
    }


    /**
     * Query assertion builder for the 'is equal' assertion - to verify that a return value from any given timed
     * query is equal to an expected value.
     *
     * @param expected expected result value of the query 
     * @param <T> type of the return value
     * @return query assertion builder for 'is equal' assertions
     */
    public static <T> QueryAssertionBuilder<T> isEqual(T expected)
    {
        return new IsEqualAssertionBuilder<T>(expected);
    }


    /**
     * <p>
     * Query assertion builder for 'is not <code>null</code>' assertion, verifying that return values from timed
     * queries are non-<code>null</code>.
     *
     * <p>
     * This method will must be used in places, where the compiler is not able to infer the type of the timed query
     * returned value
     *
     * @param targetType class of the query return value
     * @param <T> type parameter of the query return value
     * @return new query assertion builder for 'not <code>null</code>' assertions
     * @see #isNotNull()
     */
    public static <T> QueryAssertionBuilder<T> isNotNull(Class<T> targetType)
    {
        return new IsNotNullAssertionBuilder<T>();
    }

    /**
     * <p>
     * Query assertion builder for 'is not <code>null</code>' assertion, verifying that return values from timed
     * queries are non-<code>null</code>.
     *
     * @param <T> type parameter of the query return value
     * @return new query assertion builder for 'not <code>null</code>' assertions
     * @see #isNotNull(Class)
     */
    public static <T> QueryAssertionBuilder<T> isNotNull()
    {
        return new IsNotNullAssertionBuilder<T>();
    }

    /**
     * <p>
     * Query assertion builder for 'is <code>null</code>' assertion, verifying that return values from timed
     * queries are <code>null</code>.
     *
     * <p>
     * This method will must be used in places, where the compiler is not able to infer the type of the timed query
     * returned value
     *
     * @param targetType class of the query return value
     * @param <T> type parameter of the query return value
     * @return new query assertion builder for 'is <code>null</code>' assertions
     * @see #isNotNull()
     */
    public static <T> QueryAssertionBuilder<T> isNull(Class<T> targetType)
    {
        return new IsNullAssertionBuilder<T>();
    }

    /**
     * <p>
     * Query assertion builder for 'is <code>null</code>' assertion, verifying that return values from timed
     * queries are <code>null</code>.
     *
     * @param <T> type parameter of the query return value
     * @return new query assertion builder for '<code>null</code>' assertions
     * @see #isNotNull(Class)
     */
    public static <T> QueryAssertionBuilder<T> isNull()
    {
        return new IsNullAssertionBuilder<T>();
    }

    /**
     * Used to build the target query assertion that is performed in one of the timeout modes:
     * <ul>
     * <li><code>now()</code> - immediately
     * <li><code>byDefaultTimeout()</code> - by the tested query's default timeout
     * <li><code>by(long)</code> - by client-supplied timeout
     * </ul>
     *
     * @param <V> type of the query's return value
     */
    public static interface QueryAssertionBuilder<V>
    {
        QueryAssertion<V> now();

        QueryAssertion<V> byDefaultTimeout();

        QueryAssertion<V> by(long timeout);

        QueryAssertion<V> by(long timeout, TimeUnit unit);
    }


    private static abstract class AbstractQueryAssertionBuilder<V> implements QueryAssertionBuilder<V>
    {
        @Override
        public QueryAssertion<V> by(long timeout, TimeUnit unit)
        {
            return by(unit.toMillis(timeout));
        }
    }


    private static final class IsEqualAssertionBuilder<V> extends AbstractQueryAssertionBuilder<V>
    {
        private final V expected;

        public IsEqualAssertionBuilder(V expected)
        {
            this.expected = expected;
        }

        @Override
        public QueryAssertion<V> now()
        {
            return new IsEqualNowAssertion<V>(expected);
        }

        @Override
        public QueryAssertion<V> byDefaultTimeout()
        {

            return new IsEqualByDefaultTimeoutAssertion<V>(expected);
        }

        @Override
        public QueryAssertion<V> by(long timeout)
        {
            return new IsEqualByCustomTimeoutAssertion<V>(expected, timeout);
        }
    }

    private static final class IsNotNullAssertionBuilder<V> extends AbstractQueryAssertionBuilder<V>
    {
        @Override
        public QueryAssertion<V> now()
        {
            return new IsNotNullNowAssertion<V>();
        }

        @Override
        public QueryAssertion<V> byDefaultTimeout()
        {

            return new IsNotNullByDefaultTimeoutAssertion<V>();
        }

        @Override
        public QueryAssertion<V> by(long timeout)
        {
            return new IsNotNullByCustomTimeoutAssertion<V>(timeout);
        }
    }

    private static final class IsNullAssertionBuilder<V> extends AbstractQueryAssertionBuilder<V>
    {
        @Override
        public QueryAssertion<V> now()
        {
            return new IsNullNowAssertion<V>();
        }

        @Override
        public QueryAssertion<V> byDefaultTimeout()
        {

            return new IsNullByDefaultTimeoutAssertion<V>();
        }

        @Override
        public QueryAssertion<V> by(long timeout)
        {
            return new IsNullByCustomTimeoutAssertion<V>(timeout);
        }
    }


    @NotThreadSafe
    public static abstract class QueryAssertion<V>
    {
        private String message;

        protected abstract void execute(TimedQuery<V> query);

        protected final String combineMsg(String defaultMsg)
        {
            return StringUtils.isNotEmpty(message) ? message + ": "  + defaultMsg : defaultMsg;
        }

        public QueryAssertion<V> withMessage(String msg)
        {
            this.message = msg;
            return this;
        }

    }

    private static abstract class AbstractIsEqualAssertion<V> extends QueryAssertion<V>
    {
        protected final V expected;

        protected AbstractIsEqualAssertion(V expected)
        {
            this.expected = notNull("expected", expected);
        }
    }

    private static class IsEqualNowAssertion<V> extends AbstractIsEqualAssertion<V>
    {
        IsEqualNowAssertion(V expected)
        {
            super(expected);
        }

        @Override
        protected void execute(TimedQuery<V> query)
        {
            V actual = query.now();
            if (!expected.equals(actual))
            {
                throw new AssertionError(combineMsg(defMessage(query, actual)));
            }
        }

        private String defMessage(TimedQuery<V> query, V actual)
        {
            return asString("Query <",query,">, expected immediate value <", expected, ">, but was <", actual, ">");
        }
    }

    private static class IsEqualByDefaultTimeoutAssertion<V> extends AbstractIsEqualAssertion<V>
    {
        IsEqualByDefaultTimeoutAssertion(V expected)
        {
            super(expected);
        }

        @Override
        protected void execute(TimedQuery<V> query)
        {
            TimedQueryEqualsCondition<V> equalsCondition = new TimedQueryEqualsCondition<V>(expected, query);
            if (!equalsCondition.byDefaultTimeout())
            {
                throw new AssertionError(combineMsg(defMessage(equalsCondition)));
            }
        }

        private String defMessage(TimedQueryEqualsCondition<V> equalsCondition)
        {
            return asString("Query <",equalsCondition.query,">, expected value <", expected, "> by ",
                    equalsCondition.query.defaultTimeout(), "ms (default timeout), but failed, with last recorded value <",
                    equalsCondition.lastActual, ">");
        }
    }

    private static class IsEqualByCustomTimeoutAssertion<V> extends AbstractIsEqualAssertion<V>
    {
        private final long timeout;

        IsEqualByCustomTimeoutAssertion(V expected, long timeout)
        {
            super(expected);
            this.timeout = greaterThan("timeout", timeout, 0);
        }

        @Override
        protected void execute(TimedQuery<V> query)
        {
            TimedQueryEqualsCondition<V> equalsCondition = new TimedQueryEqualsCondition<V>(expected, query);
            if (!equalsCondition.by(timeout))
            {
                throw new AssertionError(combineMsg(defMessage(equalsCondition)));
            }
        }

        private String defMessage(TimedQueryEqualsCondition<V> equalsCondition)
        {
            return asString("Query <",equalsCondition.query,">, expected value <", expected, "> by ",
                    timeout, "ms, but failed, with last recorded value <", equalsCondition.lastActual, ">");
        }
    }

    private static class IsNotNullNowAssertion<V> extends QueryAssertion<V>
    {

        @Override
        protected void execute(TimedQuery<V> query)
        {
            assertNotNull(combineMsg(defMessage(query)), query.now());
        }

        private String defMessage(TimedQuery<V> query)
        {
            return asString("Query <", query, "> expected to return non-null result immediately, but failed");
        }
    }

    private static class IsNotNullByDefaultTimeoutAssertion<V> extends QueryAssertion<V>
    {

        @Override
        protected void execute(TimedQuery<V> query)
        {
            final TimedQueryIsNotNullCondition<V> notNull = new TimedQueryIsNotNullCondition<V>(query);
            if (!notNull.byDefaultTimeout())
            {
                throw new AssertionError(combineMsg(defMessage(notNull)));
            }
        }

        private String defMessage(TimedQueryIsNotNullCondition<V> notNull)
        {
            return asString("Query <", notNull.query, "> expected to return non-null result by ",
                    notNull.defaultTimeout(), "ms (default timeout), but failed");
        }
    }

    private static class IsNotNullByCustomTimeoutAssertion<V> extends QueryAssertion<V>
    {
        private final long timeout;

        public IsNotNullByCustomTimeoutAssertion(long timeout)
        {
            this.timeout = timeout;
        }

        @Override
        protected void execute(TimedQuery<V> query)
        {
            final TimedQueryIsNotNullCondition<V> notNull = new TimedQueryIsNotNullCondition<V>(query);
            if (!notNull.by(timeout))
            {
                throw new AssertionError(combineMsg(defMessage(notNull)));
            }
        }

        private String defMessage(TimedQueryIsNotNullCondition<V> notNull)
        {
            return asString("Query <", notNull.query, "> expected to return non-null result by ", timeout,
                    "ms, but failed");
        }
    }

    private static class IsNullNowAssertion<V> extends QueryAssertion<V>
    {

        @Override
        protected void execute(TimedQuery<V> query)
        {
            V val = query.now();
            assertNull(combineMsg(defMessage(query, val)), val);
        }

        private String defMessage(TimedQuery<V> query, V val)
        {
            return asString("Query <", query, "> expected to return null immediately, but was <" + val + ">");
        }
    }

    private static class IsNullByDefaultTimeoutAssertion<V> extends QueryAssertion<V>
    {

        @Override
        protected void execute(TimedQuery<V> query)
        {
            final TimedQueryIsNullCondition<V> isNull = new TimedQueryIsNullCondition<V>(query);
            if (!isNull.byDefaultTimeout())
            {
                throw new AssertionError(combineMsg(defMessage(isNull)));
            }
        }

        private String defMessage(TimedQueryIsNullCondition<V> isNull)
        {
            return asString("Query <", isNull.query, "> expected to return null by ", isNull.defaultTimeout(),
                    "ms (default timeout), but failed, with last recorded value <", isNull.lastActual, ">");
        }
    }

    private static class IsNullByCustomTimeoutAssertion<V> extends QueryAssertion<V>
    {
        private final long timeout;

        public IsNullByCustomTimeoutAssertion(long timeout)
        {
            this.timeout = timeout;
        }

        @Override
        protected void execute(TimedQuery<V> query)
        {
            final TimedQueryIsNullCondition<V> notNull = new TimedQueryIsNullCondition<V>(query);
            if (!notNull.by(timeout))
            {
                throw new AssertionError(combineMsg(defMessage(notNull)));
            }
        }

        private String defMessage(TimedQueryIsNullCondition<V> isNull)
        {
            return asString("Query <", isNull.query, "> expected to return null by ", timeout, "ms, but failed, with "
                    + "last recorded value <", isNull.lastActual, ">");
        }
    }



    private static abstract class AbstractTimedQueryTestCondition<V> extends AbstractTimedCondition
    {
        protected final TimedQuery<V> query;
        protected V lastActual;

        public AbstractTimedQueryTestCondition(TimedQuery<V> query)
        {
            super(notNull("query", query));
            this.query = query;
        }

        @Override
        public final boolean now()
        {
            lastActual = query.now();
            return evaluate(lastActual);
        }

        protected abstract boolean evaluate(V lastRecordedValue);
    }

    private static class TimedQueryEqualsCondition<V> extends AbstractTimedQueryTestCondition<V>
    {
        private final V expectedValue;

        protected TimedQueryEqualsCondition(V expVal, TimedQuery<V> query)
        {
            super(query);
            this.expectedValue = notNull("expectedValue", expVal);
        }

        @Override
        protected boolean evaluate(V lastRecordedVal)
        {
            return expectedValue.equals(lastRecordedVal);
        }

        @Override
        public String toString()
        {
            return asString("Expected result <", expectedValue, "> from the timed query: ", query, ". Last recorded value",
                    " <", lastActual, ">");
        }
    }

    private static class TimedQueryIsNotNullCondition<V> extends AbstractTimedQueryTestCondition<V>
    {
        protected TimedQueryIsNotNullCondition(TimedQuery<V> query)
        {
            super(query);
        }

        @Override
        public boolean evaluate(V lastRecordedVal)
        {
            return lastRecordedVal != null;
        }

        @Override
        public String toString()
        {
            return asString("Timed query <", query, "> expected to return non-null value. Last recorded value",
                    " <", lastActual, ">");
        }
    }

    private static class TimedQueryIsNullCondition<V> extends AbstractTimedQueryTestCondition<V>
    {
        protected TimedQueryIsNullCondition(TimedQuery<V> query)
        {
            super(query);
        }

        @Override
        public boolean evaluate(V lastRecorded)
        {
            return lastRecorded == null;
        }

        @Override
        public String toString()
        {
            return asString("Timed query <", query, "> expected to return null value. Last recorded value",
                    " <", lastActual, ">");
        }
    }

}
