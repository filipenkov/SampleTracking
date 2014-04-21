package com.atlassian.jira.quickedit.action;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.IssueInputParametersImpl;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.fields.rest.FieldHtmlFactory;
import com.atlassian.jira.issue.fields.rest.json.beans.FieldHtmlBean;
import com.atlassian.jira.issue.operation.IssueOperation;
import com.atlassian.jira.issue.operation.IssueOperations;
import com.atlassian.jira.quickedit.rest.api.field.QuickEditFields;
import com.atlassian.jira.quickedit.user.UserPreferencesStore;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.UserIssueHistoryManager;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.plugins.rest.common.json.DefaultJaxbJsonMarshaller;
import com.atlassian.plugins.rest.common.json.JaxbJsonMarshaller;
import org.apache.commons.httpclient.HttpStatus;
import webwork.action.ActionContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A webwork action to produce JSON with field edit html.  This is an action and not a REST resource mainly because our
 * fields API is still so heavily tied to webwork.  All methods on this action should return JSON content.
 *
 * @since 5.0
 */
public class QuickEditIssue extends JiraWebActionSupport implements OperationContext
{
    private final UserPreferencesStore userPreferencesStore;
    private final UserIssueHistoryManager userIssueHistoryManager;
    private final FieldHtmlFactory fieldHtmlFactory;
    private final IssueService issueService;

    private final Map<String, Object> fieldValuesHolder = new HashMap<String, Object>();
    private boolean retainValues;
    private Long issueId;
    private boolean singleFieldEdit = false;
    private QuickEditFields fields;
    private ErrorCollection errors;
    private IssueService.UpdateValidationResult updateValidationResult;

    public QuickEditIssue(final IssueService issueService, final UserPreferencesStore userPreferencesStore,
            final UserIssueHistoryManager userIssueHistoryManager, final FieldHtmlFactory fieldHtmlFactory)
    {
        this.issueService = issueService;
        this.userPreferencesStore = userPreferencesStore;
        this.userIssueHistoryManager = userIssueHistoryManager;
        this.fieldHtmlFactory = fieldHtmlFactory;
    }

    public String doDefault() throws Exception
    {
        ActionContext.getResponse().setContentType("application/json");

        final IssueService.IssueResult result = issueService.getIssue(getLoggedInUser(), issueId);
        final Issue issue = result.getIssue();
        if (!result.isValid() || result.getIssue() == null)
        {
            this.errors = ErrorCollection.of(result.getErrorCollection());
            setReturnCode();
            return ERROR;
        }

        if (!issueService.isEditable(issue, getLoggedInUser()))
        {
            this.errors = ErrorCollection.of(getText("editissue.error.no.edit.permission"));
            setReturnCode();
            return ERROR;
        }

        final QuickEditFields.Builder fieldsBuilder = new QuickEditFields.Builder();
        List<FieldHtmlBean> editFields = fieldHtmlFactory.getEditFields(getLoggedInUser(), this, this, issue, retainValues);
        fieldsBuilder.addFields(editFields);

        fields = fieldsBuilder.build(userPreferencesStore.getEditUserPreferences(getLoggedInUser()), getXsrfToken());

        return "json";
    }

    private void setReturnCode()
    {
        if (getLoggedInUser() == null)
        {
            ActionContext.getResponse().setStatus(HttpStatus.SC_UNAUTHORIZED);
        }
        else
        {
            ActionContext.getResponse().setStatus(HttpStatus.SC_BAD_REQUEST);
        }
    }

    protected void doValidation()
    {
        ActionContext.getResponse().setContentType("application/json");

        final IssueService.IssueResult result = issueService.getIssue(getLoggedInUser(), issueId);
        final Issue issue = result.getIssue();
        if (!result.isValid() || result.getIssue() == null)
        {
            addErrorCollection(result.getErrorCollection());
            this.errors = ErrorCollection.of(result.getErrorCollection());
            ActionContext.getResponse().setStatus(HttpStatus.SC_BAD_REQUEST);
            return;
        }

        final IssueInputParameters issueInputParameters = new IssueInputParametersImpl(ActionContext.getParameters());
        if(singleFieldEdit)
        {
            issueInputParameters.setRetainExistingValuesWhenParameterNotProvided(singleFieldEdit, true);
        }
        else
        {
            issueInputParameters.setRetainExistingValuesWhenParameterNotProvided(singleFieldEdit);
        }
        updateValidationResult = issueService.validateUpdate(getLoggedInUser(), issue.getId(), issueInputParameters);
        setFieldValuesHolder(updateValidationResult.getFieldValuesHolder());
        if (!updateValidationResult.isValid())
        {
            addErrorCollection(updateValidationResult.getErrorCollection());
            this.errors = ErrorCollection.of(updateValidationResult.getErrorCollection());
            ActionContext.getResponse().setStatus(HttpStatus.SC_BAD_REQUEST);
        }
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        try
        {
            final IssueService.IssueResult issueResult = issueService.update(getLoggedInUser(), updateValidationResult);
            if (!issueResult.isValid())
            {
                addErrorCollection(issueResult.getErrorCollection());
                this.errors = ErrorCollection.of(issueResult.getErrorCollection());
                ActionContext.getResponse().setStatus(HttpStatus.SC_BAD_REQUEST);
                return ERROR;
            }

            userIssueHistoryManager.addIssueToHistory(getLoggedInUser(), issueResult.getIssue());

            //200 response is good enough.
            return NONE;
        }
        catch (Throwable e)
        {
            addErrorMessage(getText("admin.errors.issues.exception.occured", e));
            log.error("Exception occurred editing issue: " + e, e);
            this.errors = ErrorCollection.of(getText("admin.errors.issues.exception.occured", e));
            ActionContext.getResponse().setStatus(HttpStatus.SC_BAD_REQUEST);
            return ERROR;
        }
    }

    public Long getIssueId()
    {
        return issueId;
    }

    public void setIssueId(final Long issueId)
    {
        this.issueId = issueId;
    }

    public String getFieldsJson()
    {
        final JaxbJsonMarshaller marshaller = new DefaultJaxbJsonMarshaller();
        return marshaller.marshal(fields);
    }

    public String getErrorJson()
    {
        final JaxbJsonMarshaller marshaller = new DefaultJaxbJsonMarshaller();
        return marshaller.marshal(errors);
    }

    @Override
    public Map getFieldValuesHolder()
    {
        return fieldValuesHolder;
    }

    public void setFieldValuesHolder(final Map<String, Object> fieldValuesHolder)
    {
        this.fieldValuesHolder.clear();
        this.fieldValuesHolder.putAll(fieldValuesHolder);
    }

    public boolean isRetainValues()
    {
        return retainValues;
    }

    public void setRetainValues(final boolean retainValues)
    {
        this.retainValues = retainValues;
    }

    public boolean isSingleFieldEdit()
    {
        return singleFieldEdit;
    }

    public void setSingleFieldEdit(final boolean singleFieldEdit)
    {
        this.singleFieldEdit = singleFieldEdit;
    }

    @Override
    public IssueOperation getIssueOperation()
    {
        return IssueOperations.EDIT_ISSUE_OPERATION;
    }

}
