package com.atlassian.jira.webtest.framework.core.condition;

import junit.framework.TestCase;

import static com.atlassian.jira.webtest.framework.core.mock.MockCondition.FALSE;
import static com.atlassian.jira.webtest.framework.core.mock.MockCondition.TRUE;

/**
 * Test case for {@link Conditions}.
 *
 * @since v4.2
 */
public class TestConditions extends TestCase
{
    public void testNot()
    {
        TimedCondition notFalse = Conditions.not(FALSE);
        assertTrue(notFalse.now());
        TimedCondition notTrue = Conditions.not(TRUE);
        assertFalse(notTrue.now());
    }

    public void testAndTrue()
    {
        TimedCondition and = Conditions.and(TRUE, TRUE, TRUE);
        assertTrue(and.now());
    }

    public void testAndFalse()
    {
        TimedCondition and = Conditions.and(TRUE, FALSE, TRUE, TRUE);
        assertFalse(and.now());
    }

    public void testOrTrue()
    {
        TimedCondition or = Conditions.or(FALSE, FALSE, TRUE, FALSE);
        assertTrue(or.now());
    }

    public void testOrFalse()
    {
        TimedCondition or = Conditions.or(FALSE, FALSE, FALSE, FALSE);
        assertFalse(or.now());
    }
}
