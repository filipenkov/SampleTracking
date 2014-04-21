/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.mantis.transformer;

import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.customfields.converters.DateTimePickerConverter;
import com.atlassian.jira.plugins.importer.external.CustomFieldConstants;
import com.atlassian.jira.plugins.importer.external.beans.ExternalComment;
import com.atlassian.jira.plugins.importer.external.beans.ExternalCustomField;
import com.atlassian.jira.plugins.importer.external.beans.ExternalIssue;
import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.atlassian.jira.plugins.importer.imports.AbstractIssueTransformer;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.atlassian.jira.plugins.importer.imports.importer.SingleStringResultTransformer;
import com.atlassian.jira.plugins.importer.imports.mantis.MantisConfigBean;
import com.atlassian.jira.plugins.importer.imports.mantis.MantisFieldConstants;
import com.atlassian.jira.plugins.importer.imports.mantis.config.MultipleSelectionValueMapper;
import com.atlassian.jira.plugins.importer.imports.mantis.config.ResolutionValueMapper;
import com.atlassian.jira.plugins.importer.imports.mantis.config.StatusValueMapper;
import com.atlassian.jira.util.RegexpUtils;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class IssueTransformerVer118OrOlder extends AbstractIssueTransformer<MantisConfigBean> {
	protected final ExternalProject externalProject;

	private final String mantisUrl;
	private final DateTimePickerConverter dateTimePicker;
    private MantisConfigBean.TimestampHelper timestampHelper;

    public IssueTransformerVer118OrOlder(String mantisUrl,
			final MantisConfigBean configBean,
			final ExternalProject externalProject, final DateTimePickerConverter dateTimePicker,
			ImportLogger importLogger) {
		super(configBean, importLogger);
		this.mantisUrl = mantisUrl;
        this.timestampHelper = new MantisConfigBean.TimestampHelper();
		this.dateTimePicker = dateTimePicker;
		this.externalProject = externalProject;
	}

	// -------------------------------------------------------------------------------------------------- Public Methods
	public String getSqlQuery() {
		return "SELECT *, "
				+ "(SELECT username FROM mantis_user_table p WHERE p.id=b.reporter_id limit 1) reporter_name,"
				+ "(SELECT username FROM mantis_user_table p WHERE p.id=b.handler_id limit 1) assignee_name"
				+ " FROM mantis_bug_table b LEFT OUTER JOIN mantis_bug_text_table t ON (b.bug_text_id=t.id) WHERE project_id=" + externalProject.getId();
	}

	public ExternalIssue transform(final ResultSet rs) throws SQLException {
		final String summary = rs.getString("summary");

		if (StringUtils.isEmpty(summary)) {
			log.warn("Summary is blank and is not imported");
			return null;
		}

		final String oldId = rs.getString("id");

		ExternalIssue externalIssue = new ExternalIssue();
		externalIssue.setExternalId(oldId);
		externalIssue.setSummary(escapeMantisString(summary));
		externalIssue.setDescription(escapeMantisString(rs.getString("description")));
		// 10 as a severity means a new feature
		externalIssue.setIssueType("10".equals(rs.getString("severity")) ? IssueFieldConstants.NEWFEATURE_TYPE : IssueFieldConstants.BUG_TYPE);

		externalIssue.setEnvironment(getEnvironment(rs));

		final String category = rs.getString("category");
		if (StringUtils.isNotBlank(category)) {
			externalIssue.setComponents(Collections.singleton(category));
		}

		final String version = rs.getString("version");
		if (StringUtils.isNotBlank(version)) {
			externalIssue.setAffectedVersions(Collections.singleton(version));
		}

		final String fixFor = rs.getString("fixed_in_version");
		if (StringUtils.isNotBlank(fixFor)) {
			externalIssue.setFixedVersions(Collections.singleton(fixFor));
		}

		externalIssue.setReporter(configBean.getUsernameForLoginName(rs.getString("reporter_name")));
		externalIssue.setAssignee(configBean.getUsernameForLoginName(rs.getString("assignee_name")));

		externalIssue.setCreated(timestampHelper.getTimestamp(rs, "date_submitted"));

		externalIssue.setLabels(configBean.getJdbcConnection().queryDb(
				new SingleStringResultTransformer("SELECT name FROM mantis_tag_table AS t"
						+ " INNER JOIN mantis_bug_tag_table AS bt ON (t.id=bt.tag_id) WHERE bug_id=" + oldId)));

		final Date closed = timestampHelper.getTimestamp(rs, "last_updated");
		final Date resolved = timestampHelper.getTimestamp(rs, "last_updated");
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

		setCustomFieldValues(externalIssue, rs, oldId);
		
		final String status = configBean
				.getValueMappingHelper().getValueMappingForImport(StatusValueMapper.FIELD,
				MantisFieldConstants.getStatusName(rs.getString("status")));

		externalIssue.setStatus(status);

		// Set resolution only if status is mapped to Resolved or Closed
		// By defaul all issues in Mantis have resolution so we need to be specific here
		if (status.equals(IssueFieldConstants.RESOLVED_STATUS)
				|| status.equals(Integer.toString(IssueFieldConstants.RESOLVED_STATUS_ID))
				|| status.equals(IssueFieldConstants.CLOSED_STATUS)
				|| status.equals(Integer.toString(IssueFieldConstants.CLOSED_STATUS_ID))) {

			externalIssue.setResolution(configBean
				.getValueMappingHelper().getValueMappingForImport(ResolutionValueMapper.FIELD,
				MantisFieldConstants.getResolutionName(rs.getString("resolution"))));
		}

		// Deal with comments
		final CommentTransformer commentTransformer = new CommentTransformer(oldId, configBean);
		final List<ExternalComment> comments = configBean.getJdbcConnection().queryDb(commentTransformer);
		externalIssue.setComments(comments);

		// Deal with work estimates
		//final long originalEstimate = rs.getLong("estimated_time") * SECONDS_IN_HOUR;
		//final long currentEstimate = rs.getLong("remaining_time") * SECONDS_IN_HOUR;

		// Deal with watchers
		final WatchersTransformer watchersTransformer = new WatchersTransformer(oldId, configBean);
		externalIssue.setWatchers(configBean.getJdbcConnection().queryDb(watchersTransformer));

		//externalIssue.setOriginalEstimate(originalEstimate > 0 ? new Long(originalEstimate) : null);
		//externalIssue.setEstimate(currentEstimate > 0 ? new Long(currentEstimate) : null);

		// Deal with worklog
		/*
		final WorklogTransformer worklogTransformer = new WorklogTransformer(oldId, configBean);
		final List<ExternalWorklog> worklog = connectionBean.queryDb(worklogTransformer);
		externalIssue.setWorklogs(worklog);
		*/

		return externalIssue;
	}

	protected String getEnvironment(ResultSet rs) throws SQLException {
		final String os = rs.getString("os");
		final String platform = rs.getString("platform");

		final StringBuilder environment = new StringBuilder();

		if (StringUtils.isNotBlank(os)) {
			environment.append("Operating System: ")
				.append(os)
				.append("\n");
		}

		if (StringUtils.isNotBlank(platform)) {
			environment.append("Platform: ")
				.append(platform);
		}

		return environment.toString();
	}

	public static String escapeMantisString(String str) {
		String newString = RegexpUtils
			.replaceAll(str, "<a (?:target=\"_new\" )?href=['\"](?:mailto:)?(.*?)['\"](?: target=\"_new\")?>.*</a>", "$1");

		newString = RegexpUtils.replaceAll(newString, "&quot;", "\"");
		newString = RegexpUtils.replaceAll(newString, "&lt;", "<");
		newString = RegexpUtils.replaceAll(newString, "&gt;", ">");
		newString = RegexpUtils.replaceAll(newString, "&amp;", ">");
		
		return newString;
	}

	@Nullable
	protected Object getCustomFieldValue(ResultSet rs, String bugId, ExternalCustomField customField)
			throws SQLException {
		if ("bug_url".equals(customField.getId())) {
			return mantisUrl + (mantisUrl.endsWith("/") ? "" : "/") + "view.php?id=" + bugId;
		}

		final String cfTypeKey = customField.getTypeKey();
		if (StringUtils.isNumeric(customField.getId())) {
			List<String> dbValue = jdbcConnection.queryDb(
				new SingleStringResultTransformer(
						"SELECT value FROM mantis_custom_field_string_table WHERE field_id="
						+ customField.getId() + " AND bug_id=" + bugId));

			if (dbValue.size() == 1 && StringUtils.isNotBlank(dbValue.get(0))) {
				if (CustomFieldConstants.MULTISELECT_FIELD_TYPE.equals(cfTypeKey)
						|| CustomFieldConstants.MULTICHECKBOXES_FIELD_TYPE.equals(cfTypeKey)) {
					return getMappedValues(customField, dbValue.get(0));
				} else if (CustomFieldConstants.SELECT_FIELD_TYPE.equals(cfTypeKey)) {
					return configBean.getValueMappingHelper().getValueMappingForImport(
							customField.getName(), dbValue.get(0));
				} else if (CustomFieldConstants.DATETIME_FIELD_TYPE.equals(cfTypeKey)) {
					return dateTimePicker.getString(new Date(Long.valueOf(dbValue.get(0)) * 1000));
				} else {
					return configBean.getValueMappingHelper().getValueMappingForImport(customField.getName(),
							dbValue.get(0));
				}
			}
		} else {
            String dbValue = rs.getString(customField.getId());
			// if it's not numeric custom field id refers to the column in result set
            if (MantisConfigBean.PRIORITY_FIELD.equals(customField.getId())) {
                dbValue = MantisFieldConstants.getPriorityName(dbValue);
            }
            if (MantisConfigBean.SEVERITY_FIELD.equals(customField.getId())) {
                dbValue = MantisFieldConstants.getSeverityName(dbValue);
            }
			return escapeMantisString(configBean.getValueMappingHelper().getValueMappingForImport(customField.getId(),
					dbValue));
		}
		return null;
	}

	private Collection<String> getMappedValues(final ExternalCustomField customField, final String s) {
		return Lists.transform(MultipleSelectionValueMapper.getValues(s),
				new Function<String, String>() {
					public String apply(@Nonnull String from) {
						return configBean.getValueMappingHelper().getValueMappingForImport(customField.getName(), from);
					}
				});
	}

}
