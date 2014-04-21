package com.atlassian.jira.pageobjects.pages;


import com.atlassian.jira.pageobjects.components.JiraHeader;
import com.atlassian.jira.pageobjects.gadgets.Gadget;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.page.HomePage;

/**
 * Page object implementation for the Dashbaord page in JIRA.
 * 
 */
public class DashboardPage extends AbstractJiraPage implements HomePage<JiraHeader>
{
    // TODO this should make use of stuff in atlassian-gadgets!

    private static final String URI = "/secure/Dashboard.jspa";

    @ElementBy(className = "dashboard-contents")
    private PageElement container;

    /**
     * TODO: fix this.
     */
    public boolean canAddGadget()
    {
        return false;
    }

    @Override
    public TimedCondition isAt()
    {
        return container.timed().isPresent();
    }

    public Gadget getGadget(String gadgetId)
    {
        return pageBinder.bind(Gadget.class, gadgetId);
    }

    public String getUrl()
    {
        return URI;
    }
}