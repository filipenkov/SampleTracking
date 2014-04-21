package com.atlassian.jira.pageobjects.project.screens;

import com.atlassian.jira.pageobjects.components.DropDown;
import com.atlassian.jira.pageobjects.pages.admin.SelectIssueTypeScreenScheme;
import com.atlassian.jira.pageobjects.pages.admin.issuetype.screen.schemes.configure.ConfigureIssueTypeScreenSchemePage;
import com.atlassian.jira.pageobjects.pages.admin.screen.ConfigureScreenScheme;
import com.atlassian.jira.pageobjects.project.AbstractProjectConfigPageTab;
import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import org.openqa.selenium.By;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Page object for the screens tab.
 *
 * @since v4.4
 */
public class ScreensPageTab extends AbstractProjectConfigPageTab
{
    private final String projectKey;
    private static final String SCHEME_NAME_ID = "project-config-screens-scheme-name";
    private static final String EDIT_LINK_ID = "project-config-screens-scheme-edit";
    private static final String CHANGE_LINK_ID = "project-config-screens-scheme-change";

    @ElementBy (id = "project-config-panel-screens")
    private PageElement container;

    private PageElement schemeName;
    private PageElement schemeEditLink;
    private PageElement schemeChangeLink;
    private DropDown dropDown;

    @Inject
    private PageElementFinder elementFinder;

    @Init
    public void initialise()
    {
        dropDown = pageBinder.bind(DropDown.class, By.id("project-config-tab-actions"), By.id("project-config-tab-actions-list"));
        schemeName = elementFinder.find(By.id(SCHEME_NAME_ID));
        schemeEditLink = elementFinder.find(By.id(EDIT_LINK_ID));
        schemeChangeLink = elementFinder.find(By.id(CHANGE_LINK_ID));
    }

    public ScreensPageTab(String projectKey)
    {
        this.projectKey = projectKey;
    }

    public boolean isSchemeLinked()
    {
        return dropDown.hasItemById(EDIT_LINK_ID);
    }

    public boolean isSchemeChangeAvailable()
    {
        return dropDown.hasItemById(CHANGE_LINK_ID);
    }

    public ConfigureIssueTypeScreenSchemePage gotoScheme()
    {
        final String schemeId = schemeEditLink.getAttribute("data-id");
        return dropDown.openAndClick(By.id(EDIT_LINK_ID), ConfigureIssueTypeScreenSchemePage.class, schemeId);
    }

    /**
     * This methods handles being intercepted and allows overriding the page to the expected page.
     *
     * @param expectedPage the expected page to navigate to.
     * @param args additional arguments to pass to {@link com.atlassian.pageobjects.PageBinder} when binding
     * @return the expected page object
     */
    public <P extends Page> P gotoSelectScheme(Class<P> expectedPage, Object...args)
    {
        return dropDown.openAndClick(By.id(CHANGE_LINK_ID), expectedPage, args);
    }

    public SelectIssueTypeScreenScheme gotoSelectScheme()
    {
        final String projectId = schemeChangeLink.getAttribute("data-id");
        return gotoSelectScheme(SelectIssueTypeScreenScheme.class, Long.valueOf(projectId));
    }

    public String getSchemeName()
    {
        return schemeName.getText();
    }

    @Override
    public String getUrl()
    {
        return String.format("/plugins/servlet/project-config/%s/screens", projectKey);
    }

    @Override
    public TimedCondition isAt()
    {
        return container.timed().isPresent();
    }

    public List<ScreenSchemeInfo> getScreenSchemes()
    {
        List<ScreenSchemeInfo> screenSchemes = new ArrayList<ScreenSchemeInfo>();

        List<PageElement> sections = container.findAll(By.cssSelector("div.project-config-screenScheme"));
        for (PageElement section : sections)
        {
            ScreenSchemeInfo screenSchemeInfo = new ScreenSchemeInfo();
            // Get the name and basic stuff
            PageElement title = section.find(By.cssSelector(".project-config-scheme-item-name"));
            screenSchemeInfo.setName(title.getText());
            PageElement editLink = section.find(By.cssSelector("a.project-config-icon-edit"));
            screenSchemeInfo.setEditLink(editLink);
            PageElement schemeId = section.find(By.cssSelector("input.project-config-screens-field-screen-scheme-id"));
            screenSchemeInfo.setId(schemeId.getValue());
            PageElement isDefault = section.find(By.cssSelector(".status-default"));
            screenSchemeInfo.setDefault(isDefault.isPresent());

            // Other projects
            PageElement sharedByLink = section.find(By.cssSelector("a.shared-item-trigger"));
            if (sharedByLink.isPresent())
            {
                sharedByLink.click();

                // Get the href from the link.  That is the id of the inline dialog, not including the #
                final String href = sharedByLink.getAttribute("href");
                int triggerTargetStart = href.indexOf("#");
                final String triggerTarget = href.substring(triggerTargetStart + 1);

                String id = "inline-dialog-" + triggerTarget;

                PageElement otherProjectDiv = elementFinder.find(By.id(id));
                // This should always be present so get the list items
                List<PageElement> sharedProjectElements = otherProjectDiv.findAll(By.cssSelector("li"));
                List<OtherProjectInfo> projects = new ArrayList<OtherProjectInfo>();
                for (PageElement listItem : sharedProjectElements)
                {
                    PageElement anchor = listItem.find(By.cssSelector("a"));
                    PageElement icon = anchor.find(By.cssSelector("img"));
                    projects.add(new OtherProjectInfo(anchor.getText().trim(), icon.getAttribute("src")));
                }
                screenSchemeInfo.setProjects(projects);
            }
            else
            {
                screenSchemeInfo.setProjects(Collections.<OtherProjectInfo>emptyList());
            }

            // Issue types
            PageElement issueTypeDiv = section.find(By.cssSelector("div.project-config-screens-issuetypes"));
            List<PageElement> issueTypeElements = issueTypeDiv.findAll(By.cssSelector("li"));
            List<IssueTypeInfo> issueTypes = new ArrayList<IssueTypeInfo>();
            for (PageElement listItem : issueTypeElements)
            {
                PageElement name = listItem.find(By.cssSelector(".project-config-issuetype-name"));
                PageElement icon = listItem.find(By.cssSelector("img.project-config-icon-issuetype"));
                issueTypes.add(new IssueTypeInfo(name.getText().trim(), icon.getAttribute("src")));
            }
            screenSchemeInfo.setIssueTypes(issueTypes);

            // Get the screen info project-config-screens-definition
            PageElement screensDiv = section.find(By.cssSelector("div.project-config-screens-definition"));
            final boolean screenSchemeShown = screensDiv.isVisible();
            if(!screenSchemeShown)
            {
                title.click();
                assertTrue("Tried to unhide screen operations but was unsuccessful", screensDiv.isVisible());
            }

            List<PageElement> rows = screensDiv.findAll(By.cssSelector("tr.project-config-screens-screen"));
            List<ScreenInfo> screens = new ArrayList<ScreenInfo>();
            for (PageElement row : rows)
            {
                PageElement operationName = row.find(By.cssSelector("td.project-config-screens-screen-operation"));
                PageElement screenId = row.find(By.cssSelector("td.project-config-screens-screen-name input.project-config-screens-field-screen-id"));
                PageElement link = row.find(By.cssSelector("td.project-config-screens-screen-name a"));
                String screenName = null;
                if (link.isPresent())
                {
                    screenName = link.getText().trim();
                }
                else
                {
                    PageElement nameElement = row.find(By.cssSelector("td.project-config-screens-screen-name"));
                    screenName = nameElement.getText().trim();
                }
                screens.add(new ScreenInfo(operationName.getText().trim(), screenId.getValue(), screenName, link));
            }
            screenSchemeInfo.setScreens(screens);

            screenSchemes.add(screenSchemeInfo);
        }
        return screenSchemes;
    }




    public class ScreenSchemeInfo
    {
        private String id;
        private String name;
        private String projectCount;
        private List<OtherProjectInfo> projects;
        private PageElement editLink;
        private List<IssueTypeInfo> issueTypes;
        private List<ScreenInfo> screens;
        private boolean isDefault;

        public ScreenSchemeInfo()
        {
        }

        public Long getId()
        {
            return Long.parseLong(id);
        }

        public void setId(String id)
        {
            this.id = id;
        }

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public boolean isDefault()
        {
            return isDefault;
        }

        public void setDefault(boolean aDefault)
        {
            isDefault = aDefault;
        }

        public String getProjectCount()
        {
            return projectCount;
        }

        public void setProjectCount(String projectCount)
        {
            this.projectCount = projectCount;
        }

        public List<OtherProjectInfo> getProjects()
        {
            return projects;
        }

        public void setProjects(List<OtherProjectInfo> projects)
        {
            this.projects = projects;
        }

        public boolean isEditLinkPresent()
        {
            return editLink.isPresent();
        }

        public ConfigureScreenScheme gotoEditScheme()
        {
            Long schemeId = getId();
            assertTrue("No link present to change the issue type screen scheme.", editLink.isPresent());

            return pageBinder.navigateToAndBind(ConfigureScreenScheme.class, schemeId);
        }


        public void setEditLink(PageElement editLink)
        {
            this.editLink = editLink;
        }

        public List<IssueTypeInfo> getIssueTypes()
        {
            return issueTypes;
        }

        public void setIssueTypes(List<IssueTypeInfo> issueTypes)
        {
            this.issueTypes = issueTypes;
        }

        public List<ScreenInfo> getScreens()
        {
            return screens;
        }

        public void setScreens(List<ScreenInfo> screens)
        {
            this.screens = screens;
        }
    }

    public static class OtherProjectInfo
    {
        private String name;
        private String iconUrl;

        public OtherProjectInfo(String name, String iconUrl)
        {
            this.name = name;
            this.iconUrl = iconUrl;
        }

        public String getName()
        {
            return name;
        }

        public String getIconUrl()
        {
            return iconUrl;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            OtherProjectInfo that = (OtherProjectInfo) o;

            if (iconUrl != null ? !iconUrl.equals(that.iconUrl) : that.iconUrl != null) { return false; }
            if (name != null ? !name.equals(that.name) : that.name != null) { return false; }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + (iconUrl != null ? iconUrl.hashCode() : 0);
            return result;
        }
    }

    public static class IssueTypeInfo
    {
        private String name;
        private String iconUrl;

        public IssueTypeInfo(String name, String iconUrl)
        {
            this.name = name;
            this.iconUrl = iconUrl;
        }

        public String getName()
        {
            return name;
        }

        public String getIconUrl()
        {
            return iconUrl;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            IssueTypeInfo that = (IssueTypeInfo) o;

            if (iconUrl != null ? !iconUrl.equals(that.iconUrl) : that.iconUrl != null) { return false; }
            if (name != null ? !name.equals(that.name) : that.name != null) { return false; }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + (iconUrl != null ? iconUrl.hashCode() : 0);
            return result;
        }
    }


    /**
     * Represents an item in an issue types screen scheme as shown in the screens tab on the project configuration
     * summary page
     *
     * @since v4.4
     */
    public static class ScreenInfo
    {
        private final String operationName;
        private final String screenId;
        private final String screenName;
        private final PageElement link;

        public ScreenInfo(String operationName, String screenId, String screenName, PageElement link)
        {
            this.operationName = operationName;
            this.screenId = screenId;
            this.screenName = screenName;
            this.link = link;
        }

        public String getOperationName()
        {
            return operationName;
        }

        public Long getScreenId()
        {
            return Long.parseLong(screenId);
        }

        public String getScreenName()
        {
            return screenName;
        }

        public PageElement getLink()
        {
            return link;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            ScreenInfo that = (ScreenInfo) o;

            if (!operationName.equals(that.operationName))
            {
                return false;
            }
            if (!screenName.equals(that.screenName))
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = operationName.hashCode();
            result = 31 * result + screenName.hashCode();
            return result;
        }
    }

}
