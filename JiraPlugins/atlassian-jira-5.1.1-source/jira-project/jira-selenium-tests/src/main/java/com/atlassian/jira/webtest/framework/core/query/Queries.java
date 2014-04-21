package com.atlassian.jira.webtest.framework.core.query;

import com.atlassian.jira.util.Function;
import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;

import java.util.concurrent.TimeUnit;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.atlassian.jira.util.lang.JiraStringUtils.asString;


/**
 * Factory of useful {@link TimedQuery} implementations.
 *
 * @since v4.3
 */
public final class Queries
{
    private Queries()
    {
        throw new AssertionError("Don't instantiate me");
    }

    /**
     * <p>
     * Return a builder that will create new TimedQuery instance that will evaluate to the provided <tt>value</tt>
     * and only return, if the provided <tt>condition</tt> evaluates to <code>true</code>. The timeout characteristics
     * of the returned query will be the same as in <tt>condition</tt>, and may be customized by modifying the builder.
     *
     * <p>
     * A default {@link ExpirationHandler} instance provided to handle the case of expired query will be
     * {@link ExpirationHandler#RETURN_NULL}. Custom handlers may be provided to the builder if necessary.
     *
     * @param value value of the target query
     * @param condition underlying condition
     * @param <T> type of the query result
     * @return new conditional query
     */
    public static <T> ForValueBuilder<T> conditionalQuery(T value, TimedCondition condition)
    {
        return new ForValueBuilder<T>(value).condition(condition).evaluationInterval(condition.interval())
                .defaultTimeout(condition.defaultTimeout());
    }

    /**
     * <p>
     * Create new TimedQuery instance that will evaluate to the value provided by <tt>query</tt> and only return,
     * if the provided <tt>condition</tt> evaluates to <code>true</code>. The timeout characteristics of the
     * returned query will be the same as in <tt>query</tt>, and may be customized by modifying the builder.
     *
     * <p>
     * A default {@link ExpirationHandler} instance provided to handle the case of expired query will be
     * {@link ExpirationHandler#RETURN_NULL}. Custom handlers may be provided to the builder if necessary.
     *
     * @param query underlying query
     * @param condition underlying condition
     * @param <T> type of the query result
     * @return new conditional query
     */
    public static <T> ForQueryBuilder<T> conditionalQuery(TimedQuery<T> query, TimedCondition condition)
    {
        return new ForQueryBuilder<T>(query).condition(condition).evaluationInterval(condition.interval())
                .defaultTimeout(condition.defaultTimeout());
    }

    /**
     * Transform original query into a new timed query that has the same timing semantics (conditions of
     * return, default timeout, wait interval etc.), but returns value of new type transformed from the
     * original by the <tt>transformer</tt> function.
     *
     * @param original original query
     * @param transformer function transforming results of the query
     * @param <T> original query result type
     * @param <U> target query result type
     * @return new transformed query
     */
    public static <T,U> TimedQuery<U> transform(TimedQuery<T> original, Function<T,U> transformer)
    {
        return new TransformingQuery<T,U>(original, transformer);
    }

    public static class ForValueBuilder<V> extends AbstractConditionBasedTimedQueryBuilder<ForValueBuilder<V>,ConditionalQueryForValue<V>,V>
    {
        private final V value;

        @SuppressWarnings ({ "unchecked" })
        private ForValueBuilder(V value)
        {
            super((Class) ForValueBuilder.class);
            this.value = value;
        }


        @Override
        public ConditionalQueryForValue<V> build()
        {
            return new ConditionalQueryForValue<V>(this);
        }
    }

    public static class ForQueryBuilder<V> extends AbstractConditionBasedTimedQueryBuilder<ForQueryBuilder<V>,ConditionalQueryForQuery<V>,V>
    {
        private final TimedQuery<V> query;

        @SuppressWarnings ({ "unchecked" })
        private ForQueryBuilder(TimedQuery<V> query)
        {
            super((Class) ForQueryBuilder.class);
            this.query = notNull("query", query);
        }


        @Override
        public ConditionalQueryForQuery<V> build()
        {
            return new ConditionalQueryForQuery<V>(this);
        }
    }

    public static class ConditionalQueryForQuery<V> extends AbstractConditionBasedQuery<V>
    {
        private final TimedQuery<V> query;
        private final V substituteValue;

        private ConditionalQueryForQuery(ForQueryBuilder<V> builder)
        {
            super(builder.condition(), builder.defaultTimeout(), builder.evaluationInterval(), builder.expirationHandler());
            this.query = builder.query;
            this.substituteValue = builder.substituteValue();
        }

        @Override
        protected V evaluateNow()
        {
            return query.now();
        }

        @Override
        protected V substituteValue()
        {
            return substituteValue;
        }

        @Override
        public String toString()
        {
            return asString("ConditionalQueryForQuery[query=",query,",condition=",condition(),"]");
        }
    }

    public static class ConditionalQueryForValue<V> extends AbstractConditionBasedQuery<V>
    {
        private final V value;
        private final V substituteValue;

        private ConditionalQueryForValue(ForValueBuilder<V> builder)
        {
            super(builder.condition(), builder.defaultTimeout(), builder.evaluationInterval(), builder.expirationHandler());
            this.value = builder.value;
            this.substituteValue = builder.substituteValue();

        }

        @Override
        protected V evaluateNow()
        {
            return value;
        }

        @Override
        protected V substituteValue()
        {
            return substituteValue;
        }

        @Override
        public String toString()
        {
            return asString("ConditionalQueryForValue[value=",value,",condition=",condition(),"]");
        }
    }

    private static class TransformingQuery<V,W> implements TimedQuery<W>
    {
        private final TimedQuery<V> original;
        private final Function<V,W> transformer;

        TransformingQuery(TimedQuery<V> original, Function<V, W> transformer)
        {
            this.original = notNull("original", original);
            this.transformer = notNull("transformer", transformer);
        }

        @Override
        public W by(long timeout)
        {
            return transform(original.by(timeout));
        }

        public final W by(long timeout, TimeUnit unit)
        {
            return by(TimeUnit.MILLISECONDS.convert(timeout, unit));
        }

        @Override
        public W byDefaultTimeout()
        {
            return transform(original.byDefaultTimeout());
        }

        @Override
        public W now()
        {
            return transform(original.now());
        }

        private W transform(V input)
        {
            return transformer.get(input);
        }

        @Override
        public long interval()
        {
            return original.interval();
        }

        @Override
        public long defaultTimeout()
        {
            return original.defaultTimeout();
        }
    }
}
