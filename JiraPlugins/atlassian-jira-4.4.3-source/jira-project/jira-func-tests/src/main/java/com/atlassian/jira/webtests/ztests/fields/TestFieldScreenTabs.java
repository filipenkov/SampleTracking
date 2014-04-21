package com.atlassian.jira.webtests.ztests.fields;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;

/**
 * Tests stuff relating to FieldScreenTabs
 */
@WebTest ({ Category.FUNC_TEST, Category.FIELDS })
public class TestFieldScreenTabs extends JIRAWebTest
{
    public TestFieldScreenTabs(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreBlankInstance();
    }

    public void testFieldScreenTabDeleteKeepsOrdering()
    {
        gotoAdmin();

        // Create a field screen with 3 tabs and delete the middle tab
        clickLink("field_screens");
        setFormElement("fieldScreenName", "Test Screen");
        submit("Add");
        setFormElement("fieldScreenName", "");
        clickLink("configure_fieldscreen_Test Screen");
        setFormElement("newTabName", "tab 1");
        submit("Add");
        setFormElement("newTabName", "tab 2");
        submit("Add");
        clickLinkWithText("tab 1");
        clickLink("delete_fieldscreentab");
        submit("Delete");

        // Create a custom field and assign it to the new screen
        clickLink("view_custom_fields");
        clickLink("add_custom_fields");
        checkCheckbox("fieldType", "com.atlassian.jira.plugin.system.customfieldtypes:textarea");
        submit("nextBtn");
        setFormElement("fieldName", "text field");
        submit("nextBtn");

        // add the field to the screen tab
        clickLink("field_screens");
        clickLink("configure_fieldscreen_Test Screen");
        clickLinkWithText("tab 2");
        clickLink("field_screens");
        setFormElement("fieldScreenName", "");
        clickLink("configure_fieldscreen_Test Screen");
        clickLinkWithText("tab 2");
        selectOption("fieldId", "text field");
        submit("Add");

        // browse to the field and see if the link to the screen works
        clickLink("view_custom_fields");
        clickLinkWithText("Test Screen");
        assertTextPresent("Configure Screen");
        assertTextPresent("text field");
    }

}
