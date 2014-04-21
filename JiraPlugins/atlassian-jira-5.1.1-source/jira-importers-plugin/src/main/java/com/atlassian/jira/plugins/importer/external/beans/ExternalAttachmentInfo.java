/*
 * Copyright (c) 2012. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.external.beans;

import org.joda.time.DateTime;

import java.net.URI;

public final class ExternalAttachmentInfo {
	public final String author;
	public final DateTime timestamp;
	public final String filename;
	public final URI uri;

	public ExternalAttachmentInfo(String author, DateTime timestamp, String filename, URI uri) {
		this.author = author;
		this.timestamp = timestamp;
		this.filename = filename;
		this.uri = uri;
	}

	public static ExternalAttachmentInfo create(URI uri) {
		return new ExternalAttachmentInfo(null, null, null, uri);
	}

	public static ExternalAttachmentInfo create(String filename, URI uri) {
		return new ExternalAttachmentInfo(null, null, filename, uri);
	}

	public static ExternalAttachmentInfo create(String author, String filename, URI uri) {
		return new ExternalAttachmentInfo(author, null, filename, uri);
	}

	public static ExternalAttachmentInfo create(DateTime dateTime, String author, String filename, URI uri) {
		return new ExternalAttachmentInfo(author, dateTime, filename, uri);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		final ExternalAttachmentInfo that = (ExternalAttachmentInfo) o;

		if (author != null ? !author.equals(that.author) : that.author != null) return false;
		if (filename != null ? !filename.equals(that.filename) : that.filename != null) return false;
		if (timestamp != null ? !timestamp.equals(that.timestamp) : that.timestamp != null) return false;
		if (uri != null ? !uri.equals(that.uri) : that.uri != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = author != null ? author.hashCode() : 0;
		result = 31 * result + (timestamp != null ? timestamp.hashCode() : 0);
		result = 31 * result + (filename != null ? filename.hashCode() : 0);
		result = 31 * result + (uri != null ? uri.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "ExternalAttachmentInfo{" +
				"author='" + author + '\'' +
				", timestamp=" + timestamp +
				", filename='" + filename + '\'' +
				", uri=" + uri +
				'}';
	}

}
