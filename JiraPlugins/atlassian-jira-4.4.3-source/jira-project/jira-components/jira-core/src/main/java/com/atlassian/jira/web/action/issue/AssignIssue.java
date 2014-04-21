/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.issue;

import com.atlassian.core.action.ActionUtils;
import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.user.UserUtils;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.action.ActionNames;
import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.exception.IssueNotFoundException;
import com.atlassian.jira.exception.IssuePermissionException;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderTab;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderer;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.issue.operation.IssueOperations;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.opensymphony.user.User;
import com.opensymphony.util.TextUtils;
import webwork.action.ActionContext;
import webwork.dispatcher.ActionResult;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class AssignIssue extends AbstractCommentableAssignableIssue implements OperationContext
{
    private FieldScreenRenderer fieldScreenRenderer;
    private FieldScreenRendererFactory fieldScreenRendererFactory;
    private FieldLayoutManager fieldLayoutManager;
    private FieldManager fieldManager;
    private final IssueManager issueManager;

    public AssignIssue(IssueLinkManager issueLinkManager, SubTaskManager subTaskManager,
                       FieldScreenRendererFactory fieldScreenRendererFactory, FieldLayoutManager fieldLayoutManager,
                       FieldManager fieldManager, CommentService commentService, final IssueManager issueManager)
    {
        super(issueLinkManager, subTaskManager, fieldScreenRendererFactory, commentService);
        this.fieldScreenRendererFactory = fieldScreenRendererFactory;
        this.fieldLayoutManager = fieldLayoutManager;
        this.fieldManager = fieldManager;
        this.issueManager = issueManager;
    }

    public String doDefault() throws Exception
    {
        try
        {
            for (Iterator iterator = getFieldScreenRenderer().getFieldScreenRenderTabs().iterator(); iterator.hasNext();)
            {
                FieldScreenRenderTab fieldScreenRenderTab = (FieldScreenRenderTab) iterator.next();
                for (Iterator iterator1 = fieldScreenRenderTab.getFieldScreenRenderLayoutItems().iterator(); iterator1.hasNext();)
                {
                    FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem = (FieldScreenRenderLayoutItem) iterator1.next();
                    if (fieldScreenRenderLayoutItem.isShow(getIssueObject()))
                    {
                        fieldScreenRenderLayoutItem.populateFromIssue(getFieldValuesHolder(), getIssueObject());
                    }
                }
            }

            return super.doDefault();
        }
        catch (IssueNotFoundException e)
        {
            // Error is added above
            return ERROR;
        }
        catch (IssuePermissionException e)
        {
            return ERROR;
        }
    }

    public void doValidation()
    {
        try
        {
            // validate the comment params
            doCommentValidation();

            // validate assignee params
            OrderableField field = (OrderableField) fieldManager.getField(IssueFieldConstants.ASSIGNEE);
            field.populateFromParams(getFieldValuesHolder(), ActionContext.getParameters());
            field.validateParams(this, this, this, getIssueObject(), getFieldScreenRendererLayoutItemForField(field));

            if(!issueManager.isEditable(getIssueObject()))
            {
                addErrorMessage(getText("editissue.error.no.edit.workflow"));
            }

            // Do extra validation that only needs to occur on the assign issue screen
            if (!TextUtils.stringSet(getAssignee()))
            {
                if (!TextUtils.stringSet(getIssue().getString("assignee")))
                {
                    addError("assignee", getText("assign.error.alreadyunassigned"));
                }
            }
            else
            {
                if (getAssignee().equals(getIssue().getString("assignee")))
                {
                    User currentAssignee = UserUtils.getUser(getAssignee());
                    addError("assignee", getText("assign.error.alreadyassigned", EasyList.build(currentAssignee.getDisplayName(), currentAssignee.getName())));
                }
            }
        }
        catch (Exception e)
        {
            log.error(e, e);
            addError("assignee", getText("assign.error.userdoesnotexist", getAssignee()));
        }
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        MutableIssue issue = getIssueObject();

        // update the assignee system field
        OrderableField field = (OrderableField) fieldManager.getField(IssueFieldConstants.ASSIGNEE);
        FieldLayoutItem fieldLayoutItem = fieldLayoutManager.getFieldLayout(issue.getGenericValue()).getFieldLayoutItem(field);
        field.updateIssue(fieldLayoutItem, issue, getFieldValuesHolder());

        // This hack is here until the comment field becomes placeable on screens by the users
        OrderableField commentField = (OrderableField) fieldManager.getField(IssueFieldConstants.COMMENT);
        FieldLayoutItem fieldLayoutItem2 = fieldLayoutManager.getFieldLayout(issue.getProject(), issue.getIssueTypeObject().getId()).getFieldLayoutItem(commentField);
        commentField.updateIssue(fieldLayoutItem2, issue, getFieldValuesHolder());

        Map actionParams = EasyMap.build("issue", issue.getGenericValue(), "issueObject", issue, "remoteUser", getRemoteUser(), "eventTypeId", EventType.ISSUE_ASSIGNED_ID);

        ActionResult aResult = CoreFactory.getActionDispatcher().execute(ActionNames.ISSUE_UPDATE, actionParams);

        ActionUtils.checkForErrors(aResult);

        if (isInlineDialogMode())
        {
            return returnComplete();
        }

        return getRedirect("/browse/" + issue.getString("key"));
    }

    protected FieldScreenRenderer getFieldScreenRenderer()
    {
        if (fieldScreenRenderer == null)
        {
            fieldScreenRenderer = fieldScreenRendererFactory.getFieldScreenRenderer(getRemoteUser(), getIssueObject(), IssueOperations.EDIT_ISSUE_OPERATION, false);
        }

        return fieldScreenRenderer;
    }

    @Override
    public Map<String, Object> getDisplayParams()
    {
        final Map<String, Object> displayParams = new HashMap<String, Object>(super.getDisplayParams());
        displayParams.put("theme", "aui");
        return displayParams;
    }
}
