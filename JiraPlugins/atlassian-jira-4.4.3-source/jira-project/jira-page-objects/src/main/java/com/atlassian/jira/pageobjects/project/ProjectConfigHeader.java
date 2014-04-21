package com.atlassian.jira.pageobjects.project;

import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.webdriver.AtlassianWebDriver;
import com.atlassian.webdriver.utils.JavaScriptUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import javax.inject.Inject;

/**
 * Represents the project config header.
 *
 * @since v4.4
 */
public class ProjectConfigHeader
{
    @Inject
    private AtlassianWebDriver driver;

    @ElementBy (id = "project-config-description")
    private PageElement description;

    @ElementBy (id = "project-config-details-project-key")
    private PageElement projectKey;

    @ElementBy(id = "project-config-details-project-url")
    private PageElement projectUrl;

    @ElementBy(id = "project-config-details-project-category")
    private PageElement projectCategory;

    @ElementBy(id = "project-config-header-avatar")
    private PageElement projectAvatar;

    @ElementBy(id = "project-config-header-name")
    private PageElement projectName;

    /**
     * The project url as displayed in the project details section. Use {@link #getProjectUrlLink()}
     * to get the href value
     *
     * @return
     */
    public String getProjectUrl()
    {
        return projectUrl.getText();
    }

    /**
     * If the project url is specified, returns the href value of the project url link.
     * Otherwise, returns null
     *
     * @return the href value of the project url link
     */
    public String getProjectUrlLink()
    {
        return projectUrl.getAttribute("href");
    }

    /**
     * The project key as displayed in the project details section
     *
     * @return
     */
    public String getProjectKey()
    {
        return projectKey.getText();
    }

    /**
     * The project id.
     *
     * @return
     */
    public long projectId()
    {
        return Long.parseLong(projectKey.getAttribute("data-pid"));
    }

    /**
     * The project category as displayed in the project details section. Use
     * {@link #getProjectCategoryLink()} to get the link for the category
     *
     * @return
     */
    public String getProjectCategory()
    {
        return projectCategory.getText();
    }

    /**
     * The href value for the link surrounding project category. Used
     * to change the category of the project
     *
     * @return
     */
    public String getProjectCategoryLink()
    {
        return projectCategory.getAttribute("href");
    }

        /**
     * The src attribute of the main project avatar, present on every project configuration page
     * @return
     */
    public String getProjectAvatarIconSrc()
    {
        return projectAvatar.getAttribute("src");
    }

      /**
     * The src attribute of the main project avatar, present on every project configuration page
     * @return
     */
    public String getProjectAvatarIconId()
    {
        return projectAvatar.getAttribute("data-avatar-id");
    }

    /**
     * The project name, present on every project configuration page
     * @return
     */
    public String getProjectName()
    {
        return projectName.getText();
    }

    /**
     * The innerHtml of the project description block. It is possible and valid for the
     * project description to contain HTML
     *
     * @return the html of the description
     */
    public String getDescriptionHtml()
    {
        if (description.isPresent())
        {
            WebElement element = driver.getDriver().findElement(By.id("project-config-description"));
            return JavaScriptUtils.innerHtml(element, driver.getDriver());
        }
        else
        {
            return null;
        }
    }

    /**
     * The text of the project description block.
     *
     * @return the project's description.
     */
    public String getDescription()
    {
        if (description.isPresent())
        {
            return description.getText();
        }
        else
        {
            return null;
        }
    }
}
