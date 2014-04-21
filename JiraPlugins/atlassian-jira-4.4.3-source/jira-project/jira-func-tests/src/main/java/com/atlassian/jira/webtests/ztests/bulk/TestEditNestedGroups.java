package com.atlassian.jira.webtests.ztests.bulk;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;
import com.meterware.httpunit.HttpUnitOptions;

@WebTest ({ Category.FUNC_TEST, Category.USERS_AND_GROUPS })
public class TestEditNestedGroups extends JIRAWebTest
{

    private static final String PLEASE_REFRESH_MEMBERS_LIST = "Newly selected group(s) may have different members.";
    private static final String UNASSIGN = "unassign";
    private static final String ASSIGN = "assign";
    private static final String FIELD_GROUPS_TO_UNASSIGN = "childrenToUnassign";
    private static final String FIELD_GROUPS_TO_ASSIGN = "childrenToAssignStr";
    private static final String FIELD_SELECTED_GROUPS = "selectedGroupsStr";

    private static final String ERROR_SELECT_GROUPS = "Please select group(s) to edit";
    private static final String ERROR_SELECT_GROUPS_TO_REMOVE = "Please select groups to remove from the selected group(s)";
    private static final String ERROR_SELECT_GROUPS_TO_ADD = "Please select groups to add to all the selected group(s)";

    public TestEditNestedGroups(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreData("TestEditNestedGroups.xml");
        HttpUnitOptions.setScriptingEnabled(true);
    }

    public void tearDown()
    {
        HttpUnitOptions.setScriptingEnabled(false);
        super.tearDown();
    }

    public void testEditNestedGroupGroupsInvalidGroups()
    {
        gotoPage(page.addXsrfToken("/secure/admin/group/EditNestedGroups.jspa?selectedGroupsStr=invalid&assign=true&childrenToAssignStr=group200"));
        assertTextPresent("The group &#39;invalid&#39; is not a valid group.");
        gotoPage(page.addXsrfToken("/secure/admin/group/EditNestedGroups.jspa?selectedGroupsStr=invalid&unassign=true&childrenToUnassign=group200"));
        assertTextPresent("The group &#39;invalid&#39; is not a valid group.");
    }

    public void testUnassignGroupsFromGroups()
    {
        gotoEditNestedGroups();

        //Remove 1 group from 1 selected group
        // Add admin, dev & users to 200 & 203
        selectMultiOption(FIELD_SELECTED_GROUPS, "group202");
        selectMultiOption(FIELD_GROUPS_TO_ASSIGN, "jira-developers");
        selectMultiOption(FIELD_GROUPS_TO_ASSIGN, "jira-users");
        selectMultiOption(FIELD_GROUPS_TO_ASSIGN, "group203");
        submit(ASSIGN);
        // now test remove
        selectMultiOption(FIELD_SELECTED_GROUPS, "group202");
        selectMultiOption(FIELD_GROUPS_TO_UNASSIGN, "jira-developers");
        submit(UNASSIGN);
        assertTextPresent("Selected 1 of 5 Groups");
        assertTextPresent("2 Group Member(s)");
        //make sure the removed group is not on the list
        String[] options = getDialog().getOptionValuesFor(FIELD_GROUPS_TO_UNASSIGN);
        for (int i = 0; i < options.length; i++)
        {
            String option = options[i];
            assertFalse(option.equals("jira-developers______group202"));
        }

        //remove 2 groups from 2 selected groups
        // Add admin, dev & users to 200 & 203
        selectMultiOption(FIELD_SELECTED_GROUPS, "group200");
        selectMultiOption(FIELD_GROUPS_TO_ASSIGN, "jira-users");
        submit(ASSIGN);
        selectMultiOption(FIELD_SELECTED_GROUPS, "group200");
        selectMultiOption(FIELD_SELECTED_GROUPS, "group202");
        selectMultiOption(FIELD_GROUPS_TO_ASSIGN, "jira-developers");
        selectMultiOption(FIELD_GROUPS_TO_ASSIGN, "group203");
        submit(ASSIGN);
        // now test remove
        selectGroup200And202();
        options = getDialog().getOptionValuesFor(FIELD_GROUPS_TO_UNASSIGN);
        assertTrue(options.length == 3);
        selectMultiOptionByValue(FIELD_GROUPS_TO_UNASSIGN, "jira-users");
        selectMultiOptionByValue(FIELD_GROUPS_TO_UNASSIGN, "jira-developers");
        submit(UNASSIGN);
        assertTextPresent("Selected 2 of 5 Groups");
        //make sure both groups are removed
        options = getDialog().getOptionValuesFor(FIELD_GROUPS_TO_UNASSIGN);
        assertTrue(options.length == 1);
        for (int i = 0; i < options.length; i++)
        {
            String option = options[i];
            assertFalse(option.equals("jira-developers______group202"));
            assertFalse(option.equals("jira-users"));
        }
    }

    public void testAssignGroupsFromGroups()
    {
        gotoEditNestedGroups();

        //Add one group to 1 group from 1 of the selected group
        selectDeveloperGroupOnly();
        assertTextPresent("No groups in selected group(s)");
        selectMultiOption(FIELD_GROUPS_TO_ASSIGN, "group200");
        submit(ASSIGN);
        assertTextPresent("Selected 1 of 5 Groups");
        assertTextPresent("1 Group Member(s)");
        //make sure the added group is on the list
        String[] options = getDialog().getOptionValuesFor(FIELD_GROUPS_TO_UNASSIGN);
        boolean found = false;
        for (int i = 0; i < options.length; i++)
        {
            String option = options[i];
            if (option.equals("group200______jira-developers"))
                found = true;
        }
        assertTrue(found);

        //Add 2 groups to all of the 2 selected groups
        selectGroupsAndDevelopersGroup();
        options = getDialog().getOptionValuesFor(FIELD_GROUPS_TO_UNASSIGN);
        assertTrue(options.length == 1);
        selectMultiOption(FIELD_GROUPS_TO_ASSIGN, "group200");
        selectMultiOption(FIELD_GROUPS_TO_ASSIGN, "group202");
        submit(ASSIGN);
        assertTextPresent("Selected 2 of 5 Groups");
        //make sure both groups are added
        options = getDialog().getOptionValuesFor(FIELD_GROUPS_TO_UNASSIGN);
        //added 2 groups but list length goes up by 1 only
        //because dev is in jira-groups originally, hence it is removed from jira-groups section and displayed under All
        assertEquals(2, options.length);
        boolean found2 = false;
        found = false;
        for (int i = 0; i < options.length; i++)
        {
            String option = options[i];
            if (option.equals("group200")) found = true;
            if (option.equals("group202")) found2 = true;
        }
        assertTrue(found && found2);
    }


    public void testUnassignGroupsFromGroupsValidation()
    {
        // Add admin, dev & users to 200 & 203
        gotoEditNestedGroups();
        selectMultiOption(FIELD_SELECTED_GROUPS, "group200");
        selectMultiOption(FIELD_GROUPS_TO_ASSIGN, "jira-users");
        submit(ASSIGN);
        selectMultiOption(FIELD_SELECTED_GROUPS, "group200");
        selectMultiOption(FIELD_SELECTED_GROUPS, "group202");
        selectMultiOption(FIELD_GROUPS_TO_ASSIGN, "jira-developers");
        selectMultiOption(FIELD_GROUPS_TO_ASSIGN, "group203");
        submit(ASSIGN);

        //try unassiging a group without selecting a group
        gotoEditNestedGroups();
        submit(UNASSIGN);
        assertTextPresent(ERROR_SELECT_GROUPS);

        //select groups and try to remove no groups from them
        selectGroupsAndDevelopersGroup();
        submit(UNASSIGN);
        assertTextPresent(ERROR_SELECT_GROUPS_TO_REMOVE);

        //select two group, select a group and select a new child that does not contain selected group then try unassign without refreshing
        selectMultiOption(FIELD_SELECTED_GROUPS, "group200");
        selectMultiOption(FIELD_SELECTED_GROUPS, "group202");
        refreshMembersList();
        selectMultiOption(FIELD_GROUPS_TO_UNASSIGN, "jira-developers");
        selectOption(FIELD_SELECTED_GROUPS, "group203");
        assertTextPresent(PLEASE_REFRESH_MEMBERS_LIST);
        submit(UNASSIGN);
        assertTextPresent("Cannot remove group &#39;jira-developers&#39; from group &#39;group203&#39; since group is not a member of &#39;group203&#39;");

        //select multiple groups, select a child and select a new group that does not contain selected group then try unassign without refreshing
        selectMultiOption(FIELD_SELECTED_GROUPS, "group200");
        selectMultiOption(FIELD_SELECTED_GROUPS, "group202");
        refreshMembersList();
        selectMultiOption(FIELD_GROUPS_TO_UNASSIGN, "jira-users");
        selectOption(FIELD_SELECTED_GROUPS, "group202");
        assertTextPresent(PLEASE_REFRESH_MEMBERS_LIST);
        submit(UNASSIGN);
        assertTextPresent("Cannot remove group &#39;jira-users&#39; from group &#39;group200&#39; since the group was not selected. Please make sure to refresh after selecting new group(s)");
    }

    public void testAssignGroupsFromGroupsValidation()
    {
        //try assigning no groups with no groups selected
        gotoEditNestedGroups();
        assertTextPresent("Selected 0 of 5 Groups");
        submit(ASSIGN);
        assertTextPresent(ERROR_SELECT_GROUPS);

        //try assigning groups with no groups selected
        gotoEditNestedGroups();
        selectMultiOption(FIELD_GROUPS_TO_ASSIGN, "group200");
        assertTextPresent("Selected 0 of 5 Groups");
        submit(ASSIGN);
        assertTextPresent(ERROR_SELECT_GROUPS);

        //select groups and add without selecting groups to assign
        selectMultiOption(FIELD_SELECTED_GROUPS, "group200");
        selectMultiOption(FIELD_SELECTED_GROUPS, "group202");
        submit(ASSIGN);
        assertTextPresent(ERROR_SELECT_GROUPS_TO_ADD);

        //add a existing member to a group
        //select groups and add without selecting groups to assign
        selectMultiOption(FIELD_SELECTED_GROUPS, "group200");
        selectMultiOption(FIELD_GROUPS_TO_ASSIGN, "group202");
        submit(ASSIGN);
        // And again
        selectMultiOption(FIELD_SELECTED_GROUPS, "group200");
        selectMultiOption(FIELD_GROUPS_TO_ASSIGN, "group202");
        submit(ASSIGN);
        assertTextPresent("Cannot add group &#39;group202&#39;, group is already a member of &#39;group200&#39;");

        //add a existing member to multiple groups
        selectOption(FIELD_SELECTED_GROUPS, "jira-users");
        selectOption(FIELD_GROUPS_TO_ASSIGN, "group202");
        submit(ASSIGN);
        // And again
        selectMultiOption(FIELD_SELECTED_GROUPS, "jira-users");
        selectMultiOption(FIELD_SELECTED_GROUPS, "group200");
        selectMultiOption(FIELD_GROUPS_TO_ASSIGN, "group202");
        submit(ASSIGN);
        assertTextPresent("Cannot add group &#39;group202&#39;, group is already a member of all the selected group(s)");

        // Not to oneself
        selectMultiOption(FIELD_SELECTED_GROUPS, "group200");
        selectMultiOption(FIELD_GROUPS_TO_ASSIGN, "group200");
        submit(ASSIGN);
        assertTextPresent("Cannot add a group to itself.");

        // test recursion
        //select groups and add without selecting groups to assign
        selectMultiOption(FIELD_SELECTED_GROUPS, "group202");
        selectMultiOption(FIELD_GROUPS_TO_ASSIGN, "group200");
        submit(ASSIGN);
        assertTextPresent("Cannot add child group &#39;group200&#39; to parent group &#39;group202&#39; - this would cause a circular dependency.");

        selectMultiOption(FIELD_SELECTED_GROUPS, "group202");
        selectMultiOption(FIELD_GROUPS_TO_ASSIGN, "jira-developers");
        submit(ASSIGN);
        selectMultiOption(FIELD_SELECTED_GROUPS, "jira-developers");
        selectMultiOption(FIELD_GROUPS_TO_ASSIGN, "group200");
        submit(ASSIGN);
        assertTextPresent("Cannot add child group &#39;group200&#39; to parent group &#39;jira-developers&#39; - this would cause a circular dependency.");

    }

    //----------------------------------------------------------------------------------------------------Helper Methods
    private void gotoEditNestedGroups()
    {
        clickOnAdminPanel("admin.groupsgroups", "group_browser");
        clickLink("edit_nested_groups");
        assertTextPresent("This page allows you to edit nested group memberships.");
        assertTextPresent("Selected 0 of 5 Groups");
        assertTextPresent("No groups in selected group(s)");
    }

    private void selectDeveloperGroupOnly()
    {
        selectSingleGroupOnly(getDeveloperOption());
    }

    private void selectAdminGroupOnly()
    {
        selectSingleGroupOnly(getAdminOption());
    }

    private void selectGroupsAndDevelopersGroup()
    {
        selectTwoGroups(getGroupOption(), getDeveloperOption());
    }

    private void selectGroup200And202()
    {
        selectTwoGroups(getGroup200Option(), getGroup202Option());
    }

    private void selectSingleGroupOnly(String group)
    {
        selectMultiOption(FIELD_SELECTED_GROUPS, group);
        assertTextPresent(PLEASE_REFRESH_MEMBERS_LIST);
        refreshMembersList();
        assertTextPresent("Selected 1 of 5 Groups");
    }

    private void selectTwoGroups(String group1, String group2)
    {
        selectMultiOption(FIELD_SELECTED_GROUPS, group1);
        selectMultiOption(FIELD_SELECTED_GROUPS, group2);
        assertTextPresent(PLEASE_REFRESH_MEMBERS_LIST);
        refreshMembersList();
        assertTextPresent("Selected 2 of 5 Groups");
    }

    private void selectAllGroups()
    {
        selectMultiOption(FIELD_SELECTED_GROUPS, getAdminOption());
        selectMultiOption(FIELD_SELECTED_GROUPS, getDeveloperOption());
        selectMultiOption(FIELD_SELECTED_GROUPS, getGroupOption());
        assertTextPresent(PLEASE_REFRESH_MEMBERS_LIST);
        refreshMembersList();
        assertTextPresent("Selected 3 of 5 Groups");
    }

    private void refreshMembersList()
    {
        if (HttpUnitOptions.isScriptingEnabled())
            clickLink("refresh-dependant-fields");
        else
            submit("refresh");
    }

    private String getAdminOption()     { return "group203"; }
    private String getDeveloperOption() { return "jira-developers"; }
    private String getGroupOption()      { return "jira-users"; }
    private String getGroup200Option()  { return "group200"; }
    private String getGroup202Option()  { return "group202"; }
}
