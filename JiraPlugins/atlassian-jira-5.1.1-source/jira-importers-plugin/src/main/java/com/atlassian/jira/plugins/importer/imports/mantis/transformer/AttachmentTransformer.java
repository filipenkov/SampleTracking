/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.mantis.transformer;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.plugins.importer.FileCopyUtil;
import com.atlassian.jira.plugins.importer.external.beans.ExternalAttachment;
import com.atlassian.jira.plugins.importer.imports.AbstractAttachmentTransformer;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.atlassian.jira.plugins.importer.imports.mantis.MantisClient;
import com.atlassian.jira.plugins.importer.imports.mantis.MantisConfigBean;
import com.atlassian.jira.plugins.importer.web.SiteConfiguration;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AttachmentTransformer extends AbstractAttachmentTransformer {
    private final ImportLogger log;
	protected final String ixBug;

	private MantisClient mantisClient;
    private MantisConfigBean.TimestampHelper timestampHelper;

    public AttachmentTransformer(final String ixBug, SiteConfiguration mantisUrl, ImportLogger log) {
		this.ixBug = ixBug;
        this.timestampHelper = new MantisConfigBean.TimestampHelper();
        this.log = log;
		this.mantisClient = new MantisClient(mantisUrl);
	}

	public String getSqlQuery() {
		return "SELECT id,filename,file_type,content,date_added,description FROM mantis_bug_file_table WHERE bug_id = "
				+ ixBug + " ORDER BY date_added ASC";
	}

	public ExternalAttachment transform(final ResultSet rs) throws SQLException {
		ExternalAttachment externalAttachment = null;

		final String fileName = rs.getString("filename");
		
		if (StringUtils.isNotBlank(fileName)) {

			try {
				File file = copyAttachment(rs);

				externalAttachment = new ExternalAttachment(fileName, file, timestampHelper.getTimestamp(rs, "date_added"));
				externalAttachment.setDescription(rs.getString("description"));
			} catch (DataAccessException e) {
				log.fail(e, "Can't get attachment details");
				return null;
			}
		}
		return externalAttachment;
	}

	protected File copyAttachment(ResultSet rs) throws SQLException {
		try {
			// Deal with the file
			final Blob content = rs.getBlob("content");
			if (content.length() != 0) {
				final File file = getTempFile();
				//does destinations directory exist ?

				final InputStream contentStream = content.getBinaryStream();

				FileCopyUtil.copy(contentStream, file);

				return file;
			} else {
				return downloadAttachment(rs.getString("id"));
			}
		} catch (final IOException e) {
			throw new DataAccessException("Exception occurred dealing with attachment.", e);
		}
	}

	protected File downloadAttachment(String attachId) throws SQLException {
		try {
			if (mantisClient.getUrlBean().isUseCredentials()
					&& !mantisClient.isAuthenticated()) {
				mantisClient.login();
			}
			return mantisClient.getAttachment(ixBug, attachId);
		} catch (IOException e) {
			throw new DataAccessException("Exception occurred dealing with attachment.", e);
		}
	}

}
