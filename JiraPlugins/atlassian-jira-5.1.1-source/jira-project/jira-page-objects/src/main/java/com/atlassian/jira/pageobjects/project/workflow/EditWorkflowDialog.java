package com.atlassian.jira.pageobjects.project.workflow;

import com.atlassian.jira.pageobjects.pages.admin.workflow.ViewWorkflowSteps;
import com.atlassian.jira.pageobjects.pages.admin.workflow.WorkflowDesignerPage;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import org.openqa.selenium.By;

import javax.inject.Inject;

/**
* @since v5.1
*/
public class EditWorkflowDialog
{
    @Inject
    private PageBinder binder;

    @Inject
    private PageElementFinder finder;

    private PageElement workflowMigrateDialog;
    private PageElement buttons;
    private PageElement progressBar;

    @Init
    public void initialise()
    {
        workflowMigrateDialog = finder.find(By.id("wait-migrate-dialog"));
        buttons = workflowMigrateDialog.find(By.className("buttons"));
        progressBar = workflowMigrateDialog.find(By.id("progress-bar-container"));
    }

    public <T> T clickContinueAndBind(Class<T> bindingClass, Object...args)
    {
        workflowMigrateDialog.find(By.cssSelector("input[type=submit]")).click();
        return binder.bind(bindingClass, args);
    }

    public WorkflowDesignerPage gotoEditWorkflowDigram(String workflowName)
    {
        return clickContinueAndBind(WorkflowDesignerPage.class, workflowName, true);
    }

    public ViewWorkflowSteps gotoEditWorkflowText(String workflowName)
    {
        return clickContinueAndBind(ViewWorkflowSteps.class, workflowName, true);
    }

    public boolean isButtonsVisible()
    {
        return buttons.isVisible();
    }

    public boolean isProgressBarPresent()
    {
        return progressBar.isPresent();
    }
}
