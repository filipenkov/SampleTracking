package com.atlassian.jira.webtests.ztests.navigator.jql;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Test that the JQL input box's focus doesn't interfere with keyboard navigation.
 *
 * <p>
 * We can't quite Selenium test the focus for the input box, so we'll just make sure the correct class is set on it.
 * </p>
 *
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.JQL })
public class TestJqlInputFocus extends FuncTestCase
{
    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestJqlInputFocus.xml");
    }

    public void testJqlInputFocus()
    {
        navigation.issueNavigator().gotoNavigator();
        tester.clickLink("switchnavtype");
        assertJqlHasFocus(true);

        // Every submut of the search should focus JQL.
        assertQueryWithResultingFocus("", true);
        assertQueryWithResultingFocus("", true);
        assertQueryWithResultingFocus("key > TEST-20", true);
        assertQueryWithResultingFocus("key > TEST-20", true);

        // Navigating away and return to the same search shouldn't focus JQL.
        navigation.issue().gotoIssue("TEST-25");
        tester.clickLink("return-to-search");
        assertJqlHasFocus(false);

        // Toggling to edit mode focuses JQL.
        assertQueryWithResultingFocus("key > TEST-10", true);
        tester.clickLink("viewfilter");
        tester.clickLink("editfilter");
        assertJqlHasFocus(true);

        // New search.
        tester.clickLink("new_filter");
        assertJqlHasFocus(true);

        // Always focus when there are errors.
        assertQueryWithResultingFocus("key >", true);
        assertQueryWithResultingFocus("key >", true);
        assertQueryWithResultingFocus("key", true);
        assertQueryWithResultingFocus("key", true);
    }

    private void assertQueryWithResultingFocus(String query, boolean hasFocus)
    {
        tester.setWorkingForm("jqlform");
        tester.setFormElement("jqlQuery", query);
        tester.submit();
        assertJqlHasFocus(hasFocus);
    }
    

    private void assertJqlHasFocus(boolean focused)
    {
        final XPathLocator locator = new XPathLocator(tester, "//textarea[@id='jqltext' and contains(@class, 'focused')]");
        if (focused)
        {
            assertTrue(locator.exists());
        }
        else
        {
            assertFalse(locator.exists());
        }
    }
}

