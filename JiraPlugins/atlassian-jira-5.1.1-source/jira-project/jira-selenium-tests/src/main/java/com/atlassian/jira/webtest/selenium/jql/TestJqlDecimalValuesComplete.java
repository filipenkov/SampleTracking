package com.atlassian.jira.webtest.selenium.jql;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.framework.core.Timeouts;
import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocators;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import com.atlassian.webtest.ui.keys.SpecialKeys;

import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.assertTrueByDefaultTimeout;

/**
 * Just another JQL test.
 *
 * @since v4.3
 */
@WebTest({Category.SELENIUM_TEST })
public class TestJqlDecimalValuesComplete extends JiraSeleniumTest
{

    private static final String JQLTEXT_INPUT = "jqltext";

    private Locator jqlLocator;

    @Override
    public void onSetUp()
    {
        super.onSetUp();
        this.jqlLocator = SeleniumLocators.id(JQLTEXT_INPUT, context());
    }

    public void testCompletionWorksForDecimalValuesWithoutQuotes() throws Exception
    {
        restoreData("TestJQLAutocomplete.xml");
        getNavigator().gotoFindIssuesAdvanced();
        client.typeWithFullKeyEvents(JQLTEXT_INPUT, "affectedVersion = 4.", false);
        waitFor(500);
        client.typeWithFullKeyEvents(JQLTEXT_INPUT, "0", false);
        assertTrueByDefaultTimeout(SeleniumLocators.css("#jqlform .suggestions.dropdown-ready", context())
                .withDefaultTimeout(Timeouts.AJAX_ACTION).element().isPresent());
        jqlLocator.element().type(SpecialKeys.ARROW_DOWN);
        jqlLocator.element().type(SpecialKeys.ENTER);
        if (!client.getValue(JQLTEXT_INPUT).contains("\"4.0\"")) {
            throw new RuntimeException("Expected '4.0' to be selectable");
        }
    }

}
