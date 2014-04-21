package com.atlassian.jira.webtest.framework.core.condition;

import com.atlassian.jira.util.StrictMockClock;
import com.atlassian.jira.webtest.framework.core.QueryAssertions;
import com.atlassian.jira.webtest.framework.core.mock.MockTimedQuery;
import com.atlassian.jira.webtest.framework.core.query.ExpirationHandler;
import com.atlassian.jira.webtest.framework.core.query.StaticQuery;
import com.atlassian.jira.webtest.framework.core.query.TimedQuery;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.atlassian.jira.webtest.framework.core.QueryAssertions.isEqual;
import static com.atlassian.jira.webtest.framework.core.QueryAssertions.isNotNull;
import static com.atlassian.jira.webtest.framework.core.QueryAssertions.isNull;
import static com.atlassian.jira.webtest.framework.core.TimedAssertions.assertThat;
import static org.junit.Assert.assertEquals;

/**
 * Test case for {@link com.atlassian.jira.webtest.framework.core.QueryAssertions}.
 *
 * @since 4.3
 */
public class TestQueryAssertions
{
    private static final int DEFAULT_INTERVAL = 100;

    @Test
    public void queryEqualsAssertionShouldPassForPassingQuery()
    {
        assertThat(queryFor("test"), isEqual("test").now());
        assertThat(queryFor("test"), isEqual("test").byDefaultTimeout());
        assertThat(queryFor("test"), isEqual("test").by(500));
    }

    @Test
    public void queryNotNullAssertionShouldPassForNotNullQuery()
    {
        assertThat(queryFor("test"), isNotNull(String.class).now());
        assertThat(queryFor("test"), isNotNull(String.class).byDefaultTimeout());
        assertThat(queryFor("test"), isNotNull(String.class).by(500));

        assertThat(queryFor("test"), QueryAssertions.<String>isNotNull().now());
        assertThat(queryFor("test"), QueryAssertions.<String>isNotNull().byDefaultTimeout());
        assertThat(queryFor("test"), QueryAssertions.<String>isNotNull().by(500));
    }

    @Test
    public void queryIsNullAssertionShouldPassForNullQuery()
    {
        assertThat(nullQuery(), isNull(String.class).now());
        assertThat(nullQuery(), isNull(String.class).byDefaultTimeout());
        assertThat(nullQuery(), isNull(String.class).by(500));

        assertThat(nullQuery(), QueryAssertions.<String>isNull().now());
        assertThat(nullQuery(), QueryAssertions.<String>isNull().byDefaultTimeout());
        assertThat(nullQuery(), QueryAssertions.<String>isNull().by(500));
    }


    @Test
    public void queryEqualsAssertionShouldFailForDifferentQueryResultNow()
    {
        try
        {
            assertThat(queryFor("something"), isEqual("somethingelse").now());
            throw new IllegalStateException("Should fail");
        }
        catch(AssertionError e)
        {
            assertEquals("Query <com.atlassian.jira.webtest.framework.core.query.StaticQuery[interval=100,"
                    + "defaultTimeout=500][value=something]>, expected immediate value <somethingelse>, but was <something>",
                    e.getMessage());
        }
    }

    @Test
    public void queryEqualsAssertionShouldFailForDifferentQueryResultByDefaultTimeout()
    {
        try
        {
            assertThat(forMultipleReturns("something", "somethingdifferent", "somethingmore").returnAll(),
                    isEqual("somethingelse").byDefaultTimeout());
            throw new IllegalStateException("Should fail");
        }
        catch(AssertionError e)
        {
            assertEquals("Query <com.atlassian.jira.webtest.framework.core.mock.MockTimedQuery[interval=100,"
                    + "defaultTimeout=200]>, expected value <somethingelse> by 200ms (default timeout), but failed, "
                    + "with last recorded value <somethingmore>",
                    e.getMessage());
        }
    }

    @Test
    public void queryEqualsAssertionShouldFailForDifferentQueryResultByCustomTimeout()
    {
        try
        {
            assertThat(forMultipleReturns("something", "somethingdifferent", "somethingmore",
                    "andnowforsomethingcompletelydifferent").returnAll(), isEqual("somethingelse").by(100));
            throw new IllegalStateException("Should fail");
        }
        catch(AssertionError e)
        {
            assertEquals("Query <com.atlassian.jira.webtest.framework.core.mock.MockTimedQuery[interval=100,"
                    + "defaultTimeout=300]>, expected value <somethingelse> by 100ms, but failed, "
                    + "with last recorded value <somethingdifferent>",
                    e.getMessage());
        }
    }

    @Test
    public void isNotNullAssertionShouldFailForNullQueryNow()
    {
        try
        {
            assertThat(nullQuery(), isNotNull(String.class).now());
            throw new IllegalStateException("Should fail");
        }
        catch(AssertionError e)
        {
            assertEquals("Query <com.atlassian.jira.webtest.framework.core.mock.MockTimedQuery[interval=100,"
                    + "defaultTimeout=500]> expected to return non-null result immediately, but failed",
                    e.getMessage());
        }
    }

    @Test
    public void isNotNullAssertionShouldFailForNullQueryByDefaultTimeout()
    {
        try
        {
            assertThat(forMultipleReturns("one", "two", "three", "four", "five").returnNull(), isNotNull(String.class).byDefaultTimeout());
            throw new IllegalStateException("Should fail");
        }
        catch(AssertionError e)
        {
            assertEquals("Query <com.atlassian.jira.webtest.framework.core.mock.MockTimedQuery[interval=100,"
                    + "defaultTimeout=400]> expected to return non-null result by 400ms (default timeout), but failed",
                    e.getMessage());
        }
    }

    @Test
    public void isNotNullAssertionShouldFailForNullQueryByCustomTimeout()
    {
        try
        {
            assertThat(forMultipleReturns("five", "six", "seven", "eight").returnLast(), isNotNull(String.class).by(200));
            throw new IllegalStateException("Should fail");
        }
        catch(AssertionError e)
        {
            assertEquals("Query <com.atlassian.jira.webtest.framework.core.mock.MockTimedQuery[interval=100,"
                    + "defaultTimeout=300]> expected to return non-null result by 200ms, but failed", e.getMessage());
        }
    }

    @Test
    public void isNullAssertionShouldFailForNonNullQueryNow()
    {
        try
        {
            assertThat(forMultipleReturns("one", "two", "three", "four").returnAll(), isNull(String.class).now());
            throw new IllegalStateException("Should fail");
        }
        catch(AssertionError e)
        {
            assertEquals("Query <com.atlassian.jira.webtest.framework.core.mock.MockTimedQuery[interval=100,"
                    + "defaultTimeout=300]> expected to return null immediately, but was <one>", e.getMessage());
        }
    }

    @Test
    public void isNullAssertionShouldFailForNonNullQueryByDefaultTimeout()
    {
        try
        {
            assertThat(forMultipleReturns("one", "two", "three", "four", "five").returnAll(), isNull(String.class).byDefaultTimeout());
            throw new IllegalStateException("Should fail");
        }
        catch(AssertionError e)
        {
            assertEquals("Query <com.atlassian.jira.webtest.framework.core.mock.MockTimedQuery[interval=100,"
                    + "defaultTimeout=400]> expected to return null by 400ms (default timeout), but failed, "
                    + "with last recorded value <five>", e.getMessage());
        }
    }

    @Test
    public void isNullAssertionShouldFailForNonNullQueryByCustomTimeout()
    {
        try
        {
            assertThat(forMultipleReturns("five", "six", "seven", "eight").returnAll(), isNull(String.class).by(200));
            throw new IllegalStateException("Should fail");
        }
        catch(AssertionError e)
        {
            assertEquals("Query <com.atlassian.jira.webtest.framework.core.mock.MockTimedQuery[interval=100,"
                    + "defaultTimeout=300]> expected to return null by 200ms, but failed, with last recorded "
                    + "value <seven>", e.getMessage());
        }
    }


    private TimedQuery<String> queryFor(String value)
    {
        return new StaticQuery<String>(value, 500, DEFAULT_INTERVAL);
    }

    private TimedQuery<String> nullQuery()
    {
        return new MockTimedQuery<String>(500, DEFAULT_INTERVAL, ExpirationHandler.RETURN_NULL).returnNull();
    }

    private MockTimedQuery<String> forMultipleReturns(String... returns)
    {
        StrictMockClock clock = clockFor(returns.length);
        return new MockTimedQuery<String>(clock, clock.last(), DEFAULT_INTERVAL, ExpirationHandler.RETURN_NULL)
                .returnValues(returns);
    }

    private StrictMockClock clockFor(int returnSize)
    {
        final List<Long> times = new ArrayList<Long>(returnSize);
        long time = 0;
        times.add(time); // initial call before loop
        for (int i=0; i<returnSize; i++)
        {
            // each loop in AbstractTimedQuery/Condition calls currentTime() 2 times
            times.add(time);
            times.add(time);
            time += DEFAULT_INTERVAL;
        }
        return new StrictMockClock(times);
    }


}
