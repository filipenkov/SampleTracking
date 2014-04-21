/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.imports.bugzilla.transformer;

import com.atlassian.jira.plugins.importer.external.beans.ExternalAttachment;
import com.atlassian.jira.plugins.importer.imports.bugzilla.BugzillaConfigBean;
import com.atlassian.jira.plugins.importer.imports.importer.impl.ConsoleImportLogger;
import com.atlassian.jira.plugins.importer.web.SiteConfiguration;
import org.junit.Test;
import org.ofbiz.core.util.UtilDateTime;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestAttachmentTransformerBigFiles {

	@Test
	public void testCreateAttachmentsDownloadsThem() throws Exception {
		final boolean downloadCalled[] = {false};
		final String bug_id = "1";
		final String attach_id = "1123";
		final String fileName = "test.file.txt";
		final Timestamp ts = UtilDateTime.nowTimestamp();
		BugzillaConfigBean configBean = mock(BugzillaConfigBean.class);
		when(configBean.getUsernameForLoginName("me@localhost")).thenReturn("pniewiadomski");

		ResultSet mockedResultSet = mock(ResultSet.class);

		when(mockedResultSet.next()).thenReturn(true).thenReturn(false);
		when(mockedResultSet.getString("filename")).thenReturn(fileName).thenReturn(fileName);
		when(mockedResultSet.getString("submitter")).thenReturn("me@localhost");
		when(mockedResultSet.getTimestamp("creation_ts")).thenReturn(ts);
		when(mockedResultSet.getBinaryStream("thedata")).thenReturn(new ByteArrayInputStream(new byte[0]));
		// 0 size data means a big attachment (stored in the filesystem)
		when(mockedResultSet.getString("attach_id")).thenReturn(attach_id);

		AttachmentTransformerForAttachDataAndBigFiles transformer = new AttachmentTransformerForAttachDataAndBigFiles(
				bug_id, configBean, new SiteConfiguration("http://test"), ConsoleImportLogger.INSTANCE) {
            @Override
            protected File getTempDir() {
                return new File(System.getProperty("java.io.tmpdir"));
            }

            @Override
			protected File downloadAttachment(String attachId)
					throws SQLException {
				downloadCalled[0] = true;
				assertEquals(attach_id, attachId);
				return null;
			}
		};

		ExternalAttachment at = transformer.transform(mockedResultSet);
		assertNotNull(at);

		assertTrue(downloadCalled[0]);
	}

}
