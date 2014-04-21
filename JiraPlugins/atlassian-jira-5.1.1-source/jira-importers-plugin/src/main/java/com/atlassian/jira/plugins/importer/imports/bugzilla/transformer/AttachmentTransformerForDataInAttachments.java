/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.bugzilla.transformer;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.plugins.importer.FileCopyUtil;
import com.atlassian.jira.plugins.importer.external.beans.ExternalAttachment;
import com.atlassian.jira.plugins.importer.imports.AbstractAttachmentTransformer;
import com.atlassian.jira.plugins.importer.imports.bugzilla.BugzillaConfigBean;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AttachmentTransformerForDataInAttachments extends AbstractAttachmentTransformer {
	protected final BugzillaConfigBean configBean;
	protected final ImportLogger log;
	protected final String ixBug;

	public AttachmentTransformerForDataInAttachments(final String ixBug, final BugzillaConfigBean configBean,
			ImportLogger log) {
		this.ixBug = ixBug;
		this.configBean = configBean;
		this.log = log;
	}

	public String getSqlQuery() {
		return "SELECT a.filename, a.creation_ts, a.thedata, a.description, "
				+ "p.login_name AS submitter FROM attachments AS a, profiles AS p WHERE p.userid = a.submitter_id AND bug_id = "
				+ ixBug + " ORDER BY attach_id ASC";
	}

	public ExternalAttachment transform(final ResultSet rs) throws SQLException {
		ExternalAttachment externalAttachment = null;

		final String fileName = rs.getString("filename");
		final String attacher = configBean.getUsernameForLoginName(rs.getString("submitter"));

		if (StringUtils.isNotBlank(fileName)) {
			File file = copyAttachment(rs);

			externalAttachment = new ExternalAttachment(fileName, file, rs.getTimestamp("creation_ts"));
			externalAttachment.setAttacher(attacher);
			externalAttachment.setDescription(rs.getString("description"));
		}
		return externalAttachment;
	}

	protected File copyAttachment(ResultSet rs) throws SQLException {
		try {
			// Deal with the file
			final InputStream binaryStream = rs.getBinaryStream("thedata");
			final File file = getTempFile();

			FileCopyUtil.copy(binaryStream, file);

			return file;
		} catch (final IOException e) {
			throw new DataAccessException("Exception occurred dealing with attachment.", e);
		}
	}

}
