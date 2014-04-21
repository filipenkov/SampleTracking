package com.atlassian.jira.webtest.framework.core.condition;

import com.atlassian.jira.webtest.framework.core.mock.MockCondition;
import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for spin wait functionality in the {@link AbstractTimedCondition}.
 *
 * @since v4.3
 */
public class TestAbstractTimedCondition
{
    private static final Logger log = Logger.getLogger(TestAbstractTimedCondition.class);

    @Test
    public void testNotSuccessful()
    {
        // interval is 50,
        MockCondition tested = MockCondition.successAfter(9).withClock(ConditionClocks.forInterval(MockCondition.DEFAULT_INTERVAL));
        assertFalse(tested.by(400));
    }

    @Test
    public void testNotSuccessfulDefault()
    {
        // interval is 50,
        MockCondition tested = MockCondition.successAfter(11).withClock(ConditionClocks.forInterval(MockCondition.DEFAULT_INTERVAL));
        // default is 500
        assertFalse(tested.byDefaultTimeout());
    }

    @Test
    public void testSuccessful()
    {
        // interval is 50,
        MockCondition tested = MockCondition.successAfter(8).withClock(ConditionClocks.forInterval(MockCondition.DEFAULT_INTERVAL));
        assertTrue(tested.by(400));
    }

    @Test
    public void testSuccessfulDefault()
    {
        // interval is 50,
        MockCondition tested = MockCondition.successAfter(10).withClock(ConditionClocks.forInterval(MockCondition.DEFAULT_INTERVAL));
        // default is 500
        assertTrue(tested.byDefaultTimeout());
    }
}
