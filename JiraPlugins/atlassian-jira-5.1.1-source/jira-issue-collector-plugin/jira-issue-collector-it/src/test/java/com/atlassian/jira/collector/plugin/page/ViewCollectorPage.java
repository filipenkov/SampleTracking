package com.atlassian.jira.collector.plugin.page;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;

public class ViewCollectorPage extends AbstractJiraPage
{
    public static final String URI = "/secure/ViewCollector!default.jspa?projectKey=%s&collectorId=%s";
    private final String projectKey;
    private final ViewCollectorsPage.Collector collector;

    @ElementBy (id = "script-source-html")
    private PageElement scriptSource;

    @ElementBy (id = "collector-id")
    private PageElement collectorId;


    public ViewCollectorPage(final String projectKey, final ViewCollectorsPage.Collector collector)
    {
        this.projectKey = projectKey;

        this.collector = collector;
    }

    public String getScriptSource()
    {
        return scriptSource.getValue();
    }
    
    public String getCollectorId()
    {
        return collectorId.getValue();
    }
    
    @Override
    public TimedCondition isAt()
    {
        return collectorId.timed().isPresent();
    }

    @Override
    public String getUrl()
    {
        return String.format(URI, projectKey, collector.getId());
    }
}
