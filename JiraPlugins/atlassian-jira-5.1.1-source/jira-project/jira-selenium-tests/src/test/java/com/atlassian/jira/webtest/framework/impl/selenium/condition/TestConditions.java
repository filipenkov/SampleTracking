package com.atlassian.jira.webtest.framework.impl.selenium.condition;

import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.selenium.mock.MockSeleniumClient;
import com.atlassian.selenium.mock.MockSeleniumConfiguration;
import junit.framework.TestCase;

/**
 * Test case for Selenium conditions.
 *
 * @since v4.3
 */
public class TestConditions extends TestCase
{
    private MockSeleniumConfiguration mockConfig = new MockSeleniumConfiguration().conditionInterval(50L);
    private SeleniumContext mockContext = new SeleniumContext(new MockSeleniumClient(), mockConfig);

    public void testIsPresent()
    {
        IsPresentCondition tested = newIsPresent("id=test", 400L);
        assertEquals(400L, tested.defaultTimeout());
        assertEquals(50L, tested.interval());
        assertEquals("id=test", tested.locator());
    }

    public void testValueChanged()
    {
        ValueChangedCondition tested = newInputValueChanged("id=testid", 500L);
        assertEquals(500L, tested.defaultTimeout());
        assertEquals(50L, tested.interval());
        assertEquals("id=testid", tested.locator());
        assertEquals(ElementType.INPUT, tested.elementType());
    }

    private IsPresentCondition newIsPresent(String locator, long defTimeout)
    {
        return IsPresentCondition.forContext(mockContext).locator(locator).defaultTimeout(defTimeout).build();
    }

    private ValueChangedCondition newInputValueChanged(String locator, long defTimeout)
    {
        return ValueChangedCondition.newInputChanged(mockContext).locator(locator).defaultTimeout(defTimeout).build();
    }
}
