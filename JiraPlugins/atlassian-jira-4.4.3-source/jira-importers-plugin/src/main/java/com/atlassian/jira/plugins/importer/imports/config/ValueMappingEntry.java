/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.imports.config;

public class ValueMappingEntry {

	private final String name;
	private final String id;

	public ValueMappingEntry(String name, String id) {
		this.name = name;
		this.id = id;
	}

	public ValueMappingEntry(String name, Integer id) {
		this(name, id.toString());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ValueMappingEntry that = (ValueMappingEntry) o;

		if (!id.equals(that.id)) return false;
		if (!name.equals(that.name)) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = name.hashCode();
		result = 31 * result + id.hashCode();
		return result;
	}

	public String getName() {
		return name;
	}

	public String getId() {
		return id;
	}
}
