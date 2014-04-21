/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.bugzilla;

import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.issue.customfields.converters.DateTimePickerConverter;
import com.atlassian.jira.plugins.importer.SqlUtils;
import com.atlassian.jira.plugins.importer.external.beans.*;
import com.atlassian.jira.plugins.importer.imports.bugzilla.transformer.*;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.atlassian.jira.plugins.importer.imports.importer.ResultSetTransformer;
import com.atlassian.jira.plugins.importer.imports.importer.impl.AbstractDataBean;
import com.atlassian.jira.plugins.importer.web.JdbcConnection;
import com.atlassian.jira.plugins.importer.web.SiteConfiguration;
import com.google.common.collect.Sets;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class BugzillaDataBean extends AbstractDataBean<BugzillaConfigBean> {
	private final JdbcConnection jdbcConnection;
	private final SiteConfiguration bugzillaUrl;
	private final DateTimeFormatterFactory dateTimeFormatterFactory;
	private final AttachmentTransformerFactory attachmentTransformerFactory;
	public static final String ISSUE_KEY_REGEX = "(?:bug|case|cases|case_id):? #?([0-9 ,]+)";

	public BugzillaDataBean(final JdbcConnection jdbcConnection, final BugzillaConfigBean configBean,
			final SiteConfiguration urlBean, final DateTimeFormatterFactory dateTimeFormatterFactory) {
		super(configBean);
		this.jdbcConnection = jdbcConnection;
		this.bugzillaUrl = urlBean;
		this.dateTimeFormatterFactory = dateTimeFormatterFactory;
		attachmentTransformerFactory = new AttachmentTransformerFactory(jdbcConnection);
	}

	public Set<ExternalUser> getRequiredUsers(Collection<ExternalProject> projects, ImportLogger importLogger) {
		return new HashSet<ExternalUser>(jdbcConnection.queryDb(RequiredUserTransformer.create(jdbcConnection, configBean, projects, importLogger)));
	}

	public Set<ExternalUser> getAllUsers(ImportLogger log) {
		return new HashSet<ExternalUser>(jdbcConnection.queryDb(new UserTransformer(configBean, log)));
	}

	public Set<ExternalProject> getAllProjects(ImportLogger log) {
		return Sets.newHashSet(jdbcConnection.queryDb(new ProjectTransformer(configBean)));
	}

	public Collection<ExternalVersion> getVersions(final ExternalProject externalProject, ImportLogger importLogger) {
		final List<ExternalVersion> versions = jdbcConnection.queryDb(new VersionTransformer(externalProject, importLogger));
		final List<ExternalVersion> affectsVersions = jdbcConnection.queryDb(new MilestoneTransformer(externalProject, importLogger));

		// Note that the order here is important, as the fixfor versions will have more info
		versions.addAll(affectsVersions);
		return versions;
	}

	public Collection<ExternalComponent> getComponents(final ExternalProject externalProject, ImportLogger importLogger) {
		return jdbcConnection.queryDb(new ComponentTransformer(configBean, externalProject, importLogger));
	}

	public Iterator<ExternalIssue> getIssuesIterator(final ExternalProject externalProject, ImportLogger importLogger) {
		return jdbcConnection.queryDb(IssueTransformer.create(jdbcConnection, bugzillaUrl.getUrl(), configBean, externalProject,
				new DateTimePickerConverter(dateTimeFormatterFactory), importLogger)).iterator();
	}

	public Collection<ExternalLink> getLinks(ImportLogger log) {
		final List<ExternalLink> duplicates = jdbcConnection.queryDb(new DuplicateLinksTransformer(configBean));
		final List<ExternalLink> related = jdbcConnection.queryDb(new RelatedLinksTransformer(configBean));

		// Note that the order here is important, as the fixfor versions will have more info
		duplicates.addAll(related);
		return duplicates;
	}

	public long getTotalIssues(final Set<ExternalProject> selectedProjects, ImportLogger log) {
		final List<Long> count = jdbcConnection.queryDb(new ResultSetTransformer<Long>() {
			public String getSqlQuery() {
				return "SELECT count(*) AS issues FROM bugs WHERE product_id IN ("
						+ SqlUtils.comma(selectedProjects) + ")";
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
		return jdbcConnection.queryDb(
				attachmentTransformerFactory.create(configBean, externalIssueOldId, bugzillaUrl, log));
	}

	public String getIssueKeyRegex() {
		return ISSUE_KEY_REGEX;
	}

	@Override
	public String getExternalSystemUrl() {
		return bugzillaUrl.getUrl();
	}
}
