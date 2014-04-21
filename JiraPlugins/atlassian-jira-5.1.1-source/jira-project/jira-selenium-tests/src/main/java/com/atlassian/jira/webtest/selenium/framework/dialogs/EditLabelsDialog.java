package com.atlassian.jira.webtest.selenium.framework.dialogs;

import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.selenium.framework.components.issue.EditLabelsContents;
import com.atlassian.jira.webtest.selenium.framework.model.ActionType;
import com.atlassian.jira.webtest.selenium.framework.model.LegacyIssueOperation;

/**
 * Represents the "Edit labels" dialog. 
 *
 * @since v4.1
 */
public final class EditLabelsDialog extends AbstractIssueDialog<EditLabelsDialog>
{

    private final EditLabelsContents contents;

    public EditLabelsDialog(SeleniumContext ctx)
    {
        super(LegacyIssueOperation.EDIT_LABELS, EditLabelsDialog.class, ActionType.NEW_PAGE, ctx);
        this.contents = new EditLabelsContents(ctx, VISIBLE_DIALOG_CONTENT_SELECTOR); 
    }

    public EditLabelsDialog openFromViewIssueForCustomField(Long customFieldId)
    {
        client.click("edit-labels-customfield_" + customFieldId);
        assertThat.visibleByTimeout("jquery=.aui-dialog-open", 5000);
        return asTargetType();
    }

    @Override
    protected String dialogContentsReadyLocator()
    {
        return contents.labelsPicker().locator();
    }

    // TODO pull up this method to IssueActionDialog - every dialog can also be opened in a
    // TODO standalone mode

    public EditLabelsContents contents()
    {
        return contents;
    }
    
}
