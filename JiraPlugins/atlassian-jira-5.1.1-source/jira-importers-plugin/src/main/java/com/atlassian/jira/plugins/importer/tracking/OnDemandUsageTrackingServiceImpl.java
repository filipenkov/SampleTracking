package com.atlassian.jira.plugins.importer.tracking;

import com.atlassian.plugin.webresource.WebResourceManager;

public final class OnDemandUsageTrackingServiceImpl implements UsageTrackingService {
	private final WebResourceManager webResourceManager;

	public OnDemandUsageTrackingServiceImpl(WebResourceManager webResourceManager) {
		this.webResourceManager = webResourceManager;
	}

	@Override
    public void includeTrackingWhenActive() {
        webResourceManager.requireResource(UsageTrackingServiceImpl.GA_RESOURCE);
	}

	@Override
    public synchronized boolean isActive() {
		return true;
	}

	@Override
    public boolean isTrackingStatusDefined() {
		return true;
	}

	@Override
    public synchronized void activate() {
        throw new UnsupportedOperationException();
	}

	@Override
    public synchronized void deactivate() {
        throw new UnsupportedOperationException();
	}
}
