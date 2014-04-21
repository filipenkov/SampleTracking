package com.atlassian.jira.webtest.framework.impl.selenium.form.issueaction;

import com.atlassian.jira.webtest.framework.component.CommentInput;
import com.atlassian.jira.webtest.framework.component.fc.IssuePicker;
import com.atlassian.jira.webtest.framework.core.Localizable;
import com.atlassian.jira.webtest.framework.core.component.Select;
import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.form.issueaction.LinkIssueForm;
import com.atlassian.jira.webtest.framework.impl.selenium.component.SeleniumCommentInput;
import com.atlassian.jira.webtest.framework.impl.selenium.component.fc.SeleniumIssuePicker;
import com.atlassian.jira.webtest.framework.impl.selenium.core.AbstractSeleniumPageObject;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.core.component.SeleniumSelect;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.atlassian.jira.webtest.framework.core.condition.Conditions.and;

/**
 * Selenium implementation of {@link com.atlassian.jira.webtest.framework.form.issueaction.LinkIssueForm}.
 *
 * @since v4.3
 */
public class SeleniumLinkIssueForm extends AbstractSeleniumPageObject implements LinkIssueForm
{
    private final Localizable parent;

    private final Select linkTypeSelect;
    private final IssuePicker issuePicker;
    private final CommentInput comment;

    public SeleniumLinkIssueForm(Localizable parent, SeleniumContext context)
    {
        super(context);
        this.parent = notNull("parent", parent);
        this.linkTypeSelect = new SeleniumSelect(linkTypeSelectLocator(parent), context());
        this.issuePicker = new SeleniumIssuePicker("jira-issue-keys", context());
        this.comment = new SeleniumCommentInput(commentContainerLocator(), context());
    }

    private SeleniumLocator linkTypeSelectLocator(Localizable parent)
    {
        return (SeleniumLocator) parent.locator().combine(id("issue-link-link-type"));
    }

    private SeleniumLocator commentContainerLocator()
    {
        return (SeleniumLocator) parent.locator().combine(forClass("comment-input"));
    }

    

    @Override
    public TimedCondition isReady()
    {
        return and(parent.isReady(), issuePicker().isReady());
    }

    @Override
    public Select linkTypeSelect()
    {
        return linkTypeSelect;
    }

    @Override
    public IssuePicker issuePicker()
    {
        return issuePicker;
    }

    @Override
    public CommentInput comment()
    {
        return comment;
    }
}
