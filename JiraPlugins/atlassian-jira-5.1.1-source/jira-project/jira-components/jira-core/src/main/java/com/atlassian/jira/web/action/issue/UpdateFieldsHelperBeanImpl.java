package com.atlassian.jira.web.action.issue;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.action.ActionNames;
import com.atlassian.jira.exception.IssueNotFoundException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderTab;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderer;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.operation.IssueOperations;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.workflow.WorkflowManager;
import org.apache.log4j.Logger;
import webwork.dispatcher.ActionResult;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class UpdateFieldsHelperBeanImpl implements UpdateFieldsHelperBean
{
    private static final Logger log = Logger.getLogger(UpdateFieldsHelperBeanImpl.class);

    private final PermissionManager permissionManager;
    private final WorkflowManager workflowManager;
    private final JiraAuthenticationContext authenticationContext;
    private FieldScreenRendererFactory fieldScreenRendererFactory;

    public UpdateFieldsHelperBeanImpl(PermissionManager permissionManager, WorkflowManager workflowManager, JiraAuthenticationContext authenticationContext, FieldScreenRendererFactory fieldScreenRendererFactory)
    {
        this.permissionManager = permissionManager;
        this.workflowManager = workflowManager;
        this.authenticationContext = authenticationContext;
        this.fieldScreenRendererFactory = fieldScreenRendererFactory;
    }

    public ActionResult updateIssue(MutableIssue issueObject,
                                    OperationContext operationContext,
                                    User user,
                                    ErrorCollection errors,
                                    I18nHelper i18n) throws Exception
    {
        Map fieldValuesHolder = operationContext.getFieldValuesHolder();
        final FieldScreenRenderer fieldScreenRenderer = getFieldScreenRenderer(user, issueObject);
        for (Iterator iterator = fieldScreenRenderer.getFieldScreenRenderTabs().iterator(); iterator.hasNext();)
        {
            FieldScreenRenderTab fieldScreenRenderTab = (FieldScreenRenderTab) iterator.next();
            for (Iterator iterator1 = fieldScreenRenderTab.getFieldScreenRenderLayoutItemsForProcessing().iterator(); iterator1.hasNext();)
            {
                FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem = (FieldScreenRenderLayoutItem) iterator1.next();
                if (fieldScreenRenderLayoutItem.isShow(issueObject))
                {
                    final OrderableField orderableField = fieldScreenRenderLayoutItem.getOrderableField();
                    if (fieldValuesHolder.containsKey(orderableField.getId()))
                    {
                        orderableField.updateIssue(fieldScreenRenderLayoutItem.getFieldLayoutItem(), issueObject, fieldValuesHolder);
                    }
                }
            }
        }

        Map newActionParams = EasyMap.build("issue", issueObject.getGenericValue(),
                                            "issueObject", issueObject,
                                            "remoteUser", user);

        return CoreFactory.getActionDispatcher().execute(ActionNames.ISSUE_UPDATE, newActionParams);
    }

    public void validate(Issue issueObject,
                         OperationContext operationContext,
                         Map actionParams,
                         User user,
                         ErrorCollection errors,
                         I18nHelper i18n)
    {
        if (!isEditable(issueObject))
        {
            errors.addErrorMessage(authenticationContext.getI18nHelper().getText("editissue.error.no.edit.permission"));
            return;
        }

        final FieldScreenRenderer fieldScreenRenderer = getFieldScreenRenderer(user, issueObject);
        for (Iterator iterator = fieldScreenRenderer.getFieldScreenRenderTabs().iterator(); iterator.hasNext();)
        {
            FieldScreenRenderTab fieldScreenRenderTab = (FieldScreenRenderTab) iterator.next();
            for (Iterator iterator1 = fieldScreenRenderTab.getFieldScreenRenderLayoutItemsForProcessing().iterator(); iterator1.hasNext();)
            {
                FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem = (FieldScreenRenderLayoutItem) iterator1.next();
                if (fieldScreenRenderLayoutItem.isShow(issueObject))
                {
                    final OrderableField orderableField = fieldScreenRenderLayoutItem.getOrderableField();
                    if (actionParams.containsKey(orderableField.getId()))
                    {
                        orderableField.populateFromParams(operationContext.getFieldValuesHolder(), actionParams);
                        orderableField.validateParams(operationContext, errors, i18n, issueObject, fieldScreenRenderLayoutItem);
                    }
                }
            }
        }
    }

    public boolean isEditable(Issue issue)
    {
        // preconditions
        if (issue == null)
        {
            throw new IssueNotFoundException("Issue unexpectedly null");
        }

        boolean hasPermission = permissionManager.hasPermission(Permissions.EDIT_ISSUE, issue, authenticationContext.getLoggedInUser());
        if (hasPermission)
        {
            return issue.isEditable();
        }
        return false;
    }

    public List getFieldsForEdit(User user, Issue issueObject)
    {
        final List fields = new ArrayList();

        FieldScreenRenderer fieldScreenRenderer = getFieldScreenRenderer(user, issueObject);

        for (Iterator iterator = fieldScreenRenderer.getFieldScreenRenderTabs().iterator(); iterator.hasNext();)
        {
            FieldScreenRenderTab fieldScreenRenderTab = (FieldScreenRenderTab) iterator.next();
            for (Iterator iterator1 = fieldScreenRenderTab.getFieldScreenRenderLayoutItems().iterator(); iterator1.hasNext();)
            {
                FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem = (FieldScreenRenderLayoutItem) iterator1.next();
                if (fieldScreenRenderLayoutItem.isShow(issueObject))
                {
                    fields.add(fieldScreenRenderLayoutItem.getOrderableField());
                }
            }
        }
        return fields;
    }

    public boolean isFieldValidForEdit(User user, String fieldId, Issue issueObject)
    {
        if (fieldId != null)
        {
            FieldScreenRenderer fieldScreenRenderer = getFieldScreenRenderer(user, issueObject);
            for (Iterator iterator = fieldScreenRenderer.getFieldScreenRenderTabs().iterator(); iterator.hasNext();)
            {
                FieldScreenRenderTab fieldScreenRenderTab = (FieldScreenRenderTab) iterator.next();
                for (Iterator iterator1 = fieldScreenRenderTab.getFieldScreenRenderLayoutItems().iterator(); iterator1.hasNext();)
                {
                    FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem = (FieldScreenRenderLayoutItem) iterator1.next();
                    if (fieldScreenRenderLayoutItem.isShow(issueObject) && fieldId.equals(fieldScreenRenderLayoutItem.getOrderableField().getId()))
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private FieldScreenRenderer getFieldScreenRenderer(User user, final Issue issueObject)
    {
        return fieldScreenRendererFactory.getFieldScreenRenderer(user, issueObject, IssueOperations.EDIT_ISSUE_OPERATION, false);
    }
}
