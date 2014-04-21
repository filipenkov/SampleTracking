package com.atlassian.jira.webtest.selenium.jql;

import com.atlassian.jira.functest.framework.admin.TimeTracking;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.Quarantine;
import com.atlassian.jira.webtests.JIRAWebTest;
import com.atlassian.selenium.Browser;
import com.atlassian.selenium.SkipInBrowser;

/**
 * Split out this test for our batch builds so it can be balanced better.
 *
 * @since v4.1
 */
@SkipInBrowser(browsers={Browser.IE})  //JS Exception - Responsibility: JIRA Team
@Quarantine
@WebTest({Category.SELENIUM_TEST })
public class TestJqlAutoCompleteFieldsNotVisibleWhenHidden extends AbstractTestJqlAutoComplete
{

    public void testAutocompleteFieldsNotVisibleWhenHidden() throws Exception
    {
        restoreData("TestSearchConstrainedByConfiguration.xml");
        getNavigator().gotoFindIssuesAdvanced();

        final JIRAWebTest funcTest = getWebUnitTest();

        hideFieldAndAssertFieldNoLongerVisible(funcTest, "affectedVersion", "Affects Version/s", "aff");
        hideFieldAndAssertFieldNoLongerVisible(funcTest, "assignee", "Assignee", "ass");
        hideFieldAndAssertFieldNoLongerVisible(funcTest, "Cascading Select CF", "Cas");
        hideFieldAndAssertFieldNoLongerVisible(funcTest, "component", "Component/s", "comp");
        hideFieldAndAssertFieldNoLongerVisible(funcTest, "description", "Description", "desc");

        // The due is special since there is a field called duedate and due
        _testBoldSuggestions("du", new String[]{"due", "duedate"});
        _testOrderByBoldSuggestions("du", new String[]{"due", "duedate"});
        // Hide the field in all configurations
        hideFieldWithName(funcTest, "Due Date");
        assertFieldNoLongerVisible("du", new String[]{}, new String[]{});

        hideFieldAndAssertFieldNoLongerVisible(funcTest, "environment", "Environment", "env");
        hideFieldAndAssertFieldNoLongerVisible(funcTest, "fixVersion", "Fix Version/s", "fix");
        hideFieldAndAssertFieldNoLongerVisible(funcTest, "Free Text Field CF", "free");
        hideFieldAndAssertFieldNoLongerVisible(funcTest, "Group Picker CF", "gro");
        hideFieldAndAssertFieldNoLongerVisible(funcTest, "level", "Security Level", "lev");
        hideFieldAndAssertFieldNoLongerVisible(funcTest, "Multi Checkboxes CF", "\"multi chec");
        hideFieldAndAssertFieldNoLongerVisible(funcTest, "Multi Group Picker CF", "\"multi gr");
        hideFieldAndAssertFieldNoLongerVisible(funcTest, "Multi Select CF", "\"multi sel");
        hideFieldAndAssertFieldNoLongerVisible(funcTest, "Multi User Picker CF", "\"multi user");
        hideFieldAndAssertFieldNoLongerVisible(funcTest, "Number Field CF", "\"numb");
        hideFieldAndAssertFieldNoLongerVisible(funcTest, "priority", "Priority", "\"prior");
        hideFieldAndAssertFieldNoLongerVisible(funcTest, "Project Picker CF", "\"project pi");
        hideFieldAndAssertFieldNoLongerVisible(funcTest, "Radio Buttons CF", "\"radio b");
        hideFieldAndAssertFieldNoLongerVisible(funcTest, "reporter", "Reporter", "repo");

        // The resolution is special since there is a field called resolutiondate
        _testBoldSuggestions("resol", new String[]{"resolution", "resolutiondate", "resolved"});
        _testOrderByBoldSuggestions("resol", new String[]{"resolution", "resolutiondate", "resolved"});
        // Hide the field in all configurations
        hideFieldWithName(funcTest, "Resolution");
        assertFieldNoLongerVisible("resoluti", new String[]{"resolution"}, new String[]{"resolution"});

        hideFieldAndAssertFieldNoLongerVisible(funcTest, "Select List CF", "\"select l");
        hideFieldAndAssertFieldNoLongerVisible(funcTest, "Single Version Picker CF", "\"single v");
        hideFieldAndAssertFieldNoLongerVisible(funcTest, "Text Field 255", "\"text f");
        hideFieldAndAssertFieldNoLongerVisible(funcTest, "URL Field CF", "url");
        hideFieldAndAssertFieldNoLongerVisible(funcTest, "User Picker CF", "\"user pi");
        hideFieldAndAssertFieldNoLongerVisible(funcTest, "Version Picker CF", "\"version p");

        // test parent by disabling subtasks
        _testBoldSuggestions("paren", new String[]{"parent"});
        funcTest.getAdministration().subtasks().disable();
        // Reload the page so we get the changes
        client.getEval("this.browserbot.getCurrentWindow().onbeforeunload = null");
        client.refresh();
        client.waitForPageToLoad(PAGE_LOAD_WAIT);
        // Now make sure it is no longer in the dropdown, NOTE you can not order by parent
        _testBoldSuggestions("paren", new String[]{});

        // test votes by disabling votes
        _testBoldSuggestions("vote", new String[]{ "voter", "votes" });
        _testOrderByBoldSuggestions("vote", new String[]{ "votes" });
        funcTest.getAdministration().generalConfiguration().disableVoting();
        assertFieldNoLongerVisible("vote");

        _testBoldSuggestions("wat", new String[]{ "watcher" });
        funcTest.getAdministration().generalConfiguration().disableWatching();
        assertFieldNoLongerVisible("wat");

        // test time tracking fields by disabling time tracking
        _testTimeTrackingFieldsVisibility(funcTest);

        //submit the search in the end to make sure we don't leave the form in a dirty state!
        client.click("jqlrunquery", true);
    }

    private void _testTimeTrackingFieldsVisibility(final JIRAWebTest funcTest)
            throws InterruptedException
    {
        _testBoldSuggestions("originalEstima", new String[]{"originalEstimate"});
        _testOrderByBoldSuggestions("originalEstima", new String[]{"originalEstima"});
        _testBoldSuggestions("remainingEstimat", new String[]{"remainingEstimate"});
        _testOrderByBoldSuggestions("remainingEstimat", new String[]{"remainingEstimate"});
        _testBoldSuggestions("timeestimat", new String[]{"timeestimate"});
        _testOrderByBoldSuggestions("timeestimat", new String[]{"timeestimate"});
        _testBoldSuggestions("timeoriginalestimat", new String[]{"timeoriginalestimate"});
        _testOrderByBoldSuggestions("timeoriginalestimat", new String[]{"timeoriginalestimate"});
        _testBoldSuggestions("timespen", new String[]{"timespent"});
        _testOrderByBoldSuggestions("timespen", new String[]{"timespent"});
        // deactivate time tracking
        funcTest.getAdministration().timeTracking().disable();
        assertFieldNoLongerVisible("originalEstima");
        assertFieldNoLongerVisible("remainingEstimat");
        assertFieldNoLongerVisible("timeestimat");
        assertFieldNoLongerVisible("timeoriginalestimat");
        assertFieldNoLongerVisible("timespen");

        // Re-enable time tracking
        funcTest.getAdministration().timeTracking().enable(TimeTracking.Mode.LEGACY);
        client.getEval("this.browserbot.getCurrentWindow().onbeforeunload = null");
        client.refresh();
        client.waitForPageToLoad(PAGE_LOAD_WAIT);

        // test time tracking by disabling the time tracking field
        // Make sure it is there
        _testBoldSuggestions("originalEstima", new String[]{"originalEstimate"});
        _testOrderByBoldSuggestions("originalEstima", new String[]{"originalEstima"});
        _testBoldSuggestions("remainingEstimat", new String[]{"remainingEstimate"});
        _testOrderByBoldSuggestions("remainingEstimat", new String[]{"remainingEstimate"});
        _testBoldSuggestions("timeestimat", new String[]{"timeestimate"});
        _testOrderByBoldSuggestions("timeestimat", new String[]{"timeestimate"});
        _testBoldSuggestions("timeoriginalestimat", new String[]{"timeoriginalestimate"});
        _testOrderByBoldSuggestions("timeoriginalestimat", new String[]{"timeoriginalestimate"});
        _testBoldSuggestions("timespen", new String[]{"timespent"});
        _testOrderByBoldSuggestions("timespen", new String[]{"timespent"});
        // Hide the field
        hideFieldWithName(funcTest, "Time Tracking");
        client.getEval("this.browserbot.getCurrentWindow().onbeforeunload = null");
        client.refresh();
        client.waitForPageToLoad(PAGE_LOAD_WAIT);
        waitFor(1500);
        _testBoldSuggestions("originalEstima", new String[] {});
        _testBoldSuggestions("remainingEstimat", new String[] {});
        _testBoldSuggestions("timeestimat", new String[] {});
        _testBoldSuggestions("timeoriginalestimat", new String[] {});
        _testBoldSuggestions("timespen", new String[] {});
        // Not testing the order by for these since there is an existing bug in the DefaultFieldManager such that
        // it returns the time tracking fields as "availableNavigable" fields even when the TimeTracking field
        // is hidden. Not really a big deal
    }
}