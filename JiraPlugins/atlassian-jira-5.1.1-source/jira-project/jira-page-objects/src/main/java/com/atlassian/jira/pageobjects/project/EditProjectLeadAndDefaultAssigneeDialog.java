package com.atlassian.jira.pageobjects.project;

import com.atlassian.jira.pageobjects.components.fields.SingleSelect;
import com.atlassian.jira.pageobjects.dialogs.FormDialog;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.Options;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.SelectElement;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;
import org.openqa.selenium.By;

/**
 * Edit Project Lead and Default assignee dialog as launched from the people panel of the project config summary page.
 *
 * @since v4.4
 */
public class EditProjectLeadAndDefaultAssigneeDialog extends FormDialog
{
    @ElementBy(name = "assigneeType", timeoutType = TimeoutType.DIALOG_LOAD)
    private SelectElement assigneeType;

    @ElementBy(id = "project-edit-lead-and-default-assignee-submit", timeoutType = TimeoutType.DIALOG_LOAD)
    private PageElement submit;

    @ElementBy(id = "project-edit-lead-and-default-assignee-cancel", timeoutType = TimeoutType.DIALOG_LOAD)
    private PageElement cancel;

    private SingleSelect leadSelect;

    public EditProjectLeadAndDefaultAssigneeDialog()
    {
        super("project-config-project-edit-lead-and-default-assignee-dialog");
    }

    @Init
    public void init()
    {
        leadSelect = binder.bind(SingleSelect.class, find(By.id("lead-container")));
    }

    public EditProjectLeadAndDefaultAssigneeDialog setProjectLead(final String lead)
    {
        leadSelect.select(lead);
        return this;
    }

    public EditProjectLeadAndDefaultAssigneeDialog setDefaultAssignee(final String assignee)
    {
        assigneeType.select(Options.text(assignee));
        return this;
    }

    public SingleSelect getLeadSelect()
    {
        return leadSelect;
    }

    public boolean submitUpdate()
    {
        return !submit(submit);
    }

    public boolean isLeadpickerDisabled()
    {
        return leadSelect.isAutocompleteDisabled();
    }

    public void cancel()
    {
        cancel.click();
        assertDialogClosed();
    }
}
