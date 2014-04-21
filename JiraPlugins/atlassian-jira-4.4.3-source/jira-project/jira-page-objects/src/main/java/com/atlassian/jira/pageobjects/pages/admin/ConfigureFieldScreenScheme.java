package com.atlassian.jira.pageobjects.pages.admin;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.jira.pageobjects.project.ProjectSharedBy;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import org.hamcrest.Matchers;

/**
 * @since v4.4
 */
public class ConfigureFieldScreenScheme extends AbstractJiraPage
{
    @ElementBy (id = "screens-table")
    private PageElement screensTable;

    @ElementBy (id = "id")
    private PageElement schemeIdElement;

    @ElementBy (id = "screen-scheme-name")
    private PageElement name;

    @ElementBy (className = "shared-by")
    private PageElement sharedBy;

    private final long schemeId;

    public ConfigureFieldScreenScheme(long schemeId)
    {
        this.schemeId = schemeId;
    }

    @Override
    public String getUrl()
    {
        return "/secure/admin/ConfigureFieldScreenScheme!default.jspa?id=" + schemeId;
    }

    public String getName()
    {
        return name.getText();
    }

    @Override
    public TimedCondition isAt()
    {
        return Conditions.and(screensTable.timed().isPresent(),
                Conditions.forMatcher(schemeIdElement.timed().getValue(), Matchers.equalTo(String.valueOf(schemeId))));
    }

    public long getSchemeId()
    {
        return schemeId;
    }

    public ProjectSharedBy getSharedBy()
    {
        return pageBinder.bind(ProjectSharedBy.class, sharedBy);
    }

}
