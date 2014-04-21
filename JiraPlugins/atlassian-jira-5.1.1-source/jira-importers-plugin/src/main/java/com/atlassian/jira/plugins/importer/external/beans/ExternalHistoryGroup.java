package com.atlassian.jira.plugins.importer.external.beans;

import com.google.common.collect.ImmutableList;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.List;

public class ExternalHistoryGroup {
	private final String author;
	private final DateTime created;
	private final List<ExternalHistoryItem> items;

	@JsonCreator
	public ExternalHistoryGroup(@JsonProperty("author") String author, @JsonProperty("created") DateTime created,
			@JsonProperty("items") Collection<ExternalHistoryItem> items) {
		this.author = author;
		this.created = created;
		this.items = ImmutableList.copyOf(items);
	}

	public String getAuthor() {
		return author;
	}

	public DateTime getCreated() {
		return created;
	}

	public List<ExternalHistoryItem> getItems() {
		return items;
	}
}
