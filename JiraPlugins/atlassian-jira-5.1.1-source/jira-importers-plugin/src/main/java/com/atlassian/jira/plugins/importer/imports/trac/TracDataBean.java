/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.trac;

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
import com.atlassian.jira.plugins.importer.imports.trac.transformer.ActiveWatchersTransformer;
import com.atlassian.jira.plugins.importer.imports.trac.transformer.AttachmentTransformer;
import com.atlassian.jira.plugins.importer.imports.trac.transformer.ComponentTransformer;
import com.atlassian.jira.plugins.importer.imports.trac.transformer.IssueTransformer;
import com.atlassian.jira.plugins.importer.imports.trac.transformer.ProjectTransformer;
import com.atlassian.jira.plugins.importer.imports.trac.transformer.RequiredUserTransformer;
import com.atlassian.jira.plugins.importer.imports.trac.transformer.VersionTransformer;
import com.atlassian.jira.plugins.importer.web.JdbcConnection;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class TracDataBean extends AbstractDataBean<TracConfigBean> {
	private final JdbcConnection jdbcConnection;
	public static final String ISSUE_KEY_REGEX = "(?:ticket:|#)([0-9]+)";
	private final TracWikiConverter wikiConverter;

	public TracDataBean(final TracConfigBean configBean, final TracWikiConverter wikiConverter) {
		super(configBean);
		this.wikiConverter = wikiConverter;
		this.jdbcConnection = configBean.getJdbcConnection();
	}

	public Set<ExternalUser> getRequiredUsers(Collection<ExternalProject> projects, ImportLogger importLogger) {
		return getAllUsers(importLogger);
	}

	public Set<ExternalUser> getAllUsers(ImportLogger log) {
		Set<ExternalUser> users = Sets
				.newHashSet(jdbcConnection.queryDb(new RequiredUserTransformer(configBean, log)));
		users.addAll(jdbcConnection.queryDbAppendCollection(new ActiveWatchersTransformer(configBean, log)));
		return users;
	}

	public Set<ExternalProject> getAllProjects(final ImportLogger log) {
		return Sets.newHashSet(Collections2.transform(configBean.getExternalProjectNames(), new ProjectTransformer(configBean)));
	}

	public Collection<ExternalVersion> getVersions(final ExternalProject externalProject, ImportLogger importLogger) {
		return jdbcConnection.queryDb(new VersionTransformer(importLogger));
	}

	public Collection<ExternalComponent> getComponents(final ExternalProject externalProject, ImportLogger importLogger) {
		return jdbcConnection.queryDb(new ComponentTransformer(configBean, importLogger));
	}

	public Iterator<ExternalIssue> getIssuesIterator(final ExternalProject externalProject, ImportLogger importLogger) {
		return jdbcConnection.queryDb(new IssueTransformer(configBean, importLogger, wikiConverter)).iterator();
	}

	public Collection<ExternalLink> getLinks(ImportLogger log) {
		return Lists.newArrayList();
	}

	public long getTotalIssues(Set<ExternalProject> selectedProjects, ImportLogger log) {
		final List<Long> count = jdbcConnection.queryDb(new ResultSetTransformer<Long>() {
			public String getSqlQuery() {
				return "SELECT count(*) AS issues FROM ticket";
			}

			public Long transform(final ResultSet rs) throws SQLException {
				return rs.getLong("issues");
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
		return jdbcConnection.queryDb(new AttachmentTransformer(configBean, externalIssueOldId, log));
	}

	public String getIssueKeyRegex() {
		return ISSUE_KEY_REGEX;
	}

}
