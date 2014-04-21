package com.atlassian.jira.plugins.importer;

import org.apache.commons.io.IOUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class FileCopyUtil {

	public static void copy(InputStream is, File outputFile) throws IOException {
		try {
			final OutputStream os = new BufferedOutputStream(new FileOutputStream(outputFile));
			try {
				IOUtils.copy(is, os);
			} finally {
				IOUtils.closeQuietly(os);
			}
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

};