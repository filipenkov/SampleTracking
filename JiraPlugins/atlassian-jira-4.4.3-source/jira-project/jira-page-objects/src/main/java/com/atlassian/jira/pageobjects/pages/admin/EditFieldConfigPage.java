package com.atlassian.jira.pageobjects.pages.admin;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.jira.pageobjects.project.ProjectSharedBy;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;

/**
 * Represents the Edit Field Configuration page
 *
 * @since v4.4
 */
public class EditFieldConfigPage extends AbstractJiraPage
{
    private String URI = "/secure/project/ConfigureFieldLayout!default.jspa?id=%s";
    private Long fieldConfigId;

    @ElementBy (className = "shared-by")
    private PageElement sharedBy;

    @ElementBy (id = "field-layout-name")
    private PageElement name;

    public EditFieldConfigPage(final long fieldConfigId)
    {
        this.fieldConfigId = fieldConfigId;
    }

    public String getName()
    {
        return name.getText();
    }

    @Override
    public String getUrl()
    {
        return String.format(URI, fieldConfigId);
    }

    @Override
    public TimedCondition isAt()
    {
        return name.timed().isPresent();
    }

    public ProjectSharedBy getSharedBy()
    {
        return pageBinder.bind(ProjectSharedBy.class, sharedBy);
    }
}
