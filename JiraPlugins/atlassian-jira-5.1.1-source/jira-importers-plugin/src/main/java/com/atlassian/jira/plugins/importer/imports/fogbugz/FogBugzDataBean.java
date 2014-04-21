/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.fogbugz;

import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.issue.customfields.converters.DateTimePickerConverter;
import com.atlassian.jira.plugins.importer.SqlUtils;
import com.atlassian.jira.plugins.importer.external.beans.ExternalAttachment;
import com.atlassian.jira.plugins.importer.external.beans.ExternalComponent;
import com.atlassian.jira.plugins.importer.external.beans.ExternalIssue;
import com.atlassian.jira.plugins.importer.external.beans.ExternalLink;
import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.atlassian.jira.plugins.importer.external.beans.ExternalUser;
import com.atlassian.jira.plugins.importer.external.beans.ExternalVersion;
import com.atlassian.jira.plugins.importer.imports.fogbugz.transformer.AffectsVersionTransformer;
import com.atlassian.jira.plugins.importer.imports.fogbugz.transformer.AttachmentTransformerFactory;
import com.atlassian.jira.plugins.importer.imports.fogbugz.transformer.ComponentTransformer;
import com.atlassian.jira.plugins.importer.imports.fogbugz.transformer.DuplicateLinksTransformer;
import com.atlassian.jira.plugins.importer.imports.fogbugz.transformer.FixForVersionTransformer;
import com.atlassian.jira.plugins.importer.imports.fogbugz.transformer.IssueTransformer;
import com.atlassian.jira.plugins.importer.imports.fogbugz.transformer.ProjectTransformer;
import com.atlassian.jira.plugins.importer.imports.fogbugz.transformer.RelatedLinksTransformer;
import com.atlassian.jira.plugins.importer.imports.fogbugz.transformer.SubcaseLinksTransformer;
import com.atlassian.jira.plugins.importer.imports.fogbugz.transformer.UserTransformer;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.atlassian.jira.plugins.importer.imports.importer.ResultSetTransformer;
import com.atlassian.jira.plugins.importer.imports.importer.impl.AbstractDataBean;
import com.atlassian.jira.plugins.importer.web.JdbcConnection;
import com.google.common.collect.Sets;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class FogBugzDataBean extends AbstractDataBean<FogBugzConfigBean> {
	public static final String ISSUE_KEY_REGEX = "(?:bug|case|cases|case_id):? #?([0-9 ,]+)";

	private final JdbcConnection jdbcConnection;
	private final AttachmentTransformerFactory attachmentTransformerFactory;
	private final DateTimeFormatterFactory dateTimeFormatterFactory;

	public FogBugzDataBean(final FogBugzConfigBean configBean, DateTimeFormatterFactory dateTimeFormatterFactory) {
		super(configBean);
		this.dateTimeFormatterFactory = dateTimeFormatterFactory;
		this.jdbcConnection = configBean.getJdbcConnection();
		this.attachmentTransformerFactory = new AttachmentTransformerFactory(jdbcConnection);
	}

	public Set<ExternalUser> getRequiredUsers(Collection<ExternalProject> projects, ImportLogger importLogger) {
		return getAllUsers(importLogger);
	}

	public Set<ExternalUser> getAllUsers(ImportLogger importLogger) {
		return new HashSet<ExternalUser>(jdbcConnection.queryDb(new UserTransformer(configBean, importLogger)));
	}

	public Set<ExternalProject> getAllProjects(ImportLogger log) {
		return Sets.newHashSet(jdbcConnection.queryDb(new ProjectTransformer(configBean)));
	}

	public Collection<ExternalVersion> getVersions(final ExternalProject externalProject, ImportLogger importLogger) {
		final List<ExternalVersion> versions = jdbcConnection
				.queryDb(new FixForVersionTransformer(externalProject, importLogger));
		final List<ExternalVersion> affectsVersions = jdbcConnection
				.queryDb(new AffectsVersionTransformer(externalProject, importLogger));

		// Note that the order here is important, as the fixfor versions will have more info
		versions.addAll(affectsVersions);
		return versions;
	}

	public Collection<ExternalComponent> getComponents(final ExternalProject externalProject, ImportLogger importLogger) {
		return jdbcConnection.queryDb(new ComponentTransformer(configBean, externalProject, importLogger));
	}

	public Iterator<ExternalIssue> getIssuesIterator(final ExternalProject externalProject, ImportLogger importLogger) {
		return jdbcConnection.queryDb(new IssueTransformer(configBean, externalProject,
				new DateTimePickerConverter(dateTimeFormatterFactory), importLogger)).iterator();
	}

	public Collection<ExternalLink> getLinks(ImportLogger log) {
		final List<ExternalLink> links = jdbcConnection.queryDb(new DuplicateLinksTransformer(configBean));
		links.addAll(jdbcConnection.queryDb(new RelatedLinksTransformer(configBean)));
		links.addAll(jdbcConnection.queryDb(new SubcaseLinksTransformer(configBean)));
		return links;
	}

	public long getTotalIssues(final Set<ExternalProject> selectedProjects, ImportLogger log) {
		final List<Long> count = jdbcConnection.queryDb(new ResultSetTransformer<Long>() {
			public String getSqlQuery() {
				return "SELECT count(*) AS issues FROM Bug WHERE ixProject IN ("
						+ SqlUtils.comma(selectedProjects) + ")";
			}

			public Long transform(final ResultSet rs) throws SQLException {
				return Long.valueOf(rs.getLong("issues"));
			}
		});
		return count.iterator().next();
	}

	public String getUnusedUsersGroup() {
		return configBean.getUnusedUsersGroup();
	}

    public void cleanUp() {
		jdbcConnection.closeConnection();
	}

	public Collection<ExternalAttachment> getAttachmentsForIssue(final ExternalIssue externalIssue, ImportLogger log) {
		final String externalIssueOldId = externalIssue.getExternalId();
		final List<ExternalAttachment> listOfExternalAttachments = jdbcConnection
				.queryDb(attachmentTransformerFactory.create(externalIssueOldId, configBean, log));
		return listOfExternalAttachments;
	}

	public String getIssueKeyRegex() {
		return ISSUE_KEY_REGEX;
	}

}
