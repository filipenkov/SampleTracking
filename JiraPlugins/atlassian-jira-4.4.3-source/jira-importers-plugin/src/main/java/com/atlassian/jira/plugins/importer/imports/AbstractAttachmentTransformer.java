/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.imports;

import com.atlassian.jira.plugins.importer.external.beans.ExternalAttachment;
import com.atlassian.jira.plugins.importer.imports.importer.ResultSetTransformer;
import com.atlassian.jira.util.AttachmentUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public abstract class AbstractAttachmentTransformer implements ResultSetTransformer<ExternalAttachment> {

    protected File getTempDir() {
        return AttachmentUtils.getTemporaryAttachmentDirectory();
    }

    protected File getTempFile() throws IOException {
        final File file = File.createTempFile("attachmentTransformer-", ".tmp", getTempDir());

		if (file.getParentFile() != null) {
			FileUtils.forceMkdir(file.getParentFile());
        }

        return file;
    }

	@edu.umd.cs.findbugs.annotations.SuppressWarnings(value="RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
	protected void deleteTempFile(final File file) {
		final File parentDir = file.getParentFile();
		file.delete();
		if ((parentDir.listFiles() == null) || (parentDir.listFiles().length == 0)) {
			parentDir.delete();
		}
	}

}
