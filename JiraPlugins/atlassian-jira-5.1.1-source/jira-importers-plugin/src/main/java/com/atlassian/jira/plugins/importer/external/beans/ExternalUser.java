/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.external.beans;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonIgnore;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

public class ExternalUser implements NamedExternalObject {
	private String id;
	private String name;
	private String fullname;
	private String email;
	private final Set<String> groups = new HashSet<String>();
	private final Multimap<String, String> projectRoles = HashMultimap.create();
	private boolean active = true;

	public ExternalUser() {
	}

	public ExternalUser(ExternalUser user) {
		this.id = user.id;
		this.name = user.name;
		this.fullname = user.fullname;
		this.email = user.email;
		groups.addAll(user.groups);
		this.active = user.active;
		this.projectRoles.putAll(user.projectRoles);
	}

	public ExternalUser(String name, String fullname) {
		this.name = name;
		this.fullname = fullname;
	}

	public ExternalUser(String name, String fullname, String email) {
		this.name = name;
		this.fullname = fullname;
		this.email = email;
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

	//------------------------------------------------------------------------------------------------------------------
	// Getters and Setters
	//------------------------------------------------------------------------------------------------------------------

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFullname() {
		return fullname;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public Set<String> getGroups() {
		return groups;
	}

	public void setGroups(Set<String> groups) {
		this.groups.clear();
		this.groups.addAll(groups);
	}

    @JsonIgnore
	public void addRole(String project, String role) {
		projectRoles.put(project, role);
	}

    @JsonIgnore
	public Multimap<String, String> getProjectRoles() {
		return Multimaps.unmodifiableMultimap(projectRoles);
	}

    @JsonIgnore
	public void setProjectRoles(Multimap<String, String> projectRoles) {
		this.projectRoles.clear();
		this.projectRoles.putAll(projectRoles);
	}

    @JsonIgnore
    @Nullable
    public String getPassword() {
        return null;
    }
}
