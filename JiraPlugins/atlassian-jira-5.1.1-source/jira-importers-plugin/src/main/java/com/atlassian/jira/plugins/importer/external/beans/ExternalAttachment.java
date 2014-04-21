/*
 * Copyright (C) 2002-2012 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.external.beans;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Date;

public class ExternalAttachment {

	private final String name;
	private final DateTime created;

	private File attachment;
    private String attacher;
    private String description;

    @JsonIgnore
	public ExternalAttachment(String name, @Nullable File attachment, Date created) {
		this(name, attachment, new DateTime(created.getTime()));
	}

    @JsonCreator
	public ExternalAttachment(@JsonProperty("name") String name, @JsonProperty("attachment") @Nullable File attachment, @JsonProperty("created") DateTime created) {
		this.name = name;
		this.attachment = attachment;
		this.created = created;
	}

	public String getName() {
		return name;
	}

	public DateTime getCreated() {
		return created;
	}

	public String getAttacher() {
		return attacher;
	}

	public void setAttacher(String attacher) {
		this.attacher = attacher;
	}

    public void setAttachment(File attachment) {
        this.attachment = attachment;
    }

	public File getAttachment() {
		return attachment;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("name", name)
				.append("created", created)
				.append("attacher", attacher)
				.append("file", attachment)
				.toString();
	}
}
