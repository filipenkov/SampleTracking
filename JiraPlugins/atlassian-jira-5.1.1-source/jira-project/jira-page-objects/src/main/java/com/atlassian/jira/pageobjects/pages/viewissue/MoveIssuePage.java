package com.atlassian.jira.pageobjects.pages.viewissue;

import com.atlassian.jira.pageobjects.components.fields.SingleSelect;
import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;

import java.util.ArrayList;
import java.util.List;

/**
 * @since v5.0
 */
public class MoveIssuePage extends AbstractJiraPage
{
    private static final String URI = "/secure/MoveIssue!default.jspa";

    private final String issueKey;

    @ElementBy (id = "project-field")
    private PageElement newProjectField;

    @ElementBy (cssSelector = "#project-single-select .drop-menu")
    private PageElement newProjectDropMenuTrigger;

    @ElementBy (id = "next_submit")
    private PageElement nextButton;

    public MoveIssuePage(String issueKey) {
        this.issueKey = issueKey;
    }

    @Override
    public TimedCondition isAt()
    {
        return newProjectField.timed().isPresent();
    }

    @Override
    public String getUrl()
    {
        return URI + "?key=" + issueKey;
    }

    public MoveIssuePage setNewProject(String newProject)
    {
        newProjectDropMenuTrigger.click();

        final SingleSelect singleSelect = pageBinder.bind(SingleSelect.class, elementFinder.find(By.id("project_container")));
        singleSelect.select(newProject);
        return this;
    }

    public List<String> getIssueTypes()
    {
        List<String> issueTypes = new ArrayList<String>();
        final List<PageElement> options = elementFinder.findAll(By.cssSelector("#issuetype option"));
        for (PageElement option : options)
        {
            if (option.getValue() != null && !option.getValue().isEmpty())
            {
                issueTypes.add(StringUtils.trim(option.getAttribute("innerHTML")));
            }
        }
        return issueTypes;
    }

    public MoveIssueUpdateFields next()
    {
        nextButton.click();

        return pageBinder.bind(MoveIssueUpdateFields.class);
    }

    public MoveIssueUpdateStatus submitAndGoToSetNewIssueStatus(String issueID, String assignee)
    {
        nextButton.click();
        return pageBinder.bind(MoveIssueUpdateStatus.class, issueID, assignee);
    }

    public MoveIssuePage setNewIssueType(String newIssueType)
    {
        final SingleSelect singleSelect = pageBinder.bind(SingleSelect.class, elementFinder.find(By.id("issuetype_container")));
        singleSelect.select(newIssueType);
        return this;
    }
}
