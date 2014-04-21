package com.atlassian.jira.pageobjects.dialogs;

import com.atlassian.jira.pageobjects.components.fields.AutoComplete;
import com.atlassian.jira.pageobjects.components.menu.IssueActions;
import com.atlassian.jira.pageobjects.dialogs.quickedit.DeleteIssueDialog;
import com.atlassian.jira.pageobjects.dialogs.quickedit.EditIssueDialog;
import com.atlassian.jira.pageobjects.pages.viewissue.ActionTrigger;
import com.atlassian.jira.pageobjects.pages.viewissue.AddCommentSection;
import com.atlassian.jira.pageobjects.pages.viewissue.AssignIssueDialog;
import com.atlassian.jira.pageobjects.pages.viewissue.CloseIssueDialog;
import com.atlassian.jira.pageobjects.pages.viewissue.CommentType;
import com.atlassian.jira.pageobjects.pages.viewissue.ResolveIssueDialog;
import com.atlassian.jira.pageobjects.pages.viewissue.ViewIssuePage;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import org.openqa.selenium.By;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

public class IssueActionsUtil
{
    @Inject
    PageBinder pageBinder;

    @Inject
    PageElementFinder pageElementFinder;

    private void execKeyboardShortcut(final CharSequence... keys)
    {
        pageElementFinder.find(By.tagName("body")).type(keys);
    }

    private void selectViaActionsDialog(String label)
    {
        execKeyboardShortcut(".");
        final AutoComplete autoComplete = pageBinder.bind(IssueActionsDialog.class).getAutoComplete();
        autoComplete.query(label);
        autoComplete.acceptUsingKeyboard(autoComplete.getActiveSuggestion());
    }

    private void invokeActionTrigger(IssueActions action, ActionTrigger trigger)
    {
        switch (trigger)
        {
            case ACTIONS_DIALOG:
                selectViaActionsDialog(action.getLabel());
                break;
            case MENU:
                PageElement assignElement = pageElementFinder.find(action.getSelector());
                if (!assignElement.isPresent() || !assignElement.isVisible())
                {
                    assignElement = pageElementFinder.find(By.cssSelector(".ajs-layer.active")).find(action.getSelector());
                }
                assignElement.click();
                break;
            case KEYBOARD_SHORTCUT:
                execKeyboardShortcut(action.getShortcut());
                break;
        }
    }

    public void assignIssue(String user, ActionTrigger trigger)
    {
        invokeActionTrigger(IssueActions.ASSIGN_ISSUE, trigger);
        AssignIssueDialog assignIssueDialog = pageBinder.bind(AssignIssueDialog.class);
        assignIssueDialog.setAssignee(user);
        assignIssueDialog.submit();
    }

    public void addLabels(List<String> labels, ActionTrigger trigger)
    {
        invokeActionTrigger(IssueActions.LABELS, trigger);
        final LabelsDialog labelsDialog = pageBinder.bind(LabelsDialog.class);
        labelsDialog.addLabels(labels);
        labelsDialog.submit();
    }

    public void addComment(String comment, ActionTrigger trigger, CommentType expectedType)
    {
        invokeActionTrigger(IssueActions.COMMENT, trigger);
        switch (expectedType)
        {
            case DIALOG:
                final CommentDialog commentDialog = pageBinder.bind(CommentDialog.class);
                commentDialog.setComment(comment);
                commentDialog.submit();
                break;
            case DRAWER:
                final String key = pageElementFinder.find(By.id("key-val")).getText();
                final ViewIssuePage viewIssuePage = pageBinder.bind(ViewIssuePage.class, key);
                AddCommentSection drawer = pageBinder.bind(AddCommentSection.class, viewIssuePage);
                drawer.typeComment(comment).addAndWait();
                break;
        }

    }

    public void editIssue(Map<String, String> values, ActionTrigger trigger)
    {
        invokeActionTrigger(IssueActions.EDIT_ISSUE, trigger);
        final EditIssueDialog editIssueDialog = pageBinder.bind(EditIssueDialog.class);
        editIssueDialog.setFields(values);
        editIssueDialog.submit();
    }

    public void closeIssue(ActionTrigger trigger)
    {
        invokeActionTrigger(IssueActions.CLOSE_ISSUE, trigger);
        final CloseIssueDialog closeIssueDialog = pageBinder.bind(CloseIssueDialog.class);
        closeIssueDialog.submit();
    }

    public void resolveIssue(ActionTrigger trigger)
    {
        invokeActionTrigger(IssueActions.RESOLVE_ISSUE, trigger);
        final ResolveIssueDialog resolveIssueDialog = pageBinder.bind(ResolveIssueDialog.class);
        resolveIssueDialog.submit();
    }

    public void startProgress(ActionTrigger trigger)
    {
        invokeActionTrigger(IssueActions.START_PROGRESS, trigger);
    }

    public void stopProgress(ActionTrigger trigger)
    {
        invokeActionTrigger(IssueActions.STOP_PROGRESS, trigger);
    }

    public void stopWatching(ActionTrigger trigger)
    {
        invokeActionTrigger(IssueActions.STOP_WATCHING, trigger);
    }

    public void startWatching(ActionTrigger trigger)
    {
        invokeActionTrigger(IssueActions.START_WATCHING, trigger);
    }

    public void delete(ActionTrigger trigger)
    {
        invokeActionTrigger(IssueActions.DELETE, trigger);
        final DeleteIssueDialog deleteIssueDialog = pageBinder.bind(DeleteIssueDialog.class);
        deleteIssueDialog.deleteIssue();
    }
}
