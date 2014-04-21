/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.trac.transformer;

import com.atlassian.jira.issue.label.LabelParser;
import com.atlassian.jira.plugins.importer.SqlUtils;
import com.atlassian.jira.plugins.importer.external.CustomFieldConstants;
import com.atlassian.jira.plugins.importer.external.beans.ExternalComment;
import com.atlassian.jira.plugins.importer.external.beans.ExternalCustomField;
import com.atlassian.jira.plugins.importer.external.beans.ExternalIssue;
import com.atlassian.jira.plugins.importer.imports.AbstractIssueTransformer;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.atlassian.jira.plugins.importer.imports.importer.SingleStringResultTransformer;
import com.atlassian.jira.plugins.importer.imports.trac.TracConfigBean;
import com.atlassian.jira.plugins.importer.imports.trac.TracWikiConverter;
import com.atlassian.jira.plugins.importer.imports.trac.config.ResolutionValueMapper;
import com.atlassian.jira.plugins.importer.imports.trac.config.StatusValueMapper;
import com.atlassian.jira.plugins.importer.imports.trac.config.TypeValueMapper;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class IssueTransformer extends AbstractIssueTransformer<TracConfigBean> {

	private final TracWikiConverter wikiConverter;

	public IssueTransformer(final TracConfigBean configBean, ImportLogger importLogger, TracWikiConverter wikiConverter) {
		super(configBean, importLogger);
		this.wikiConverter = wikiConverter;
	}

	// -------------------------------------------------------------------------------------------------- Public Methods
	public String getSqlQuery() {
		return "SELECT id,type,time,changetime,component,severity,priority,owner,reporter,cc,version,milestone,status,"
				+ "resolution,summary,description,keywords FROM ticket";
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
		externalIssue.setSummary(summary);
		externalIssue.setDescription(wikiConverter.convert(rs.getString("description"), log));

		final String issueType = rs.getString("type");
		final String translatedIssueType = configBean.getValueMappingHelper()
				.getValueMappingForImport(TypeValueMapper.FIELD, issueType);
		externalIssue.setIssueType(translatedIssueType);

		final String component = rs.getString("component");
		if (StringUtils.isNotBlank(component)) {
			externalIssue.setComponents(Collections.singleton(component));
		}

		final String version = rs.getString("version");
		if (StringUtils.isNotBlank(version)) {
			externalIssue.setAffectedVersions(Collections.singleton(version));
		}

		String reporter = rs.getString("reporter");
		if (StringUtils.isNotEmpty(reporter)) {
			externalIssue.setReporter(configBean.getUsernameForEmail(reporter));
		}

		String assignee = rs.getString("owner");
		if (StringUtils.isNotEmpty(assignee)) {
			externalIssue.setAssignee(configBean.getUsernameForEmail(assignee));
		}

		externalIssue.setCreated(configBean.getTimestamp(rs, "time"));

		externalIssue.setLabels(LabelParser.buildFromString(new LabelParser.CreateFromString<String>() {
            @Override
            public String create(String s) {
                return s;
            }
        }, rs.getString("keywords")));

		final Date closed = configBean.getTimestamp(rs, "changetime");
		final Date resolved = configBean.getTimestamp(rs, "changetime");
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
				.getValueMappingHelper().getValueMappingForImport(StatusValueMapper.FIELD, rs.getString("status")));
		externalIssue.setResolution(configBean
				.getValueMappingHelper().getValueMappingForImport(ResolutionValueMapper.FIELD,
				rs.getString("resolution")));

		// Deal with comments
		final CommentTransformer commentTransformer = new CommentTransformer(oldId, configBean, wikiConverter, log);
		final List<ExternalComment> comments = jdbcConnection.queryDb(commentTransformer);
		externalIssue.setComments(comments);

		// Deal with watchers
		final WatchersTransformer watchersTransformer = new WatchersTransformer(oldId);
		externalIssue.setWatchers(jdbcConnection.queryDbAppendCollection(watchersTransformer));

		return externalIssue;
	}

    @Nullable
	protected Object getCustomFieldValue(ResultSet rs, String bugId, ExternalCustomField customField)
            throws SQLException {
		String dbValue;

		if (SqlUtils.getColumnNames(rs.getMetaData()).contains(customField.getId())) {
			dbValue = rs.getString(customField.getId());
		} else {
			List<String> result = jdbcConnection.queryDb(new SingleStringResultTransformer(
					"SELECT value FROM ticket_custom WHERE name = '" + customField.getId()
							+ "' AND ticket=" + bugId));
			dbValue = Iterables.getFirst(result, null);
		}

		if (CustomFieldConstants.MULTICHECKBOXES_FIELD_TYPE.equals(customField.getTypeKey())) {
			dbValue = "1".equals(dbValue) ? customField.getName() : null;
		}

        if (StringUtils.isBlank(dbValue)) {
            return null;
        }

        String mappedValue = configBean.getValueMappingHelper().getValueMappingForImport(
                customField.getId(), dbValue);

		if (mappedValue != null
			&& (CustomFieldConstants.MULTISELECT_FIELD_TYPE.equals(customField.getTypeKey())
				|| CustomFieldConstants.MULTICHECKBOXES_FIELD_TYPE.equals(customField.getTypeKey()))) {
			return Lists.newArrayList(mappedValue);
		}

        return StringUtils.trimToNull(mappedValue);
    }

}
