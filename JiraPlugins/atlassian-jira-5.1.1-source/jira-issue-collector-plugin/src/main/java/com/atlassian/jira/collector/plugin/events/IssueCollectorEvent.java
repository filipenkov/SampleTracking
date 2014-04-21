package com.atlassian.jira.collector.plugin.events;

import com.atlassian.analytics.api.annotations.Analytics;

@Analytics("issuecollector")
public class IssueCollectorEvent {
	private final String collectorId;
	private final String templateType;
	private final boolean reporterMatching;
	private final boolean collectBrowserInfo;

	public IssueCollectorEvent(String collectorId, String templateType, boolean reporterMatching, boolean collectBrowserInfo) {
		this.collectorId = collectorId;
		this.templateType = templateType;
		this.reporterMatching = reporterMatching;
		this.collectBrowserInfo = collectBrowserInfo;
	}

	public String getCollectorId() {
		return collectorId;
	}

	public String getTemplateType() {
		return templateType;
	}

	public boolean isReporterMatching() {
		return reporterMatching;
	}

	public boolean isCollectBrowserInfo() {
		return collectBrowserInfo;
	}
}
