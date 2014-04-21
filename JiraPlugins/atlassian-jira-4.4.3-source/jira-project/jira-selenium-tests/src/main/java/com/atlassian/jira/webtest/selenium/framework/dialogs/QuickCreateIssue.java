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
    private static final String OPEN_DIALOG_LOCATOR = JQUERY.create("div#inline-dialog-create_issue_popup.aui-dialog-content-ready");
    private static final String SUBMIT_BUTTON_LOCATOR = JQUERY.create("div.buttons #quick-create-button");
    private static final String CANCEL_LINK_LOCATOR = JQUERY.create("div.buttons a#quick-create-cancel");
    
    private static final String PROJECT_SELECT_LOCATOR = "#quick-pid";
    private static final String ISSUE_TYPE_SELECT_LOCATOR = "#quick-issuetype";

    public QuickCreateIssue(SeleniumContext ctx)
    {
        super(QuickCreateIssue.class, ActionType.NEW_PAGE, ctx);
    }

    /* ---------------------------------------------- LOCATORS ------------------------------------------------------ */

    @Override
    protected String visibleDialogContentsLocator()
    {
        return OPEN_DIALOG_LOCATOR;
    }

    public String cancelTriggerLocator()
    {
        return inDialog(CANCEL_LINK_LOCATOR);
    }

    public String submitTriggerLocator()
    {
        return inDialog(SUBMIT_BUTTON_LOCATOR);
    }

    public String projectSelectLocator()
    {
        return inDialog(PROJECT_SELECT_LOCATOR);
    }

    public String issueTypeSelectLocator()
    {
        return inDialog(ISSUE_TYPE_SELECT_LOCATOR);
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
        client.click(CREATE_LINK_LOCATOR);
        assertReady(context.timeouts().dialogLoad());
        return this;
    }

}
