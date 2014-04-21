package com.atlassian.jira.webtest.framework.core;

import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import net.jcip.annotations.NotThreadSafe;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;

import java.util.concurrent.TimeUnit;

import static com.atlassian.jira.util.dbc.NumberAssertions.greaterThan;
import static com.atlassian.jira.util.lang.JiraStringUtils.asString;
import static com.atlassian.jira.webtest.framework.core.condition.Conditions.not;

/**
 * Utility to perform assertions with meaningful messages against {@link com.atlassian.jira.webtest.framework.core.condition.TimedCondition} and
 * {@link com.atlassian.jira.webtest.framework.core.query.TimedQuery} objects.
 *
 * @since v4.2
 */
public final class ConditionAssertions
{

    private ConditionAssertions()
    {
        throw new AssertionError("I want to be static!");
    }


    /**
     * Equivalent to:
     * <code>
     * TimedAssertions.assertThat(condition, isTrue().byDefaultTimeout));
     * </code>
     *
     * @param condition condition to execute assertion on
     */
    public static void assertTrueByDefaultTimeout(TimedCondition condition)
    {
        TimedAssertions.assertThat(condition, isTrue().byDefaultTimeout());
    }

    /**
     * Equivalent to:
     * <code>
     * TimedAssertions.assertThat(msg, condition, isTrue().byDefaultTimeout));
     * </code>
     *
     * @param msg custom assertion message
     * @param condition condition to execute assertion on
     */
    public static void assertTrueByDefaultTimeout(String msg, TimedCondition condition)
    {
        TimedAssertions.assertThat(msg, condition, isTrue().byDefaultTimeout());
    }

    /**
     * Equivalent to:
     * <code>
     * TimedAssertions.assertThat(condition, isFalse().byDefaultTimeout));
     * </code>
     *
     * @param condition condition to execute assertion on
     */
    public static void assertFalseByDefaultTimeout(TimedCondition condition)
    {
        TimedAssertions.assertThat(condition, isFalse().byDefaultTimeout());
    }

    /**
     * Equivalent to:
     * <code>
     * TimedAssertions.assertThat(msg, condition, isFalse().byDefaultTimeout));
     * </code>
     *
     * @param msg custom assertion message
     * @param condition condition to execute assertion on
     */
    public static void assertFalseByDefaultTimeout(String msg, TimedCondition condition)
    {
        TimedAssertions.assertThat(msg, condition, isFalse().byDefaultTimeout());
    }




    /**
     * <p>
     * Condition assertion builder to assert that a given condition is <code>true</code>. Use in the
     * <code>assertThat(...)</code> methods of this class to construct required
     * {@link ConditionAssertions.ConditionAssertion} objects.
     * Example usage:<br>
     * <code>
     * assertThat(someCondition(), isTrue().byDefaultTimeout());
     * assertThat(someCondition(), isTrue().by(1000));
     * assertThat(someCondition(), isTrue().now());
     * </code>
     * </p>
     *
     * @return condition assertion builder for <code>true</code> assertions.
     */
    public static ConditionAssertionBuilder isTrue()
    {
        return new TrueConditionAssertionBuilder();
    }

    /**
     * <p>
     * Condition assertion builder to assert that a given condition is <code>false</code>. Use in the
     * <code>assertThat(...)</code> methods of this class to construct required
     * {@link ConditionAssertions.ConditionAssertion} objects.
     * Example usage:<br>
     * <code>
     * assertThat(someCondition(), isFalse().byDefaultTimeout());
     * assertThat(someCondition(), isFalse().by(1000));
     * assertThat(someCondition(), isFalse().now());
     * </code>
     * </p>
     *
     * @return condition assertion builder for <code>false</code> assertions.
     */
    public static ConditionAssertionBuilder isFalse()
    {
        return new FalseConditionAssertionBuilder();
    }

    /**
     * Synonym for a call: <code>isTrue().now()</code>.
     *
     * @return assertion verifying that a given condition is <code>true</code> immediately.
     */
    public static ConditionAssertion now()
    {
        return isTrue().now();
    }

    /**
     * Synonym for a call: <code>isTrue().byDefaultTimeout()</code>.
     *
     * @return assertion verifying that a given condition is <code>true</code> before its default timeout expires.
     */
    public static ConditionAssertion byDefaultTimeout()
    {
        return isTrue().byDefaultTimeout();
    }

    /**
     * Synonym for a call: <code>isTrue().by(long)</code>.
     *
     * @param timeout assertion timeout
     * @return assertion verifying that a given condition is <code>true</code> before the supplied <tt>timeout</tt>
     * expires.
     */
    public static ConditionAssertion by(long timeout)
    {
        return isTrue().by(timeout);
    }
    

    /**
     * Synonym for a call: <code>isTrue().by(long, TimeUnit)</code>.
     *
     * @param timeout assertion timeout
     * @param unit time unit of the timeout
     * @return assertion verifying that a given condition is <code>true</code> before the supplied <tt>timeout</tt>
     * expires.
     */
    public static ConditionAssertion by(long timeout, TimeUnit unit)
    {
        return isTrue().by(timeout, unit);
    }

    /**
     * Builds condition assertions that are used as parameters to the <code>assertThat(...)</code> methods.
     *
     */
    public static interface ConditionAssertionBuilder
    {
        ConditionAssertion now();

        ConditionAssertion byDefaultTimeout();

        ConditionAssertion by(long millis);

        ConditionAssertion by(long timeout, TimeUnit unit);
    }


    @NotThreadSafe
    public static abstract class ConditionAssertion
    {
        private String message;

        ConditionAssertion()
        {
        }

        ConditionAssertion withMessage(String msg)
        {
            this.message = msg;
            return this;
        }


        protected abstract void execute(TimedCondition condition);


        protected final String combineMsg(String defaultMsg)
        {
            return StringUtils.isNotEmpty(message) ? message + ": "  + defaultMsg : defaultMsg;
        }

    }

    private static class TrueNowAssertion extends ConditionAssertion
    {

        @Override
        protected void execute(TimedCondition condition)
        {
            Assert.assertTrue(combineMsg(msg(condition)), condition.now());
        }

        protected String msg(TimedCondition condition)
        {
            return asString("Condition <", condition, "> failed immediate evaluation");
        }
    }

    private static class FalseNowAssertion extends TrueNowAssertion
    {
        @Override
        protected void execute(TimedCondition condition)
        {
            super.execute(not(condition));
        }
    }

    private static class TrueDefaultTimeoutAssertion extends ConditionAssertion
    {

        @Override
        protected void execute(TimedCondition condition)
        {
            Assert.assertTrue(combineMsg(msg(condition)), condition.byDefaultTimeout());
        }

        protected String msg(TimedCondition condition)
        {
            return  asString("Condition <",condition,"> failed by ",condition.defaultTimeout(),"ms (default timeout)");
        }
    }

    private static class FalseDefaultTimeoutAssertion extends TrueDefaultTimeoutAssertion
    {

        @Override
        protected void execute(TimedCondition condition)
        {
            super.execute(not(condition));
        }
    }

    private static class TrueCustomTimeoutAssertion extends ConditionAssertion
    {
        protected final long timeout;

        TrueCustomTimeoutAssertion(long timeout)
        {
            this.timeout = greaterThan("timeout", timeout, 0);
        }

        @Override
        protected void execute(TimedCondition condition)
        {
            Assert.assertTrue(combineMsg(msg(condition)), condition.by(timeout));
        }

        protected String msg(TimedCondition condition)
        {
            return  asString("Condition <",condition,"> failed by ",timeout,"ms");
        }
    }

    private static class FalseCustomTimeoutAssertion extends TrueCustomTimeoutAssertion
    {

        FalseCustomTimeoutAssertion(long timeout)
        {
            super(timeout);
        }

        @Override
        protected void execute(TimedCondition condition)
        {
            super.execute(not(condition));
        }
    }


    private static abstract class AbstractConditionAssertionBuilder implements ConditionAssertionBuilder
    {
        @Override
        public ConditionAssertion by(long timeout, TimeUnit unit)
        {
            return by(unit.toMillis(timeout));
        }
    }

    private static final class TrueConditionAssertionBuilder extends AbstractConditionAssertionBuilder
    {
        public ConditionAssertion now()
        {
            return new TrueNowAssertion();
        }


        public ConditionAssertion byDefaultTimeout()
        {
            return new TrueDefaultTimeoutAssertion();
        }

        @Override
        public ConditionAssertion by(long millis)
        {
            return new TrueCustomTimeoutAssertion(millis);
        }

    }

    private static final class FalseConditionAssertionBuilder extends AbstractConditionAssertionBuilder
    {
        public ConditionAssertion now()
        {
            return new FalseNowAssertion();
        }


        public ConditionAssertion byDefaultTimeout()
        {
            return new FalseDefaultTimeoutAssertion();
        }

        @Override
        public ConditionAssertion by(long millis)
        {
            return new FalseCustomTimeoutAssertion(millis);
        }
    }

}
