package com.atlassian.jira.collector.plugin.components;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.collector.plugin.events.IssueCollectorCreatedEvent;
import com.atlassian.jira.collector.plugin.events.IssueCollectorDeletedEvent;
import com.atlassian.jira.collector.plugin.events.IssueCollectorDisabledEvent;
import com.atlassian.jira.collector.plugin.events.IssueCollectorEnabledEvent;
import com.atlassian.jira.collector.plugin.events.IssueCollectorIssueSubmittedEvent;

public class IssueCollectorEventDispatcher {
	private final EventPublisher eventPublisher;

	public IssueCollectorEventDispatcher(EventPublisher eventPublisher) {
		this.eventPublisher = eventPublisher;
	}

	public void collectorCreated(Collector collector) {
		eventPublisher.publish(new IssueCollectorCreatedEvent(collector.getId(), collector.getTemplate().getId(),
				collector.isUseCredentials(), collector.isRecordWebInfo()));
	}

	public void collectorDeleted(Collector collector) {
		eventPublisher.publish(new IssueCollectorDeletedEvent(collector.getId(), collector.getTemplate().getId(),
				collector.isUseCredentials(), collector.isRecordWebInfo()));
	}

	public void collectorEnabled(Collector collector) {
		eventPublisher.publish(new IssueCollectorEnabledEvent(collector.getId(), collector.getTemplate().getId(),
				collector.isUseCredentials(), collector.isRecordWebInfo()));
	}

	public void collectorDisabled(Collector collector) {
		eventPublisher.publish(new IssueCollectorDisabledEvent(collector.getId(), collector.getTemplate().getId(),
				collector.isUseCredentials(), collector.isRecordWebInfo()));
	}

	public void issueSubmitted(Collector collector) {
		eventPublisher.publish(new IssueCollectorIssueSubmittedEvent(collector.getId(), collector.getTemplate().getId(),
				collector.isUseCredentials(), collector.isRecordWebInfo()));
	}

}
