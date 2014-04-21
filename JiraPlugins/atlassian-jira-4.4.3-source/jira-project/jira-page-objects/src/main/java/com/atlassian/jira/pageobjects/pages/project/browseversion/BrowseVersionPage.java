package com.atlassian.jira.pageobjects.pages.project.browseversion;

import com.atlassian.jira.pageobjects.pages.AbstractJiraTabPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import org.hamcrest.Matchers;

import static com.atlassian.pageobjects.elements.query.Conditions.and;

public class BrowseVersionPage extends AbstractJiraTabPage<BrowseVersionTab>
{
    private static final String URI = "/browse/%s/fixforversion/%s";



    private final String projectKey;
    private final String versionId;

    @ElementBy(className = "version-navigation")
    private PageElement versionNavigationWrapper;

    @ElementBy(id = "project-key")
    private PageElement projectKeyHidden;

    @ElementBy(id = "version-id")
    private PageElement versionIdHidden;

    public BrowseVersionPage(String projectKey, String versionId)
    {
        this.projectKey = projectKey;
        this.versionId = versionId;
    }


    @Override
    public TimedCondition isAt()
    {
        return and(versionNavigationWrapper.timed().isPresent(),
                Conditions.forMatcher(projectKeyHidden.timed().getValue(), Matchers.equalTo(projectKey)),
                Conditions.forMatcher(versionIdHidden.timed().getValue(), Matchers.equalTo(versionId))
        );
    }

    public String getUrl()
    {
        return String.format(URI, projectKey, versionId);
    }
}
