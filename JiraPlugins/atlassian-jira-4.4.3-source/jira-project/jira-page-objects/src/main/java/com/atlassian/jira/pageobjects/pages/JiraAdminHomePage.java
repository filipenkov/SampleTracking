package com.atlassian.jira.pageobjects.pages;

import com.atlassian.jira.pageobjects.components.JiraHeader;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.page.AdminHomePage;

/**
 * Home administration page, which is synonym for projects page.
 *
 * @since 4.4
 */
public class JiraAdminHomePage extends AbstractJiraAdminPage implements AdminHomePage<JiraHeader>
{
    private final static String URI = "/secure/admin";

    @ElementBy(id = "admin-config")
    private PageElement mainContainer;


    public String getUrl()
    {
        return URI;
    }

    @Override
    public TimedCondition isAt()
    {
        return mainContainer.timed().isPresent();
    }

    @Override
    public String linkId()
    {
        return "admin_summary";
    }
}
