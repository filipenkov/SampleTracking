package com.atlassian.jira.webtests.ztests.customfield;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;
import com.atlassian.jira.webtests.Permissions;

/**
 * Test for JRA-13808
 *
 */
@WebTest ({ Category.FUNC_TEST, Category.CUSTOM_FIELDS, Category.FIELDS, Category.PERMISSIONS })
public class TestCustomFieldsNoSearcherPermissions extends JIRAWebTest
{
    public TestCustomFieldsNoSearcherPermissions(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreData("TestCustomFieldsNoSearcherPermissions.xml");
    }

    /**
     * Tests to ensure that setting the searcher of a customfield that's used in a permission scheme or issue level
     * security scheme will throw an error.
     */
    public void testEditCustomFieldSetSearcherToNone()
    {
        gotoAdmin();
        clickLink("view_custom_fields");
        clickLink("edit_User picker");
        setFormElement("name", "User picker");
        //set the searcher to None.
        selectOption("searcher", "None");
        submit("Update");

        //check that the update didn't succeed.
        assertTextPresent("Search Template cannot be set to &#39;None&#39; because this custom field is used in the following Permission Scheme(s): Default Permission Scheme");
        assertTextPresent("Search Template cannot be set to &#39;None&#39; because this custom field is used in the following Issue Level Security Scheme(s): TestScheme");

        gotoAdmin();
        clickLink("view_custom_fields");
        clickLink("edit_multigrouppicker");
        setFormElement("name", "multigrouppicker");
        selectOption("searcher", "None");
        submit("Update");

        assertTextPresent("Search Template cannot be set to &#39;None&#39; because this custom field is used in the following Permission Scheme(s): Default Permission Scheme");
        assertTextPresent("Search Template cannot be set to &#39;None&#39; because this custom field is used in the following Issue Level Security Scheme(s): TestScheme");
    }

    /**
     * Tests to ensure that deleting a customfield that's used in a permission scheme or issue level
     * security scheme will throw an error.
     */
    public void testDeleteCustomField()
    {
        //try deleting the user picker field
        gotoAdmin();
        clickLink("view_custom_fields");
        clickLink("del_customfield_10000");
        submit("Delete");
        assertTextPresent("Custom field cannot be deleted because it is used in the following Permission Scheme(s): Default Permission Scheme");
        assertTextPresent("Custom field cannot be deleted because it is used in the following Issue Level Security Scheme(s): TestScheme");

        //try deleting the group picker field.
        gotoAdmin();
        clickLink("view_custom_fields");
        clickLink("del_customfield_10001");
        submit("Delete");
        assertTextPresent("Custom field cannot be deleted because it is used in the following Permission Scheme(s): Default Permission Scheme");
        assertTextPresent("Custom field cannot be deleted because it is used in the following Issue Level Security Scheme(s): TestScheme");
    }

    public void testAddCustomFieldWithoutSearcherToPermission()
    {
        // We shouldn't be able to use the "nosearchercf" Custom Field in a Permission scheme because it has no searcher.
        gotoAdmin();
        clickLink("permission_schemes");
        clickLinkWithText("Default Permission Scheme");
        clickLink("add_perm_11");
        checkCheckbox("type", "userCF");
        selectOption("userCF", "nosearchercf");
        submit(" Add ");
        assertTextPresent("Custom field &#39;nosearchercf&#39; is not indexed for searching - please add a Search Template to this Custom Field.");
    }

    public void testAddCustomFieldWithoutSearcherToIssueLevelPermission()
    {
        // We shouldn&#39;t be able to use the "nosearchercf" Custom Field in an Issue Level Permission because it has no searcher.
        gotoAdmin();
        clickLink("security_schemes");
        clickLinkWithText("Security Levels");
        clickLink("add_TestLevel");
        checkCheckbox("type", "userCF");
        selectOption("userCF", "nosearchercf");
        submit(" Add ");
        assertTextPresent("Custom field &#39;nosearchercf&#39; is not indexed for searching - please add a Search Template to this Custom Field.");
    }

    /** Test that adding a searcher to the customfield, makes it possible for that customfield to be added to a permission. */
    public void testAddingSearcherToCustomField()
    {
        gotoAdmin();
        clickLink("view_custom_fields");
        clickLink("edit_nosearchercf");
        selectOption("searcher", "User Picker Searcher");
        submit("Update");
        clickLink("permission_schemes");
        clickLink("0_edit");
        clickLink("add_perm_25");
        checkCheckbox("type", "userCF");
        selectOption("userCF", "nosearchercf");
        submit(" Add ");
        assertTextPresent("Default Permission Scheme");
        assertTextSequence(new String[] { "Move Issues", "nosearchercf" });
    }

    /** Test that we can remove a customfield, after we&#39;ve removed it from permission and issuelevelschemes */
    public void testRemovingCustomField()
    {
        //remove from permission scheme
        gotoAdmin();
        clickLink("permission_schemes");
        clickLink("0_edit");
        clickLink("del_perm_12_customfield_10000");
        submit("Delete");

        //remove from security scheme
        clickLink("security_schemes");
        clickLinkWithText("Security Levels");
        clickLink("delGroup_customfield_10000_TestLevel");
        submit("Delete");

        //delete the custom field
        clickLink("view_custom_fields");
        clickLink("del_customfield_10000");
        submit("Delete");

        assertTextPresent("Custom Fields");
        assertTextNotPresent("User picker");
    }

    /**
     * Tests to ensure that deleting a customfield that&#39;s used in a permission scheme or issue level
     * security scheme will throw an error.
     */
    public void testDeleteIssueSecurityLevelFlushesCache()
    {
        //grant the admin user permission to set issue security and add the scheme for the HSP project
        grantPermissionToUserInEnterprise(Permissions.SET_ISSUE_SECURITY, ADMIN_USERNAME);
        associateIssueLevelSecuritySchemeToProject("homosapien", "TestScheme");
        //grant the admin user permission to the security level.
        gotoAdmin();
        clickLink("security_schemes");
        clickLinkWithText("Security Levels");
        clickLink("add_TestLevel");
        checkCheckbox("type", "user");
        setFormElement("user", ADMIN_USERNAME);
        submit(" Add ");

        //check that the level is present.
        gotoIssue("HSP-1");
        clickLink("edit-issue");
        assertTextPresent("TestLevel");

        //add the admin to the user CF
        setFormElement("customfield_10000", ADMIN_USERNAME);
        submit("Update");


        //now lets remove the user -> admin security level.  The level should still be available due to the
        //user CF
        gotoAdmin();
        clickLink("security_schemes");
        clickLinkWithText("Security Levels");
        clickLink("delGroup_admin_TestLevel");
        submit("Delete");

        //check that the level is present.
        gotoIssue("HSP-1");
        clickLink("edit-issue");
        assertTextPresent("TestLevel");

        //now lets delete the user CF.  Level should no longer be present in the issue afterwards. - except for a temporary fix to JRA-14323
        gotoAdmin();
        clickLink("security_schemes");
        clickLinkWithText("Security Levels");
        clickLink("delGroup_customfield_10000_TestLevel");
        submit("Delete");

        // TODO: Remove this with the proper fix to  JRA-14323.
        clickLink("security_schemes");
        clickLinkWithText("Security Levels");
        clickLink("delGroup_customfield_10001_TestLevel");
        submit("Delete");

        //check that the level is no longer present.
        gotoIssue("HSP-1");
        clickLink("edit-issue");
        assertTextNotPresent("TestLevel");
    }

}
