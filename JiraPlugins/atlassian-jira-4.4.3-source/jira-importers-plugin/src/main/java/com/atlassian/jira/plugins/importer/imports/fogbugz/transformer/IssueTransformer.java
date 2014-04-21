/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.fogbugz.transformer;

import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.customfields.converters.DateTimePickerConverter;
import com.atlassian.jira.plugins.importer.SQLRuntimeException;
import com.atlassian.jira.plugins.importer.external.CustomFieldConstants;
import com.atlassian.jira.plugins.importer.external.beans.ExternalComment;
import com.atlassian.jira.plugins.importer.external.beans.ExternalCustomField;
import com.atlassian.jira.plugins.importer.external.beans.ExternalCustomFieldValue;
import com.atlassian.jira.plugins.importer.external.beans.ExternalIssue;
import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.atlassian.jira.plugins.importer.imports.AbstractIssueTransformer;
import com.atlassian.jira.plugins.importer.imports.AbstractResultSetTransformer;
import com.atlassian.jira.plugins.importer.imports.fogbugz.FogBugzConfigBean;
import com.atlassian.jira.plugins.importer.imports.fogbugz.config.PriorityValueMapper;
import com.atlassian.jira.plugins.importer.imports.fogbugz.config.ResolutionValueMapper;
import com.atlassian.jira.plugins.importer.imports.fogbugz.config.StatusValueMapper;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.atlassian.jira.plugins.importer.imports.importer.SingleStringResultTransformer;
import com.atlassian.jira.plugins.importer.web.JdbcConnection;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public class IssueTransformer extends AbstractResultSetTransformer<ExternalIssue> {
	private static final int SECONDS_IN_HOUR = 3600;

	private final JdbcConnection jdbcConnection;
	private final FogBugzConfigBean configBean;
	private final ExternalProject externalProject;
	private final DateTimePickerConverter dateTimePicker;

	public IssueTransformer(final FogBugzConfigBean configBean, final ExternalProject externalProject,
			final DateTimePickerConverter dateTimePicker, ImportLogger importLogger) {
		super(importLogger);
		this.configBean = configBean;
		this.jdbcConnection = configBean.getJdbcConnection();
		this.externalProject = externalProject;
		this.dateTimePicker = dateTimePicker;
	}

	public String getSqlQuery() {
		try {
			return String.format(
					"SELECT " + "b.ixBug, " + "b.sTitle, " + "issueType.sCategory, " + "b.fOpen, " + "s.sStatus, "
					+ "%s AS priority, " + "p.sProject, " + "a.sArea, " + "b.sVersion, " + "ff.sFixFor, "
					+ "reporter.sFullName AS Reporter, " + "assignee.sFullName AS Assignee, " + "b.sComputer, "
					+ "b.hrsOrigEst, " + "b.hrsCurrEst, " + "b.hrsElapsed, " + "b.sCustomerEmail, " + "b.sReleaseNotes, "
					+ "b.dtOpened, " + "b.dtClosed, " + "b.dtResolved, " + "b.dtDue "
					+ "FROM Bug b, Project p, Area a, Priority pr, Person reporter, FixFor ff, Person assignee, Category issueType, Status s "
					+ "WHERE b.ixProject = " + externalProject.getId() + " AND " + "b.ixProject = p.ixProject AND "
					+ "b.ixArea = a.ixArea AND " + "b.ixPriority = pr.ixPriority AND " + "b.ixFixFor = ff.ixFixFor AND "
					+ "b.ixPersonOpenedBy = reporter.ixPerson AND " + "b.ixPersonAssignedTo = assignee.ixPerson AND "
					+ "b.ixCategory = issueType.ixCategory AND " + "b.ixStatus = s.ixStatus " + "ORDER BY b.ixBug",
					(PriorityValueMapper.isSqlServer(jdbcConnection)
							? "(CAST(pr.ixPriority AS VARCHAR(20)) + '-' + pr.sPriority)" : "CONCAT(pr.ixPriority,CONCAT('-',pr.sPriority))"));
		} catch (SQLException e) {
			throw new SQLRuntimeException(e);
		}
	}

	@Nullable
	public ExternalIssue transform(final ResultSet rs) throws SQLException {
		final String summary = rs.getString("sTitle");

		if (StringUtils.isBlank(summary)) {
			log.warn("Issuename is blank and is not imported");
			return null;
		}

		final String oldId = rs.getString("ixBug");
		final ExternalIssue externalIssue = new ExternalIssue();
		externalIssue.setExternalId(oldId);
		externalIssue.setSummary(summary);
		//externalIssue.setDescription(summary);
		final String issueType = rs.getString("sCategory");
		final String translatedIssueType = configBean.getValueMappingHelper()
				.getValueMappingForImport("sCategory", issueType);
		externalIssue.setIssueType(translatedIssueType);

		final String priority = rs.getString("priority");
		final String translatedPriority = configBean.getValueMappingHelper()
				.getValueMappingForImport(PriorityValueMapper.PRIORITY_FIELD, priority);
		externalIssue.setPriority(translatedPriority);

		externalIssue.setExternalComponents(Lists.<String>newArrayList(rs.getString("sArea")));

		final String version = rs.getString("sVersion");
		if (StringUtils.isNotBlank(version)) {
			externalIssue.setAffectedVersions(Lists.<String>newArrayList(version));
		}

		final String fixFor = rs.getString("sFixFor");
		if (StringUtils.isNotBlank(fixFor)) {
			externalIssue.setFixedVersions(Lists.<String>newArrayList(fixFor));
		}

		externalIssue.setReporter(configBean.getUsernameForFullName(rs.getString("Reporter")));
		externalIssue.setAssignee(configBean.getUsernameForFullName(rs.getString("Assignee")));

		externalIssue.setCreated(rs.getTimestamp("dtOpened"));
		externalIssue.setDuedate(rs.getTimestamp("dtDue"));

		externalIssue.setLabels(AbstractIssueTransformer.createLabels(jdbcConnection.queryDb(
				new SingleStringResultTransformer("SELECT sTag FROM Tag t"
						+ " INNER JOIN TagAssociation ta ON t.ixTag = ta.ixTag WHERE ta.ixBug = " + oldId))));

		final Timestamp closed = rs.getTimestamp("dtClosed");
		final Timestamp resolved = rs.getTimestamp("dtResolved");
		//don't need to do a null check here for the resolved date, since the ExternalUtils.convertExternalIssueToIssue()
		//method used by the DefaultJiraDataImporter will fall back to the last updated date anyway, if this is null
		//and the issue is marked as resolved.
		externalIssue.setResolutionDate(resolved);
		if ((closed != null) && (resolved == null)) {
			externalIssue.setUpdated(closed);
		} else if ((closed == null) && (resolved != null)) {
			externalIssue.setUpdated(resolved);
		} else if (closed != null) {
			if (closed.after(resolved)) {
				externalIssue.setUpdated(closed);
			} else {
				externalIssue.setUpdated(resolved);
			}
		}

		externalIssue.setExternalCustomFieldValues(getCustomFieldValues(rs, oldId));

		final String fogBugzStatus = rs.getString("sStatus");
		final String cleanedResolution = ResolutionValueMapper.getCleanedResolution(fogBugzStatus);
		final String cleanedStatus = StatusValueMapper.getCleanedStatus(fogBugzStatus);

		externalIssue.setStatus(configBean.getValueMappingHelper().getValueMappingForImport(
				StatusValueMapper.FIELD, rs.getBoolean("fOpen") ? cleanedStatus : IssueFieldConstants.CLOSED_STATUS));
		if (cleanedResolution != null) {
			externalIssue.setResolution(configBean.getValueMappingHelper().getValueMappingForImport(
					ResolutionValueMapper.FIELD, cleanedResolution));
		}

		// Deal with comments
		final CommentTransformer commentTransformer = new CommentTransformer(oldId, configBean, log);
		final List<ExternalComment> comments = configBean.getJdbcConnection().queryDb(commentTransformer);
		externalIssue.setExternalComments(comments);

		// Deal with work estimates
		final long originalEstimate = rs.getLong("hrsOrigEst") * SECONDS_IN_HOUR;
		final long timeSpent = rs.getLong("hrsElapsed") * SECONDS_IN_HOUR;

		final long currentEstimate = (rs.getLong("hrsCurrEst") * SECONDS_IN_HOUR) - timeSpent;

		externalIssue.setOriginalEstimate(originalEstimate > 0 ? originalEstimate : null);
		externalIssue.setEstimate(currentEstimate > 0 ? currentEstimate : null);
		externalIssue.setTimeSpent(timeSpent > 0 ? timeSpent : null);

		return externalIssue;
	}

	protected List<ExternalCustomFieldValue> getCustomFieldValues(ResultSet rs, String bugId) throws SQLException {
		// Add the custom fields if the field mapping is not null
		final List<ExternalCustomFieldValue> customFields = Lists.newArrayList();
		for (ExternalCustomField customField : configBean.getCustomFields()) {
			String value;
			/*if (CustomFieldConstants.MULTISELECT_FIELD_TYPE.equals(customField.getTypeKey())) {
				value = MultiSelectCFType.getStringFromTransferObject(
						connectionBean.queryDb(new CustomMultiSelectTransformer(bugId, configBean, customField.getId())));
			} else */

			if (CustomFieldConstants.SELECT_FIELD_TYPE.equals(customField.getTypeKey())) {
				value = configBean.getValueMappingHelper().getValueMappingForImport(
						customField.getId(), rs.getString(customField.getId()));
				value = "---".equals(value) ? null : value;
			} else if (CustomFieldConstants.DATETIME_FIELD_TYPE.equals(customField.getTypeKey())) {
				value = dateTimePicker.getString(rs.getTimestamp(customField.getId()));
			} else {
				value = configBean.getValueMappingHelper().getValueMappingForImport(customField.getId(),
						rs.getString(customField.getId()));
			}

			if (StringUtils.isBlank(value)) {
				continue;
			}

			final String mapping = configBean.getFieldMapping(customField.getId());
			// map to a specific custom field (if it exists)
			final String customFieldName = StringUtils.isNotEmpty(mapping) ? mapping : customField.getName();

			final ExternalCustomFieldValue customFieldValue = new ExternalCustomFieldValue(customFieldName,
					customField.getTypeKey(), customField.getSearcherKey(), value);

			customFields.add(customFieldValue);
		}

		return customFields;
	}
}
