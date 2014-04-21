package com.atlassian.jira.webtest.selenium.framework.components;

import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;

/**
 * Object representation issue picker control in the wild wild Selenium west.
 *
 * @since v4.2
 * @deprecated use {@link com.atlassian.jira.webtest.framework.component.fc.IssuePicker} instead
 */
@Deprecated
public final class IssuePicker extends MultiSelect<IssuePicker>
{
    public static final String DEFAULT_FIELD_NAME = "linkKey";

    public static final class IssuePickerSuggestions extends MultiSelectSuggestions<IssuePicker>
    {
        private static final String HISTORY_SEARCH = "History Search";
        private static final String CURRENT_SEARCH = "Current Search";
        private static final String USER_INPUTTED_OPTION = "User Inputted Option";

        IssuePickerSuggestions(IssuePicker parent, MultiSelectLocatorData locators, SeleniumContext ctx)
        {
            super(parent, locators, ctx);
        }

        public void assertHistorySearchContains(String suggestion)
        {
            assertContains(HISTORY_SEARCH, suggestion);
        }

        public void assertCurrentSearchContains(String suggestion)
        {
            assertContains(CURRENT_SEARCH, suggestion);
        }

        public void assertUserInputContains(String suggestion)
        {
            assertContains(USER_INPUTTED_OPTION, suggestion);
        }

        public void assertHistorySearchSuggestionSelected(String suggestion)
        {
            assertSelected(HISTORY_SEARCH, suggestion);
        }

        public void assertCurrentSearchSuggestionSelected(String suggestion)
        {
            assertSelected(CURRENT_SEARCH, suggestion);
        }

        public void assertUserInputSelected(String suggestion)
        {
            assertSelected(USER_INPUTTED_OPTION, suggestion);
        }

        public void assertHistorySearchDisplayedCountEquals(int expected)
        {
            assertDisplayedIssueCountEquals(HISTORY_SEARCH, expected);
        }

        public void assertHistorySearchTotalCountEquals(int expected)
        {
            assertTotalIssueCountEquals(HISTORY_SEARCH, expected);
        }

        public void assertCurrentSearchDisplayedCountEquals(int expected)
        {
            assertDisplayedIssueCountEquals(CURRENT_SEARCH, expected);
        }

        public void assertCurrentSearchTotalCountEquals(int expected)
        {
            assertTotalIssueCountEquals(CURRENT_SEARCH, expected);
        }
    }



    /**
     * Constructs issue picker using default field name for linking issue.
     *
     * @param contextLocator picker context locator (in case there is more than one picker on the page). If empty,
     * the picker locator will be considered global within the page (discouraged)
     * @param ctx Selenium context
     *
     * @see com.atlassian.jira.webtest.selenium.framework.components.IssuePicker#DEFAULT_FIELD_NAME
     */
    public IssuePicker(String contextLocator, SeleniumContext ctx)
    {
        this(contextLocator, DEFAULT_FIELD_NAME, ctx);
    }

    /**
     * Create new issue picker with custom field name.
     *
     * @param contextLocator picker context locator (in case there is more than one picker on the page). If empty,
     * the picker locator will be considered global within the page (discouraged)
     * @param fieldName name of the field associated with this issue picker
     * @param ctx Selenium context
     */
    public IssuePicker(String contextLocator, String fieldName, SeleniumContext ctx)
    {
        this(MultiSelectLocatorData.forFieldNameInContext(fieldName, contextLocator), ctx);
    }

    private IssuePicker(MultiSelectLocatorData locators, SeleniumContext ctx)
    {
        super(IssuePicker.class, locators, ctx);
    }

    @Override
    protected MultiSelectSuggestions<IssuePicker> createSuggestions()
    {
        return new IssuePickerSuggestions(this, locators, context);
    }

    @Override
    public IssuePickerSuggestions suggestions()
    {
        return (IssuePickerSuggestions) super.suggestions();
    }
}
