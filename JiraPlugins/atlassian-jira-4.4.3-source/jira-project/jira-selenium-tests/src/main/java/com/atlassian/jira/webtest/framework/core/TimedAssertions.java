package com.atlassian.jira.webtest.framework.core;

import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.core.query.TimedQuery;

/**
 * Common assertions for timeout-based queries inheriting from
 * {@link com.atlassian.jira.webtest.framework.core.PollingQuery}.
 *
 * @see com.atlassian.jira.webtest.framework.core.PollingQuery
 * @see com.atlassian.jira.webtest.framework.core.query.TimedQuery
 * @see com.atlassian.jira.webtest.framework.core.condition.TimedCondition
 * @since v4.3
 */
public final class TimedAssertions
{
    private TimedAssertions() {
        throw new AssertionError("Don't instantiate me");
    }

    /**
     * <p>
     * Execute an <tt>assertion</tt> to check if a given <tt>condition</tt> is <code>true</code>, or <code>false<code>
     * by given timeout. This method will produce a default assertion failure message, consisting of name of the
     * condition (as returned by its <code>toString()</code> method) and the timeout that was used to evaluate it.
     *
     * <p>
     * The <tt>assertion</tt> argument is supposed to be created using condition builders supplied by static methods
     * of this class.
     *
     * @param condition condition to verify
     * @param assertion assertion to execute
     * @see ConditionAssertions.ConditionAssertion
     * @see ConditionAssertions#isTrue()
     * @see ConditionAssertions#isFalse()
     * @see ConditionAssertions#by(long)
     * @see ConditionAssertions#byDefaultTimeout()
     * @see ConditionAssertions#now()
     */
    public static void assertThat(TimedCondition condition, ConditionAssertions.ConditionAssertion assertion)
    {
        assertion.execute(condition);
    }


    /**
     * <p>
     * Execute an <tt>assertion</tt> to check if a given <tt>condition</tt> is <code>true</code>, or <code>false<code>
     * by given timeout, and with client-supplied custom <tt>message</tt>. The custom failure message will be prepended
     * to the default one, produced as described in
     * {@link #assertThat(com.atlassian.jira.webtest.framework.core.condition.TimedCondition, com.atlassian.jira.webtest.framework.core.ConditionAssertions.ConditionAssertion)}
     *
     * <p>
     * The <tt>assertion</tt> is supposed to be created using condition builders supplied by static methods of
     * {@link ConditionAssertions}.
     *
     * @param message message to include when the assertion fails
     * @param condition condition to verify
     * @param assertion assertion to execute
     * @see ConditionAssertions.ConditionAssertion
     * @see ConditionAssertions.ConditionAssertion
     * @see ConditionAssertions#isTrue()
     * @see ConditionAssertions#isFalse()
     * @see ConditionAssertions#by(long)
     * @see ConditionAssertions#byDefaultTimeout()
     * @see ConditionAssertions#now()
     */
    public static void assertThat(String message, TimedCondition condition, ConditionAssertions.ConditionAssertion assertion)
    {
        assertion.withMessage(message).execute(condition);
    }



    /**
     * <p>
     * Build an assertion to verify if result of given <tt>query</tt> fulfils certain conditions (involving timeout)
     * specified by given <tt>assertion</tt>.
     *
     * <p>
     * Assertion instances are meant to be created via static factory methods in {@link QueryAssertions} that create
     * {@link QueryAssertions.QueryAssertionBuilder}s (that may further be used to create target
     * {@link QueryAssertions.QueryAssertion} instances).
     *
     * @param query timed query to verify
     * @param assertion object specifying the assertion
     * @see QueryAssertions.QueryAssertionBuilder
     *
     */
    public static <T> void assertThat(TimedQuery<T> query, QueryAssertions.QueryAssertion<T> assertion)
    {
        assertion.execute(query);
    }


    /**
     * <p>
     * Build an assertion to verify if result of given <tt>query</tt> fulfils certain conditions (involving timeout)
     * specified by given <tt>assertion</tt>. Provide additional custom message to provide contextual failure messages.
     *
     * <p>
     * The <tt>message</tt> parameter will be prepended to the default failure message.
     *
     * <p>
     * Assertion instances are meant to be created via static factory methods in {@link QueryAssertions} that create
     * {@link QueryAssertions.QueryAssertionBuilder}s (that may further be used to create target
     * {@link QueryAssertions.QueryAssertion} instances).
     *
     * @param message custom failure message
     * @param query timed query to verify
     * @param assertion object specifying the assertion
     * @see QueryAssertions.QueryAssertionBuilder
     *
     */
    public static <T> void assertThat(String message, TimedQuery<T> query, QueryAssertions.QueryAssertion<T> assertion)
    {
        assertion.withMessage(message).execute(query);
    }
}


