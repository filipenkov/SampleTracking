package com.atlassian.jira.webtests.ztests.user;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;

@WebTest ({ Category.FUNC_TEST, Category.USERS_AND_GROUPS })
public class TestUserPickerGroupSearcher extends JIRAWebTest
{
    public TestUserPickerGroupSearcher(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreData("TestUserPickerGroupSearcher.xml");
    }

    public void tearDown()
    {
        restoreBlankInstance();
        super.tearDown();
    }

    /**
     * Check that the custom field searcher is visible under correct search contexts (issue type & project)
     * with the custom fields shown in any field schemes.
     */
    public void testUserPickerGroupSearcherVisibilityInDifferentSearchContextsAndFieldShown()
    {
        //all projects/issue types
        assertSearcherVisibleInNewSearch();
        clickLink("find_link");
        selectOption("pid", "All projects");
        selectOption("type", "Any");
        assertUserPickerGroupSearcherPresent("User Picker in all context", "customfield_10000");
        assertUserPickerGroupSearcherNotPresent("User Picker by issue type", "customfield_10001");
        assertUserPickerGroupSearcherNotPresent("User Picker by project", "customfield_10002");
        assertUserPickerGroupSearcherNotPresent("User Picker by type and project", "customfield_10003");

        //project: ANY, IssueTypes: Bug
        assertSearcherVisibleInNewSearch();
        selectOption("pid", "All projects");
        selectOption("type", "Bug");
        submit("show");
        assertUserPickerGroupSearcherPresent("User Picker in all context", "customfield_10000");
        assertUserPickerGroupSearcherPresent("User Picker by issue type", "customfield_10001");
        assertUserPickerGroupSearcherNotPresent("User Picker by project", "customfield_10002");
        assertUserPickerGroupSearcherNotPresent("User Picker by type and project", "customfield_10003");

        //project: MKY, IssueTypes: ANY
        assertSearcherVisibleInNewSearch();
        selectOption("pid", "monkey");
        selectOption("type", "Any");
        submit("show");
        assertUserPickerGroupSearcherPresent("User Picker in all context", "customfield_10000");
        assertUserPickerGroupSearcherNotPresent("User Picker by issue type", "customfield_10001");
        assertUserPickerGroupSearcherNotPresent("User Picker by project", "customfield_10002");
        assertUserPickerGroupSearcherNotPresent("User Picker by type and project", "customfield_10003");

        //project: MKY, IssueTypes: Bug
        assertSearcherVisibleInNewSearch();
        selectOption("pid", "monkey");
        selectOption("type", "Bug");
        submit("show");
        assertUserPickerGroupSearcherPresent("User Picker in all context", "customfield_10000");
        assertUserPickerGroupSearcherPresent("User Picker by issue type", "customfield_10001");
        assertUserPickerGroupSearcherNotPresent("User Picker by project", "customfield_10002");
        assertUserPickerGroupSearcherNotPresent("User Picker by type and project", "customfield_10003");

        //project: HSP, IssueTypes: ANY
        assertSearcherVisibleInNewSearch();
        selectOption("pid", "homosapien");
        selectOption("type", "Any");
        submit("show");
        assertUserPickerGroupSearcherPresent("User Picker in all context", "customfield_10000");
        assertUserPickerGroupSearcherNotPresent("User Picker by issue type", "customfield_10001");
        assertUserPickerGroupSearcherPresent("User Picker by project", "customfield_10002");
        assertUserPickerGroupSearcherNotPresent("User Picker by type and project", "customfield_10003");

        //project: HSP, IssueTypes: Bug
        assertSearcherVisibleInNewSearch();
        selectOption("pid", "homosapien");
        selectOption("type", "Bug");
        submit("show");
        assertUserPickerGroupSearcherPresent("User Picker in all context", "customfield_10000");
        assertUserPickerGroupSearcherPresent("User Picker by issue type", "customfield_10001");
        assertUserPickerGroupSearcherPresent("User Picker by project", "customfield_10002");
        assertUserPickerGroupSearcherPresent("User Picker by type and project", "customfield_10003");

        //project: HSP, IssueTypes: New Feature
        assertSearcherVisibleInNewSearch();
        selectOption("pid", "homosapien");
        selectOption("type", "New Feature");
        submit("show");
        assertUserPickerGroupSearcherPresent("User Picker in all context", "customfield_10000");
        assertUserPickerGroupSearcherNotPresent("User Picker by issue type", "customfield_10001");
        assertUserPickerGroupSearcherPresent("User Picker by project", "customfield_10002");
        assertUserPickerGroupSearcherPresent("User Picker by type and project", "customfield_10003");
    }

    /**
     * Check that the custom field searcher is hidden if the field is hidden
     */
    public void testUserPickerGroupSearcherVisibilityInDifferentSearchContextsAndFieldHidden()
    {
        //hide all the custom fields
        gotoFieldConfigurationDefault();
        administration.fieldConfigurations().defaultFieldConfiguration().hideFields("User Picker by issue type");
        administration.fieldConfigurations().defaultFieldConfiguration().hideFields("User Picker by project");
        administration.fieldConfigurations().defaultFieldConfiguration().hideFields("User Picker by type and project");
        administration.fieldConfigurations().defaultFieldConfiguration().hideFields("User Picker in all context");

        //all projects/issue types
        assertSearcherNOTVisibleInNewSearch();
        clickLink("find_link");
        selectOption("pid", "All projects");
        selectOption("type", "Any");
        assertUserPickerGroupSearcherNotPresent("User Picker in all context", "customfield_10000");
        assertUserPickerGroupSearcherNotPresent("User Picker by issue type", "customfield_10001");
        assertUserPickerGroupSearcherNotPresent("User Picker by project", "customfield_10002");
        assertUserPickerGroupSearcherNotPresent("User Picker by type and project", "customfield_10003");

        //project: ANY, IssueTypes: Bug
        assertSearcherNOTVisibleInNewSearch();
        selectOption("pid", "All projects");
        selectOption("type", "Bug");
        submit("show");
        assertUserPickerGroupSearcherNotPresent("User Picker in all context", "customfield_10000");
        assertUserPickerGroupSearcherNotPresent("User Picker by issue type", "customfield_10001");
        assertUserPickerGroupSearcherNotPresent("User Picker by project", "customfield_10002");
        assertUserPickerGroupSearcherNotPresent("User Picker by type and project", "customfield_10003");

        //project: MKY, IssueTypes: ANY
        assertSearcherNOTVisibleInNewSearch();
        selectOption("pid", "monkey");
        selectOption("type", "Any");
        submit("show");
        assertUserPickerGroupSearcherNotPresent("User Picker in all context", "customfield_10000");
        assertUserPickerGroupSearcherNotPresent("User Picker by issue type", "customfield_10001");
        assertUserPickerGroupSearcherNotPresent("User Picker by project", "customfield_10002");
        assertUserPickerGroupSearcherNotPresent("User Picker by type and project", "customfield_10003");

        //project: MKY, IssueTypes: Bug
        assertSearcherNOTVisibleInNewSearch();
        selectOption("pid", "monkey");
        selectOption("type", "Bug");
        submit("show");
        assertUserPickerGroupSearcherNotPresent("User Picker in all context", "customfield_10000");
        assertUserPickerGroupSearcherNotPresent("User Picker by issue type", "customfield_10001");
        assertUserPickerGroupSearcherNotPresent("User Picker by project", "customfield_10002");
        assertUserPickerGroupSearcherNotPresent("User Picker by type and project", "customfield_10003");

        //project: HSP, IssueTypes: ANY
        assertSearcherNOTVisibleInNewSearch();
        selectOption("pid", "homosapien");
        selectOption("type", "Any");
        submit("show");
        assertUserPickerGroupSearcherNotPresent("User Picker in all context", "customfield_10000");
        assertUserPickerGroupSearcherNotPresent("User Picker by issue type", "customfield_10001");
        assertUserPickerGroupSearcherNotPresent("User Picker by project", "customfield_10002");
        assertUserPickerGroupSearcherNotPresent("User Picker by type and project", "customfield_10003");

        //project: HSP, IssueTypes: Bug
        assertSearcherNOTVisibleInNewSearch();
        selectOption("pid", "homosapien");
        selectOption("type", "Bug");
        submit("show");
        assertUserPickerGroupSearcherNotPresent("User Picker in all context", "customfield_10000");
        assertUserPickerGroupSearcherNotPresent("User Picker by issue type", "customfield_10001");
        assertUserPickerGroupSearcherNotPresent("User Picker by project", "customfield_10002");
        assertUserPickerGroupSearcherNotPresent("User Picker by type and project", "customfield_10003");

        //project: HSP, IssueTypes: New Feature
        assertSearcherNOTVisibleInNewSearch();
        selectOption("pid", "homosapien");
        selectOption("type", "New Feature");
        submit("show");
        assertUserPickerGroupSearcherNotPresent("User Picker in all context", "customfield_10000");
        assertUserPickerGroupSearcherNotPresent("User Picker by issue type", "customfield_10001");
        assertUserPickerGroupSearcherNotPresent("User Picker by project", "customfield_10002");
        assertUserPickerGroupSearcherNotPresent("User Picker by type and project", "customfield_10003");
    }

    private void assertUserPickerGroupSearcherPresent(String fieldName, String fieldId)
    {
        assertTextPresent(fieldName);
        assertFormElementPresent(fieldId);
        assertFormElementPresent(fieldId + "Select");
    }

    private void assertUserPickerGroupSearcherNotPresent(String fieldName, String fieldId)
    {
        assertTextNotPresent(fieldName);
        assertFormElementNotPresent(fieldId);
        assertFormElementNotPresent(fieldId + "Select");
    }

    private void assertSearcherVisibleInNewSearch()
    {
        resetIssueNavigator();
        submit("show");
        assertUserPickerGroupSearcherPresent("User Picker in all context", "customfield_10000");
        assertUserPickerGroupSearcherNotPresent("User Picker by issue type", "customfield_10001");
        assertUserPickerGroupSearcherNotPresent("User Picker by project", "customfield_10002");
        assertUserPickerGroupSearcherNotPresent("User Picker by type and project", "customfield_10003");
    }

    private void assertSearcherNOTVisibleInNewSearch()
    {
        resetIssueNavigator();
        submit("show");
        assertUserPickerGroupSearcherNotPresent("User Picker in all context", "customfield_10000");
        assertUserPickerGroupSearcherNotPresent("User Picker by issue type", "customfield_10001");
        assertUserPickerGroupSearcherNotPresent("User Picker by project", "customfield_10002");
        assertUserPickerGroupSearcherNotPresent("User Picker by type and project", "customfield_10003");
    }

    private void resetIssueNavigator()
    {
        getNavigation().issueNavigator().displayAllIssues();
        if (getDialog().isLinkPresent("new_filter"))
        {
            clickLink("new_filter");
        }
    }
}
