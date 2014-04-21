package com.atlassian.jira.pageobjects.navigator;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.MultiSelectElement;
import com.atlassian.pageobjects.elements.Options;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;

import javax.annotation.Nullable;

/**
 * Author: Geoffrey Wong
 * Page for Basic Mode Issue Navigator page (whilst KickAss Navigator still in development)
 */
public class BasicSearch extends AbstractJiraPage
{

    @Nullable
    protected Long filterId;
    
    @ElementBy (id = "issue-filter-submit")
    protected PageElement search;

    @ElementBy (id = "searcher-pid")
    protected MultiSelectElement projectSelect;
    
    @ElementBy (id = "searcher-type")
    protected MultiSelectElement issueTypeSelect;
    
    @ElementBy (id = "searcher-status")
    protected MultiSelectElement issueStatusSelect;
    
    public String getUrl()
    {
        if (filterId != null)
        {
            return "/secure/IssueNavigator.jspa?navType=simple&mode=show&requestId=" + filterId;
        } else
        {
            return "/secure/IssueNavigator!switchView.jspa?navType=simple&mode=show&createNew=true";
        }
    }

    public BasicSearch()
    {
        // empty
    }
    
    public BasicSearch(Long filterId)
    {
        this.filterId = filterId;
    }
    
    @Override
    public TimedCondition isAt()
    {
        return search.timed().isPresent();
    }
    
    public BasicSearch selectProject(String project)
    {
        projectSelect.select(Options.text(project));
        return this;
    }
    
    public BasicSearch selectIssueType(String issueType)
    {
        issueTypeSelect.select(Options.text(issueType));
        return this;
    }
    
    public BasicSearch selectIssueStatus(String status)
    {
        issueStatusSelect.select(Options.text(status));
        return this;
    }

    public BasicSearch search()
    {
        search.click();
        return this;
    }

    public IssueNavigatorResults getResults()
    {
        return pageBinder.bind(IssueNavigatorResults.class);
    }
}
