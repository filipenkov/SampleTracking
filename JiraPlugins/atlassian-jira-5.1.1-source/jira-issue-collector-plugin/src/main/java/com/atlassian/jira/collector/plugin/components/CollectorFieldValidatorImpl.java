package com.atlassian.jira.collector.plugin.components;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderTab;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderer;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.operation.IssueOperations;
import com.atlassian.jira.jelly.tag.admin.CreateCustomField;
import com.atlassian.jira.project.Project;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class CollectorFieldValidatorImpl implements CollectorFieldValidator {

	private static final Set<String> FIELDS_TO_INCLUDE = ImmutableSet.of(
			IssueFieldConstants.SUMMARY,
			IssueFieldConstants.DESCRIPTION,
			IssueFieldConstants.COMPONENTS,
			IssueFieldConstants.AFFECTED_VERSIONS,
			IssueFieldConstants.ENVIRONMENT,
			IssueFieldConstants.PRIORITY);

	private static final Set<String> CUSTOM_FIELDS_TYPES_TO_INCLUDE = ImmutableSet.of(
			CreateCustomField.FIELD_TYPE_PREFIX + "datetime",
			CreateCustomField.FIELD_TYPE_PREFIX + "radiobuttons",
			CreateCustomField.FIELD_TYPE_PREFIX + "select",
			CreateCustomField.FIELD_TYPE_PREFIX + "url",
			CreateCustomField.FIELD_TYPE_PREFIX + "multiversion",
			CreateCustomField.FIELD_TYPE_PREFIX + "cascadingselect",
			CreateCustomField.FIELD_TYPE_PREFIX + "version",
			CreateCustomField.FIELD_TYPE_PREFIX + "textfield",
			CreateCustomField.FIELD_TYPE_PREFIX + "datepicker",
			CreateCustomField.FIELD_TYPE_PREFIX + "textarea",
			CreateCustomField.FIELD_TYPE_PREFIX + "multicheckboxes",
			CreateCustomField.FIELD_TYPE_PREFIX + "multiselect",
			CreateCustomField.FIELD_TYPE_PREFIX + "float");

	private static final Set<String> REQUIRED_BY_DEFAULT = ImmutableSet.of(
			IssueFieldConstants.SECURITY,
			IssueFieldConstants.ISSUE_TYPE,
			IssueFieldConstants.REPORTER);

	private final CustomFieldManager customFieldManager;
	private final IssueFactory issueFactory;
	private final FieldScreenRendererFactory fieldScreenRendererFactory;


	public CollectorFieldValidatorImpl(final CustomFieldManager customFieldManager, final IssueFactory issueFactory, FieldScreenRendererFactory fieldScreenRendererFactory) {
		this.customFieldManager = customFieldManager;
		this.issueFactory = issueFactory;
		this.fieldScreenRendererFactory = fieldScreenRendererFactory;
	}

	@Override
	public Map<String, Set<String>> getRequiredInvalidFieldsForProject(final User loggedUser, final Project project) {
		final Map<String, Set<String>> requiredFileds = Maps.newHashMap();
		for (final IssueType issueType : project.getIssueTypes()) {
			requiredFileds.put(issueType.getId(), getRequiredInvalidFieldsForIssueType(loggedUser, project, issueType.getId()));
		}
		return requiredFileds;
	}


	@Override
	public Set<String> getRequiredInvalidFieldsForIssueType(final User loggedUser, final Project project, final String issueTypeId) {
		final MutableIssue issue = issueFactory.getIssue();
		issue.setProjectId(project.getId());
		issue.setIssueTypeId(issueTypeId);

		final FieldScreenRenderer fieldScreenRenderer = fieldScreenRendererFactory.getFieldScreenRenderer(loggedUser, issue, IssueOperations.CREATE_ISSUE_OPERATION, false);
		final List<FieldScreenRenderTab> fieldScreenRenderTabs = fieldScreenRenderer.getFieldScreenRenderTabs();
		final Set<String> allowedCustomFieldIds = getAllowedCustomFieldIds(project, issue.getIssueTypeObject().getId());
		final Set<String> notAllowedFields = Sets.newHashSet();

		for (final FieldScreenRenderTab fieldScreenRenderTab : fieldScreenRenderTabs)
		{
			for (final FieldScreenRenderLayoutItem fsrli : fieldScreenRenderTab.getFieldScreenRenderLayoutItems())
			{
				final FieldLayoutItem fieldLayoutItem = fsrli.getFieldLayoutItem();
				if (fieldLayoutItem.isRequired() && getDefaultValue(issue, fieldLayoutItem) == null) {
					final String fieldId = fieldLayoutItem.getOrderableField().getId();
					if (!isFieldAllowed(fieldId) && !allowedCustomFieldIds.contains(fieldId)) {
						notAllowedFields.add(fieldLayoutItem.getOrderableField().getName());
					}
				}
			}
		}
		return notAllowedFields;
	}

	private Object getDefaultValue(final MutableIssue issue, final FieldLayoutItem fieldLayoutItem) {
		return fieldLayoutItem.getOrderableField().getDefaultValue(issue);
	}

	@Override
	public boolean isFieldAllowedInCustomCollector(final String fieldId) {
		return FIELDS_TO_INCLUDE.contains(fieldId);
	}

	@Override
	public Set<String> getAllowedCustomFieldIds(final Project project, final String issueType) {
		final List<CustomField> customFieldObjects = customFieldManager.getCustomFieldObjects(project.getId(), issueType);

		return ImmutableSet.copyOf(Iterables.transform(
				Iterables.filter(customFieldObjects, new Predicate<CustomField>() {
					@Override
					public boolean apply(final CustomField customField) {
						return CUSTOM_FIELDS_TYPES_TO_INCLUDE.contains(customField.getCustomFieldType().getKey());
					}
				}),
				new Function<CustomField, String>() {
					@Override
					public String apply(final CustomField customField) {
						return customField.getId();
					}
				}));
	}

	private boolean isFieldAllowed(final String fieldId) {
		return FIELDS_TO_INCLUDE.contains(fieldId) || REQUIRED_BY_DEFAULT.contains(fieldId);
	}

}
