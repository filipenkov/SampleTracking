/*
 * Copyright (c) 2002-2005
 * All rights reserved.
 */

package com.atlassian.jira.webtests.ztests.user;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;

/**
 * Tests the GroupCF permission type, which allows a select-list (or radiobutton, etc) custom field to specify a group,
 * which is then granted certain permissions.
 */
@WebTest ({ Category.FUNC_TEST, Category.PERMISSIONS, Category.USERS_AND_GROUPS })
public class TestGroupSelectorPermissions extends JIRAWebTest
{
    public void setUp()
    {
        super.setUp();
        restoreData("GroupSelectorPermissions.xml");
        // Ensure attachments are enabled
        administration.attachments().enable();
    }

    public TestGroupSelectorPermissions(String name)
    {
        super(name);
    }

    public void testWorkflowPermissions()
    {
        // In this data we grant the 'comment' permission to a 'GroupRadio' selector, and 'attach' permission to an 'Assigned Groups' selector
        // Initially, GroupRadio is set to 'HelpDesk' and 'Assigned Groups' is unset.
        logSection("Testing group permission selector");

        log("Testing that 'GroupRadio' selection (helpdesk) can comment, can't attach");
        login("helpdesk", "helpdesk");
        gotoIssue("NP-1");
        assertLinkPresent("comment-issue");
        assertLinkNotPresent("attach-file");
        assertLinkNotPresent("delete-issue");
        assertLinkNotPresent("move-issue");

        log("Check that webadmin users cannot do anything");
        login("webadmin", "webadmin");
        gotoIssue("NP-1");
        assertLinkNotPresent("comment-issue");
        assertLinkNotPresent("attach-file");
        assertLinkNotPresent("delete-issue");
        assertLinkNotPresent("move-issue");

        log("Check that unixadmin users cannot do anything");
        login("unixadmin", "unixadmin");
        gotoIssue("NP-1");
        assertLinkNotPresent("comment-issue");
        assertLinkNotPresent("attach-file");
        assertLinkNotPresent("delete-issue");
        assertLinkNotPresent("move-issue");

        log("Testing that regular users can't comment, can't attach");
        login("test", "test");
        gotoIssue("NP-1");
        assertLinkNotPresent("comment-issue");
        assertLinkNotPresent("attach-file");
        assertLinkNotPresent("delete-issue");
        assertLinkNotPresent("move-issue");

        login("dba", "dba");
        gotoIssue("NP-1");
        assertLinkNotPresent("comment-issue");
        assertLinkNotPresent("attach-file");
        assertLinkNotPresent("delete-issue");
        assertLinkNotPresent("move-issue");

        // Now we change the GroupRadio (comment) selector and set the 'Assigned groups' selector
        log("Editing fields: setting GroupRadio (comment perm) to WebAdmin, and 'Assigned Groups' (attach perm) to helpdesk");
        gotoIssue("NP-1");
        clickLink("edit-issue");
        assertOptionValuesEqual("customfield_10010", new String[] {"-1", "10000", "10001", "10002", "10003"}); //{"-1", "dba-user-group", "help-desk-group", "unix-admin-group", "webadmin-group"}
        assertOptionValuesEqual("customfield_10030", new String[] {"-1", "10020", "10021", "10022", "10023"}); //{"-1", "dba-user-group", "help-desk-group", "unix-admin-group", "webadmin-group"}
        assertOptionValuesEqual("customfield_10040", new String[] {"10030", "10031", "10032", "10033"}); //{"dba-user-group", "help-desk-group", "unix-admin-group", "webadmin-group"}
        assertOptionValuesEqual("customfield_10041", new String[] {"-1", "10034", "10035", "10036", "10037"}); // {"-1", "dba-user-group", "help-desk-group", "unix-admin-group", "webadmin-group"}
        selectOption("customfield_10010", "help-desk-group"); // Assigned Group (attach)
        checkCheckbox("customfield_10030", "10023"); // GroupRadio (comment) "webadmin-group"
        setFormElement("customfield_10040", "10030"); // Assigned Group (move)
        selectOption("customfield_10041", "unix-admin-group"); // Assigned Group (delete)
        submit();
        assertTextPresentBeforeText("Assigned Groups:", "help-desk-group"); // attach
        assertTextPresentBeforeText("GroupRadio:", "webadmin-group"); // comment
        assertTextPresentBeforeText("Multi Checkboxes:", "dba-user-group"); // move
        assertTextPresentBeforeText("Select List:", "unix-admin-group"); // delete

        log("Testing that 'GroupRadio' selection (now webadmin) can comment, can't attach");
        login("webadmin", "webadmin");
        gotoIssue("NP-1");
        assertLinkNotPresent("attach-file");
        assertLinkPresent("comment-issue");
        assertLinkNotPresent("delete-issue");
        assertLinkNotPresent("move-issue");

        log("Testing that helpdesk can no longer comment, but can attach");
        login("helpdesk", "helpdesk");
        gotoIssue("NP-1");
        assertLinkPresent("attach-file");
        assertLinkNotPresent("comment-issue");
        assertLinkNotPresent("delete-issue");
        assertLinkNotPresent("move-issue");

        log("Check that DBA can delete issues");
        login("dba", "dba");
        gotoIssue("NP-1");
        assertLinkNotPresent("attach-file");
        assertLinkNotPresent("comment-issue");
        assertLinkNotPresent("delete-issue");
        assertLinkPresent("move-issue");

        log("Check that Unix admin can move issues");
        login("unixadmin", "unixadmin");
        gotoIssue("NP-1");
        assertLinkNotPresent("attach-file");
        assertLinkNotPresent("comment-issue");
        assertLinkPresent("delete-issue");
        assertLinkNotPresent("move-issue");
    }
}
