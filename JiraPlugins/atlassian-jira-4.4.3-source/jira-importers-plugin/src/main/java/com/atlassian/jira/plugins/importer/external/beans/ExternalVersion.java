/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.external.beans;

import com.atlassian.jira.issue.IssueFieldConstants;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.util.Date;

public class ExternalVersion implements NamedExternalObject, Comparable {

	public static final String AFFECTED_VERSION_PREFIX = IssueFieldConstants.AFFECTED_VERSIONS;
	public static final String FIXED_VERSION_PREFIX = IssueFieldConstants.FIX_FOR_VERSIONS;

	private String id;
	private String name;
	private boolean released;
	private boolean archived;
	private DateTime releaseDate;
	private String description;

	public ExternalVersion() {
	}

	public ExternalVersion(String name) {
		this.name = name;
	}


	@Nullable
	public String getDescription() {
		return description;
	}

	public void setDescription(@Nullable String description) {
		this.description = description;
	}

	@Nullable
	public String getId() {
		return id;
	}

	public void setId(@Nullable String id) {
		this.id = id;
	}

	@Nullable
	public String getName() {
		return name;
	}

	public void setName(@Nullable String name) {
		this.name = name;
	}

	public boolean isReleased() {
		return released;
	}

	public void setReleased(boolean released) {
		this.released = released;
	}

	public boolean isArchived() {
		return archived;
	}

	public void setArchived(boolean archived) {
		this.archived = archived;
	}

	@Nullable
	public DateTime getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(@Nullable Date releaseDate) {
		this.releaseDate = releaseDate != null ? new DateTime(releaseDate) : null;
	}

	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	public boolean equals(Object o) {
		return EqualsBuilder.reflectionEquals(this, o);
	}

	public int compareTo(Object o) {
		com.atlassian.jira.plugins.importer.external.beans.ExternalVersion rhs = (com.atlassian.jira.plugins.importer.external.beans.ExternalVersion) o;
		return new CompareToBuilder()
				.append(this.getName(), rhs.getName())
				.toComparison();
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
