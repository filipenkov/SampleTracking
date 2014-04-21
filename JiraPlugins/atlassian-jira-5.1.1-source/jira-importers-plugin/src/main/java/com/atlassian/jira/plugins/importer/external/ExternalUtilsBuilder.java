/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.external;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.context.manager.JiraContextTreeManager;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.plugins.importer.managers.CreateIssueLinkManager;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowSchemeManager;

public class ExternalUtilsBuilder {
	private ProjectManager projectManager;
	private IssueManager issueManager;
	private JiraAuthenticationContext authenticationContext;
	private VersionManager versionManager;
	private ConstantsManager constantsManager;
	private WorkflowManager workflowManager;
	private PermissionManager permissionManager;
	private IssueFactory issueFactory;
	private AttachmentManager attachmentManager;
	private IssueLinkTypeManager issueLinkTypeManager;
	private CreateIssueLinkManager issueLinkManager;
	private FieldManager fieldManager;
	private ApplicationProperties applicationProperties;
	private IssueTypeSchemeManager issueTypeSchemeManager;
	private CommentManager commentManager;
	private WorkflowSchemeManager workflowSchemeManager;
	private OfBizDelegator genericDelegator;
	private IssueSecurityLevelManager issueSecurityLevelManager;
	private SubTaskManager subTaskManager;

	public ExternalUtilsBuilder setProjectManager(ProjectManager projectManager) {
		this.projectManager = projectManager;
		return this;
	}

	public ExternalUtilsBuilder setIssueManager(IssueManager issueManager) {
		this.issueManager = issueManager;
		return this;
	}

	public ExternalUtilsBuilder setAuthenticationContext(JiraAuthenticationContext authenticationContext) {
		this.authenticationContext = authenticationContext;
		return this;
	}

	public ExternalUtilsBuilder setVersionManager(VersionManager versionManager) {
		this.versionManager = versionManager;
		return this;
	}

	public ExternalUtilsBuilder setConstantsManager(ConstantsManager constantsManager) {
		this.constantsManager = constantsManager;
		return this;
	}

	public ExternalUtilsBuilder setWorkflowManager(WorkflowManager workflowManager) {
		this.workflowManager = workflowManager;
		return this;
	}

	public ExternalUtilsBuilder setPermissionManager(PermissionManager permissionManager) {
		this.permissionManager = permissionManager;
		return this;
	}

	public ExternalUtilsBuilder setIssueFactory(IssueFactory issueFactory) {
		this.issueFactory = issueFactory;
		return this;
	}

	public ExternalUtilsBuilder setAttachmentManager(AttachmentManager attachmentManager) {
		this.attachmentManager = attachmentManager;
		return this;
	}

	public ExternalUtilsBuilder setIssueLinkTypeManager(IssueLinkTypeManager issueLinkTypeManager) {
		this.issueLinkTypeManager = issueLinkTypeManager;
		return this;
	}

	public ExternalUtilsBuilder setIssueLinkManager(CreateIssueLinkManager issueLinkManager) {
		this.issueLinkManager = issueLinkManager;
		return this;
	}

	public ExternalUtilsBuilder setFieldManager(FieldManager fieldManager) {
		this.fieldManager = fieldManager;
		return this;
	}

	public ExternalUtilsBuilder setApplicationProperties(ApplicationProperties applicationProperties) {
		this.applicationProperties = applicationProperties;
		return this;
	}

	public ExternalUtilsBuilder setIssueTypeSchemeManager(IssueTypeSchemeManager issueTypeSchemeManager) {
		this.issueTypeSchemeManager = issueTypeSchemeManager;
		return this;
	}

	public ExternalUtilsBuilder setCommentManager(CommentManager commentManager) {
		this.commentManager = commentManager;
		return this;
	}

	public ExternalUtilsBuilder setWorkflowSchemeManager(WorkflowSchemeManager workflowSchemeManager) {
		this.workflowSchemeManager = workflowSchemeManager;
		return this;
	}

	public ExternalUtils createExternalUtils() {
		return new ExternalUtils(projectManager, issueManager, authenticationContext,
				versionManager, constantsManager, workflowManager, permissionManager,
				issueFactory, attachmentManager, issueLinkTypeManager, issueLinkManager, fieldManager,
				applicationProperties, issueTypeSchemeManager, commentManager,
				workflowSchemeManager, genericDelegator, issueSecurityLevelManager, subTaskManager);
	}

	public ExternalUtilsBuilder setGenericDelegator(OfBizDelegator genericDelegator) {
		this.genericDelegator = genericDelegator;
		return this;
	}

	public void setIssueSecurityLevelManager(IssueSecurityLevelManager issueSecurityLevelManager) {
		this.issueSecurityLevelManager = issueSecurityLevelManager;
	}

	public void setSubTaskManager(SubTaskManager subTaskManager) {
		this.subTaskManager = subTaskManager;
	}
}