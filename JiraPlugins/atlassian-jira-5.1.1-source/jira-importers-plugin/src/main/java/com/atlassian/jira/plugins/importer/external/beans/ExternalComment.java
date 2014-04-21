/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.external.beans;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.util.Date;

public class ExternalComment {
	private String body;
	private String author;
	private DateTime created;

    @JsonCreator
	public ExternalComment(String body) {
		this(body, null, (DateTime) null);
	}

    @JsonIgnore
	public ExternalComment(String body, @Nullable String author, @Nullable Date created) {
		this(body, author, created != null ? new DateTime(created.getTime()) : null);
	}

    @JsonCreator
	public ExternalComment(@JsonProperty("body") String body, @JsonProperty("author") @Nullable String author, @JsonProperty("created") @Nullable DateTime created) {
		this.body = body;
		this.author = author;
		this.created = created;
	}

	public String getBody() {
		return body;
	}

	@Nullable
	public String getAuthor() {
		return author;
	}

	@Nullable
	public DateTime getCreated() {
		return created;
	}

	public String toString() {
		return new ToStringBuilder(this)
				.append(StringUtils.abbreviate(StringUtils.replaceChars(getBody(), "\r\n", ""), 50))
				.toString();
	}
}

