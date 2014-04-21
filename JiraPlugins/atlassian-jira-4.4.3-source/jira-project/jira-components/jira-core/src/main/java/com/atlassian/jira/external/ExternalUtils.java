package com.atlassian.jira.external;

import com.atlassian.core.action.ActionDispatcher;
import com.atlassian.core.action.ActionUtils;
import com.atlassian.core.util.HTMLUtils;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.external.beans.ExternalAttachment;
import com.atlassian.jira.external.beans.ExternalComment;
import com.atlassian.jira.external.beans.ExternalComponent;
import com.atlassian.jira.external.beans.ExternalCustomFieldValue;
import com.atlassian.jira.external.beans.ExternalIssue;
import com.atlassian.jira.external.beans.ExternalProject;
import com.atlassian.jira.external.beans.ExternalUser;
import com.atlassian.jira.external.beans.ExternalVersion;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.context.GlobalIssueContext;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.MultipleSettableCustomFieldType;
import com.atlassian.jira.issue.customfields.impl.MultiSelectCFType;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenTab;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.issue.link.IssueLinkType;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.project.AssigneeTypes;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectCategory;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.ImportUtils;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.action.admin.customfields.CreateCustomField;
import com.atlassian.jira.web.action.admin.issuetypes.ViewIssueTypes;
import com.atlassian.jira.web.util.AttachmentException;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.jira.workflow.WorkflowManager;
import com.opensymphony.user.User;
import com.opensymphony.workflow.loader.StepDescriptor;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericDelegator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import webwork.dispatcher.ActionResult;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

public class ExternalUtils
{
    public static final String GENERIC_CONTENT_TYPE = "application/octet-stream";

    private final ProjectManager projectManager;
    private final PermissionSchemeManager permissionSchemeManager;
    private final IssueManager issueManager;
    private final JiraAuthenticationContext authenticationContext;
    private final VersionManager versionManager;
    private final ProjectComponentManager componentManager;
    private final CustomFieldManager customFieldManager;
    private final OptionsManager optionsManager;
    private final GenericDelegator genericDelegator;
    private final ActionDispatcher actionDispatcher;
    private final ConstantsManager constantsManager;
    private final WorkflowManager workflowManager;
    private final IssueTypeScreenSchemeManager issueTypeScreenSchemeManager;
    private final FieldScreenManager fieldScreenManager;
    private final PermissionManager permissionManager;
    private final IssueFactory issueFactory;
    private final AttachmentManager attachmentManager;
    private final IssueLinkTypeManager issueLinkTypeManager;
    private final IssueLinkManager issueLinkManager;
    private final FieldManager fieldManager;
    private final ApplicationProperties applicationProperties;
    private final IssueTypeSchemeManager issueTypeSchemeManager;
    private final CommentManager commentManager;
    private final UserUtil userUtil;

    private static final Logger log = Logger.getLogger(ExternalUtils.class);

    public static final String TYPE_SEPERATOR = ":";
    public static final String CF_PREFIX = "customfield_";

    private static final String SEARCHER = "searcher";

    public ExternalUtils(final ProjectManager projectManager, final PermissionSchemeManager permissionSchemeManager, final IssueManager issueManager, final JiraAuthenticationContext authenticationContext, final VersionManager versionManager, final ProjectComponentManager componentManager, final CustomFieldManager customFieldManager, final OptionsManager optionsManager, final GenericDelegator genericDelegator, final ActionDispatcher actionDispatcher, final ConstantsManager constantsManager, final WorkflowManager workflowManager, final IssueTypeScreenSchemeManager issueTypeScreenSchemeManager, final FieldScreenManager fieldScreenManager, final PermissionManager permissionManager, final IssueFactory issueFactory, final AttachmentManager attachmentManager, final IssueLinkTypeManager issueLinkTypeManager, final IssueLinkManager issueLinkManager, final FieldManager fieldManager, final ApplicationProperties applicationProperties, final IssueTypeSchemeManager issueTypeSchemeManager, final CommentManager commentManager, final UserUtil userUtil)
    {
        this.projectManager = projectManager;
        this.permissionSchemeManager = permissionSchemeManager;
        this.issueManager = issueManager;
        this.authenticationContext = authenticationContext;
        this.versionManager = versionManager;
        this.componentManager = componentManager;
        this.customFieldManager = customFieldManager;
        this.optionsManager = optionsManager;
        this.genericDelegator = genericDelegator;
        this.actionDispatcher = actionDispatcher;
        this.constantsManager = constantsManager;
        this.workflowManager = workflowManager;
        this.issueTypeScreenSchemeManager = issueTypeScreenSchemeManager;
        this.fieldScreenManager = fieldScreenManager;
        this.permissionManager = permissionManager;
        this.issueFactory = issueFactory;
        this.attachmentManager = attachmentManager;
        this.issueLinkTypeManager = issueLinkTypeManager;
        this.issueLinkManager = issueLinkManager;
        this.fieldManager = fieldManager;
        this.applicationProperties = applicationProperties;
        this.issueTypeSchemeManager = issueTypeSchemeManager;
        this.commentManager = commentManager;
        this.userUtil = userUtil;
    }

    /**
     * Tries to find an existing Project based on the values in the given ExternalProject.
     * @param externalProject the ExternalProject.
     * @return the project or null if none exist
     *
     * @deprecated Use {@link #getProjectObject(com.atlassian.jira.external.beans.ExternalProject)} instead. Since v4.4.
     */
    public GenericValue getProject(final ExternalProject externalProject)
    {
        GenericValue projectGV = null;

        if (StringUtils.isNotEmpty(externalProject.getKey()))
        {
            projectGV = projectManager.getProjectByKey(externalProject.getKey());
        }

        if ((projectGV == null) && StringUtils.isNotEmpty(externalProject.getName()))
        {
            projectGV = projectManager.getProjectByName(externalProject.getName());
        }

        return projectGV;
    }

    /**
     * Tries to find an existing Project based on the values in the given ExternalProject.
     * @param externalProject the ExternalProject.
     * @return the project or null if none exist
     */
    public Project getProjectObject(final ExternalProject externalProject)
    {
        Project project = null;

        if (StringUtils.isNotEmpty(externalProject.getKey()))
        {
            project = projectManager.getProjectObjByKey(externalProject.getKey());
        }

        if ((project == null) && StringUtils.isNotEmpty(externalProject.getName()))
        {
            project = projectManager.getProjectObjByName(externalProject.getName());
        }

        return project;
    }

    /**
     * Create a project in JIRA from the given ExternalProject.
     * @param externalProject the ExternalProject definition
     * @return The newly created Project
     * @throws ExternalException if anything goes wrong
     * @deprecated since v4.4. Use {@link #createProjectObject(com.atlassian.jira.external.beans.ExternalProject)} instead.
     */
    public GenericValue createProject(final ExternalProject externalProject) throws ExternalException
    {
        return createProjectObject(externalProject).getGenericValue();
    }

    /**
     * Create a project in JIRA from the given ExternalProject.
     * @param externalProject the ExternalProject definition
     * @return The newly created Project
     * @throws ExternalException if anything goes wrong
     */
    public Project createProjectObject(final ExternalProject externalProject) throws ExternalException
    {
        try
        {
            // Set lead to current user if none exists
            if (externalProject.getLead() == null)
            {
                externalProject.setLead(authenticationContext.getLoggedInUser().getName());
            }

            // JRA-19699: if there is no assignee type - set it to either UNASSIGNED or PROJECT LEAD
            if (externalProject.getAssigneeType() == null)
            {
                if (isUnassignedIssuesAllowed())
                {
                    externalProject.setAssigneeType(String.valueOf(AssigneeTypes.UNASSIGNED));
                }
                else
                {
                    externalProject.setAssigneeType(String.valueOf(AssigneeTypes.PROJECT_LEAD));
                }
            }

            final Project project = projectManager.createProject(
                    externalProject.getName(),
                    externalProject.getKey(),
                    externalProject.getDescription(),
                    externalProject.getLead(),
                    externalProject.getUrl(),
                    new Long(externalProject.getAssigneeType())
                    );

            // Add the default schemes for this project
            permissionSchemeManager.addDefaultSchemeToProject(project.getGenericValue());
            issueTypeScreenSchemeManager.associateWithDefaultScheme(project.getGenericValue());

            return project;
        }
        catch (final Exception e)
        {
            throw new ExternalException("Unable to create project: " + externalProject, e);
        }
    }

    private boolean isUnassignedIssuesAllowed()
    {
        return applicationProperties.getOption(APKeys.JIRA_OPTION_ALLOWUNASSIGNED);
    }

    public Version createVersion(final ExternalProject externalProject, final ExternalVersion externalVersion)
    {
        Version jiraVersion = null;
        try
        {
            final String versionName = externalVersion.getName();
            jiraVersion = versionManager.createVersion(versionName, externalVersion.getReleaseDate(), externalVersion.getDescription(),
                externalProject.getProjectGV(), null);

            if (externalVersion.isArchived())
            {
                versionManager.archiveVersion(jiraVersion, true);
            }
        }
        catch (final Exception e)
        {
            log.warn("Problems encoutered while creating Version " + externalVersion, e);
        }
        return jiraVersion;
    }

    public Version getVersion(final ExternalProject externalProject, final ExternalVersion externalVersion)
    {
        Version jiraVersion = null;
        try
        {
            final String versionName = externalVersion.getName();
            jiraVersion = versionManager.getVersion(externalProject.getProjectGV(), versionName);
        }
        catch (final Exception e)
        {
            log.warn("Problems encoutered while retrieving Version " + externalVersion, e);
        }
        return jiraVersion;
    }

    public GenericValue createComponent(final ExternalProject externalProject, final ExternalComponent externalComponent)
    {
        GenericValue jiraComponent = null;
        try
        {
            final String componentName = externalComponent.getName();
            final ProjectComponent projectComponent = componentManager.create(componentName, null, null, AssigneeTypes.PROJECT_DEFAULT,
                externalProject.getProjectGV().getLong("id"));
            jiraComponent = componentManager.convertToGenericValue(projectComponent);
        }
        catch (final Exception e)
        {
            log.warn("Problems encoutered while creating Component " + externalComponent, e);
        }

        return jiraComponent;
    }

    public GenericValue getComponent(final ExternalProject externalProject, final ExternalComponent externalComponent)
    {
        GenericValue jiraComponent = null;
        try
        {
            final String componentName = externalComponent.getName();
            final ProjectComponent projectComponent = componentManager.findByComponentName(externalProject.getProjectGV().getLong("id"),
                componentName);
            jiraComponent = componentManager.convertToGenericValue(projectComponent);
        }
        catch (final Exception e)
        {
            log.warn("Problems encoutered while retrieving Component " + externalComponent, e);
        }

        return jiraComponent;
    }

    public User createUser(final ExternalUser externalUser)
    {
        try
        {
            return userUtil.createUserNoEvent(externalUser.getName(), externalUser.getPassword(), externalUser.getEmail(), externalUser.getFullname());
        }
        catch (final Exception e)
        {
            log.warn("Problems encoutered while creating User " + externalUser, e);
            return null;
        }
    }

    public boolean canActivateNumberOfUsers(final int numberOfUsers)
    {
        return userUtil.canActivateNumberOfUsers(numberOfUsers);
    }

    public User getJiraUser(final ExternalUser externalUser)
    {
        return userUtil.getUser(externalUser.getName());
    }

    public GenericValue createIssue(final Issue issue, final String status, final String resolution) throws ExternalException
    {
        try
        {
            if (StringUtils.isNotBlank(status))
            {
                // Validate status and that it has a linked step in the workflow. This method will throw exception
                // if some data is invalid.
                checkStatus(issue, status);
            }

            final GenericValue issueGV = issueManager.createIssue(authenticationContext.getUser(), issue);

            if (StringUtils.isNotBlank(status))
            {
                setCurrentWorkflowStep(issueGV, status, resolution);
            }

            return issueGV;
        }
        catch (final Exception e)
        {
            throw new ExternalException("Unable to create issue: " + issue, e);
        }
    }

    protected void checkStatus(final Issue issue, final String status) throws WorkflowException, ExternalException
    {
        // Check that the status is OK
        if (issue != null)
        {
            final GenericValue statusGV = constantsManager.getStatus(status);

            if (statusGV != null)
            {
                final JiraWorkflow workflow = workflowManager.getWorkflow(issue.getProject().getLong("id"), issue.getIssueType().getString("id"));
                final StepDescriptor linkedStep = workflow.getLinkedStep(statusGV);

                if (linkedStep == null)
                {
                    throw new ExternalException(
                        "Status '" + statusGV.getString("name") + "' does not have a linked step in the '" + workflow.getName() + "' workflow. Please map to a different status.");
                }
            }
            else
            {
                throw new ExternalException("Cannot find status with id '" + status + "'.");
            }
        }
    }

    protected GenericValue createIssue(final Map fields) throws CreateException
    {
        return issueManager.createIssue(authenticationContext.getUser(), fields);
    }

    public void setCurrentWorkflowStep(final GenericValue issue, final String status, final String resolution) throws GenericEntityException, WorkflowException
    {
        // retrieve the wfCurrentStep for this issue and change it
        if (issue != null)
        {
            final GenericValue statusGV = constantsManager.getStatus(status);

            if (statusGV != null)
            {
                final JiraWorkflow workflow = workflowManager.getWorkflow(issue);
                final StepDescriptor linkedStep = workflow.getLinkedStep(statusGV);

                final Collection<GenericValue> wfCurrentStepCollection = genericDelegator.findByAnd("OSCurrentStep",
                        MapBuilder.build("entryId", issue.getLong("workflowId")));
                if ((wfCurrentStepCollection != null) && !wfCurrentStepCollection.isEmpty())
                {
                    final GenericValue wfCurrentStep = wfCurrentStepCollection.iterator().next();
                    if (linkedStep != null)
                    {
                        wfCurrentStep.set("stepId", linkedStep.getId());
                        wfCurrentStep.store();
                    }
                    else
                    {
                        // This should never occur as the status had to be checked before this
                        log.error("Workflow '" + workflow.getName() + "' does not have a step for status '" + statusGV.getString("name") + "'.");
                    }
                }
                else
                {
                    log.warn("Workflow Id not found");
                }

                // Set the resolution & statuses nicely
                issue.set("status", status);
                issue.set("resolution", resolution);
                issue.store();
            }
            else
            {
                log.warn("Status' GV for '" + status + "' was null. Issue not updated. " + issue);
            }
        }
    }

    public CustomField getCustomField(final ExternalCustomFieldValue customFieldValue)
    {
        final String customfieldId = customFieldValue.getKey();
        return getCustomField(customfieldId);

    }

    public CustomField getCustomField(final String customfieldId)
    {
        CustomField customFieldObject = null;
        try
        {
            try
            {
                customFieldObject = customFieldManager.getCustomFieldObject(customfieldId);
            }
            catch (final NumberFormatException e)
            {
                // Don't do anything, expected for new stuff
            }
            catch (final Exception e)
            {
                log.warn(e.getMessage(), e);
            }

            if (customFieldObject == null)
            {
                final String fieldName = extractCustomFieldId(customfieldId);
                customFieldObject = customFieldManager.getCustomFieldObjectByName(fieldName);
            }

        }
        catch (final Exception e)
        {
            // Can't get the custom field
            log.warn(e.getMessage(), e);
        }

        return customFieldObject;
    }

    public void addOptions(final CustomField customFieldObject, final ExternalCustomFieldValue customFieldValue, final GenericValue issueGV)
    {
        if ((customFieldObject != null) && (customFieldObject.getCustomFieldType() instanceof MultipleSettableCustomFieldType))
        {
            // Add the value as a new option
            final String cfvalue = customFieldValue.getValue();
            Collection<String> values;

            // Hard coded hack... :(
            if (customFieldObject.getCustomFieldType() instanceof MultiSelectCFType)
            {
                // TODO: What about the other Multi Select Custom Fields? User and Group.
                // See eg JRA-10515 and JRA-13870
                values = MultiSelectCFType.extractTransferObjectFromString(cfvalue);
            }
            else
            {
                values = CollectionBuilder.newBuilder(cfvalue).asList();
            }

            final Issue issue = issueFactory.getIssue(issueGV);
            final FieldConfig config = customFieldObject.getRelevantConfig(issue);
            final Options options = customFieldObject.getOptions(null, config, null);
            for (final String value : values)
            {
                if (StringUtils.isNotBlank(value) && (options.getOptionForValue(value, null) == null))
                {
                    final int sequence = options.size();
                    optionsManager.createOption(config, null, new Long(sequence), value);
                }
            }
        }
    }

    public CustomField createCustomField(final ExternalCustomFieldValue customFieldValue)
    {
        CustomField customFieldObject = null;

        final String customfieldId = customFieldValue.getKey();
        final String fieldName = extractCustomFieldId(customfieldId);
        final String fieldType = extractCustomFieldType(customfieldId);

        try
        {
            // Create a new custom field
            customFieldObject = createCustomField(fieldName, fieldType);
        }
        catch (final ExternalException e)
        {
            log.warn("Unable to create custom field " + customFieldValue, e);
        }

        return customFieldObject;
    }

    public String extractCustomFieldType(final String customfieldId)
    {
        return StringUtils.substringAfter(customfieldId, TYPE_SEPERATOR);
    }

    public String extractCustomFieldId(final String customfieldId)
    {
        String fieldId;
        if (StringUtils.contains(customfieldId, TYPE_SEPERATOR))
        {
            fieldId = StringUtils.substringBetween(customfieldId, CF_PREFIX, TYPE_SEPERATOR);
        }
        else
        {
            fieldId = StringUtils.substringAfter(customfieldId, CF_PREFIX);
        }
        return fieldId;
    }

    private static final String TEXT_FIELD_TYPE = "textfield";
    private static final String TEXT_FIELD_SEARCHER = "textsearcher";

    private static final String DATE_FIELD_TYPE = "datepicker";
    private static final String DATE_FIELD_SEARCHER = "daterange";

    private CustomField createCustomField(final String customFieldName, final String type) throws ExternalException
    {
        try
        {
            // Create cf of the correct type
            CustomFieldType cfType;
            CustomFieldSearcher searcher;

            // @TODO this is surely unescessary? Should just always match the field name
            if ("select".equals(type) || "userpicker".equals(type) || "multiselect".equals(type))
            {
                cfType = customFieldManager.getCustomFieldType(CreateCustomField.FIELD_TYPE_PREFIX + type);
                searcher = customFieldManager.getCustomFieldSearcher(CreateCustomField.FIELD_TYPE_PREFIX + type + SEARCHER);
            }
            else if ("date".equals(type) || DATE_FIELD_TYPE.equals(type))
            {
                cfType = customFieldManager.getCustomFieldType(CreateCustomField.FIELD_TYPE_PREFIX + DATE_FIELD_TYPE);
                searcher = customFieldManager.getCustomFieldSearcher(CreateCustomField.FIELD_TYPE_PREFIX + DATE_FIELD_SEARCHER);
            }
            else
            {
                cfType = customFieldManager.getCustomFieldType(CreateCustomField.FIELD_TYPE_PREFIX + TEXT_FIELD_TYPE);
                searcher = customFieldManager.getCustomFieldSearcher(CreateCustomField.FIELD_TYPE_PREFIX + TEXT_FIELD_SEARCHER);
            }

            final CustomField customField = customFieldManager.createCustomField(customFieldName, customFieldName, cfType, searcher,
                EasyList.build(GlobalIssueContext.getInstance()), EasyList.buildNull());

            associateCustomFieldWithScreen(customField, null);

            return customField;

        }
        catch (final GenericEntityException e)
        {
            throw new ExternalException(e);
        }
    }

    public void associateCustomFieldWithScreen(final CustomField customField, FieldScreen screen)
    {
        if (screen == null)
        {
            screen = fieldScreenManager.getFieldScreen(FieldScreen.DEFAULT_SCREEN_ID);
        }

        if ((screen != null) && (screen.getTabs() != null) && !screen.getTabs().isEmpty())
        {
            final FieldScreenTab tab = screen.getTab(0);
            tab.addFieldScreenLayoutItem(customField.getId());
        }
    }

    /**
     * Adds an external comment to an issue and portentially dispatches and event about this.  The issue updated date will be set to now via this method.
     *
     * @param issue           the issue GV
     * @param externalComment the external Comment to add
     * @param dispatchEvent   whether to dispatch an issue updated event
     * @throws ExternalException if something bad happens
     */
    public void addComments(final GenericValue issue, final ExternalComment externalComment, final boolean dispatchEvent) throws ExternalException
    {
        addComments(issue, externalComment, dispatchEvent, true);
    }

    /**
     * Adds an external comment to an issue and portentially dispatches and event about this and also updates the issue
     * updated date.  When doing a CSV import one might not want the event dispatched nor the issue updated date changed.
     *
     * @param issue                the issue GV
     * @param externalComment      the external Comment to add
     * @param dispatchEvent        whether to dispatch an issue updated event
     * @param tweakIssueUpdateDate whether to tweak the issue updated date or not
     * @throws ExternalException if something bad happens
     */
    public void addComments(final GenericValue issue, final ExternalComment externalComment, final boolean dispatchEvent, final boolean tweakIssueUpdateDate) throws ExternalException
    {
        final String username = externalComment.getUsername();
        User commenter;
        if (username != null)
        {
            commenter = userUtil.getUser(username);
            if (commenter == null)
            {
                log.warn("Commenter named " + username + " not found. Creating issue with currently logged in user instead");
                commenter = authenticationContext.getUser();
            }
        }
        else
        {
            commenter = authenticationContext.getUser();
        }

        if (!permissionManager.hasPermission(Permissions.COMMENT_ISSUE, issue, commenter))
        {
            final String s = "Comment not created. The user (" + commenter.getDisplayName() + ") do not have permission to comment on an issue in project: " + projectManager.getProject(
                issue.getLong("project")).getString("name");
            log.warn(s);
            throw new ExternalException(s);
        }
        else
        {
            try
            {
                final String author = commenter.getName();
                final Date timePerformed = externalComment.getTimePerformed();
                commentManager.create(issueFactory.getIssue(issue), author, author, externalComment.getBody(), externalComment.getGroupLevel(),
                    externalComment.getRoleLevelId(), timePerformed, timePerformed, dispatchEvent, tweakIssueUpdateDate);
            }
            catch (final Exception e)
            {
                log.warn("Unable to create comment " + externalComment + ". Comment not created", e);
                throw new ExternalException(e);
            }
        }
    }

    public void attachFile(final ExternalAttachment externalAttachment, final GenericValue issueGv) throws ExternalException
    {
        final String username = externalAttachment.getAttacher();
        User user;
        if (username != null)
        {
            user = userUtil.getUser(username);
            if (user == null)
            {
                log.warn("User named " + username + " not found. attaching to issue with currently logged in user instead");
                user = authenticationContext.getUser();
            }
        }
        else
        {
            user = authenticationContext.getUser();
        }

        try
        {
            attachmentManager.createAttachment(externalAttachment.getAttachedFile(), externalAttachment.getFileName(), GENERIC_CONTENT_TYPE, user,
                issueGv, Collections.EMPTY_MAP, externalAttachment.getAttachedDate());
        }
        catch (final AttachmentException e)
        {
            throw new ExternalException(e);
        }
        catch (final GenericEntityException e)
        {
            throw new ExternalException(e);
        }
    }


    private IssueLinkType createOrFindLinkType(final String name) throws ExternalException
    {
        Collection<IssueLinkType> dependency = issueLinkTypeManager.getIssueLinkTypesByName(name);
        if (dependency.isEmpty())
        {
            try
            {
                issueLinkTypeManager.createIssueLinkType(name, name, name, null);
            }
            catch (IllegalArgumentException e)
            {
                throw new ExternalException(e);
            }
            dependency = issueLinkTypeManager.getIssueLinkTypesByName(name);
        }
        return dependency.iterator().next();
    }

    public GenericValue getConstant(final String constantName, final String constantType)
    {
        final GenericValue constantById = constantsManager.getConstant(constantType, constantName);
        if (constantById == null)
        {
            return constantsManager.getConstantByName(constantType, constantName);
        }
        else
        {
            return constantById;
        }
    }

    public String addConstant(final String constantName, final String constantType) throws ExternalException
    {
        if (ConstantsManager.ISSUE_TYPE_CONSTANT_TYPE.equals(constantType))
        {
            final Map<String, String> parameters = MapBuilder.<String, String> newBuilder().add("iconurl", ViewIssueTypes.NEW_ISSUE_TYPE_DEFAULT_ICON).toMap();
            return addConstant(constantName, ConstantsManager.ISSUE_TYPE_CONSTANT_TYPE, parameters);
        }
        else if (ConstantsManager.PRIORITY_CONSTANT_TYPE.equals(constantType))
        {
            final Map<String, String> parameters = MapBuilder.<String, String> newBuilder().add("iconurl", "/images/icons/priority_major.gif").add(
                "statusColor", "#009900").toMap();
            return addConstant(constantName, ConstantsManager.PRIORITY_CONSTANT_TYPE, parameters);
        }
        else if (ConstantsManager.RESOLUTION_CONSTANT_TYPE.equals(constantType))
        {
            return addConstant(constantName, ConstantsManager.RESOLUTION_CONSTANT_TYPE, null);

        }
        else
        {
            throw new ExternalException("Unknown contantType:" + constantType);
        }
    }

    protected String addConstant(final String constantName, final String constantType, final Map<String, String> extraParams) throws ExternalException
    {
        try
        {
            final Map<String, String> parameters = MapBuilder.newBuilder(extraParams).add("name", constantName).add("description", constantName).toMap();

            final ActionResult aResult = actionDispatcher.execute("Add" + constantType, parameters);
            ActionUtils.checkForErrors(aResult);

            final IssueConstant constant = constantsManager.getIssueConstantByName(constantType, constantName);

            // Don't check for null since it should be there...
            return constant.getId();
        }
        catch (final Exception e)
        {
            throw new ExternalException("Unable to create " + constantType + " " + constantName, e);
        }
    }

    public MutableIssue newIssueInstance()
    {
        return issueFactory.getIssue();
    }

    private String preFilterSummary(final ExternalIssue externalIssue)
    {
        final String summary = externalIssue.getSummary();
        if (StringUtils.isBlank(summary) && !StringUtils.isNotBlank(externalIssue.getDescription()))
        {
            return StringUtils.abbreviate(externalIssue.getDescription(), 250);
        }
        else
        {
            return summary;
        }
    }

    private Timestamp toTimeStamp(final Date date)
    {
        if (date != null)
        {
            return new Timestamp(date.getTime());
        }
        else
        {
            return null;
        }
    }

    public static String getTextDataFromMimeMessage(final String s)
    {
        try
        {
            final Session session = Session.getDefaultInstance(new Properties());

            final InputStream is = new ByteArrayInputStream(s.getBytes());

            final MimeMessage message = new MimeMessage(session, is);
            return getTextDataFromMimeMessage(message);
        }
        catch (final Exception e)
        {
            throw new ExternalRuntimeException(e);
        }
    }

    private static String getTextDataFromMimeMessage(final MimeMessage message) throws IOException, MessagingException
    {
        final Object content = message.getContent();
        if (content instanceof Multipart)
        {
            final Multipart mm = (Multipart) content;
            final StringBuffer sb = new StringBuffer();
            for (int i = 0; i < mm.getCount(); i++)
            {
                final BodyPart part = mm.getBodyPart(i);
                if (part.getContentType().startsWith("text") && (i == 0))
                {
                    sb.append(getCleanedContent(part));
                }
                else if ((part.getFileName() == null) && (part.getContent() instanceof MimeMessage))
                {
                    sb.append(getTextDataFromMimeMessage((MimeMessage) part.getContent()));
                }
            }

            return sb.toString();
        }
        else
        {
            return getCleanedContent(message);
        }
    }

    private static String getCleanedContent(final Part part) throws IOException, MessagingException
    {
        final String content = part.getContent().toString();
        if (part.getContentType().startsWith("text/html"))
        {
            final String noTags = HTMLUtils.stripTags(content);

            try
            {
                return StringEscapeUtils.unescapeHtml(noTags);
            }
            catch (final NumberFormatException e)
            {
                return ImportUtils.stripHTMLStrings(noTags);
            }
        }
        else
        {
            return content;
        }
    }

    /**
     * Tries to find a Project Category with the given name.
     * @param projectCategoryName
     * @return a Project Category with the given name or null.
     * @deprecated Use {@link #getProjectCategoryObject(String)} instead. Since v4.4.
     */
    public GenericValue getProjectCategory(final String projectCategoryName)
    {
        return projectManager.getProjectCategoryByName(projectCategoryName);
    }

    /**
     * Tries to find a Project Category with the given name.
     * @param projectCategoryName
     * @return a Project Category with the given name or null.
     */
    public ProjectCategory getProjectCategoryObject(final String projectCategoryName)
    {
        return projectManager.getProjectCategoryObjectByName(projectCategoryName);
    }

    /**
     * Creates a projectCategory with the given name.
     * @param projectCategoryName The name.
     * @return The newly create ProjectCategory
     * @throws ExternalException
     *
     * @deprecated Use {@link #createProjectCategoryObject(String)} instead. Since v4.4
     */
    public GenericValue createProjectCategory(final String projectCategoryName) throws ExternalException
    {
        ProjectCategory projectCategory = projectManager.createProjectCategory(projectCategoryName, "");
        return projectManager.getProjectCategory(projectCategory.getId());
    }

    public ProjectCategory createProjectCategoryObject(final String projectCategoryName) throws ExternalException
    {
        return projectManager.createProjectCategory(projectCategoryName, "");
    }

    /**
     * Associates a project with a projectCategory.
     *
     * @param project The Project
     * @param projectCategory The category.
     * @deprecated since v4.4. Use {@link #associateProjectCategory(com.atlassian.jira.project.Project, com.atlassian.jira.project.ProjectCategory)} instead.
     */
    public void associateProjectCategory(final GenericValue project, final GenericValue projectCategory)
    {
        projectManager.setProjectCategory(project, projectCategory);
    }

    /**
     * Associates a project with a projectCategory.
     *
     * @param project The Project
     * @param category The category.
     */
    public void associateProjectCategory(Project project, ProjectCategory category)
    {
        projectManager.setProjectCategory(project, category);
    }
}
