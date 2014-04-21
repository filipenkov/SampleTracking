/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.bugzilla.transformer;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.plugins.importer.imports.bugzilla.BugzillaClient;
import com.atlassian.jira.plugins.importer.imports.bugzilla.BugzillaConfigBean;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.atlassian.jira.plugins.importer.web.SiteConfiguration;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;


public class AttachmentTransformerForAttachDataAndBigFiles extends AttachmentTransformerForDataInAttachments {
	private final BugzillaClient bugzillaClient;

	public AttachmentTransformerForAttachDataAndBigFiles(String ixBug, BugzillaConfigBean configBean,
			final SiteConfiguration bugzillaUrl, ImportLogger log) {
		super(ixBug, configBean, log);
		this.bugzillaClient = new BugzillaClient(bugzillaUrl);
	}

	@Override
	public String getSqlQuery() {
		return "SELECT a.attach_id, a.filename, a.creation_ts, a.description, ad.thedata, "
				+ "p.login_name AS submitter FROM attachments AS a, profiles AS p, attach_data AS ad WHERE p.userid = a.submitter_id"
				+ " AND ad.id = a.attach_id"
				+ " AND bug_id = " + ixBug + " ORDER BY attach_id ASC";
	}

	@Override

	@edu.umd.cs.findbugs.annotations.SuppressWarnings(value="RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
	protected File copyAttachment(ResultSet rs) throws SQLException {
		File file = super.copyAttachment(rs);
		if (file.length() == 0) {
			file.delete();
			file = downloadAttachment(rs.getString("attach_id"));
		}
		return file;
	}

	protected File downloadAttachment(String attachId) throws SQLException {
		try {
			if (bugzillaClient.getUrlBean().isUseCredentials()
					&& !bugzillaClient.isAuthenticated()) {
				bugzillaClient.login();
			}
			return bugzillaClient.getAttachment(ixBug, attachId);
		} catch (IOException e) {
			throw new DataAccessException("Exception occurred dealing with attachment.", e);
		}
	}
}