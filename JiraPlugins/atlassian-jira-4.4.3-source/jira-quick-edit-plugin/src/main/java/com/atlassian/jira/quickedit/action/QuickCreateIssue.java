package com.atlassian.jira.quickedit.action;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueInputParametersImpl;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.fields.IssueTypeSystemField;
import com.atlassian.jira.issue.fields.ProjectSystemField;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderTab;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderer;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.operation.IssueOperation;
import com.atlassian.jira.issue.operation.IssueOperations;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.quickedit.rest.api.field.FieldTab;
import com.atlassian.jira.quickedit.rest.api.field.IssueBean;
import com.atlassian.jira.quickedit.rest.api.field.QuickEditField;
import com.atlassian.jira.quickedit.rest.api.field.QuickEditFields;
import com.atlassian.jira.quickedit.user.UserPreferencesStore;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.UserProjectHistoryManager;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.SessionKeys;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.web.action.issue.IssueCreationHelperBean;
import com.atlassian.plugins.rest.common.json.DefaultJaxbJsonMarshaller;
import com.atlassian.plugins.rest.common.json.JaxbJsonMarshaller;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import webwork.action.ActionContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A webwork action to produce JSON with field create html.  This is an action and not a REST resource mainly because
 * our fields API is still so heavily tied to webwork.  All methods on this action should return JSON content.
 *
 * @since 5.0
 */
public class QuickCreateIssue extends JiraWebActionSupport implements OperationContext
{
    private final UserPreferencesStore userPreferencesStore;
    private final UserProjectHistoryManager userProjectHistoryManager;
    private final ApplicationProperties applicationProperties;
    private final PermissionManager permissionManager;
    private final IssueTypeSchemeManager issueTypeSchemeManager;
    private final IssueFactory issueFactory;
    private final IssueCreationHelperBean issueCreationHelperBean;
    private final IssueService issueService;
    private FieldScreenRenderer fieldScreenRenderer;

    private final Map<String, Object> fieldValuesHolder = new HashMap<String, Object>();
    private Long pid;
    private boolean retainValues;
    private IssueBean issue;
    private String issuetype;
    private List<String> fieldsToRetain;
    private MutableIssue issueObject = null;
    private String createdIssueKey;

    private QuickEditFields fields;
    private ErrorCollection errors;
    private IssueService.CreateValidationResult validationResult;

    public QuickCreateIssue(final IssueFactory issueFactory, final IssueCreationHelperBean issueCreationHelperBean,
            final IssueService issueService, final UserPreferencesStore userPreferencesStore,
            final UserProjectHistoryManager userProjectHistoryManager, final ApplicationProperties applicationProperties,
            final PermissionManager permissionManager, final IssueTypeSchemeManager issueTypeSchemeManager)
    {
        this.issueFactory = issueFactory;
        this.issueCreationHelperBean = issueCreationHelperBean;
        this.issueService = issueService;
        this.userPreferencesStore = userPreferencesStore;
        this.userProjectHistoryManager = userProjectHistoryManager;
        this.applicationProperties = applicationProperties;
        this.permissionManager = permissionManager;
        this.issueTypeSchemeManager = issueTypeSchemeManager;
    }

    public String doDefault() throws Exception
    {
        ActionContext.getResponse().setContentType("application/json");
        issueCreationHelperBean.validateLicense(this, this);
        if (hasAnyErrors())
        {
            this.errors = ErrorCollection.of(this);
            setReturnCode();
            return ERROR;
        }

        final Collection<Project> projects = permissionManager.getProjectObjects(Permissions.CREATE_ISSUE, getLoggedInUser());
        if(projects.isEmpty())
        {
            addErrorMessage(getText(getLoggedInUser() == null ? "createissue.notloggedin":"createissue.projectnopermission"));
            this.errors = ErrorCollection.of(this);
            setReturnCode();
            return ERROR;
        }

        final MutableIssue newIssueObject = issueObject == null ? issueFactory.getIssue() : issueObject;

        //check project and issuetype are valid and that the user has permission to create an issue in this project!
        validateIssueTypeAndProject(newIssueObject);
        if (hasAnyErrors())
        {
            this.errors = ErrorCollection.of(this);
            setReturnCode();
            return ERROR;
        }

        // NOTE: this is passing null because the issueGV is null at this point and we can't
        // resolve a fieldLayoutItem to pass. For these two fields we are fine, since they are not renderable
        final ProjectSystemField projectField = (ProjectSystemField) getField(IssueFieldConstants.PROJECT);
        final IssueTypeSystemField issueTypeField = (IssueTypeSystemField) getField(IssueFieldConstants.ISSUE_TYPE);

        projectField.updateIssue(null, newIssueObject, getFieldValuesHolder());
        issueTypeField.updateIssue(null, newIssueObject, getFieldValuesHolder());

        // Store last issue type, so it can be set as the default in the next issue the user files
        recordHistoryIssueType(newIssueObject);

        // Store last project, so it can be set as the default in the next issue the user files
        setSelectedProjectId(newIssueObject.getProjectObject().getId());

        //populate custom field values holder with default values and construct JSON object
        final QuickEditFields.Builder fieldsBuilder = new QuickEditFields.Builder();
        fieldsBuilder.addField(new QuickEditField(projectField.getId(), projectField.getName(), true,
                projectField.getCreateHtml(null, this, this, newIssueObject,
                        MapBuilder.<String, Object>build("noHeader", "true", "theme", "aui")), null));

        fieldsBuilder.addField(new QuickEditField(issueTypeField.getId(), issueTypeField.getName(), true,
                issueTypeField.getCreateHtml(null, this, this, newIssueObject,
                        MapBuilder.<String, Object>build("noHeader", "true", "theme", "aui")), null));

        for (final FieldScreenRenderTab fieldScreenRenderTab : getFieldScreenRenderer(newIssueObject).getFieldScreenRenderTabs())
        {
            final FieldTab currentTab = new FieldTab(fieldScreenRenderTab.getName(), fieldScreenRenderTab.getPosition());
            for (final FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem : fieldScreenRenderTab.getFieldScreenRenderLayoutItems())
            {
                String fieldId = fieldScreenRenderLayoutItem.getOrderableField().getId();
                // If the field is not in the excluded list or not Project or Issue Type
                if (!IssueFieldConstants.PROJECT.equals(fieldId) && !IssueFieldConstants.ISSUE_TYPE.equals(fieldId) &&
                        fieldScreenRenderLayoutItem.isShow(newIssueObject))
                {
                    if (retainValues)
                    {
                        //used when switching from full create back to quick create to keep the field values.
                        fieldScreenRenderLayoutItem.getOrderableField().populateFromParams(getFieldValuesHolder(), ActionContext.getParameters());
                    }
                    else if (fieldsToRetain == null || !fieldsToRetain.contains(fieldId))
                    {
                        //if this field is not meant to retain its value from a previous create remove it from the
                        //fieldValuesHolder.  Then populate it with the field's default value.
                        getFieldValuesHolder().remove(fieldId);
                        fieldScreenRenderLayoutItem.populateDefaults(getFieldValuesHolder(), newIssueObject);
                    }

                    final String editHtml = fieldScreenRenderLayoutItem.getEditHtml(this, this, newIssueObject, MapBuilder.<String, Object>build("noHeader", "true", "theme", "aui"));
                    //some custom fields may not have an edit view at all (JRADEV-7032)
                    if (StringUtils.isNotBlank(editHtml))
                    {
                        fieldsBuilder.addField(new QuickEditField(fieldScreenRenderLayoutItem.getFieldLayoutItem().getOrderableField().getId(),
                                getText(fieldScreenRenderLayoutItem.getFieldLayoutItem().getOrderableField().getNameKey()),
                                fieldScreenRenderLayoutItem.isRequired(),
                                editHtml.trim(),
                                currentTab));
                    }
                }
            }
        }

        if(createdIssueKey != null)
        {
            fieldsBuilder.createdIssue(createdIssueKey);
        }

        fields = fieldsBuilder.build(userPreferencesStore.getCreateUserPreferences(getLoggedInUser()));

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

        validationResult = issueService.validateCreate(getLoggedInUser(), new IssueInputParametersImpl(ActionContext.getParameters()));
        this.issueObject = validationResult.getIssue();
        // We want to be able to repopulate the fields with their input values
        setFieldValuesHolder(validationResult.getFieldValuesHolder());
        if (!validationResult.isValid())
        {
            addErrorCollection(validationResult.getErrorCollection());
            this.errors = ErrorCollection.of(validationResult.getErrorCollection());
            ActionContext.getResponse().setStatus(HttpStatus.SC_BAD_REQUEST);
        }
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        try
        {
            IssueService.IssueResult issueResult = issueService.create(getLoggedInUser(), validationResult);
            if (!issueResult.isValid())
            {
                addErrorCollection(issueResult.getErrorCollection());
                this.errors = ErrorCollection.of(validationResult.getErrorCollection());
                ActionContext.getResponse().setStatus(HttpStatus.SC_BAD_REQUEST);
            }

            if (hasAnyErrors())
            {
                return ERROR;
            }

            if (fieldsToRetain != null) //TODO: || createMoreSubmitButton != null)
            {
                createdIssueKey = issueResult.getIssue().getKey();
                //reset the issue Object, fields and errors
                issueObject = null;
                fields = null;
                errors = null;

                return doDefault();
            }

            issue = new IssueBean(issueResult.getIssue().getKey(), issueResult.getIssue().getId());

            return "issue";
        }
        catch (Exception e)
        {
            log.error(e, e);
            addErrorMessage((e.getMessage() != null ? e.getMessage() : ExceptionUtils.getFullStackTrace(e)));
            this.errors = ErrorCollection.of(getText("admin.errors.issues.exception.occured", e));
            ActionContext.getResponse().setStatus(HttpStatus.SC_BAD_REQUEST);
            return ERROR;
        }
    }

    private void validateIssueTypeAndProject(MutableIssue issue)
    {
        // Most calls using the issue object will fail unless the issue object has the project and issue type are set
        try
        {
            issue.setProjectId(getSelectedProjectId());
            if(getPid() != null)
            {
                issue.setIssueTypeId(getSelectedIssueTypeId(getProjectManager().getProjectObj(getPid())));
            }
        }
        catch (IllegalArgumentException e)
        {
            //this may throw an exception if the project/issuetype ids are invalid. Doesn't matter, the validation below
            //will pick it up anyways.
        }
        validateProject(issue);
        validateIssueType(issue);
    }

    private void validateProject(final Issue issue)
    {
        // Check that the project selected is a valid one
        ProjectSystemField projectField = (ProjectSystemField) getField(IssueFieldConstants.PROJECT);
        projectField.validateParams(this, this, this, issue, null);
    }

    private void validateIssueType(final Issue issue)
    {
        IssueTypeSystemField issueTypeField = (IssueTypeSystemField) getField(IssueFieldConstants.ISSUE_TYPE);
        issueTypeField.validateParams(this, this, this, issue, null);
    }

    private Long getSelectedProjectId()
    {
        Long pid = null;
        if (getPid() != null)
        {
            pid = getPid();
        }
        else
        {
            final Project currentProject = userProjectHistoryManager.getCurrentProject(Permissions.CREATE_ISSUE, getLoggedInUser());
            if (currentProject != null)
            {
                pid = currentProject.getId();
            }
        }

        //didn't get one from the user history or had one set explicitly. Lets just fall back to the first project in the list!
        if (pid == null)
        {
            final Collection<Project> projects = permissionManager.getProjectObjects(Permissions.CREATE_ISSUE, getLoggedInUser());
            if (!projects.isEmpty())
            {
                pid = projects.iterator().next().getId();
            }
        }

        getFieldValuesHolder().put(IssueFieldConstants.PROJECT, pid);
        setPid(pid);
        return pid;
    }

    private String getSelectedIssueTypeId(final Project project)
    {
        String issueType;
        if (getIssuetype() != null)
        {
            issueType = getIssuetype();
        }
        else if (ActionContext.getSession().containsKey(SessionKeys.USER_HISTORY_ISSUETYPE))
        {
            issueType = (String) ActionContext.getSession().get(SessionKeys.USER_HISTORY_ISSUETYPE);
        }
        else
        {
            issueType = applicationProperties.getString(APKeys.JIRA_CONSTANT_DEFAULT_ISSUE_TYPE);
        }

        final List<String> issueTypeIdsForProject = getIssueTypeIds(project);
        if(!issueTypeIdsForProject.contains(issueType))
        {
            issueType = issueTypeIdsForProject.get(0);
        }

        getFieldValuesHolder().put(IssueFieldConstants.ISSUE_TYPE, issueType);
        setIssuetype(issueType);
        return issueType;
    }

    private List<String> getIssueTypeIds(final Project project)
    {
        final List<String> issueTypeIdsForProject = new ArrayList<String>();
        for (IssueType type : issueTypeSchemeManager.getIssueTypesForProject(project))
        {
            issueTypeIdsForProject.add(type.getId());
        }
        return issueTypeIdsForProject;
    }

    private void recordHistoryIssueType(Issue issue)
    {
        ActionContext.getSession().put(SessionKeys.USER_HISTORY_ISSUETYPE, issue.getIssueTypeObject().getId());
    }

    private FieldScreenRenderer getFieldScreenRenderer(Issue issue)
    {
        if (fieldScreenRenderer == null)
        {
            fieldScreenRenderer = issueCreationHelperBean.createFieldScreenRenderer(getLoggedInUser(), issue);
        }

        return fieldScreenRenderer;
    }

    public String getIssuetype()
    {
        return issuetype;
    }

    public void setIssuetype(final String issuetype)
    {
        this.issuetype = issuetype;
    }

    public Long getPid()
    {
        return pid;
    }

    public void setPid(final Long pid)
    {
        this.pid = pid;
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

    public String getIssueJson()
    {
        final JaxbJsonMarshaller marshaller = new DefaultJaxbJsonMarshaller();
        return marshaller.marshal(issue);
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

    @Override
    public IssueOperation getIssueOperation()
    {
        return IssueOperations.CREATE_ISSUE_OPERATION;
    }

    public boolean isRetainValues()
    {
        return retainValues;
    }

    public void setRetainValues(final boolean retainValues)
    {
        this.retainValues = retainValues;
    }

    public String[] getFieldsToRetain()
    {
        return fieldsToRetain.toArray(new String[fieldsToRetain.size()]);
    }

    public void setFieldsToRetain(final String[] fieldsToRetain)
    {
        this.fieldsToRetain = Arrays.asList(fieldsToRetain);
    }
}
