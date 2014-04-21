/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.external.beans;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.annotation.Nullable;

public class ExternalComponent implements NamedExternalObject {

	private String id;
	private String name;
	private String lead;
	private String description;

    @JsonCreator
    public ExternalComponent(String name) {
		this.name = name;
	}

    @JsonCreator
    public ExternalComponent(@JsonProperty("name") String name, @JsonProperty("id") @Nullable String id, @JsonProperty("lead") @Nullable String lead, @JsonProperty("description") @Nullable String description) {
		this.name = name;
		this.id = id;
		this.lead = lead;
		this.description = description;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getLead() {
		return lead;
	}

	public String getDescription() {
		return description;
	}

	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	public boolean equals(Object o) {
		return EqualsBuilder.reflectionEquals(this, o);
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
