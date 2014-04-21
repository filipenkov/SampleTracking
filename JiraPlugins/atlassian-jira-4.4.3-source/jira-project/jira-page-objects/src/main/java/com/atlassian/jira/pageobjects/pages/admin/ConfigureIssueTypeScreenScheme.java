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
public class ConfigureIssueTypeScreenScheme extends AbstractJiraPage
{
    @ElementBy (id = "issue-type-table")
    private PageElement issueTypeTable;

    @ElementBy (id = "id")
    private PageElement schemeIdElement;

    @ElementBy (id = "issue-type-screen-scheme-name")
    private PageElement name;

    @ElementBy (className = "shared-by")
    private PageElement sharedBy;

    private final long schemeId;

    public ConfigureIssueTypeScreenScheme(long schemeId)
    {
        this.schemeId = schemeId;
    }

    @Override
    public String getUrl()
    {
        return "/secure/project/ConfigureIssueTypeScreenScheme!default.jspa?id=" + schemeId;
    }

    @Override
    public TimedCondition isAt()
    {
        return Conditions.and(issueTypeTable.timed().isPresent(),
                Conditions.forMatcher(schemeIdElement.timed().getValue(), Matchers.equalTo(String.valueOf(schemeId))));
    }

    public String getName()
    {
        return name.getText();
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
