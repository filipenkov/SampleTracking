/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.issue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.util.UserUtil;
import com.opensymphony.util.TextUtils;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.ComponentManager.getComponent;

public abstract class AbstractCommentableAssignableIssue extends AbstractCommentableIssue implements Assignable
{
    private String assignee;

    protected AbstractCommentableAssignableIssue(SubTaskManager subTaskManager,
            FieldScreenRendererFactory fieldScreenRendererFactory, CommentService commentService, UserUtil userUtil)
    {
        super(subTaskManager, fieldScreenRendererFactory, getComponent(FieldManager.class), getComponent(ProjectRoleManager.class), commentService, userUtil);
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
                if (getAssignee() != null)
                {
                    final User assigneeUser = userUtil.getUser(getAssignee());
                    if (assigneeUser != null)
                    {
                        // Check that the assignee has the assignable permission
                        if (!ManagerFactory.getPermissionManager().hasPermission(Permissions.ASSIGNABLE_USER, getAssignIn(), assigneeUser))
                        {
                            addError("assignee", getText("admin.errors.issues.user.cannot.be.assigned",getAssignee()));
                        }
                    }
                    else
                    {
                        addError("assignee", getText("admin.errors.issues.user.does.not.exit", getAssignee()));
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
        return ManagerFactory.getPermissionManager().hasPermission(Permissions.ASSIGN_ISSUE, project, getLoggedInUser());
    }
}
