/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.importer.impl;

import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.project.version.Version;
import org.apache.commons.collections.map.MultiKeyMap;

public class ImportObjectIdMappings {
	private final MultiKeyMap versionMappings = new MultiKeyMap();
	private final MultiKeyMap componentMappings = new MultiKeyMap();

	public void addVersionMapping(final String externalProject, final String externalVersion, final Version version) {
		versionMappings.put(externalProject, externalVersion, version);
	}

	public Version getVersion(final String externalProject, final String externalVersion) {
		return (Version) versionMappings.get(externalProject, externalVersion);
	}

	public void addComponentMapping(final String externalProject, final String externalComponent, final ProjectComponent component) {
		componentMappings.put(externalProject, externalComponent, component);
	}

	public ProjectComponent getComponent(final String externalProject, final String externalComponent) {
		return (ProjectComponent) componentMappings.get(externalProject, externalComponent);
	}
}
