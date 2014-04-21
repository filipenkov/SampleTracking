/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.mantis;

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
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.atlassian.jira.plugins.importer.imports.importer.ResultSetTransformer;
import com.atlassian.jira.plugins.importer.imports.importer.impl.AbstractDataBean;
import com.atlassian.jira.plugins.importer.imports.mantis.transformer.AttachmentTransformer;
import com.atlassian.jira.plugins.importer.imports.mantis.transformer.ComponentTransformerFactory;
import com.atlassian.jira.plugins.importer.imports.mantis.transformer.IssueTransformerFactory;
import com.atlassian.jira.plugins.importer.imports.mantis.transformer.LinksTransformer;
import com.atlassian.jira.plugins.importer.imports.mantis.transformer.ProjectTransformer;
import com.atlassian.jira.plugins.importer.imports.mantis.transformer.RequiredUserTransformer;
import com.atlassian.jira.plugins.importer.imports.mantis.transformer.UserTransformer;
import com.atlassian.jira.plugins.importer.imports.mantis.transformer.VersionTransformer;
import com.atlassian.jira.plugins.importer.web.JdbcConnection;
import com.atlassian.jira.plugins.importer.web.SiteConfiguration;
import com.google.common.collect.Sets;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class MantisDataBean extends AbstractDataBean<MantisConfigBean> {
	private final JdbcConnection jdbcConnection;
	private final SiteConfiguration mantisUrl;
	private final DateTimeFormatterFactory dateTimeFormatterFactory;
	public static final String ISSUE_KEY_REGEX = "(?:bug)?#([0-9 ,]+)";
	private final ComponentTransformerFactory componentTransformerFactory;
	private final IssueTransformerFactory issueTransformerFactory;

	public MantisDataBean(final JdbcConnection jdbcConnection, final MantisConfigBean configBean,
			final SiteConfiguration urlBean, DateTimeFormatterFactory dateTimeFormatterFactory) {
		super(configBean);
		this.jdbcConnection = jdbcConnection;
		this.mantisUrl = urlBean;
		this.dateTimeFormatterFactory = dateTimeFormatterFactory;
		componentTransformerFactory = new ComponentTransformerFactory(jdbcConnection);
		issueTransformerFactory = new IssueTransformerFactory(jdbcConnection);
	}

	public Set<ExternalUser> getRequiredUsers(Collection<ExternalProject> projects, ImportLogger log) {
		return new HashSet<ExternalUser>(
				jdbcConnection.queryDb(new RequiredUserTransformer(configBean, projects, log)));
	}

	public Set<ExternalUser> getAllUsers(ImportLogger log) {
		return new HashSet<ExternalUser>(jdbcConnection.queryDb(new UserTransformer(configBean, log)));
	}

	public Set<ExternalProject> getAllProjects(ImportLogger log) {
		return Sets.newHashSet(jdbcConnection.queryDb(new ProjectTransformer(configBean, log)));
	}

	public Collection<ExternalVersion> getVersions(final ExternalProject externalProject, ImportLogger importLogger) {
		return jdbcConnection.queryDb(new VersionTransformer(externalProject, importLogger));
	}

	public Collection<ExternalComponent> getComponents(final ExternalProject externalProject, ImportLogger importLogger) {
		return jdbcConnection.queryDb(componentTransformerFactory.create(configBean, externalProject, importLogger));
	}

	public Iterator<ExternalIssue> getIssuesIterator(final ExternalProject externalProject, ImportLogger importLogger) {
		return jdbcConnection.queryDb(issueTransformerFactory.create(mantisUrl.getUrl(), configBean, externalProject,
				new DateTimePickerConverter(dateTimeFormatterFactory), importLogger)).iterator();
	}

	public Collection<ExternalLink> getLinks(ImportLogger log) {
		return jdbcConnection.queryDb(new LinksTransformer(configBean));
	}

	public long getTotalIssues(final Set<ExternalProject> selectedProjects, ImportLogger log) {
		final List<Long> count = jdbcConnection.queryDb(new ResultSetTransformer<Long>() {
			public String getSqlQuery() {
				return "SELECT count(*) AS issues FROM mantis_bug_table WHERE project_id IN ("
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
		return jdbcConnection.queryDb(
				new AttachmentTransformer(externalIssueOldId, configBean, mantisUrl, log));
	}

	public String getIssueKeyRegex() {
		return ISSUE_KEY_REGEX;
	}

	@Override
	public String getExternalSystemUrl() {
		return mantisUrl.getUrl();
	}
}
