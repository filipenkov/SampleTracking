package com.atlassian.jira.web.action.issue;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.IssueInputParametersImpl;
import com.atlassian.jira.issue.customfields.OperationContextImpl;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderer;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.web.SessionKeys;
import com.atlassian.jira.web.util.SubTaskQuickCreationConfig;
import com.opensymphony.util.TextUtils;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ActionContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CreateSubTaskIssueDetails extends CreateIssueDetails
{
    public static final String SUB_TASK_LINK_TYPE_NAME = "jira_subtask_link";
    public static final String SUB_TASK_LINK_TYPE_STYLE = "jira_subtask";
    public static final String SUB_TASK_LINK_TYPE_INWARD_NAME = "jira_subtask_inward";
    public static final String SUB_TASK_LINK_TYPE_OUTWARD_NAME = "jira_subtask_outward";

    private final ConstantsManager constantsManager;
    private final SubTaskManager subTaskManager;
    private final SubTaskQuickCreationConfig subTaskQuickCreationConfig;
    private final FieldScreenRendererFactory fieldScreenRendererFactory;
    private final IssueService issueService;

    private Long parentIssueId;
    private boolean quickCreate;
    private boolean quickCreateValidation;
    private boolean fieldErrorsPresent;
    private boolean requiresLogin;
    private Collection providedFields;
    private FieldScreenRenderer quickCreationFieldScreenRenderer;

    public CreateSubTaskIssueDetails(ConstantsManager constantsManager, SubTaskManager subTaskManager, IssueCreationHelperBean issueCreationHelperBean,
                                     SubTaskQuickCreationConfig subTaskQuickCreationConfig, FieldScreenRendererFactory fieldScreenRendererFactory,
                                     IssueFactory issueFactory, IssueService issueService)
    {
        super(issueFactory, issueCreationHelperBean, issueService);
        this.constantsManager = constantsManager;
        this.subTaskManager = subTaskManager;
        this.subTaskQuickCreationConfig = subTaskQuickCreationConfig;
        this.fieldScreenRendererFactory = fieldScreenRendererFactory;
        this.issueService = issueService;
        // Do not display issue level security field for sub-tasks as it is inherited from the parent issue
        getIgnoreFieldIds().add(IssueFieldConstants.SECURITY);
        this.quickCreateValidation = false;
    }

    protected void doValidation()
    {
        Long parentIssueId = getParentIssueId();

        // Check that we have a parent issue id
        if (parentIssueId == null)
        {
            addErrorMessage(getText("admin.errors.issues.parent.issue.id.not.set"));
            return;
        }

        final Issue parent = getIssueManager().getIssueObject(parentIssueId);
        if ((parent != null) && !parent.isEditable())
        {
            addErrorMessage(getText("admin.errors.issues.parent.issue.not.editable"));
        }

        getIssueObject().setParentId(getParentIssueId());
        getIssueObject().setProject(getProject());
        getIssueObject().setIssueTypeId(getIssuetype());

        // Is the form submitted from 'quick create' (directly from view issue page)
        if (isQuickCreate())
        {
            // If so, we need to check is there are any mandatory field values missing
            if (hasMandatoryFields())
            {
                // JRA-6061 - populate all fields with defaults - except for the fields in the 'Provided' collection.
                // This allows for possible future changes to the 'quick create' UI - where other fields may be added
                // and will already hold a default or user value at this point. The 'getProvidedFields' method should
                // be updated to reflect any field additions to the sub-task 'quick create' UI.
                populateFieldHolderWithDefaults(getIssueObject(), getProvidedFields());

                // If we have required fields that are not on the sceen, populate the values holder with all the field values that we have been provided with
                for (Iterator iterator = getProvidedFields().iterator(); iterator.hasNext();)
                {
                    String fieldId = (String) iterator.next();
                    OrderableField field = (OrderableField) getField(fieldId);
                    field.populateFromParams(getFieldValuesHolder(), ActionContext.getParameters());
                }

                fieldErrorsPresent = true;
                return;
            }
            else
            {
                // Remember that we are creating an issue from the quick create form and therefore should
                // only check the fields that the form contains.
                quickCreateValidation = true;

                final IssueInputParameters issueInputParameters = new IssueInputParametersImpl(ActionContext.getParameters());
                issueInputParameters.setProvidedFields(getProvidedFields());
                // If not, then valiate the rest of the input
                this.validationResult = issueService.validateSubTaskCreate(getRemoteUser(), getParentIssueId(), issueInputParameters);
                 if (!this.validationResult.isValid())
                 {
                     addErrorCollection(this.validationResult.getErrorCollection());
                 }
                this.fieldValuesHolder = this.validationResult.getFieldValuesHolder();
                setIssueObject(validationResult.getIssue());
                // If we found a problem then disable quick creation validation, as we will
                // redirect to a normal Create screen that should show all its fields, and not just the ones
                // that are present on the quick creation form.
                if (invalidInput())
                    quickCreateValidation = false;
            }
        }
        else
        {
            final IssueInputParameters issueInputParameters = new IssueInputParametersImpl(ActionContext.getParameters());

            // If not, then run the create issue code
            this.validationResult = issueService.validateSubTaskCreate(getRemoteUser(), getParentIssueId(), issueInputParameters);
            if (!this.validationResult.isValid())
            {
                addErrorCollection(this.validationResult.getErrorCollection());
            }
            this.fieldValuesHolder = this.validationResult.getFieldValuesHolder();
            setIssueObject(validationResult.getIssue());

        }

        if (getReasons() != null && getReasons().contains(Reason.FORBIDDEN) && getLoggedInUser() == null)
        {
            requiresLogin = true;
        }

    }

    protected FieldScreenRenderer getFieldScreenRenderer()
    {
        if (quickCreateValidation)
        {
            if (quickCreationFieldScreenRenderer == null)
            {
                quickCreationFieldScreenRenderer = fieldScreenRendererFactory.getFieldScreenRenderer(new ArrayList(getProvidedFields()), getRemoteUser(), getIssueObject(), getIssueOperation());
            }

            return quickCreationFieldScreenRenderer;
        }
        else
        {
            return super.getFieldScreenRenderer();
        }

    }

    protected boolean hasMandatoryFields()
    {
        Collection providedFields = getProvidedFields();

        Collection requiredFieldScreenRenderItems = getFieldScreenRenderer().getRequiredFieldScreenRenderItems();

        // Note that we are looking through all the required fields that actually appear on the Create screen for the
        // sub task, and NOT through all the required fields for the sub-task. The reason for this is that, if we find
        // any required fields that are not present on the quick creation form and do appear on the Create screen, we do not
        // want to show any 'red' error messages to the user. We want to show a message that informs them that they should
        // fill in all the required fields.
        // If the required field does appear on the quick creation form we want to show the 'red' error message.
        // Similarly, if the field does not appear on the quick creation form, nor the create screen, we have a bigger
        // problem (the issue cannot be created at all) and we do want to show the 'red' error message.
        for (Iterator iterator = requiredFieldScreenRenderItems.iterator(); iterator.hasNext();)
        {
            FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem = (FieldScreenRenderLayoutItem) iterator.next();
            OrderableField field = fieldScreenRenderLayoutItem.getOrderableField();
            if (!providedFields.contains(field.getId()))
            {
                // Need to determine if the field does not have valid default.
                // Populate the values holder with the default value
                Map fieldValuesHolder = new HashMap();
                field.populateDefaults(fieldValuesHolder, getIssueObject());
                // Validate the default
                ErrorCollection errorCollection = new SimpleErrorCollection();
                OperationContextImpl operationContext = new OperationContextImpl(getIssueOperation(), fieldValuesHolder);
                field.validateParams(operationContext, errorCollection, this, getIssueObject(), fieldScreenRenderLayoutItem);
                // If there are errors then we have problem with a required field
                if (errorCollection.hasAnyErrors())
                    return true;
            }
        }

        return false;
    }

    protected Collection getProvidedFields()
    {
        if (providedFields == null)
        {
            providedFields = subTaskQuickCreationConfig.getFieldIds();
        }

        return providedFields;
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        // Have we found and probelms in validation stage with mandatory fields
        if (isFieldErrorsPresent())
        {
            // if so, we need to ask the user to enter all mandatory fields
            return INPUT;
        }
        else
        {
            // If not, then run the create issue code
            return super.doExecute();
        }
    }

    protected Collection getIssueTypes()
    {
        return constantsManager.getSubTaskIssueTypes();
    }

    protected String doPostCreationTasks() throws Exception
    {
        if (invalidInput())
        {
            return getResult();
        }

        // Create a link to the parent issue
        createSubTaskLink();

        // In case the sub-task was created a 'quick way' (directly from the view issue page of the parent issue)
        // record the sub-task issue type
        recordHistoryIssueType();

        if (TextUtils.stringSet(getViewIssueKey()))
        {
            try
            {
                GenericValue viewIssue = getIssueManager().getIssue(getViewIssueKey());
                if (ManagerFactory.getPermissionManager().hasPermission(Permissions.BROWSE, viewIssue, getRemoteUser()))
                {
                    return getRedirect("/browse/" + getViewIssueKey() + "#summary");
                }
            }
            catch (GenericEntityException e)
            {
                log.error("Subtask created successfully, but return view issue key '" + getViewIssueKey() + "' is an invalid issue");
            }
        }
        return super.doPostCreationTasks();
    }

    protected void recordHistoryIssueType()
    {
        ActionContext.getSession().put(SessionKeys.USER_HISTORY_SUBTASK_ISSUETYPE, getIssuetype());
    }

    private void createSubTaskLink() throws GenericEntityException, CreateException
    {
        final GenericValue parentIssue = getIssueManager().getIssue(getParentIssueId());
        subTaskManager.createSubTaskIssueLink(parentIssue, getIssue(), getRemoteUser());
    }

    public String getParentIssueKey()
    {
        try
        {
            final GenericValue parentIssue = getParentIssue();
            if (parentIssue != null)
            {
                return parentIssue.getString("key");
            }
        }
        catch (GenericEntityException e)
        {
            log.error("Error occurred while retrieving parent issue.", e);
            log.error("Error occurred while retrieving parent issue. Please see log for more detail.");
        }

        return null;
    }

    /**
     * Gets the relative path to the parent issue.
     * It does not include the {@link javax.servlet.http.HttpServletRequest#getContextPath() context path}.
     * @return The relative path to the parent issue.
     */
    public String getParentIssuePath()
    {
        return "/browse/" + getParentIssueKey();
    }

    private GenericValue getParentIssue() throws GenericEntityException
    {
        return getIssueManager().getIssue(getParentIssueId());
    }

    public Long getParentIssueId()
    {
        return parentIssueId;
    }

    public void setParentIssueId(Long parentIssueId)
    {
        this.parentIssueId = parentIssueId;
    }

    public boolean isQuickCreate()
    {
        return quickCreate;
    }

    public boolean isFieldErrorsPresent()
    {
        return fieldErrorsPresent;
    }

    public void setQuickCreate(boolean quickCreate)
    {
        this.quickCreate = quickCreate;
    }

    public boolean isRequiresLogin()
    {
        return requiresLogin;
    }

    public Map<String, Object> getDisplayParams()
    {
        final Map<String, Object> displayParams = new HashMap<String, Object>();
        displayParams.put("theme", "aui");
        return displayParams;
    }
}
