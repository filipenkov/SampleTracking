/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.issue;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderer;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.operation.IssueOperations;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;

import java.util.Map;

public class DeleteComment extends AbstractIssueSelectAction
{
    private static final String ERROR_NO_PERMISSION = "errorNoPermission";
    
    private Long id;
    private FieldScreenRenderer fieldScreenRendererWithAllFields;
    private FieldScreenRendererFactory fieldScreenRendererFactory;
    private CommentService commentService;
    private Comment commentObject;

    public DeleteComment(FieldScreenRendererFactory fieldScreenRendererFactory, CommentService commentService)
    {
        this.fieldScreenRendererFactory = fieldScreenRendererFactory;
        this.commentService = commentService;
    }

    /**
     * Handles the initial request to delete a comment coming from the user.
     * <p>It will return the view to render the confirm delete comment form, if the user is authorised to delete the
     * comment.</p>
     * 
     * @return The name of the view to render the confirm delete comment form ({@link webwork.action.Action#INPUT INPUT}),
     * if the user has authorisation to delete the comment.
     *
     * <p>Otherwise, {@link #ERROR_NO_PERMISSION} will be returned so that a screen with an error message can be
     * rendered.</p>
     */
    @Override
    public String doDefault()
    {
        return commentService.hasPermissionToDelete(getJiraServiceContext(), getCommentId()) ? INPUT : ERROR_NO_PERMISSION;
    }

    /**
     * Handles a request to delete a comment.
     *
     * <p>It will delete the comment and redirect the user to the current issue if the user is authorised to the delete
     * the comment and there are no input errors.</p>
     *
     * @return The user will be redirected to the current issue if he/she is authorised to the delete the comment and
     * there are no input errors.
     *
     * <p>{@link #ERROR_NO_PERMISSION} will be returned if the user is not authorised to delete the comment,
     * so that a screen with an error message can be rendered.</p>
     */
    @RequiresXsrfCheck
    @Override
    protected String doExecute()
    {
        if (!commentService.hasPermissionToDelete(getJiraServiceContext(), getCommentId()))
        {
            return ERROR_NO_PERMISSION;
        }

        String issueKey = getIssueObject().getKey();
        commentService.delete(getJiraServiceContext(), getCommentObject(), true);

        if (hasAnyErrors())
        {
            return ERROR;
        }

        return getRedirect("/browse/" + issueKey);
    }

    /**
     * Sets the id of the comment to be deleted.
     * @param id The id of the comment to be deleted.
     */
    public void setCommentId(Long id)
    {
        this.id = id;
    }

    /**
     * Gets the id of the comment to be deleted.
     * @return The id of the comment to be deleted.
     */
    public Long getCommentId()
    {
        return id;
    }

    @Override
    public String getIssuePath()
    {
        return "/browse/" + getIssueObject().getKey();
    }

    public String getRenderedContent()
    {
        OrderableField field = (OrderableField) getField(IssueFieldConstants.COMMENT);
        FieldLayoutItem fieldLayoutItem = getFieldScreenRendererLayoutItemForField(field).getFieldLayoutItem();
        Map value = EasyMap.build(field.getId(), getCommentObject().getBody());
        return fieldLayoutItem.getOrderableField().getViewHtml(fieldLayoutItem, this, getIssueObject(), value, null);
    }

    public FieldScreenRenderLayoutItem getFieldScreenRendererLayoutItemForField(OrderableField field)
    {
        return getFieldScreenRendererWithAllFields().getFieldScreenRenderLayoutItem(field);
    }

    protected FieldScreenRenderer getFieldScreenRendererWithAllFields()
    {
        if (fieldScreenRendererWithAllFields == null)
        {
            fieldScreenRendererWithAllFields = fieldScreenRendererFactory.getFieldScreenRenderer(getLoggedInUser(), getIssueObject(), IssueOperations.VIEW_ISSUE_OPERATION, false);
        }
        return fieldScreenRendererWithAllFields;
    }

    public Comment getCommentObject()
    {
        if (commentObject == null)
        {
            commentObject = commentService.getCommentById(getLoggedInUser(), getCommentId(), this);
        }
        return commentObject;
    }
}
