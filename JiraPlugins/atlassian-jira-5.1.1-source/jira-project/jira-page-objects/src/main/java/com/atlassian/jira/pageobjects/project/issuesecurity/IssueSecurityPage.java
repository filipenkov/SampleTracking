package com.atlassian.jira.pageobjects.project.issuesecurity;

import com.atlassian.jira.pageobjects.components.DropDown;
import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.jira.pageobjects.pages.admin.EditIssueSecurityScheme;
import com.atlassian.jira.pageobjects.pages.admin.SelectIssueSecurityScheme;
import com.atlassian.jira.pageobjects.project.ProjectSharedBy;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;

import java.util.ArrayList;
import java.util.List;

/**
 * @since v4.4
 */
public class IssueSecurityPage extends AbstractJiraPage
{
    private static final String SCHEME_NAME_ID = "project-config-issuesecurity-scheme-name";
    private static final String EDIT_LINK_ID = "project-config-issuesecurity-scheme-edit";
    private static final String CHANGE_LINK_ID = "project-config-issuesecurity-scheme-change";

    @ElementBy (id = "project-config-panel-header")
    private PageElement header;

    @ElementBy(id = "project-config-issuesecurities-table")
    private PageElement table;

    @ElementBy(id = "project-config-issuesecurities-none")
    private PageElement noSecurityLevelsMessage;

    @ElementBy (className = "shared-by")
    private PageElement sharedBy;

    private final String projectKey;

    private PageElement schemeName;
    private PageElement schemeEditLink;
    private PageElement schemeChangeLink;
    private DropDown dropDown;

    public IssueSecurityPage(final String projectKey)
    {
        this.projectKey = projectKey;
    }

    @Init
    public void initialise()
    {
        dropDown = pageBinder.bind(DropDown.class, By.id("project-config-tab-actions"), By.id("project-config-tab-actions-list"));
        schemeName = elementFinder.find(By.id(SCHEME_NAME_ID));
        schemeEditLink = elementFinder.find(By.id(EDIT_LINK_ID));
        schemeChangeLink = elementFinder.find(By.id(CHANGE_LINK_ID));
    }

    @Override
    public String getUrl()
    {
        return "/plugins/servlet/project-config/" + projectKey + "/issuesecurity";
    }

    @Override
    public TimedCondition isAt()
    {
        return table.timed().isPresent();
    }

    public List<IssueSecurity> getIssueSecurities()
    {
        final List<IssueSecurity> levels = new ArrayList<IssueSecurity>();

        List<PageElement> rows = table.findAll(By.cssSelector(".project-config-issuesecurity"));
        for (PageElement row : rows)
        {
            IssueSecurity securityLevel = new IssueSecurity();
            List<PageElement> tds = row.findAll(By.cssSelector("td"));

            boolean hasSecurityLevel = false;
            for (PageElement td : tds)
            {
                if (td.hasClass("project-config-issuesecurity-name"))
                {
                    securityLevel.setName(StringUtils.stripToNull(td.getText()));
                    hasSecurityLevel = true;
                }
                else if (td.hasClass("project-config-issuesecurity-description"))
                {
                    securityLevel.setDescription(StringUtils.stripToNull(td.getText()));
                }
                else if (td.hasClass("project-config-issuesecurity-entitylist"))
                {
                    List<String> entities = new ArrayList<String>();
                    List<PageElement> lis = td.findAll(By.cssSelector("li"));
                    for (PageElement li : lis)
                    {
                        entities.add(li.getText());
                    }
                    securityLevel.setEntities(entities);
                }
            }
            if (hasSecurityLevel)
            {
                levels.add(securityLevel);
            }
        }
        return levels;
    }

    public PageElement getTable()
    {
        return table;
    }

    public PageElement getNoSecurityLevelsMessage()
    {
        return noSecurityLevelsMessage;
    }

    public PageElement getSchemeName()
    {
        return schemeName;
    }

    public ProjectSharedBy getSharedBy()
    {
        return pageBinder.bind(ProjectSharedBy.class, sharedBy);
    }

    public boolean isSchemeLinked()
    {
        return dropDown.hasItemById(EDIT_LINK_ID);
    }

    public boolean isSchemeChangeAvailable()
    {
        return dropDown.hasItemById(CHANGE_LINK_ID);
    }

    public EditIssueSecurityScheme gotoScheme()
    {
        final String schemeId = schemeEditLink.getAttribute("data-id");
        return dropDown.openAndClick(By.id(EDIT_LINK_ID), EditIssueSecurityScheme.class, Long.valueOf(schemeId));
    }

    public SelectIssueSecurityScheme gotoSelectScheme()
    {
        final String projectId = schemeChangeLink.getAttribute("data-id");
        return dropDown.openAndClick(By.id(CHANGE_LINK_ID), SelectIssueSecurityScheme.class, Long.valueOf(projectId));
    }

}
