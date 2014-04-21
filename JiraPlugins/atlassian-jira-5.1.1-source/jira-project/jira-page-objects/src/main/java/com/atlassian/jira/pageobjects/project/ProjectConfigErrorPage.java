package com.atlassian.jira.pageobjects.project;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import org.openqa.selenium.By;

import java.util.ArrayList;
import java.util.List;

/**
 * @since v4.4
 */
public class ProjectConfigErrorPage extends AbstractJiraPage
{
    private final String url;

    @ElementBy(id = "project-config-error")
    private PageElement rootElement;

    @ElementBy(id = "project-config-view-all")
    private PageElement viewAllLink;
    
    public ProjectConfigErrorPage(String suffix)
    {
        this.url = String.format("/plugins/servlet/project-config/%s", suffix);
    }

    public boolean hasErrors()
    {
        return !getMessages().isEmpty();
    }

    public List<String> getMessages()
    {
        List<PageElement> elements = rootElement.findAll(By.cssSelector("li"));
        List<String> errorMessages = new ArrayList<String>(elements.size());
        for (PageElement element : elements)
        {
            errorMessages.add(element.getText());
        }
        return errorMessages;
    }

    public ViewProjectsPage clickViewProjects()
    {
        viewAllLink.click();
        return pageBinder.bind(ViewProjectsPage.class);
    }

    @Override
    public TimedCondition isAt()
    {
        return rootElement.timed().isPresent();
    }

    @Override
    public String getUrl()
    {
        return url;
    }
}
