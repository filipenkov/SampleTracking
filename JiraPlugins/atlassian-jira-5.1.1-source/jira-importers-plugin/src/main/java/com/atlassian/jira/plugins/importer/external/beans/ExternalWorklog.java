/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.external.beans;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.joda.time.DateTime;
import org.joda.time.Period;

import javax.annotation.Nullable;
import java.util.Date;

/**
 * Used to represent a worklog when importing data.
 *
 * @since v3.13
 */
public class ExternalWorklog {
	private String author;
	private String comment;
	private DateTime startDate;
	private Long timeSpent;


	public ExternalWorklog() {
	}

	public ExternalWorklog(String author, String comment, DateTime startDate, Long timeSpent) {
		this.author = author;
		this.comment = comment;
		this.startDate = startDate;
		this.timeSpent = timeSpent;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(final String author) {
		this.author = author;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(final String comment) {
		this.comment = comment;
	}

    @Nullable
	public DateTime getStartDate() {
		return startDate;
	}

    @JsonProperty
    public void setStartDate(@Nullable final DateTime startDate) {
        this.startDate = startDate;
    }

    @JsonIgnore
	public void setStartDate(@Nullable final Date startDate) {
		this.startDate = newDateNullSafe(startDate);
	}

	public Long getTimeSpent() {
		return timeSpent;
	}

    @JsonProperty
    public void setTimeSpent(Period period) {
        this.timeSpent = Long.valueOf(period.toStandardSeconds().getSeconds());
    }

    @JsonIgnore
	public void setTimeSpent(final Long timeSpent) {
		this.timeSpent = timeSpent;
	}

	private DateTime newDateNullSafe(final Date d) {
		return d == null ? null : new DateTime(d.getTime());
	}

}
