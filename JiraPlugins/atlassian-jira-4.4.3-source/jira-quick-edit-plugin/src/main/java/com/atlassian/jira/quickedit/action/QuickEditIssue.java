package com.atlassian.jira.quickedit.action;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.IssueInputParametersImpl;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.fields.CommentSystemField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderTab;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderer;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.operation.IssueOperation;
import com.atlassian.jira.issue.operation.IssueOperations;
import com.atlassian.jira.quickedit.rest.api.field.FieldTab;
import com.atlassian.jira.quickedit.rest.api.field.QuickEditField;
import com.atlassian.jira.quickedit.rest.api.field.QuickEditFields;
import com.atlassian.jira.quickedit.user.UserPreferencesStore;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.plugins.rest.common.json.DefaultJaxbJsonMarshaller;
import com.atlassian.plugins.rest.common.json.JaxbJsonMarshaller;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang.StringUtils;
import webwork.action.ActionContext;

import java.util.HashMap;
import java.util.Map;

/**
 * A webwork action to produce JSON with field edit html.  This is an action and not a REST resource mainly because our
 * fields API is still so heavily tied to webwork.  All methods on this action should return JSON content.
 *
 * @since 5.0
 */
public class QuickEditIssue extends JiraWebActionSupport implements OperationContext
{
    private final FieldScreenRendererFactory fieldScreenRendererFactory;
    private final UserPreferencesStore userPreferencesStore;
    private final PermissionManager permissionManager;
    private final IssueService issueService;

    private FieldScreenRenderer fieldScreenRenderer;

    private final Map<String, Object> fieldValuesHolder = new HashMap<String, Object>();
    private boolean retainValues;
    private Long issueId;
    private QuickEditFields fields;
    private ErrorCollection errors;
    private IssueService.UpdateValidationResult updateValidationResult;

    public QuickEditIssue(final IssueService issueService, final FieldScreenRendererFactory fieldScreenRendererFactory,
            final UserPreferencesStore userPreferencesStore, final PermissionManager permissionManager)
    {
        this.issueService = issueService;
        this.fieldScreenRendererFactory = fieldScreenRendererFactory;
        this.userPreferencesStore = userPreferencesStore;
        this.permissionManager = permissionManager;
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
        FieldTab firstTab = null;
        for (final FieldScreenRenderTab fieldScreenRenderTab : getFieldScreenRenderer(issue).getFieldScreenRenderTabs())
        {
            final FieldTab currentTab = new FieldTab(fieldScreenRenderTab.getName(), fieldScreenRenderTab.getPosition());
            if(firstTab == null && currentTab.getPosition() == 0 )
            {
                firstTab = currentTab;
            }
            for (final FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem : fieldScreenRenderTab.getFieldScreenRenderLayoutItems())
            {
                if (fieldScreenRenderLayoutItem.isShow(issue))
                {
                    if (retainValues)
                    {
                        //this gets used when switching from the full edit form back to quick edit.  We need to keep the values that were
                        //posted with this change.
                        fieldScreenRenderLayoutItem.getOrderableField().populateFromParams(getFieldValuesHolder(), ActionContext.getParameters());
                    }
                    else
                    {
                        fieldScreenRenderLayoutItem.populateFromIssue(getFieldValuesHolder(), issue);
                    }

                    Map<String, Object> displayParams = new HashMap<String, Object>();
                    displayParams.put("theme", "aui");
                    displayParams.put("noHeader", "true");

                    final String id = fieldScreenRenderLayoutItem.getFieldLayoutItem().getOrderableField().getId();
                    final String text = getText(fieldScreenRenderLayoutItem.getFieldLayoutItem().getOrderableField().getNameKey());
                    final boolean required = fieldScreenRenderLayoutItem.isRequired();
                    final String editHtml = fieldScreenRenderLayoutItem.getEditHtml(this, this, issue, displayParams);

                    //some custom fields may not have an edit view at all (JRADEV-7032)
                    if (StringUtils.isNotBlank(editHtml))
                    {
                        fieldsBuilder.addField(new QuickEditField(id,
                                text,
                                required,
                                editHtml.trim(),
                                currentTab));
                    }
                }
            }
        }

        //JRADEV-6908: The comment field is special and will always be there on edit!
        if(permissionManager.hasPermission(Permissions.COMMENT_ISSUE, issue, getLoggedInUser()))
        {
            final CommentSystemField commentSystemField = (CommentSystemField) getField(IssueFieldConstants.COMMENT);
            final FieldLayoutItem commentFieldLayoutItem = getFieldScreenRenderer(issue).getFieldScreenRenderLayoutItem(commentSystemField).getFieldLayoutItem();
            fieldsBuilder.addField(new QuickEditField(commentSystemField.getId(), commentSystemField.getName(), false,
                    commentSystemField.getEditHtml(commentFieldLayoutItem, this, this, issue, MapBuilder.<String, Object>build("noHeader", "true", "theme", "aui")), firstTab));
        }

        fields = fieldsBuilder.build(userPreferencesStore.getEditUserPreferences(getLoggedInUser()));

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
        issueInputParameters.setRetainExistingValuesWhenParameterNotProvided(false);
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

    private FieldScreenRenderer getFieldScreenRenderer(final Issue issue)
    {
        if (fieldScreenRenderer == null)
        {
            fieldScreenRenderer = fieldScreenRendererFactory.getFieldScreenRenderer(getLoggedInUser(), issue, IssueOperations.EDIT_ISSUE_OPERATION, false);
        }

        return fieldScreenRenderer;
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

    @Override
    public IssueOperation getIssueOperation()
    {
        return IssueOperations.EDIT_ISSUE_OPERATION;
    }


}
