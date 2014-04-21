package com.atlassian.jira.webtest.selenium.harness.util;

import com.atlassian.jira.functest.framework.navigation.BulkChangeWizard;
import com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation;
import com.atlassian.jira.pageobjects.global.User;

/**
 * Provides helper methods for navigating around JIRA.
 */
public interface Navigator
{
    /**
     * Login using the user name and password as the same value
     *
     * @param username the username and password to use
     * @return a Navigator instance of call chaining
     */
    Navigator login(String username);

    Navigator login(String username, String password);

    /**
     * Login and redirect to a specified destination.
     * User must not be already logged in (leaving full implementation of that for PageObjects).
     *
     * @param user User to extract username and password from
     * @param destination destination to set as os_destination in the login URL
     * @return a Navigator instance of call chaining
     */
    Navigator login(User user, String destination);

    /**
     * Login as the system administrator and redirect to a specified destination.
     * Waits for the desination page to load.
     * Admin must not be already logged in (leaving full implementation of that for PageObjects).
     *
     * @param destination URL chunk to set as os_destination in the login URL
     * @return a Navigator instance of call chaining
     */
    Navigator loginAsSystemAdmin(String destination);

    Navigator logout(String xsrfToken);

    /**
     * Navigates to the page specified by the relative URL.
     * <p/>
     * NOTE: URL must be relative. Do not use leading '/' as this breaks on Orion server.
     *
     * @param relativeUrl relative URL - not starting with '/'
     * @return this navigator
     */
    Navigator gotoPage(String relativeUrl, boolean waitForPageLoad);

    Navigator gotoHome();

    /**
     * Navigates to the Manage Filters page.
     * @return this navigator
     */
    Navigator gotoManageFilters();

    /**
     * 
     * @return
     */
    Navigator gotoFindIssues();

    /**
     * Goes to the issue navigator and ensures the simple search mode.
     *
     * @return the Navigator
     */
    Navigator gotoFindIssuesSimple();

    /**
     * Goes to the JQL view of the issue navigator.
     * 
     * @return the Navigator
     */
    Navigator gotoFindIssuesAdvanced();

    /**
     *
     * @return
     */
    Navigator gotoAdmin();

    /**
     * Checks if we have been redirected to the websudo login form and logs in.
     * @param password
     */
    public void webSudoAuthenticate (final String password);

    public void webSudoAuthenticateUsingLastPassword ();

    public void disableWebSudo ();

    /**
     *
     *
     * @return
     */
    Navigator gotoUserProfile();

    /**
     *
     * @param user
     * @return
     */
    Navigator gotoUserProfile(String user);

    /**
     * 
     * @param tab
     * @return
     */
    Navigator gotoUserProfileTab(String tab);

    /**
     *
     * 
     * @param tab
     * @param user
     * @return
     */
    Navigator gotoUserProfileTab(String tab, String user);

    /**
     *
     * 
     * @param issueKey
     * @return this navigator instance
     */
    Navigator gotoIssue(String issueKey);

    /**
     * Go to the 'History' tab on the view issue screen.
     *
     * @return this navigator instance
     */
    Navigator openHistoryTab();

    /**
     *
     * 
     * @param issueKey
     * @return
     */
    Navigator editIssue(String issueKey);

    /**
     * Finds all issues using simple navigation. Use {@link #findIssuesWithJql(String)} for JQL searching
     *
     * NOTE: This will reset the issue navigator navType to simple if it is currently JQL
     *
     * @return this navigator
     */
    Navigator findAllIssues();

    /**
     *
     *
     * @param jql
     * @return
     */
    Navigator findIssuesWithJql(String jql);

    /**
     *
     *
     * @param projectName
     * @return
     */
    Navigator gotoBrowseProject(String projectName);

    /**
     * Click and wait for the page to load
     *
     * @param id element id
     * @return this navigator
     */
    Navigator clickAndWaitForPageLoad(String id);

    /**
     *
     * 
     * @param id
     * @return
     */
    Navigator click(String id);

    /**
     * Creates an issue in the database
     *
     * @param projectName - the name of the project - can be nulll
     * @param issueType   the type of issue - can be nulll
     * @param summary     the summary of the issue
     * @return the newly created issue key
     */
    String createIssue(String projectName, String issueType, String summary);


    /**
     * Goes to the browse page for the project with the given key.
     *
     * @param projectKey the project's key.
     */
    void browseProject(String projectKey);

    /**
     * Returns dashboard navigation and utilities for the dashboard with the given id.
     *
     * @param id the id of the dashboard
     * @return A Dasbhoard navigation instance
     */
    Dashboard dashboard(String id);

    /**
     * Goes to the dashboard action which will navigate to the most recent dashboard in the session.
     *
     * @return A Dasbhoard navigation instance
     */
    Dashboard currentDashboard();

    /**
     * Expands all collapsed navigator sections in the left hand column of the issuenavigator
     *
     * @return this navigator
     */
    public Navigator expandAllNavigatorSections();

    /**
     * Collapses a particular expanded content session in the content pane of the issuenavigator
     *
     * @param sectionId the id of the section to collapse
     * @return this navigator
     */
    public Navigator collapseContentSection(String sectionId);

    /**
     * Expands a particular collapsed content session in the content pane of the issuenavigator
     *
     * @param sectionId the id of the section to expand
     * @return this navigator
     */
    public Navigator expandContentSection(String sectionId);

    /**
     * Initiate the bulk change wizard on the current search results.
     *
     * @param bulkChangeOption whether to bulk change all results or just the current page.
     * @return an instance of the bulk change wizard which will be used to step through the wizard.
     */
    BulkChangeWizard bulkChange(IssueNavigatorNavigation.BulkChangeOption bulkChangeOption);

    /**
     * Returns a handle on issue navigation utilities.
     * @return Issue navigation utilities
     */
    IssueNavigation issue();

    /**
     * Returns an object that can be used to control the issue navigator.
     *
     * @return an object that can be used to control the issuer navigator.
     */
    IssueNavigatorNavigation issueNavigator();

    /**
     * Goes to the first page for creating an issue but doesn't actually create an issue.
     *
     * @param project The name of the project to create the issue in
     * @param issueType The type of issue to create
     * @return this object
     */
    Navigator gotoCreateIssueScreen(String project, String issueType);
}
