package com.atlassian.jira.webtests.ztests.project;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;

@WebTest ({ Category.FUNC_TEST, Category.PROJECTS })
public class TestProjectCategory extends JIRAWebTest
{
    public TestProjectCategory(String name)
    {
        super(name);
    }

    private static final String CATEGORY_NAME = "New Project Category For Testing";
    private static final String CATEGORY_DESCRIPTION = "Testing for project category";


    public void testProjectCategory()
    {
        if (projectExists(PROJECT_HOMOSAP))
        {
            log ("Project: " + PROJECT_HOMOSAP + " exists");
        }
        else
        {
            addProject(PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, ADMIN_USERNAME);
        }

        if (projectCategoryExists(CATEGORY_NAME))
        {
            placeProjectInCategory(PROJECT_HOMOSAP, "None");
            deleteProjectCategory(CATEGORY_NAME);
        }

        projectCategoryAddProjectCategory();
        projectCategoryDeleteProjectCategory();
        projectCategoryPlaceProjectInProjectCategory();
        projectCategoryAddDuplicateCategory();
        projectCategoryAddInvalidCategory();

        placeProjectInCategory(PROJECT_HOMOSAP, "None");
        deleteProjectCategory(CATEGORY_NAME);
    }

    /* -------- Project Category helper methods -------- */
    public void projectCategoryAddProjectCategory()
    {
        log("Project Category: Add project category");
        createProjectCategory(CATEGORY_NAME, CATEGORY_DESCRIPTION);
        assertTextPresent(CATEGORY_NAME);
    }

    public void projectCategoryDeleteProjectCategory()
    {
        log("Project Category: Delete project category");
        deleteProjectCategory(CATEGORY_NAME);
        assertTextNotPresent(CATEGORY_NAME);

        createProjectCategory(CATEGORY_NAME, CATEGORY_DESCRIPTION);
    }

    public void projectCategoryPlaceProjectInProjectCategory()
    {
        log("Project Category: Place a project in a project category");
        placeProjectInCategory(PROJECT_HOMOSAP, CATEGORY_NAME);
        gotoPage("/plugins/servlet/project-config/HSP/summary");
        assertTextInElement("project-config-details-project-category", CATEGORY_NAME);
    }

    public void projectCategoryAddDuplicateCategory()
    {
        log("Project Category: Attempt to create a project category with a duplicate name");
        createProjectCategory(CATEGORY_NAME, CATEGORY_DESCRIPTION);
        assertions.getJiraFormAssertions().assertFieldErrMsg("The project category '" + CATEGORY_NAME + "' already exists");
    }

    public void projectCategoryAddInvalidCategory()
    {
        log("Project Category: Attempt to create a project category with an invalid name");
        createProjectCategory("", "");
        assertions.getJiraFormAssertions().assertFieldErrMsg("Please specify a name");
    }

}
