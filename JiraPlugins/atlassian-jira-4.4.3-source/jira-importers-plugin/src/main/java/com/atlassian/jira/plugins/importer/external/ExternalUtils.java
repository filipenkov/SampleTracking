/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.external;

import com.atlassian.core.util.DateUtils;
import com.atlassian.core.util.HTMLUtils;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.context.GlobalIssueContext;
import com.atlassian.jira.issue.context.manager.JiraContextTreeManager;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenTab;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.link.IssueLinkType;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.plugins.importer.external.beans.ExternalAttachment;
import com.atlassian.jira.plugins.importer.external.beans.ExternalComment;
import com.atlassian.jira.plugins.importer.external.beans.ExternalComponent;
import com.atlassian.jira.plugins.importer.external.beans.ExternalCustomFieldValue;
import com.atlassian.jira.plugins.importer.external.beans.ExternalIssue;
import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.atlassian.jira.plugins.importer.external.beans.ExternalVersion;
import com.atlassian.jira.plugins.importer.imports.csv.ImportException;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.atlassian.jira.plugins.importer.imports.importer.impl.ImportObjectIdMappings;
import com.atlassian.jira.plugins.importer.managers.CreateIssueLinkManager;
import com.atlassian.jira.project.AssigneeTypes;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.ImportUtils;
import com.atlassian.jira.web.util.AttachmentException;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.opensymphony.workflow.loader.StepDescriptor;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import javax.annotation.Nullable;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import static com.atlassian.jira.plugins.importer.external.CustomFieldConstants.TEXT_FIELD_TYPE;

public class ExternalUtils {
	public static final String GENERIC_CONTENT_TYPE = "application/octet-stream";

	private final ProjectManager projectManager;
	private final IssueManager issueManager;

	private final JiraAuthenticationContext authenticationContext;
	private final VersionManager versionManager;
	private final ProjectComponentManager componentManager;
	private final CustomFieldManager customFieldManager;
	private final WorkflowManager workflowManager;
	private final WorkflowSchemeManager workflowSchemeManager;
	private final FieldScreenManager fieldScreenManager;
	private final PermissionManager permissionManager;
	private final IssueFactory issueFactory;
	private final AttachmentManager attachmentManager;
	private final IssueLinkTypeManager issueLinkTypeManager;
	private final CreateIssueLinkManager issueLinkManager;
	private final FieldManager fieldManager;
	private final ApplicationProperties applicationProperties;
	private final IssueTypeSchemeManager issueTypeSchemeManager;
	private final CommentManager commentManager;
	private final OfBizDelegator genericDelegator;
	private final ConstantsManager constantsManager;
	private final IssueSecurityLevelManager issueSecurityLevelManager;
	private final SubTaskManager subTaskManager;
	private final FieldConfigSchemeManager fieldConfigSchemeManager;
	private final JiraContextTreeManager treeManager;

	public ExternalUtils(final ProjectManager projectManager, final IssueManager issueManager,
			final JiraAuthenticationContext authenticationContext, final VersionManager versionManager,
			final ProjectComponentManager componentManager,
			final CustomFieldManager customFieldManager,
			final ConstantsManager constantsManager, final WorkflowManager workflowManager,
			final FieldScreenManager fieldScreenManager,
			final PermissionManager permissionManager, final IssueFactory issueFactory,
			final AttachmentManager attachmentManager,
			final IssueLinkTypeManager issueLinkTypeManager, final CreateIssueLinkManager issueLinkManager,
			final FieldManager fieldManager,
			final ApplicationProperties applicationProperties, final IssueTypeSchemeManager issueTypeSchemeManager,
			final CommentManager commentManager,
			final WorkflowSchemeManager workflowSchemeManager,
			final OfBizDelegator genericDelegator, IssueSecurityLevelManager issueSecurityLevelManager,
			final SubTaskManager subTaskManager,
			final FieldConfigSchemeManager fieldConfigSchemeManager,
			final JiraContextTreeManager treeManager) {
		this.projectManager = projectManager;
		this.issueManager = issueManager;
		this.authenticationContext = authenticationContext;
		this.versionManager = versionManager;
		this.componentManager = componentManager;
		this.workflowSchemeManager = workflowSchemeManager;
		this.customFieldManager = customFieldManager;
		this.constantsManager = constantsManager;
		this.workflowManager = workflowManager;
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
		this.genericDelegator = genericDelegator;
		this.issueSecurityLevelManager = issueSecurityLevelManager;
		this.subTaskManager = subTaskManager;
		this.fieldConfigSchemeManager = fieldConfigSchemeManager;
		this.treeManager = treeManager;
	}


	public ExternalUtils(final ProjectManager projectManager, final IssueManager issueManager,
			final JiraAuthenticationContext authenticationContext, final VersionManager versionManager,
			final ProjectComponentManager componentManager,
			final CustomFieldManager customFieldManager, final OptionsManager optionsManager,
			final ConstantsManager constantsManager, final WorkflowManager workflowManager,
			final FieldScreenManager fieldScreenManager,
			final PermissionManager permissionManager, final IssueFactory issueFactory,
			final AttachmentManager attachmentManager,
			final IssueLinkTypeManager issueLinkTypeManager, final CreateIssueLinkManager issueLinkManager,
			final FieldManager fieldManager,
			final ApplicationProperties applicationProperties, final IssueTypeSchemeManager issueTypeSchemeManager,
			final CommentManager commentManager,
			final WorkflowSchemeManager workflowSchemeManager,
			final OfBizDelegator genericDelegator, IssueSecurityLevelManager issueSecurityLevelManager,
			final SubTaskManager subTaskManager,
			final FieldConfigSchemeManager fieldConfigSchemeManager) {
		this(projectManager, issueManager, authenticationContext, versionManager, componentManager,
				customFieldManager, constantsManager, workflowManager, fieldScreenManager,
				permissionManager, issueFactory, attachmentManager, issueLinkTypeManager, issueLinkManager,
				fieldManager, applicationProperties, issueTypeSchemeManager, commentManager,
				workflowSchemeManager, genericDelegator, issueSecurityLevelManager, subTaskManager,
				fieldConfigSchemeManager, ComponentManager.getComponentInstanceOfType(JiraContextTreeManager.class));
	}

	@Nullable
	public Project getProject(final ExternalProject externalProject) {
		Project project = null;

		if (StringUtils.isNotEmpty(externalProject.getKey())) {
			project = projectManager.getProjectObjByKey(externalProject.getKey());
		}

		if ((project == null) && StringUtils.isNotEmpty(externalProject.getName())) {
			project = projectManager.getProjectObjByName(externalProject.getName());
		}

		return project;
	}

	public Version createVersion(final ExternalProject externalProject, final ExternalVersion externalVersion,
			final ImportLogger log) {
		Version jiraVersion = null;
		try {
			final String versionName = externalVersion.getName();
			jiraVersion = versionManager
					.createVersion(versionName,
							externalVersion.getReleaseDate() != null ? externalVersion.getReleaseDate().toDate() : null,
							externalVersion.getDescription(), externalProject.getJiraId(), null);
			if (externalVersion.isReleased()) {
				versionManager.releaseVersion(jiraVersion, true);
			}
			if (externalVersion.isArchived()) {
				versionManager.archiveVersion(jiraVersion, true);
			}
		} catch (final Exception e) {
			log.warn(e, "Problems encoutered while creating Version %s", externalVersion);
		}
		return jiraVersion;
	}

	@Nullable
	public ProjectComponent createComponent(final ExternalProject externalProject,
			final ExternalComponent externalComponent, ImportLogger log) {
		try {
			final String componentName = externalComponent.getName();
			final ProjectComponent projectComponent = componentManager
					.create(componentName, null, null, AssigneeTypes.PROJECT_DEFAULT,
							externalProject.getJiraId());
			return projectComponent;
		} catch (final Exception e) {
			log.warn(e, "Problems encoutered while creating Component %s", externalComponent);
		}
		return null;
	}

	@Nullable
	public ProjectComponent getComponent(final ExternalProject externalProject,
			final ExternalComponent externalComponent, final ImportLogger log) {
		try {
			final String componentName = externalComponent.getName();
			final ProjectComponent projectComponent = componentManager
					.findByComponentName(externalProject.getJiraId(), componentName);
			return projectComponent;
		} catch (final Exception e) {
			log.warn(e, "Problems encoutered while retrieving Component %s", externalComponent);
		}
		return null;
	}

	public GenericValue createIssue(final Issue issue, final String status, final String resolution,
			final ImportLogger log) throws ExternalException {
		try {
			if (StringUtils.isNotBlank(status)) {
				// Validate status and that it has a linked step in the workflow. This method will throw exception
				// if some data is invalid.
				checkStatus(issue, status);
			}

			final GenericValue issueGV = issueManager.createIssue(authenticationContext.getLoggedInUser(), issue);

			if (StringUtils.isNotBlank(status)) {
				setCurrentWorkflowStep(issueGV, status, resolution, log);
			}

			return issueGV;
		} catch (final Exception e) {
			throw new ExternalException("Unable to create issue: " + issue, e);
		}
	}

	protected void checkStatus(final Issue issue, final String statusId) throws WorkflowException, ExternalException {
		// Check that the status is OK
		if (issue != null) {
			final Status status = constantsManager.getStatusObject(statusId);

			if (status != null) {
				final JiraWorkflow workflow = workflowManager
						.getWorkflow(issue.getProjectObject().getId(), issue.getIssueTypeObject().getId());
				final StepDescriptor linkedStep = workflow.getLinkedStep(status.getGenericValue());

				if (linkedStep == null) {
					throw new ExternalException(
							"Status '" + status.getName() + "' does not have a linked step in the '"
									+ workflow.getName() + "' workflow. Please map to a different status.");
				}
			} else {
				throw new ExternalException("Cannot find status with id '" + statusId + "'.");
			}
		}
	}

	public void setCurrentWorkflowStep(final GenericValue issue, final String statusId, final String resolution,
			final ImportLogger log)
			throws GenericEntityException, WorkflowException {
		// retrieve the wfCurrentStep for this issue and change it
		if (issue != null) {
			final Status status = constantsManager.getStatusObject(statusId);

			if (status != null) {
				final JiraWorkflow workflow = workflowManager.getWorkflow(issue);
				final StepDescriptor linkedStep = workflow.getLinkedStep(status.getGenericValue());

				@SuppressWarnings("unchecked")
				final Collection<GenericValue> wfCurrentStepCollection = genericDelegator.findByAnd(
						"OSCurrentStep", EasyMap.build("entryId", issue.getLong("workflowId")));
				if ((wfCurrentStepCollection != null) && !wfCurrentStepCollection.isEmpty()) {
					final GenericValue wfCurrentStep = wfCurrentStepCollection.iterator().next();
					if (linkedStep != null) {
						wfCurrentStep.set("stepId", linkedStep.getId());
						wfCurrentStep.store();
					} else {
						// This should never occur as the status had to be checked before this
						log.fail(null, "Workflow '%s' does not have a step for status '%s'.", workflow.getName(), status.getName());
					}
				} else {
					log.warn("Workflow Id not found");
				}

				// Set the resolution & statuses nicely
				issue.set("status", statusId);
				issue.set("resolution", resolution);
				issue.store();
			} else {
				log.warn("Status GV for '%s' was null. Issue not updated: %s" , statusId, issue);
			}
		}
	}

	@Nullable
	public CustomField getCustomField(final ExternalCustomFieldValue customFieldValue, final ImportLogger log) {
		try {
			if (!StringUtils.isEmpty(customFieldValue.getFieldName())) {
				final Collection<CustomField> customFieldObjectsByName = customFieldManager
						.getCustomFieldObjectsByName(customFieldValue.getFieldName());
				if (customFieldObjectsByName == null || customFieldObjectsByName.isEmpty()) {
					return null;
				}

				CustomField elligibleField = null;
				for (CustomField customField : customFieldObjectsByName) {
					if (customField.isGlobal()) {
						return customField;
					}
					if (customField.isAllProjects()) {
						elligibleField = customField;
					} else {
						log.warn("Custom field '%s' is not available for all projects. Can't use it, going to create a new one.",
								customFieldValue.getFieldName());
					}
				}

				if (elligibleField == null) {
					return null;
				}
				final List<FieldConfigScheme> schemes = fieldConfigSchemeManager.getConfigSchemesForField(elligibleField);
				if (schemes.isEmpty()) {
					log.warn("Cannot retrieve config scheme for field " + customFieldValue.getFieldName());
					log.warn("Importer is unable to extend custom field context automatically.");
					return null;
				}
				if (schemes.size() > 1) {
					log.warn("Custom field '" + customFieldValue.getFieldName() + "' has multiple configuration schemes.");
					log.warn("Importer is unable to extend custom field context automatically.");
					return null;
				}
				final FieldConfigScheme scheme = Iterables.getOnlyElement(schemes);
				final FieldConfig onlyConfig = scheme.getOneAndOnlyConfig();
				if (onlyConfig == null) {
					log.warn("Unable to retrieve the single config associated with custom field '" + customFieldValue.getFieldName() + "'.");
					log.warn("Importer is unable to extend custom field context automatically.");
					return null;
				}

				final FieldConfigScheme configScheme = new FieldConfigScheme.Builder(scheme)
						.setConfigs(Collections.singletonMap("-1", onlyConfig))
						.toFieldConfigScheme();
				fieldConfigSchemeManager.updateFieldConfigScheme(configScheme,
						Collections.singletonList(treeManager.getRootNode()),
						elligibleField);
				customFieldManager.refresh();

				log.log("Making custom field '%s' global.", customFieldValue.getFieldName());

				return customFieldManager.getCustomFieldObject(elligibleField.getId());
			}
		} catch (final Exception e) {
			// Can't get the custom field
			log.warn(e, "Can't get custom field %s", customFieldValue.getFieldName());
		}

		return null;
	}

	public CustomField createCustomField(final ExternalCustomFieldValue customFieldValue) throws ExternalException {
		final String fieldName = customFieldValue.getFieldName();
		final String fieldType = customFieldValue.getFieldType();
		final String searcherType = customFieldValue.getSearcherType();

		if (customFieldValue.getFieldName() == null) {
			throw new ExternalException("Name must not be null");
		}

		// Create a new custom field
		return createCustomField(fieldName, fieldType, searcherType);
	}

	private CustomField createCustomField(final String customFieldName, @Nullable final String type, @Nullable final String searcherType)
			throws ExternalException {
		try {
			// Create cf of the correct type
			CustomFieldType cfType;
			CustomFieldSearcher searcher = null;

			if (StringUtils.isNotEmpty(type)) {
				cfType = customFieldManager.getCustomFieldType(type);
			} else {
				cfType = customFieldManager.getCustomFieldType(TEXT_FIELD_TYPE);
			}

			if (cfType == null) {
				throw new ExternalException("Cannot create custom field [" + customFieldName + "] because its type ["
						+ type + "] is not recognized by this JIRA instance");				}

			if (StringUtils.isNotEmpty(searcherType)) {
				searcher = customFieldManager.getCustomFieldSearcher(searcherType);
			} else {
				List searchers = customFieldManager.getCustomFieldSearchers(cfType);
				if (searchers != null && !searchers.isEmpty()) {
					searcher = (CustomFieldSearcher) searchers.get(0);
				}
			}

			final CustomField customField = customFieldManager
					.createCustomField(customFieldName, customFieldName, cfType, searcher,
							EasyList.build(GlobalIssueContext.getInstance()), EasyList.buildNull());

			associateCustomFieldWithScreen(customField, null);

			return customField;

		} catch (final GenericEntityException e) {
			throw new ExternalException(e);
		}
	}

	public void associateCustomFieldWithScreen(final CustomField customField, @Nullable FieldScreen screen) {
		if (screen == null) {
			screen = fieldScreenManager.getFieldScreen(FieldScreen.DEFAULT_SCREEN_ID);
		}

		if ((screen != null) && (screen.getTabs() != null) && !screen.getTabs().isEmpty()) {
			final FieldScreenTab tab = screen.getTab(0);
			tab.addFieldScreenLayoutItem(customField.getId());
		}
	}

	/**
	 * Adds an external comment to an issue and portentially dispatches and event about this.  The issue updated date will be set to now via this method.
	 *
	 * @param issue		   the issue GV
	 * @param externalComment the external Comment to add
	 * @param dispatchEvent   whether to dispatch an issue updated event
	 * @throws com.atlassian.jira.plugins.importer.external.ExternalException
	 *          if something bad happens
	 */
	public void addComments(final UserProvider userProvider,
			final Issue issue, final ExternalComment externalComment, final boolean dispatchEvent,
			final ImportLogger log) throws ExternalException {
		addComments(userProvider, issue, externalComment, dispatchEvent, true, log);
	}

	/**
	 * Adds an external comment to an issue and portentially dispatches and event about this and also updates the issue
	 * updated date.  When doing a CSV import one might not want the event dispatched nor the issue updated date changed.
	 *
	 * @param issue				the issue GV
	 * @param externalComment	  the external Comment to add
	 * @param dispatchEvent		whether to dispatch an issue updated event
	 * @param tweakIssueUpdateDate whether to tweak the issue updated date or not
	 * @throws com.atlassian.jira.plugins.importer.external.ExternalException
	 *          if something bad happens
	 */
	public void addComments(final UserProvider userProvider,
			final Issue issue, final ExternalComment externalComment, final boolean dispatchEvent,
			final boolean tweakIssueUpdateDate, final ImportLogger log) throws ExternalException {
		final String username = externalComment.getUsername();
		User commenter = userProvider.getUser(username);
		if (commenter == null) {
			log.warn("Commenter named %s not found. Creating issue with currently logged in user instead", username);
			commenter = authenticationContext.getLoggedInUser();
		}

		if (!permissionManager.hasPermission(Permissions.COMMENT_ISSUE, issue, commenter)) {
			final String s = "Comment not created. The user ("
					+ commenter.getDisplayName() + ") do not have permission to comment on an issue in project: "
					+ projectManager.getProjectObj(issue.getLong("project")).getName();
			log.warn(s);
			throw new ExternalException(s);
		} else {
			try {
				final String author = commenter.getName();
				final Date timePerformed =
						externalComment.getCreated() != null ? externalComment.getCreated().toDate() : null;
				commentManager.create(issue, author, author,
						externalComment.getBody(), null, null,
						timePerformed, timePerformed, dispatchEvent, tweakIssueUpdateDate);
			} catch (final Exception e) {
				log.warn(e, "Unable to create comment %s. Comment not created", externalComment);
				throw new ExternalException(e);
			}
		}
	}

	protected String cleanFileName(String fileName) {
		fileName = StringUtils.replaceChars(fileName, "\\/", "--");
		return StringUtils.stripStart(fileName, "-");
	}

	public void attachFile(final UserProvider userProvider, final ExternalAttachment externalAttachment, final MutableIssue issue,
			final ImportLogger log) throws ExternalException {
		final String username = externalAttachment.getAttacher();
		User user = userProvider.getUser(username);
		if (user == null) {
			log.warn("User named %s not found. attaching to issue with currently logged in user instead", username);
			user = authenticationContext.getLoggedInUser();
		}

		try {
			final String fileName = cleanFileName(externalAttachment.getFileName());
			attachmentManager.createAttachment(externalAttachment.getAttachedFile(),
					fileName, GENERIC_CONTENT_TYPE, user, issue.getGenericValue(), Collections.EMPTY_MAP,
					new java.sql.Timestamp(
							externalAttachment.getAttachedDate().getMillis())); // needs to be java.sql.Timestamp

			if (StringUtils.isNotBlank(externalAttachment.getDescription())) {
				ExternalComment comment = new ExternalComment(getAuthenticationContext().getI18nHelper()
						.getText("jira-importer-plugins.external.utils.attachment.description",
								fileName, externalAttachment.getDescription()),
						user.getName(),
						externalAttachment.getAttachedDate()

				);
				addComments(userProvider, issue, comment, false, log);
			}
		} catch (final AttachmentException e) {
			throw new ExternalException(e);
		} catch (final GenericEntityException e) {
			throw new ExternalException(e);
		}
	}

	public void createIssueLink(final String sourceKey, final String destinationKey, final String linkName,
			boolean subtask, final ImportLogger log)
			throws ExternalException {
		final Issue sourceIssue = issueManager.getIssueObject(sourceKey);
		final Issue destinationIssue = issueManager.getIssueObject(destinationKey);

		if ((sourceIssue != null) && (destinationIssue != null)) {
			try {
				if (subtask) {
					if (!sourceIssue.getIssueTypeObject().isSubTask()) {
						log.fail(null, "Issue '" + sourceIssue.getKey() + "' is not of a sub-task type ("
								+ sourceIssue.getIssueTypeObject().getName() + "). It will NOT be a sub-task of the issue '"
								+ destinationIssue.getKey() + "'");
						return;
					}
					subTaskManager.createSubTaskIssueLink(destinationIssue, sourceIssue,
							authenticationContext.getLoggedInUser());
					log.log("Created parent-child (sub-task) relationship between " + sourceKey + " and " + destinationKey);
				} else {
					final IssueLinkType linkType = createOrFindLinkType(linkName);
					if (linkType != null) {
						issueLinkManager.createIssueLink(sourceIssue.getId(), destinationIssue.getId(), linkType.getId(), null);
						log.log("Created link '" + linkType.getName() + "'  between " + sourceKey + " and " + destinationKey);
					}

				}
			} catch (final CreateException e) {
				throw new ExternalException(e.getMessage(), e);
			}
		}
	}

	private IssueLinkType createOrFindLinkType(final String name) throws ExternalException {
		Collection<IssueLinkType> dependency = issueLinkTypeManager.getIssueLinkTypesByName(name);
		if (dependency.isEmpty()) {
			try {
				issueLinkTypeManager.createIssueLinkType(name, name, name, null);
			} catch (IllegalArgumentException e) {
				throw new ExternalException(e);
			}
			dependency = issueLinkTypeManager.getIssueLinkTypesByName(name);
		}
		return dependency.iterator().next();
	}

	@Nullable
	private List<Version> retrieveVersionsFromExternalIds(final List<String> externalVersionIds,
			final ExternalProject project, final ImportObjectIdMappings mappings, final ImportLogger log) {
		if ((externalVersionIds != null) && !externalVersionIds.isEmpty()) {
			final List<Version> versions = new ArrayList<Version>();
			for (final String externalVersionId : externalVersionIds) {
				final Version version = mappings.getVersion(project.getName(), externalVersionId);
				if (version != null) {
					versions.add(version);
				} else {
					log.log(
							"Version does not exist for project: '" + project + "' : and version: '" + externalVersionId
									+ "'.");
				}
			}
			return versions;
		} else {
			return null;
		}
	}

	@Nullable
	private List<GenericValue> retrieveComponents(@Nullable final List<String> externalComponents,
			final ExternalProject project, final ImportObjectIdMappings mappings, ImportLogger log) {
		if ((externalComponents != null) && !externalComponents.isEmpty()) {
			final List<GenericValue> components = Lists.newArrayList();
			for (final String externalComponent : externalComponents) {
				final ProjectComponent component = mappings.getComponent(project.getName(), externalComponent);
				if (component != null) {
					components.add(component.getGenericValue());
				} else {
					log.log("Component does not exist for project:" + project + " : and component: "
							+ externalComponent + ".");
				}
			}
			return components;
		} else {
			return null;
		}
	}

	public Issue convertExternalIssueToIssue(final UserProvider userProvider,
			final ExternalIssue externalIssue, final ExternalProject project,
			final ImportObjectIdMappings mappings, final ImportLogger log) throws ImportException {
		final MutableIssue issue = newIssueInstance();

		issue.setProjectId(project.getJiraId());

		issue.setIssueTypeId(preFilterIssueType(externalIssue, project).getId());

		if (issue.getSecurityLevelId() == null) {
			try {
				issue.setSecurityLevelId(issueSecurityLevelManager
						.getSchemeDefaultSecurityLevel(
								projectManager.getProjectObj(project.getJiraId()).getGenericValue()));
			} catch (GenericEntityException e) {
				throw new ImportException("Can't get default security level for a project", e);
			}
		}

		issue.setReporter(userProvider.getUser(externalIssue.getReporter()));
		issue.setAssignee(userProvider.getUser(externalIssue.getAssignee()));

		final String summary = preFilterSummary(externalIssue);
		if (StringUtils.isBlank(summary)) {
			throw new ImportException("Blank summary detected. Such issue cannot be imported");
		}
		issue.setSummary(summary);
		issue.setDescription(externalIssue.getDescription());
		issue.setEnvironment(externalIssue.getEnvironment());

		issue.setPriorityId(externalIssue.getPriority());

		issue.setResolutionId(externalIssue.getResolution());

		issue.setCreated(toTimeStamp(externalIssue.getCreated()));
		issue.setUpdated(toTimeStamp(externalIssue.getUpdated()));
		issue.setDueDate(toTimeStamp(externalIssue.getDuedate()));
		//if created and updated date aren't being set form the external issue, init them to now.  An issue must have
		//a created and updated time.
		if (issue.getCreated() == null) {
			issue.setCreated(new Timestamp(System.currentTimeMillis()));
		}
		if (issue.getUpdated() == null) {
			issue.setUpdated(issue.getCreated());
		}
		//NOTE: This HAS to come after the setResolutionId() call, otherwhise that will override this date.
		//if we have a resolution date and the resolution is set, set it on the issue
		if ((externalIssue.getResolutionDate() != null) && (externalIssue.getResolution() != null)) {
			issue.setResolutionDate(toTimeStamp(externalIssue.getResolutionDate()));
		}
		//otherwise, if we don't have a resolution date, however the issue is resolved, fall back to the last updated date
		//which will either be now or whatever was imported.
		else if (externalIssue.getResolution() != null) {
			issue.setResolutionDate(issue.getUpdated());
		}

		issue.setVotes(externalIssue.getVotes());

		// Only add to external estimates if time tracking is turned on
		if (fieldManager.isTimeTrackingOn()) {
			issue.setOriginalEstimate(externalIssue.getOriginalEstimate());
			issue.setTimeSpent(externalIssue.getTimeSpent());
			issue.setEstimate(externalIssue.getEstimate());
		}

		// Deal with versions
		issue.setAffectedVersions(
				retrieveVersionsFromExternalIds(externalIssue.getAffectedVersions(), project, mappings, log));
		issue.setFixVersions(retrieveVersionsFromExternalIds(externalIssue.getFixedVersions(), project, mappings, log));

		issue.setLabels(externalIssue.getLabels());

		issue.setComponents(retrieveComponents(externalIssue.getExternalComponents(), project, mappings, log));

		return issue;
	}

	public MutableIssue newIssueInstance() {
		return issueFactory.getIssue();
	}

	private String preFilterSummary(final ExternalIssue externalIssue) {
		final String summary = externalIssue.getSummary();
		if (StringUtils.isBlank(summary) && !StringUtils.isNotBlank(externalIssue.getDescription())) {
			return StringUtils.abbreviate(externalIssue.getDescription(), 250);
		} else {
			return summary;
		}
	}

	private IssueType preFilterIssueType(final ExternalIssue externalIssue, final ExternalProject project)
			throws ImportException {
		final String issueType = externalIssue.getIssueType();

		if (StringUtils.isNotBlank(issueType)) {
			final IssueType type = constantsManager.getIssueTypeObject(issueType);
			if (type == null) {
				throw new ImportException("No issue type mapping found for value '" + issueType + "'");
			}
			return type;
		} else {
			final IssueType issueTypeDefault = issueTypeSchemeManager
					.getDefaultValue(projectManager.getProjectObj(project.getJiraId()).getGenericValue());
			if (issueTypeDefault != null) {
				return issueTypeDefault;
			} else {
				throw new ImportException("No default issue type found for project: " + project.getKey());
			}
		}
	}

	@Nullable
	private Timestamp toTimeStamp(@Nullable final DateTime date) {
		if (date != null) {
			return new Timestamp(date.getMillis());
		} else {
			return null;
		}
	}

	public static String getTextDataFromMimeMessage(final String s) {
		try {
			final Session session = Session.getDefaultInstance(new Properties());

			final InputStream is = new ByteArrayInputStream(s.getBytes());

			final MimeMessage message = new MimeMessage(session, is);
			return getTextDataFromMimeMessage(message);
		} catch (final Exception e) {
			throw new ExternalRuntimeException(e);
		}
	}

	private static String getTextDataFromMimeMessage(final MimeMessage message) throws IOException, MessagingException {
		final Object content = message.getContent();
		if (content instanceof Multipart) {
			final Multipart mm = (Multipart) content;
			final StringBuffer sb = new StringBuffer();
			for (int i = 0; i < mm.getCount(); i++) {
				final BodyPart part = mm.getBodyPart(i);
				if (part.getContentType().startsWith("text") && (i == 0)) {
					sb.append(getCleanedContent(part));
				} else if ((part.getFileName() == null) && (part.getContent() instanceof MimeMessage)) {
					sb.append(getTextDataFromMimeMessage((MimeMessage) part.getContent()));
				}
			}

			return sb.toString();
		} else {
			return getCleanedContent(message);
		}
	}

	private static String getCleanedContent(final Part part) throws IOException, MessagingException {
		final String content = part.getContent().toString();
		if (part.getContentType().startsWith("text/html")) {
			final String noTags = HTMLUtils.stripTags(content);

			try {
				return StringEscapeUtils.unescapeHtml(noTags);
			} catch (final NumberFormatException e) {
				return ImportUtils.stripHTMLStrings(noTags);
			}
		} else {
			return content;
		}
	}

	public DateUtils getDateUtils() {
		return new DateUtils(authenticationContext.getI18nHelper().getDefaultResourceBundle());
	}

	public boolean isIssueLinkingOn() {
		return applicationProperties.getOption(APKeys.JIRA_OPTION_ISSUELINKING);
	}

	public boolean areAttachmentsEnabled() {
		return applicationProperties.getOption(APKeys.JIRA_OPTION_ALLOWATTACHMENTS);
	}

	public boolean areSubtasksEnabled() {
		return applicationProperties.getOption(APKeys.JIRA_OPTION_ALLOWSUBTASKS);
	}

	public String getPotentialProjectKey(String name, int keylength) {
		name = StringUtils.deleteWhitespace(name);

		String potentialKey;
		if (name.length() < keylength) {
			potentialKey = name + generatePaddingString(keylength - name.length());
		} else {
			potentialKey = name.substring(0, keylength);
		}

		if (projectManager.getProjectObjByKey(potentialKey) != null) {
			return getPotentialProjectKey(name, ++keylength);
		} else {
			return potentialKey;
		}
	}

	public String getProjectKey(final String name) {
		final Project project = projectManager.getProjectObjByName(name);
		if (project == null) {
			return getPotentialProjectKey(name.toUpperCase(), 3); //minimum key length of 3
		}
		return project.getKey();
	}


	private String generatePaddingString(final int length) {
		final char[] padarray = new char[length];
		for (int i = 0; i < length; i++) {
			padarray[i] = 'J';
		}
		return String.valueOf(padarray);
	}

	public JiraAuthenticationContext getAuthenticationContext() {
		return authenticationContext;
	}

	public ProjectManager getProjectManager() {
		return projectManager;
	}

	public IssueLinkTypeManager getIssueLinkTypeManager() {
		return issueLinkTypeManager;
	}

	public WorkflowSchemeManager getWorkflowSchemeManager() {
		return workflowSchemeManager;
	}

	public WorkflowManager getWorkflowManager() {
		return workflowManager;
	}

	public ConstantsManager getConstantsManager() {
		return constantsManager;
	}

	public FieldManager getFieldManager() {
		return fieldManager;
	}

	public CustomFieldManager getCustomFieldManager() {
		return customFieldManager;
	}

	public PermissionManager getPermissionManager() {
		return permissionManager;
	}

	public IssueFactory getIssueFactory() {
		return issueFactory;
	}

	public IssueManager getIssueManager() {
		return issueManager;
	}

	public OfBizDelegator getGenericDelegator() {
		return genericDelegator;
	}

	public ApplicationProperties getApplicationProperties() {
		return applicationProperties;
	}

	public boolean isTimeTrackingOn() {
		return fieldManager.isTimeTrackingOn();
	}
}
