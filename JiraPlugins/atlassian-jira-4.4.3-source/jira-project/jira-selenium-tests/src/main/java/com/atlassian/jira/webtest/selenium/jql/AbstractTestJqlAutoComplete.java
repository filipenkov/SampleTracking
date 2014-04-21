package com.atlassian.jira.webtest.selenium.jql;

import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.impl.selenium.condition.IsVisibleCondition;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocators;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import com.atlassian.jira.webtests.JIRAWebTest;
import com.atlassian.webtest.ui.keys.SpecialKeys;

import java.util.Set;

import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.by;
import static com.atlassian.jira.webtest.framework.core.TimedAssertions.assertThat;
import static com.atlassian.jira.webtest.framework.core.condition.Conditions.not;

/**
 * Baseclass for selenium autocomplete tests
 *
 * @since v4.0
 */
public abstract class AbstractTestJqlAutoComplete extends JiraSeleniumTest
{
    protected final String VALUE_ID_PREFIX = "jql_value_suggest_";
    protected final String FUNC_ID_PREFIX = "jql_function_suggest_";
    protected static final String OPERATOR_ID_PREFIX = "jql_operator_suggest_";
    protected static final String ILLEGAL_CHARS_STRING = "{}*/%+$#@?.;";
    protected static final String[] NO_FUNCTION_SUGGESTIONS = new String[]{};
    protected static final String[] NO_VALUE_SUGGESTIONS = new String[]{};
    public static final Set<String> SOME_RESERVED_WORDS;
    protected static final String JQL_INPUT = "jqltext";

    static
    {
        // Takes too long to run all of them so lets just try a few.
        final CollectionBuilder<String> builder = CollectionBuilder.newBuilder();
        builder.addAll("abort", "access", "add", "after", "alias", "all", "alter", "and", "any", "as", "asc");

        SOME_RESERVED_WORDS = builder.asSet();
    }

    private SeleniumLocator suggestionsLocator;
    private SeleniumLocator suggestionsReadyLocator;

    protected Locator jqlLocator;

    @Override
    public void onSetUp()
    {
        super.onSetUp();
        suggestionsLocator = SeleniumLocators.css("div.suggestions", context());
        suggestionsReadyLocator = SeleniumLocators.css("div.suggestions.dropdown-ready", context());
        this.jqlLocator = SeleniumLocators.id(JQL_INPUT, context());
    }

    abstract class ParseResult
    {
        abstract void check();
    }

    protected final ParseResult SUCCESS = new ParseResult()
    {
        void check()
        {
            assertThat.elementPresent("//span[contains(@class, 'jqlgood')]");
        }
    };

    protected final ParseResult PARSE_FAIL = new ParseResult()
    {
        void check()
        {
            assertThat.elementPresent("//span[contains(@class, 'jqlerror')]");
        }
    };

    protected void _testParse(final String jql, final ParseResult expected) throws InterruptedException
    {
        client.type(JQL_INPUT, jql);
        // Send a right arrow with an event to kick the js in
        client.keyPress(JQL_INPUT, "\\16");
        // We need a little delay
        waitFor(2500);
        // Lets verify that the query does not parse
        expected.check();
    }

    protected void _testSuggestions(final String jql, final String[] valueSuggestions, final ParseResult expected)
            throws InterruptedException
    {
        client.type(JQL_INPUT, jql);
        // Send a right arrow with an event to kick the js in
        client.keyPress(JQL_INPUT, "\\16");
        // We need a little delay
        if (valueSuggestions.length != 0)
        {
            assertSuggestionsOpen();
        }
        else
        {
            assertSuggestionsClosed();
        }
        assertThat.elementNotPresent(VALUE_ID_PREFIX + valueSuggestions.length);
        for (int i = 0; i < valueSuggestions.length; i++)
        {
            final String valueSuggestion = valueSuggestions[i];
            assertValueSuggestion(i, valueSuggestion);
        }
        assertThat.elementNotPresent(FUNC_ID_PREFIX + 0);
        // Lets verify that the query does not parse
        expected.check();
    }

    protected void _testSuggestions(final String jqlClauseName, final String operator, final String[] valueSuggestions,
                                    final String[] functionSuggestions) throws InterruptedException
    {
        _testSuggestions(jqlClauseName, operator, valueSuggestions, functionSuggestions, null, null);
    }

    protected void _testSuggestions(final String jqlClauseName, final String[] valueSuggestions,
                                    final String[] functionSuggestions) throws InterruptedException
    {
        _testSuggestions(jqlClauseName, valueSuggestions, functionSuggestions, null, null);
    }

    protected void _testSuggestions(final String jqlClauseName, final String operator, final String[] valueSuggestions,
                                    final String[] functionSuggestions, final String boldText,
                                    final String[] boldSuggestions) throws InterruptedException
    {
        // Test version auto complete
        client.type(JQL_INPUT, jqlClauseName + " " + operator + " ");
        // Send a right arrow with an event to kick the js in
        client.keyPress(JQL_INPUT, "\\16");
        if (functionSuggestions.length == 0 && valueSuggestions.length == 0)
        {
            assertSuggestionsClosed();
        }
        else
        {
            assertSuggestionsOpen();
        }

        assertThat.elementNotPresent(VALUE_ID_PREFIX + valueSuggestions.length);
        if (valueSuggestions.length > 0)
        {
            for (int i = 0; i < valueSuggestions.length; i++)
            {
                final String valueSuggestion = valueSuggestions[i];
                assertValueSuggestion(i, valueSuggestion);
            }
        }

        assertThat.elementNotPresent(FUNC_ID_PREFIX + functionSuggestions.length);
        if (functionSuggestions.length > 0)
        {
            for (int i = 0; i < functionSuggestions.length; i++)
            {
                final String functionSuggestion = functionSuggestions[i];
                assertFunctionSuggestion(i, functionSuggestion);
            }
        }
        // Lets verify that the query does not parse
        PARSE_FAIL.check();

        _testBoldSuggestions(boldText, boldSuggestions, false);
        client.click("id=jqlrunquery", true);
    }

    protected void _testSuggestions(final String jqlClauseName, final String[] valueSuggestions,
                                    final String[] functionSuggestions, final String boldText,
                                    final String[] boldSuggestions) throws InterruptedException
    {
        _testSuggestions(jqlClauseName, "in", valueSuggestions, functionSuggestions, boldText, boldSuggestions);
    }

    protected void _testOrderByBoldSuggestions(final String boldText, final String[] boldSuggestions) throws InterruptedException
    {
        client.type(JQL_INPUT, "afield = avalue order by ");
        _testFieldSuggestions(boldText, boldSuggestions, false);
    }

    protected void _testFieldSuggestions(final String boldText, final String[] fieldSuggestions) throws InterruptedException
    {
        _testFieldSuggestions(boldText, fieldSuggestions, true);
    }

    protected void _testFieldSuggestions(final String boldText, final String[] fieldSuggestions, final boolean overwrite) throws InterruptedException
    {
        if (fieldSuggestions != null && boldText != null)
        {
            // Check bolding
            if (overwrite)
            {
                client.type(JQL_INPUT, boldText);
            }
            else
            {
                final String existingValue = client.getValue(JQL_INPUT);
                client.type(JQL_INPUT, existingValue + " " + boldText);
            }
            jqlLocator.element().type(SpecialKeys.ARROW_RIGHT);
            if (fieldSuggestions.length != 0)
            {
                assertSuggestionsOpen();
            }
            else
            {
                assertSuggestionsClosed();
            }
            assertThat.elementNotPresent(VALUE_ID_PREFIX + fieldSuggestions.length);
            if (fieldSuggestions.length > 0)
            {
                for (int i = 0; i < fieldSuggestions.length; i++)
                {
                    final String boldSuggestion = fieldSuggestions[i];
                    int lengthOfBold = boldText.length();
                    if (boldText.startsWith("\""))
                    {
                        lengthOfBold--;
                    }
                    // Check the entire value
                    assertValueSuggestion(i, boldSuggestion);
                    // Check the bolded bit
                    assertValueSuggestion(i, boldSuggestion, boldSuggestion.substring(0, lengthOfBold));
                }
            }
        }
    }

    protected void _testBoldSuggestions(final String boldText, final String[] boldSuggestions) throws InterruptedException
    {
        _testBoldSuggestions(boldText, boldSuggestions, true);
    }

    protected void _testBoldSuggestions(final String boldText, final String[] boldSuggestions, final boolean overwrite)
            throws InterruptedException
    {
        if (boldSuggestions != null && boldText != null)
        {
            // Check bolding
            if (overwrite)
            {
                client.type(JQL_INPUT, boldText);
            }
            else
            {
                final String existingValue = client.getValue(JQL_INPUT);
                client.type(JQL_INPUT, existingValue + " " + boldText);
            }
            // Send a right arrow with an event to kick the js in
            client.keyPress(JQL_INPUT, "\\16");
            // We need a little delay
            waitFor(2500);

            if (boldSuggestions.length != 0)
            {
                assertSuggestionsOpen();
            }
            assertThat.elementNotPresent(VALUE_ID_PREFIX + boldSuggestions.length);
            if (boldSuggestions.length > 0)
            {
                for (int i = 0; i < boldSuggestions.length; i++)
                {
                    final String boldSuggestion = boldSuggestions[i];
                    int lengthOfBold = boldText.length();
                    if (boldText.startsWith("\""))
                    {
                        lengthOfBold--;
                    }
                    assertValueSuggestion(i, boldSuggestion, boldSuggestion.substring(0, lengthOfBold));
                }
            }
        }
    }

    protected void assertValueSuggestion(final int pos, final String text)
    {
        assertValueSuggestion(pos, text, null);
    }

    protected void assertOperatorSuggestion(final int pos, final String text, final String boldText)
    {
        assertThat.elementPresentByTimeout(OPERATOR_ID_PREFIX + pos, 10000);
        assertThat.elementContainsText(OPERATOR_ID_PREFIX + pos, text);
        if (boldText != null)
        {
            assertThat.elementContainsText("//li[@id='" + OPERATOR_ID_PREFIX + pos + "']/b", boldText);
        }
    }

    protected void assertValueSuggestion(final int pos, final String text, final String boldText)
    {
        assertThat.elementPresentByTimeout(VALUE_ID_PREFIX + pos, 10000);
        assertThat.elementContainsText(VALUE_ID_PREFIX + pos, text);
        if (boldText != null)
        {
            assertThat.elementContainsText("//li[@id='" + VALUE_ID_PREFIX + pos + "']/b", boldText);
        }
    }

    protected void assertFunctionSuggestion(final int pos, final String text)
    {
        assertFunctionSuggestion(pos, text, null);
    }

    protected void assertFunctionSuggestion(final int pos, final String text, final String boldText)
    {
        assertThat.elementPresent(FUNC_ID_PREFIX + pos);
        assertThat.elementContainsText(FUNC_ID_PREFIX + pos, text);
        if (boldText != null)
        {
            assertThat.elementContainsText("//li[@id='" + FUNC_ID_PREFIX + pos + "']/b", boldText);
        }
    }

    // call this method if the field you are hiding has the same "clause name" (for JQL) and "field name" (for Field Configuration)
    protected void hideFieldAndAssertFieldNoLongerVisible(final JIRAWebTest funcTest, final String fieldName, final String startingString) throws InterruptedException
    {
        hideFieldAndAssertFieldNoLongerVisible(funcTest, fieldName, fieldName, startingString);
    }

    protected void hideFieldAndAssertFieldNoLongerVisible(final JIRAWebTest funcTest, final String fieldClauseName, final String fieldConfigName, final String startingString) throws InterruptedException
    {
        // Make sure it is there
        _testBoldSuggestions(startingString, new String[]{fieldClauseName});
        _testOrderByBoldSuggestions(startingString, new String[]{fieldClauseName});
        // Hide the field in all configurations
        hideFieldWithName(funcTest, fieldConfigName);
        assertFieldNoLongerVisible(startingString);
    }

    protected void assertFieldNoLongerVisible(final String startingString) throws InterruptedException
    {
        assertFieldNoLongerVisible(startingString, new String[]{}, new String[]{});
    }

    protected void assertFieldNoLongerVisible(final String startingString, final String[] fieldSuggestions, final String[] orderBySuggestions) throws InterruptedException
    {
        // Reload the page so we get the changes
        client.getEval("this.browserbot.getCurrentWindow().onbeforeunload = null");
        client.refresh();
        client.waitForPageToLoad(PAGE_LOAD_WAIT);
        // Now make sure it is no longer in the dropdown
        _testBoldSuggestions(startingString, fieldSuggestions);
        _testOrderByBoldSuggestions(startingString, orderBySuggestions);
    }

    protected void hideFieldWithName(final JIRAWebTest funcTest, final String fieldName)
    {
        funcTest.getAdministration().fieldConfigurations().fieldConfiguration("Hide Stuff Configuration").hideFields(fieldName);
        funcTest.getAdministration().fieldConfigurations().fieldConfiguration("Default Field Configuration").hideFields(fieldName);
        funcTest.getAdministration().reIndex();
    }

    protected void typeInStringAndComplete(final String stringToType, final int positionToComplete)
    {
        client.typeWithFullKeyEvents(JQL_INPUT, stringToType, false);
        assertSuggestionsOpen();
        client.click(VALUE_ID_PREFIX + positionToComplete);
        assertSuggestionsClosed();
    }

    protected void typeInStringAndCompleteFunction(final String stringToType, final int positionToComplete)
    {
        client.typeWithFullKeyEvents(JQL_INPUT, stringToType, false);
        assertSuggestionsOpen();
        client.click(FUNC_ID_PREFIX + positionToComplete);
        assertSuggestionsClosed();
    }

    protected final void assertSuggestionsClosed()
    {
        assertThat("Expected suggestions drop-down to be closed", suggestionsClosed(), by(DROP_DOWN_WAIT));
    }

    protected final void assertSuggestionsOpen()
    {
        assertThat("Expected suggestions drop-down to open", suggestionsOpenAndReady(), by(DROP_DOWN_WAIT));
    }

    protected final TimedCondition suggestionsOpenAndReady()
    {
        return IsVisibleCondition.forContext(context()).defaultTimeout(DROP_DOWN_WAIT)
                .locator(suggestionsReadyLocator).build();
    }

    protected final TimedCondition suggestionsClosed()
    {
        return not(IsVisibleCondition.forContext(context()).defaultTimeout(DROP_DOWN_WAIT)
                .locator(suggestionsLocator).build());
    }
}
