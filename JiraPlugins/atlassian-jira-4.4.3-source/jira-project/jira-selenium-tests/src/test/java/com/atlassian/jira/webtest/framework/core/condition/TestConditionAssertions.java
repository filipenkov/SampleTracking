package com.atlassian.jira.webtest.framework.core.condition;

import com.atlassian.jira.webtest.framework.core.mock.MockCondition;
import org.junit.Test;

import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.by;
import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.byDefaultTimeout;
import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.isFalse;
import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.isTrue;
import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.now;
import static com.atlassian.jira.webtest.framework.core.TimedAssertions.assertThat;
import static junit.framework.Assert.assertEquals;

/**
 * Test case for {@link TestConditionAssertions}.
 *
 * @since v4.3
 */
public class TestConditionAssertions
{
    @Test
    public void shouldPassForPassingCondition()
    {
        assertThat(passingCondition(), isTrue().byDefaultTimeout());
        assertThat(passingCondition(), isTrue().by(1000));
        assertThat(passingCondition(), isTrue().now());
        assertThat(passingCondition(), byDefaultTimeout());
        assertThat(passingCondition(), by(1000));
        assertThat(passingCondition(), now());
    }

    @Test
    public void shouldProduceMeaningfulErrorMessage()
    {
        try
        {
            assertThat(failingCondition(), byDefaultTimeout());
            throw new IllegalStateException("Should fail");
        }
        catch(AssertionError e)
        {
            assertEquals("Condition <Failing Condition> failed by 500ms (default timeout)", e.getMessage());
        }
    }

    @Test
    public void shouldProduceMeaningfulErrorMessageForNegatedAssertion()
    {
        try
        {
            assertThat(passingCondition(), isFalse().byDefaultTimeout());
            throw new IllegalStateException("Should fail");
        }
        catch(AssertionError e)
        {
            assertEquals("Condition <Negated: <Passing Condition>> failed by 500ms (default timeout)", e.getMessage());
        }
    }


    private TimedCondition passingCondition()
    {
        return new MockCondition(true)
        {
            @Override
            public String toString()
            {
                return "Passing Condition";
            }
        };
    }

    private TimedCondition failingCondition()
    {
        return new MockCondition(false)
        {
            @Override
            public String toString()
            {
                return "Failing Condition";
            }
        };
    }

}
