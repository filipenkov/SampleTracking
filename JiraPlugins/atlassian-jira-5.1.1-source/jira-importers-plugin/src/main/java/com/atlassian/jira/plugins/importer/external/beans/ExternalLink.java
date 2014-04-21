/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.external.beans;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

public class ExternalLink {
	/** for parent-child links - source is the child, destination is the parent */
	public static final String SUB_TASK_LINK = "sub-task-link";

	private final String name;
	private final String sourceId;
	private final String destinationId;

    @JsonCreator
	public ExternalLink(@JsonProperty("name") String name, @JsonProperty("sourceId") String sourceId, @JsonProperty("destinationId") String destinationId) {
		this.name = name;
		this.sourceId = sourceId;
		this.destinationId = destinationId;
	}

	/**
	 * Returns the "link name" of this link's link type.
	 * This is used for imports from non-JIRA systems where we just have a link name.
	 * The import will later find or create an appropriate "link type" for this name.
	 * An import from a JIRA backup would use LinkType instead.
	 *
	 * @return the "link name" of this link's link type.
	 */
	public String getName() {
		return name;
	}

	public String getSourceId() {
		return sourceId;
	}

	public String getDestinationId() {
		return destinationId;
	}

	public boolean isSubtask() {
		return SUB_TASK_LINK.equals(name);
	}
}
