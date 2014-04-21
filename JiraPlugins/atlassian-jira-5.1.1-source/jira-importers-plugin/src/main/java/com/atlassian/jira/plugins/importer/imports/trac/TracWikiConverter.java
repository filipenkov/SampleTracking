/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.imports.trac;

import com.atlassian.jira.plugins.importer.FileCopyUtil;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.atlassian.util.concurrent.LazyReference;
import com.atlassian.uwc.ui.UWCUserSettings;
import com.atlassian.uwc.ui.listeners.FeedbackHandler;
import com.atlassian.uwc.util.PropertyFileManager;
import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class TracWikiConverter {

	private LazyReference<List<String>> converterStrings = new LazyReference<List<String>>() {
		@Override
		protected List<String> create() throws Exception {
			File config = File.createTempFile("uwc-", ".properties");
			try {
				FileCopyUtil.copy(getClass().getResourceAsStream("/converter.trac.properties"), config);

				final TreeMap<String, String> converters = PropertyFileManager.loadPropertiesFile(config.toString());

				if (converters == null)
					throw new IllegalArgumentException(); //unlikely, as the error handling above should be sufficient

				final List<String> result = Lists.newArrayListWithCapacity(converters.keySet().size());
				for (Map.Entry<String, String> converter : converters.entrySet()) {
					result.add(converter.getKey() + "=" + converter.getValue());
				}
				return result;
			} finally {
				FileUtils.deleteQuietly(config);
			}
		}
	};

	public String convert(String wiki, ImportLogger log) {
		try {
			final File inputDir = createTempDirectory();
			try {
				final File outputDir = createTempDirectory();
				try {
					UWCUserSettings settings = new UWCUserSettings();
					settings.setWikitype("trac");

					ConverterEngine engine = new ConverterEngine();
					engine.getState(settings);

					File tempComment = File.createTempFile("comment-", ".txt", inputDir);
					FileUtils.writeStringToFile(tempComment, wiki);

					//get the pages
					engine.convert(outputDir, Lists.<File>newArrayList(tempComment), converterStrings.get(), settings);

					if (engine.getConverterFeedback() != FeedbackHandler.Feedback.OK) {
						log.fail(null, "Failed to convert Wiki markup: %s", engine.getErrors().getAllErrorMessages());
						return wiki;
					} else {
						return FileUtils.readFileToString(new File(outputDir, tempComment.getName()));
					}
				} finally {
					FileUtils.deleteDirectory(outputDir);
				}
			} finally {
				FileUtils.deleteDirectory(inputDir);
			}
		} catch (Exception e) {
			log.fail(e, "Failed to convert Wiki markup");
			return wiki;
		}
	}

	public static File createTempDirectory() throws IOException {
		final File temp;

		temp = File.createTempFile("temp", Long.toString(System.nanoTime()));

		if (!(temp.delete())) {
			throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
		}

		if (!(temp.mkdir())) {
			throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
		}

		return (temp);
	}
}
