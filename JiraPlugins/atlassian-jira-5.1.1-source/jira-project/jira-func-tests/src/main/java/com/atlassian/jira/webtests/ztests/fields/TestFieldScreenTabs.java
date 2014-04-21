package com.atlassian.jira.webtests.ztests.fields;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Tests stuff relating to FieldScreenTabs
 */
@WebTest ({ Category.FUNC_TEST, Category.FIELDS })
public class TestFieldScreenTabs extends FuncTestCase
{
    @Override
    public void setUpTest()
    {
        super.setUpTest();
        backdoor.restoreBlankInstance();
    }

    public void testFieldScreenTabDeleteKeepsOrdering()
    {
        navigation.gotoAdmin();

        // Create a field screen with 3 tabs and delete the middle tab
        tester.clickLink("field_screens");
        tester.clickLink("add-field-screen");
        tester.setFormElement("fieldScreenName", "Test Screen");
        tester.submit("Add");
        tester.clickLink("field_screens");
        tester.clickLink("configure_fieldscreen_Test Screen");
        tester.setFormElement("newTabName", "tab 1");
        tester.submit("Add");
        tester.setFormElement("newTabName", "tab 2");
        tester.submit("Add");
        tester.clickLinkWithText("tab 1");
        tester.clickLink("delete_fieldscreentab");
        tester.submit("Delete");

        // Create a custom field and assign it to the new screen
        tester.clickLink("view_custom_fields");
        tester.clickLink("add_custom_fields");
        tester.checkCheckbox("fieldType", "com.atlassian.jira.plugin.system.customfieldtypes:textarea");
        tester.submit("nextBtn");
        tester.setFormElement("fieldName", "text field");
        tester.submit("nextBtn");

        // add the field to the screen tab
        tester.clickLink("field_screens");
        tester.clickLink("configure_fieldscreen_Test Screen");
        tester.clickLinkWithText("tab 2");
        tester.clickLink("field_screens");
        tester.clickLink("configure_fieldscreen_Test Screen");
        tester.clickLinkWithText("tab 2");
        tester.selectOption("fieldId", "text field");
        tester.submit("Add");

        // browse to the field and see if the link to the screen works
        tester.clickLink("view_custom_fields");
        tester.clickLinkWithText("Test Screen");
        tester.assertTextPresent("Configure Screen");
        tester.assertTextPresent("text field");
    }
}
