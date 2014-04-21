/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.external.beans;

import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Date;

public class ExternalAttachment {

	private final String fileName;
	private final DateTime attachedDate;
	private final File attachedFile;

	private String attacher;
	private String id;
    private String description;

	public ExternalAttachment(String fileName, @Nullable File attachedFile, Date attachedDate) {
		this(fileName, attachedFile, new DateTime(attachedDate.getTime()));
	}

	public ExternalAttachment(String fileName, @Nullable File attachedFile, DateTime attachedDate) {
		this.fileName = fileName;
		this.attachedFile = attachedFile;
		this.attachedDate = attachedDate;
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public String getFileName() {
		return fileName;
	}

	public DateTime getAttachedDate() {
		return attachedDate;
	}

	public String getAttacher() {
		return attacher;
	}

	public void setAttacher(String attacher) {
		this.attacher = attacher;
	}

	public File getAttachedFile() {
		return attachedFile;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
