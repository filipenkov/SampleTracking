/*
 * Copyright (c) 2002-2006
 * All rights reserved.
 */

package com.atlassian.jira.web.action.issue;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.event.issue.IssueEventDispatcher;
import com.atlassian.jira.event.issue.IssueEventSource;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.exception.IssueNotFoundException;
import com.atlassian.jira.exception.IssuePermissionException;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.fields.CommentSystemField;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.util.UtilDateTime;

import java.util.HashMap;
import java.util.Map;


public class AddComment extends AbstractCommentableIssue implements OperationContext
{
    private final PermissionManager permissionManager;

    public AddComment(IssueLinkManager issueLinkManager, SubTaskManager subTaskManager, FieldManager fieldManager,
            FieldScreenRendererFactory fieldScreenRendererFactory, ProjectRoleManager projectRoleManager,
            CommentService commentService, PermissionManager permissionManager)
    {
        super(issueLinkManager, subTaskManager, fieldScreenRendererFactory, fieldManager, projectRoleManager,
                commentService);
        
        this.permissionManager = permissionManager;
    }

    /**
     * Returns the name of the view to render the form to add a comment. This is only called when you right-click and
     * open in a new tab from the view issue page, it is not used to render the inline comment form.
     *
     * @return {@link #INPUT} if the issue exists and the user is authorised to comment on it; otherwise,
     * {@link #ERROR} is returned.
     */
    public String doDefault()
    {
        try
        {
            if (!isAbleToComment())
            {
                return ERROR;
            }
        }
        catch (IssueNotFoundException e)
        {
            //do not show error messages since the view will take care of it
            getErrorMessages().clear();
            return ERROR;
        }
        catch (IssuePermissionException ipe)
        {
            //do not show error messages since the view will take care of it
            getErrorMessages().clear();
            return ERROR;
        }

        return INPUT;
    }

    public boolean isAbleToComment()
    {
        return (getIssue() == null) ? permissionManager.hasPermission(Permissions.COMMENT_ISSUE, getIssueObject().getProject(), getRemoteUser())
                : permissionManager.hasPermission(Permissions.COMMENT_ISSUE, getIssue(), getRemoteUser());
    }

    protected void doValidation()
    {
        try
        {
            getFieldValuesHolder().put(CommentSystemField.CREATE_COMMENT, "true");
            super.doValidation();
        }
        catch (IssueNotFoundException e)
        {
            // error message has been added in the super class
        }
        catch (IssuePermissionException e)
        {
            // error message has been added in the super class
        }
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        Comment comment = createComment(getIssueObject());

        // dispatch event and update the issues updated field
        alertSystemOfComment(comment);

        // Reset the fields as comment has been persisted to the db.
        getIssueObject().resetModifiedFields();

        //if a return url was explicitly specified (like when triggering this action from the Issue navigator
        //return a redirect to that return URL instead of the view issue page with the comment focused!
        if(StringUtils.isNotBlank(getReturnUrl()))
        {
            return returnCompleteWithInlineRedirect(getReturnUrl());
        }
        //
        // Its possible that the comment is in fact null for empty input.  While a bit strange, this is established
        // JIRA behaviour, probably because on transition you can have empty comments.  So we cater for it.
        //
        // I call it strange because all of the above code must be handling NULL comment objects.
        //
        final String browseIssue = "/browse/" + getIssue().getString("key");
        if (comment != null)
        {
            return returnComplete(browseIssue + "?focusedCommentId=" + comment.getId() +
                    "#comment-" + comment.getId());
        }
        else
        {
            return returnComplete(browseIssue);
        }
    }

    private void alertSystemOfComment(Comment comment) throws GenericEntityException
    {
        getIssueObject().setUpdated(UtilDateTime.nowTimestamp());
        getIssueObject().store();

        // fire a comment event
        IssueEventDispatcher.dispatchEvent(EventType.ISSUE_COMMENTED_ID, getIssueObject(), getRemoteUser(), comment, null, null, EasyMap.build("eventsource", IssueEventSource.ACTION));
    }

    @Override
    public Map<String, Object> getDisplayParams()
    {
        final Map<String, Object> displayParams = new HashMap<String, Object>(super.getDisplayParams());
        displayParams.put("theme", "aui");
        return displayParams;
    }
}
