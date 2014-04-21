package com.atlassian.jira.webtests.ztests.bulk;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.meterware.httpunit.HttpUnitOptions;

import java.util.HashMap;
import java.util.Map;

@WebTest ({ Category.FUNC_TEST, Category.BULK_OPERATIONS, Category.ISSUES })
public class TestBulkChangeIssues extends BulkChangeIssues
{
    /**
     * MAX is the number of 'known' issues to add<br>
     * 'known' issues are issues that are used to control some of the events<br>
     * and to validate through the bulk edit process
     */
    protected static final int MAX = 3;
    protected static final String PREFIX_ISSUE_ON_CURR_PG      = "issue_";
    private static final int NUM_ISSUES_IN_XML_FILE = 51;
    private static final int TOTAL_ISSUES = NUM_ISSUES_IN_XML_FILE + MAX;

    //arrays to keep track of the 'known' issue values
    protected static String issueID[] = new String[MAX];

    /** A variant of the commonly used version name used to show unescaped HTML problems. */
    private static final String MY_OPTION_VERSION_ONE_UNESCAPED = "New Version 1 &trade;";
    /** A variant of the commonly used component name used to show unescaped HTML problems. */
    private static final String MY_OPTION_COMPONENT_ONE_UNESCAPED = "New Component 1 &trade;";

    public TestBulkChangeIssues(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreData("TestBulkChangeInitialised.xml");

        for(int i=0; i < MAX; i++)
        {
            issueID[i] = getIssueIdWithIssueKey(addIssue(PREFIX_ISSUE_ON_CURR_PG + i));
        }
    }

    public void tearDown()
    {
        super.tearDown();
    }

    public void testBulkChangeIssues()
    {
        _testCheckLabelForStepIssueNavigator();
        _testCheckLabelsSelectIssuesOneSingleProject();
        _testCheckLabelsSelectIssuesSomeSingleProject();
        _testCheckLabelsSelectIssuesAllSingleProject();
        _testCheckLabelsSelectIssuesSomeMultipleProject();
        _testCheckLabelsSelectIssuesAllMultipleProject();
        _testCheckOperationDetailsContent();
        _testClickNextWithoutFormCompletionForOperationEdit();
        _testClickNextWithoutFormCompletionForOperationDelete();
        _testCancelLinkForStepChooseIssues();
        _testCancelLinkForStepChooseOperation();
        _testCancelLinkForStepOperationDetails();
        _testCancelLinkForStepConfirmationEdit();
        _testCancelLinkForStepConfirmationDelete();
        _testSideBarLinks();
        _testCheckIssueContentIncludeCurrentPageFromCurrentPage();
        _testCheckIssueContentIncludeCurrentPageFromNextPage();
        _testCheckIssueContentIncludeAllPageFromCurrentPage();
        _testCheckIssueContentIncludeAllPageFromNextPage();
    }

    /**
     * cycles through all the results pages of the issue navigator<br>
     * checking that the correct label is displayed.
     */
    public void _testCheckLabelForStepIssueNavigator()
    {
        log("Bulk Change - Check Labels: iterate through ALL pages and check ISSUE NAVIGATOR labels");
        displayAllIssues();

        int i=1;
        checkLabelForStepIssueNavigator(i, TOTAL_ISSUES);
        while(getDialog().isLinkPresentWithText(LINK_NEXT_PG))
        {
            i++;
            clickLinkWithText(LINK_NEXT_PG);
            checkLabelForStepIssueNavigator(i, TOTAL_ISSUES);
        }
    }

    /**
     *  test to check each dynamic labels in each step. <br>
     * selected ONE issues from a SINGLE project
     */
    public void _testCheckLabelsSelectIssuesOneSingleProject()
    {
        boolean mailServerExists = isMailServerExists();

        log("Bulk Change - Check Labels: select ONE known issue from a SINGLE project");
        String summary = "testCheckLabelsSelectIssuesOneSingleProject";
        addCurrentPageLink(); // add enough issues to get to a second page
        String issueKey = addIssue(summary);

        displayAllIssues();
        checkLabelForStepIssueNavigator(1, TOTAL_ISSUES + 1); //plus 1 since 1 issue is added

        bulkChangeIncludeAllPages();
        bulkChangeSelectIssue(issueKey);

        checkLabelForStepChooseOperation(1, 1);
        bulkChangeChooseOperationEdit();

        checkLabelForStepOperationDetails(1, 1);
        Map fields = new HashMap();
        fields.put(FIELD_FIX_VERSIONS, MY_OPTION_VERSION_ONE_UNESCAPED);
        fields.put(FIELD_VERSIONS, OPTION_VERSION_TWO);
        fields.put(FIELD_COMPONENTS, MY_OPTION_COMPONENT_ONE_UNESCAPED);
        fields.put(FIELD_ASSIGNEE, ADMIN_FULLNAME);
        fields.put(FIELD_PRIORITY, OPTION_PRIORITY_ONE);
        bulkEditOperationDetailsSetAs(fields);

        checkLabelForStepConfirmationEdit(1, 1);

        checkLinkToStepChooseOperation();
        bulkChangeChooseOperationDelete(mailServerExists);
        checkLabelForStepConfirmationDelete(1, 1);
        deleteIssue(issueKey); //delete added issue
    }

    /**
     *  test to check each dynamic labels in each step.<br>
     * selected SOME issues from a SINGLE project
     */
    public void _testCheckLabelsSelectIssuesSomeSingleProject()
    {
        boolean mailServerExists = isMailServerExists();

        log("Bulk Change - Check Labels: select SOME known issues from a SINGLE project");
        displayAllIssues();

        bulkChangeIncludeAllPages();
        bulkChangeChooseIssuesSome();
        clickOnNext();

        checkLabelForStepChooseOperation(MAX, 1);
        bulkChangeChooseOperationEdit();

        checkLabelForStepOperationDetails(MAX, 1);
        Map fields = new HashMap();
        fields.put(FIELD_FIX_VERSIONS, MY_OPTION_VERSION_ONE_UNESCAPED);
        fields.put(FIELD_VERSIONS, OPTION_VERSION_TWO);
        fields.put(FIELD_COMPONENTS, MY_OPTION_COMPONENT_ONE_UNESCAPED);
        fields.put(FIELD_ASSIGNEE, ADMIN_FULLNAME);
        fields.put(FIELD_PRIORITY, OPTION_PRIORITY_ONE);
        bulkEditOperationDetailsSetAs(fields);

        checkLabelForStepConfirmationEdit(MAX, 1);

        checkLinkToStepChooseOperation();
        bulkChangeChooseOperationDelete(mailServerExists);
        checkLabelForStepConfirmationDelete(MAX, 1);
    }

    /**
     *  test to check each dynamic labels in each step. <br>
     * selected ALL issues from a SINGLE project
     */
    public void _testCheckLabelsSelectIssuesAllSingleProject()
    {
        boolean mailServerExists = isMailServerExists();
        log("Bulk Change - Check Labels: select ALL known issues from a SINGLE project");
        displayAllIssues();

        bulkChangeIncludeAllPages();
        bulkChangeChooseIssuesAll();

        checkLabelForStepChooseOperation(TOTAL_ISSUES, 1);
        bulkChangeChooseOperationEdit();

        checkUnescapedVersionAndComponentNotPresent();
        checkLabelForStepOperationDetails(TOTAL_ISSUES, 1);
        Map fields = new HashMap();
        fields.put(FIELD_FIX_VERSIONS, MY_OPTION_VERSION_ONE_UNESCAPED);
        fields.put(FIELD_VERSIONS, OPTION_VERSION_TWO);
        fields.put(FIELD_COMPONENTS, MY_OPTION_COMPONENT_ONE_UNESCAPED);
        fields.put(FIELD_ASSIGNEE, ADMIN_FULLNAME);
        fields.put(FIELD_PRIORITY, OPTION_PRIORITY_ONE);
        bulkEditOperationDetailsSetAs(fields);

        checkUnescapedVersionAndComponentNotPresent();
        checkLabelForStepConfirmationEdit(TOTAL_ISSUES, 1);

        checkLinkToStepChooseOperation();
        bulkChangeChooseOperationDelete(mailServerExists);
        checkLabelForStepConfirmationDelete(TOTAL_ISSUES, 1);
    }

    /**
     *  test to check each dynamic labels in each step.<br>
     * selected SOME issues from a MULTIPLE projects
     */
    public void _testCheckLabelsSelectIssuesSomeMultipleProject()
    {
        boolean mailServerExists = isMailServerExists();

        log("Bulk Change - Check Labels: select SOME known issues from MULTIPLE projects");
        String projectTwoIssueKey = addIssueInProject(SUMMARY_ISSUE_IN_PROJECT_TWO, PROJECT_NEO, PROJECT_NEO_KEY);
        String projectTwoIssueID = getIssueIdWithIssueKey(projectTwoIssueKey);
        displayAllIssues();

        bulkChangeIncludeAllPages();
        bulkChangeChooseIssuesSome(); //selected MAX amount of checkboxes
        selectCheckbox("bulkedit_" + projectTwoIssueID);
        clickOnNext();

        checkLabelForStepChooseOperation(MAX + 1, 2);
        bulkChangeChooseOperationEdit();

        checkLabelForStepOperationDetails(MAX + 1, 2);
        checkMultipleProjectNote();
        Map fields = new HashMap();
        fields.put(FIELD_PRIORITY, OPTION_PRIORITY_FOUR);
        bulkEditOperationDetailsSetAs(fields);

        checkLabelForStepConfirmationEdit(MAX + 1, 2);

        checkLinkToStepChooseOperation();
        bulkChangeChooseOperationDelete(mailServerExists);
        checkLabelForStepConfirmationDelete(MAX + 1, 2);

        deleteIssue(projectTwoIssueKey);
    }

    /**
     *  test to check each dynamic labels in each step. <br>
     * selected ALL issues from a MULTIPLE projects<br>
     * also check correct fields are displayed for selecting multiple projects
     */
    public void _testCheckLabelsSelectIssuesAllMultipleProject()
    {
        boolean mailServerExists = isMailServerExists();

        log("Bulk Change - Check Labels: select ALL known issues from MULTIPLE projects");
        String projectTwoIssueKey = addIssueInProject(SUMMARY_ISSUE_IN_PROJECT_TWO, PROJECT_NEO, PROJECT_NEO_KEY);
        displayAllIssues();

        bulkChangeIncludeAllPages();
        bulkChangeChooseIssuesAll();

        checkLabelForStepChooseOperation(TOTAL_ISSUES + 1, 2);
        bulkChangeChooseOperationEdit();

        checkLabelForStepOperationDetails(TOTAL_ISSUES + 1, 2);
        checkMultipleProjectNote();
        Map fields = new HashMap();
        fields.put(FIELD_PRIORITY, OPTION_PRIORITY_FIVE);
        bulkEditOperationDetailsSetAs(fields);

        checkLabelForStepConfirmationEdit(TOTAL_ISSUES + 1, 2);

        checkLinkToStepChooseOperation();
        bulkChangeChooseOperationDelete(mailServerExists);
        checkLabelForStepConfirmationDelete(TOTAL_ISSUES + 1, 2);

        deleteIssue(projectTwoIssueKey);
    }

    private void checkUnescapedVersionAndComponentNotPresent()
    {
        assertTextNotPresent(MY_OPTION_COMPONENT_ONE_UNESCAPED);
        assertTextNotPresent(MY_OPTION_VERSION_ONE_UNESCAPED);
    }

    /**
     * adds a temporary project and a issue (since its new, it has no versions and components)<br>
     * checks that in step Operation details, the version and component fields are not present
     */
    public void _testCheckOperationDetailsContent()
    {
        log("Bulk Change - Check label & contents: Step Operation Details");
        addProject(PROJECT_TEMP, "tmp", ADMIN_USERNAME);
        String key = addIssueInProject(SUMMARY_ISSUE_IN_PROJECT_TEMP, PROJECT_TEMP, "TMP");
        displayAllIssues();

        bulkChangeIncludeAllPages();
        bulkChangeSelectIssue(key);

        bulkChangeChooseOperationEdit();

        assertTextPresent(NOTE_NO_VERSIONS);
        assertTextPresent(NOTE_NO_COMPONENTS);
        assertTextPresent(NOTE_NO_CUSTOM_FIELDS);
        assertFormElementNotPresent(FIELD_FIX_VERSIONS);
        assertFormElementNotPresent(FIELD_VERSIONS);
        assertFormElementNotPresent(FIELD_COMPONENTS);
        assertFormElementPresent(FIELD_ASSIGNEE);
        assertFormElementPresent(FIELD_PRIORITY);

        deleteProject(PROJECT_TEMP);
    }

    /**
     * Tests if the correct Error message is displayed when clicking Next
     * without any required fields completed for edit operations
     */
    public void _testClickNextWithoutFormCompletionForOperationEdit()
    {
        log("Bulk Change - Errors: Click NEXT without form completion for operation Edit");
        displayAllIssues();
        bulkChangeIncludeAllPages();

        bulkChangeNextWithoutFormCompletionStepChooseIssue();
        bulkChangeChooseIssuesAll();

        bulkChangeNextWithoutFormCompletionStepChooseOperation();
        bulkChangeChooseOperationEdit();

        bulkChangeNextWithoutFormCompletionStepOperationDetails();
    }

    /**
     * Tests if the correct Error message is displayed when clicking Next
     * without any required fields completed for delete operations
     */
    public void _testClickNextWithoutFormCompletionForOperationDelete()
    {
        log("Bulk Change - Errors: Click NEXT without form completion for operation Delete");
        displayAllIssues();
        bulkChangeIncludeAllPages();

        bulkChangeNextWithoutFormCompletionStepChooseIssue();
        bulkChangeChooseIssuesAll();

        bulkChangeNextWithoutFormCompletionStepChooseOperation();
    }

    /**
     * Goes to Step Choose Issues<br>
     * and checks that the cancel button works properly
     */
    public void _testCancelLinkForStepChooseIssues()
    {
        log("Bulk Change - Navigation: Click CANCEL at step CHOOSE ISSUES");
        displayAllIssues();
        // Only turn this on so that the cancel button will work
        HttpUnitOptions.setScriptingEnabled(true);
        bulkChangeIncludeAllPages();
        isStepChooseIssues();
        bulkChangeCancel();
        HttpUnitOptions.setScriptingEnabled(false);
    }

    /**
     * Goes to Step Choose Operation<br>
     * and checks that the cancel button works properly
     */
    public void _testCancelLinkForStepChooseOperation()
    {
        log("Bulk Change - Navigation: Click CANCEL at step CHOOSE OPERATION");

        displayAllIssues();
        bulkChangeIncludeAllPages();
        // Only turn this on so that the cancel button will work
        HttpUnitOptions.setScriptingEnabled(true);
        bulkChangeChooseIssuesAll();
        isStepChooseOperation();
        bulkChangeCancel();
        HttpUnitOptions.setScriptingEnabled(false);
    }

    /**
     * Goes to Step Operation Details<br>
     * and checks that the cancel button works properly
     */
    public void _testCancelLinkForStepOperationDetails()
    {
        log("Bulk Change - Navigation: Click CANCEL at step OPERATION DETAILS");
        displayAllIssues();
        bulkChangeIncludeAllPages();
        bulkChangeChooseIssuesAll();
        // Only turn this on so that the cancel button will work
        HttpUnitOptions.setScriptingEnabled(true);
        bulkChangeChooseOperationEdit();
        isStepOperationDetails();
        bulkChangeCancel();
        HttpUnitOptions.setScriptingEnabled(false);
    }

    /**
     * Goes to Step Confirmation Edit<br>
     * and checks that the cancel button works properly
     */
    public void _testCancelLinkForStepConfirmationEdit()
    {
        log("Bulk Change - Navigation: Click CANCEL at step CONFIRMATION EDIT");
        displayAllIssues();
        bulkChangeIncludeAllPages();
        bulkChangeChooseIssuesAll();
        bulkChangeChooseOperationEdit();
        // Only turn this on so that the cancel button will work
        HttpUnitOptions.setScriptingEnabled(true);
        Map fields = new HashMap();
        fields.put(FIELD_FIX_VERSIONS, MY_OPTION_VERSION_ONE_UNESCAPED);
        fields.put(FIELD_VERSIONS, OPTION_VERSION_TWO);
        fields.put(FIELD_COMPONENTS, MY_OPTION_COMPONENT_ONE_UNESCAPED);
        fields.put(FIELD_ASSIGNEE, ADMIN_FULLNAME);
        fields.put(FIELD_PRIORITY, OPTION_PRIORITY_ONE);
        bulkEditOperationDetailsSetAs(fields);
        isStepConfirmation();
        bulkChangeCancel();
        HttpUnitOptions.setScriptingEnabled(false);
    }

    /**
     * Goes to Step Confirmation Delete<br>
     * and checks that the cancel button works properly
     */
    public void _testCancelLinkForStepConfirmationDelete()
    {
        boolean mailServerExists = isMailServerExists();

        log("Bulk Change - Navigation: Click CANCEL at step CONFIRMATION DELETE");
        displayAllIssues();
        bulkChangeIncludeAllPages();
        bulkChangeChooseIssuesAll();
        // Only turn this on so that the cancel button will work
        HttpUnitOptions.setScriptingEnabled(true);
        bulkChangeChooseOperationDelete(mailServerExists);
        isStepConfirmation();
        bulkChangeCancel();
        HttpUnitOptions.setScriptingEnabled(false);
    }

    /**
     * Tests if each side menu links are working correctly
     */
    public void _testSideBarLinks()
    {
        boolean mailServerExists = isMailServerExists();

        log("Bulk Change - Navigation: check side bar links are valid and working");
        displayAllIssues();
        bulkChangeIncludeAllPages();

        //Step: Choose Issues
        checkSideBarLinksInStepChooseIssues();
        bulkChangeChooseIssuesAll();

        //Step: Choose Operations
        checkSideBarLinksInStepChooseOperation();
        checkLinkToStepChooseIssues();
        clickOnNext();
        bulkChangeChooseOperationEdit();

        //Step: Operation Details
        checkSideBarLinksInStepOperationDetails();
        checkLinkToStepChooseOperation();
        bulkChangeChooseOperationEdit();
        checkLinkToStepChooseIssues();
        clickOnNext();
        bulkChangeChooseOperationEdit();
        Map fields = new HashMap();
        fields.put(FIELD_PRIORITY, OPTION_PRIORITY_ONE);
        bulkEditOperationDetailsSetAs(fields);

        //Step: Confirmation Edit
        checkSideBarLinksInStepConfirmationForEditOp();

        checkLinkToStepOperationDetails();
        clickOnNext();
        checkLinkToStepChooseIssues();
        clickOnNext();
        bulkChangeChooseOperationEdit();
        clickOnNext();
        fields = new HashMap();
        fields.put(FIELD_PRIORITY, OPTION_PRIORITY_TWO);
        bulkEditOperationDetailsSetAs(fields);
        checkLinkToStepChooseOperation();
        bulkChangeChooseOperationDelete(mailServerExists);

        //Step: Confirmation Delete
        checkSideBarLinksInStepConfirmationForDeleteOp();
        checkLinkToStepChooseIssues();
        clickOnNext();
        bulkChangeChooseOperationDelete(mailServerExists);
        checkLinkToStepChooseOperation();
    }

    /**
     * Includes the Current page and checks that ISSUE_ON_NEXT_PAGE is not
     * included for bulk change, and that issues with the prefix
     * PREFIX_ISSUE_ON_CURR_PG are in the same page.
     */
    public void _testCheckIssueContentIncludeCurrentPageFromCurrentPage()
    {
        log("Bulk Change - Issue Content: Check correct issues are displayed, Include CURRENT page from CURRENT page");
        displayAllIssues();
        //select current pages
        bulkChangeIncludeCurrentPage();
        //check that the issue on the next page is NOT included
        assertLinkNotPresentWithText(SUMMARY_ISSUE_ON_NEXT_PG);
        //check that all the 'known' issues are included
        checkIssuesAreListed(PREFIX_ISSUE_ON_CURR_PG, MAX);

    }

    /**
     * Includes the Current page from the next page
     * and checks that ISSUE_ON_NEXT_PAGE is included for bulk change,
     * and that issues with the prefix PREFIX_ISSUE_ON_CURR_PG are
     * NOT included.
     */
    public void _testCheckIssueContentIncludeCurrentPageFromNextPage()
    {
        log("Bulk Change - Issue Content: Check correct issues are displayed, Include CURRENT page from NEXT page");
        displayAllIssues();
        //goto the next page and select current page
        clickLinkWithText(LINK_NEXT_PG);
        bulkChangeIncludeCurrentPage();
        //check that the issue on the next page (ie this current page) is included
        assertLinkPresentWithText(SUMMARY_ISSUE_ON_NEXT_PG);
        //check that the 'known' issues are NOT included
        checkIssuesAreNotListed(PREFIX_ISSUE_ON_CURR_PG, MAX);
    }

    /**
     * Includes All the pages from the current page
     * and checks that ISSUE_ON_NEXT_PAGE is
     * included for bulk change, and that issues with the prefix
     * PREFIX_ISSUE_ON_CURR_PG are also included.
     */
    public void _testCheckIssueContentIncludeAllPageFromCurrentPage()
    {
        log("Bulk Change - Issue Content: Check correct issues are displayed, Include All page from CURRENT page");
        displayAllIssues();
        //select all pages
        bulkChangeIncludeAllPages();
        //check that the issue on the next page is included
        assertLinkPresentWithText(SUMMARY_ISSUE_ON_NEXT_PG);
        //check that all the 'known' issues are included
        checkIssuesAreListed(PREFIX_ISSUE_ON_CURR_PG, MAX);
    }

    /**
     * Includes all the pages from the next page
     * and checks that ISSUE_ON_NEXT_PAGE is included for bulk change,
     * and that issues with the prefix PREFIX_ISSUE_ON_CURR_PG
     * are also included
     */
    public void _testCheckIssueContentIncludeAllPageFromNextPage()
    {
        log("Bulk Change - Issue Content: Check correct issues are displayed, Include All page from NEXT page");
        displayAllIssues();
        //goto the next page and select all pages
        clickLinkWithText(LINK_NEXT_PG);
        bulkChangeIncludeAllPages();
        //check that the issue on the next page (ie this current page) is included
        assertLinkPresentWithText(SUMMARY_ISSUE_ON_NEXT_PG);
        //check that the 'known' issues are included
        checkIssuesAreListed(PREFIX_ISSUE_ON_CURR_PG, MAX);
    }

    /**
     * selects the checkbox's of the 'known' issues<br>
     * Used in the Step Choose Issues
     */
    protected void bulkChangeChooseIssuesSome()
    {
        isStepChooseIssues();
        for(int i=0; i < MAX; i++)
        {
            selectCheckbox("bulkedit_" + issueID[i]);
        }
    }
}
