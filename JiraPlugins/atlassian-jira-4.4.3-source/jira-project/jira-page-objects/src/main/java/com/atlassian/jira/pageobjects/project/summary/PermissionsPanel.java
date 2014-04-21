package com.atlassian.jira.pageobjects.project.summary;

import com.atlassian.jira.pageobjects.pages.admin.EditIssueSecurityScheme;
import com.atlassian.jira.pageobjects.pages.admin.SelectIssueSecurityScheme;
import com.atlassian.jira.pageobjects.project.issuesecurity.IssueSecurityPage;
import com.atlassian.jira.pageobjects.project.permissions.ProjectPermissionPageTab;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;

import static org.junit.Assert.assertTrue;

/**
 * @since v4.4
 */
public class PermissionsPanel extends AbstractSummaryPanel
{

    @ElementBy(id = "project-config-permissions")
    private PageElement permissionsElement;

    @ElementBy(id = "project-config-issue-sec")
    private PageElement issueSecurity;


    public boolean isHasPermissionScheme()
    {
        return permissionsElement.isPresent();
    }

    public String getPermissionScheme()
    {
        return permissionsElement.getText();
    }

    public boolean isCanEditPermissionScheme()
    {
        return isHasPermissionScheme() && permissionsElement.getAttribute("href") != null;
    }

    public ProjectPermissionPageTab gotoPermissionsPage()
    {
        assertTrue("User cannot see link to edit permission scheme.", isCanEditPermissionScheme());
        permissionsElement.click();
        return getBinder().bind(ProjectPermissionPageTab.class, getProjectKey());
    }

    public String getIssueSecurityScheme()
    {
        return issueSecurity.getText();
    }

    public IssueSecurityPage gotoIssueSecurityPage()
    {
        issueSecurity.click();
        return getBinder().bind(IssueSecurityPage.class, getProjectKey());
    }
}
