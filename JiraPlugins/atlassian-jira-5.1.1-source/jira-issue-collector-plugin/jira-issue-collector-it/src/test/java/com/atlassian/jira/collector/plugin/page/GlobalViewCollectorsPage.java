package com.atlassian.jira.collector.plugin.page;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import org.openqa.selenium.By;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

public class GlobalViewCollectorsPage extends AbstractJiraPage
{
    public static final String URI = "/secure/admin/ViewGlobalCollectors!default.jspa";

    @Inject
    private PageElementFinder elementFinder;

    @ElementBy (className = "issue-collector-admin")
    private PageElement adminArea;

    @ElementBy (id = "admin-page-heading")
    private PageElement pageHeading;

    @Override
    public TimedCondition isAt()
    {
        return pageHeading.timed().isPresent();
    }

    public String getUrl()
    {
        return URI;
    }

    public List<String> getProjectNames()
    {
        List<PageElement> headers = adminArea.findAll(By.className("mod-header"));
        final List<String> ret = new ArrayList<String>();
        for (PageElement header : headers)
        {
            ret.add(header.getText());
        }

        return ret;
    }

    public List<String> getCollectorNames()
    {
        final List<PageElement> collectorNames = adminArea.findAll(By.className("collector-lnk"));
        final List<String> ret = new ArrayList<String>();
        for (PageElement header : collectorNames)
        {
            ret.add(header.getText());
        }

        return ret;
    }
}
