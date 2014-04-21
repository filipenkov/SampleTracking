/*
 * Copyright (c) 2002-2007
 * All rights reserved.
 */

package com.atlassian.jira.web.action.issue;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.comments.MutableComment;
import com.atlassian.jira.issue.fields.CommentSystemField;
import com.atlassian.jira.issue.fields.CommentVisibility;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.util.UrlBuilder;
import webwork.action.ActionContext;

import java.util.HashMap;
import java.util.Map;

public class EditComment extends AbstractCommentableIssue
{
    private CommentService commentService;
    private Long commentId;
    private MutableComment commentObject;

    private final static String ERROR_KEY_NO_ASSOC_ISSUE = "edit.comment.no.associated.issue";
    private static final String ERROR_NO_PERMISSION = "errorNoPermission";

    public EditComment(IssueLinkManager issueLinkManager, SubTaskManager subTaskManager, FieldScreenRendererFactory fieldScreenRendererFactory, FieldManager fieldManager, ProjectRoleManager projectRoleManager, CommentService commentService)
    {
        super(issueLinkManager, subTaskManager, fieldScreenRendererFactory, fieldManager, projectRoleManager, commentService);
        this.commentService = commentService;
    }

    protected void doValidation()
    {
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        // Check that we can edit the comment
        if (!commentService.hasPermissionToEdit(getJiraServiceContext(), getCommentId()))
        {
            return ERROR_NO_PERMISSION;
        }

        CommentVisibility commentVisibility = new CommentVisibility(getCommentLevel());
        final String roleLevel = commentVisibility.getRoleLevel();
        commentService.validateCommentUpdate(getRemoteUser(), getCommentId(), getComment(),
                commentVisibility.getGroupLevel(), roleLevel == null ? null : new Long(roleLevel), this);

        // By getting the comment we will see if it exists, if it does not then an error will be added.
        final MutableComment commentObject = getCommentObject();
        if (commentObject == null)
        {
            return ERROR;
        }

        Issue issue = commentObject.getIssue();
        if (issue == null)
        {
            addErrorMessage(getText(ERROR_KEY_NO_ASSOC_ISSUE, commentObject.getId()));
            return ERROR;
        }

        // This populates the values that were entered by the user into the field and security level dropdown
        // NOTE: We do not want to use the CommentSystemFields validation as it validates for Create not update
        OrderableField field = (OrderableField) ComponentManager.getInstance().getFieldManager().getField(IssueFieldConstants.COMMENT);
        field.populateFromParams(getFieldValuesHolder(), ActionContext.getParameters());

        if (hasAnyErrors())
        {
            return ERROR;
        }

        final Long roleLevelId = (commentVisibility.getRoleLevel() == null)
                ? null : new Long(commentVisibility.getRoleLevel());

        commentObject.setBody(getComment());
        commentObject.setGroupLevel(commentVisibility.getGroupLevel());
        commentObject.setRoleLevelId(roleLevelId);
        commentService.update(getRemoteUser(), commentObject, true, this);

        // redirect to the comment focused and anchored
        final String baseUrl = "/browse/" + issue.getKey();
        final UrlBuilder urlBuilder = new UrlBuilder(baseUrl);
        urlBuilder.addParameter("focusedCommentId", getCommentId().toString());
        urlBuilder.addParameter("page", "com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel");
        urlBuilder.addAnchor("comment-" + getCommentId().toString());
        return getRedirect(urlBuilder.asUrlString());
    }

    public String doDefault() throws Exception
    {
        if (!commentService.hasPermissionToEdit(getJiraServiceContext(), getCommentId()))
        {
            return ERROR_NO_PERMISSION;
        }

        final MutableComment commentObject = getCommentObject();
        if (commentObject == null)
        {
            return ERROR;
        }

        // Check that we can edit the comment
        if (!commentService.hasPermissionToEdit(getRemoteUser(), commentObject, this))
        {
            return ERROR;
        }

        Map initialValues = new HashMap();
        initialValues.put(IssueFieldConstants.COMMENT, new String []{commentObject.getBody()});
        initialValues.put(CommentSystemField.PARAM_COMMENT_LEVEL, new String []{CommentVisibility.getCommentLevelFromLevels(commentObject.getGroupLevel(), commentObject.getRoleLevelId())});

        // Initialize the comment textarea with the current comments body
        OrderableField commentField = (OrderableField) getField(IssueFieldConstants.COMMENT);
        commentField.populateFromParams(getFieldValuesHolder(), initialValues);

        return super.doDefault();
    }

    public void setCommentId(Long id)
    {
        this.commentId = id;
    }

    public Long getCommentId()
    {
        return commentId;
    }

    public MutableComment getCommentObject()
    {
        if (commentObject == null)
        {
            commentObject = commentService.getMutableComment(getRemoteUser(), getCommentId(), this);
        }
        return commentObject;
    }

    @Override
    public Map<String, Object> getDisplayParams()
    {
        final Map<String, Object> displayParams = new HashMap<String, Object>(super.getDisplayParams());
        displayParams.put("theme", "aui");
        return displayParams;
    }

}
