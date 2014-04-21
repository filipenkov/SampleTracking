package com.atlassian.jira.webtest.selenium.issue;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocators;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import com.atlassian.jira.webtest.selenium.framework.components.LabelsPicker;
import com.atlassian.jira.webtest.selenium.framework.model.Locators;
import com.atlassian.jira.webtest.selenium.harness.util.Navigator;

import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.isTrue;
import static com.atlassian.jira.webtest.framework.core.TimedAssertions.assertThat;

/**
 * @since v4.2
 */
@WebTest({Category.SELENIUM_TEST })
public class TestEditLabelsAnonymous extends JiraSeleniumTest
{
    private Navigator navigation;

    @Override
    public void onSetUp()
    {
        super.onSetUp();
        restoreData("TestCreateAndEditLabelsAnonymous.xml");
        navigation = getNavigator();
        navigation.logout(getXsrfToken());
    }

    public void testEdit() throws Exception
    {
        navigation.editIssue("HSP-1");
        final String locator = Locators.JQUERY.addPrefix("form#issue-edit");
        final LabelsPicker labels = LabelsPicker.newSystemLabelsPicker(locator, context());
        labels.insertQuery("something").confirmInput();
        labels.assertElementPicked("something");

        client.click("issue-edit-submit", true);
        assertLabelsPresentOnIssuePage("#labels-10000-value li a.lozenge[title='%s'] span", "something");
    }

    private void assertLabelsPresentOnIssuePage(String valueSelector, String firstExpectedLabel, String... restExpectedLabels)
    {
        String firstExpectedLabelSelector = String.format(valueSelector, firstExpectedLabel);
        Locator locator = SeleniumLocators.jQuery(firstExpectedLabelSelector, context());
        assertThat(locator.element().isPresent(), isTrue().byDefaultTimeout());

        for (String expectedLabel : restExpectedLabels)
        {
            String restExpectedLabelSelector = String.format(valueSelector, expectedLabel);
            Locator restLocator = SeleniumLocators.jQuery(restExpectedLabelSelector, context());
            assertThat(restLocator.element().isPresent(), isTrue().byDefaultTimeout());
        }
    }
}
