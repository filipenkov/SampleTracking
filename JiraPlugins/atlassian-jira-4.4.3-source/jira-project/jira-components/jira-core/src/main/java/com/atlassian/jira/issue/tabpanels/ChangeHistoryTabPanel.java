package com.atlassian.jira.issue.tabpanels;

import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.action.IssueActionComparator;
import com.atlassian.jira.issue.changehistory.ChangeHistory;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.issue.history.DateTimeFieldChangeLogHelper;
import com.atlassian.jira.plugin.issuetabpanel.AbstractIssueTabPanel;
import com.atlassian.jira.plugin.issuetabpanel.IssueAction;
import com.atlassian.jira.util.JiraDurationUtils;
import com.google.common.collect.Lists;
import com.opensymphony.user.User;

import java.util.Collections;
import java.util.List;

public class ChangeHistoryTabPanel extends AbstractIssueTabPanel
{
    private static final String ALWAYS_SHOW_HEADER = "alwaysShowHeader";
    private final ChangeHistoryManager changeHistoryManager;
    private final AttachmentManager attachmentManager;
    private final JiraDurationUtils jiraDurationUtils;
    private final CustomFieldManager customFieldManager;
    private final DateTimeFormatterFactory dateTimeFormatterFactory;
    private final DateTimeFieldChangeLogHelper changeLogHelper;

    public ChangeHistoryTabPanel(
            final ChangeHistoryManager changeHistoryManager,
            final AttachmentManager attachmentManager,
            final JiraDurationUtils jiraDurationUtils,
            final CustomFieldManager customFieldManager,
            final DateTimeFormatterFactory dateTimeFormatterFactory,
            final DateTimeFieldChangeLogHelper changeLogHelper)
    {
        this.changeHistoryManager = changeHistoryManager;
        this.attachmentManager = attachmentManager;
        this.jiraDurationUtils = jiraDurationUtils;
        this.customFieldManager = customFieldManager;
        this.dateTimeFormatterFactory = dateTimeFormatterFactory;
        this.changeLogHelper = changeLogHelper;
    }

    public List getActions(Issue issue, User remoteUser)
    {
        boolean alwaysShowHeader = Boolean.valueOf(descriptor.getParams().get(ALWAYS_SHOW_HEADER));
        List<ChangeHistory> allChangeHistories = changeHistoryManager.getChangeHistoriesForUser(issue, remoteUser);
        List<IssueAction> changeHistoryActions = Lists.newArrayList();
        boolean first = true;
        for (ChangeHistory changeHistoryItem : allChangeHistories)
        {
            boolean showHeader = first || alwaysShowHeader;
            changeHistoryActions.add(new ChangeHistoryAction(descriptor, changeHistoryItem, showHeader, attachmentManager, jiraDurationUtils, customFieldManager, dateTimeFormatterFactory.formatter(), issue, changeLogHelper));
            first = false;
        }

        // This is a bit of a hack to indicate that there are no change history to display
        if (changeHistoryActions.isEmpty())
        {
            GenericMessageAction action = new GenericMessageAction(descriptor.getI18nBean().getText("viewissue.nochanges"));
            return Lists.newArrayList(action);
        }

        Collections.sort(changeHistoryActions, IssueActionComparator.COMPARATOR);

        return changeHistoryActions;
    }

    public boolean showPanel(Issue issue, User remoteUser)
    {
        return true;
    }
}
