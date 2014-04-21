/*
 * Copyright (c) 2012. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.imports.csv.mappers;

import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.plugins.importer.external.beans.ExternalAttachmentInfo;
import com.atlassian.jira.plugins.importer.imports.csv.CsvDateParser;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;

public class ExternalAttachmentInfoMapper implements ExternalMapper<ExternalAttachmentInfo> {

	private  final CsvDateParser csvDateParser;

	public ExternalAttachmentInfoMapper(CsvDateParser csvDateParser) {
		this.csvDateParser = csvDateParser;
	}

	@Override
	public Iterable<ExternalAttachmentInfo> buildFromMultiMap(Multimap<String, String> bean, ImportLogger log) {
		final Collection<String> attachmentStr = bean.get(IssueFieldConstants.ATTACHMENT);
		return Iterables.transform(attachmentStr, new Function<String, ExternalAttachmentInfo>() {
			@Override
			public ExternalAttachmentInfo apply(String input) {
				try {
					return parse(input);
				} catch (ParseException e) {
					throw new RuntimeException(e);
				}
			}
		});
	}

	// http://bla/file
	// https://bla/file
	// file:relative/to/home/import/attachments
	// filename;<url>
	// author:
	// [[[timestamp;]author;]filename;]http(s)://url
	public ExternalAttachmentInfo parse(String token) throws ParseException {
		if (token.startsWith("http://") || token.startsWith("https://") || token.startsWith("file:")) {
			try {
				return ExternalAttachmentInfo.create(new URI(token));
			} catch (URISyntaxException e) {
				throw new ParseException(e.getMessage(), 0);
			}
		}

		final int i = Collections.max(ImmutableList.of(token.lastIndexOf(";http://"), token.lastIndexOf(";https://"), token .lastIndexOf(";file:")));
		if (i == -1) {
			throw new ParseException(String.format("No protocols found in token '%s'. Supported protocols are: https, http and file.", token), 0);
		}
		final URI uri = toUri(token.substring(i + 1));
		// narrow remaining stuff
		token = token.substring(0, i);

		final String[] tokens = StringUtils.splitPreserveAllTokens(token, ";");
		if (tokens.length > 3) {
			throw new ParseException("to many tokens", 0);
		}
		final String filename = StringUtils.trimToNull(tokens[tokens.length - 1]);

		final String author = tokens.length >= 2 ? StringUtils.trimToNull(tokens[tokens.length - 2]) : null;
		final String timestampStr = tokens.length >= 3 ? tokens[tokens.length - 3] : null;
		final DateTime timestamp = timestampStr != null ? new DateTime(csvDateParser.parseDate(timestampStr)) : null;
		return new ExternalAttachmentInfo(author, timestamp, filename, uri);
	}

	private URI toUri(String uri) {
		try {
			return new URI(uri);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

}
