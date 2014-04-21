/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.webtests.ztests.customfield;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.Groups;
import com.atlassian.jira.webtests.JIRAWebTest;
import com.meterware.httpunit.HttpUnitOptions;
import org.xml.sax.SAXException;

@WebTest ({ Category.FUNC_TEST, Category.BROWSING, Category.CUSTOM_FIELDS })
public class TestMultiGroupSelector extends JIRAWebTest
{
    public static final String MULTGROUP_PICKER_CF_NAME = "mypickerofmultigroups";
    public static final String INVALID_GROUP_NAME = "invalid_group_name";
    public static final String ISSUE_SUMMARY = "This is my summary";

    private String multigroupPickerId = null;

    public TestMultiGroupSelector(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        // Clean JIRA instance
        restoreBlankInstance();
        // Enable javascript
        HttpUnitOptions.setScriptingEnabled(true);

        // Add GroupPicker custom field
        multigroupPickerId = addCustomField(CUSTOM_FIELD_TYPE_MULTIGROUPPICKER, FIELD_SCOPE_GLOBAL, MULTGROUP_PICKER_CF_NAME, null, null, null, null);
        addFieldToFieldScreen(DEFAULT_FIELD_SCREEN_NAME, MULTGROUP_PICKER_CF_NAME);
    }

    public void testCreateIssueWithMultiGroupPicker() throws SAXException
    {
        // Start the create issue operation
        navigation.issue().goToCreateIssueForm(null,null);
        setFormElement("summary", ISSUE_SUMMARY);

        // Assert that the group picker link is available
        assertLinkPresent(CUSTOM_FIELD_PREFIX + multigroupPickerId + "-trigger");

        // Attempt to add invalid group name
        setFormElement(CUSTOM_FIELD_PREFIX + multigroupPickerId, INVALID_GROUP_NAME + ", " + INVALID_GROUP_NAME);
        submit("Create");
        assertTextPresent("Could not find group names: " + INVALID_GROUP_NAME + ", " + INVALID_GROUP_NAME);

        setFormElement(CUSTOM_FIELD_PREFIX + multigroupPickerId, Groups.USERS + ", " + Groups.DEVELOPERS);
        submit("Create");

        assertTextPresent(ISSUE_SUMMARY);
        assertTextPresent(MULTGROUP_PICKER_CF_NAME);
        assertTextPresent(Groups.USERS);
        assertTextPresent(Groups.DEVELOPERS);
    }

    public void testIssueNavWithMultiGroupPicker()
    {
        // Create issue with JIRA-USER group selected
        navigation.issue().goToCreateIssueForm(null,null);
        setWorkingForm("issue-create");
        setFormElement("summary", ISSUE_SUMMARY);
        setFormElement(CUSTOM_FIELD_PREFIX + multigroupPickerId, Groups.USERS + ", " + Groups.DEVELOPERS);
        submit("Create");

        clickLink("find_link");
        assertTextPresent(MULTGROUP_PICKER_CF_NAME);

        // Assert that the group picker link is available
        assertLinkPresent("searcher-" + CUSTOM_FIELD_PREFIX + multigroupPickerId + "-trigger");

        // Search for invalid group name in group picker
        setFormElement(CUSTOM_FIELD_PREFIX + multigroupPickerId, INVALID_GROUP_NAME);
        submit("show");
        // Assert errors are displayed
        assertions.getTextAssertions().assertTextPresentHtmlEncoded("Could not find group with name '" + INVALID_GROUP_NAME + "'");
        assertTextPresent("There are errors with your search query on the left, please correct them before continuing.");

        // Search for valid group
        setFormElement(CUSTOM_FIELD_PREFIX + multigroupPickerId, Groups.USERS);
        submit("show");
        assertTextPresent("HSP-1");

        setFormElement(CUSTOM_FIELD_PREFIX + multigroupPickerId, Groups.DEVELOPERS);
        submit("show");
        assertTextPresent("HSP-1");
    }
}
