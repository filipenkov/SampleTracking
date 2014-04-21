/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.trac.transformer;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.plugins.importer.FileCopyUtil;
import com.atlassian.jira.plugins.importer.external.beans.ExternalAttachment;
import com.atlassian.jira.plugins.importer.imports.AbstractAttachmentTransformer;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.atlassian.jira.plugins.importer.imports.trac.TracConfigBean;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.BitSet;

public class AttachmentTransformer extends AbstractAttachmentTransformer {
    private final TracConfigBean configBean;
    private final ImportLogger log;
	protected final String ixBug;

	protected static final BitSet mark = new BitSet(256);
    // Static initializer for mark
    static {
        mark.set('!');
        mark.set('~');
        mark.set('*');
        mark.set('\'');
        mark.set('(');
        mark.set(')');
		mark.set('%');
		mark.set('+');
		mark.set(',');
		mark.set('$');
    }

	protected static final BitSet allowed = new BitSet(256);
	static {
		allowed.or(URI.allowed_within_path);
		allowed.andNot(mark);
	}

	public AttachmentTransformer(TracConfigBean configBean, final String ixBug, ImportLogger log) {
		this.ixBug = ixBug;
        this.configBean = configBean;
        this.log = log;
	}

	public String getSqlQuery() {
		return "SELECT id,filename,time,description,author FROM attachment WHERE id='" + ixBug + "' AND type='ticket' ORDER BY time ASC";
	}

	public ExternalAttachment transform(final ResultSet rs) throws SQLException {
		ExternalAttachment externalAttachment = null;

		final String fileName = rs.getString("filename");
		
		if (StringUtils.isNotBlank(fileName)) {
			try {
				File file = copyAttachment(rs);

				externalAttachment = new ExternalAttachment(fileName, file,
						TracConfigBean.getTimestamp(rs, "time"));
				externalAttachment.setAttacher(configBean.getUsernameForEmail(rs.getString("author")));
				externalAttachment.setDescription(rs.getString("description"));
			} catch (DataAccessException e) {
				log.fail(e, "Failed to get attachment");
				return null;
			}
		}
		return externalAttachment;
	}

	protected File copyAttachment(ResultSet rs) throws SQLException {
		try {
			ZipFile zip = new ZipFile(configBean.getEnvironmentZip());
			try {
				// Deal with the file
				final String filename = String.format("attachments/ticket/%d/%s", rs.getInt("id"),
						encodeFilename(rs.getString("filename")));
				final ZipArchiveEntry zipEntry = zip.getEntry(filename);
				if (zipEntry != null) {
					final File file = getTempFile();

					final InputStream contentStream = zip.getInputStream(zipEntry);

					FileCopyUtil.copy(contentStream, file);

					return file;
				} else {
					throw new DataAccessException("Attachment doesn't exist: " + filename);
				}
			} finally {
				zip.close();
			}
		} catch (final IOException e) {
			throw new DataAccessException("Exception occurred dealing with attachment.", e);
		}
	}

	protected String encodeFilename(String filename) throws URIException {
		return URIUtil.encode(filename, allowed, "UTF-8");
	}
}
