/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.webtests.ztests.issue;

import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.Groups;
import com.atlassian.jira.webtests.JIRAWebTest;

@WebTest ({ Category.FUNC_TEST, Category.ISSUES })
public class TestIssueOperations extends JIRAWebTest
{

    public TestIssueOperations(String name)
    {
        super(name);
    }

    private static final String ATTACHMENT_DOUBLE_QUOTES = "bigdummy\"\"Attachment";
    private static final String ISSUE_KEY_1 = "HSP-1";
    private static final String ISSUE_KEY_2 = "HSP-2";
    private static final String TEST_USER_1 = "testuser";
    private static final String TEST_USER_2 = "testuser2";

    public void testIssueOperations()
    {
        restoreData("TestIssueOperations.xml");

        administration.attachments().enable();

        issueOperationsWithWorkOnPermission(ISSUE_KEY_1);
        issueOperationWithUnassignableUser(ISSUE_KEY_1);
        issueOperationWithAssignPermission(ISSUE_KEY_1);
        issueOperationWithCreateAttachmentsPermission(ISSUE_KEY_1);
//        issueOperationAttachFileWithDoubleQuote(issueKey);
        issueOperationCacheControl(ISSUE_KEY_1);
        issueWithoutBrowsePermissionLookForRDF(ISSUE_KEY_1);
    }

    //JRADEV-610
    public void testIssueLinksForOperations()
    {
        restoreData("TestIssueIconOperations.xml");

        //check issue links, attachments and time tracking.  First with a user that has access
        login(FRED_USERNAME);
        gotoIssue("HSP-1");

        //first of all check links are present
        assertLinkPresent("aszip");
        assertLinkPresent("manage-attachment-link");
        assertLinkPresent("add-attachments-link");

        assertLinkPresent("add-links-link");

        assertLinkPresent("log-work-link");

        //then check they go to the right pages
        clickLink("manage-attachment-link");
        assertTextSequence(new String[] {"Manage Attachments", "This page allows you to manage the attachments", "foobar.json"});
        assertLinkPresent("aszipbutton");
        gotoIssue("HSP-1");
        clickLink("add-attachments-link");
        assertTextSequence(new String[] {"Attach Files"});
        gotoIssue("HSP-1");

        gotoIssue("HSP-1");
        clickLink("add-links-link");
        assertTextSequence(new String[] {"Link Issue"});
        gotoIssue("HSP-1");

        clickLink("log-work-link");
        assertTextSequence(new String[] {"Log Work"});

        //try a user with permissions for nothing.
        login("user", "user");
        gotoIssue("HSP-1");
        assertLinkPresent("aszip");
        assertLinkNotPresent("manage-attachment-link");
        assertLinkNotPresent("add-attachments-link");
        //the admin user does not have the linking permission.
        assertLinkNotPresent("add-links-link");
        //and can also not log work
        assertLinkNotPresent("log-work-link");

        //try a user with manage attachments permission (can delete), but no add permission
        login(ADMIN_USERNAME);
        gotoIssue("HSP-1");
        assertLinkPresent("aszip");
        assertLinkPresent("manage-attachment-link");
        assertLinkNotPresent("add-attachments-link");
        //the admin user does not have the linking permission.
        assertLinkNotPresent("add-links-link");
        //and can also not log work
        assertLinkNotPresent("log-work-link");

        // ensure that if ZIP support is disabled, then the link is not present
        administration.attachments().disableZipSupport();
        gotoIssue("HSP-1");
        assertLinkNotPresent("aszip");
        clickLink("manage-attachment-link");
        assertLinkNotPresent("aszipbutton");
    }

    public void issueWithoutBrowsePermissionLookForRDF(String issueKey)
    {
        try
        {
            // make browse project not available
            removeGroupPermission(BROWSE, Groups.USERS);
            addUser(BOB_USERNAME, BOB_PASSWORD, BOB_FULLNAME, BOB_EMAIL);
            logout();
            login(BOB_USERNAME, BOB_PASSWORD);

            // test browse project
            beginAt("/browse/" + issueKey);
            assertTextPresent("It seems that you have tried to perform an operation which you are not permitted to perform.");
            assertTextPresent("rdf:Description");
            assertTextPresent("trackback:ping=");
        }
        finally
        {
            try
            {
                logout();
                login(ADMIN_USERNAME, ADMIN_PASSWORD);
                deleteUser(BOB_USERNAME);
                grantGroupPermission(BROWSE, Groups.USERS);
            }
            catch(Exception e)
            {
                // don't worry, it is already set
            }
        }
    }

    /**
     * Tests the availability of 'Log Work' Link with 'Work On Issues' permission removed
     */
    public void issueOperationsWithWorkOnPermission(String issueKey)
    {
        log("Issue Operation: Test availability of Log Work Link with 'Work On Issues' permission.");
        activateTimeTracking();
        removeGroupPermission(WORK_ISSUE, Groups.DEVELOPERS);
        gotoIssue(issueKey);
        assertLinkNotPresent("log-work");

        // Grant Work issue Permission
        grantGroupPermission(WORK_ISSUE, Groups.DEVELOPERS);
        gotoIssue(issueKey);
        assertLinkPresent("log-work");
        deactivateTimeTracking();
    }

    public void testLogWorkOperationTimeTrackingDisabled()
    {
        restoreData("TestIssueOperations.xml");
        deactivateTimeTracking();

        gotoIssue(ISSUE_KEY_1);
        //log work operation/permission message should not be displayed at all
        assertLinkNotPresent("log-work");
    }

    public void testLogWorkOperationAnonymous()
    {
        restoreData("TestIssueOperations.xml");
        activateTimeTracking();

        navigation.gotoAdmin();
        clickLink("permission_schemes");
        clickLinkWithText("Default Permission Scheme");

        clickLink("add_perm_10");
        checkCheckbox("type", "group");
        submit(" Add ");

        logout();

        gotoPage("/browse/HSP-1");

        //log work operation/permission message should not be displayed at all
        assertLinkNotPresent("log-work");
    }

    public void testLogWorkOperationIssueInNonEditableState()
    {
        restoreData("TestIssueOperations.xml");
        activateTimeTracking();

        gotoIssue(ISSUE_KEY_1);
        clickLinkWithText("Close Issue");
        setWorkingForm("issue-workflow-transition");
        submit("Transition");

        //log work operation/permission message should not be displayed at all
        assertLinkNotPresent("log-work");
    }

    public void testLogWorkOperationHappyPath()
    {
        restoreData("TestIssueOperations.xml");
        activateTimeTracking();
        gotoIssue(ISSUE_KEY_1);

        //log work operation/permission message should not be displayed at all
        assertLinkPresent("log-work");
    }

    /**
     * Attempts to assign an issue from the issue page with the 'Assignable User' permission removed
     */
    public void issueOperationWithUnassignableUser (String issueKey)
    {
        log("Issue Operation: Attempt to set the assignee to be an unassignable user ...");

        // Remove assignable permission
        removeGroupPermission(ASSIGNABLE_USER, Groups.DEVELOPERS);
        gotoIssue(issueKey);

        clickLink("assign-issue");
        setWorkingForm("assign-issue");
        submit();

        assertTextPresent(DEFAULT_ASSIGNEE_ERROR_MESSAGE);


        //Restore permission
        grantGroupPermission(ASSIGNABLE_USER, Groups.DEVELOPERS);
    }

    /**
     * Tests if the 'Assign' link is available with the 'Assign Issues' Permission removed
     */
    public void issueOperationWithAssignPermission (String issueKey)
    {
        log("Issue Operation: Test the availability of the 'Assign Link' with 'Assign Issues' Permission.");

        gotoIssue(issueKey);
        assertLinkPresent("assign-issue");

        // Remove assignable permission
        removeGroupPermission(ASSIGN_ISSUE, Groups.DEVELOPERS);
        gotoIssue(issueKey);
        assertLinkNotPresent("assign-issue");

        //Restore permission
        grantGroupPermission(ASSIGN_ISSUE, Groups.DEVELOPERS);
    }

    /**
     * Tests if the 'Attach File' link is available with the 'Create Attachments' Permission removed
     */
    public void issueOperationWithCreateAttachmentsPermission(String issueKey)
    {
        log("Issue Operation: Test the availability of the 'Attach Link' with 'Create Attachments' Permission.");
        // Attach screenshots only works for Windows
        // Remove assignable permission
        removeGroupPermission(CREATE_ATTACHMENT, Groups.USERS);
        gotoIssue(issueKey);
        assertLinkNotPresent("attach-file");
//        assertLinkNotPresent("attach_screenshot");

        //Restore permission
        grantGroupPermission(CREATE_ATTACHMENT, Groups.USERS);
        gotoIssue(issueKey);
        assertLinkPresent("attach-file");
//        assertLinkPresent("attach_screenshot");
    }

    /**
     * Tests that cache control is correct for /browse/ and issue navigator pages
     */
    public void issueOperationCacheControl(String issueKey)
    {
        log("Issue Operation: Test cache control for Issue Navigator pages.");
        clickLink("find_link");
        tester.submit("show");
        tester.clickLink("viewfilter");


        String cache = getDialog().getResponse().getHeaderField("Cache-Control");
        assertEquals(cache, "no-cache, no-store, must-revalidate");

        gotoIssue(issueKey);
        cache = getDialog().getResponse().getHeaderField("Cache-Control");
        assertEquals(cache, "no-cache, no-store, must-revalidate");
    }

    /**
     * Tests the "Manage Watchers" permission when the user is a 'Reporter' and not a reporter
     */
    public void testIssueOperationManageWatcherList()
    {
        restoreData("TestIssueOperationsWithReporter.xml");
        administration.attachments().enable();

        grantPermissionToReporter(MANAGE_WATCHER_LIST);
        // go to issue as a reporter
        logout();
        login(TEST_USER_1, TEST_USER_1);
        gotoIssue(ISSUE_KEY_2);
        assertLinkPresent("manage-watchers");
        logout();
        login(TEST_USER_2, TEST_USER_2);
        assertLinkNotPresent("manage-watchers");
        logout();
    }

    public void testIssueOperationsWithLongNames()
    {
        restoreData("TestIssueOperationsWithLongTransitions.xml");
        gotoIssue(ISSUE_KEY_1);

        IdLocator locator = new IdLocator(tester, "action_id_711");
        text.assertTextPresent(locator, "Another really long on...");
        assertEquals("Another really long one that has a description as well - Yep, this is the description", locator.getNode().getAttributes().getNamedItem("title").getNodeValue());

        locator = new IdLocator(tester, "action_id_4");
        text.assertTextPresent(locator, "Start Progress that is...");
        assertEquals("Start Progress that is stupidly long and will make the buttons go really wide.  Quite Silly when you think about it.", locator.getNode().getAttributes().getNamedItem("title").getNodeValue());

        locator = new IdLocator(tester, "action_id_5");
        text.assertTextPresent(locator, "Resolve Issue");
        assertEquals("Resolve Issue - We can still give it a description", locator.getNode().getAttributes().getNamedItem("title").getNodeValue());

        locator = new IdLocator(tester, "action_id_2");
        text.assertTextPresent(locator, "Close Issue");
        assertEquals("", locator.getNode().getAttributes().getNamedItem("title").getNodeValue());


    }
}