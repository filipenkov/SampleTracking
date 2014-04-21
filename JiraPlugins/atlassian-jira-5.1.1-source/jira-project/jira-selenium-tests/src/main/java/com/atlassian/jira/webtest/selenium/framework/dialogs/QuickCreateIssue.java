package com.atlassian.jira.webtest.selenium.framework.dialogs;

import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.selenium.framework.model.ActionType;

import java.util.Arrays;
import java.util.List;

import static com.atlassian.jira.webtest.selenium.framework.model.Locators.JQUERY;

/**
 * 'Quick create issue' dialog.
 *
 * @since v4.2
 */
public class QuickCreateIssue extends AbstractSubmittableDialog<QuickCreateIssue>
{
    private static final String CREATE_LINK_LOCATOR = JQUERY.create("#create_link");
    private static final String SUBMIT_BUTTON_LOCATOR = JQUERY.create("div.buttons #issue-create-submit");
    private static final String CANCEL_LINK_LOCATOR = JQUERY.create("div.buttons a#issue-create-cancel");
    
    private static final String PROJECT_SELECT_LOCATOR = "pid";
    private static final String ISSUE_TYPE_SELECT_LOCATOR = "issuetype";

    public QuickCreateIssue(SeleniumContext ctx)
    {
        super(QuickCreateIssue.class, ActionType.NEW_PAGE, ctx);
    }

    /* ---------------------------------------------- LOCATORS ------------------------------------------------------ */

    @Override
    protected String visibleDialogContentsLocator()
    {
        throw new IllegalStateException("This is no longer a dialog");
    }

    public String cancelTriggerLocator()
    {
        return CANCEL_LINK_LOCATOR;
    }

    public String submitTriggerLocator()
    {
        return SUBMIT_BUTTON_LOCATOR;
    }

    public String projectSelectLocator()
    {
        return PROJECT_SELECT_LOCATOR;
    }

    public String issueTypeSelectLocator()
    {
        return ISSUE_TYPE_SELECT_LOCATOR;
    }

    /* ----------------------------------------------- QUERIES ------------------------------------------------------ */

    public boolean isOpenable()
    {
        return client.isElementPresent(CREATE_LINK_LOCATOR);
    }

    public List<String> getAllProjectNames()
    {
        return Arrays.asList(client.getSelectOptions(projectSelectLocator()));
    }

    public List<String> getAllIssueTypes()
    {
        return Arrays.asList(client.getSelectOptions(issueTypeSelectLocator()));
    }

    /* ----------------------------------------------- ACTIONS ------------------------------------------------------ */

    public QuickCreateIssue open()
    {
        client.open(context.environmentData().getContext() + "/secure/CreateIssue!default.jspa");
        client.waitForPageToLoad();
        return this;
    }

}
