package com.atlassian.jira.pageobjects.project;

import com.atlassian.jira.pageobjects.components.fields.SingleSelect;
import com.atlassian.jira.pageobjects.dialogs.AbstractFormDialog;
import com.atlassian.jira.pageobjects.project.summary.ProjectSummaryPageTab;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import org.openqa.selenium.By;

import static org.junit.Assert.assertTrue;

/**
 * @since v4.4
 */
public class AddProjectDialog extends AbstractFormDialog
{
    private PageElement nameElement;
    private PageElement keyElement;
    private PageElement submit;
    private PageElement leadContainer;
    private SingleSelect leadSelect;

    public AddProjectDialog()
    {
        super("create-project-dialog");
    }

    @Init
    public void init()
    {
        nameElement = find(By.name("name"));
        keyElement = find(By.name("key"));
        submit = find(By.name("Add"));
        leadContainer = find(By.id("lead-picker"));
        leadSelect = binder.bind(SingleSelect.class, leadContainer);
    }

    public AddProjectDialog createProjectFail(String key, String name, String lead)
    {
        setFields(key, name, lead);
        return submitFail();
    }

    public ProjectSummaryPageTab createProjectSuccess(String key, String name, String lead)
    {
        setFields(key, name, lead);
        return submitSuccess();
    }

    public void setFields(String key, String name, String lead)
    {
        setKey(key);
        setName(name);
        if (lead != null)
        {
            setLead(lead);
        }
    }

    public AddProjectDialog setName(String name)
    {
        assertDialogOpen();
        setElement(nameElement, name);
        return this;
    }

    public AddProjectDialog setKey(String key)
    {
        assertDialogOpen();
        setElement(keyElement, key);
        return this;
    }

    public AddProjectDialog setLead(String lead)
    {
        assertDialogOpen();
        assertTrue("The lead element is not present. Only one user in the system?", isLeadPresent());
        leadSelect.select(lead);
        return this;
    }

    public boolean isLeadPresent()
    {
        return leadContainer.isPresent();
    }

    public AddProjectDialog submitFail()
    {
        submit(submit);
        assertDialogOpen();
        return this;
    }

    public ProjectSummaryPageTab submitSuccess()
    {
        submit(submit);
        assertDialogClosed();
        return binder.bind(ProjectSummaryPageTab.class);
    }

    public boolean isLeadpickerDisabled()
    {
        return leadSelect.isAutocompleteDisabled();
    }
}
