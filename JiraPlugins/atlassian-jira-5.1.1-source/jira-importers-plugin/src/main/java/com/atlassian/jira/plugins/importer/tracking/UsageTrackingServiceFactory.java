package com.atlassian.jira.plugins.importer.tracking;

import com.atlassian.jira.config.CoreFeatures;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.plugin.webresource.WebResourceManager;

public class UsageTrackingServiceFactory {

    private final FeatureManager featureManager;
    private final WebResourceManager webResourceManager;

    public UsageTrackingServiceFactory(FeatureManager featureManager, WebResourceManager webResourceManager) {
        this.featureManager = featureManager;
        this.webResourceManager = webResourceManager;
    }

    public UsageTrackingService create() {
        if (featureManager.isEnabled(CoreFeatures.ON_DEMAND)) {
            return new OnDemandUsageTrackingServiceImpl(webResourceManager);
        } else {
            return new UsageTrackingServiceImpl(webResourceManager);
        }
    }

}
