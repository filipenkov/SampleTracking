package com.atlassian.jira.webtest.selenium.framework.dialogs;

import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.page.issue.SeleniumViewIssue;
import com.atlassian.jira.webtest.framework.page.issue.ViewIssue;
import com.atlassian.jira.webtest.selenium.framework.model.ActionType;
import com.atlassian.jira.webtest.selenium.framework.model.LegacyIssueOperation;

import static com.atlassian.jira.webtest.selenium.framework.model.Locators.JQUERY;

/**
 * TODO: Document this class / interface here
 *
 * @since v4.3
 */
public class DeleteIssueLinkDialog extends AbstractSubmittableDialog<DeleteIssueLinkDialog>
{
    protected final ViewIssue viewIssue;
    protected final String linkId;
    protected static final String VISIBLE_DIALOG_CONTENT_SELECTOR = JQUERY.create(".aui-dialog-open");
    private static final String GENERIC_SUBMIT_SELECTOR = VISIBLE_DIALOG_CONTENT_SELECTOR + " :submit";
    private static final String GENERIC_CANCEL_SELECTOR = VISIBLE_DIALOG_CONTENT_SELECTOR + " .cancel";

    public DeleteIssueLinkDialog(SeleniumContext ctx, String linkId)
    {
        super(DeleteIssueLinkDialog.class, ActionType.NEW_PAGE, ctx);
        this.viewIssue =  new SeleniumViewIssue(ctx);
        this.linkId =  linkId;
    }

    @Override
    protected String visibleDialogContentsLocator()
    {
        return VISIBLE_DIALOG_CONTENT_SELECTOR;
    }

    @Override
    public String cancelTriggerLocator()
    {
        return GENERIC_CANCEL_SELECTOR;
    }

    @Override
    public String submitTriggerLocator()
    {
        return GENERIC_SUBMIT_SELECTOR;
    }

    @Override
    public boolean isOpenable()
    {
        return viewIssue.isAt().byDefaultTimeout();
    }

    @Override
    public Dialog open()
    {
        client.click(linkId);
        assertReady(context.timeouts().dialogLoad());
        return this;
    }
}
