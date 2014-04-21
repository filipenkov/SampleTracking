package com.atlassian.jira.pageobjects.project.summary.components;

import com.atlassian.jira.pageobjects.global.User;
import com.atlassian.jira.pageobjects.project.summary.AbstractSummaryPanel;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.google.common.collect.Lists;
import org.openqa.selenium.By;

import java.util.List;

/**
 * Represents the "Components" panel on the summary page of the project configuration.
 *
 * @since v4.4
 */
public class ComponentsSummaryPanel extends AbstractSummaryPanel
{
    @ElementBy (id = "project-config-webpanel-summary-components")
    private PageElement componentsSummaryPanel;

    public List<ProjectComponent> components()
    {
        final List<ProjectComponent> components = Lists.newArrayList();
        final List<PageElement> componentElements = componentsSummaryPanel.findAll(By.cssSelector(".project-config-list > li"));

        for (final PageElement componentElement : componentElements)
        {
            final String componentName = componentElement.find(By.cssSelector(".project-config-list-label")).getText();
            final PageElement componentLeadElement = componentElement.find(By.cssSelector(".project-config-list-value a"));
            final User user;
            if (componentLeadElement.isPresent())
            {
                final String componentLeadFullName = componentLeadElement.getText();
                final String componentLead = componentLeadElement.getAttribute("rel");
                user = new User(componentLead, componentLeadFullName);
            }
            else
            {
                user = null;
            }

            components.add(new ProjectComponent(componentName, user));
        }
        return components;
    }

    /*
     * The summary page's paragraph describing that the project has no compnents yet. This is not for general consumption.
     */
    public String getNoComponentsText()
    {
        final PageElement element = componentsSummaryPanel.find(By.cssSelector(".project-config-list-empty span"));
        return element.isPresent() ? element.getText() : null;
    }

    /*
     * The summary page's link text when the project has no compnents yet. This is not for general consumption.
     */
    public String getNoComponentsLinkText()
    {
        final PageElement link = componentsSummaryPanel.find(getNoLinkLocator());
        return link.isPresent() ? link.getText() : null;
    }

    /*
     * The summary page's link href when the project has no compnents yet. This is not for general consumption.
     */
    public String getNoComponentsLinkUrl()
    {
        final PageElement link = componentsSummaryPanel.find(getNoLinkLocator());
        return link.isPresent() ? link.getAttribute("href") : null;
    }

    /*
     * The summary page's text when the project has more than the number of compnents we display in the panel.
     * This is not for general consumption.
     */
    public String getSomeComponentText()
    {
        final PageElement element = componentsSummaryPanel.find(By.cssSelector(".project-config-list-note span"));
        return element.isPresent() ? element.getText() : null;
    }

    /*
     * The summary page's link text when the project has more than the number of compnents we display in the panel.
     * This is not for general consumption.
     */
    public String getSomeComponentLinkText()
    {
        final PageElement element = componentsSummaryPanel.find(getSomeLinkLocator());
        return element.isPresent() ? element.getText() : null;
    }

    /*
     * The summary page's link href when the project has more than the number of compnents we display in the panel.
     * This is not for general consumption.
     */
    public String getSomeComponentLinkUrl()
    {
        final PageElement element = componentsSummaryPanel.find(getSomeLinkLocator());
        return element.isPresent() ? element.getAttribute("href") : null;
    }

    private By getNoLinkLocator()
    {
        return By.cssSelector(".project-config-list-empty a");
    }

    private By getSomeLinkLocator()
    {
        return By.cssSelector(".project-config-list-note a");
    }
}
