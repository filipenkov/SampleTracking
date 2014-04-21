/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.issue;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.opensymphony.user.EntityNotFoundException;
import com.opensymphony.user.User;
import com.opensymphony.user.UserManager;
import com.opensymphony.util.TextUtils;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.ComponentManager.getComponent;

public abstract class AbstractCommentableAssignableIssue extends AbstractCommentableIssue implements Assignable
{
    private String assignee;

    protected AbstractCommentableAssignableIssue(IssueLinkManager issueLinkManager, SubTaskManager subTaskManager, FieldScreenRendererFactory fieldScreenRendererFactory, CommentService commentService)
    {
        super(issueLinkManager, subTaskManager, fieldScreenRendererFactory, getComponent(FieldManager.class), getComponent(ProjectRoleManager.class), commentService);
    }

    public String doDefault() throws Exception
    {
        assignee = getIssue().getString("assignee");
        return INPUT;
    }

    protected void doValidation()
    {
        super.doValidation();

        // If the user has the permission then they may have updated the assignee
        if (assigneeChanged())
        {
            if (hasAssigneePermission(getAssignIn()))
            {
                // Check that the assignee is valid
                try
                {
                    if (getAssignee() != null)
                    {
                        final User assigneeUser = UserManager.getInstance().getUser(getAssignee());

                        // Check that the assignee has the assignable permission
                        if (!ManagerFactory.getPermissionManager().hasPermission(Permissions.ASSIGNABLE_USER, getAssignIn(), assigneeUser))
                        {
                            addError("assignee", getText("admin.errors.issues.user.cannot.be.assigned",getAssignee()));
                        }
                    }
                    else
                    {
                        // check whether assigning to null is allowed
                        if (!getApplicationProperties().getOption(APKeys.JIRA_OPTION_ALLOWUNASSIGNED))
                        {
                            log.info("Validation error: Issues must be assigned");
                            addError("assignee", getText("admin.errors.issues.must.be.assigned"));
                        }
                    }
                }
                catch (EntityNotFoundException e)
                {
                    addError("assignee", getText("admin.errors.issues.user.does.not.exit", getAssignee()));
                }
            }
            else
            {
                addErrorMessage(getText("admin.errors.issues.no.permission"));
            }
        }
    }

    public String getAssignee()
    {
        return assignee;
    }

    public void setAssignee(String assignee)
    {
        if (TextUtils.stringSet(assignee))
            this.assignee = assignee;
        else
            this.assignee = null;
    }

    public GenericValue getAssignIn()
    {
        return getProject();
    }

    protected boolean assigneeChanged()
    {
        String originalAssignee = getIssue().getString("assignee");
        return !((originalAssignee == null && getAssignee() == null) || (originalAssignee != null && originalAssignee.equals(getAssignee())));
    }

    protected boolean hasAssigneePermission(GenericValue project)
    {
        return ManagerFactory.getPermissionManager().hasPermission(Permissions.ASSIGN_ISSUE, project, getRemoteUser());
    }
}
