package com.atlassian.jira.webtest.webdriver.tests.projectconfig;

import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.jira.pageobjects.project.ProjectConfigActions;
import com.atlassian.jira.pageobjects.project.summary.EditProjectDialog;
import com.atlassian.jira.pageobjects.project.summary.ProjectSummaryPageTab;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @since v4.4
 */
@WebTest ({ Category.WEBDRIVER_TEST, Category.ADMINISTRATION, Category.PLUGINS, Category.PROJECTS })
@Restore ("xml/TestEditProjectDialog.xml")
public class TestEditProject extends BaseJiraWebTest
{
    private static final String NEW_URL = "http://www.realsurf.com";
    private static final String NEW_DESCRIPTION = "Yeah you would like me to set a description";
    private static final String NEW_NAME = "Scott's Project";
    private static final String NEW_AVATAR_ID = "10008";
    private static final String INVALID_URL = "dfgsfdgs";

    @Test
    public void testSuccessfulEdit()
    {

        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);

        ProjectSummaryPageTab projectSummaryPageTab = navigateToSummaryPageFor("BLA");

        EditProjectDialog editProjectDialog = openEditProjectDialog(projectSummaryPageTab);

        editProjectDialog.setProjectName(NEW_NAME)
                .setUrl(NEW_URL)
                .setAvatar(NEW_AVATAR_ID)
                .setDescription(NEW_DESCRIPTION)
                .submit();

        projectSummaryPageTab = navigateToSummaryPageFor("BLA");

        assertEquals(NEW_NAME, projectSummaryPageTab.getProjectHeader().getProjectName());
        assertEquals(NEW_AVATAR_ID, projectSummaryPageTab.getProjectHeader().getProjectAvatarIconId());
        assertEquals(NEW_URL, projectSummaryPageTab.getProjectHeader().getProjectUrl());
        assertEquals(NEW_DESCRIPTION, projectSummaryPageTab.getProjectHeader().getDescription());
    }

    @Test
    public void testEditWithInlineErrors()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);

        ProjectSummaryPageTab projectSummaryPageTab = navigateToSummaryPageFor("BLA");

        EditProjectDialog editProjectDialog = openEditProjectDialog(projectSummaryPageTab);

        String longName = "";

        for (int i =0; i < 152; i++)
        {
            longName += "a";
        }

        editProjectDialog.setProjectName(longName)
                .setUrl(INVALID_URL)
                .submit();

        editProjectDialog = pageBinder.bind(EditProjectDialog.class);

        Map<String, String> errors = editProjectDialog.getFormErrors();

        assertEquals("The URL specified is not valid - it must start with http://", errors.get("url"));
        assertEquals("The project name must not exceed 150 characters in length.", errors.get("name"));
    }

    @Test
    public void testEditWithInvalidProjectLead()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);
        ProjectSummaryPageTab projectSummaryPageTab = navigateToSummaryPageFor("MKY");
        EditProjectDialog editProjectDialog = openEditProjectDialog(projectSummaryPageTab);

        assertEquals("The user you have specified as project lead does not exist. Please select a valid user before editing this project.",
                editProjectDialog.getErrorMessage());
    }

    private ProjectSummaryPageTab navigateToSummaryPageFor(final String projectKey)
    {
        return pageBinder.navigateToAndBind(ProjectSummaryPageTab.class, projectKey);
    }

    private EditProjectDialog openEditProjectDialog(final ProjectSummaryPageTab projectSummaryPageTab)
    {
         return projectSummaryPageTab.openOperations()
                .click(EditProjectDialog.class, ProjectConfigActions.ProjectOperations.EDIT);
    }
}
