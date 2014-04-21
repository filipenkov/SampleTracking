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
public class EditIssueSecurityScheme extends AbstractJiraPage
{
    @ElementBy (id = "issue-security-table")
    private PageElement issueSecurityTable;

    @ElementBy (id = "schemeId")
    private PageElement schemeIdElement;

    @ElementBy (className = "shared-by")
    private PageElement sharedBy;

    private final long schemeId;

    public EditIssueSecurityScheme(long schemeId)
    {
        this.schemeId = schemeId;
    }

    @Override
    public String getUrl()
    {
        return "/secure/admin/EditIssueSecurities!default.jspa?schemeId=" + schemeId;
    }

    @Override
    public TimedCondition isAt()
    {
        return Conditions.and(issueSecurityTable.timed().isPresent(),
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
