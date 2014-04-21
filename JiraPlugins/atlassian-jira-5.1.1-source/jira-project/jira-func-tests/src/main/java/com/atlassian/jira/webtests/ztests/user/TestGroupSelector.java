package com.atlassian.jira.webtests.ztests.user;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.Groups;
import com.meterware.httpunit.HttpUnitOptions;

@WebTest ({ Category.FUNC_TEST, Category.BROWSING })
public class TestGroupSelector extends FuncTestCase
{
    public static final String GROUP_PICKER_CF_NAME = "mypickerofgroups";
    public static final String INVALID_GROUP_NAME = "invalid_group_name";
    public static final String ISSUE_SUMMARY = "This is my summary";

    private String groupPickerId = null;

    @Override
    public void setUpTest()
    {
        super.setUpTest();
        administration.restoreBlankInstance();
        HttpUnitOptions.setScriptingEnabled(true);

        groupPickerId = administration.customFields().
                addCustomField("com.atlassian.jira.plugin.system.customfieldtypes:grouppicker", GROUP_PICKER_CF_NAME);
    }

    public void testCreateIssueWithGroupPicker()
    {
        // Start the create issue operation
        navigation.issue().goToCreateIssueForm(null, null);
        tester.setFormElement("summary", ISSUE_SUMMARY);

        // Assert that the group picker is available
        tester.assertLinkPresent(groupPickerId + "-trigger");

        // Attempt to add invalid group name
        tester.setFormElement(groupPickerId, INVALID_GROUP_NAME);
        tester.submit("Create");
        text.assertTextPresentHtmlEncoded("Could not find group with name '" + INVALID_GROUP_NAME + "'");

        tester.setFormElement(groupPickerId, Groups.USERS);
        tester.submit("Create");

        text.assertTextPresent(locator.page(), ISSUE_SUMMARY);
        text.assertTextPresent(locator.page(), GROUP_PICKER_CF_NAME);
        tester.assertTextPresent(Groups.USERS);
    }

    public void testIssueNavWithGroupPicker()
    {
        // Create issue with JIRA-USER group selected
        navigation.issue().goToCreateIssueForm(null, null);
        tester.setFormElement("summary", ISSUE_SUMMARY);
        tester.setFormElement(groupPickerId, Groups.USERS);
        tester.submit("Create");

        navigation.issueNavigator().gotoNavigator();
        text.assertTextPresent(locator.page(), GROUP_PICKER_CF_NAME);

        // Assert that the group picker is available
        tester.assertLinkPresent("searcher-" + groupPickerId + "-trigger");

        // Search for invalid group name in group picker
        tester.setFormElement(groupPickerId, INVALID_GROUP_NAME);
        tester.submit("show");

        // Assert errors are displayed
        assertions.getTextAssertions().assertTextPresentHtmlEncoded("Could not find group with name '" + INVALID_GROUP_NAME + "'");
        text.assertTextPresent(locator.page(), "There are errors with your search query on the left, please correct them before continuing.");

        // Search for valid group
        tester.setFormElement(groupPickerId, Groups.USERS);
        tester.submit("show");
        text.assertTextPresent(locator.page(), "HSP-1");
    }
}
