package com.atlassian.jira.webtests.ztests.fields;

import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;

import java.io.IOException;

@WebTest ({ Category.FUNC_TEST, Category.FIELDS })
public class TestFieldScreens extends JIRAWebTest
{
    String issueKey;
    String issueKey2;
    String customFieldId;
    String customFieldId2;
    private static final String ADDED_SCREEN_NAME = "Test Add Screen";
    private static final String COPIED_SCREEN_NAME = "Test Copy Screen";
    private static final String ADDED_SCREEN_SCHEME_NAME = "Test Add Screen Scheme";
    private static final String COPIED_SCREEN_SCHEME_NAME = "Test Copy Screen Scheme";
    private static final String ADDED_ISSUE_TYPE_SCREEN_SCHEME_NAME = "Test Add Issue Type Screen Scheme";
    private static final String COPIED_ISSUE_TYPE_SCREEN_SCHEME_NAME = "Test Copy Issue Tyep Screen Scheme";
    private static final String CUSTOM_FIELD_NAME = "Animal";
    private static final String CUSTOM_FIELD_NAME_TWO = "Approval Rating";
    private static final String TAB_NAME = "Tab for Testing";
    private static final String DEFAULT_TAB_NAME = "Field Tab";

    public TestFieldScreens(String name)
    {
        super(name);
    }

    public void setUp ()
    {
        super.setUp();

        login(ADMIN_USERNAME);
        restoreBlankInstance();

        resetSettings();
        customFieldId = addCustomField("textfield", "global", CUSTOM_FIELD_NAME, "custom field 1", null, null, null);
        customFieldId2 = addCustomField("textfield", "global", CUSTOM_FIELD_NAME_TWO, "custom field 2", null, null, null);
        issueKey = addIssue(PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, "Bug", "test field screen", "Minor", null,null,null, ADMIN_FULLNAME, "priority is added to assign issue screen", "test description for field screens", null, null, null);
        createIssueWithCustomField();
    }

    public void tearDown ()
    {
        try
        {
            deleteAllIssuesInAllPages();
            removeAllCustomFields();
            super.tearDown();
        }
        catch (Throwable t)
        {
            log("Some problem in tear down of " + getClass().getName(), t);
        }
        finally
        {
            customFieldId = null;
            customFieldId2 = null;
            issueKey = null;
        }
    }

    public void testFieldScreens()
    {
        fieldScreensAddFieldToFieldScreen();
        fieldScreensAddFieldToFieldScreenWithInvalidPosition();
        fieldScreensSetFieldInWorkflow();
        fieldScreensRemoveFieldFromFieldScreen();
        fieldScreensAddScreen();
        fieldScreensAddScreenWithDuplicateName();
        fieldScreensAddScreenWithInvalidName();
        fieldScreensStandardScreens();

        fieldScreensAddScreenScheme();
        fieldScreensAddScreenSchemeWithDuplicateName();
        fieldScreensAddScreenSchemeWithInvalidName();

        fieldScreensAddIssueTypeScreenScheme();
        fieldScreensAddIssueTypeScreenSchemeWithDuplicateName();
        fieldScreensAddIssueTypeScreenSchemeWithInvalidName();
        fieldScreensAddIssueTypeToScreenAssociation();

        fieldScreensProjectScreenSchemes();

        fieldScreensIssueTypeScreenSchemes();
        fieldScreensAddTab();
        fieldScreensAddTabWithDuplicateName();
        fieldScreensAddTabWithInvalidName();
        fieldScreensAddFieldToTab();
        fieldScreensTabViews();
        fieldScreensRemoveFieldFromTab();
        fieldScreensDeleteTab();
        fieldScreensCopyIssueTypeScreenSchemes();
        fieldScreensDeleteIssueTypeScreenSchemes();

        fieldScreensCopyScreenScheme();
        fieldScreensDeleteScreenScheme();

//        fieldScreensWithRequiredFields(); todo
        fieldScreensCopyScreen();
        fieldScreensDeleteScreen();
    }

    public void testFieldScreensOrdering()
    {
        log("Testing Ordering of options for fields in field screens");
        String optionValue[] = {"null", "Summary", "Issue Type", "Description", "Priority", "Reporter"};
        String optionId[] = {"null", "summary", "issuetype", "description", "priority", "reporter"};

        resetInAscendingOrdering(optionId, "Field");
        checkOrderingUsingArrows(optionValue, optionId);

        checkOrderingUsingMoveToPos(optionValue, optionId, "Field");
        removeAllFieldScreens();
    }

    /**
     * Check that fields are added correctly and in the right position
     */
    private void fieldScreensAddFieldToFieldScreen()
    {
        String expectedRow = "1";

        addFieldToFieldScreen(ASSIGN_FIELD_SCREEN_NAME, CUSTOM_FIELD_NAME);
        if (findRowWithName(FIELD_TABLE_ID, SCREEN_TABLE_NAME_COLUMN_INDEX, CUSTOM_FIELD_NAME) == null)
        {
            fail("Field " + CUSTOM_FIELD_NAME + " was not added");
        }
        addFieldToFieldScreen(ASSIGN_FIELD_SCREEN_NAME, "Issue Type", "2");

        String issueTypeRow = findRowWithName(FIELD_TABLE_ID, SCREEN_TABLE_NAME_COLUMN_INDEX, "Issue Type");
        assertTrue(issueTypeRow.equals(expectedRow));
    }

    /**
     * Test error checking if an invalid field position is entered.
     */
    private void fieldScreensAddFieldToFieldScreenWithInvalidPosition()
    {
        addFieldToFieldScreen(ASSIGN_FIELD_SCREEN_NAME, "Affects Version/s", "0");
        assertTextPresent("Invalid field position.");
    }

    /**
     * Test the configuration of the assign issue screen
     */
    private void fieldScreensSetFieldInWorkflow()
    {
        gotoIssue(issueKey);
        clickLinkWithText("Close Issue");
        assertFormElementNotPresent(CUSTOM_FIELD_PREFIX + customFieldId);
        setWorkingForm("issue-workflow-transition");
        submit("Transition");
        clickLinkWithText("Reopen Issue");
        setFormElement(CUSTOM_FIELD_PREFIX + customFieldId, "Polar Bear");
        setWorkingForm("issue-workflow-transition");
        submit("Transition");
    }

    /**
     * Test that field screens can be removed from field screens
     */
    private void fieldScreensRemoveFieldFromFieldScreen()
    {
        String[] fieldNames = new String[] {CUSTOM_FIELD_NAME, "Issue Type"};

        removeFieldFromFieldScreen(ASSIGN_FIELD_SCREEN_NAME, fieldNames);

        for (int i = 0;i < fieldNames.length; i++)
        {
            if (findRowWithName(FIELD_TABLE_ID, SCREEN_TABLE_NAME_COLUMN_INDEX, fieldNames[i]) != null)
            {
                fail("Field " + fieldNames[i] + " was not removed");
            }
        }
    }

    /**
     * Test that a screen is added properly
     */
    private void fieldScreensAddScreen()
    {
        addScreen(ADDED_SCREEN_NAME, "");
        assertLinkPresent("delete_fieldscreen_" + ADDED_SCREEN_NAME);
    }

    /**
     * Test error checking if a screen is added with a duplicate name
     */
    private void fieldScreensAddScreenWithDuplicateName()
    {
        addScreen(ADDED_SCREEN_NAME, "");
        assertTextPresent("A Screen with this name already exists.");
    }

    /**
     * Test error checking if a screen is added with a empty string as the name.
     */
    private void fieldScreensAddScreenWithInvalidName()
    {
        addScreen("", "");
        assertTextPresent("You must enter a valid name.");
    }

    /**
     * Check screen is deleted correctly
     */
    private void fieldScreensDeleteScreen()
    {
        deleteScreen(ADDED_SCREEN_NAME);
        assertLinkNotPresent("delete_fieldscreen_" + ADDED_SCREEN_NAME);
        deleteScreen(COPIED_SCREEN_NAME);
        assertLinkNotPresent("delete_fieldscreen_" + COPIED_SCREEN_NAME);
    }

    /**
     * Check screen is copied correctly
     */
    private void fieldScreensCopyScreen()
    {
        copyScreen(ASSIGN_FIELD_SCREEN_NAME, COPIED_SCREEN_NAME, "");
        clickLink("configure_fieldscreen_" + ASSIGN_FIELD_SCREEN_NAME);
        if (findRowWithName(FIELD_TABLE_ID, SCREEN_TABLE_NAME_COLUMN_INDEX, "Assignee") != null)
        {
                assertFormElementNotPresent("removeField_1");
        }
        else
            fail("Fields not copied");
    }

    /**
     * Check a screen scheme is added
     */
    private void fieldScreensAddScreenScheme()
    {
        addFieldScreenScheme(ADDED_SCREEN_SCHEME_NAME, "", DEFAULT_FIELD_SCREEN_NAME);
        assertLinkPresent("delete_fieldscreenscheme_" + ADDED_SCREEN_SCHEME_NAME);
    }

    /**
     * Test error checking if a screen scheme is added with a duplicate name
     */
    private void fieldScreensAddScreenSchemeWithDuplicateName()
    {
        addFieldScreenScheme(ADDED_SCREEN_SCHEME_NAME, "", DEFAULT_FIELD_SCREEN_NAME);
        assertTextPresent("A screen scheme with this name already exists.");
    }

    /**
     * Test error checking if a screen scheme is added with an invalid name
     */
    private void fieldScreensAddScreenSchemeWithInvalidName()
    {
        addFieldScreenScheme("", "", DEFAULT_FIELD_SCREEN_NAME);
        assertTextPresent("You must enter a valid name.");
    }

    /**
     * Check that the screen scheme is copied correctly
     */
    private void fieldScreensCopyScreenScheme()
    {
        copyFieldScreenScheme(ADDED_SCREEN_SCHEME_NAME, COPIED_SCREEN_SCHEME_NAME, "");
        clickLink("configure_fieldscreenscheme_" + COPIED_SCREEN_SCHEME_NAME);
        assertLinkPresent("edit_fieldscreenscheme_" + DEFAULT_OPERATION_SCREEN);
//        assertLinkPresent("edit_fieldscreenscheme_" + CREATE_ISSUE_OPERATION_SCREEN);
    }

    /**
     * Check that the screen scheme is deleted
     */
    private void fieldScreensDeleteScreenScheme()
    {
        deleteFieldScreenScheme(COPIED_SCREEN_SCHEME_NAME);
        assertLinkNotPresent("delete_fieldscreenscheme_" + COPIED_SCREEN_SCHEME_NAME);
        deleteFieldScreenScheme(ADDED_SCREEN_SCHEME_NAME);
        assertLinkNotPresent("delete_fieldscreenscheme_" + ADDED_SCREEN_SCHEME_NAME);
    }

    private void fieldScreensAddIssueTypeScreenScheme()
    {
        addIssueTypeFieldScreenScheme(ADDED_ISSUE_TYPE_SCREEN_SCHEME_NAME, "", DEFAULT_SCREEN_SCHEME);
        assertLinkPresent("delete_issuetypescreenscheme_" + ADDED_ISSUE_TYPE_SCREEN_SCHEME_NAME);
    }

    private void fieldScreensAddIssueTypeScreenSchemeWithDuplicateName()
    {
        addIssueTypeFieldScreenScheme(ADDED_ISSUE_TYPE_SCREEN_SCHEME_NAME, "", DEFAULT_SCREEN_SCHEME);
        assertTextPresent("A scheme with this name already exists.");
    }

    private void fieldScreensAddIssueTypeScreenSchemeWithInvalidName()
    {
        addIssueTypeFieldScreenScheme("", "", DEFAULT_SCREEN_SCHEME);
        assertTextPresent("You must enter a valid name.");
    }

    private void fieldScreensAddIssueTypeToScreenAssociation()
    {
        addIssueTypeToScreenAssociation(ADDED_ISSUE_TYPE_SCREEN_SCHEME_NAME, "Bug", ADDED_SCREEN_SCHEME_NAME);
        assertLinkPresent("delete_issuetypescreenschemeentity_Bug");
    }

    private void fieldScreensCopyIssueTypeScreenSchemes()
    {
        copyIssueTypeFieldScreenSchemeName(ADDED_ISSUE_TYPE_SCREEN_SCHEME_NAME, COPIED_ISSUE_TYPE_SCREEN_SCHEME_NAME, "");
        clickLink("configure_issuetypescreenscheme_" + COPIED_ISSUE_TYPE_SCREEN_SCHEME_NAME);
        assertLinkPresent("edit_issuetypescreenschemeentity_default");
        assertLinkPresent("edit_issuetypescreenschemeentity_Bug");
    }

    private void fieldScreensDeleteIssueTypeScreenSchemes()
    {
        deleteIssueTypeFieldScreenScheme(COPIED_ISSUE_TYPE_SCREEN_SCHEME_NAME);
        assertLinkNotPresent("delete_issuetypescreenscheme_" + COPIED_ISSUE_TYPE_SCREEN_SCHEME_NAME);

        associateIssueTypeScreenSchemeToProject(PROJECT_NEO_KEY, DEFAULT_ISSUE_TYPE_SCREEN_SCHEME);
        deleteIssueTypeFieldScreenScheme(ADDED_ISSUE_TYPE_SCREEN_SCHEME_NAME);
        assertLinkNotPresent("delete_issuetypescreenscheme_" + ADDED_ISSUE_TYPE_SCREEN_SCHEME_NAME);
    }

    private void fieldScreensAddTab()
    {
        log("Adding tabs");
        addTabToScreen(ADDED_SCREEN_NAME, TAB_NAME);
        assertTextPresent(TAB_NAME);
        assertLinkPresentWithText(DEFAULT_TAB_NAME);
    }

    private void fieldScreensDeleteTab()
    {
        log("Deleting tabs");
        deleteTabFromScreen(ADDED_SCREEN_NAME, TAB_NAME);
        assertLinkNotPresentWithText(TAB_NAME);
    }

    private void fieldScreensAddFieldToTab()
    {
        addFieldToFieldScreenTab(ADDED_SCREEN_NAME, TAB_NAME, CUSTOM_FIELD_NAME, "");
        if (findRowWithName(FIELD_TABLE_ID, SCREEN_TABLE_NAME_COLUMN_INDEX, CUSTOM_FIELD_NAME) == null)
            fail("Field was not added to tab");
        addFieldToFieldScreenTab(ADDED_SCREEN_NAME, DEFAULT_TAB_NAME, CUSTOM_FIELD_NAME_TWO, "");
        addFieldToFieldScreenTab(ADDED_SCREEN_NAME, DEFAULT_TAB_NAME, "Summary", "");
    }

    private void fieldScreensAddTabWithDuplicateName()
    {
        addTabToScreen(ADDED_SCREEN_NAME, TAB_NAME);
        assertTextPresent("Field Tab with this name already exists.");
    }

    private void fieldScreensAddTabWithInvalidName()
    {
        addTabToScreen(ADDED_SCREEN_NAME, "");
        assertTextPresent("You must enter a valid name.");
    }

    private void fieldScreensRemoveFieldFromTab()
    {
        removeFieldFromFieldScreenTab(ADDED_SCREEN_NAME, TAB_NAME, new String[] {CUSTOM_FIELD_NAME});
        if (findRowWithName(FIELD_TABLE_ID, SCREEN_TABLE_NAME_COLUMN_INDEX, CUSTOM_FIELD_NAME) != null)
            fail("Fields not deleted.");
        removeFieldFromFieldScreenTab(ADDED_SCREEN_NAME, DEFAULT_TAB_NAME, new String[] {"Summary", CUSTOM_FIELD_NAME_TWO});
        if (findRowWithName(FIELD_TABLE_ID, SCREEN_TABLE_NAME_COLUMN_INDEX, CUSTOM_FIELD_NAME_TWO) != null)
            fail("Fields not deleted.");
    }

    /**
     * Check schemes using issue type based schemes
     */
    private void fieldScreensIssueTypeScreenSchemes()
    {
        log("Check schemes using issue type based schemes");
        associateIssueTypeScreenSchemeToProject(PROJECT_NEO_KEY, ADDED_ISSUE_TYPE_SCREEN_SCHEME_NAME);
        addFieldToFieldScreen(ADDED_SCREEN_NAME, "Summary");
        addFieldToFieldScreen(ADDED_SCREEN_NAME, CUSTOM_FIELD_NAME);

        String issueKeyCustomField = checkCreateIssueScreenScheme(ADDED_SCREEN_SCHEME_NAME, PROJECT_NEO, PROJECT_NEO_KEY, "Bug");
        checkViewIssueScreenScheme(ADDED_SCREEN_SCHEME_NAME, PROJECT_NEO, "Bug", issueKeyCustomField);
        checkEditIssueScreenScheme(ADDED_SCREEN_SCHEME_NAME, PROJECT_NEO, "Bug", issueKeyCustomField);

        addIssueOperationToScreenAssociation(ADDED_SCREEN_SCHEME_NAME, CREATE_ISSUE_OPERATION_SCREEN, ADDED_SCREEN_NAME);
        addIssueOperationToScreenAssociation(ADDED_SCREEN_SCHEME_NAME, VIEW_ISSUE_OPERATION_SCREEN, ADDED_SCREEN_NAME);
        addIssueOperationToScreenAssociation(ADDED_SCREEN_SCHEME_NAME, EDIT_ISSUE_OPERATION_SCREEN, ADDED_SCREEN_NAME);
        checkNoScreenScheme(PROJECT_NEO, "Improvement", issueKey2);
        deleteIssueOperationFromScreenAssociation(ADDED_SCREEN_SCHEME_NAME, CREATE_ISSUE_OPERATION_SCREEN);
        deleteIssueOperationFromScreenAssociation(ADDED_SCREEN_SCHEME_NAME, VIEW_ISSUE_OPERATION_SCREEN);
        deleteIssueOperationFromScreenAssociation(ADDED_SCREEN_SCHEME_NAME, EDIT_ISSUE_OPERATION_SCREEN);

        checkNoScreenScheme(PROJECT_NEO, "Bug", issueKeyCustomField);

        removeFieldFromFieldScreen(ADDED_SCREEN_NAME, new String[] {"Summary", CUSTOM_FIELD_NAME});
        associateIssueTypeScreenSchemeToProject(PROJECT_NEO_KEY, DEFAULT_ISSUE_TYPE_SCREEN_SCHEME);
    }

    /**
     * Check screens using project based schemes
     */
    private void fieldScreensProjectScreenSchemes()
    {
        log("Check screens using project based schemes");
        associateIssueTypeScreenSchemeToProject(PROJECT_HOMOSAP_KEY, ADDED_ISSUE_TYPE_SCREEN_SCHEME_NAME);
        addFieldToFieldScreen(ADDED_SCREEN_NAME, "Summary");
        addFieldToFieldScreen(ADDED_SCREEN_NAME, CUSTOM_FIELD_NAME);

        String issueKeyCustomField = checkCreateIssueScreenScheme(ADDED_SCREEN_SCHEME_NAME, PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, "Bug");
        checkViewIssueScreenScheme(ADDED_SCREEN_SCHEME_NAME, PROJECT_HOMOSAP, "Bug", issueKeyCustomField);
        checkEditIssueScreenScheme(ADDED_SCREEN_SCHEME_NAME, PROJECT_HOMOSAP, "Bug", issueKeyCustomField);

        addIssueOperationToScreenAssociation(ADDED_SCREEN_SCHEME_NAME, CREATE_ISSUE_OPERATION_SCREEN, ADDED_SCREEN_NAME);
        addIssueOperationToScreenAssociation(ADDED_SCREEN_SCHEME_NAME, VIEW_ISSUE_OPERATION_SCREEN, ADDED_SCREEN_NAME);
        addIssueOperationToScreenAssociation(ADDED_SCREEN_SCHEME_NAME, EDIT_ISSUE_OPERATION_SCREEN, ADDED_SCREEN_NAME);
        checkNoScreenScheme(PROJECT_NEO, "Bug", issueKey2);
        deleteIssueOperationFromScreenAssociation(ADDED_SCREEN_SCHEME_NAME, CREATE_ISSUE_OPERATION_SCREEN);
        deleteIssueOperationFromScreenAssociation(ADDED_SCREEN_SCHEME_NAME, VIEW_ISSUE_OPERATION_SCREEN);
        deleteIssueOperationFromScreenAssociation(ADDED_SCREEN_SCHEME_NAME, EDIT_ISSUE_OPERATION_SCREEN);

        removeFieldFromFieldScreen(ADDED_SCREEN_NAME, new String[] {"Summary", CUSTOM_FIELD_NAME});
        checkNoScreenScheme(PROJECT_HOMOSAP, "Bug", issueKeyCustomField);

        associateIssueTypeScreenSchemeToProject(PROJECT_HOMOSAP_KEY, DEFAULT_ISSUE_TYPE_SCREEN_SCHEME);
    }

    /**
     * Check screen functionality using standard settings
     */
    private void fieldScreensStandardScreens()
    {
        log("Check screens for standard settings");
        addFieldToFieldScreen(ADDED_SCREEN_NAME, "Summary");
        addFieldToFieldScreen(ADDED_SCREEN_NAME, CUSTOM_FIELD_NAME);

        String issueKeyCustomField = checkCreateIssueScreenScheme(DEFAULT_SCREEN_SCHEME, PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, "Bug");
        checkViewIssueScreenScheme(DEFAULT_SCREEN_SCHEME, PROJECT_HOMOSAP, "Bug", issueKeyCustomField);
        checkEditIssueScreenScheme(DEFAULT_SCREEN_SCHEME, PROJECT_HOMOSAP, "Bug", issueKeyCustomField);

        removeFieldFromFieldScreen(ADDED_SCREEN_NAME, new String[] {"Summary", CUSTOM_FIELD_NAME});
        checkNoScreenScheme(PROJECT_HOMOSAP, "Bug", issueKeyCustomField);
    }

    /**
     * Check the tab functionality in the create, edit and view screens
     */
     protected void fieldScreensTabViews()
    {
        addIssueOperationToScreenAssociation(DEFAULT_SCREEN_SCHEME, CREATE_ISSUE_OPERATION_SCREEN, ADDED_SCREEN_NAME);
        addIssueOperationToScreenAssociation(DEFAULT_SCREEN_SCHEME, VIEW_ISSUE_OPERATION_SCREEN, ADDED_SCREEN_NAME);
        addIssueOperationToScreenAssociation(DEFAULT_SCREEN_SCHEME, EDIT_ISSUE_OPERATION_SCREEN, ADDED_SCREEN_NAME);

        createIssueStep1();

        setFormElement(CUSTOM_FIELD_PREFIX + customFieldId2, "High");
        setFormElement("summary", "This is a test issue");
        setFormElement(CUSTOM_FIELD_PREFIX + customFieldId, "Rhino");

        submit("Create");

        text.assertTextPresent(new XPathLocator(tester, "//ul[@id=\"tabCellPane1\"]/li[1]//strong"), CUSTOM_FIELD_NAME_TWO);
        text.assertTextPresent(new XPathLocator(tester, "//ul[@id=\"tabCellPane1\"]/li[1]//div[@class='wrap']/div"), "High");

        text.assertTextPresent(new XPathLocator(tester, "//ul[@id=\"tabCellPane2\"]/li[1]//strong"), CUSTOM_FIELD_NAME);
        text.assertTextPresent(new XPathLocator(tester, "//ul[@id=\"tabCellPane2\"]/li[1]//div[@class='wrap']/div"), "Rhino");

        clickLink("editIssue");
        setFormElement(CUSTOM_FIELD_PREFIX + customFieldId2, "Low");
        setFormElement(CUSTOM_FIELD_PREFIX + customFieldId, "Tiger");
        submit("Update");

        text.assertTextPresent(new XPathLocator(tester, "//ul[@id=\"tabCellPane1\"]/li[1]//strong"), CUSTOM_FIELD_NAME_TWO);
        text.assertTextPresent(new XPathLocator(tester, "//ul[@id=\"tabCellPane1\"]/li[1]//div[@class='wrap']/div"), "Low");

        text.assertTextPresent(new XPathLocator(tester, "//ul[@id=\"tabCellPane2\"]/li[1]//strong"), CUSTOM_FIELD_NAME);
        text.assertTextPresent(new XPathLocator(tester, "//ul[@id=\"tabCellPane2\"]/li[1]//div[@class='wrap']/div"), "Tiger");

        deleteIssueOperationFromScreenAssociation(DEFAULT_SCREEN_SCHEME, CREATE_ISSUE_OPERATION_SCREEN);
        deleteIssueOperationFromScreenAssociation(DEFAULT_SCREEN_SCHEME, VIEW_ISSUE_OPERATION_SCREEN);
        deleteIssueOperationFromScreenAssociation(DEFAULT_SCREEN_SCHEME, EDIT_ISSUE_OPERATION_SCREEN);
    }

    private void fieldScreensWithRequiredFields()
    {
        setRequiredField(CUSTOM_FIELD_NAME);
        removeFieldFromFieldScreen(DEFAULT_FIELD_SCREEN_NAME, new String[] {"summary", CUSTOM_FIELD_NAME});
        createIssueStep1();
        submit();
        assertTextPresent("Summary: You must specify a summary of the issue.");

        addFieldToFieldScreen(DEFAULT_FIELD_SCREEN_NAME, "summary", "1");
        setRequiredField(CUSTOM_FIELD_NAME);
    }

    // Helper Functions
    private void resetSettings()
    {
        if (projectExists(PROJECT_HOMOSAP))
            log("Project " + PROJECT_HOMOSAP + " exists");
        else
            addProject(PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, ADMIN_USERNAME);
        if (projectExists(PROJECT_NEO))
            log ("Project: " + PROJECT_NEO + " exists");
        else
            addProject(PROJECT_NEO, PROJECT_NEO_KEY, ADMIN_USERNAME);

        associateIssueTypeScreenSchemeToProject(PROJECT_HOMOSAP_KEY, DEFAULT_ISSUE_TYPE_SCREEN_SCHEME);
        associateIssueTypeScreenSchemeToProject(PROJECT_NEO_KEY, DEFAULT_ISSUE_TYPE_SCREEN_SCHEME);
        removeAllIssueTypeScreenSchemes();

        removeFieldFromFieldScreen(ASSIGN_FIELD_SCREEN_NAME, new String[] {CUSTOM_FIELD_NAME, "Issue Type"});

        removeAllCustomFields();
        removeAllScreenAssociationsFromDefault();
        removeAllFieldScreenSchemes();
        removeAllFieldScreens();
    }

    private void createIssueWithCustomField()
    {
        addFieldToFieldScreen(DEFAULT_FIELD_SCREEN_NAME, CUSTOM_FIELD_NAME);
        getNavigation().issue().goToCreateIssueForm(PROJECT_NEO, "Improvement");

        setFormElement("summary", "This is an issue in project 2 with a custom field");
        setFormElement(CUSTOM_FIELD_PREFIX + customFieldId, "Elephant");

        submit();

        issueKey2 = getIssueKey(PROJECT_NEO_KEY);

        removeFieldFromFieldScreen(DEFAULT_FIELD_SCREEN_NAME, new String[] {CUSTOM_FIELD_NAME});
    }

    private String getIssueKey(String projectKey)
    {
        try
        {
            String text = getDialog().getResponse().getText();
            int projectIdLocation = text.indexOf(projectKey);
            int endOfIssueKey = text.indexOf("]", projectIdLocation);
            String issueKeyCustomField = text.substring(projectIdLocation, endOfIssueKey);
            return issueKeyCustomField;
        }
        catch (IOException e)
        {
            fail("Unable to retrieve issue key" + e.getMessage());
        }

        return null;
    }

    /**
     * Check that field screen is not shown for all screens
     */
    private void checkNoScreenScheme(String project, String issueType, String issueKeyCustomField)
    {
        log("Checking scheme association for with no scheme selected");
        createIssueStep1(project, issueType);

        assertFormElementNotPresent(CUSTOM_FIELD_PREFIX + customFieldId);

        submit();

        gotoIssue(issueKeyCustomField);
        assertTextNotPresent(CUSTOM_FIELD_NAME);

        clickLink("editIssue");
        assertFormElementNotPresent(CUSTOM_FIELD_PREFIX + customFieldId);
    }

    /**
     * Check that field screen is shown for the Create Issue screen but not for view issue or edit issue
     */
    private String checkCreateIssueScreenScheme(String screenScheme, String project, String project_key, String issueType)
    {
        log("Checking scheme association for Create");
        addIssueOperationToScreenAssociation(screenScheme, CREATE_ISSUE_OPERATION_SCREEN, ADDED_SCREEN_NAME);

        createIssueStep1(project, issueType);

        setFormElement("summary", "This is a test to see if field is shown");
        setFormElement(CUSTOM_FIELD_PREFIX + customFieldId, "Elephant");

        submit();

        assertTextNotPresent("Elephant");
        assertTextNotPresent(CUSTOM_FIELD_NAME);

        String issueKeyCustomField = getIssueKey(project_key);

        clickLink("editIssue");
        assertFormElementNotPresent(CUSTOM_FIELD_PREFIX + customFieldId);

        deleteIssueOperationFromScreenAssociation(screenScheme, CREATE_ISSUE_OPERATION_SCREEN);

        return issueKeyCustomField;
    }

    /**
     * Check field screen is shown for View issue screen but not create issue or edit issue screens
     */
    private void checkViewIssueScreenScheme(String screenScheme, String project, String issueType, String issueKeyCustomField)
    {
        log("Checking scheme association for View");
        addIssueOperationToScreenAssociation(screenScheme, VIEW_ISSUE_OPERATION_SCREEN, ADDED_SCREEN_NAME);

        createIssueStep1(project, issueType);
        assertFormElementNotPresent(CUSTOM_FIELD_PREFIX + customFieldId);

        gotoIssue(issueKeyCustomField);
        assertTextPresent("Elephant");
        assertTextPresent(CUSTOM_FIELD_NAME);

        clickLink("editIssue");
        assertFormElementNotPresent(CUSTOM_FIELD_PREFIX + customFieldId);

        deleteIssueOperationFromScreenAssociation(screenScheme, VIEW_ISSUE_OPERATION_SCREEN);
    }

    /**
     * Check field screen is shown for Edit issue screen but not create issue or view issue screens
     */
    private void checkEditIssueScreenScheme(String screenScheme, String project, String issueType, String issueKeyCustomField)
    {
        log("Checking scheme association for Edit");
        addIssueOperationToScreenAssociation(screenScheme, EDIT_ISSUE_OPERATION_SCREEN, ADDED_SCREEN_NAME);

        createIssueStep1(project, issueType);
        assertFormElementNotPresent(CUSTOM_FIELD_PREFIX + customFieldId);

        gotoIssue(issueKeyCustomField);
        clickLink("editIssue");

        setFormElement(CUSTOM_FIELD_PREFIX + customFieldId, "Whale");
        submit();
        assertTextNotPresent("Whale");
        assertTextNotPresent(CUSTOM_FIELD_NAME);

        deleteIssueOperationFromScreenAssociation(screenScheme, EDIT_ISSUE_OPERATION_SCREEN);
    }
}
