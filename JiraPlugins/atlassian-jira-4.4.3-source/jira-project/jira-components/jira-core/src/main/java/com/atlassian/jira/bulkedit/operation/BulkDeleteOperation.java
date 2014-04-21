/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.bulkedit.operation;

import com.atlassian.core.action.ActionUtils;
import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.action.ActionNames;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.web.bean.BulkEditBean;
import org.apache.log4j.Logger;
import webwork.dispatcher.ActionResult;

import java.util.List;

public class BulkDeleteOperation extends AbstractBulkOperation implements BulkOperation
{
    protected static final Logger log = Logger.getLogger(BulkDeleteOperation.class);

    public static final String NAME = "BulkDelete";
    public static final String NAME_KEY = "bulk.delete.operation.name";
    private static final String DESCRIPTION_KEY = "bulk.delete.operation.description";
    private static final String CANNOT_PERFORM_MESSAGE_KEY = "bulk.delete.cannotperform";

    public boolean canPerform(final BulkEditBean bulkEditBean, final User remoteUser)
    {
        //  Check whether the user has the delete permission for all the selected issues
        List selectedIssues = bulkEditBean.getSelectedIssues();
        PermissionManager permissionManager = ManagerFactory.getPermissionManager();
        for (int i = 0; i < selectedIssues.size(); i++)
        {
            Issue issue = (Issue) selectedIssues.get(i);
            if (!permissionManager.hasPermission(Permissions.DELETE_ISSUE, issue.getGenericValue(), remoteUser))
            {
                return false;
            }
        }

        return true;
    }

    public boolean canPerform(BulkEditBean bulkEditBean, com.opensymphony.user.User remoteUser)
    {
        return canPerform(bulkEditBean, (User) remoteUser);
    }

    public void perform(final BulkEditBean bulkEditBean, final User remoteUser) throws Exception
    {
        List selectedIssues = bulkEditBean.getSelectedIssues();

        for (int i = 0; i < selectedIssues.size(); i++)
        {
            Issue issue = (Issue) selectedIssues.get(i);
            // During bulk delete an issue could have been removed as it is a sub-task of another issue
            // Hence, we need to check whether the issue is actually still in the database
            if (ManagerFactory.getIssueManager().getIssue(issue.getId()) != null)
            {
                // Check if mail should be sent for this bulk operation
                boolean sendMail = bulkEditBean.isSendBulkNotification();

                // Kick off the backend action to delete the issue, only if the issue is still in the database
                ActionResult aResult = CoreFactory.getActionDispatcher().execute(ActionNames.ISSUE_DELETE, EasyMap.build("issue", issue.getGenericValue(), "remoteUser", remoteUser, IssueEvent.SEND_MAIL, Boolean.valueOf(sendMail)));
                ActionUtils.checkForErrors(aResult);
            }
            else if (log.isDebugEnabled())
            {
                log.debug("Not deleting issue with id '" + issue.getLong("id") + "' and key '" + issue.getString("key") + "' as it does not exist in the database (it could have been deleted earlier as it might be a subtask).");
            }
        }
    }

    public void perform(BulkEditBean bulkEditBean, com.opensymphony.user.User remoteUser) throws Exception
    {
        perform(bulkEditBean, (User) remoteUser);
    }


    public String getNameKey()
    {
        return NAME_KEY;
    }

    public String getDescriptionKey()
    {
        return DESCRIPTION_KEY;
    }

    public boolean equals(Object o)
    {
        return this == o || o instanceof BulkDeleteOperation;
    }

    public String getOperationName()
    {
        return NAME;
    }

    public String getCannotPerformMessageKey()
    {
        return CANNOT_PERFORM_MESSAGE_KEY;
    }
}
