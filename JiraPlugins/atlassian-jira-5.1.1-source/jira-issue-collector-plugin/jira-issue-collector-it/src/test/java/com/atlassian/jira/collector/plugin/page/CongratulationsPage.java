package com.atlassian.jira.collector.plugin.page;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.TimedCondition;

import javax.inject.Inject;

public class CongratulationsPage extends AbstractJiraPage
{
    public static final String URI = "/secure/InsertCollectorHelp!default.jspa?projectKey=";

    @Inject
    private PageElementFinder elementFinder;

    @ElementBy (id = "script-source-html")
    private PageElement scriptSource;

    @ElementBy (id = "script-source-script")
    private PageElement scriptSourceJavascript;

    @ElementBy (id = "collector-id")
    private PageElement collectorId;

    private final String projectKey;

    public CongratulationsPage(final String projectKey)
    {
        this.projectKey = projectKey;
    }

    @Override
    public TimedCondition isAt()
    {
        return scriptSource.timed().isPresent();
    }

    public String getScriptSource()
    {
        return scriptSource.getValue();
    }

    public String getScriptSourceJavascript()
    {
        return scriptSourceJavascript.getValue();
    }
    
    public String getCollectorId()
    {
        return collectorId.getValue();
    }

    public String getUrl()
    {
        return URI + projectKey;
    }
}
