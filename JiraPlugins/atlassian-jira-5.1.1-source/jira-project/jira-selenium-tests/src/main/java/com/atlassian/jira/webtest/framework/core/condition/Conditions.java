package com.atlassian.jira.webtest.framework.core.condition;

import com.atlassian.jira.util.Supplier;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;

import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notEmpty;
import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.atlassian.jira.util.lang.JiraStringUtils.asString;

/**
 * Utilities to create miscellaneous {@link TimedCondition}s.
 *
 * @since v4.2
 */
public final class Conditions
{
    private static final Logger log = Logger.getLogger(Conditions.class);
    
    private static final int DEFAULT_TIMEOUT = 100;


    private Conditions()
    {
        throw new AssertionError("No way");
    }

    /**
     * Return new timed condition that is a negation of <tt>condition</tt>.
     * 
     * @param condition condition to be negated
     * @return negated {@link TimedCondition} instance.
     */
    public static TimedCondition not(TimedCondition condition)
    {
        if (condition instanceof Not)
        {
            return asDecorator(condition).wrapped;
        }
        return new Not(condition);
    }

    /**
     * <p>
     * Return new combinable condition that is logical product of <tt>conditions</tt>.
     *
     * <p>
     * The resulting condition will have interval of the first condition
     * in the <tt>conditions</tt> array,
     *
     * @param conditions conditions to conjoin
     * @return product of <tt>conditions</tt>
     * @throws IllegalArgumentException if <tt>conditions</tt> array is <code>null</code> or empty
     *
     * @see TimedCondition#interval()
     */
    public static CombinableCondition and(TimedCondition... conditions)
    {
        return new And(conditions);
    }

    /**
     * <p>
     * Return new combinable condition that is logical product of <tt>conditions</tt>.
     *
     * <p>
     * The resulting condition will have interval of the first condition
     * in the <tt>conditions</tt> array,
     *
     * @param conditions conditions to conjoin
     * @return product of <tt>conditions</tt>
     * @throws IllegalArgumentException if <tt>conditions</tt> array is <code>null</code> or empty
     *
     * @see TimedCondition#interval()
     */
    public static CombinableCondition and(List<TimedCondition> conditions)
    {
        return and(conditions.toArray(new TimedCondition[conditions.size()]));
    }

    /**
     * <p>
     * Return new combinable condition that is logical sum of <tt>conditions</tt>.
     *
     * <p>
     * The resulting condition will have interval of the first condition
     * in the <tt>conditions</tt> array,
     *
     * @param conditions conditions to sum
     * @return logical sum of <tt>conditions</tt>
     * @throws IllegalArgumentException if <tt>conditions</tt> array is <code>null</code> or empty
     * 
     * @see TimedCondition#interval()
     */
    public static CombinableCondition or(TimedCondition... conditions)
    {
        return new Or(conditions);
    }

    /**
     * <p>
     * Return new combinable condition that is logical sum of <tt>conditions</tt>.
     *
     * <p>
     * The resulting condition will have interval of the first condition
     * in the <tt>conditions</tt> array,
     *
     * @param conditions conditions to sum
     * @return logical sum of <tt>conditions</tt>
     * @throws IllegalArgumentException if <tt>conditions</tt> array is <code>null</code> or empty
     *
     * @see TimedCondition#interval()
     */
    public static CombinableCondition or(List<TimedCondition> conditions)
    {
        return or(conditions.toArray(new TimedCondition[conditions.size()]));
    }


    /**
     * Condition that always returns <code>false<code>. Its interval will be equal to the default timeout.
     *
     * @param defaultTimeout default timeout
     * @return false condition
     */
    public static TimedCondition falseCondition(long defaultTimeout)
    {
        return new AbstractTimedCondition(defaultTimeout, defaultTimeout)
        {
            @Override
            public boolean now()
            {
                return false;
            }
        };
    }

    /**
     * Condition that always returns <code>false<code>, with default timeout of 100ms.
     *
     * @return false condition
     */
    public static TimedCondition falseCondition()
    {
        return falseCondition(DEFAULT_TIMEOUT);
    }

    // TODO true condition

    /**
     * <p>
     * Returns a condition that combines <tt>original</tt> and <tt>dependant</tt> in a manner that dependant condition
     * will only ever be retrieved if the <tt>original</tt> condition is <code>true</code>. This is useful
     * when dependant condition may only be retrieved given the original condition is <code>true</code>.
     *
     * <p>
     * The supplier for dependant condition is allowed to return <code>null</code> or throw exception if the
     * original condition returns false. But it <i>may not</i> do so given the original condition is <code>true</code>,
     * as this will lead to <code>NullPointerException</code> be raised or given exception be propagated by
     * this condition.
     *
     * @param original original condition
     * @param dependant supplier for dependant condition that will only be evaluated given the original condition
     * evaluates to <code>true</code>
     * @return new dependant condition
     */
    public static TimedCondition dependantCondition(TimedCondition original, Supplier<TimedCondition> dependant)
    {
        return new DependantCondition(original, dependant);
    }


    private static AbstractConditionDecorator asDecorator(TimedCondition condition)
    {
        return (AbstractConditionDecorator) condition;
    }


    /**
     * A timed condition that may be logically combined with others, by means of basic logical operations: 'and'/'or'. 
     *
     */
    public static interface CombinableCondition extends TimedCondition
    {
        /**
         * Combine <tt>other</tt> condition with this condition logical query, such that the resulting condition
         * represents a logical product of this condition and <tt>other</tt>.
         *
         * @param other condition to combine with this one
         * @return new combined 'and' condition
         */
        CombinableCondition and(TimedCondition other);

        /**
         * Combine <tt>other</tt> condition with this condition logical query, such that the resulting condition
         * represents a logical sum of this condition and <tt>other</tt>.
         *
         * @param other condition to combine with this one
         * @return new combined 'or' condition
         */
        CombinableCondition or(TimedCondition other);

    }

    
    private abstract static class AbstractConditionDecorator extends AbstractTimedCondition
    {
        protected final TimedCondition wrapped;

        public AbstractConditionDecorator(TimedCondition wrapped)
        {
            super(wrapped);
            this.wrapped = notNull("wrapped", wrapped);
        }
    }

    private abstract static class AbstractConditionsDecorator extends AbstractTimedCondition implements CombinableCondition
    {
        protected final TimedCondition[] conditions;

        public AbstractConditionsDecorator(TimedCondition... conditions)
        {
            super(notEmpty("conditions", conditions)[0]);
            this.conditions = conditions;
        }

        @Override
        public String toString()
        {
            StringBuilder answer = new StringBuilder(conditions.length * 20).append(getClass().getName()).append(":\n");
            for (TimedCondition condition : conditions)
            {
                answer.append(" -").append(condition.toString()).append('\n');
            }
            return answer.deleteCharAt(answer.length()-1).toString();
        }
    }

    private static class Not extends AbstractConditionDecorator
    {
        public Not(TimedCondition other)
        {
            super(other);
        }

        public boolean now()
        {
            return !wrapped.now();
        }

        @Override
        public String toString()
        {
            return asString("Negated: <", wrapped, ">");
        }
    }

    private static class And extends AbstractConditionsDecorator
    {
        public And(TimedCondition... conditions)
        {
            super(conditions);
        }

        And(TimedCondition[] somes, TimedCondition[] more)
        {
            super((TimedCondition[]) ArrayUtils.addAll(somes, more));
        }

        And(TimedCondition[] somes, TimedCondition oneMore)
        {
            super((TimedCondition[]) ArrayUtils.add(somes, oneMore));
        }

        public boolean now()
        {
            boolean result = true;
            for (TimedCondition condition : conditions)
            {
                result &= condition.now();
                if (!result)
                {
                    log.debug(asString("[And] Condition <",condition,"> returned false"));
                    break;
                }
            }
            return result;
        }

        @Override
        public CombinableCondition and(TimedCondition other)
        {
            if (other.getClass().equals(And.class))
            {
                return new And(this.conditions, ((And) other).conditions);
            }
            return new And(this.conditions, other);
        }

        @Override
        public CombinableCondition or(TimedCondition other)
        {
            if (other instanceof Or)
            {
                return ((Or)other).or(this);
            }
            return new Or(this, other);
        }
    }

    private static class Or extends AbstractConditionsDecorator
    {
        public Or(TimedCondition... conditions)
        {
            super(conditions);
        }

        Or(TimedCondition[] somes, TimedCondition[] more)
        {
            super((TimedCondition[]) ArrayUtils.addAll(somes, more));
        }

        Or(TimedCondition[] somes, TimedCondition oneMore)
        {
            super((TimedCondition[]) ArrayUtils.add(somes, oneMore));
        }

        public boolean now()
        {
            boolean result = false;
            for (TimedCondition condition : conditions)
            {
                result |= condition.now();
                if (result)
                {
                    break;
                }
                log.debug(asString("[Or] Condition <",condition,"> returned false"));
            }
            return result;
        }

        @Override
        public CombinableCondition and(TimedCondition other)
        {
            if (other instanceof And)
            {
                return ((And)other).and(this);
            }
            return new And(this, other);
        }

        @Override
        public CombinableCondition or(TimedCondition other)
        {
            if (other.getClass().equals(Or.class))
            {
                return new Or(this.conditions, ((Or) other).conditions);
            }
            return new Or(this.conditions, other);
        }
    }

    private static final class DependantCondition extends AbstractConditionDecorator
    {
        private final Supplier<TimedCondition> dependant;

        DependantCondition(TimedCondition original, Supplier<TimedCondition> dependant)
        {
            super(original);
            this.dependant = notNull("dependant", dependant);
        }

        @Override
        public boolean now()
        {
            return wrapped.now() && dependant.get().now();
        }

        @Override
        public String toString()
        {
            if (wrapped.now())
            {
                TimedCondition dep = dependant.get();
                return asString("DependantCondition[original=",wrapped,",dependant=",dep,"]");
            }
            return asString("DependantCondition[original=",wrapped,"]");
        }
    }

}
