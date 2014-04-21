package com.atlassian.jira.webtest.selenium.harness.util;

import com.atlassian.jira.functest.framework.navigation.BulkChangeWizard;
import com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation;
import com.atlassian.jira.functest.framework.security.xsrf.XsrfCheck;
import com.atlassian.jira.pageobjects.global.User;
import com.atlassian.jira.webtest.framework.core.Timeouts;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContextAware;
import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import org.apache.commons.lang.StringUtils;

import static com.atlassian.jira.functest.framework.FunctTestConstants.ADMIN_PASSWORD;
import static com.atlassian.jira.functest.framework.FunctTestConstants.ADMIN_USERNAME;

public class NavigatorImpl extends SeleniumContextAware implements Navigator
{
    private static final String EDIT_ISSUE_PAGE_WAIT = "50000";
    private static final String CHANGE_HISTORY_TAB_ID = "changehistory-tabpanel";
    private final IssueNavigation issueNavigation;
    private final IssueNavigatorNavigation issueNavigatorNavigation;

    private String lastPasswordUsed;

    public NavigatorImpl(SeleniumContext ctx)
    {
        super(ctx);
        this.issueNavigation = new IssueNavigationImpl(context, this);
        this.issueNavigatorNavigation = new IssueNavigatorNavigationImpl(client, assertThat);
    }

    public Navigator login(final String username)
    {
        return login(username,username, null);
    }

    public Navigator login(String username, String password)
    {
        return login(username,password, null);
    }

    public Navigator loginAsSystemAdmin(String destination)
    {
        return login(ADMIN_USERNAME, ADMIN_PASSWORD, destination);
    }

    public Navigator login(User user, String destination)
    {
        return login(user.getUserName(), user.getPassword(), destination);
    }

    private Navigator login(String username, String password, String destination)
    {
        try
        {
            // are we already logged in as that user
            assertThat.elementPresent("id=header-details-user-fullname");
            assertThat.attributeContainsValue("id=header-details-user-fullname", "data-username", username);
            if (StringUtils.isNotBlank(destination))
            {
                gotoPage(destination, true);
            }
        }
        catch (AssertionFailedError e)
        {
            try
            {
                // are we on the login page already
                assertThat.elementPresent("//input[@id='login-form-submit']");
            }
            catch (AssertionFailedError ae)
            {
                String destinationQuery = StringUtils.isBlank(destination) ? "" : "?os_destination=/" + destination;
                gotoPage("login.jsp" + destinationQuery, true);
            }

            client.type("os_username", username);
            client.type("os_password", password);
            lastPasswordUsed =  password;
            clickAndWaitForPageLoad("login-form-submit");

        }
        return this;
    }
    
    public Navigator logout(String xsrfToken)
    {
        gotoPage("logout?" + XsrfCheck.ATL_TOKEN +"="+ xsrfToken, true);
        return this;
    }

    public Navigator gotoPage(String relativeUrl, boolean waitForPageToLoad)
    {
        client.open(context.environmentData().getBaseUrl() + "/" + relativeUrl, "false"); // we expect valid response
        if(waitForPageToLoad)
        {
            client.waitForPageToLoad(context.timeoutFor(Timeouts.PAGE_LOAD));
        }
        return this;
    }

    public Navigator gotoHome()
    {
        gotoPage("", true);
        client.waitForPageToLoad(context.timeoutFor(Timeouts.PAGE_LOAD));    
        return this;
    }

    public Navigator gotoManageFilters()
    {
        gotoPage("secure/ManageFilters.jspa", true);
        client.waitForPageToLoad(context.timeoutFor(Timeouts.PAGE_LOAD));
        return this;
    }

    public Navigator gotoFindIssues()
    {
        clickAndWaitForPageLoad("find_link");
        return this;
    }

    public Navigator gotoFindIssuesSimple()
    {
        issueNavigatorNavigation.gotoEditOrNewMode(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        return this;
    }

    public Navigator gotoFindIssuesAdvanced()
    {
        issueNavigatorNavigation.gotoEditOrNewMode(IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        return this;
    }

    public Navigator gotoAdmin()
    {
        if (client.isElementPresent("id=admin_link"))
        {
            clickAndWaitForPageLoad("admin_link");
        }

        //need to click on all dropdowns to make sure the links beneath work
        clickLinkIfPresent("admin_project_menu");
        clickLinkIfPresent("admin_plugins_menu");
        clickLinkIfPresent("admin_users_menu");
        clickLinkIfPresent("admin_issues_menu");
        clickLinkIfPresent("admin_system_menu");
        clickLinkIfPresent("system.admin");

        return this;
    }

    private void clickLinkIfPresent(final String linkId)
    {
        if(client.isElementPresent(linkId))
        {
            client.click(linkId);
        }
    }

    public void webSudoAuthenticate(final String password)
    {
        if (client.isElementPresent("id=login-notyou"))
        {
            client.typeInElementWithName("webSudoPassword", password);
            client.clickButtonWithName("authenticate",true);
            client.waitForPageToLoad();
        }
    }

    public void webSudoAuthenticateUsingLastPassword()
    {
        webSudoAuthenticate(lastPasswordUsed != null ? lastPasswordUsed : "admin");
    }

    public void disableWebSudo()
    {

    }

    public Navigator gotoUserProfile()
    {
        gotoPage("secure/ViewProfile.jspa", true);

        return this;
    }

    public Navigator gotoUserProfile(String user)
    {
        gotoPage("secure/ViewProfile.jspa?name=" + user, true);

        return this;
    }

    public Navigator gotoUserProfileTab(String tab)
    {
        gotoPage("secure/ViewProfile.jspa?selectedTab=" + tab, true);

        return this;
    }

    public Navigator gotoUserProfileTab(String tab, String user)
    {
        gotoPage("secure/ViewProfile.jspa?name=" + user + "&selectedTab=" + tab, true);

        return this;
    }

    public Navigator gotoIssue(String issueKey)
    {
        gotoPage("browse/" + issueKey, true);
        client.getEval("this.browserbot.getCurrentWindow().onbeforeunload = function() {}");
        return this;
    }

    public Navigator openHistoryTab()
    {
        if (client.isElementPresent("jquery=a#" + CHANGE_HISTORY_TAB_ID))
        {
            clickAndWaitForPageLoad(CHANGE_HISTORY_TAB_ID);
        } else if (!client.isElementPresent("jquery=li#" + CHANGE_HISTORY_TAB_ID))
        {
            throw new AssertionError("Not on the view issue page");
        }
        return this;
    }

    protected void editCurrentIssue()
    {
        client.open(client.getAttribute("edit-issue@href"));
        client.waitForPageToLoad(EDIT_ISSUE_PAGE_WAIT);
    }

    public Navigator editIssue(String issueKey)
    {
        gotoIssue(issueKey);
        editCurrentIssue();
        return this;
    }

    public Navigator findAllIssues()
    {
        issueNavigatorNavigation.displayAllIssues();
        return this;
    }

    public Navigator findIssuesWithJql(String jql)
    {
        issueNavigatorNavigation.createSearch(jql);
        return this;
    }

    public Navigator clickAndWaitForPageLoad(String id)
    {
        client.click(id);
        client.waitForPageToLoad(context.timeoutFor(Timeouts.SLOW_PAGE_LOAD));
        return this;
    }

    public Navigator click(final String id)
    {
        client.click(id);
        return this;
    }


    public Navigator gotoBrowseProject(final String projectName)
    {
        this.gotoPage("/secure/BrowseProjects.jspa", true);
        client.click("link=" + projectName, true);
        return this;
    }

    public String createIssue(final String projectName, final String issueType, final String summary)
    {
        gotoCreateIssueScreen(projectName, issueType);
        client.type("summary", summary);
        client.click("Create");
        client.waitForPageToLoad(context.timeoutFor(Timeouts.PAGE_LOAD));

        String issueKey = client.getText("Id=key-val");
        Assert.assertNotNull(issueKey);
        return issueKey;
    }

    public void browseProject(final String projectKey)
    {
        gotoPage("/browse/" + projectKey, true);
    }

    public Dashboard dashboard(final String id)
    {
        return new DashboardImpl(id, client, context.environmentData());
    }

    public Dashboard currentDashboard()
    {
        return new DashboardImpl(null, client, context.environmentData());
    }

    public Navigator expandAllNavigatorSections()
    {
        issueNavigatorNavigation.expandAllNavigatorSections();
        return this;
    }

    public Navigator expandContentSection(final String sectionId)
    {
        if(client.isElementPresent("id=" + sectionId))
        {
            final String toggleClass = client.getAttribute("id=" + sectionId + "@class");
            if(toggleClass.contains("collapsed"))
            {
                client.click("jquery=#"+sectionId+" .toggle-title");
            }
        }
        return this;
     }

    public BulkChangeWizard bulkChange(final IssueNavigatorNavigation.BulkChangeOption bulkChangeOption)
    {
        return issueNavigatorNavigation.bulkChange(bulkChangeOption);
    }

    public IssueNavigation issue()
    {
        return issueNavigation;
    }

    public IssueNavigatorNavigation issueNavigator()
    {
        return issueNavigatorNavigation;
    }

    public Navigator gotoCreateIssueScreen(final String project, final String issueType)
    {
        this.gotoPage("/secure/CreateIssue!default.jspa", true);

        if (project != null)
        {
            client.select("project", "label=" + project);
        }
        if (issueType != null)
        {
            client.select("issuetype", "label=" + issueType);
        }
        client.click("Next");
        client.waitForPageToLoad(context.timeoutFor(Timeouts.PAGE_LOAD));
        return this;
    }

    public Navigator collapseContentSection(final String sectionId)
    {
        if(client.isElementPresent("id=" + sectionId))
        {
            final String toggleClass = client.getAttribute("id=" + sectionId + "@class");
            if(!toggleClass.contains("collapsed"))
            {
                client.click("jquery=#"+sectionId+" .toggle-title");
            }
        }
        return this;
    }
}
