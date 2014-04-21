package com.atlassian.jira.pageobjects.project.components;

import com.atlassian.jira.pageobjects.components.fields.SingleSelect;
import com.atlassian.jira.pageobjects.components.restfultable.AbstractEditRow;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.PageElementFinder;
import org.openqa.selenium.By;

import javax.inject.Inject;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilFalse;

/**
 * @since v4.4
 */
public class EditComponentForm extends AbstractEditRow
{
    final String COMPONENT_NAME = ".project-config-component-name";
    final String COMPONENT_DESC = ".project-config-component-description";
    final String COMPONENT_LEAD = ".project-config-component-lead";
    final String COMPONENT_ASSIGNEE = ".project-config-component-assignee";

    @Inject
    private PageBinder pageBinder;

    @Inject
    private PageElementFinder elementFinder;

    public EditComponentForm(By rowSelector)
    {
        super(rowSelector);
    }

    public EditComponentForm fill(final String name, final String description, final String componentLead, final String defaultAssignee)
    {
        if (name != null)
        {
            getNameField().clear().type(name);
        }

        if (description != null)
        {
            getDescriptionField().clear().type(description);
        }

        if (componentLead != null)
        {

            if (isLeadpickerDisabled())
            {
                getDisabledComponentLeadField().clear().type(componentLead);
            }
            else
            {
                getComponentLeadField().clear().select(componentLead);
            }
        }

        if (defaultAssignee != null)
        {
            getDefaultAssigneeField().selectByValue(defaultAssignee);
        }

        return this;
    }

    public EditComponentForm submit()
    {
        getAddButton().click();
        waitUntilFalse(row.timed().hasClass("loading"));
        return this;
    }

    public boolean isLeadpickerDisabled()
    {
        return findInRow(COMPONENT_LEAD).find(By.tagName("input")).hasClass("aui-ss-disabled");
    }

    public AbstractEditRow.Field getNameField()
    {
        return pageBinder.bind(AbstractEditRow.Field.class, findInRow(COMPONENT_NAME));
    }

    public AbstractEditRow.Field getDescriptionField()
    {
        return pageBinder.bind(AbstractEditRow.Field.class, findInRow(COMPONENT_DESC));
    }

    public AbstractEditRow.Field getDisabledComponentLeadField()
    {
        return pageBinder.bind(AbstractEditRow.Field.class, findInRow(COMPONENT_LEAD));
    }

    public SingleSelect getComponentLeadField()
    {
        return pageBinder.bind(SingleSelect.class, findInRow(COMPONENT_LEAD));
    }

    public AbstractEditRow.SelectField getDefaultAssigneeField()
    {
        return pageBinder.bind(AbstractEditRow.SelectField.class, findInRow(COMPONENT_ASSIGNEE));
    }
}
