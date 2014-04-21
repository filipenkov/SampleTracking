package com.atlassian.jira.pageobjects.project.issuetypes;

import com.atlassian.jira.pageobjects.components.DropDown;
import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.jira.pageobjects.pages.admin.issuetype.ChangeIssueTypeSchemePage;
import com.atlassian.jira.pageobjects.pages.admin.issuetype.EditIssueTypeSchemePage;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the issuetypes page in JIRA.
 *
 * @since v4.4
 */
public class IssueTypesPage extends AbstractJiraPage
{
    private static final String SCHEME_NAME_ID = "project-config-issuetype-scheme-name";
    private static final String EDIT_LINK_ID = "project-config-issuetype-scheme-edit";
    private static final String CHANGE_LINK_ID = "project-config-issuetype-scheme-change";

    @ElementBy(id = "project-config-panel-header")
    private PageElement header;

    @ElementBy(id = "project-config-issuetypes-table")
    private PageElement table;

    private PageElement schemeName;
    private PageElement schemeEditLink;
    private PageElement schemeChangeLink;
    private DropDown dropDown;

    private final String projectKey;

    @Init
    public void initialise()
    {
        dropDown = pageBinder.bind(DropDown.class, By.id("project-config-tab-actions"), By.id("project-config-tab-actions-list"));
        schemeName = elementFinder.find(By.id(SCHEME_NAME_ID));
        schemeEditLink = elementFinder.find(By.id(EDIT_LINK_ID));
        schemeChangeLink = elementFinder.find(By.id(CHANGE_LINK_ID));
    }

    public IssueTypesPage(String projectKey)
    {
        this.projectKey = projectKey;
    }

    public List<IssueType> getIssueTypes()
    {
        List<IssueType> issuetypes = new ArrayList<IssueType>();

        List<PageElement> rows = table.findAll(By.cssSelector(".project-config-issuetype"));
        for (PageElement row : rows)
        {
            IssueType issueType = new IssueType();
            List<PageElement> tds = row.findAll(By.cssSelector("td"));

            for (PageElement td : tds)
            {
                if (td.hasClass("project-config-issuetype-name"))
                {
                    String text = td.find(By.cssSelector("span.project-config-issuetype-name")).getText();
                    issueType.setName(StringUtils.stripToNull(text));
                    PageElement subtaskMarker = td.find(By.cssSelector("span.project-config-issuetype-subtask"));
                    issueType.setSubtask(subtaskMarker.isPresent());
                    PageElement defaultMarker = td.find(By.cssSelector("span.project-config-issuetype-default"));
                    issueType.setDefaultIssueType(defaultMarker.isPresent());
                }
                else if (td.hasClass("project-config-issuetype-description"))
                {
                    String text = td.find(By.cssSelector("span.project-config-issuetype-description")).getText();
                    issueType.setDescription(StringUtils.stripToNull(text));
                }
                else if (td.hasClass("project-config-issuetype-workflow"))
                {
                    // First try for the link and if that is not present then get the text
                    PageElement link = td.find(By.cssSelector("a.project-config-issuetype-workflow"));
                    if (link.isPresent())
                    {
                        issueType.setWorkflow(new IssueType.Link(link));
                    }
                    else
                    {
                        String text = td.find(By.cssSelector("span.project-config-issuetype-workflow")).getText();
                        issueType.setWorkflowName(text);
                    }
                }
                else if (td.hasClass("project-config-issuetype-field-layout"))
                {
                    // First try for the link and if that is not present then get the text
                    PageElement link = td.find(By.cssSelector("a.project-config-issuetype-field-layout"));
                    if (link.isPresent())
                    {
                        issueType.setFieldLayout(new IssueType.Link(link));
                    }
                    else
                    {
                        String text = td.find(By.cssSelector("span.project-config-issuetype-field-layout")).getText();
                        issueType.setFieldLayoutName(text);
                    }
                }
                else if (td.hasClass("project-config-issuetype-screen"))
                {
                    // First try for the link and if that is not present then get the text
                    PageElement link = td.find(By.cssSelector("a.project-config-issuetype-screen"));
                    if (link.isPresent())
                    {
                        issueType.setFieldScreenScheme(new IssueType.Link(link));
                    }
                    else
                    {
                        String text = td.find(By.cssSelector("span.project-config-issuetype-screen")).getText();
                        issueType.setFieldScreenSchemeName(text);
                    }
                }
            }
            issuetypes.add(issueType);
        }
        return issuetypes;
    }

    @Override
    public String getUrl()
    {
        return "/plugins/servlet/project-config/" + projectKey + "/issuetypes";
    }

    @Override
    public TimedCondition isAt()
    {
        return table.timed().isPresent();
    }

    public boolean isSchemeLinked()
    {
        return dropDown.hasItemById(EDIT_LINK_ID);
    }

    public boolean isSchemeChangeAvailable()
    {
        return dropDown.hasItemById(CHANGE_LINK_ID);
    }

    public EditIssueTypeSchemePage gotoScheme()
    {
        final String schemeId = schemeEditLink.getAttribute("data-id");
        return dropDown.openAndClick(By.id(EDIT_LINK_ID), EditIssueTypeSchemePage.class, Long.valueOf(schemeId));
    }

    public ChangeIssueTypeSchemePage gotoSelectScheme()
    {
        final String projectId = schemeChangeLink.getAttribute("data-id");
        return dropDown.openAndClick(By.id(CHANGE_LINK_ID), ChangeIssueTypeSchemePage.class, Long.valueOf(projectId));
    }

    public String getSchemeName()
    {
        return schemeName.getText();
    }
}
