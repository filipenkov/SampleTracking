/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.webtests.ztests.misc;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;
import org.junit.Ignore;

/**
 * Not included in AcceptanceTest as these tests cannot be fully automated yet.
 */
@Ignore ("These tests cannot be fully automated. They require Perforce to be setup and configured.")
@WebTest({ Category.FUNC_TEST, Category.RELOADABLE_PLUGINS })
public class TestPerforcePlugin extends JIRAWebTest
{
    public TestPerforcePlugin(String name)
    {
        super(name);
    }

    /**
     * Verify that the P4 plugin is displayed in plugin page.
     */
    public void testVerifyPluginInstalled()
    {
        restoreData("TestPerforceData.xml");
        gotoAdmin();
        clickLink("plugins");
        assertTextPresent("Perforce Plugin");
        clickLinkWithText("Perforce Plugin");
        assertTextPresent("JIRA Perforce Plugin");
    }

    /**
     * Verify that P4 changes tab is displayed with correct details.
     */
    public void testVerifyP4TabDetails()
    {
        restoreData("TestPerforceData.xml");
        gotoIssue("TST-1");

        assertTextPresent("P4 Changes");
        clickLinkWithText("P4 Changes");
        assertTextPresent("Change 2 by");
        assertTextPresent("Change 7 by");
        assertTextPresent("Change 23 by");
        assertTextPresent("Change 25 by");
        assertTextPresent("Change 29 by");
        assertTextPresent("Change - for <a href=\"http://dogbolter:8080/browse/TST-1\" title=\"Test\">TST-1</a>.");
    }

    /**
     * Test that the P4 job can be created and verify display when viewing issue.
     */
    public void testVerifyP4JobDetails()
    {
        restoreData("TestPerforceData.xml");

        createP4CustomField();

        gotoIssue("TST-1");

        clickLink("edit-issue");

        assertTextPresent("Create Perforce job");
        submit("Update");
        assertTextPresent("P4 Job");
        assertTextPresent("No Perforce job exists for this issue.");
        assertLinkWithTextNotPresent("P4 Job");
    }

    /**
     * Test that the 'Set P4 Job' workflow post-function is available and can be set
     */
    public void testVerifyP4PostFunctionAvailable()
    {
        restoreData("TestPerforceData.xml");

        administration.workflows().goTo().copyWorkflow("jira", "Test Workflow");
        clickLink("steps_live_Test Workflow");
        clickLinkWithText("Open");
        clickLinkWithText("Create Issue");
        clickLinkWithText("Post Functions");
        clickLinkWithText("Add");
        checkCheckbox("type", "com.atlassian.jira.plugin.ext.perforce:createjob-function");
        submit("Add");
        assertTextPresent("Creates a Perforce job for this issue (if required) during a transition.");
    }

    /**
     * Test that the workflow post-function correctly sets the P4 job.
     */
    public void testP4JobCreation()
    {
        restoreData("TestPerforceWorkflow.xml");

        createP4CustomField();

        navigation.issue().goToCreateIssueForm(null,null);
        setFormElement("summary", "Test");
        assertTextPresent("Create Perforce job");
        submit("Create");
        assertTextPresent("P4 Job:");
        assertTextPresent("Perforce job exists");
        clickLinkWithText("P4 Job");
        assertTextPresent("This shows all of the change lists associated with this issue's Perforce job");
    }

    /**
     * Test that the workflow post-function correctly sets the hidden P4 job.
     */
    public void testP4JobHiddenCreation()
    {
        restoreData("TestPerforceWorkflow.xml");

        createHiddenP4CustomField();

        navigation.issue().goToCreateIssueForm(null,null);
        setFormElement("summary", "Test");
        assertTextNotPresent("Create Perforce job");
        submit("Create");
        assertTextPresent("P4 Job:");
        assertTextPresent("Perforce job exists");
        clickLinkWithText("P4 Job");
        assertTextPresent("This shows all of the change lists associated with this issue's Perforce job");
    }

    /**
     * Verifies that the correct issues are displayed based on the P4 Job searcher
     */
    public void testP4IssueNavColumns()
    {

        restoreData("TestPerforceNav.xml");

        clickLink("find_link");
        submit("show");
        clickLinkWithText("Configure");
        selectOption("fieldId", "P4 Job");
        submit("add");
        assertTextPresent("P4 Job");
        clickLink("find_link");
        selectOption("customfield_10000", "with or without");
        submit("show");
        assertTextPresent("<a href=\"/browse/TST-1\">TST-1</a>");
        assertTextPresent("<a href=\"/browse/TST-2\">TST-2</a>");
        assertTextPresent("No Perforce job exists for this issue.");
        assertTextPresent("Perforce job exists");
        assertTextPresent("<a href=\"/browse/TST-1?page=com.atlassian.jira.plugin.ext.perforce:perforce-job-tabpanel\">view details</a>.");

        selectOption("customfield_10000", "with");
        submit("show");
        assertTextPresent("<a href=\"/browse/TST-1\">TST-1</a>");
        assertTextPresent("Perforce job exists");
        assertTextPresent("<a href=\"/browse/TST-1?page=com.atlassian.jira.plugin.ext.perforce:perforce-job-tabpanel\">view details</a>.");
        assertTextNotPresent("<a href=\"/browse/TST-2\">TST-2</a>");
        assertTextNotPresent("No Perforce job exists for this issue.");

        selectOption("customfield_10000", "without");
        submit("show");

        assertTextNotPresent("<a href=\"/browse/TST-1\">TST-1</a>");
        assertTextNotPresent("<a href=\"/browse/TST-1?page=com.atlassian.jira.plugin.ext.perforce:perforce-job-tabpanel\">view details</a>.");
        assertTextPresent("<a href=\"/browse/TST-2\">TST-2</a>");
        assertTextPresent("No Perforce job exists for this issue.");
    }

    /**
     * Create a P4 custom field.
     */
    private void createP4CustomField()
    {
        gotoAdmin();
        clickLink("view_custom_fields");
        clickLink("add_custom_fields");
        checkCheckbox("fieldType", "com.atlassian.jira.plugin.ext.perforce:jobcheckbox");
        submit("nextBtn");
        setFormElement("fieldName", "P4 Job");
        submit("nextBtn");
        checkCheckbox("associatedScreens", "1");
        submit("Update");
        assertTextPresent("P4 Job");
        assertTextPresent("Job Checkbox");
    }

    /**
     * Create a hidden P4 custom field.
     */
    private void createHiddenP4CustomField()
    {
        gotoAdmin();
        clickLink("view_custom_fields");
        clickLink("add_custom_fields");
        checkCheckbox("fieldType", "com.atlassian.jira.plugin.ext.perforce:hiddenjobswitch");
        submit("nextBtn");
        setFormElement("fieldName", "Hidden P4 Job");
        submit("nextBtn");
        checkCheckbox("associatedScreens", "1");
        submit("Update");
        assertTextPresent("Hidden P4 Job");
        assertTextPresent("Hidden Job Switch");
    }
}
