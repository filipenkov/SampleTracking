/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.issue;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderer;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.issue.operation.IssueOperation;
import com.atlassian.jira.issue.operation.IssueOperations;
import com.atlassian.jira.issue.util.DefaultIssueChangeHolder;
import com.atlassian.jira.issue.util.IssueChangeHolder;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import org.apache.commons.lang.StringUtils;
import webwork.action.ActionContext;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This class should be extended by any action that modifies issues and can have an associated comment.
 * <p/>
 * Subclasses should call super.doValidation() to check commenting permission.
 */
public class AbstractCommentableIssue extends AbstractViewIssue implements OperationContext
{
    private String comment;
    private String groupLevel;
    private String commentLevel;

    private final Map fieldValuesHolder = new HashMap();
    private transient final FieldScreenRendererFactory fieldScreenRendererFactory;
    private transient final FieldManager fieldManager;
    private transient final ProjectRoleManager projectRoleManager;
    private transient final CommentService commentService;
    private transient FieldScreenRenderer fieldScreenRendererWithAllFields;

    public AbstractCommentableIssue(final IssueLinkManager issueLinkManager, final SubTaskManager subTaskManager,
            final FieldScreenRendererFactory fieldScreenRendererFactory, final FieldManager fieldManager,
            final ProjectRoleManager projectRoleManager, final CommentService commentService)
    {
        super(issueLinkManager, subTaskManager);
        this.fieldScreenRendererFactory = fieldScreenRendererFactory;
        this.fieldManager = fieldManager;
        this.projectRoleManager = projectRoleManager;
        this.commentService = commentService;
    }

    @Override
    protected void doValidation()
    {
        doCommentValidation();
    }

    protected void doCommentValidation()
    {
        // validate comments params if there
        final OrderableField field = getOrderableField(IssueFieldConstants.COMMENT);
        field.populateFromParams(getFieldValuesHolder(), ActionContext.getParameters());
        field.validateParams(this, this, this, getIssueObject(), getFieldScreenRendererLayoutItemForField(field));
    }

    protected Comment createComment(final MutableIssue commentedIssue) throws Exception
    {
        final OrderableField commentField = getOrderableField(IssueFieldConstants.COMMENT);
        final FieldLayoutItem fieldLayoutItem = ComponentAccessor.getFieldLayoutManager().getFieldLayout(
            commentedIssue.getGenericValue()).getFieldLayoutItem(commentField);
        commentField.updateIssue(fieldLayoutItem, getIssueObject(), getFieldValuesHolder());

        final ModifiedValue comment = commentedIssue.getModifiedFields().get(IssueFieldConstants.COMMENT);
        final IssueChangeHolder changeHolder = new DefaultIssueChangeHolder();
        if (comment != null)
        {
            commentField.updateValue(fieldLayoutItem, commentedIssue, comment, changeHolder);
        }
        return changeHolder.getComment();
    }

    public String getComment()
    {
        return comment;
    }

    public void setComment(final String comment)
    {
        this.comment = comment;
    }

    public String getCommentLevel()
    {
        return commentLevel;
    }

    public void setCommentLevel(final String commentLevel)
    {
        this.commentLevel = commentLevel;
    }

    /**
     * @return the name of the group to which comment visibility will be restricted
     */
    public String getGroupLevel()
    {
        return groupLevel;
    }

    /**
     * @param groupLevel
     */
    public void setGroupLevel(final String groupLevel)
    {
        if (!StringUtils.isBlank(groupLevel))
        {
            // make sure that people only set levels they are in
            if ((getRemoteUser() != null) && getRemoteUser().inGroup(groupLevel))
            {
                if (!StringUtils.isBlank(groupLevel))
                {
                    this.groupLevel = groupLevel;
                }
            }
        }
    }

    public Collection getGroupLevels()
    {
        Collection groups;
        if ((getRemoteUser() == null) || !commentService.isGroupVisiblityEnabled())
        {
            groups = Collections.EMPTY_LIST;
        }
        else
        {
            groups = getRemoteUser().getGroups();
        }
        return groups;
    }

    public Collection<ProjectRole> getRoleLevels()
    {
        Collection<ProjectRole> roleLevels;
        if (commentService.isProjectRoleVisiblityEnabled())
        {
            roleLevels = projectRoleManager.getProjectRoles(getRemoteUser(), getIssueObject().getProjectObject());
        }
        else
        {
            roleLevels = Collections.emptyList();
        }
        return roleLevels;
    }

    public Map getFieldValuesHolder()
    {
        return fieldValuesHolder;
    }

    public IssueOperation getIssueOperation()
    {
        return IssueOperations.EDIT_ISSUE_OPERATION;
    }

    public void setFieldValuesHolder(final Map fieldValuesHolder)
    {
        this.fieldValuesHolder.clear();
        this.fieldValuesHolder.putAll(fieldValuesHolder);
    }

    public FieldScreenRenderLayoutItem getFieldScreenRendererLayoutItemForField(final OrderableField field)
    {
        return getFieldScreenRendererWithAllFields().getFieldScreenRenderLayoutItem(field);
    }

    public Map<String, Object> getDisplayParams()
    {
        return Collections.emptyMap();
    }

    protected FieldScreenRenderer getFieldScreenRendererWithAllFields()
    {
        if (fieldScreenRendererWithAllFields == null)
        {
            fieldScreenRendererWithAllFields = fieldScreenRendererFactory.getFieldScreenRenderer(getRemoteUser(), getIssueObject(),
                IssueOperations.VIEW_ISSUE_OPERATION, false);
        }

        return fieldScreenRendererWithAllFields;
    }

    protected OrderableField getOrderableField(final String fieldId)
    {
        return (OrderableField) fieldManager.getField(fieldId);
    }
}
