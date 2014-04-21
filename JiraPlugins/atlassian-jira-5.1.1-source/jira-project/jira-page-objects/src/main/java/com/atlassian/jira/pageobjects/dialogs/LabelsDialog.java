package com.atlassian.jira.pageobjects.dialogs;

import com.atlassian.jira.pageobjects.components.MultiSelect;
import com.atlassian.pageobjects.PageBinder;
import org.openqa.selenium.By;

import javax.inject.Inject;
import java.util.List;

public class LabelsDialog extends FormDialog
{

    @Inject
    PageBinder pageBinder;

    public LabelsDialog()
    {
        super("edit-labels-dialog");
    }

    public void addLabels(List<String> labels)
    {
        final MultiSelect multiSelect = pageBinder.bind(MultiSelect.class, "labels");

        for (String label : labels)
        {
            multiSelect.add(label);
        }
    }

    public boolean submit()
    {
        return super.submit(By.name("edit-labels-submit"));
    }

    @Override
    public boolean isOpen()
    {
        return getDialogElement().hasClass("aui-dialog-content-ready");
    }
}
