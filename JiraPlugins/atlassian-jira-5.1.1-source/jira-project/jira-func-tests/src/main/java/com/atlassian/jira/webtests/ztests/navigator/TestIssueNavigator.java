package com.atlassian.jira.webtests.ztests.navigator;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.assertions.TextAssertionsImpl;
import com.atlassian.jira.functest.framework.locator.CssLocator;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation;
import com.atlassian.jira.functest.framework.navigator.IssueTypeCondition;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.CustomFieldValue;
import com.atlassian.jira.webtests.Groups;
import com.atlassian.jira.webtests.JIRAWebTest;
import com.atlassian.jira.webtests.Permissions;
import com.meterware.httpunit.WebLink;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@WebTest ({ Category.FUNC_TEST, Category.ISSUE_NAVIGATOR, Category.ISSUES })
public class TestIssueNavigator extends FuncTestCase
{
    private static final String FIELD_FIX_FOR = "Fix For";
    private static final String FIELD_AFFECTS_VERSION = "Affects Versions";
    private static final String FIELD_COMPONENTS = "Components";
    private static final String CUSTOM_FIELD_GLOBAL = "global custom field";
    private static final String CUSTOM_FIELD_ISSUETYPE = "issueType only custom field";
    private static final String CUSTOM_FIELD_PROJECT = "project only custom field";
    private static final String CUSTOM_FIELD_ISSUETYPE_AND_PROJECT = "issue type &amp; project custom field";
    private static final String PROJECT_DOG = "dog";
    private static final String PROJECT_HOMOSAP = "homosapien";
    private static final String homosapId = "10000";
    private static final Long   homosapIdLong = 1000L;

    private static final Long   hspUnresolvedFilterId = 10010L;

    private static final String GROUP_NAME = "test group";

    @SuppressWarnings("unchecked")
    private static final List<CustomFieldValue>[] cfValuesPerIssue = new ArrayList[]{new ArrayList<CustomFieldValue>(), new ArrayList<CustomFieldValue>(), new ArrayList<CustomFieldValue>()};

    private static final String issueKey = "HSP-1";
    private static final String issueKey2 = "HSP-2";
    private static final String issueKey3 = "HSP-3";
    private static final String issueKey4 = "HSP-4";

    private static final String customFieldIdSelectList = "10000";
    private static final String customFieldIdRadioButton = "10001";
    private static final String customFieldIdMultiSelect = "10002";
    private static final String customFieldIdCheckBox = "10003";
    private static final String customFieldIdTextField = "10004";
    private static final String customFieldIdUserPicker = "10005";
    private static final String customFieldIdDatePicker = "10006";

    private static final String CUSTOM_FIELD_SELECT = "Custom Field Select";
    private static final String CUSTOM_FIELD_RADIO = "Custom Field Radio";
    private static final String CUSTOM_FIELD_MULTI_SELECT = "Custom Field Multi Select";
    private static final String CUSTOM_FIELD_TEXTFIELD = "Custom Field Text Field";
    private static final String CUSTOM_FIELD_CHECKBOX = "Custom Field Check Box";
    private static final String CUSTOM_FIELD_USERPICKER = "Custom Field User Picker";
    private static final String CUSTOM_FIELD_DATEPICKER = "Custom Field Date Picker";

    // test users
    private static final String ABC_USERNAME = "abcuser";

    private static final String DEF_USERNAME = "defuser";

    private static final String GHI_USERNAME = "ghiuser";

    private static final String RESULTS_COUNT_CLASS = ".results-count";



    private static final String[] defaultOptions = new String[]{"abc", "def", "ghi"};
    private static final String[] dateOptions = new String[]{"01/Jan/05", "01/Feb/05", "01/Mar/05"};
    private static final String[] userOptions = new String[]{ABC_USERNAME, DEF_USERNAME, GHI_USERNAME};
    private static final String[] customFieldNames = new String[]{CUSTOM_FIELD_SELECT, CUSTOM_FIELD_RADIO, CUSTOM_FIELD_TEXTFIELD, CUSTOM_FIELD_MULTI_SELECT,
            CUSTOM_FIELD_CHECKBOX, CUSTOM_FIELD_USERPICKER, CUSTOM_FIELD_DATEPICKER};
    private static final String[] customFieldIds = new String[]{customFieldIdSelectList, customFieldIdRadioButton,
            customFieldIdTextField, customFieldIdMultiSelect,
            customFieldIdCheckBox, customFieldIdUserPicker,
            customFieldIdDatePicker};

    public void testSubtaskIssueNavigatorColumn()
    {
        administration.restoreData("TestIssueNavigatorSubtaskColumnView.xml");

        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        navigation.issue().gotoIssue("");
        text.assertTextPresent("HSP-5");
        text.assertTextPresent("HSP-6");
        log("Successfully found subtask issue keys in the subtask issue navigator column");
    }

    protected void removeColumnFromIssueNavigatorByPosition(final int pos)
    {
        tester.clickLink("Profile");
        tester.clickLink("view_nav_columns");

        tester.clickLink("del_col_" + pos);
    }

    public void testNavigatorColumnVisibilityForCustomFields()
    {
        administration.restoreData("TestIssueNavigatorColumnVisibilityForCustomFields.xml");
        final String dogProjectId = "10010";

        // run some issue navigator tests and make sure the columns look right
        // search all projects and make sure none of the columns are visible
        tester.clickLink("find_link");
        tester.submit("show");
        text.assertTextNotPresent(PROJECT_HOMOSAP + " cf");
        text.assertTextNotPresent(PROJECT_HOMOSAP + " bug cf");
        text.assertTextNotPresent(PROJECT_DOG + " cf");
        text.assertTextNotPresent(PROJECT_DOG + " bug cf");

        // search only homosap
        tester.clickLink("new_filter");
        tester.selectOption("pid", PROJECT_HOMOSAP);
        tester.submit("show");
        text.assertTextPresent(PROJECT_HOMOSAP + " cf");
        text.assertTextNotPresent(PROJECT_HOMOSAP + " bug cf");
        text.assertTextNotPresent(PROJECT_DOG + " cf");
        text.assertTextNotPresent(PROJECT_DOG + " bug cf");

        // search homosap bugs
        tester.clickLink("new_filter");
        tester.selectOption("pid", PROJECT_HOMOSAP);
        tester.selectOption("type", "Bug");
        tester.submit("show");
        text.assertTextPresent(PROJECT_HOMOSAP + " cf");
        text.assertTextPresent(PROJECT_HOMOSAP + " bug cf");
        text.assertTextNotPresent(PROJECT_DOG + " cf");
        text.assertTextNotPresent(PROJECT_DOG + " bug cf");

        // search only dog
        tester.clickLink("new_filter");
        tester.selectOption("pid", PROJECT_DOG);
        tester.submit("show");
        text.assertTextNotPresent(PROJECT_HOMOSAP + " cf");
        text.assertTextNotPresent(PROJECT_HOMOSAP + " bug cf");
        text.assertTextPresent(PROJECT_DOG + " cf");
        text.assertTextNotPresent(PROJECT_DOG + " bug cf");

        // search dog bugs
        tester.clickLink("new_filter");
        tester.selectOption("pid", PROJECT_DOG);
        tester.selectOption("type", "Bug");
        tester.submit("show");
        text.assertTextNotPresent(PROJECT_HOMOSAP + " cf");
        text.assertTextNotPresent(PROJECT_HOMOSAP + " bug cf");
        text.assertTextPresent(PROJECT_DOG + " cf");
        text.assertTextPresent(PROJECT_DOG + " bug cf");

        // search both projects
        tester.clickLink("new_filter");
        tester.checkCheckbox("pid", homosapId);
        tester.checkCheckbox("pid", dogProjectId);
        tester.submit("show");
        text.assertTextPresent(PROJECT_HOMOSAP + " cf");
        text.assertTextNotPresent(PROJECT_HOMOSAP + " bug cf");
        text.assertTextPresent(PROJECT_DOG + " cf");
        text.assertTextNotPresent(PROJECT_DOG + " bug cf");

        // search both projects and bugs
        tester.clickLink("new_filter");
        tester.checkCheckbox("pid", homosapId);
        tester.checkCheckbox("pid", dogProjectId);
        tester.selectOption("type", "Bug");
        tester.submit("show");
        text.assertTextPresent(PROJECT_HOMOSAP + " cf");
        text.assertTextPresent(PROJECT_HOMOSAP + " bug cf");
        text.assertTextPresent(PROJECT_DOG + " cf");
        text.assertTextPresent(PROJECT_DOG + " bug cf");
    }

    //Test to see if the project drop down is hidden when there is only one projet
    public void testProjectDropDownVisibility()
    {
        log("Issue Navigator: Test Issue navigator with only one project");
        administration.restoreData("TestIssueNavigatorProjectDropDownVisibility.xml");

        tester.clickLink("find_link");

        Locator issueFilterForm = new IdLocator(tester, "issue-filter");

        //check that there is no project, components, fixfor, and versions drop down list
        text.assertTextPresent(issueFilterForm, "Project");
        text.assertTextNotPresent(issueFilterForm, FIELD_FIX_FOR);
        text.assertTextNotPresent(issueFilterForm, FIELD_COMPONENTS);
        text.assertTextNotPresent(issueFilterForm, FIELD_AFFECTS_VERSION);

        tester.submit("show");
        tester.clickLink("viewfilter");

        tester.assertLinkPresentWithText(PROJECT_HOMOSAP);
        tester.assertLinkPresentWithText(issueKey);
        tester.assertLinkPresentWithText("This issue is in the project " + PROJECT_HOMOSAP);

        navigation.issue().deleteIssue(issueKey);
    }

    /**
     * Test to see if the custom fields, fix for versions, affects versions, and components show up
     * if there is only one visible project.
     */
    public void testProjectCustomFieldsFixForAffectsComponetsVisibilityOneProject()
    {
        log("Issue Navigator: Test Issue navigator with only one project for visible related fields");
        administration.restoreData("TestIssueNavigatorComponentsVisibilityOneProject.xml");

        tester.clickLink("find_link");

        Locator issueFilterForm = new IdLocator(tester, "issue-filter");

        // first go to issue navigator as admin and see that the custom fields do not show and the components do
        // not show
        text.assertTextNotPresent(issueFilterForm, FIELD_FIX_FOR);
        text.assertTextNotPresent(issueFilterForm, FIELD_COMPONENTS);
        text.assertTextNotPresent(issueFilterForm, FIELD_AFFECTS_VERSION);
        text.assertTextNotPresent(issueFilterForm, CUSTOM_FIELD_PROJECT);

        // now login as a mere jira user
        navigation.logout();
        navigation.login(BOB_USERNAME, BOB_PASSWORD);

        tester.clickLink("find_link");

        // we should only be able to see the one project, so make sure all the associated stuff is shown
        text.assertTextPresent(FIELD_FIX_FOR);
        text.assertTextPresent(FIELD_COMPONENTS);
        text.assertTextPresent(FIELD_AFFECTS_VERSION);
        text.assertTextPresent(CUSTOM_FIELD_PROJECT);
    }

    public void testSearchAfterProjectRemoval()
    {
        log("Issue Navigator: Searching right after removal of the last searched project");
        administration.restoreData("TestIssueNavigatorSearchAfterProjectRemoval.xml");

        tester.clickLink("find_link");
        form.selectOptionsByValue("pid",new String[]{homosapId});
        tester.submit("show");
        tester.clickLink("viewfilter");


        administration.project().deleteProject(homosapIdLong);

//        // test saved filter that searches deleted project, should not show hits for deleted project
//        dashboard();
//        clickLinkWithText("Monkey and Homosapien Bugs");
//        assertTextPresent("all 1 issue(s)");
//        assertTextPresent("Some Monkey Bug");
//        assertTextPresent("MKY-1");

        administration.project().addProject(PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, ADMIN_USERNAME);

        navigation.issueNavigator().displayAllIssues();
        text.assertTextNotPresent("A system error has occurred");
    }

    /**
     * tests to check if project components (versions and components) are visible only when a project with a component
     * or a version is selected from the project list
     */
    public void testProjectComponentsVisibility()
    {
        log("Issue Navigator: Test project componenets visibility");
        administration.restoreData("TestIssueNavigatorProjectComponentsVisibility.xml");

        final String projectIdOne = "10010";
        final String projectIdTwo = "10011";
        final String projectIdThree = "10012";
        final String projectIdFour = "10013";

        final String issueKeyOne = "HSP-1";
        final String issueKeyTwo = "MKY-1";
        final String issueKeyThree = "NDT-1";
        final String issueKeyFour = "DOG-1";

        log("  Search all projects ie. there should be no project components section");

        tester.clickLink("find_link");
        form.selectOptionsByValue("pid", new String[]{"-1"});
        tester.submit("show");
        //all issues should be shown
        Locator labelLocator = new XPathLocator(tester,"//*[@id=\"common.concepts.projectcomponents-group\"]//label");
        tester.assertLinkPresentWithText(issueKeyOne);
        tester.assertLinkPresentWithText(issueKeyTwo);
        tester.assertLinkPresentWithText(issueKeyThree);
        tester.assertLinkPresentWithText(issueKeyFour);
        text.assertTextNotPresent(labelLocator,FIELD_FIX_FOR);
        text.assertTextNotPresent(labelLocator,FIELD_COMPONENTS);
        text.assertTextNotPresent(labelLocator,FIELD_AFFECTS_VERSION);

        log("  Search Project 1 that has NO components and versions field");
        tester.clickLink("find_link");
        form.selectOptionsByValue("pid",new String[]{projectIdOne});
        tester.submit("show");
        labelLocator = new XPathLocator(tester,"//*[@id=\"common.concepts.projectcomponents-group\"]//label");
        //issue one should be shown only
        tester.assertLinkPresentWithText(issueKeyOne);
        tester.assertLinkNotPresentWithText(issueKeyTwo);
        tester.assertLinkNotPresentWithText(issueKeyThree);
        tester.assertLinkNotPresentWithText(issueKeyFour);
        text.assertTextNotPresent(labelLocator,FIELD_FIX_FOR);
        text.assertTextNotPresent(labelLocator,FIELD_COMPONENTS);
        text.assertTextNotPresent(labelLocator,FIELD_AFFECTS_VERSION);

        log("  Search Project 2 that has components field");
        tester.clickLink("find_link");
        form.selectOptionsByValue("pid",new String[]{projectIdTwo});
        tester.submit("show");
        labelLocator = new XPathLocator(tester,"//*[@id=\"common-concepts-projectcomponents-group\"]//label");
        //issue two should be shown only
        tester.assertLinkNotPresentWithText(issueKeyOne);
        tester.assertLinkPresentWithText(issueKeyTwo);
        tester.assertLinkNotPresentWithText(issueKeyThree);
        tester.assertLinkNotPresentWithText(issueKeyFour);
        text.assertTextNotPresent(labelLocator,FIELD_FIX_FOR);
        text.assertTextPresent(labelLocator,FIELD_COMPONENTS);
        text.assertTextNotPresent(labelLocator,FIELD_AFFECTS_VERSION);

        log("  Search Project 3 that has versions field");
        tester.clickLink("find_link");
        form.selectOptionsByValue("pid",new String[]{projectIdThree});
        tester.submit("show");
        labelLocator = new XPathLocator(tester,"//*[@id=\"common-concepts-projectcomponents-group\"]//label");
        //issue three should be shown only
        tester.assertLinkNotPresentWithText(issueKeyOne);
        tester.assertLinkNotPresentWithText(issueKeyTwo);
        tester.assertLinkPresentWithText(issueKeyThree);
        tester.assertLinkNotPresentWithText(issueKeyFour);
        text.assertTextPresent(labelLocator,FIELD_FIX_FOR);
        text.assertTextNotPresent(labelLocator,FIELD_COMPONENTS);
        text.assertTextPresent(labelLocator,FIELD_AFFECTS_VERSION);

        log("  Search Project 4 that has a component and a version field");
        tester.clickLink("find_link");
        form.selectOptionsByValue("pid",new String[]{projectIdFour});
        tester.submit("show");
        labelLocator = new XPathLocator(tester,"//*[@id=\"common-concepts-projectcomponents-group\"]//label");
        //issue three should be shown only
        tester.assertLinkNotPresentWithText(issueKeyOne);
        tester.assertLinkNotPresentWithText(issueKeyTwo);
        tester.assertLinkNotPresentWithText(issueKeyThree);
        tester.assertLinkPresentWithText(issueKeyFour);
        text.assertTextPresent(labelLocator,FIELD_FIX_FOR);
        text.assertTextPresent(labelLocator,FIELD_COMPONENTS);
        text.assertTextPresent(labelLocator,FIELD_AFFECTS_VERSION);

        navigation.gotoAdmin();
        administration.project().deleteProject(PROJECT_DOG);
    }

    /**
     * Tests to check if the Status searcher is displayed when the SearchContext is made invalid by specifying bad
     * Project IDs via the URL. Note that, if at least one Project ID is valid, then Status should be visible
     */
    public void testJQLQueryBoxShownWhenInvalidProjectIdsSetInUrl()
    {
        log("Issue Navigator: Test project componenets visibility");
        administration.restoreData("TestIssueNavigatorProjectComponentsVisibility.xml");

        // specify one invalid PID - statuses should not appear
        tester.gotoPage("/secure/IssueNavigator.jspa?reset=true&pid=99999");
        text.assertTextPresent("A value with ID &#39;99999&#39; does not exist for the field &#39;project&#39;.");
        assertions.getIssueNavigatorAssertions().assertJqlTooComplex();

        // specify two invalid PIDs - statuses should not appear
        tester.gotoPage("/secure/IssueNavigator.jspa?reset=true&pid=99999&pid=88888");
        text.assertTextPresent("A value with ID &#39;88888&#39; does not exist for the field &#39;project&#39;.");
        text.assertTextPresent("A value with ID &#39;99999&#39; does not exist for the field &#39;project&#39;.");
        assertions.getIssueNavigatorAssertions().assertJqlTooComplex();

        // Always make sure we are on the simple page
        // specify two invalid PIDs and one valid PID - statuses should appear
        tester.gotoPage("/secure/IssueNavigator.jspa?reset=true&pid=99999&pid=88888&pid=10010");
        text.assertTextPresent("A value with ID &#39;88888&#39; does not exist for the field &#39;project&#39;.");
        text.assertTextPresent("A value with ID &#39;99999&#39; does not exist for the field &#39;project&#39;.");
        text.assertTextNotPresent("A value with ID &#39;10010&#39; does not exist for the field &#39;project&#39;.");
        assertions.getIssueNavigatorAssertions().assertJqlTooComplex();
    }

    /**
     * Tests custom fields visibility on the issue navigator
     */
    public void testCustomfieldVisibility()
    {
        administration.restoreData("TestIssueNavigatorCustomfieldVisibility.xml");

        tester.clickLink("find_link");
        form.selectOptionsByValue("type",new String[]{ ""});
        form.selectOptionsByValue("pid",new String[]{ "-1"});
        tester.submit("show");
        text.assertTextPresent(CUSTOM_FIELD_GLOBAL);
        text.assertTextNotPresent(CUSTOM_FIELD_ISSUETYPE);
        text.assertTextNotPresent(CUSTOM_FIELD_PROJECT);
        text.assertTextNotPresent(CUSTOM_FIELD_ISSUETYPE_AND_PROJECT);

        tester.clickLink("find_link");
        form.selectOptionsByDisplayName("type", new String[]{IssueTypeCondition.IssueType.BUG.getName()});
        form.selectOptionsByValue("pid",new String[]{ "-1"});
        tester.submit("show");
        text.assertTextPresent(CUSTOM_FIELD_GLOBAL);
        text.assertTextPresent(CUSTOM_FIELD_ISSUETYPE);
        text.assertTextNotPresent(CUSTOM_FIELD_PROJECT);
        text.assertTextNotPresent(CUSTOM_FIELD_ISSUETYPE_AND_PROJECT);

        tester.clickLink("find_link");
        form.selectOptionsByValue("type",new String[]{ ""});;
        form.selectOptionsByValue("pid",new String[]{homosapId});
        tester.submit("show");
        text.assertTextPresent(CUSTOM_FIELD_GLOBAL);
        text.assertTextNotPresent(CUSTOM_FIELD_ISSUETYPE);
        text.assertTextPresent(CUSTOM_FIELD_PROJECT);
        text.assertTextNotPresent(CUSTOM_FIELD_ISSUETYPE_AND_PROJECT);

        tester.clickLink("find_link");
        form.selectOptionsByDisplayName("type",new String[]{IssueTypeCondition.IssueType.BUG.getName()});
        form.selectOptionsByValue("pid",new String[]{homosapId});
        tester.submit("show");
        text.assertTextPresent(CUSTOM_FIELD_GLOBAL);
        text.assertTextPresent(CUSTOM_FIELD_ISSUETYPE);
        text.assertTextPresent(CUSTOM_FIELD_PROJECT);
        text.assertTextPresent(CUSTOM_FIELD_ISSUETYPE_AND_PROJECT);
    }

    /**
     * Checks if the entered search parameters are displayed on the view panel (after clicking hide)
     */
    public void testSearchParametersDisplayed()
    {
        administration.restoreData("TestIssueNavigatorCommon.xml");
        final String componentId = "10000";
        final String versionFourId = "10000";
        final String versionFiveId = "10001";

        final String customFieldId = administration.customFields().addCustomField("com.atlassian.jira.plugin.system.customfieldtypes:textfield", CUSTOM_FIELD_GLOBAL);

        navigation.issueNavigator().displayAllIssues();

        form.selectOptionsByValue("pid",new String[]{homosapId});
        tester.submit("show");
        form.selectOptionsByDisplayName("type", new String[]{IssueTypeCondition.IssueType.BUG.getName()});
        //Project Components
        form.selectOptionsByValue("fixfor", new String[]{versionFourId});
        form.selectOptionsByValue("component", new String[]{componentId});
        form.selectOptionsByValue("version", new String[]{versionFiveId});
        //Text Search
        tester.setFormElement("query","this is the query field");
        //Issue Attributes
        tester.setFormElement("reporterSelect", "specificuser");
        tester.setFormElement("reporter", ADMIN_USERNAME);
        tester.setFormElement("assigneeSelect", "issue_current_user");
        tester.setFormElement("status", "1"); //Open
        tester.setFormElement("resolution", "1"); //Fixed
        tester.setFormElement("priority", "3"); //Major
        //Dates and Times
        tester.setFormElement("created:after", "15/Jul/05");
        tester.setFormElement("created:before", "16/Jul/05");
        tester.setFormElement("created:previous", "-1d");
        tester.setFormElement("created:next", "2d");
        tester.setFormElement("updated:after", "17/Jul/05");
        tester.setFormElement("updated:before", "18/Jul/05");
        tester.setFormElement("updated:previous", "-3d");
        tester.setFormElement("updated:next", "4d");
        tester.setFormElement("duedate:after", "19/Jul/05");
        tester.setFormElement("duedate:before", "20/Jul/05");
        tester.setFormElement("duedate:previous", "-5d");
        tester.setFormElement("duedate:next", "6d");
        //Actual vs Estimated Work Ratio
        tester.setFormElement("workratio:min", "12");
        tester.setFormElement("workratio:max", "13");
        //Custom Fields
        tester.setFormElement(customFieldId, "custom value");

        tester.submit("show");
        tester.clickLink("viewfilter");


        text.assertTextSequence(new IdLocator(tester,"filter-summary"),new String[]{"Project:", PROJECT_HOMOSAP});
        text.assertTextSequence(new IdLocator(tester,"filter-summary"),new String[]{"Issue Type:", "Bug"});
        //Project Components
        text.assertTextSequence(new IdLocator(tester,"filter-summary"),new String[]{"Fix For:", VERSION_NAME_FOUR});
        text.assertTextSequence(new IdLocator(tester,"filter-summary"),new String[]{"Components:", COMPONENT_NAME_ONE});
        text.assertTextSequence(new IdLocator(tester,"filter-summary"),new String[]{"Affects Versions:", VERSION_NAME_FIVE});
        //Text Search
        text.assertTextSequence(new IdLocator(tester,"filter-summary"),new String[]{"Query", "this is the query field"});
        //Issue Attributes
        text.assertTextSequence(new IdLocator(tester,"filter-summary"),new String[]{"Reporter:", ADMIN_USERNAME });
        text.assertTextSequence(new IdLocator(tester,"filter-summary"),new String[]{"Assignee:", "Current User"});
        text.assertTextSequence(new IdLocator(tester,"filter-summary"),new String[]{"Status:", "Open"});
        text.assertTextSequence(new IdLocator(tester,"filter-summary"),new String[]{"Resolutions:", "Fixed"});
        text.assertTextSequence(new IdLocator(tester,"filter-summary"),new String[]{"Priorities:", "Major"});
        //Dates and Times
        text.assertTextSequence(new IdLocator(tester,"filter-summary"),new String[]{"Created After:", "15/Jul/05"});
        text.assertTextSequence(new IdLocator(tester,"filter-summary"),new String[]{"Created Before:", "16/Jul/05"});
        text.assertTextSequence(new IdLocator(tester,"filter-summary"),new String[]{"Created:", "From 1 day ago"});
        text.assertTextSequence(new IdLocator(tester,"filter-summary"),new String[]{"From 1 day ago", "2 days from now"});
        text.assertTextSequence(new IdLocator(tester,"filter-summary"),new String[]{"Updated After:", "17/Jul/05"});
        text.assertTextSequence(new IdLocator(tester,"filter-summary"),new String[]{"Updated Before:", "18/Jul/05"});
        text.assertTextSequence(new IdLocator(tester,"filter-summary"),new String[]{"Updated:", "From 3 days ago"});
        text.assertTextSequence(new IdLocator(tester,"filter-summary"),new String[]{"From 3 days ago", "4 days from now"});
        text.assertTextSequence(new IdLocator(tester,"filter-summary"),new String[]{"Due After:", "19/Jul/05"});
        text.assertTextSequence(new IdLocator(tester,"filter-summary"),new String[]{"Due Before:", "20/Jul/05"});
        text.assertTextSequence(new IdLocator(tester,"filter-summary"),new String[]{"Due Date:", "From 5 days ago"});
        text.assertTextSequence(new IdLocator(tester,"filter-summary"),new String[]{"From 5 days ago", "6 days from now"});
        //Actual vs Estimated Work Ratio
        text.assertTextSequence(new IdLocator(tester,"filter-summary"),new String[]{"Work Ratio Min:", "12%"});
        text.assertTextSequence(new IdLocator(tester,"filter-summary"),new String[]{"Work Ratio Max:", "13%"});
        //Custom Fields
        text.assertTextSequence(new IdLocator(tester,"filter-summary"),new String[]{CUSTOM_FIELD_GLOBAL, "custom value"});
    }

    public void testIssueNavigatorSortByComponent()
    {
        log("Issue Navigator: Test that the filter correctly orders issues for components.");
        administration.restoreData("TestIssueNavigatorCommon.xml");

        tester.clickLink("find_link");
        tester.submit("show");
        tester.clickLink("viewfilter");


        tester.clickLinkWithText("Configure");
        try
        {
            form.selectOption("fieldId", "Component/s");
            tester.submit("add");
        }
        catch (Throwable t)
        {
            log("Component field already added");
        }

        tester.clickLinkWithText("Issue Navigator");

        navigation.issueNavigator().sortIssues("issuekey", "ASC");// One very big hack does not test the functionality of the actual 'onClick' event.

        navigation.issueNavigator().sortIssues("components", "ASC");
        text.assertTextSequence(new IdLocator(tester,"issuetable"),new String[]{issueKey4,issueKey2,issueKey,issueKey3 });

        navigation.issueNavigator().sortIssues("components", "DESC");
        text.assertTextSequence(new IdLocator(tester,"issuetable"),new String[]{issueKey,issueKey3, issueKey2, issueKey4});

        tester.clickLinkWithText("Configure");
        tester.clickLinkWithText("Restore Defaults");
    }

    public void testIssueNavigatorSortByCustomField()
    {
        log("Issue Navigator: Test that the filter correctly orders issues for custom fields.");
        administration.restoreData("TestIssueNavigatorCommon.xml");
        navigation.issueNavigator(). addColumnToIssueNavigator(customFieldNames);

        navigation.issueNavigator().displayAllIssues();//make sure there's a current search request
        for (int i = 0; i < customFieldIds.length; i++)
        {
            log("Sorting by " + customFieldNames[i]);
            navigation.issueNavigator().sortIssues(CUSTOM_FIELD_PREFIX + customFieldIds[i], "ASC");
            text.assertTextSequence(new IdLocator(tester,"issuetable"),new String[]{issueKey, issueKey2, issueKey3, issueKey4, });

            navigation.issueNavigator().sortIssues(CUSTOM_FIELD_PREFIX + customFieldIds[i], "DESC");
            text.assertTextSequence(new IdLocator(tester,"issuetable"),new String[]{issueKey4, issueKey3,issueKey2, issueKey, });
        }
        navigation.issueNavigator().restoreColumnDefaults();
    }

    public void testIssueNavigatorHideReporter()
    {
        log("Issue Navigator: Test that the filter correctly hides the reporter field with full content view.");
        administration.restoreData("TestIssueNavigatorCommon.xml");
        administration.fieldConfigurations().defaultFieldConfiguration().hideFields(REPORTER_FIELD_ID);
        navigation.issueNavigator().displayAllIssues();
        tester.clickLink("fullContent");
        text.assertTextNotPresent("Reporter");
        tester.clickLinkWithText("test issue 1");

        administration.fieldConfigurations().defaultFieldConfiguration().showFields(REPORTER_FIELD_ID);
        navigation.issueNavigator().displayAllIssues();
        tester.clickLink("fullContent");
        text.assertTextPresent("Reporter");
        tester.clickLinkWithText("test issue 1");
    }

    public void testIssueNavigatorManyFields()
    {
        administration.restoreData("TestIssueNavigatorCommon.xml");
        administration.addGlobalPermission(Permissions.BULK_CHANGE, Groups.USERS);
        final String testIssueKey = navigation.issue().createIssue(PROJECT_HOMOSAP,"Bug","test issue 6");
        navigation.issue().assignIssue(testIssueKey, "assign to admin", ADMIN_FULLNAME);
        navigation.issue().setPriority(testIssueKey,"Trivial");
        navigation.issue().setComponents(testIssueKey,COMPONENT_NAME_ONE);
        navigation.issue().setAffectsVersions(testIssueKey,new String[]{JIRAWebTest.VERSION_NAME_FIVE});
        navigation.issue().setFixVersions(testIssueKey,VERSION_NAME_FIVE);
        navigation.issue().setEnvironment(testIssueKey,"test environment 5");
        navigation.issue().setDueDate(testIssueKey,dateOptions[1]);

                // FIXME Robert Remove this when sure not needed
        //(PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY,
//                "Bug",
//                "test issue 6",
//                "Trivial",
//                new String[]{COMPONENT_NAME_ONE},
//                new String[]{VERSION_NAME_FIVE},
//                new String[]{VERSION_NAME_FIVE},
//                "Administrator",
//                "test environment 5",
//                "test description 4 for project actions", null, null,
//                dateOptions[1]);

        navigation.issueNavigator().displayAllIssues();
        tester.clickLink("new_filter");

        tester.selectOption("type", "Bug");
        tester.setFormElement("query", "issue 6");

        tester.selectOption("reporterSelect", "Current User");
        tester.selectOption("assigneeSelect", "Specify User");
        tester.setFormElement("assignee", ADMIN_USERNAME);

        tester.selectOption("status", "Open");
        tester.selectOption("resolution", "Unresolved");
        tester.selectOption("priority", "Trivial");

        tester.setFormElement("created:after", dateOptions[0]);
        tester.setFormElement("created:previous", "-1h");
        tester.setFormElement("updated:after", dateOptions[0]);
        tester.setFormElement("updated:previous", "-1h");

        tester.setFormElement("duedate:after", dateOptions[0]);
        tester.setFormElement("duedate:before", dateOptions[2]);

        tester.submit("show");

        text.assertTextPresent("all 1 issue");

        tester.selectOption("type", "Task");
        tester.submit("show");
        text.assertTextPresent("No matching issues found");
        administration.removeGlobalPermission(BULK_CHANGE, Groups.USERS);
    }


    public void testIssueNavigatorSelectGroup()
    {
        log("Issue Navigator: Test that all issues are filtered for a specific group");
        administration.restoreData("TestIssueNavigatorCommon.xml");
        backdoor.usersAndGroups().addGroup(GROUP_NAME);
        backdoor.usersAndGroups().addUserToGroup(BOB_USERNAME, GROUP_NAME);
        final String testIssueKey = navigation.issue().createIssue(PROJECT_HOMOSAP,"Bug","test issue 5");

        backdoor.darkFeatures().enableForSite("no.frother.assignee.field");
        try
        {
            navigation.issue().assignIssue(testIssueKey, "Assign to Bob", BOB_FULLNAME);
        }
        finally
        {
            backdoor.darkFeatures().disableForSite("no.frother.assignee.field");
        }
        tester.clickLink("find_link");
        try
        {
            tester.clickLink("new_filter");
        }
        catch (Throwable t)
        {
        }

        tester.selectOption("assigneeSelect", "Specify Group");
        tester.setFormElement("assignee", GROUP_NAME);

        tester.submit("show");
        tester.clickLink("viewfilter");

        tester.assertLinkPresentWithText(issueKey);
        tester.assertLinkPresentWithText(issueKey3);
        tester.assertLinkPresentWithText(testIssueKey);
        navigation.issue().deleteIssue(testIssueKey);
        administration.usersAndGroups().deleteGroup(GROUP_NAME);
    }

    public void testIssueNavigatorXMLViewWithCustomFields() throws Exception
    {
        log("Issue Navigator: Test that the RSS page correctly shows the custom field information.");
        administration.restoreData("TestIssueNavigatorCommon.xml");
        navigation.issueNavigator().addColumnToIssueNavigator(customFieldNames);

        navigation.issueNavigator().displayAllIssues();
        tester.clickLinkWithText("XML");

        text.assertTextPresent("An XML representation of a search request");
        text.assertTextPresent("[" + issueKey + "] test issue 1");

        final Document doc = XMLUnit.buildControlDocument(tester.getDialog().getResponse().getText());
        for (final List<CustomFieldValue> values : cfValuesPerIssue)
        {
            for (final CustomFieldValue customFieldValue : values)
            {
                // Not testing the DatePicker because I don't know what format Jira has put the value into
                if (!customFieldValue.getCfType().equals(CUSTOM_FIELD_TYPE_DATEPICKER))
                {
                    log("Searching for existence of xpath " + "//item/customfields/customfield[@id='" + CUSTOM_FIELD_PREFIX + customFieldValue.getCfId() + "'][customfieldname='" + getCustomFieldNameFromType(customFieldValue.getCfType()) + "'][customfieldvalues[customfieldvalue='" + customFieldValue.getCfValue() + "']]");
                    XMLAssert.assertXpathExists("//item/customfields/customfield[@id='" + CUSTOM_FIELD_PREFIX + customFieldValue.getCfId() + "'][customfieldname='" + getCustomFieldNameFromType(customFieldValue.getCfType()) + "'][customfieldvalues[customfieldvalue='" + customFieldValue.getCfValue() + "']]", doc);
                }
            }
        }

    }

    private String getCustomFieldNameFromType(final String type)
    {
        if (type.equals(CUSTOM_FIELD_TYPE_SELECT))
        {
            return CUSTOM_FIELD_SELECT;
        }
        else if (type.equals(CUSTOM_FIELD_TYPE_RADIO))
        {
            return CUSTOM_FIELD_RADIO;
        }
        else if (type.equals(CUSTOM_FIELD_TYPE_MULTISELECT))
        {
            return CUSTOM_FIELD_MULTI_SELECT;
        }
        else if (type.equals(CUSTOM_FIELD_TYPE_CHECKBOX))
        {
            return CUSTOM_FIELD_CHECKBOX;
        }
        else if (type.equals(CUSTOM_FIELD_TYPE_TEXTFIELD))
        {
            return CUSTOM_FIELD_TEXTFIELD;
        }
        else if (type.equals(CUSTOM_FIELD_TYPE_USERPICKER))
        {
            return CUSTOM_FIELD_USERPICKER;
        }
        else if (type.equals(CUSTOM_FIELD_TYPE_DATEPICKER))
        {
            return CUSTOM_FIELD_DATEPICKER;
        }
        else
        {
            return null;
        }
    }

    public void testSearchPermLink() throws SAXException
    {
        administration.restoreData("TestNavigatorSearchPermLink.xml"); //this data has a public project and filter

        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        navigation.issueNavigator().loadFilter(hspUnresolvedFilterId, IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        confirmSearchResults();
        //extract the filters permlink
        final WebLink permLink = tester.getDialog().getResponse().getLinkWithID("permlink");
        final String permlinkURLString = permLink.getURLString();

        //goto admin (different view) to confirm that the permlink is correct
        navigation.gotoAdmin();

        //go directly to the permlink while logged in
        tester.beginAt(permlinkURLString);
        confirmSearchResults();

        //go directly to the permlink for a logged out user
        navigation.logout();
        tester.beginAt(permlinkURLString);
        confirmSearchResults();
    }

    public void testIssueNavigatorWithInvalidFilterId()
    {
        final String invalidFilterId = "14a";
        administration.restoreData("TestIssueNavigatorCommon.xml");
        tester.gotoPage("/secure/IssueNavigator.jspa?mode=hide&requestId=" + invalidFilterId);
        text.assertTextPresent("You do not currently have a search or filter selected.");
        text.assertTextPresent("Invalid filter id &#39;" + invalidFilterId + "&#39;. Filter id must be a number.");
        text.assertTextPresent("The selected filter with id '" + invalidFilterId + "' does not exist.");
    }

    /**
     * Test 'printable' and 'fullcontent' view's 'Back to previous view' link returns the
     * correct view results. JRA-12036
     */
    public void testBackToPreviousViewLinks()
    {
        administration.restoreData("TestIssueNavigatorCommon.xml");
        //Add a new filter (copy of 'Assigned to me')
        tester.gotoPage("secure/IssueNavigator.jspa?reset=true&jqlQuery=resolution+%3D+Unresolved+AND+assignee+%3D+currentUser%28%29+ORDER+BY+created+ASC%2C+priority+DESC");
        final long filterId = navigation.issueNavigator().saveCurrentAsNewFilter("my issues", "",true, null);

        //check theres only four issues
        navigation.issueNavigator().displayAllIssues();
        assertions.getIssueNavigatorAssertions().assertIssueNavigatorDisplaying(new CssLocator(tester, RESULTS_COUNT_CLASS),"1", "4", "4");

        //check printable view's 'Back to previous view' link
        tester.gotoPage("secure/IssueNavigator.jspa?reset=true&jqlQuery=resolution+%3D+Unresolved+AND+assignee+%3D+currentUser%28%29+ORDER+BY+created+ASC%2C+priority+DESC");
        assertPrintableViewsBackToPreviousViewLink();
        navigation.issueNavigator().loadFilter(filterId,IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);// gotoFilter("my issues");
        assertPrintableViewsBackToPreviousViewLink();

        //check full content view's 'Back to previous view' link
        tester.gotoPage("secure/IssueNavigator.jspa?reset=true&jqlQuery=resolution+%3D+Unresolved+AND+assignee+%3D+currentUser%28%29+ORDER+BY+created+ASC%2C+priority+DESC");
        assertFullContentViewsBackToPreviousViewLink();
        navigation.issueNavigator().loadFilter(filterId,IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);//gotoFilter("my issues");
        assertFullContentViewsBackToPreviousViewLink();
    }

    //----------------------------------------------------------------- testBackToPreviousViewLinks helper methods START
    private void assertPrintableViewsBackToPreviousViewLink()
    {
        final String fromIssueCount = "1";
        final String toIssueCount = "2";
        final String totalIssueCount = "2";

        assertExpectedIssueLinksPresent();
        assertions.getIssueNavigatorAssertions().assertIssueNavigatorDisplaying(new CssLocator(tester,RESULTS_COUNT_CLASS),fromIssueCount, toIssueCount, totalIssueCount);
        tester.clickLink("printable");
        assertExpectedIssueLinksPresent();
        assertions.getIssueNavigatorAssertions().assertIssueNavigatorDisplaying(new CssLocator(tester,".result-header"),fromIssueCount, toIssueCount, totalIssueCount);
        tester.clickLinkWithText("Back to previous view");
        assertExpectedIssueLinksPresent();
        assertions.getIssueNavigatorAssertions().assertIssueNavigatorDisplaying(new CssLocator(tester,RESULTS_COUNT_CLASS),fromIssueCount, toIssueCount, totalIssueCount);
    }

    private void assertFullContentViewsBackToPreviousViewLink()
    {
        final String fromIssueCount = "1";
        final String toIssueCount = "2";
        final String totalIssueCount = "2";

        assertExpectedIssueLinksPresent();
        assertions.getIssueNavigatorAssertions().assertIssueNavigatorDisplaying(new CssLocator(tester,RESULTS_COUNT_CLASS), fromIssueCount, toIssueCount, totalIssueCount);
        tester.clickLink("fullContent");
        assertExpectedIssueLinksPresent();
        tester.clickLinkWithText("Back to previous view");
        assertExpectedIssueLinksPresent();
        assertions.getIssueNavigatorAssertions().assertIssueNavigatorDisplaying(new CssLocator(tester,RESULTS_COUNT_CLASS), fromIssueCount, toIssueCount, totalIssueCount);
    }

    private void assertExpectedIssueLinksPresent()
    {
        tester.assertLinkNotPresentWithText("test issue 1");
        tester.assertLinkNotPresentWithText("test issue 3");
        tester.assertLinkPresentWithText("test issue 2");
        tester.assertLinkPresentWithText("test issue 4");
    }

    //------------------------------------------------------------------- testBackToPreviousViewLinks helper methods END

    public void testSearchAbsoluteDateRangeDueDate()
    {
        administration.restoreData("TestIssueNavigatorSearchParams.xml");

        tester.clickLink("find_link");

        // Test that the after portion of an AbsoluteDateRangeParameter WITHOUT the before portion works correctly
        tester.setFormElement("duedate:after", "12/Sep/06");
        tester.submit("show");
        text.assertTextPresent("HSP-2");
        // The date searcher is inclusive, this should not show up
        text.assertTextNotPresent("HSP-1");

        // Test that the after portions inclusivity (haha) of an AbsoluteDateRangeParameter WITHOUT the before portion works correctly
        tester.setFormElement("duedate:after", "10/Sep/06");
        tester.submit("show");
        text.assertTextPresent("HSP-2");
        text.assertTextPresent("HSP-1");

        // Test that the before portions inclusivity of an AbsoluteDateRangeParameter WITHOUT the after portion works correctly
        tester.setFormElement("duedate:before", "14/Sep/06");
        tester.submit("show");
        text.assertTextPresent("HSP-2");
        text.assertTextPresent("HSP-1");

        // Test that the before portions inclusivity of an AbsoluteDateRangeParameter WITHOUT the after portion works correctly
        tester.setFormElement("duedate:before", "12/Sep/06");
        tester.submit("show");
        text.assertTextPresent("HSP-1");
        text.assertTextNotPresent("HSP-2");

        // Test that the before and after portions of AbsoluteDateRangeParameter works correctly
        tester.setFormElement("duedate:before", "14/Sep/06");
        tester.setFormElement("duedate:after", "10/Sep/06");
        tester.submit("show");
        text.assertTextPresent("HSP-2");
        text.assertTextPresent("HSP-1");

        // Test that the things that should not show up do not show up
        tester.setFormElement("duedate:before", "17/Sep/06");
        tester.setFormElement("duedate:after", "15/Sep/06");
        tester.submit("show");
        text.assertTextNotPresent("HSP-2");
        text.assertTextNotPresent("HSP-1");

    }

    public void testSearchRelativeDateRangeDueDate()
    {
        administration.restoreData("TestIssueNavigatorSearchParams.xml");

        // We need to update the due dates to todays date so that we can build a proper relative range
        navigation.issue().viewIssue("HSP-1");
        tester.clickLink("edit-issue");

        final Date today = new Date();
        final String todayString = new SimpleDateFormat("dd/MMM/yy").format(today);
        tester.setFormElement("duedate", todayString);
        tester.submit("Update");

        tester.clickLink("find_link");

        // Test where we set the range on either end by one day, we should find only the issue we just updated
        tester.setFormElement("duedate:previous", "-1d");
        tester.setFormElement("duedate:next", "1d");
        tester.submit("show");
        text.assertTextPresent("HSP-1");
        text.assertTextNotPresent("HSP-2");

        // Test where nothing is found with range on either end
        tester.setFormElement("duedate:previous", "2d");
        tester.setFormElement("duedate:next", "4d");
        tester.submit("show");
        text.assertTextNotPresent("HSP-1");
        text.assertTextNotPresent("HSP-2");

        // Test only the from field
        tester.setFormElement("duedate:previous", "-1d");
        tester.setFormElement("duedate:next", "");
        tester.submit("show");
        text.assertTextPresent("HSP-1");
        text.assertTextNotPresent("HSP-2");

        // Test only the to field
        tester.setFormElement("duedate:previous", "");
        tester.setFormElement("duedate:next", "1d");
        tester.submit("show");
        text.assertTextPresent("HSP-1");
        text.assertTextPresent("HSP-2");
    }

    public void testSearchWorkRatioParam()
    {
        administration.restoreData("TestIssueNavigatorSearchParams.xml");

        tester.clickLink("find_link");

        // Test that we find an issue that falls in a bounded range
        tester.setFormElement("workratio:min", "2");
        tester.setFormElement("workratio:max", "5");
        tester.submit("show");
        text.assertTextPresent("HSP-2");
        text.assertTextNotPresent("HSP-1");

        // Test that we find an issue that falls in a bounded range, inclusive
        tester.setFormElement("workratio:min", "4");
        tester.setFormElement("workratio:max", "5");
        tester.submit("show");
        text.assertTextPresent("HSP-2");
        text.assertTextNotPresent("HSP-1");

        // Test that we find an issue that falls in a bounded range, inclusive
        tester.setFormElement("workratio:min", "3");
        tester.setFormElement("workratio:max", "4");
        tester.submit("show");
        text.assertTextPresent("HSP-2");
        text.assertTextNotPresent("HSP-1");

        // Test that we find nothing for bounded range
        tester.setFormElement("workratio:min", "5");
        tester.setFormElement("workratio:max", "9");
        tester.submit("show");
        text.assertTextNotPresent("HSP-2");
        text.assertTextNotPresent("HSP-1");

        // Test only the min side of the range
        tester.setFormElement("workratio:min", "3");
        tester.setFormElement("workratio:max", "");
        tester.submit("show");
        text.assertTextPresent("HSP-2");
        text.assertTextNotPresent("HSP-1");

        // Test only the max side of the range
        tester.setFormElement("workratio:min", "");
        tester.setFormElement("workratio:max", "5");
        tester.submit("show");
        text.assertTextPresent("HSP-2");
        text.assertTextNotPresent("HSP-1");
    }

    public void testSearchNumberCustomFieldParam()
    {
        administration.restoreData("TestIssueNavigatorSearchParams.xml");

        tester.clickLink("find_link");

        // Test that we find an issue that falls in a bounded range
        tester.setFormElement("customfield_10000:greaterThan", "0");
        tester.setFormElement("customfield_10000:lessThan", "3");
        tester.submit("show");
        text.assertTextPresent("HSP-2");
        text.assertTextPresent("HSP-1");

        // Test that we find an issue that falls in a bounded range, inclusive
        tester.setFormElement("customfield_10000:greaterThan", "1");
        tester.setFormElement("customfield_10000:lessThan", "3");
        tester.submit("show");
        text.assertTextPresent("HSP-2");
        text.assertTextPresent("HSP-1");

        // Test that we find an issue that falls in a bounded range, inclusive
        tester.setFormElement("customfield_10000:greaterThan", "0");
        tester.setFormElement("customfield_10000:lessThan", "2");
        tester.submit("show");
        text.assertTextPresent("HSP-2");
        text.assertTextPresent("HSP-1");

        // Test that we find nothing for bounded range
        tester.setFormElement("customfield_10000:greaterThan", "5");
        tester.setFormElement("customfield_10000:lessThan", "9");
        tester.submit("show");
        text.assertTextNotPresent("HSP-2");
        text.assertTextNotPresent("HSP-1");

        // Test only the min side of the range
        tester.setFormElement("customfield_10000:greaterThan", "2");
        tester.setFormElement("customfield_10000:lessThan", "");
        tester.submit("show");
        text.assertTextPresent("HSP-2");
        text.assertTextNotPresent("HSP-1");

        // Test only the max side of the range
        tester.setFormElement("customfield_10000:greaterThan", "");
        tester.setFormElement("customfield_10000:lessThan", "1");
        tester.submit("show");
        text.assertTextNotPresent("HSP-2");
        text.assertTextPresent("HSP-1");
    }

    /**
     * Tests that the sorting order of the issue navigator is correct and does not crash
     * as in JRA-12974.
     *
     */
    public void testNavigatorOrdering()
    {
        administration.restoreData("TestIssueNavigatorCommon.xml");
        tester.clickLink("find_link");
        tester.submit("show");

        tester.gotoPage("secure/IssueNavigator.jspa?sorter/field=summary&sorter/order=ASC");
        text.assertTextSequence(new IdLocator(tester,"issuetable"),new String[]{
                "test issue 1",
                "test issue 2",
                "test issue 3",
                "test issue 4",
        });

        tester.gotoPage("secure/IssueNavigator.jspa?sorter/field=assignee&sorter/order=ASC");
        text.assertTextSequence(new IdLocator(tester,"issuetable"),new String[] {
                ADMIN_FULLNAME,
                ADMIN_FULLNAME,
                BOB_FULLNAME,
                BOB_FULLNAME,
        });


        tester.gotoPage("secure/IssueNavigator.jspa?sorter/field=summary&sorter/order=ASC");
        tester.gotoPage("secure/IssueNavigator.jspa?sorter/field=duedate&sorter/order=ASC");
        // what can we test here if nothing is showing?
        text.assertTextSequence(new IdLocator(tester,"issuetable"),new String[]{
                "test issue 1",
                "test issue 2",
                "test issue 3",
                "test issue 4",
        });

        // add a work ration field
        tester.clickLink("find_link");
        tester.clickLinkWithText("Configure");
        tester.selectOption("fieldId", "Work Ratio");
        tester.submit("add");

        tester.clickLink("find_link");
        tester.gotoPage("secure/IssueNavigator.jspa?sorter/field=summary&sorter/order=ASC");
        tester.gotoPage("secure/IssueNavigator.jspa?sorter/field=workratio&sorter/order=ASC");
        // test that the page doest crash?
        text.assertTextSequence(new IdLocator(tester,"issuetable"),new String[]{
                "test issue 1",
                "test issue 2",
                "test issue 3",
                "test issue 4",
        });
    }

    /**
     * Check that the navigator tabs work correctly.
     */

    public void testNavigatorTabs()
    {
        administration.restoreData("TestIssueNavigatorCommon.xml");
        navigation.logout();
        tester.gotoPage("secure/IssueNavigator.jspa");

        // anonymous can see same tabs as a logged in user

        String[] loggedTabs = new String[] { "Summary", "New", "Manage" };

        text.assertTextSequence(new IdLocator(tester, "filterFormHeader"), loggedTabs);
        tester.clickLink("viewfilter");
        text.assertTextSequence(new IdLocator(tester, "filterFormHeader"), loggedTabs);
        tester.clickLink("new_filter");
        text.assertTextSequence(new IdLocator(tester, "filterFormHeader"), loggedTabs);

        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        navigation.issueNavigator().displayAllIssues();

        text.assertTextSequence(new IdLocator(tester, "filterFormHeader"), loggedTabs);
        tester.clickLink("viewfilter");
        text.assertTextSequence(new IdLocator(tester, "filterFormHeader"), loggedTabs);
        tester.clickLink("new_filter");
        text.assertTextSequence(new IdLocator(tester, "filterFormHeader"), loggedTabs);

        //assign a search to the current session.
        tester.submit("show");
        tester.clickLink("viewfilter");


        loggedTabs = new String[] { "Summary", "Edit", "New", "Manage" };

        //check that we now have an edit link.
        text.assertTextSequence(new IdLocator(tester, "filterFormHeader"), loggedTabs);
        tester.clickLink("editfilter");
        text.assertTextSequence(new IdLocator(tester, "filterFormHeader"), loggedTabs);
        tester.clickLink("new_filter");

        //make sure the edit link disappears.
        loggedTabs = new String[] { "Summary", "New", "Manage" };
        text.assertTextSequence(new IdLocator(tester, "filterFormHeader"), loggedTabs);
        tester.assertLinkNotPresent("new_filter");

        // click on the "Issues" header link and we are still on the "New" tab
        tester.clickLink("find_link");
        text.assertTextSequence(new IdLocator(tester, "filterFormHeader"), loggedTabs);
        tester.assertLinkNotPresent("new_filter");
    }

    public void testSearchSortDescriptionForInvalidField() throws Exception
    {
        administration.restoreData("TestIssueNavigatorCommon.xml");
        // set up a saved filter
        tester.clickLink("find_link");
        tester.submit("show");
        tester.clickLinkWithText("Save");
        tester.setFormElement("filterName", "My Test Filter");
        tester.submit("saveasfilter_submit");

        // sort on the custom field
        tester.gotoPage("/secure/IssueNavigator.jspa?sorter/field=customfield_10006&sorter/order=ASC");
        text.assertTextSequence(new IdLocator(tester,"filter-summary"),new String[] {"Sorted by:", "Custom Field Date Picker ascending, then", "Key descending"});
        tester.clickLinkWithText("Save");
        tester.submit("Save");

        // delete the custom field
        navigation.gotoAdminSection("view_custom_fields");
        tester.clickLink("del_customfield_10006");
        tester.submit("Delete");

        // redisplay the filter
        navigation.issueNavigator().gotoNavigator();
        tester.assertTextNotPresent("Custom Field Date Picker ascending, then");
        text.assertTextSequence(new IdLocator(tester,"filter-summary"),new String[] {"Sorted by:", "Key descending"});
    }

    public void testNoColumnsDialog() throws Exception
    {
        administration.restoreData("TestIssueNavigatorNoColumns.xml");

        navigation.issueNavigator().displayAllIssues();
        tester.submit("show");

        new TextAssertionsImpl().assertTextSequence(tester.getDialog().getResponseText(),
                new String[] { "No columns selected",
                        "/secure/ViewUserIssueColumns!default.jspa",
                        "Configure" });
    }

    // JRA-20241
    public void testCanSearchForTextWithDotAndColon() throws Exception
    {
        administration.restoreData("TestIssueNavigatorTextWithDotAndColon.xml");
        navigation.issueNavigator().createSearch("description ~ \"d.dude:123\" order by key desc");
        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults("TST-1");
        // Check that the aliases are there as well
        navigation.issueNavigator().createSearch("description ~ \"dude\" order by key asc");
        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults("TST-1", "TST-2");
    }

    // JRA-14238
    public void testXssInImageUrls() throws Exception {
        administration.restoreData("TestImageUrlXss.xml");
        navigation.issueNavigator().createSearch("");

        // status icon URL
        tester.assertTextNotPresent("\"'/><script>alert('statuzz');</script>");
        tester.assertTextPresent("&quot;'/&gt;&lt;script&gt;alert('statuzz');&lt;/script&gt;");

        // priority icon URL
        tester.assertTextNotPresent("\"'/><script>alert('prioritiezz');</script>");
        tester.assertTextPresent("&quot;'/&gt;&lt;script&gt;alert('prioritiezz');&lt;/script&gt;");

        // issue type icon URL
        tester.assertTextNotPresent("\"'/><script>alert('issue typezz');</script>");
        tester.assertTextPresent("&quot;'/&gt;&lt;script&gt;alert('issue typezz');&lt;/script&gt;");
    }

    private void confirmSearchResults()
    {
        assertions.getIssueNavigatorAssertions().assertIssueNavigatorDisplaying(new CssLocator(tester,RESULTS_COUNT_CLASS),"1", "4", "4");
        tester.assertLinkNotPresentWithText("this is a NDT bug - should not be shown on the HSP filter");
        tester.assertLinkPresentWithText("unresolved issue 1");
        tester.assertLinkPresentWithText("unresolved issue 2");
        tester.assertLinkPresentWithText("unresolved issue 3");
        tester.assertLinkPresentWithText("unresolved issue 4");
    }
}
