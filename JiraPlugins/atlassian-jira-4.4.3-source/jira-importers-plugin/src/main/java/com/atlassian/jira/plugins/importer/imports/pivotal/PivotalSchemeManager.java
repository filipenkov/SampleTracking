/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.imports.pivotal;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.context.manager.JiraContextTreeManager;
import com.atlassian.jira.issue.customfields.CustomFieldUtils;
import com.atlassian.jira.issue.fields.ConfigurableField;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.plugins.importer.XmlUtil;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.ofbiz.GenericValueUtils;
import com.atlassian.jira.web.action.admin.issuetypes.IssueTypeManageableOption;
import com.atlassian.jira.web.action.admin.issuetypes.ManageableOptionType;
import com.atlassian.jira.web.action.admin.statuses.ViewStatuses;
import com.atlassian.jira.workflow.ConfigurableJiraWorkflow;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opensymphony.workflow.loader.WorkflowDescriptor;
import com.opensymphony.workflow.loader.WorkflowLoader;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.jaxen.JaxenException;
import org.jaxen.jdom.JDOMXPath;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.Action;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PivotalSchemeManager {
	private static final String issueTypeSchemeName = "PT Issue Type Scheme";
	private static final String issueTypeSchemeDesc = "Issue Type Scheme used for projects imported from Pivotal Tracker";

	private static final String mainWorkflowName = "PT Workflow";
	private static final String subtaskWorkflowName = "PT Subtask Workflow";

	static final String mainWorkflowSource = "/pivotal/pt_workflow.xml";
	static final String subtaskWorkflowSource = "/pivotal/pt_subtask_workflow.xml";

	private static final String workflowSchemeName = "PT Workflow Scheme";
	private static final String workflowSchemeDescription = "Workflow Scheme for projects imported from Pivotal Tracker";

	static final String BUG = IssueFieldConstants.BUG_TYPE;
	static final String FEATURE = IssueFieldConstants.NEWFEATURE_TYPE;
	static final String CHORE = "Chore";
	static final String RELEASE = "Release";
	static final String SUBTASK = "Sub-task";

	private static final Map<String, String> defaultTypesAndIcons = ImmutableMap.of(
			BUG, "/images/icons/bug.gif",
			FEATURE, "/images/icons/newfeature.gif",
			CHORE, "/images/icons/task.gif",
			RELEASE, "/images/icons/requirement.gif",
			SUBTASK, "/images/icons/issue_subtask.gif"
	);

	static final String NOT_YET_STARTED = "Not Yet Started";
	static final String STARTED = "Started";
	static final String FINISHED = "Finished";
	static final String DELIVERED = "Delivered";
	static final String ACCEPTED = "Accepted";
	static final String REJECTED = "Rejected";

	private static final Map<String, String> pivotalStatuses = ImmutableMap.<String, String>builder()
			.put(NOT_YET_STARTED, "/images/icons/status_generic.gif")
			.put(STARTED, "/images/icons/status_inprogress.gif")
			.put(FINISHED, "/images/icons/status_resolved.gif")
			.put(DELIVERED, "/images/icons/status_up.gif")
			.put(ACCEPTED, "/images/icons/status_closed.gif")
			.put(REJECTED, "/images/icons/status_reopened.gif")
			.build();

	private final IssueTypeSchemeManager issueTypeSchemeManager;
	private final ConstantsManager constantsManager;
	private final SubTaskManager subTaskManager;
	private final FieldConfigSchemeManager configSchemeManager;
	private final ManageableOptionType manageableOptionType;
	private final FieldManager fieldManager;
	private final JiraContextTreeManager treeManager;
	private final WorkflowManager workflowManager;
	private final WorkflowSchemeManager workflowSchemeManager;
	private final JiraAuthenticationContext authenticationContext;



	@SuppressWarnings({"UnusedDeclaration"})
	public PivotalSchemeManager(IssueTypeSchemeManager issueTypeSchemeManager,
			SubTaskManager subTaskManager,
			ConstantsManager constantsManager,
			FieldConfigSchemeManager configSchemeManager,
			FieldManager fieldManager,
			WorkflowManager workflowManager,
			WorkflowSchemeManager workflowSchemeManager,
			JiraAuthenticationContext authenticationContext) {
		this(issueTypeSchemeManager, subTaskManager, constantsManager, configSchemeManager,	fieldManager, workflowManager,
				workflowSchemeManager, authenticationContext,
				ComponentManager.getComponentInstanceOfType(IssueTypeManageableOption.class),
				ComponentManager.getComponentInstanceOfType(JiraContextTreeManager.class));
	}

	public PivotalSchemeManager(IssueTypeSchemeManager issueTypeSchemeManager,
			SubTaskManager subTaskManager,
			ConstantsManager constantsManager,
			FieldConfigSchemeManager configSchemeManager,
			FieldManager fieldManager,
			WorkflowManager workflowManager,
			WorkflowSchemeManager workflowSchemeManager,
			JiraAuthenticationContext authenticationContext,
			IssueTypeManageableOption issueTypeManageableOption,
			JiraContextTreeManager jiraContextTreeManager) {
		this.issueTypeSchemeManager = issueTypeSchemeManager;
		this.constantsManager = constantsManager;
		this.subTaskManager = subTaskManager;
		this.configSchemeManager = configSchemeManager;
		this.fieldManager = fieldManager;
		this.authenticationContext = authenticationContext;
		this.workflowSchemeManager = workflowSchemeManager;
		this.manageableOptionType = issueTypeManageableOption;
		this.treeManager = jiraContextTreeManager;
		this.workflowManager = workflowManager;
	}

	public void setPTSchemesForProject(Project project) throws Exception {
		if (!subTaskManager.isSubTasksEnabled()) {
			try {
				subTaskManager.enableSubTasks();
			} catch (CreateException e) {
				throw new RuntimeException("Cannot enable subtasks", e);
			}
		}

		assignPTIssueTypeScheme(project);
	    assignPTWorkflowScheme(project);
	}

	void assignPTIssueTypeScheme(Project project) {
		final FieldConfigScheme ptScheme = getPTScheme();

		final List<GenericValue> projectsList = ptScheme.getAssociatedProjects();
		final Long[] existing = GenericValueUtils.transformToLongIds(projectsList);
		final Long[] allPTProjects = (Long[]) ArrayUtils.add(existing, project.getId());


		// Cargo cult, see com.atlassian.jira.web.action.admin.issuetypes.AbstractManageIssueTypeOptionsAction
		final List<JiraContextNode> context = CustomFieldUtils.buildJiraIssueContexts(false,
				null,
				allPTProjects,
				treeManager);
		final ConfigurableField field = fieldManager.getConfigurableField(manageableOptionType.getFieldId());
		configSchemeManager.updateFieldConfigScheme(ptScheme, context, field);
        fieldManager.refresh();
	}

	FieldConfigScheme getPTScheme() {
		@SuppressWarnings({"unchecked"})
		final List<FieldConfigScheme> allSchemas = issueTypeSchemeManager.getAllSchemes();
		for (FieldConfigScheme scheme : allSchemas) {
			if (issueTypeSchemeName.equals(scheme.getName())) {
				return scheme;
			}
		}

		final List<String> options = Lists.newArrayListWithCapacity(defaultTypesAndIcons.size());
		for (String issueTypeName : defaultTypesAndIcons.keySet()) {
			final IssueConstant issueConstant = constantsManager
					.getIssueConstantByName(ConstantsManager.ISSUE_TYPE_CONSTANT_TYPE, issueTypeName);
			if (issueConstant != null) {
				options.add(issueConstant.getId());
			} else {
				final String icon = defaultTypesAndIcons.get(issueTypeName);
				final String style = issueTypeSchemeName.equals(SUBTASK)
						? SubTaskManager.SUB_TASK_ISSUE_TYPE_STYLE : null;
				final GenericValue issueType;
				try {
					issueType = constantsManager.createIssueType(issueTypeName, null, style, "", icon);
				} catch (CreateException e) {
					throw new RuntimeException("Cannpt create issue type: " + issueTypeName, e);
				}
				options.add(issueType.getString("id"));
			}
		}

		final FieldConfigScheme scheme = issueTypeSchemeManager
				.create(issueTypeSchemeName, issueTypeSchemeDesc, options);
		issueTypeSchemeManager.setDefaultValue(scheme.getOneAndOnlyConfig(), String.valueOf(IssueFieldConstants.NEWFEATURE_TYPE_ID));
		return scheme;
	}

	void assignPTWorkflowScheme(Project project) {
		workflowSchemeManager.addSchemeToProject(project, getPTWorkflowScheme());
	}

	Scheme getPTWorkflowScheme() {
		final Scheme existingScheme = workflowSchemeManager.getSchemeObject(workflowSchemeName);
		if (existingScheme != null) {
			return existingScheme;
		}
		final JiraWorkflow ptWorkflow = getPTWorkflow(mainWorkflowName, mainWorkflowSource);
		final JiraWorkflow ptSubtaskWorkflow = getPTWorkflow(subtaskWorkflowName, subtaskWorkflowSource);

		try {
			final GenericValue schemeGV = workflowSchemeManager.createScheme(workflowSchemeName, workflowSchemeDescription);
			final String subtaskId = constantsManager
					.getIssueConstantByName(ConstantsManager.ISSUE_TYPE_CONSTANT_TYPE, SUBTASK).getId();
			workflowSchemeManager.addWorkflowToScheme(schemeGV, ptSubtaskWorkflow.getName(), subtaskId);
			workflowSchemeManager.addWorkflowToScheme(schemeGV, ptWorkflow.getName(), "0"); // all issue types
		} catch (GenericEntityException e) {
			throw new RuntimeException("Error creating workflow scheme for projects imported from Pivotal Tracker", e);
		}

		return workflowSchemeManager.getSchemeObject(workflowSchemeName);
	}

	JiraWorkflow getPTWorkflow(final String workflowName, final String workflowSource) {
		final JiraWorkflow workflow = workflowManager.workflowExists(workflowName)
				? workflowManager.getWorkflow(workflowName) : null;
		if (workflow != null) {
			return workflow;
		}

		loadPTWorkflow(workflowName, workflowSource);
		return workflowManager.getWorkflow(workflowName);
	}

	void loadPTWorkflow(final String workflowName, final String workflowSource) {
		final Map<String, String> nameToIdMapping = getPTStatusNameToIdMapping();
		final WorkflowDescriptor workflowDescriptor;
		try {
			final String substitutedXml = substituteStatusIds(getClass().getResourceAsStream(workflowSource), nameToIdMapping);

			workflowDescriptor = WorkflowLoader.load(IOUtils.toInputStream(substitutedXml), true);
		} catch (Exception e) {
			throw new RuntimeException("Error creating workflow for projects imported from Pivotal Tracker", e);
		}
		final ConfigurableJiraWorkflow newWorkflow = new ConfigurableJiraWorkflow(workflowName, workflowDescriptor, workflowManager);
		workflowManager.createWorkflow(authenticationContext.getLoggedInUser(), newWorkflow);
	}

	String substituteStatusIds(InputStream input, Map<String, String> mapping) throws JDOMException, IOException, JaxenException {
		final Document document = XmlUtil.getSAXBuilder().build(input);
		final Element rootElement = document.getRootElement();
		@SuppressWarnings({"unchecked"})
		final List<Element> statusIdNodes = new JDOMXPath("//step/meta[@name='jira.status.id'][. >= 10000]").selectNodes(rootElement);
		for (Element statusNode : statusIdNodes) {
			final String name = statusNode.getParentElement().getAttributeValue("name");
			final String mappedId = mapping.get(name);
			statusNode.setText(mappedId);
		}

		return new XMLOutputter(Format.getRawFormat()).outputString(document);
	}

	Map<String, String> getPTStatusNameToIdMapping() {
		final Map<String, String> mapping = Maps.newHashMapWithExpectedSize(pivotalStatuses.size());
		for (String statusName : pivotalStatuses.keySet()) {
			Status statusByName = constantsManager.getStatusByName(statusName);
			if (statusByName == null) {
				final ViewStatuses vs = new ViewStatuses(null) {
					@Override
					protected String redirectToView() {
						return Action.SUCCESS;
					}
				};
				vs.setIconurl(pivotalStatuses.get(statusName));
				vs.setName(statusName);
				 // todo: i18n
				final String description = MessageFormat.format("Mapping for {0} in Pivotal Tracker", statusName);
				vs.setDescription(description);

				final String result;
				try {
					result = vs.doAddStatus();
				} catch (Exception e) {
					throw new RuntimeException("Error adding status " + statusName, e);
				}
				if (Action.ERROR.equals(result)) {
					throw new RuntimeException("Error adding status " + statusName); // todo add error handling
				}
				statusByName = constantsManager.getStatusByName(statusName);
			}
			mapping.put(statusName, statusByName.getId());
		}
		return mapping;
	}

	public boolean isPTCompatible(Project project) {
		final Set<String> ptStatusNames = getRegisteredStatusNames();
		final JiraWorkflow workflow = workflowManager.getWorkflow(project.getId(), "0");
		final Set<String> linkedStatusNames = Sets.newHashSet(
				Iterables.transform(workflow.getLinkedStatusObjects(), new Function<Status, String>() {
					@Override
					public String apply(Status input) {
						return input.getName();
					}
				}));
		return linkedStatusNames.containsAll(ptStatusNames);
	}

	static Set<String> getRegisteredStatusNames() {
		return pivotalStatuses.keySet();
	}

}
