package com.atlassian.jira.jelly.tag.issue;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.jelly.AbstractJellyTestCase;
import com.atlassian.jira.project.AssigneeTypes;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.upgrade.UpgradeTask;
import com.atlassian.jira.upgrade.tasks.UpgradeTask_Build101;
import com.atlassian.jira.upgrade.tasks.UpgradeTask_Build83;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import com.opensymphony.user.User;
import electric.xml.Document;
import electric.xml.Element;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ActionContext;

import java.util.Collection;

public class TestLinkIssue extends AbstractJellyTestCase
{
    private User u;
    GenericValue project1;
    GenericValue issueLinkType1;

    public TestLinkIssue(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        u = UtilsForTests.getTestUser("logged-in-user");
        JiraTestUtil.loginUser(u);
        ManagerFactory.getGlobalPermissionManager().addPermission(Permissions.ADMINISTER, null);

        UtilsForTests.getTestEntity("IssueType", EasyMap.build("id", "1", "name", "Bug", "sequence", new Long(1)));

        // CReate field screens
        UpgradeTask upgradeTask = (UpgradeTask) JiraUtils.loadComponent(UpgradeTask_Build83.class);
        upgradeTask.doUpgrade(false);

        UpgradeTask upgradeTask101 = (UpgradeTask) JiraUtils.loadComponent(UpgradeTask_Build101.class);
        upgradeTask101.doUpgrade(false);

        project1 = UtilsForTests.getTestEntity("Project", EasyMap.build("key", "ABC", "name", "Project 1", "lead", u.getName(), "counter", new Long(1), "assigneetype", AssigneeTypes.UNASSIGNED));
        ComponentManager.getInstance().getIssueTypeScreenSchemeManager().associateWithDefaultScheme(project1);

        issueLinkType1 = UtilsForTests.getTestEntity("IssueLinkType", EasyMap.build("linkname", "Duplicate", "inward", "is duplicated by", "outward", "duplicates"));

        ManagerFactory.getApplicationProperties().setOption(APKeys.JIRA_OPTION_ISSUELINKING, true);
        JiraTestUtil.setupAndAssociateDefaultPermissionSchemeWithPermission(project1, Permissions.BROWSE);
        JiraTestUtil.setupAndAssociateDefaultPermissionSchemeWithPermission(project1, Permissions.LINK_ISSUE);
    }

    /**
     * Link existing issues
     */
    public void testLinkIssues() throws Exception
    {
        JiraAuthenticationContext authenticationContext = ComponentAccessor.getJiraAuthenticationContext();
        // Log in user
        authenticationContext.setLoggedInUser(u);

        //Create issues
        GenericValue issue1 = UtilsForTests.getTestEntity("Issue", EasyMap.build("type", "1", "summary", "summary 1", "project", project1.getLong("id"), "key", "ABC-1"));
        GenericValue issue2 = UtilsForTests.getTestEntity("Issue", EasyMap.build("type", "1", "summary", "summary 2", "project", project1.getLong("id"), "key", "ABC-2"));

        final String scriptFilename = "link-issue-test-link-issue.jelly";
        Document document = runScript(scriptFilename);

        final Element root = document.getRoot();
        assertEquals(0, root.getElements().size());

        final Collection issueLinks = CoreFactory.getGenericDelegator().findAll("IssueLink");
        assertFalse(issueLinks.isEmpty());
        assertEquals(1, issueLinks.size());

        //Check to see if the issues was linked
        GenericValue issueLink = (GenericValue) issueLinks.iterator().next();
        assertEquals(issueLink.get("source"), issue1.getLong("id"));
        assertEquals(issueLink.get("destination"), issue2.getLong("id"));
        assertEquals(issueLink.get("linktype"), issueLinkType1.getLong("id"));

        // Log out user
        authenticationContext.setLoggedInUser(null);
    }

    /**
     * Link Created issues from different projects
     */
    public void testLinkCreatedIssues() throws Exception
    {
        JiraAuthenticationContext authenticationContext = ComponentAccessor.getJiraAuthenticationContext();
        // Log in user
        authenticationContext.setLoggedInUser(u);

        //create another project and set required permissions
        GenericValue project2 = UtilsForTests.getTestEntity("Project", EasyMap.build("key", "DEF", "name", "Project 2", "lead", u.getName(), "counter", new Long(1), "assigneetype", AssigneeTypes.UNASSIGNED));
        ComponentManager.getInstance().getIssueTypeScreenSchemeManager().associateWithDefaultScheme(project2);
        ManagerFactory.getApplicationProperties().setOption(APKeys.JIRA_OPTION_ALLOWUNASSIGNED, true);
        JiraTestUtil.setupAndAssociateDefaultPermissionSchemeWithPermission(project1, Permissions.CREATE_ISSUE);
        JiraTestUtil.setupAndAssociateDefaultPermissionSchemeWithPermission(project2, Permissions.BROWSE);
        JiraTestUtil.setupAndAssociateDefaultPermissionSchemeWithPermission(project2, Permissions.LINK_ISSUE);
        JiraTestUtil.setupAndAssociateDefaultPermissionSchemeWithPermission(project2, Permissions.CREATE_ISSUE);

        final String scriptFilename = "link-issue-test-link-created-issue.jelly";
        Document document = runScript(scriptFilename);

        final Element root = document.getRoot();
        assertEquals(0, root.getElements().size());

        final Collection issues = CoreFactory.getGenericDelegator().findAll("Issue");
        assertFalse(issues.isEmpty());
        assertEquals(2, issues.size());

        final Collection issueLinks = CoreFactory.getGenericDelegator().findAll("IssueLink");
        assertFalse(issueLinks.isEmpty());
        assertEquals(1, issueLinks.size());

        //Check to see if the issues was linked
        GenericValue issueLink = (GenericValue) issueLinks.iterator().next();
        assertEquals(issueLink.get("source").toString(), StringUtils.split(root.getTextString().trim(), ":")[0]);
        assertEquals(issueLink.get("destination").toString(), StringUtils.split(root.getTextString().trim(), ":")[1]);
        assertEquals(issueLink.get("linktype"), issueLinkType1.getLong("id"));

        // Log out user
        authenticationContext.setLoggedInUser(null);
    }

    protected String getRelativePath()
    {
        return "tag" + FS + "issue" + FS;
    }
}
