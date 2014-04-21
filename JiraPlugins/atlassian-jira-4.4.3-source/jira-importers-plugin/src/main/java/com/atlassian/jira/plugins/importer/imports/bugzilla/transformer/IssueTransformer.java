/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.bugzilla.transformer;

import com.atlassian.jira.issue.customfields.converters.DateTimePickerConverter;
import com.atlassian.jira.plugins.importer.external.CustomFieldConstants;
import com.atlassian.jira.plugins.importer.external.beans.ExternalComment;
import com.atlassian.jira.plugins.importer.external.beans.ExternalCustomField;
import com.atlassian.jira.plugins.importer.external.beans.ExternalIssue;
import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.atlassian.jira.plugins.importer.external.beans.ExternalWorklog;
import com.atlassian.jira.plugins.importer.imports.AbstractIssueTransformer;
import com.atlassian.jira.plugins.importer.imports.bugzilla.BugzillaConfigBean;
import com.atlassian.jira.plugins.importer.imports.bugzilla.config.ResolutionValueMapper;
import com.atlassian.jira.plugins.importer.imports.bugzilla.config.StatusValueMapper;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.atlassian.jira.plugins.importer.imports.importer.SingleStringResultTransformer;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public class IssueTransformer extends AbstractIssueTransformer<BugzillaConfigBean> {
	private static final int SECONDS_IN_HOUR = 3600;

	private final String bugzillaUrl;
	private final ExternalProject externalProject;
	private final DateTimePickerConverter dateTimePicker;

	public IssueTransformer(final String bugzillaUrl,
			final BugzillaConfigBean configBean, final ExternalProject externalProject,
			final DateTimePickerConverter dateTimePicker, ImportLogger importLogger) {
		super(configBean, importLogger);
		this.bugzillaUrl = bugzillaUrl;
		this.externalProject = externalProject;
		this.dateTimePicker = dateTimePicker;
	}

	// -------------------------------------------------------------------------------------------------- Public Methods
	public String getSqlQuery() {
		return "SELECT *, "
				+ "(select login_name from profiles p where p.userid=b.reporter limit 1) AS reporter_name,"
				+ "(select login_name from profiles p where p.userid=b.assigned_to limit 1) AS assignee_name,"
				+ "(select thetext from longdescs l where l.bug_id=b.bug_id order by bug_when asc limit 1) AS thetext,"
				+ "(select c.name from components c where c.id=b.component_id limit 1) AS component"
				+ " FROM bugs AS b WHERE product_id=" + externalProject.getId();
	}

	public ExternalIssue transform(final ResultSet rs) throws SQLException {
		final String summary = rs.getString("short_desc");

		if (StringUtils.isEmpty(summary)) {
			log.warn("Summary is blank and is not imported");
			return null;
		}

		final String oldId = rs.getString("bug_id");

		ExternalIssue externalIssue = new ExternalIssue();
		externalIssue.setExternalId(oldId);
		externalIssue.setSummary(summary);
		externalIssue.setDescription(rs.getString("thetext"));
		externalIssue.setIssueType("enhancement".equals(rs.getString("bug_severity")) ? "Improvement" : "Bug");

		final StringBuilder environment = new StringBuilder();
		environment.append("Operating System: ")
				.append(rs.getString("op_sys")).append("\nPlatform: ").append(rs.getString("rep_platform"));

		final String url = rs.getString("bug_file_loc");
		if (!StringUtils.isEmpty(url)) {
			environment.append("\nURL: ").append(url);
		}

		externalIssue.setEnvironment(environment.toString());

		final String component = rs.getString("component");
		if (StringUtils.isNotBlank(component)) {
			externalIssue.setExternalComponents(Lists.newArrayList(component));
		}

		final String version = rs.getString("version");
		if (StringUtils.isNotBlank(version)) {
			externalIssue.setAffectedVersions(Lists.newArrayList(version));
		}

		final String fixFor = rs.getString("target_milestone");
		if (StringUtils.isNotBlank(fixFor)) {
			externalIssue.setFixedVersions(Lists.newArrayList(fixFor));
		}

		String reporter = rs.getString("reporter_name");
		if (StringUtils.isNotEmpty(reporter)) {
			externalIssue.setReporter(configBean.getUsernameForLoginName(reporter));
		}

		String assignee = rs.getString("assignee_name");
		if (StringUtils.isNotEmpty(assignee)) {
			externalIssue.setAssignee(configBean.getUsernameForLoginName(assignee));
		}

		externalIssue.setCreated(rs.getTimestamp("creation_ts"));
		externalIssue.setDuedate(rs.getTimestamp("deadline"));

		externalIssue.setLabels(createLabels(jdbcConnection.queryDb(
				new SingleStringResultTransformer("SELECT keyworddefs.name FROM keyworddefs "
						+ " INNER JOIN keywords ON keywordid = keyworddefs.id WHERE keywords.bug_id = " + oldId))));

		final Timestamp closed = rs.getTimestamp("delta_ts");
		final Timestamp resolved = rs.getTimestamp("delta_ts");
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

		externalIssue.setStatus(configBean
				.getValueMappingHelper().getValueMappingForImport(StatusValueMapper.FIELD, rs.getString("bug_status")));
		externalIssue.setResolution(configBean
				.getValueMappingHelper().getValueMappingForImport(ResolutionValueMapper.FIELD,
				rs.getString("resolution")));

		// Deal with comments
		final CommentTransformer commentTransformer = new CommentTransformer(oldId, configBean);
		final List<ExternalComment> comments = jdbcConnection.queryDb(commentTransformer);
		externalIssue.setExternalComments(comments);

		// Deal with work estimates
		final long originalEstimate = rs.getLong("estimated_time") * SECONDS_IN_HOUR;
		final long currentEstimate = rs.getLong("remaining_time") * SECONDS_IN_HOUR;

		// Deal with votes
		final VotesTransformer votesTransformer = new VotesTransformer(oldId, configBean);
		externalIssue.setVoters(jdbcConnection.queryDb(votesTransformer));

		// Deal with watchers
		final WatchersTransformer watchersTransformer = new WatchersTransformer(oldId, configBean);
		externalIssue.setWatchers(jdbcConnection.queryDb(watchersTransformer));

		externalIssue.setOriginalEstimate(originalEstimate > 0 ? originalEstimate : null);
		externalIssue.setEstimate(currentEstimate > 0 ? currentEstimate : null);

		// Deal with worklog
		final WorklogTransformer worklogTransformer = new WorklogTransformer(oldId, configBean);
		final List<ExternalWorklog> worklog = jdbcConnection.queryDb(worklogTransformer);
		externalIssue.setWorklog(worklog);

		return externalIssue;
	}

    @Nullable
	protected Object getCustomFieldValue(ResultSet rs, String bugId, ExternalCustomField customField)
            throws SQLException {
		if ("bug_url".equals(customField.getId())) {
			return bugzillaUrl + (bugzillaUrl.endsWith("/") ? "" : "/") + "show_bug.cgi?id=" + bugId;
		}

		if (CustomFieldConstants.MULTISELECT_FIELD_TYPE.equals(customField.getTypeKey())) {
            return jdbcConnection.queryDb(new CustomMultiSelectTransformer(bugId, configBean, customField.getId()));
        } else if (CustomFieldConstants.DATETIME_FIELD_TYPE.equals(customField.getTypeKey())) {
            return dateTimePicker.getString(rs.getTimestamp(customField.getId()));
        }

        final String dbValue = rs.getString(customField.getId());

        if (StringUtils.isBlank(dbValue)) {
            return null;
        }

        String mappedValue = configBean.getValueMappingHelper().getValueMappingForImport(
                customField.getId(), dbValue);

        // we ignore --- in Bugzilla that's commonly treated as a NULL or lack of selection
        if ("---".equals(dbValue)) {
            return null;
        }

        return StringUtils.trimToNull(mappedValue);
    }
}
