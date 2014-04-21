package com.atlassian.jira.web.action.issue;

import com.atlassian.core.action.ActionUtils;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.operation.IssueOperation;
import com.atlassian.jira.issue.operation.IssueOperations;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import webwork.action.ActionContext;
import webwork.dispatcher.ActionResult;

import java.util.HashMap;
import java.util.Map;

/**
 * Action to update an issue. This differs from {@link EditIssue} in that it only tries to updates the fields that's been
 * passed down. This action has been added to make it easier to update an issue in-line.
 *
 * @deprecated Use REST API instead. Since v5.0.
 */
public class UpdateIssueFields extends AbstractViewIssue implements OperationContext
{
    // ------------------------------------------------------------------------------------------------------- Constants
    // ------------------------------------------------------------------------------------------------- Type Properties
    // ---------------------------------------------------------------------------------------------------- Dependencies
    private final UpdateFieldsHelperBean updateFieldsHelperBean;
    private final FieldManager fieldManager;
    private final Map fieldValuesHolder;

    // ---------------------------------------------------------------------------------------------------- Constructors

    public UpdateIssueFields(SubTaskManager subTaskManager, UpdateFieldsHelperBean updateFieldsHelperBean, FieldManager fieldManager)
    {
        super(subTaskManager);
        this.updateFieldsHelperBean = updateFieldsHelperBean;
        this.fieldManager = fieldManager;
        this.fieldValuesHolder = new HashMap();
    }

    // -------------------------------------------------------------------------------------------------- Action Methods
    protected void doValidation()
    {
        updateFieldsHelperBean.validate(getIssueObject(), this, ActionContext.getParameters(), getLoggedInUser(), this, this);
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        final ActionResult actionResult = updateFieldsHelperBean.updateIssue(getIssueObject(), this, getLoggedInUser(), this, this);

        ActionUtils.checkForErrors(actionResult);

        return getRedirect("/secure/MyJiraHome.jspa");
    }

    // --------------------------------------------------------------------------------------------- View Helper Methods
    public Field getField(String id)
    {
        return fieldManager.getField(id);
    }
    // -------------------------------------------------------------------------------------- Basic accessors & mutators

    public Map getFieldValuesHolder()
    {
        return fieldValuesHolder;
    }

    public IssueOperation getIssueOperation()
    {
        return IssueOperations.EDIT_ISSUE_OPERATION;
    }
}
