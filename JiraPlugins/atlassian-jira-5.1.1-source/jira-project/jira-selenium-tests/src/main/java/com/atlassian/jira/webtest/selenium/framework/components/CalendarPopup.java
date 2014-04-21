package com.atlassian.jira.webtest.selenium.framework.components;

import com.atlassian.jira.webtest.framework.core.Timeouts;
import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.impl.selenium.condition.IsPresentCondition;
import com.atlassian.jira.webtest.framework.impl.selenium.condition.ValueChangedCondition;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.selenium.framework.core.AbstractSeleniumPageObject;
import com.atlassian.jira.webtest.selenium.framework.core.LocalizablePageObject;
import com.atlassian.jira.webtest.selenium.framework.core.PageObject;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.byDefaultTimeout;
import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.isTrue;
import static com.atlassian.jira.webtest.framework.core.TimedAssertions.assertThat;
import static com.atlassian.jira.webtest.selenium.framework.model.Locators.CSS;
import static com.atlassian.jira.webtest.selenium.framework.model.Locators.removeLocatorPrefix;

/**
 * Represents the calendar popup on a page. Can be used to drive the popup.
 *
 * @since v4.2
 */
public final class CalendarPopup extends AbstractSeleniumPageObject implements PageObject, LocalizablePageObject
{
    // TODO implement openable (after Openable well defined)

    // TODO use pedefined timeout?
    private static final long CALENDAR_TIMEOUT = 2000;
    private static final String MAIN_LOCATOR = CSS.create("div.calendar.active");
    private static final String LOCATOR_DAY_TEMPLATE = " td.day-%d";
    private static final String HOUR_LOCATOR = " span.hour";


    private final String triggerLocator;
    private final String inputLocator;

    public CalendarPopup(String inputLocator, String triggerLocator, SeleniumContext context)
    {
        super(context);
        this.inputLocator = notNull("inputLocator", inputLocator);
        this.triggerLocator = notNull("triggerLocator", triggerLocator);
    }

    /* ----------------------------------------------- LOCATORS ----------------------------------------------------- */

    public String locator()
    {
        return MAIN_LOCATOR;
    }

    public String inputLocator()
    {
        return inputLocator;
    }

    public String triggerLocator()
    {
        return triggerLocator;
    }

    /**
     * Locator of a given day cell in the calendar popup.
     *
     * @param day day cell number
     * @return day locator
     */
    public String dayLocator(int day)
    {
        return inPopup(String.format(LOCATOR_DAY_TEMPLATE, day));
    }

    /**
     * Locator of the hour cell in the calendar popup.
     *
     * @return hour cell locator
     */
    public String hourLocator()
    {
        return inPopup(HOUR_LOCATOR);
    }

    /**
     * CSS locator of an element within the calendar popup.
     *
     * @param locator CSS locator
     * @return locator of the element within this popup
     */
    public String inPopup(String locator)
    {
        return locator() + " " + removeLocatorPrefix(locator);
    }

    /* ------------------------------------------------ ACTIONS ----------------------------------------------------- */

    /**
     * Insert date into
     *
     * @param date
     * @return
     */
    public CalendarPopup insertDate(String date)
    {
        client.type(inputLocator, date);
        return this;
    }


    public CalendarPopup open()
    {
        assertThat.elementPresentByTimeout(triggerLocator);
        client.click(triggerLocator);
        return this;
    }

    public CalendarPopup clickDay(int day)
    {
        String dayLocator = dayLocator(day);
        assertThat.elementPresentByTimeout(dayLocator, context.timeouts().components());
        popupClick(dayLocator);
        return this;
    }

    public CalendarPopup increaseHour()
    {
        final String hourLocator = hourLocator();
        assertThat.elementPresentByTimeout(hourLocator, context.timeouts().components());
        TimedCondition hourChanged = hourChangedCondition(hourLocator);
        popupClick(hourLocator);
        assertHourChanged(hourChanged);
        return this;
    }

    private ValueChangedCondition hourChangedCondition(String hourLocator)
    {
        return ValueChangedCondition.newTextChanged(context).locator(hourLocator).defaultTimeout(Timeouts.UI_ACTION).build();
    }

    public CalendarPopup increaseHour(int times)
    {
        for (int i=0; i<times; i++)
        {
            increaseHour();
        }
        return this;
    }

    private void assertHourChanged(TimedCondition hourChanged)
    {
        assertThat("Hour should change", hourChanged, byDefaultTimeout());
    }

    private void popupClick(final String locator)
    {
        client.mouseDown(locator);
        client.click(locator);
        client.mouseUp(locator);
    }

    /* ------------------------------------------------ QUERIES ----------------------------------------------------- */

    /**
     * Retrieve date value from the calendar input field
     *
     * @return date from the calendar text input
     */
    public String getDate()
    {
        waitForInput();
        return client.getValue(inputLocator());
    }

    private void waitForInput()
    {
        assertThat("Calendar input should be present", inputPresentCondition(), isTrue().byDefaultTimeout());
    }

    private TimedCondition inputPresentCondition()
    {
        return IsPresentCondition.forContext(context).defaultTimeout(Timeouts.UI_ACTION).locator(inputLocator()).build();
    }


    /* ---------------------------------------------- ASSERTIONS ---------------------------------------------------- */

    public void assertReady(final long timeout)
    {
        assertThat.elementPresentByTimeout(MAIN_LOCATOR, timeout);
    }

    public void assertClosed(final long timeout)
    {
        assertThat.elementNotPresentByTimeout(MAIN_LOCATOR, timeout);
    }
}
