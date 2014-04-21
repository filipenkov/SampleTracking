package com.atlassian.jira.webtest.selenium.framework.components;

import com.atlassian.jira.webtest.framework.core.Timeouts;
import com.atlassian.jira.webtest.framework.core.ui.Keys;
import com.atlassian.jira.webtest.framework.impl.selenium.condition.IsPresentCondition;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.selenium.framework.core.AbstractSeleniumPageObject;
import com.atlassian.jira.webtest.selenium.framework.core.PageObject;
import com.atlassian.selenium.keyboard.SeleniumTypeWriter;
import com.atlassian.webtest.ui.keys.TypeMode;
import junit.framework.Assert;
import junit.framework.TestCase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Suggestions component of the AUI MultiSelect.
 *
 * @since v4.2
 */
public class MultiSelectSuggestions<T extends MultiSelect> extends AbstractSeleniumPageObject implements PageObject
{
    private static final String ISSUE_COUNT_REGEX = "\\(Showing (\\d+) of (\\d+) matching issues\\)";
    private static final int DISPLAYED_ISSUE_COUNT_GROUP_INDEX = 1;
    private static final int TOTAL_ISSUE_COUNT_GROUP_INDEX = 2;

    private final MultiSelect<T> parent;
    private final MultiSelectLocatorData locators;
    private final SeleniumTypeWriter inputWriter;


    MultiSelectSuggestions(MultiSelect<T> parent, MultiSelectLocatorData locators, SeleniumContext ctx)
    {
        super(ctx);
        this.parent = notNull("parent", parent);
        this.locators = notNull("locators", locators);
        this.inputWriter =  new SeleniumTypeWriter(client, parent.inputAreaLocator(), TypeMode.TYPE);
    }

    /* --------------------------------------------------- LOCATORS ------------------------------------------------- */

    public String locator()
    {
        return locators.visibleSuggestionsLocator();
    }

    public String locatorWithin(String jQueryLocator)
    {
        return locators.inSuggestions(jQueryLocator);
    }

    public String suggestionGroupLocator(String group)
    {
        return locator() + " ul#" + groupIdFor(group);
    }

    /**
     * Locator for suggestion in a particular <tt>group</tt>.
     *
     * @param group suggestion group, in which the suggestion should appear
     * @param suggestionValue suggestion text
     * @return locator for given suggestion and group
     */
    public String suggestionInGroupLocator(String group, String suggestionValue)
    {
        return suggestionGroupLocator(group) + " li:contains('" + suggestionValue + "')";
    }

    /**
     * Locator for suggestion in any group, or without a group.
     *
     * @param suggestionValue suggestion text
     * @return locator for given suggestion
     */
    public String suggestionLocator(String suggestionValue)
    {
        return locator() + " li:contains(" + suggestionValue + ")";
    }

    /**
     * Locator for 'No Matches' case in suggestions drop-down.
     *
     * @return locator for given suggestion
     */
    public String noMatchesLocator()
    {
        return locator() + " li.no-suggestions";
    }

    /**
     * Locator for a currently selected suggestion in a particular <tt>group</tt>.
     *
     * @param group suggestion group, in which the selected suggestion should appear
     * @param suggestionValue selected suggestion text
     * @return locator for selected suggestion with given parameters
     */
    public String selectedSuggestionLocator(String group, String suggestionValue)
    {
        return suggestionGroupLocator(group) + " li.active:contains(" + suggestionValue + ")";
    }

    /**
     * Locator of the group header of given suggestion <tt>group</tt>.
     *
     * @param group suggestion group to find
     * @return locator of the given suggestion group header
     */
    public String groupHeaderLocator(String group)
    {
        return locatorWithin("h5:contains(" + group + ")");
    }


    public String groupHeaderDescriptionLocator(String group)
    {
        return groupHeaderLocator(group) + " span.aui-section-description";    
    }

    private String groupIdFor(String group)
    {
        return group.toLowerCase().replaceAll(" ", "-");
    }

    /* --------------------------------------------------- QUERIES -------------------------------------------------- */

    public String groupHeaderDescriptionText(String suggestionGroup)
    {
        return client.getText(groupHeaderDescriptionLocator(suggestionGroup)).trim();
    }

    public int displayedIssueCount(String suggestionGroup)
    {
        return Integer.parseInt(displayedIssueCountString(suggestionGroup));
    }

    private String displayedIssueCountString(final String suggestionGroup)
    {
        return issueCountFor(suggestionGroup, DISPLAYED_ISSUE_COUNT_GROUP_INDEX);
    }

    private String totalIssueCountString(final String suggestionGroup)
    {
        return issueCountFor(suggestionGroup, TOTAL_ISSUE_COUNT_GROUP_INDEX);
    }

    private String issueCountFor(final String suggestionGroup, final int groupIndex)
    {
        String headerText = groupHeaderDescriptionText(suggestionGroup);
        Matcher matcher = Pattern.compile(ISSUE_COUNT_REGEX).matcher(headerText);
        Assert.assertTrue(matcher.find());
        return matcher.group(groupIndex);
    }

    public int totalIssueCount(String suggestionGroup)
    {
        return Integer.parseInt(totalIssueCountString(suggestionGroup));
    }

    /**
     * Checks that this suggestions drop-down is open.
     *
     * @return true, if this drop down is open, false otherwise
     */
    public boolean isOpen()
    {
        return isOpenCondition().byDefaultTimeout();
    }

    private IsPresentCondition isOpenCondition()
    {
        return IsPresentCondition.forContext(context).locator(locator()).defaultTimeout(Timeouts.AJAX_ACTION).build();
    }

    public boolean containsGroup(String groupName)
    {
        return client.isElementPresent(suggestionGroupLocator(groupName));
    }

    /* --------------------------------------------------- ACTIONS -------------------------------------------------- */

    public MultiSelectSuggestions<T> up()
    {
        assertOpen();
        inputWriter.type(Keys.UP);
        return this;
    }

    public MultiSelectSuggestions<T> down()
    {
        assertOpen();
        inputWriter.type(Keys.DOWN);
        return this;
    }

    public T closeByEscape()
    {
        final int escapeKey = 27;
        client.simulateKeyPressForSpecialKey(locators.textAreaLocator(), escapeKey);
        assertClosed(1000);
        return parent.asTargetType();
    }


    /* -------------------------------------------------- ASSERTIONS ------------------------------------------------ */


    /**
     * {@inheritDoc}
     *
     */
    public void assertReady(long timeout)
    {
        parent.assertReady(timeout);
    }


    /**
     * Assert that this suggestions drop-down is open.
     *
     */
    public void assertOpen()
    {
        assertThat.elementPresentByTimeout(locator(), 5000);
    }

    /**
     * Assert that this suggestions drop-down is open by a specific <tt>timeout</tt>..
     *
     * @param timeout timeout to wait
     */
    public void assertOpen(int timeout)
    {
        assertThat.elementPresentByTimeout(locator(), timeout);
    }

    /**
     * Assert that this suggestions drop-down is closed.
     *
     */
    public void assertClosed()
    {
        assertThat.elementNotPresentByTimeout(locator(), 1000);
    }

    /**
     * Assert that this suggestions drop-down is closed by a specific <tt>timeout</tt>.
     *
     * @param timeout timeout to wait
     */
    public void assertClosed(int timeout)
    {
        if(client.isElementPresent(locator()))
        {
            assertThat.notVisibleByTimeout(locator(), timeout);
        }
    }

    
    public void assertContainsGroup(String group)
    {
        assertOpen();
        assertThat.elementPresentByTimeout(suggestionGroupLocator(group));
    }

    public void assertContains(String group, String suggestion)
    {
        assertOpen();
        assertThat.elementPresentByTimeout(suggestionInGroupLocator(group, suggestion));
    }

    /**
     * Assert that this suggestions contain given <tt>suggestion</tt> in any place, that is in any group, or
     * as a first-level suggestion (not contained in any group).
     *
     * @param suggestion suggestion value to check
     * @param moreSuggestions optional suggestions to check
     */
    public void assertContains(String suggestion, String... moreSuggestions)
    {
        assertContainsSingle(suggestion);
        for (String more : moreSuggestions)
        {
            assertContainsSingle(more);
        }
    }

    private void assertContainsSingle(String suggestion)
    {
        assertOpen();
        assertThat.elementPresentByTimeout(suggestionLocator(suggestion));
    }

    /**
     * Asserts for a special case, where a 'No Matches' string is displayed without a group.
     *
     *
     */
    public void assertNoMatches()
    {
        assertOpen();
        assertThat.elementPresentByTimeout(noMatchesLocator());
    }

    /**
     * Assert that given <tt>suggestion</tt> in given <tt>group</tt> is selected.
     *
     * @param group group
     * @param suggestion suggestion text value
     */
    public void assertSelected(String group, String suggestion)
    {
        assertOpen();
        assertThat.elementPresentByTimeout(selectedSuggestionLocator(group, suggestion));
    }

    /**
     * Assert that this suggestions component does not contain given <tt>suggestion</tt> in given <tt>group</tt>.
     *
     * @param group group to check
     * @param suggestion suggestion value to check
     */
    public void assertDoesNotContain(String group, String suggestion)
    {
        assertOpen();
        assertThat.elementNotPresentByTimeout(suggestionInGroupLocator(group, suggestion));
    }

    /**
     * Assert that this suggestions component does not contain given <tt>suggestion</tt> at all - that is suggestion in any group,
     * or a first-level suggestion (not contained in any group).
     *
     * @param suggestion suggestion value to check
     * @param moreSuggestions optional suggestions to check
     */
    public void assertDoesNotContain(String suggestion, String... moreSuggestions)
    {
        assertDoesNotContainSingle(suggestion);
        for (String more : moreSuggestions)
        {
            assertDoesNotContainSingle(more);
        }
    }

    private void assertDoesNotContainSingle(String suggestion)
    {
        assertOpen();
        assertThat.elementNotPresentByTimeout(suggestionLocator(suggestion));
    }

    public void assertDisplayedIssueCountEquals(String suggestionGroup, int expected)
    {
        assertOpen();
        TestCase.assertEquals(expected, displayedIssueCount(suggestionGroup));
    }

    
    public void assertTotalIssueCountEquals(String suggestionGroup, int expected)
    {
        assertOpen();
        TestCase.assertEquals(expected, totalIssueCount(suggestionGroup));
    }
}
