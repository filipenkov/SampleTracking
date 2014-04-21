/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.fogbugz.transformer;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.plugins.importer.external.beans.ExternalAttachment;
import com.atlassian.jira.plugins.importer.imports.AbstractAttachmentTransformer;
import com.atlassian.jira.plugins.importer.imports.fogbugz.FogBugzConfigBean;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AttachmentTransformerVer7OrOlder extends AbstractAttachmentTransformer {
	private final FogBugzConfigBean configBean;
	protected final String ixBug;

	public AttachmentTransformerVer7OrOlder(final String ixBug, final FogBugzConfigBean configBean) {
		this.ixBug = ixBug;
		this.configBean = configBean;
	}

	public String getSqlQuery() {
		return "SELECT a.sFileName, p.sFullName, b.dt, a.sData FROM BugEvent b, Attachment a, Person p"
				+ " WHERE b.ixPerson = p.ixPerson" + "  AND b.ixBugEvent = a.ixBugEvent AND b.ixBug = "
				+ ixBug + " ORDER BY b.dt ";
	}

	public ExternalAttachment transform(final ResultSet rs) throws SQLException {
		ExternalAttachment externalAttachment = null;

		final String fileName = rs.getString("sFileName");
		final String attacher = configBean.getUsernameForFullName(rs.getString("sFullName"));

		if (StringUtils.isNotBlank(fileName)) {
			try {
				// Deal with the file
				final InputStream binaryStream = rs.getBinaryStream("sData");
                try {
                    final File file = getTempFile();
                    //does destinations directory exist ?

                    final OutputStream os = new FileOutputStream(file);
                    try {
                        IOUtils.copy(binaryStream, os);
                    } finally {
                        IOUtils.closeQuietly(os);
                    }

					externalAttachment = new ExternalAttachment(fileName, file, rs.getTimestamp("dt"));
					externalAttachment.setAttacher(attacher);
                } finally {
				    IOUtils.closeQuietly(binaryStream);
                }

			} catch (final IOException e) {
				throw new DataAccessException("Exception occurred dealing with attachment.", e);
			}
		}
		return externalAttachment;
	}
}