package com.atlassian.jira.pageobjects.pages.admin;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.jira.pageobjects.project.ProjectSharedBy;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.TimedCondition;

import javax.inject.Inject;

/**
 * Represents the Edit Field Configuration Scheme page.
 *
 * @since v4.4
 */
public class EditFieldSchemePage extends AbstractJiraPage
{
    private String URI = "/secure/admin/ConfigureFieldLayoutScheme.jspa?id=%s";
    private Long schemeId;

    @Inject
    private PageElementFinder elementFinder;

    @ElementBy(className = "jiraformbody")
    private PageElement formBody;

    @ElementBy(id = "field-scheme-name")
    private PageElement schemeName;

    @ElementBy (className = "shared-by")
    private PageElement sharedBy;

    public EditFieldSchemePage(final long schemeId)
    {
        this.schemeId = schemeId;
    }

    public String getName()
    {
        return schemeName.getText();
    }

    @Override
    public TimedCondition isAt()
    {
        return formBody.timed().isPresent();
    }

    @Override
    public String getUrl()
    {
        return String.format(URI, schemeId);
    }

    public ProjectSharedBy getSharedBy()
    {
        return pageBinder.bind(ProjectSharedBy.class, sharedBy);
    }
}
