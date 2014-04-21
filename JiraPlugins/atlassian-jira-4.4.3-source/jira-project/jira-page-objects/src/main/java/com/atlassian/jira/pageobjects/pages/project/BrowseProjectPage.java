package com.atlassian.jira.pageobjects.pages.project;

import com.atlassian.jira.pageobjects.pages.AbstractJiraTabPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;

import static com.atlassian.pageobjects.elements.query.Conditions.and;

/**
 * Browse project page implementation.
 *
 * @since v4.4
 */
public class BrowseProjectPage extends AbstractJiraTabPage<BrowseProjectTab>
{

    private final String projectKey;

    @ElementBy(id="project-tab")
    private PageElement tabContents;

    public BrowseProjectPage(String projectKey)
    {
        this.projectKey = projectKey;
    }

    @Override
    public String getUrl()
    {
        return "/browse/" + projectKey;
    }

    @Override
    public TimedCondition isAt()
    {
        return and(tabContents.timed().isPresent(), tabContents.timed().hasAttribute("data-project-key", projectKey));
    }

    @Override
    protected <T extends BrowseProjectTab> Object[] argsForTab(Class<T> tabClass)
    {
        return new Object[] {projectKey};
    }
}
