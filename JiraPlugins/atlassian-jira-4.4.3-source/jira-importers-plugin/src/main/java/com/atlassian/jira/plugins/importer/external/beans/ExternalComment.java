/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.external.beans;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.util.Date;

public class ExternalComment {
	private String body;
	private String username;
	private DateTime created;

	public ExternalComment(String body) {
		this(body, null, (DateTime) null);
	}

	public ExternalComment(String body, @Nullable String author, @Nullable Date created) {
		this(body, author, created != null ? new DateTime(created.getTime()) : null);
	}

	public ExternalComment(String body, @Nullable String author, @Nullable DateTime created) {
		this.body = body;
		this.username = author;
		this.created = created;
	}

	public String getBody() {
		return body;
	}

	@Nullable
	public String getUsername() {
		return username;
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

