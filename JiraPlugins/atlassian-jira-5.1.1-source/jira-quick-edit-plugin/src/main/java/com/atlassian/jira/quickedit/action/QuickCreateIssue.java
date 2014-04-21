package com.atlassian.jira.quickedit.action;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueInputParametersImpl;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.fields.IssueTypeSystemField;
import com.atlassian.jira.issue.fields.ProjectSystemField;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.rest.FieldHtmlFactory;
import com.atlassian.jira.issue.fields.rest.json.beans.FieldHtmlBean;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.operation.IssueOperation;
import com.atlassian.jira.issue.operation.IssueOperations;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.quickedit.rest.api.field.IssueBean;
import com.atlassian.jira.quickedit.rest.api.field.QuickEditFields;
import com.atlassian.jira.quickedit.user.UserPreferencesStore;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.UserIssueHistoryManager;
import com.atlassian.jira.user.UserProjectHistoryManager;
import com.atlassian.jira.web.SessionKeys;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.web.action.issue.IssueCreationHelperBean;
import com.atlassian.plugins.rest.common.json.DefaultJaxbJsonMarshaller;
import com.atlassian.plugins.rest.common.json.JaxbJsonMarshaller;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang.exception.ExceptionUtils;
import webwork.action.ActionContext;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.atlassian.jira.quickedit.action.QuickCreateIssue.IssueTypeIdFunction.ID_FUNCTION;
import static com.google.common.collect.Collections2.transform;

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
    private final UserIssueHistoryManager userIssueHistoryManager;
    private final SubTaskManager subTaskManager;
    private final FieldHtmlFactory fieldHtmlFactory;
    private final IssueFactory issueFactory;
    private final IssueCreationHelperBean issueCreationHelperBean;
    private final IssueService issueService;

    private final Map<String, Object> fieldValuesHolder = new HashMap<String, Object>();
    private Long pid;
    private boolean retainValues;
    private IssueBean issue;
    private String issuetype;
    private List<String> fieldsToRetain;
    private MutableIssue issueObject = null;
    private String createdIssueKey;
    private Long parentIssueId = null;

    private QuickEditFields fields;
    private ErrorCollection errors;
    private IssueService.CreateValidationResult validationResult;

    public QuickCreateIssue(final IssueFactory issueFactory, final IssueCreationHelperBean issueCreationHelperBean,
            final IssueService issueService, final UserPreferencesStore userPreferencesStore,
            final UserProjectHistoryManager userProjectHistoryManager, final ApplicationProperties applicationProperties,
            final PermissionManager permissionManager, final IssueTypeSchemeManager issueTypeSchemeManager,
            final UserIssueHistoryManager userIssueHistoryManager, final SubTaskManager subTaskManager, final FieldHtmlFactory fieldHtmlFactory)
    {
        this.issueFactory = issueFactory;
        this.issueCreationHelperBean = issueCreationHelperBean;
        this.issueService = issueService;
        this.userPreferencesStore = userPreferencesStore;
        this.userProjectHistoryManager = userProjectHistoryManager;
        this.applicationProperties = applicationProperties;
        this.permissionManager = permissionManager;
        this.issueTypeSchemeManager = issueTypeSchemeManager;
        this.userIssueHistoryManager = userIssueHistoryManager;
        this.subTaskManager = subTaskManager;
        this.fieldHtmlFactory = fieldHtmlFactory;
    }

    public String doDefault() throws Exception
    {
        ActionContext.getResponse().setContentType("application/json");
        issueCreationHelperBean.validateLicense(this, this);
        if (hasAnyErrors())
        {
            addJsonErrorMessages();
            return ERROR;
        }

        final Collection<Project> projects = permissionManager.getProjectObjects(Permissions.CREATE_ISSUE, getLoggedInUser());
        if (projects.isEmpty())
        {
            addJsonErrorMessages(getText(getLoggedInUser() == null ? "createissue.notloggedin" : "createissue.projectnopermission"));
            return ERROR;
        }

        final MutableIssue newIssueObject = issueObject == null ? issueFactory.getIssue() : issueObject;

        //check if we're creating a subtask or normal issue.
        if (parentIssueId != null)
        {
            if (!subTaskManager.isSubTasksEnabled())
            {
                addJsonErrorMessages(getText("admin.errors.subtasks.disabled"));
                return ERROR;
            }
            //creating a subtask!
            IssueService.IssueResult parentIssueResult = issueService.getIssue(getLoggedInUser(), parentIssueId);
            if (!parentIssueResult.isValid())
            {
                addErrorCollection(parentIssueResult.getErrorCollection());
                addJsonErrorMessages();
                return ERROR;
            }

            final Project parentProject = parentIssueResult.getIssue().getProjectObject();
            final Collection<IssueType> subTaskIssueTypesForProject = issueTypeSchemeManager.getSubTaskIssueTypesForProject(parentProject);

            if (subTaskIssueTypesForProject.isEmpty())
            {
                addErrorMessage(getText("issue.subtask.error.no.subtask.types"));
                addJsonErrorMessages();
                return ERROR;
            }

            newIssueObject.setParentId(parentIssueId);
            setSelectedPid(parentProject.getId());
            setSelectedIssueTypeId(parentProject, true);
        }
        else
        {
            setSelectedPid(null);
            setSelectedIssueTypeId(getProjectManager().getProjectObj(getPid()), false);
        }

        //check project and issuetype are valid and that the user has permission to create an issue in this project!
        validateIssueTypeAndProject(newIssueObject);
        if (hasAnyErrors())
        {
            addJsonErrorMessages();
            return ERROR;
        }

        //populate custom field values holder with default values and construct JSON object
        final QuickEditFields.Builder fieldsBuilder = new QuickEditFields.Builder();
        final List<FieldHtmlBean> createFields;
        if (parentIssueId != null)
        {
            createFields = fieldHtmlFactory.getSubTaskCreateFields(getLoggedInUser(), this, this, newIssueObject, retainValues, fieldsToRetain);
        }
        else
        {
            createFields = fieldHtmlFactory.getCreateFields(getLoggedInUser(), this, this, newIssueObject, retainValues, fieldsToRetain);
        }
        fieldsBuilder.addFields(createFields);

        // Store last issue type, so it can be set as the default in the next issue the user files
        ActionContext.getSession().put(SessionKeys.USER_HISTORY_ISSUETYPE, newIssueObject.getIssueTypeObject().getId());

        if (createdIssueKey != null)
        {
            fieldsBuilder.createdIssue(createdIssueKey);
        }

        fields = fieldsBuilder.build(userPreferencesStore.getCreateUserPreferences(getLoggedInUser()), getXsrfToken());

        return "json";
    }

    protected void doValidation()
    {
        ActionContext.getResponse().setContentType("application/json");

        if (parentIssueId != null)
        {
            validationResult = issueService.validateSubTaskCreate(getLoggedInUser(), parentIssueId,
                    new IssueInputParametersImpl(ActionContext.getParameters()));
        }
        else
        {
            validationResult = issueService.validateCreate(getLoggedInUser(),
                    new IssueInputParametersImpl(ActionContext.getParameters()));
        }
        this.issueObject = validationResult.getIssue();
        // We want to be able to repopulate the fields with their input values
        setFieldValuesHolder(validationResult.getFieldValuesHolder());
        if (!validationResult.isValid())
        {
            addErrorCollection(validationResult.getErrorCollection());
            addJsonErrorMessages();
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
                addJsonErrorMessages();
                return ERROR;
            }

            this.issueObject = issueResult.getIssue();

            if (parentIssueId != null)
            {
                // so far so good. now create the issue link.
                try
                {
                    IssueService.IssueResult parentIssueResult = issueService.getIssue(getLoggedInUser(), parentIssueId);
                    subTaskManager.createSubTaskIssueLink(parentIssueResult.getIssue(), issueObject, getLoggedInUser());
                }
                catch (CreateException e)
                {
                    addErrorMessage(getText("admin.errors.project.import.issue.link.error"));
                    addJsonErrorMessages();
                    return ERROR;
                }
            }

            userIssueHistoryManager.addIssueToHistory(getLoggedInUser(), issueObject);

            if (fieldsToRetain != null)
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

    private void addJsonErrorMessages(String... messages)
    {
        for (String message : messages)
        {
            addErrorMessage(message);
        }
        this.errors = ErrorCollection.of(this);

        if (getLoggedInUser() == null)
        {
            ActionContext.getResponse().setStatus(HttpStatus.SC_UNAUTHORIZED);
        }
        else
        {
            ActionContext.getResponse().setStatus(HttpStatus.SC_BAD_REQUEST);
        }
    }

    private void validateIssueTypeAndProject(MutableIssue issue)
    {
        try
        {
            issue.setProjectId(getPid());
            if (getPid() != null)
            {
                issue.setIssueTypeId(getIssuetype());
            }
        }
        catch (IllegalArgumentException e)
        {
            //this may throw an exception if the project/issuetype ids are invalid. Doesn't matter, the validation below
            //will pick it up anyways.
        }
        // Most calls using the issue object will fail unless the issue object has the project and issue type are set
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

    private void setSelectedPid(final Long defaultPid)
    {
        if (defaultPid != null)
        {
            setPid(defaultPid);
        }

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
        setSelectedProjectId(pid);
    }

    private void setSelectedIssueTypeId(final Project project, boolean subtask)
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

        if(project != null)
        {
            //check if the currently set issue type is valid! If not fall back to the first valid issue type for a project.
            Collection<String> subtaskIds = transform(issueTypeSchemeManager.getSubTaskIssueTypesForProject(project), ID_FUNCTION);
            if (subtask)
            {
                if (!subtaskIds.contains(issueType) && !subtaskIds.isEmpty())
                {
                    issueType = Iterables.get(subtaskIds, 0);
                }
            }
            else
            {
                Collection<String> issueTypeIds = transform(issueTypeSchemeManager.getIssueTypesForProject(project), ID_FUNCTION);
                issueTypeIds.removeAll(subtaskIds);
                //if the issue type isn't valid for this project pick the default one in this project!
                if (!issueTypeIds.contains(issueType) && !issueTypeIds.isEmpty())
                {
                    final IssueType defaultIssueType = issueTypeSchemeManager.getDefaultValue(project.getGenericValue());
                    if(defaultIssueType != null && issueTypeIds.contains(defaultIssueType.getId()))
                    {
                        //if we have a default issue type and it's not a sub-task type then lets use that!
                        issueType = defaultIssueType.getId();
                    }
                    else
                    {
                        //sometimes we don't have a default value. Just go to the first issue type!
                        issueType = Iterables.get(issueTypeIds, 0);
                    }
                }
            }
        }

        getFieldValuesHolder().put(IssueFieldConstants.ISSUE_TYPE, issueType);
        setIssuetype(issueType);
    }

    static class IssueTypeIdFunction implements Function<IssueType, String>
    {
        public static IssueTypeIdFunction ID_FUNCTION = new IssueTypeIdFunction();

        @Override
        public String apply(final IssueType from)
        {
            return from.getId();
        }
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

    public Long getParentIssueId()
    {
        return parentIssueId;
    }

    public void setParentIssueId(final Long parentIssueId)
    {
        this.parentIssueId = parentIssueId;
    }
}
