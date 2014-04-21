package com.atlassian.jira.pageobjects.dialogs.quickedit;

import com.atlassian.jira.pageobjects.dialogs.FormDialog;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.Options;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.SelectElement;
import com.atlassian.pageobjects.elements.query.Poller;
import com.google.inject.Inject;
import org.openqa.selenium.By;

/**
 * Author: Geoffrey Wong
 * Dialog which displays when triggering a workflow transition for a JIRA issue
 */
public class WorkflowTransitionDialog extends FormDialog
{
    
    @Inject
    PageElementFinder pageElementFinder;
    
    @ElementBy (id = "issue-workflow-transition-submit")
    PageElement submitWorkflowTransitionButton;
    
    @Init
    public void init()
    {
        Poller.waitUntilTrue(submitWorkflowTransitionButton.timed().isPresent());
    }
    
    public WorkflowTransitionDialog(String transitionId)
    {
        super("workflow-transition-" + transitionId + "-dialog");
    }

    public boolean submitWorkflowTransition()
    {
        return submit(submitWorkflowTransitionButton);
    }
    
    public String getWorkflowTransitionButtonUiName()
    {
        return submitWorkflowTransitionButton.getValue();
    }
    
    
    public WorkflowTransitionDialog setResolution(String resolution)
    {
        SelectElement resolutionSelectList = pageElementFinder.find(By.id("resolution"), SelectElement.class);
        resolutionSelectList.select(Options.text(resolution));
        return this;
    }
}
