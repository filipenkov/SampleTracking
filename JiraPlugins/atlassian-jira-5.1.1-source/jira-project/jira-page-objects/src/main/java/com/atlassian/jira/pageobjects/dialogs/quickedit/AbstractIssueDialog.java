package com.atlassian.jira.pageobjects.dialogs.quickedit;

import com.atlassian.jira.pageobjects.dialogs.FormDialog;
import com.atlassian.pageobjects.elements.PageElement;
import org.openqa.selenium.By;

import java.util.ArrayList;
import java.util.List;

/**
 * @since v4.4
 */
abstract public class AbstractIssueDialog extends FormDialog
{

    protected AbstractIssueDialog(String id)
    {
        super(id);
    }

    protected FieldPicker openFieldPicker()
    {
        final PageElement trigger = find(By.id("qf-field-picker-trigger"));
        if (!trigger.hasClass("active"))
        {
            trigger.click();
        }
        return binder.bind(FieldPicker.class);
    }

    public List<String> getVisibleFields()
    {
        final List<PageElement> labels = getDialogElement().findAll(By.cssSelector(".content .field-group label"));
        List<String> visibleFields = new ArrayList<String>();

        for (PageElement label : labels)
        {
            if (label.isVisible())
            {
                visibleFields.add(label.getAttribute("for"));
            }
        }

        return visibleFields;
    }

    public abstract AbstractIssueDialog switchToCustomMode();

    public abstract AbstractIssueDialog switchToFullMode();

    public abstract AbstractIssueDialog removeFields(String... fields);

    public abstract AbstractIssueDialog addFields(String... fields);

    public abstract AbstractIssueDialog fill(final String id, final String value);

    public String getFieldValue(final String id)
    {
        return form.find(By.id(id)).getValue();
    }
}
