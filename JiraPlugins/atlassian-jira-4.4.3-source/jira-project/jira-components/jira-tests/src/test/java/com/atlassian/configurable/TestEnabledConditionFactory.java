package com.atlassian.configurable;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.local.ListeningTestCase;

public class TestEnabledConditionFactory extends ListeningTestCase
{
    @Test
    public void testFactoryInstantiatesTheRightStuff()
    {
        EnabledCondition condition = EnabledCondition.Factory.create(NotEnabledCondition.class.getName());
        assertNotNull(condition);
        assertTrue(condition instanceof NotEnabledCondition);
        assertFalse(condition.isEnabled());
    }

    @Test
    public void testFactoryReturnsNullForNull()
    {
        EnabledCondition condition = EnabledCondition.Factory.create(null);
        assertNull(condition);
    }

    @Test
    public void testFactoryReturnsNullForGarbage()
    {
        EnabledCondition condition = EnabledCondition.Factory.create("12*");
        assertNull(condition);
    }

    @Test
    public void testFactoryReturnsNullForNonImplementingClass()
    {
        EnabledCondition condition = EnabledCondition.Factory.create(Object.class.getName());
        assertNull(condition);
    }
}
