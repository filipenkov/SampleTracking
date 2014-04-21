package com.atlassian.jira.webtests.ztests.bulk;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.Groups;

@WebTest ({ Category.FUNC_TEST, Category.BULK_OPERATIONS, Category.ISSUES })
public class TestBulkMoveIssues extends BulkChangeIssues
{
    protected static final String ERROR_MOVE_PERMISSION = "You do not have the permission to move one or more of the selected issues";
    protected static final String STD_ISSUE_SELECTION = "Select Projects and Issue Types";

    private static final String MONKEY_PID_OPTION = "10000_1_pid";
    protected static final String TARGET_PROJECT_ID = MONKEY_PID_OPTION;
    protected static final String RANDOM_ISSUE_SUMMARY = "random_" + NUM_RESULTS_PER_PG;
    protected static final String CHECKBOX_RETAIN_PREFIX = "retain_";
    public static final String SAME_FOR_ALL = "sameAsBulkEditBean";
    private static final String BULK_EDIT_KEY = "10000_1_";

    public TestBulkMoveIssues(String name)
    {
        super(name);
    }

    // Placing all tests in one umbrella test so as state of issues is known at all times
    public void testBulkMove()
    {
        log("Bulk Move - Tests for Standard Version");

        restoreData("TestBulkMoveIssues.xml");
        grantGlobalPermission(BULK_CHANGE, Groups.USERS);

        _testMoveOperationUnavailableNoMovePermission();
        _testMoveSTDComponentsAndVersionsRequiredFailure();
        _testDontRetainRequiredComponentAndVersions();
        _testDontRetainNotRequiredComponentAndVersions();
        _testDontRetainNotRequiredNotSelectedComponentAndVersions();

        removeGlobalPermission(BULK_CHANGE, Groups.USERS);
    }

    public void testBulkMoveWithCaseSensitiveDifferences() throws Exception
    {
        restoreData("TestBulkMoveIssuesWithCaseSensitiveMatches.xml");
        grantGlobalPermission(BULK_CHANGE, Groups.USERS);

        // set up the bulk move - HSPs and MKYs are going to MKY
        navigation.issueNavigator().displayAllIssues();
        bulkChangeIncludeAllPages();
        bulkChangeChooseIssuesAll();
        chooseOperationBulkMove();
        isStepSelectProjectIssueType();
        tester.checkCheckbox(TestBulkMoveIssues.SAME_FOR_ALL, "10002_1_");
        tester.selectOption("10002_1_pid", TestBulkMoveIssues.PROJECT_HOMOSAP);
        navigation.clickOnNext();
        tester.assertOptionEquals("versions_10006",    "Unknown");
        tester.assertOptionEquals("fixVersions_10002", "Unknown");
        tester.assertOptionEquals("fixVersions_10006", "Unknown");
        tester.assertOptionEquals("components_10010",  "Unknown");

        tester.selectOption("versions_10006",    "New Version 5");
        tester.selectOption("fixVersions_10002", "New Version 3");
        tester.selectOption("fixVersions_10006", "New Version 5");
        tester.selectOption("components_10010",  "New Component 2");

        navigation.clickOnNext();
        isStepConfirmation();
        navigation.clickOnNext();

        gotoIssue("MKY-8");
        assertLinkPresentWithText(PROJECT_HOMOSAP);
        assertLinkNotPresentWithText(PROJECT_MONKEY);
    }

    public void testBulkMoveWithCaseSensitiveDifferencesAndRememberPreviousSelection() throws Exception
    {
        restoreData("TestBulkMoveIssuesWithCaseSensitiveMatches.xml");
        grantGlobalPermission(BULK_CHANGE, Groups.USERS);

        // set up the bulk move - HSPs and MKYs are going to MKY
        navigation.issueNavigator().displayAllIssues();
        bulkChangeIncludeAllPages();
        bulkChangeChooseIssuesAll();
        chooseOperationBulkMove();
        isStepSelectProjectIssueType();
        tester.selectOption("10002_1_pid", TestBulkMoveIssues.PROJECT_HOMOSAP);
        tester.selectOption("10002_2_pid", TestBulkMoveIssues.PROJECT_HOMOSAP);
        navigation.clickOnNext();
        tester.assertOptionEquals("versions_10006",    "Unknown");
        tester.assertOptionEquals("fixVersions_10002", "Unknown");
        tester.assertOptionEquals("fixVersions_10006", "Unknown");
        tester.assertOptionEquals("components_10010",  "Unknown");

        tester.selectOption("versions_10006",    "New Version 5");
        tester.selectOption("fixVersions_10002", "New Version 3");
        tester.selectOption("fixVersions_10006", "New Version 5");
        tester.selectOption("components_10010",  "New Component 2");

        navigation.clickOnNext();

        tester.assertOptionEquals("versions_10006",    "New Version 5");
        tester.assertOptionEquals("fixVersions_10006", "New Version 5");
        tester.assertOptionEquals("components_10010",  "New Component 2");

        navigation.clickOnNext();
        isStepConfirmation();
        navigation.clickOnNext();

        gotoIssue("MKY-8");
        assertLinkPresentWithText(PROJECT_HOMOSAP);
        assertLinkNotPresentWithText(PROJECT_MONKEY);
    }

    public void testBulkMoveRememberPreviousSelection() throws Exception {
        restoreData("TestBulkMoveMappingAssignee.xml");
        grantGlobalPermission(BULK_CHANGE, Groups.USERS);

        final String hsp1 = navigation.issue().createIssue("homosapien", "Bug",  "first issue");
        final String hsp2 = navigation.issue().createIssue("homosapien", "Task", "second issue");
        final String hsp3 = navigation.issue().createIssue("homosapien", "Task", "another issue");
        navigation.issue().setComponents(hsp1, "New Component 3");
        navigation.issue().setComponents(hsp2, "New Component 1");
        navigation.issue().setComponents(hsp3, "New Component 2");

        navigation.issue().setFixVersions(hsp1, "New Version 3");
        navigation.issue().setFixVersions(hsp2, "New Version 5");
        navigation.issue().setFixVersions(hsp3, "New Version 3");

        navigation.issue().setAffectsVersions(hsp1, "New Version 2");
        navigation.issue().setAffectsVersions(hsp2, "New Version 1");

        navigation.issue().createIssue("monkey", "Bug", "third issue");

        // set up the bulk move - HSPs and MKYs are going to MKY
        navigation.issueNavigator().displayAllIssues();
        bulkChangeIncludeAllPages();
        bulkChangeChooseIssuesAll();
        chooseOperationBulkMove();
        isStepSelectProjectIssueType();

        tester.selectOption("10000_1_pid", TestBulkMoveIssues.PROJECT_MONKEY);
        tester.selectOption("10002_1_pid", TestBulkMoveIssues.PROJECT_MONKEY);
        tester.selectOption("10000_3_pid", TestBulkMoveIssues.PROJECT_MONKEY);
        navigation.clickOnNext();

        tester.assertOptionEquals("versions_10001",    "Unknown");
        tester.assertOptionEquals("fixVersions_10003", "New Version 3");
        tester.assertOptionEquals("components_10002",  "New Component 3");

        tester.selectOption("versions_10001",    "New Version 6");
        tester.selectOption("fixVersions_10003", "New Version 5");
        tester.selectOption("components_10002",  "New Component 4");

        navigation.clickOnNext();

        tester.assertOptionEquals("versions_10000",    "Unknown");
        tester.assertOptionEquals("fixVersions_10003", "New Version 5");
        tester.assertOptionEquals("fixVersions_10005", "New Version 5");
        tester.assertOptionEquals("components_10000",  "Unknown");
        tester.assertOptionEquals("components_10001",  "Unknown");

        tester.selectOption("versions_10000",   "New Version 3");
        tester.selectOption("components_10000", "New Component 3");
        tester.selectOption("components_10001", "New Component 4");

        navigation.clickOnNext();
        isStepConfirmation();
        navigation.clickOnNext();
    }

    // Bulk Moving issues from HSP and MKY to just MKY. The existing MKY issues should not be touched and not have their
    // values mapped at all. The HSP issue needs the assignee changed and we change it to Jon and select "retain".
    public void testBulkMoveWithAssigneeRetain() throws Exception
    {
        administration.restoreData("TestBulkMoveMappingAssignee.xml");

        // set up some issues
        try
        {
            getBackdoor().darkFeatures().enableForSite("no.frother.assignee.field");
            final String hsp1 = navigation.issue().createIssue("homosapien", "Bug", "first issue");
            navigation.issue().assignIssue(hsp1, "", FRED_USERNAME);

            navigation.issue().createIssue("monkey", "Bug", "second issue");

            // set up the bulk move - HSPs and MKYs are going to MKY
            navigation.issueNavigator().displayAllIssues();
            bulkChangeIncludeAllPages();
            bulkChangeChooseIssuesAll();
            chooseOperationBulkMove();
            isStepSelectProjectIssueType();
            tester.checkCheckbox(TestBulkMoveIssues.SAME_FOR_ALL, BULK_EDIT_KEY);
            tester.selectOption(TestBulkMoveIssues.TARGET_PROJECT_ID, TestBulkMoveIssues.PROJECT_MONKEY);
            navigation.clickOnNext();
            isStepSetFields();

            // choose Jon as the assignee and set the assignee to retain
            tester.selectOption("assignee", "jon");
            tester.checkCheckbox("retain_assignee");

            // complete the wizard
            navigation.clickOnNext();
            isStepConfirmation();
            navigation.clickOnNext();

            // check the values of the fields in the new issues

            // MKY-1 should be unchanged
            navigation.issue().viewIssue("MKY-1");
            assertions.getViewIssueAssertions().assertAssignee(ADMIN_FULLNAME);

            // first issue changed to Jon
            final String firstIssueKey = getIssueKeyWithSummary("first issue", "MKY");
            navigation.issue().viewIssue(firstIssueKey);
            assertions.getViewIssueAssertions().assertAssignee("jon");
        }
        finally
        {
            getBackdoor().darkFeatures().disableForSite("no.frother.assignee.field");
        }
    }

    // Bulk Moving issues from HSP and MKY to just MKY. The existing MKY issue along with HSP should have assignee changed
    // as it is not being retained.
    public void testBulkMoveWithAssigneeDontRetain() throws Exception
    {
        administration.restoreData("TestBulkMoveMappingAssignee.xml");

        // set up some issues
        try
        {
            getBackdoor().darkFeatures().enableForSite("no.frother.assignee.field");
            final String hsp1 = navigation.issue().createIssue("homosapien", "Bug", "first issue");
            navigation.issue().assignIssue(hsp1, "", FRED_USERNAME);

            navigation.issue().createIssue("monkey", "Bug", "second issue");

            // set up the bulk move - HSPs and MKYs are going to MKY
            navigation.issueNavigator().displayAllIssues();
            bulkChangeIncludeAllPages();
            bulkChangeChooseIssuesAll();
            chooseOperationBulkMove();
            isStepSelectProjectIssueType();
            tester.checkCheckbox(TestBulkMoveIssues.SAME_FOR_ALL, BULK_EDIT_KEY);
            tester.selectOption(TestBulkMoveIssues.TARGET_PROJECT_ID, TestBulkMoveIssues.PROJECT_MONKEY);
            navigation.clickOnNext();
            isStepSetFields();

            // choose Jon as the assignee
            tester.selectOption("assignee", "jon");

            // complete the wizard
            navigation.clickOnNext();
            isStepConfirmation();
            navigation.clickOnNext();

            // check the values of the fields in the new issues

            // MKY-1 should be unchanged
            navigation.issue().viewIssue("MKY-1");
            assertions.getViewIssueAssertions().assertAssignee("jon");

            // first issue changed to Jon
            final String firstIssueKey = getIssueKeyWithSummary("first issue", "MKY");
            navigation.issue().viewIssue(firstIssueKey);
            assertions.getViewIssueAssertions().assertAssignee("jon");
        }
        finally
        {
            getBackdoor().darkFeatures().disableForSite("no.frother.assignee.field");
        }
    }

    private void _testMoveOperationUnavailableNoMovePermission()
    {
        log("Bulk Move - move operation is not available without the move permission");
        removeGroupPermission(DEFAULT_PERM_SCHEME, MOVE_ISSUE, Groups.DEVELOPERS);
        displayAllIssues();
        bulkChangeIncludeAllPages();
        bulkChangeChooseIssuesAll();
        assertTextPresent(ERROR_MOVE_PERMISSION);
        grantGroupPermission(DEFAULT_PERM_SCHEME, MOVE_ISSUE, Groups.DEVELOPERS);
    }

    private void _testMoveSTDComponentsAndVersionsRequiredFailure()
    {
        log("Bulk Move - STD - components and versions required - failure");
        setRequiredFields();
        displayAllIssues();
        bulkChangeIncludeAllPages();
        bulkChangeChooseIssuesAll();
        chooseOperationBulkMove();
        assertTextPresent(STD_ISSUE_SELECTION);
        checkCheckbox(SAME_FOR_ALL, BULK_EDIT_KEY);
        selectOption(TARGET_PROJECT_ID, PROJECT_NEO);
        navigation.clickOnNext();
        navigation.clickOnNext();
        assertErrorMsgFieldRequired(COMPONENTS_FIELD_ID, PROJECT_NEO, "components");
        assertErrorMsgFieldRequired(FIX_VERSIONS_FIELD_ID, PROJECT_NEO, "versions");
        assertErrorMsgFieldRequired(AFFECTS_VERSIONS_FIELD_ID, PROJECT_NEO, "versions");
        resetFields();
    }


    private void _testDontRetainRequiredComponentAndVersions()
    {
        log("Bulk Move - STD - Retain, components and versions Required, Select new values");
        resetFields();
        addIssue(PROJECT_MONKEY, PROJECT_MONKEY_KEY, ISSUE_TYPE_BUG, "issueKey1", PRIORITY_MAJOR, null, null, null, null, "", "", null, null, null);
        String issueKey2 = addIssue(PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, ISSUE_TYPE_BUG, "issueKey2", PRIORITY_MAJOR, new String[]{COMPONENT_NAME_TWO}, null, null, null, "", "", null, null, null);
        String issueKey3 = addIssue(PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, ISSUE_TYPE_BUG, "issueKey3", PRIORITY_MAJOR, new String[]{COMPONENT_NAME_THREE}, new String[]{VERSION_NAME_FOUR}, new String[]{VERSION_NAME_FIVE}, null, "", "", null, null, null);

        //assert index was correctly created
        assertIndexedFieldCorrect("//item", EasyMap.build("summary", "issueKey2", "priority", PRIORITY_MAJOR), null, issueKey2);
        assertIndexedFieldCorrect("//item", EasyMap.build("summary", "issueKey3", "priority", PRIORITY_MAJOR, "version", VERSION_NAME_FOUR), null, issueKey3);

        setRequiredFields();
        displayAllIssues();
        bulkChangeIncludeAllPages();
        bulkChangeChooseIssuesAll();
        chooseOperationBulkMove();
        assertTextPresent(STD_ISSUE_SELECTION);
        checkCheckbox(SAME_FOR_ALL, BULK_EDIT_KEY);
        selectOption(TARGET_PROJECT_ID, PROJECT_HOMOSAP);
        navigation.clickOnNext();

        assertTextPresent("Update Fields for Target Project");

        selectOption("components_-1", COMPONENT_NAME_ONE);
        selectOption("fixVersions_-1", VERSION_NAME_ONE);
        selectOption("versions_-1", VERSION_NAME_TWO);
        navigation.clickOnNext();
        isStepConfirmation();
        navigation.clickOnNext();

        //issue key changed...
        String issueKey1 = getIssueKeyWithSummary("issueKey1", PROJECT_HOMOSAP_KEY);
        gotoIssue(issueKey1);
        assertLinkPresentWithText(PROJECT_HOMOSAP);
        assertLinkNotPresentWithText(PROJECT_MONKEY);
        assertions.getViewIssueAssertions().assertComponents(COMPONENT_NAME_ONE);
        assertions.getViewIssueAssertions().assertFixVersions(VERSION_NAME_ONE);
        assertions.getViewIssueAssertions().assertAffectsVersions(VERSION_NAME_TWO);
        //assert item was moved and the key has been updated in the index
        assertIndexedFieldCorrect("//item", EasyMap.build("key", issueKey1, "component", COMPONENT_NAME_ONE, "fixVersion", VERSION_NAME_ONE, "version", VERSION_NAME_TWO), null, issueKey1);

        gotoIssue(issueKey2);
        assertions.getViewIssueAssertions().assertComponents(COMPONENT_NAME_TWO);
        assertions.getViewIssueAssertions().assertFixVersions(VERSION_NAME_ONE);
        assertions.getViewIssueAssertions().assertAffectsVersions(VERSION_NAME_TWO);
        assertIndexedFieldCorrect("//item", EasyMap.build("key", issueKey2, "component", COMPONENT_NAME_TWO, "fixVersion", VERSION_NAME_ONE, "version", VERSION_NAME_TWO), null, issueKey2);

        gotoIssue(issueKey3);
        assertions.getViewIssueAssertions().assertComponents(COMPONENT_NAME_THREE);
        assertions.getViewIssueAssertions().assertFixVersions(VERSION_NAME_FIVE);
        assertions.getViewIssueAssertions().assertAffectsVersions(VERSION_NAME_FOUR);
        assertIndexedFieldCorrect("//item", EasyMap.build("key", issueKey3, "component", COMPONENT_NAME_THREE, "fixVersion", VERSION_NAME_FIVE, "version", VERSION_NAME_FOUR), null, issueKey3);

        resetFields();
    }

    private void _testDontRetainNotRequiredComponentAndVersions()
    {
        log("Bulk Move - STD - Dont Retain, components and versions Not Required, Select new values");
        addIssue(PROJECT_MONKEY, PROJECT_MONKEY_KEY, ISSUE_TYPE_BUG, "Too much fear creeping in", PRIORITY_MAJOR, null, null, new String[]{VERSION_NAME_THREE}, null, "", "", null, null, null);
        String issueKey2 = addIssue(PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, ISSUE_TYPE_BUG, "issueKey2", PRIORITY_MAJOR, new String[]{COMPONENT_NAME_TWO}, null, null, null, "", "", null, null, null);
        String issueKey3 = addIssue(PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, ISSUE_TYPE_BUG, "issueKey3", PRIORITY_MAJOR, new String[]{COMPONENT_NAME_THREE}, new String[]{VERSION_NAME_FOUR}, new String[]{VERSION_NAME_THREE}, null, "", "", null, null, null);

        resetFields();

        displayAllIssues();
        bulkChangeIncludeAllPages();
        bulkChangeChooseIssuesAll();
        chooseOperationBulkMove();
        assertTextPresent(STD_ISSUE_SELECTION);
        checkCheckbox(SAME_FOR_ALL, BULK_EDIT_KEY);
        selectOption(TARGET_PROJECT_ID, PROJECT_MONKEY);
        navigation.clickOnNext();

        isStepSetFields();

        assertTextPresentBeforeText("components", "None");
        selectOption("fixVersions_10000", VERSION_NAME_FIVE);
        selectOption("versions_10004", VERSION_NAME_FIVE);
        navigation.clickOnNext();
        isStepConfirmation();
        navigation.clickOnNext();

        //issue key changed...
        String issueKey1 = getIssueKeyWithSummary("Too much fear creeping in", PROJECT_MONKEY_KEY);
        gotoIssue(issueKey1);
        assertLinkPresentWithText(PROJECT_MONKEY);
        assertLinkNotPresentWithText(PROJECT_HOMOSAP);
        // monkey has no components (monkey born fully-formed, his nature was irrepressable!)
        assertions.getViewIssueAssertions().assertComponentsAbsent();
        assertions.getViewIssueAssertions().assertAffectsVersionsNone();
        assertions.getViewIssueAssertions().assertFixVersions(VERSION_NAME_THREE);

        gotoIssue(issueKey2);
        assertions.getViewIssueAssertions().assertComponentsAbsent();
        assertions.getViewIssueAssertions().assertAffectsVersionsNone();
        assertions.getViewIssueAssertions().assertFixVersionsNone();

        gotoIssue(issueKey3);
        assertions.getViewIssueAssertions().assertComponentsAbsent();
        assertions.getViewIssueAssertions().assertAffectsVersions(VERSION_NAME_FIVE);
        assertions.getViewIssueAssertions().assertFixVersions(VERSION_NAME_THREE);

        resetFields();
    }

    private void _testDontRetainNotRequiredNotSelectedComponentAndVersions()
    {
        log("Bulk Move - STD - Dont Retain, components and versions Not Required, Dont Select new values");
        addIssue(PROJECT_MONKEY, PROJECT_MONKEY_KEY, ISSUE_TYPE_BUG, "issueKey1", PRIORITY_MAJOR, null, null, new String[]{VERSION_NAME_THREE}, null, "", "", null, null, null);
        String issueKey2 = addIssue(PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, ISSUE_TYPE_BUG, "issueKey2", PRIORITY_MAJOR, new String[]{COMPONENT_NAME_TWO}, null, null, null, "", "", null, null, null);
        String issueKey3 = addIssue(PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, ISSUE_TYPE_BUG, "issueKey3", PRIORITY_MAJOR, new String[]{COMPONENT_NAME_THREE}, new String[]{VERSION_NAME_FOUR}, new String[]{VERSION_NAME_THREE}, null, "", "", null, null, null);

        resetFields();

        displayAllIssues();
        bulkChangeIncludeAllPages();
        bulkChangeChooseIssuesAll();
        chooseOperationBulkMove();
        assertTextPresent(STD_ISSUE_SELECTION);
        checkCheckbox(SAME_FOR_ALL, BULK_EDIT_KEY);
        selectOption(TARGET_PROJECT_ID, PROJECT_MONKEY);
        navigation.clickOnNext();
        isStepSetFields();

        assertTextPresentBeforeText("components", "None");
        navigation.clickOnNext();
        isStepConfirmation();
        navigation.clickOnNext();

        //issue key changed...
        String issueKey1 = getIssueKeyWithSummary("issueKey1", PROJECT_MONKEY_KEY);
        gotoIssue(issueKey1);
        assertLinkPresentWithText(PROJECT_MONKEY);
        assertLinkNotPresentWithText(PROJECT_HOMOSAP);
        assertions.getViewIssueAssertions().assertComponentsAbsent();
        assertions.getViewIssueAssertions().assertFixVersionsNone();
        assertions.getViewIssueAssertions().assertAffectsVersionsNone();

        gotoIssue(issueKey2);
        assertions.getViewIssueAssertions().assertComponentsAbsent();
        assertions.getViewIssueAssertions().assertFixVersionsNone();
        assertions.getViewIssueAssertions().assertAffectsVersionsNone();

        gotoIssue(issueKey3);
        assertions.getViewIssueAssertions().assertComponentsAbsent();
        assertions.getViewIssueAssertions().assertFixVersionsNone();
        assertions.getViewIssueAssertions().assertAffectsVersionsNone();

        resetFields();
    }

    private void isStepSelectProjectIssueType()
    {
        tester.assertTextPresent("Select Projects and Issue Types");
    }

    private void isStepSetFields()
    {
        assertTextPresent("Update Fields for Target Project");
        log("Step Set Fields");
    }
}
